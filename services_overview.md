# Services overview

Document contains a general, early on overview of what the system as a whole will contain. Each table contain content that is relevant to each other, for example interconnected to perform a complete task.

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>1</td>
    <td>Inventory</td>
    <td>Find available rooms at specific dates</td>
    <td>Finds available room types at specific dates</td>
    <td>Unavailable room-types are omitted</td>
    <td>-</td>
    <td>cache-aside</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>2</td>
    <td>Booking</td>
    <td>Find bookings of a user</td>
    <td>Finds and returns a users bookings</td>
    <td>Must only return the users bookings</td>
    <td>-</td>
    <td>No caching</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>3</td>
    <td>Inventory</td>
    <td>Retrieve information about a specific room-type</td>
    <td>Finds and returns information about a specific room-type in DB</td>
    <td>Must use room_type_id to identify the room-type</td>
    <td>-</td>
    <td>cache-aside</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>5</td>
    <td>Booking</td>
    <td>Initiating a booking process, lock room in booking and inventory cache</td>
    <td>It initiates the booking-process, locking the room as unavailable in the inventory cache, and locking it as "started" in the booking cache</td>
    <td>Must be possible to identify the cache-locked content by the user. Should not rely on id's returned by this endpoint. Cached data must expire after a period of time. Must be possible to book multiple rooms in one booking</td>
    <td>Inventory</td>
    <td>write-back</td>
  </tr>
  <tr>
    <td>5</td>
    <td>Booking</td>
    <td>Conduct payment and store booking in relevant db's</td>
    <td>When receiving payment information, fetch content from cache regarding booking, complete the payment and store booking. Stores content in db for booking, inventory and payment. </td>
    <td>Must find content from cache to continue with payment. if content is not found in cache, then the booking should not be possible. Each booking contains one or more item-entries for each room included</td>
    <td>Inventory, payment, notification</td>
    <td>read, flush to db</td>
  </tr>
  <tr>
    <td>5</td>
    <td>Inventory</td>
    <td>Lock a room in cache</td>
    <td>Receives information from booking-service regarding which room-type to make unavailable in cache</td>
    <td>Must be possible to identify the cached content by user_id or similar; Not rely on data submitted by client (except auth token)</td>
    <td>Booking</td>
    <td>write-back</td>
  </tr>
  <tr>
    <td>5</td>
    <td>Inventory</td>
    <td>Flush from cache to DB</td>
    <td>Flush the temp cache-locked content for a booking to the inventory DB</td>
    <td>Must inform of if it failed</td>
    <td>Booking</td>
    <td>Flush, delete</td>
  </tr>
  <tr>
    <td>5</td>
    <td>Inventory</td>
    <td>Delete from cache</td>
    <td>Delete a temp cache-locked room from the inventory cache as a fallback if other processes during payment failed in other microservice</td>
    <td>Must inform of if it failed</td>
    <td>Booking</td>
    <td>Delete</td>
  </tr>
  <tr>
    <td>5</td>
    <td>Payment</td>
    <td>Conduct payment and store booking in relevant db's</td>
    <td>Receive payment-information from booking-service, and conduct payment</td>
    <td>Must inform of if it failed. Must lock the payment to prevent it from being duplicate payments</td>
    <td>Booking</td>
    <td>Write</td>
  </tr>
  <tr>
    <td>5, 9</td>
    <td>Notification</td>
    <td>Send confirmation email</td>
    <td>Send confirmation email when a booking is completed</td>
    <td>Must send email containing information specific to the booking</td>
    <td>Booking</td>
    <td>None</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>6</td>
    <td>Auth</td>
    <td>Endpoint to create new user in auth-db</td>
    <td>Creates a user_id, and a user-entry in auth-db. Contacts user-service and provides the user_id, along with user-information</td>
    <td>Must inform if it fails. Should include in auth-db before contacting user-service. Remove from auth-db if failed-response from user-service</td>
    <td>User</td>
    <td>None</td>
  </tr>
  <tr>
    <td>6</td>
    <td>User</td>
    <td>Endpoint to create a new user in user-db</td>
    <td>Creates a user-entry in user-db, and return response to auth-service (success/fail). It might be relevant to use an event instead of API</td>
    <td>Must inform if it fails so that auth-db can react relatively to that</td>
    <td>Auth</td>
    <td>None</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>7</td>
    <td>Auth</td>
    <td>Sign into account - create a session in session_table in auth-db</td>
    <td>Find user in db by email, check if password corresponds, and that the user is active. If true, create a new refresh-token and access-token</td>
    <td>Must create session in db</td>
    <td></td>
    <td>None</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>7</td>
    <td>Auth</td>
    <td>Sign out of account - invalidate/delete a session in session_table in auth-db</td>
    <td>Find session in session_table by using session_id and user_id from access-token and invalidate/remove from db</td>
    <td>Cannot find session by just session_id or user_id; Must be both</td>
    <td></td>
    <td>None</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>7</td>
    <td>Auth</td>
    <td>Provide new access-token</td>
    <td>Uses refresh-token to find session in session_table and before creating new access-token</td>
    <td>Cannot find session by just session_id or user_id; Must be both</td>
    <td></td>
    <td>None</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>8</td>
    <td>Auth</td>
    <td>Invalidate/Delete all signed in sessions of a user in session_table</td>
    <td>Sign out a user of all their sessions, including the one they are current signed into</td>
    <td>Must first validate the user_id and session_id as valid in the db before signing out: To prevent invalidated tokens from being used to sign out other sessions</td>
    <td></td>
    <td>None</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>10</td>
    <td>Booking</td>
    <td>Find and return bookings on specific date</td>
    <td>For the employee to be able to view booking-information on a selected date</td>
    <td>Should use search query for date (format: dd-mm-yyyy), and for page-selection to enable pagination. Might be a good idea to include query for showing cancelled bookings as an option</td>
    <td>User</td>
    <td>cache-aside</td>
  </tr>
  <tr>
    <td>11</td>
    <td>Booking</td>
    <td>Find and return information about a specific booking</td>
    <td>For the employee to get information about a specific booking, such as user, rooms, from-to-dates, what the booking includes etc</td>
    <td>Must provide enough information for employee to view all necessary information about the rooms, the booking and the user. Must return user-id to also be able to view the users profile. the id of the booking should be a search param</td>
    <td>User</td>
    <td>cache-aside</td>
  </tr>
  <tr>
    <td>12</td>
    <td>Booking</td>
    <td>Find and return a customers bookings</td>
    <td>Similar to functionality for user-story 2, but for the employee to view instead. Instead of relying on the auth-token for user_id, it should be in the search params</td>
    <td>Must provide basic information about the booking to view some details before opening the specific booking. Must include id's of bookings to use them for later fetching specific bookings</td>
    <td>User</td>
    <td>cache-aside</td>
  </tr>
  <tr>
    <td>10, 11, 12</td>
    <td>User</td>
    <td>Return the name of a user</td>
    <td>For when returning booking-information of a user, to provide booking-service with name of the user who owns the booking</td>
    <td>Should use user_id to get the users name</td>
    <td>Booking</td>
    <td>cache-aside</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>13</td>
    <td>User</td>
    <td>Find a user by name, email or phone number</td>
    <td>Finds user by name, email and/or phone number in the DB and return for employee to view</td>
    <td>Should return all users found regardless of what type of input was used. if using method GET, use search queries to specify email/firstname/lastname/phone, if using query, use received json body</td>
    <td>-</td>
    <td>cache-aside</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>14</td>
    <td>Inventory</td>
    <td>Get room availability for other selected dates</td>
    <td>Uses the received room_id's to find room_type and then returns the availability of different the room-types included in this specific booking</td>
    <td>Should return simple data: are the room-types (and amount) of this booking available at the selected dates? If the room-types are available: Temp cache them to lock them as unavailable for a period of time. Must be identifiable for later (for example using booking_id as key), so the cache can be found and be used/modified. if second request for other date, it should update the existing cache and renew time. ID's of rooms on the new date should be stored in cache</td>
    <td>Booking</td>
    <td>cache-aside (i think)</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Booking</td>
    <td>Cache the potential new booking-date</td>
    <td>Receive msg from inventory of other dates to cache-lock rooms for: To disable others from booking those rooms, and to use later to flush from cache to db. </td>
    <td>Should cache-lock the bookings to dates, to disable others from booking them, and enable service to flush from cache to db when booking is saved. Must be identifiable by booking_id or similar. Must be able to modify dates in cache-lock for if selected dates is changed. Might need to check db before locking with cache, meaning that caching might have to be cache-aside. Must receive id's (and date) of rooms to lock in cache from inventory.</td>
    <td>Inventory</td>
    <td>write-back</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Booking</td>
    <td>Flush from cache to DB</td>
    <td>Flushes from cache to DB to change booking (date) and the rooms included (room_id in each db-item)</td>
    <td>Must find cached content to be able to make change. Should not be able to make change without fetching content from cache (acting as a safe-guard). Must ensure previous content in db is modified (both room_id and date). contents must be removed from cache.</td>
    <td>Inventory</td>
    <td>flush, delete</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Inventory</td>
    <td>Flush from cache to DB</td>
    <td>Flushes from cache to db, to set that new rooms are not available. Has to select which rooms to make unavailable, and which to reset to available. Has to contact booking-service to trigger the update there as well.</td>
    <td>Must find cached content to be able to make change. Should not be able to make change without fetching content from cache (acting as a safe-guard). Must set previous rooms/date as available in db. Should receive relevant id's from booking for identifying content from cache</td>
    <td>Booking</td>
    <td>Flush, delete</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Inventory</td>
    <td>Find available rooms and return to employee</td>
    <td>Finds available rooms for same date and returns. Might be good idea to include query for type of room to search for: and set default to "all" types of rooms. Good idea to not return all data, instead use pagination</td>
    <td>Must check for whole booking-period, e.g. must receive dates with request (as query)</td>
    <td>Inventory</td>
    <td>comparative filtering (check db, compare with cache)</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Booking</td>
    <td>Lock a room as unavailable in booking cache</td>
    <td>Gets new room_id, existing item_id and booking_id with request, locks the content in cache (distributed lock), and contacts inventory to do the same. Locking with cache, to enable employee to make change multiple rooms in one go: for example: Lock 3 rooms in cache 1 by 1, and later "save" all changes, allowing all 3 to be updated with one request to the microservice - Might be better to completely drop cache-lock, and simply do the re-booking one rooms 1 by 1 (consider it)</td>
    <td>Must lock in cache so it cannot be booked by someone else. Must be possible to identify the specific locked room/item and booking in cache for using to flush later.</td>
    <td>Inventory</td>
    <td>write-back (Distributed lock)</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Inventory</td>
    <td>Lock a room as unavailable in inventory cache</td>
    <td>Gets new room_id, existing room_id, new dates, and occupied_id with request, locks the content in cache (distributed lock), enabling it to be either modified, removed, or flushed later</td>
    <td>Must receive and cache enough content for it to be possible to identify the specific item in the cache later. Must check if already existing in cache, and update cache instead of storing new if necessary.</td>
    <td>Booking</td>
    <td>write-back (Distributed lock)</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Booking</td>
    <td>Flush from cache and update db</td>
    <td>Find cache-locked change in cache and flush to db. Contact inventory-db to do the same. Ensure old db data is modified to represent new room-booking. Ensure cache does not still have room-lock</td>
    <td>Must receive enough information in request to find cache-locked data and flush from cache to db. Must ensure no data is left in cache and that all related db-data is updated. Must be able to handle more room-changes in single request</td>
    <td>Inventory</td>
    <td>read, flush to db</td>
  </tr>
  <tr>
    <td>14</td>
    <td>Inventory</td>
    <td>Flush from cache to db, update room-availability</td>
    <td>Find cache-locked change in cache and flush to db. Ensure old db data is modified to represent new room-booking. Ensure cache does not still have room-lock. Previous room must be made available again</td>
    <td>Must receive enough information in request to find cache-locked data and flush from cache to db. Must ensure no data is left in cache and that all related db-data is updated. Must update the previous room to be available again. Must be able to handle more room-changes in single request</td>
    <td>Booking</td>
    <td>Read, flush to db</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>15</td>
    <td>Auth</td>
    <td>Create employee account</td>
    <td>Creates account for employee, receives permission-level, and sends info to user-service it to be created there as well</td>
    <td>Must receive all information necessary in json body to create account and set permission level. Cannot create owner-account, as there should only be one owner. Email domain must match the hotel's selected domains. Only managers and owners can create new accounts</td>
    <td>user, notification</td>
    <td>-</td>
  </tr>
  <tr>
    <td>15</td>
    <td>User</td>
    <td>Create employee account in user-db</td>
    <td>Receives information from auth-service</td>
    <td>Receives all information necessary from auth-service to create the account. </td>
    <td>Auth</td>
    <td>-</td>
  </tr>
  <tr>
    <td>15</td>
    <td>Notification</td>
    <td>sends notification to employee account with confirmation of account-creation</td>
    <td>Receives information from auth-service</td>
    <td>Receives email and information from auth-service</td>
    <td>Auth</td>
    <td>-</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>15</td>
    <td>Auth</td>
    <td>Delete an account (set "is_active" to false)</td>
    <td>Receive user_id of a valid employee/manager/user account with request</td>
    <td>Employee's cannot delete other accounts. Owner can delete all accounts (except their own), manager can delete an employees account, not managers. Auth-service must authorize permission for account-deletion before contacting user-service. Deletion is done by setting "is_active" to false</td>
    <td>User</td>
    <td>-</td>
  </tr>
  <tr>
    <td>15</td>
    <td>User</td>
    <td>Delete an account (set "is_active" to false)</td>
    <td>Receive user_id of a valid employee/manager/user account with request</td>
    <td>Must not be contacted by auth-service before auth-service has verified that the current user is authorized to delete the account. Deletion is done by setting "is_active" to false</td>
    <td>Auth</td>
    <td>-</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>16</td>
    <td>Auth</td>
    <td>Edit permission level of an employee account</td>
    <td>Receives user-id of employee and new permission-level with request</td>
    <td>Must receive user_id of an employee, e.g. permission_level cannot be 0, must be 1 or 2. Cannot edit from 2 to 3. Normal employees cannot modify another employees/managers/owners account. An owner can modify all, a manager can modify an employees account</td>
    <td>-</td>
    <td>-</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>Other</td>
    <td>Inventory</td>
    <td>Allow manager to create room options that customers can book</td>
    <td>Receive id's of room-types, descriptions, and accomodations that should be made into bookable rooms, and amount of them that should be made</td>
    <td>Should not be possible to create more bookable rooms containing a room-type than total amount of that room-type. Other limits also apply. Minimum level: manager</td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <td>Other</td>
    <td>Inventory</td>
    <td>Allow manager to get list of rooms, accomodations and other relevant data necessary to create room-options</td>
    <td>Should return arrays of objects containing content used to create room-options</td>
    <td>Should not be possible to create more bookable rooms containing a room-type than total amount of that room-type. Other limits also apply. Minimum permission level: manager</td>
    <td></td>
    <td></td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>Other</td>
    <td>Inventory</td>
    <td>Allow manager to delete a room-option, so it is no longer bookable (set "is_active" to false)</td>
    <td>should only be possible for manager to do so</td>
    <td>Minimum permission level: manager. It is important that deletion is done by setting "is_active" to false</td>
    <td></td>
    <td></td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>Other</td>
    <td>Inventory</td>
    <td>Set a specific room as active/inactive</td>
    <td>Set a specific room as active/inactive, preventing/allowing booking of the room</td>
    <td>Minimum permission level: manager</td>
    <td></td>
    <td></td>
  </tr>
  <tr>
    <td>Other</td>
    <td>Inventory</td>
    <td>Set room-type as (non-)bookable</td>
    <td>Set if a room is active/inactive, preventing/allowing booking of the room-type</td>
    <td>Minimum permission level: manager</td>
    <td></td>
    <td></td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>Other</td>
    <td>Inventory</td>
    <td>Provide user with addon-options (accomodations)</td>
    <td>Provide user with options for adding things when booking is being done, so that the user for example can add a breakfast if not included in room-type</td>
    <td>A booking tied to the user must be locked for options to be returned, should not simply provide options if there is no relevant locked booking. should be available in inventory-cache</td>
    <td></td>
    <td>cache-aside</td>
  </tr>
</table>

<table>
  <tr>
    <th>User story</th>
    <th>Service</th>
    <th>Task</th>
    <th>Notes</th>
    <th>Requirements</th>
    <th>Other services</th>
    <th>initial caching idea</th>
  </tr>
  <tr>
    <td>Other</td>
    <td>Booking</td>
    <td>Add addons to a booking currently being made (pre-payment)</td>
    <td>Allow users to include addons to their bookings, for example breakfast etc if they are booking an alternative that does not include it. This one refers to the ones done during the booking of the room, not after</td>
    <td>A room-booking must be locked for it to be possible to do this, it should not be possible to select an addon-to a booking that is not currently being made</td>
    <td>inventory</td>
    <td>-</td>
  </tr>
  <tr>
    <td>Other</td>
    <td>Inventory</td>
    <td>Verify that addon exists in inventory</td>
    <td>Verify that the addon exists in the inventory-db and is possible to book as an addon-option. Used when adding accomodations to a booking (pre-payment), to ensure that it both exists, and that it is allowed to add</td>
    <td>Only available on the internal network, must receive id of accomodation and returns only boolean of if its possible to add or not</td>
    <td>Booking</td>
    <td>cache-aside</td>
  </tr>
</table>