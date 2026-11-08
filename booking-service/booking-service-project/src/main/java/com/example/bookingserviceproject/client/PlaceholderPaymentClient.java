package com.example.bookingserviceproject.client;

import com.example.bookingserviceproject.dto.payment.PaymentResult;
import com.example.bookingserviceproject.dto.request.CardDetails;
import com.example.bookingserviceproject.exception.PassThroughException;
import com.example.bookingserviceproject.exception.PaymentUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stands in for the payment-service until it exists. Behaves like it, without
 * charging anything: checks the card, decides the outcome by test card number
 * (like real payment providers' test cards), and never charges the same
 * booking twice. Never logs card data.
 * <p>
 * The retry and circuit breaker are set in application.properties
 * ({@code resilience4j.*.instances.payment}). They only work when this class
 * is called from another class, as Spring wraps it in a proxy.
 */
@Component
public class PlaceholderPaymentClient implements PaymentClient {
    private static final Logger log = LoggerFactory.getLogger(PlaceholderPaymentClient.class);

    // Test card numbers with an outcome other than success
    private static final String DECLINED = "4000000000000002";
    private static final String INSUFFICIENT_FUNDS = "4000000000009995";
    private static final String TIMES_OUT = "4000000000000119";

    // Successful payments per bookingId. The real payment-service keeps them in its database
    private final Map<UUID, PaymentResult> succeeded = new ConcurrentHashMap<>();

    @Override
    @CircuitBreaker(name = "payment")
    @Retry(name = "payment")
    public PaymentResult charge(UUID userId, UUID bookingId, int amount, String currency, CardDetails card){
        PaymentResult earlier = succeeded.get(bookingId);
        if (earlier != null){
            log.info("Booking {} was already charged (payment {}): no new charge", bookingId, earlier.paymentId());
            return earlier;
        }

        // American Express (34/37) has a 4-digit security code, other brands 3
        boolean amex = card.number().startsWith("34") || card.number().startsWith("37");
        if (card.cvc().length() != (amex ? 4 : 3)){
            throw new PassThroughException(HttpStatus.BAD_REQUEST, "The security code does not match the card");
        }

        switch (card.number()){
            case DECLINED -> throw declined(bookingId, "Your card was declined");
            case INSUFFICIENT_FUNDS -> throw declined(bookingId, "Your card has insufficient funds");
            case TIMES_OUT -> {
                log.warn("Payment for booking {} timed out (simulated)", bookingId);
                throw new PaymentUnavailableException("The payment-service did not answer in time (simulated)");
            }
            default -> {}
        }

        PaymentResult payment = new PaymentResult(UUID.randomUUID());
        succeeded.put(bookingId, payment);
        log.info("Charged {} {} for booking {} with card ending {} (payment {})",
                amount, currency, bookingId, card.last4(), payment.paymentId());
        return payment;
    }

    @Override
    public void refundFailedBooking(UUID bookingId){
        PaymentResult payment = succeeded.remove(bookingId);
        log.info("Refunded booking {} in full (payment {})", bookingId, payment == null ? "none" : payment.paymentId());
    }

    /**
     * @param bookingId the booking that was not charged
     * @param message   why, returned to the client
     * @return a 402 refusal
     */
    private PassThroughException declined(UUID bookingId, String message){
        log.info("Payment for booking {} declined: {}", bookingId, message);
        return new PassThroughException(HttpStatus.PAYMENT_REQUIRED, message);
    }
}
