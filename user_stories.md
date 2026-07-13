# Info
In this document, we specify user-stories scope what the application should be able to do.
For each user-story, we specify what it entails to be able to map out what aspects and requirements has to be considered/fulfilled.

# User stories
1. As a user, I want to be able to find available rooms at specific dates
2. As a user, I want to be able to view my future and previous bookings
3. As a user, I want to be able to view information about the room im going to book
4. As a user, I want to be able to modify an existing booking
5. As a user, I want to be able to book one or more rooms at a selected date(s)
6. As a user, I want to be able to create an account
7. As a user, I want to be able to sign in an out of my account
8. As a user, I want to be able to sign out of all sessions
9. As a user, I want to receive a confirmation email when a booking has been made
10. As a hotel employee, I want to be able to view bookings on different dates
11. As a hotel employee, I want to be able to view details about a specific booking
12. As a hotel employee, I want to view the bookings of different customers/users
13. As a hotel employee, I want to be able to find users by name
14. As a hotel employee, I want to be able to modify or cancel bookings
15. As a hotel manager, I want to be able to create new accounts for employees/managers.
16. As a hotel manager, I want to be able to manage user- and employee-accounts.
17. As a hotel manager, I want to allow people to pay before a booking is created

## Specifications
| **User story number** | **Spec** | **Spec** | **Spec** | **Spec** | **Spec** | **Spec** | **Spec** | **Spec** | **Spec** |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 1 | Searching by date (from-to) | Show types available rooms (quality) | Show amount of each room-type available | Omit unavailable room-types | Specify by amount size (for what amount of people) |  |  |  |  |
| 2 | Store bookings to user | Split displayed bookings into future/previous |  |  |  |  |  |  |  |
| 3 | Specify size and type of bed(s) | Specify air condition info | Specify bathroom details | Specify refundability | Specify if breakfast included | Specify about wifi | Specify roomservice | Specify balcony | Specify amount of people |
| 4 | Move to other date | Change type of room | Cancel a booking (refund if possible) |  |  |  |  |  |  |
| 5 | Single payment-record in payment-db for a booking | Able to make multiple rooms unavailable in inventory | booking-db must have more than one table, to specify the booking itself, and another to specify what it includes |  |  |  |  |  |  |
| 6 | Create new user instances in db | Store full name | Store email address | Store phone number | Store encrypted password |  |  |  |  |
| 7 | Able to manage sessions | Invalidate session_id's |  |  |  |  |  |  |  |
| 8 | Store a users sessions in db | Able to invalidate/remove sessions in db |  |  |  |  |  |  |  |
| 9 | Notification service is tied to a smtp server |  |  |  |  |  |  |  |  |
| 10 | Fetch all bookings on a date | Include booking_id for each booking | Include pagination to reduce amount displayed each time | Include max amount of paginations possible | Include name of persons for each booking | Specify start- and end-date of each booking |  |  |  |
| 11 | Able to select a specific booking from all bookings | Able to find booking by name | Show start- and end-date | Show room-type(s) | Show amount of people | Show room-number |  |  |  |
| 12 | Able to search bookings by name | Able to search bookings by email | Able to search bookings by phone number | Should show all bookings connected to a name/email/phone number |  |  |  |  |  |
| 13 | Users can be retrieved by first and/or last name | Include user_id of each user in data returned |Include name, email and phone number of each user |  |  |  |  |  |  |
| 14 | Edit a specific booking | Refund user if relevant (if allowed, and before expiry) | Change room number | Change room-type | View other available rooms (to know which other room to change into) | View information about other rooms | Cannot change to more expensive room on user-request | Can move to more expensive room if on behalf of hotel |  |
| 15 | Include name | Include email | Include permission levels | Include initial generated password | Not able to create user-accounts (for customers) | Send email to account with unique url to create new password | Account is not active before password has been created (flagged as inactive before password is reset) |  |  |
| 16 | Can update name | Can update phone number | Can reset password (sending email to user/employee for it to be reset) | Can update email | Can update permission level (not for user-accounts) | Can disable account (not delete, to not delete payment and booking-info) |  |  |  |
| 17 | Booking is not confirmed before payment has been done | Booking is not included in db before payment is done |  |  |  |  |  |  |  |