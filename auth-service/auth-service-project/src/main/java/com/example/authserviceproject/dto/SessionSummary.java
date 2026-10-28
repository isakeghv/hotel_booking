package com.example.authserviceproject.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Summary of a single user session.
 *
 * @param sessionId the unique id of the session
 * @param current true if this is the session making the request
 * @param issuedAt the date and time the session was issued
 */
public record SessionSummary(UUID sessionId, boolean current, LocalDateTime issuedAt) {}
