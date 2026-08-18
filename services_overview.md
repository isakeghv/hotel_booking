# Services overview

Document contains a general, early on overview of what the system as a whole will contain. Each table contain content that is relevant to each other, for example interconnected to perform a complete task.

<table>
  <tr>
    <th style="width:100px;">User story</th>
    <th style="width:100px;">Service</th>
    <th style="width:200px;">Network availability</th>
    <th style="width:100px;">Task group</th>
    <th style="width:100px;">Process group</th>
    <th style="width:150px;">User group</th>
    <th style="width:200px;">Task</th>
    <th style="width:400px;">Notes</th>
    <th style="width:400px;">Requirements</th>
    <th style="width:100px;">Other services</th>
    <th style="width:100px;">initial caching idea</th>
  </tr>
  <tr>
    <td style="width:100px;">1</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Everyone</td>
    <td style="width:200px;">Find available rooms at specific dates</td>
    <td style="width:400px;">Finds available room types at specific dates</td>
    <td style="width:400px;">Unavailable room-types are omitted</td>
    <td style="width:100px;">-</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">2</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Find bookings of a user</td>
    <td style="width:400px;">Finds and returns a users bookings</td>
    <td style="width:400px;">Must only return the users bookings</td>
    <td style="width:100px;">-</td>
    <td style="width:100px;">No caching</td>
  </tr>
  <tr>
    <td style="width:100px;">3</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Everyone</td>
    <td style="width:200px;">Retrieve information about a specific room-type</td>
    <td style="width:400px;">Finds and returns information about a specific room-type in DB</td>
    <td style="width:400px;">Must use room_type_id to identify the room-type</td>
    <td style="width:100px;">-</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">5</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">1</td>
    <td style="width:100px;">1</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Initiating a booking process, lock room in booking and inventory cache</td>
    <td style="width:400px;">It initiates the booking-process, locking the room as unavailable in the inventory cache, and locking it as "started" in the booking cache</td>
    <td style="width:400px;">Must be possible to identify the cache-locked content by the user. Should not rely on id's returned by this endpoint. Cached data must expire after a period of time. Must be possible to book multiple rooms in one booking</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:100px;">write-back</td>
  </tr>
  <tr>
    <td style="width:100px;">5</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">1</td>
    <td style="width:100px;">1</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Lock a room in cache</td>
    <td style="width:400px;">Receives information from booking-service regarding which room-type to make unavailable in cache</td>
    <td style="width:400px;">Must be possible to identify the cached content by user_id or similar; Not rely on data submitted by client (except auth token)</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">write-back</td>
  </tr>
  <tr>
    <td style="width:100px;">5</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">2</td>
    <td style="width:100px;">1</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Conduct payment and store booking in relevant db's</td>
    <td style="width:400px;">When receiving payment information, fetch content from cache regarding booking, complete the payment and store booking. Stores content in db for booking, inventory and payment. </td>
    <td style="width:400px;">Must find content from cache to continue with payment. if content is not found in cache, then the booking should not be possible. Each booking contains one or more item-entries for each room included</td>
    <td style="width:100px;">Inventory, payment, notification</td>
    <td style="width:100px;">read, flush to db</td>
  </tr>
  <tr>
    <td style="width:100px;">5</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">2</td>
    <td style="width:100px;">1</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Flush from cache to DB</td>
    <td style="width:400px;">Flush the temp cache-locked content for a booking to the inventory DB</td>
    <td style="width:400px;">Must inform of if it failed</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">Flush, delete</td>
  </tr>
  <tr>
    <td style="width:100px;">5</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">2</td>
    <td style="width:100px;">1</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Delete from cache</td>
    <td style="width:400px;">Delete a temp cache-locked room from the inventory cache as a fallback if other processes during payment failed in other microservice</td>
    <td style="width:400px;">Must inform of if it failed</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">Delete</td>
  </tr>
  <tr>
    <td style="width:100px;">5</td>
    <td style="width:100px;">Payment</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">2</td>
    <td style="width:100px;">1</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Conduct payment and store booking in relevant db's</td>
    <td style="width:400px;">Receive payment-information from booking-service, and conduct payment</td>
    <td style="width:400px;">Must inform of if it failed. Must lock the payment to prevent it from being duplicate payments</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">Write</td>
  </tr>
  <tr>
    <td style="width:100px;">5, 9</td>
    <td style="width:100px;">Notification</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">2</td>
    <td style="width:100px;">1</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Send confirmation email</td>
    <td style="width:400px;">Send confirmation email when a booking is completed</td>
    <td style="width:400px;">Must send email containing information specific to the booking</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">6</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">3</td>
    <td style="width:100px;">2</td>
    <td style="width:150px;">Everyone</td>
    <td style="width:200px;">Endpoint to create new user in auth-db</td>
    <td style="width:400px;">Creates a user_id, and a user-entry in auth-db. Contacts user-service and provides the user_id, along with user-information</td>
    <td style="width:400px;">Must inform if it fails. Should include in auth-db before contacting user-service. Remove from auth-db if failed-response from user-service</td>
    <td style="width:100px;">User</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">6</td>
    <td style="width:100px;">User</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">3</td>
    <td style="width:100px;">2</td>
    <td style="width:150px;">Everyone</td>
    <td style="width:200px;">Endpoint to create a new user in user-db</td>
    <td style="width:400px;">Creates a user-entry in user-db, and return response to auth-service (success/fail). It might be relevant to use an event instead of API</td>
    <td style="width:400px;">Must inform if it fails so that auth-db can react relatively to that</td>
    <td style="width:100px;">Auth</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">7</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Everyone</td>
    <td style="width:200px;">Sign into account - create a session in session_table in auth-db</td>
    <td style="width:400px;">Find user in db by email, check if password corresponds, and that the user is active. If true, create a new refresh-token and access-token</td>
    <td style="width:400px;">Must create session in db</td>
    <td style="width:100px;"></td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">7</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">All signed in user-groups</td>
    <td style="width:200px;">Sign out of account - invalidate/delete a session in session_table in auth-db</td>
    <td style="width:400px;">Find session in session_table by using session_id and user_id from access-token and invalidate/remove from db</td>
    <td style="width:400px;">Cannot find session by just session_id or user_id; Must be both</td>
    <td style="width:100px;"></td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">7</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">All signed in user-groups</td>
    <td style="width:200px;">Provide new access-token</td>
    <td style="width:400px;">Uses refresh-token to find session in session_table and before creating new access-token</td>
    <td style="width:400px;">Cannot find session by just session_id or user_id; Must be both</td>
    <td style="width:100px;"></td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">8</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">All signed in user-groups</td>
    <td style="width:200px;">Invalidate/Delete all signed in sessions of a user in session_table</td>
    <td style="width:400px;">Sign out a user of all their sessions, including the one they are current signed into</td>
    <td style="width:400px;">Must first validate the user_id and session_id as valid in the db before signing out: To prevent invalidated tokens from being used to sign out other sessions</td>
    <td style="width:100px;"></td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">10</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">3</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Find and return bookings on specific date</td>
    <td style="width:400px;">For the employee to be able to view booking-information on a selected date</td>
    <td style="width:400px;">Should use search query for date (format: dd-mm-yyyy), and for page-selection to enable pagination. Might be a good idea to include query for showing cancelled bookings as an option</td>
    <td style="width:100px;">User</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">11</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">4</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Find and return information about a specific booking</td>
    <td style="width:400px;">For the employee to get information about a specific booking, such as user, rooms, from-to-dates, what the booking includes etc</td>
    <td style="width:400px;">Must provide enough information for employee to view all necessary information about the rooms, the booking and the user. Must return user-id to also be able to view the users profile. the id of the booking should be a search param</td>
    <td style="width:100px;">User</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">12</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">5</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Find and return a customers bookings</td>
    <td style="width:400px;">Similar to functionality for user-story 2, but for the employee to view instead. Instead of relying on the auth-token for user_id, it should be in the search params</td>
    <td style="width:400px;">Must provide basic information about the booking to view some details before opening the specific booking. Must include id's of bookings to use them for later fetching specific bookings</td>
    <td style="width:100px;">User</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">10, 11, 12</td>
    <td style="width:100px;">User</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">3, 4, 5</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Return the name of a user</td>
    <td style="width:400px;">For when returning booking-information of a user, to provide booking-service with name of the user who owns the booking</td>
    <td style="width:400px;">Should use user_id to get the users name</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">13</td>
    <td style="width:100px;">User</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Find a user by name, email or phone number</td>
    <td style="width:400px;">Finds user by name, email and/or phone number in the DB and return for employee to view</td>
    <td style="width:400px;">Should return all users found regardless of what type of input was used. if using method GET, use search queries to specify email/firstname/lastname/phone, if using query, use received json body</td>
    <td style="width:100px;">-</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">6</td>
    <td style="width:100px;">3</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Get room availability for other selected dates</td>
    <td style="width:400px;">Uses the received room_id's to find room_type and then returns the availability of different the room-types included in this specific booking</td>
    <td style="width:400px;">Should return simple data: are the room-types (and amount) of this booking available at the selected dates? If the room-types are available: Temp cache them to lock them as unavailable for a period of time. Must be identifiable for later (for example using booking_id as key), so the cache can be found and be used/modified. if second request for other date, it should update the existing cache and renew time. ID's of rooms on the new date should be stored in cache</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">cache-aside (i think)</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">6</td>
    <td style="width:100px;">3</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Cache the potential new booking-date</td>
    <td style="width:400px;">Receive msg from inventory of other dates to cache-lock rooms for: To disable others from booking those rooms, and to use later to flush from cache to db. </td>
    <td style="width:400px;">Should cache-lock the bookings to dates, to disable others from booking them, and enable service to flush from cache to db when booking is saved. Must be identifiable by booking_id or similar. Must be able to modify dates in cache-lock for if selected dates is changed. Might need to check db before locking with cache, meaning that caching might have to be cache-aside. Must receive id's (and date) of rooms to lock in cache from inventory.</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:100px;">write-back</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">7</td>
    <td style="width:100px;">3</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Flush from cache to DB</td>
    <td style="width:400px;">Flushes from cache to DB to change booking (date) and the rooms included (room_id in each db-item)</td>
    <td style="width:400px;">Must find cached content to be able to make change. Should not be able to make change without fetching content from cache (acting as a safe-guard). Must ensure previous content in db is modified (both room_id and date). contents must be removed from cache.</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:100px;">flush, delete</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">7</td>
    <td style="width:100px;">3</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Flush from cache to DB</td>
    <td style="width:400px;">Flushes from cache to db, to set that new rooms are not available. Has to select which rooms to make unavailable, and which to reset to available. Has to contact booking-service to trigger the update there as well.</td>
    <td style="width:400px;">Must find cached content to be able to make change. Should not be able to make change without fetching content from cache (acting as a safe-guard). Must set previous rooms/date as available in db. Should receive relevant id's from booking for identifying content from cache</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">Flush, delete</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">4</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Find available rooms and return to employee</td>
    <td style="width:400px;">Finds available rooms for same date and returns. Might be good idea to include query for type of room to search for: and set default to "all" types of rooms. Good idea to not return all data, instead use pagination</td>
    <td style="width:400px;">Must check for whole booking-period, e.g. must receive dates with request (as query)</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:100px;">comparative filtering (check db, compare with cache)</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">8</td>
    <td style="width:100px;">4</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Lock a room as unavailable in booking cache</td>
    <td style="width:400px;">Gets new room_id, existing item_id and booking_id with request, locks the content in cache (distributed lock), and contacts inventory to do the same. Locking with cache, to enable employee to make change multiple rooms in one go: for example: Lock 3 rooms in cache 1 by 1, and later "save" all changes, allowing all 3 to be updated with one request to the microservice - Might be better to completely drop cache-lock, and simply do the re-booking one rooms 1 by 1 (consider it)</td>
    <td style="width:400px;">Must lock in cache so it cannot be booked by someone else. Must be possible to identify the specific locked room/item and booking in cache for using to flush later.</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:100px;">write-back (Distributed lock)</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">8</td>
    <td style="width:100px;">4</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Lock a room as unavailable in inventory cache</td>
    <td style="width:400px;">Gets new room_id, existing room_id, new dates, and occupied_id with request, locks the content in cache (distributed lock), enabling it to be either modified, removed, or flushed later</td>
    <td style="width:400px;">Must receive and cache enough content for it to be possible to identify the specific item in the cache later. Must check if already existing in cache, and update cache instead of storing new if necessary.</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">write-back (Distributed lock)</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">9</td>
    <td style="width:100px;">4</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Flush from cache and update db</td>
    <td style="width:400px;">Find cache-locked change in cache and flush to db. Contact inventory-db to do the same. Ensure old db data is modified to represent new room-booking. Ensure cache does not still have room-lock</td>
    <td style="width:400px;">Must receive enough information in request to find cache-locked data and flush from cache to db. Must ensure no data is left in cache and that all related db-data is updated. Must be able to handle more room-changes in single request</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:100px;">read, flush to db</td>
  </tr>
  <tr>
    <td style="width:100px;">14</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">9</td>
    <td style="width:100px;">4</td>
    <td style="width:150px;">Employee, Manager, Owner</td>
    <td style="width:200px;">Flush from cache to db, update room-availability</td>
    <td style="width:400px;">Find cache-locked change in cache and flush to db. Ensure old db data is modified to represent new room-booking. Ensure cache does not still have room-lock. Previous room must be made available again</td>
    <td style="width:400px;">Must receive enough information in request to find cache-locked data and flush from cache to db. Must ensure no data is left in cache and that all related db-data is updated. Must update the previous room to be available again. Must be able to handle more room-changes in single request</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">Read, flush to db</td>
  </tr>
  <tr>
    <td style="width:100px;">15</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">10</td>
    <td style="width:100px;">5</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Create employee account</td>
    <td style="width:400px;">Creates account for employee, receives permission-level, and sends info to user-service it to be created there as well</td>
    <td style="width:400px;">Must receive all information necessary in json body to create account and set permission level. Cannot create owner-account, as there should only be one owner. Email domain must match the hotel's selected domains. Only managers and owners can create new accounts</td>
    <td style="width:100px;">user, notification</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">15</td>
    <td style="width:100px;">User</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">10</td>
    <td style="width:100px;">5</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Create employee account in user-db</td>
    <td style="width:400px;">Receives information from auth-service</td>
    <td style="width:400px;">Receives all information necessary from auth-service to create the account. </td>
    <td style="width:100px;">Auth</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">15</td>
    <td style="width:100px;">Notification</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">10</td>
    <td style="width:100px;">5</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">sends notification to employee account with confirmation of account-creation</td>
    <td style="width:400px;">Receives information from auth-service</td>
    <td style="width:400px;">Receives email and information from auth-service</td>
    <td style="width:100px;">Auth</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">15</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">11</td>
    <td style="width:100px;">6</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Delete an account (set "is_active" to false)</td>
    <td style="width:400px;">Receive user_id of a valid employee/manager/user account with request</td>
    <td style="width:400px;">Employee's cannot delete other accounts. Owner can delete all accounts (except their own), manager can delete an employees account, not managers. Auth-service must authorize permission for account-deletion before contacting user-service. Deletion is done by setting "is_active" to false</td>
    <td style="width:100px;">User</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">15</td>
    <td style="width:100px;">User</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">11</td>
    <td style="width:100px;">6</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Delete an account (set "is_active" to false)</td>
    <td style="width:400px;">Receive user_id of a valid employee/manager/user account with request</td>
    <td style="width:400px;">Must not be contacted by auth-service before auth-service has verified that the current user is authorized to delete the account. Deletion is done by setting "is_active" to false</td>
    <td style="width:100px;">Auth</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">16</td>
    <td style="width:100px;">Auth</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Edit permission level of an employee account</td>
    <td style="width:400px;">Receives user-id of employee and new permission-level with request</td>
    <td style="width:400px;">Must receive user_id of an employee, e.g. permission_level cannot be 0, must be 1 or 2. Cannot edit from 2 to 3. Normal employees cannot modify another employees/managers/owners account. An owner can modify all, a manager can modify an employees account</td>
    <td style="width:100px;">-</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">7</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Allow manager to create room options that customers can book</td>
    <td style="width:400px;">Receive id's of room-types, descriptions, and accomodations that should be made into bookable rooms, and amount of them that should be made</td>
    <td style="width:400px;">Should not be possible to create more bookable rooms containing a room-type than total amount of that room-type. Other limits also apply. Minimum level: manager</td>
    <td style="width:100px;"></td>
    <td style="width:100px;"></td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">7</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Allow manager to get list of rooms, accomodations and other relevant data necessary to create room-options</td>
    <td style="width:400px;">Should return arrays of objects containing content used to create room-options</td>
    <td style="width:400px;">Should not be possible to create more bookable rooms containing a room-type than total amount of that room-type. Other limits also apply. Minimum permission level: manager</td>
    <td style="width:100px;"></td>
    <td style="width:100px;"></td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">7</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Allow manager to delete a room-option, so it is no longer bookable (set "is_active" to false)</td>
    <td style="width:400px;">should only be possible for manager to do so</td>
    <td style="width:400px;">Minimum permission level: manager. It is important that deletion is done by setting "is_active" to false</td>
    <td style="width:100px;"></td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Set a specific room as active/inactive</td>
    <td style="width:400px;">Set a specific room as active/inactive, preventing/allowing booking of the room</td>
    <td style="width:400px;">Minimum permission level: manager</td>
    <td style="width:100px;"></td>
    <td style="width:100px;"></td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Manager, Owner</td>
    <td style="width:200px;">Set room-type as (non-)bookable</td>
    <td style="width:400px;">Set if a room is active/inactive, preventing/allowing booking of the room-type</td>
    <td style="width:400px;">Minimum permission level: manager</td>
    <td style="width:100px;"></td>
    <td style="width:100px;"></td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Provide user with addon/extra-options</td>
    <td style="width:400px;">Provide user with options for adding things when booking is being done, so that the user for example can add a breakfast if not included in room-type</td>
    <td style="width:400px;">A booking tied to the user must be locked for options to be returned, should not simply provide options if there is no relevant locked booking. should be available in inventory-cache</td>
    <td style="width:100px;"></td>
    <td style="width:100px;">cache-aside</td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Booking</td>
    <td style="width:200px;">External</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Add addons to a booking currently being made (pre-payment)</td>
    <td style="width:400px;">Allow users to include addons to their bookings, for example breakfast etc if they are booking an alternative that does not include it. This one refers to the ones done during the booking of the room, not after</td>
    <td style="width:400px;">A room-booking must be locked for it to be possible to do this, it should not be possible to select an addon-to a booking that is not currently being made</td>
    <td style="width:100px;">inventory</td>
    <td style="width:100px;">None</td>
  </tr>
  <tr>
    <td style="width:100px;">Other</td>
    <td style="width:100px;">Inventory</td>
    <td style="width:200px;">Internal</td>
    <td style="width:100px;">None</td>
    <td style="width:100px;">None</td>
    <td style="width:150px;">Customer</td>
    <td style="width:200px;">Verify that addon exists in inventory</td>
    <td style="width:400px;">Verify that the addon exists in the inventory-db and is possible to book as an addon-option. Used when adding accomodations to a booking (pre-payment), to ensure that it both exists, and that it is allowed to add</td>
    <td style="width:400px;">Only available on the internal network, must receive id of accomodation and returns only boolean of if its possible to add or not</td>
    <td style="width:100px;">Booking</td>
    <td style="width:100px;">cache-aside</td>
  </tr>
</table>