# Info
In this document, we specify user-stories scope what the application should be able to do.
For each user-story, we specify what it entails to be able to map out what aspects and requirements has to be considered/fulfilled.

# User stories
1. As a user, I want to be able to find available rooms at specific dates.
2. As a user, I want to be able to view my future and previous bookings.
3. As a user, I want to be able to view information about the room im going to book.
4. As a user, I want to be able to modify an existing booking.
5. As a user, I want to be able to book one or more rooms at a selected date(s).
6. As a user, I want to be able to create an account.
7. As a user, I want to be able to sign in an out of my account.
8. As a user, I want to be able to sign out of all sessions.
9. As a user, I want to receive a confirmation email when a booking has been made.
10. As a hotel employee, I want to be able to view bookings on different dates.
11. As a hotel employee, I want to be able to view details about a specific booking.
12. As a hotel employee, I want to view the bookings of different customers/users.
13. As a hotel employee, I want to be able to find users by name.
14. As a hotel employee, I want to be able to modify or cancel bookings.
15. As a hotel manager, I want to be able to create new accounts for employees/managers.
16. As a hotel manager, I want to be able to manage user- and employee-accounts.
17. As a hotel manager, I want to allow people to pay before a booking is created.

## Specifications

**1) As a user, I want to be able to find available rooms at specific dates**<br>
The inventory database requires four tables:
| Table name | content |
| --- | --- |
| room_type_table | For room types and their information |
| room_number_table | For individual rooms each linked to a room type |
| occupied_table | For specifying when rooms are not available, linked to a room and date range. |
| images_table | For storing image-paths linked to room type. |

Search requires a from- and to-date. The response must omit unavailable room types entirely. Each room type in the response must include an image, the number of people it accommodates, and the count of available rooms of that type. If the user is viewing a specific room, they should be provided with more images of the room, meaning that the images_table also needs to specify which image is the main-image to display per room.

**2) As a user, I want to be able to view my future and previous bookings**<br>
The booking database requires two tables. Table 1 contains:

| Table 1 | Table 2 |
| --- | --- |
| booking_id (PK) | item_id (PK) |
| user_id (FK) | booking_id (FK) |
| start_date (Date) | room_id (FK) |
| end_date (Date) | room_type_id (FK) |
| price (Int) | checked_in (boolean) |
| completed (boolean) |  |
| cancelled (boolean) |  |

Table 1 has one row per booking. Table 2 can have multiple rows per booking. The response must split content into previous and upcoming bookings. Due to user story 14, the table also needs to have “cancelled”

**3) As a user, I want to be able to view information about the room I am going to book**<br>
Information is as specified in user story 1.

**4) As a user, I want to be able to modify an existing booking**<br>
Out of scope for this project.

**5) As a user, I want to be able to book one or more rooms at selected dates**<br>
Database structure is as specified in user story 2.

**6) As a user, I want to be able to create an account**<br>
Users are stored across two separate databases for separation of concerns: a user database and an auth database, each with its own service.

| user-db | auth-db user-table | auth-db session-table |
| --- | --- | --- |
| user_id (PK) | user_id (PK from user-db) | session_id (PK) |
| first_name (string) | email (email) | user_id (FK) |
| last_name (string) | password (hashed string) | issued_at (Date-time) |
| country_code (int: 00XX) | permission_level (int: 0-3) | expires_at (Date-time) |
| phone_number (int) |  |  |

Email is stored in the auth database as it is part of the authentication identity. The auth service is responsible only for authentication and authorization, not user details: user details are the responsibility of the user service. Though the system does not provide options for modifying user information etc, using a separate user-service, and including user-related information there would allow for that to be done while also respecting the separation of concerns.

**7) As a user, I want to be able to sign in and out of my account**<br>
On sign-in, a refresh token is created and set as an HTTP-only cookie. It must contain: user_id, session_id, expiration, issuer, and it must be signed. 

The session is also stored in the session table with an expiry date. The cookie must be scoped to only the necessary paths, and set to secure in production but not in development. An access token is also created containing user_id and session_id. It must be signed and be short-lived (5–15 minutes). On sign-out, the cookie is removed and the session is invalidated and removed from the session table.

**8) As a user, I want to be able to sign out of all sessions**<br>
An endpoint that uses the user_id to find all active sessions in the session table and invalidates them all.

**9) As a user, I want to receive a confirmation email when a booking has been made**<br>
The notification service connects to an SMTP server or email account using stored credentials to send emails. The email format and content are managed by the notification service. The booking service provides the notification service with the booking-specific content to include in the email.

**10) As a hotel employee, I want to be able to view bookings on different dates**<br>
An endpoint that returns bookings where the booking period starts, ends, or falls within the selected dates. The response includes booking_id, the names of the persons associated with each booking, and the start and end dates of each booking. Pagination is required and must be applied at the database query level, not server-side, to avoid inconsistent results. The response must include the total number of available pages. This endpoint is accessible only to hotel employees.

**11) As a hotel employee, I want to be able to view details about a specific booking**<br>
An endpoint that returns detailed information about a specific booking by booking_id. It must return start and end date, room type(s), room numbers, checked-in status, the person who made the booking, their contact information and user_id, and any information necessary to identify each room and to perform modifications on the booking or its rooms (such as room_id). Accessible only to hotel employees.


**12) As a hotel employee, I want to view the bookings of different customers**<br>
An endpoint that allows identifying users by email, phone number, or name. When searching by name, multiple users may be returned. Regardless of the search method or number of results, the response always returns an array. Employees must not be included in results. Each result contains sufficient information to differentiate users, including user_id, name, and other basic relevant information.

A separate endpoint takes a user_id and returns an array of that user's bookings, each containing basic booking information: date, room type, and room_id. The room_id can then be used with the endpoint from user story 11 to retrieve more detailed information.

**13) As a hotel employee, I want to be able to find users by name**<br>
Covered by the first paragraph of user story 12.

**14) As a hotel employee, I want to be able to modify or cancel bookings**<br>
Two separate endpoints are required: one for modifications and one for cancellations. Since bookings are paid on creation, cancellations must trigger a refund through the payment service where applicable. Refund data must be tracked in the payment database: payment records must include a refunded flag. A separate refunds table is also needed, linking each refund to a payment, along with other relevant data such as user_id. Since deleting a booking would leave refund data referencing nothing, bookings must also include a cancelled flag rather than being deleted. On cancellation, the room must be made available again in the inventory database. Cancellation is free only if requested more than 48 hours before the booking starts. If within 48 hours, the booking can still be cancelled but no refund is issued.

For modifications, a user may request a date change provided it is more than 48 hours before the booking starts. The new dates must cover the same duration: the length of stay cannot change. Before confirming, the system must check availability of the same room type on the new dates, and lock the necessary rooms to prevent double-booking during the modification. A user cannot change to a more expensive room type. An employee can change the room type on behalf of the hotel without any payment changes. All modifications must be reflected in both the booking and inventory databases. Cancellations must also trigger updates to the payment database.

The modification endpoint must support: 
- Changing room number/type, 
- Viewing other available rooms and their information, 
- Date changes (user-initiated, same duration, more than 48h prior), 
- Room type changes (employee-initiated only, no payment change required)

**15) As a hotel manager, I want to be able to create and delete accounts for employees and manager employees**<br>
Permission levels are split into four tiers represented by integers: customer (0), employee (1), manager (2), and owner (3). An owner has the same permissions as a manager, with the additional ability to create and delete manager accounts.

Creation requires only one endpoint, but must check the permission level of the requester. Deletion requires a separate endpoint. Attempting to create or delete a manager account requires permission level 3. Attempting to create or delete an employee account requires at least permission level 2. No one can create or delete an owner account. No one can create an owner account, as there can only be one.

**16) As a hotel manager, I want to be able to manage user and employee accounts**<br>
Similar to user story 15, but uses a patch or put operation to modify existing account information rather than creating or deleting accounts. The owner-account can be modified by the owner themself.

**17) As a hotel manager, I want people to pay before a booking is created**<br>
Before payment, the system locks the room in both the booking and inventory caches to prevent double-booking. Payment is processed first. Once payment is confirmed, it is recorded in the payment database. The booking and inventory caches are then flushed to their respective databases. Once complete, the notification service sends a confirmation email.

| Table name: | user_table |
| Column name  | Data type    |
| ------------ | ------------ |
| user_id      | PK, ObjectId |
| first_name   | String       |
| last_name    | String       |
| country_code | Int -> 0047  |
| phone_number | Int          |