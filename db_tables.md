# Database scemas
This document contains the tables (with which content) the databases should have.

<table>
  <tr>
    <th rowspan="9">User DB</th>
    <th colspan="2">users</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>user_id</td><td>UUID (PK)</td>
  </tr>
  <tr>
    <td>first_name</td><td>VARCHAR</td>
  </tr>
  <tr>
    <td>last_name</td><td>VARCHAR</td>
  </tr>
  <tr>
    <td>country_code</td><td>INT</td>
  </tr>
  <tr>
    <td>phone_number</td><td>INT</td>
  </tr>
  <tr>
    <td>active_account</td><td>Boolean</td>
  </tr>
  <tr>
    <td>email</td><td>VARCHAR</td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="7">Auth DB</th>
    <th colspan="2">user_credentials</th>
    <th colspan="2">user_sessions</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>user_id</td><td>UUID (PK)</td>
    <td>user_id</td><td>UUID (FK)</td>
  </tr>
  <tr>
    <td>email</td><td>VARCHAR</td>
    <td>session_id</td><td>UUID (PK)</td>
  </tr>
  <tr>
    <td>password</td><td>VARCHAR</td>
    <td>issued_at</td><td>DATETIME</td>
  </tr>
  <tr>
    <td>permission_level</td><td>INT</td>
    <td>expires_at</td><td>DATETIME</td>
  </tr>
  <tr>
    <td>active_account</td><td>BOOLEAN</td>
    <td></td><td></td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="7">Payment DB</th>
    <th colspan="2">payments</th>
    <th colspan="2">refunds</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>user_id</td><td>UUID</td>
    <td>refund_id</td><td>UUID(PK)</td>
  </tr>
  <tr>
    <td>payment_id</td><td>UUID (PK)</td>
    <td>payment_id</td><td>UUID (FK)</td>
  </tr>
  <tr>
    <td>booking_id</td><td>UUID</td>
    <td>amount</td><td>INT</td>
  </tr>
  <tr>
    <td>amount</td><td>INT</td>
    <td>refund_time</td><td>DATETIME</td>
  </tr>
  <tr>
    <td>payment_time</td><td>DATETIME</td>
    <td>cancellation_id</td><td>UUID</td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="7">Booking DB</th>
    <th colspan="2">bookings</th>
    <th colspan="2">booking_items</th>
    <th colspan="2">cancellations</th>
    <th colspan="2">cancelled_addons</th>
    <th colspan="2">cancelled_items</th>
    <th colspan="2">item_addons</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>user_id</td><td>UUID</td>
    <td>item_id</td><td>UUID (PK)</td>
    <td>cancellation_id</td><td>UUID (PK)</td>
    <td>cancellation_id</td><td>UUID (FK)</td>
    <td>cancellation_id</td><td>UUID (FK)</td>
    <td>addon_id</td><td>UUID (PK)</td>
  </tr>
  <tr>
    <td>booking_id</td><td>UUID (PK)</td>
    <td>booking_id</td><td>UUID (FK)</td>
    <td>booking_id</td><td>UUID (FK)</td>
    <td>addon_id</td><td>UUID (FK)</td>
    <td>item_id</td><td>UUID (FK)</td>
    <td>item_id</td><td>UUID (FK)</td>
  </tr>
  <tr>
    <td>start_time</td><td>DATETIME</td>
    <td>room_id</td><td>UUID</td>
    <td>cancellation_time</td><td>DATETIME</td>
    <td></td><td></td>
    <td></td><td></td>
    <td>accomodation_id</td><td>UUID</td>
  </tr>
  <tr>
    <td>end_time</td><td>DATETIME</td>
    <td>checked_in</td><td>BOOLEAN</td>
    <td>cancellation_time</td><td>ENUM (varchar)</td>
    <td></td><td></td>
    <td></td><td></td>
    <td>amount</td><td>INT</td>
  </tr>
  <tr>
    <td></td><td></td>
    <td>checked_out</td><td>BOOLEAN</td>
    <td></td><td></td>
    <td></td><td></td>
    <td></td><td></td>
    <td></td><td></td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="8">Inventory DB</th>
    <th colspan="2">room_type</th>
    <th colspan="2">room_numbers</th>
    <th colspan="2">room_occupied</th>
    <th colspan="2">room_images</th>
    <th colspan="2">accommodations</th>
    <th colspan="2">room_included</th>
    <th colspan="2">room_information</th>
    <th colspan="2">room_rules</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>type_id</td><td>UUID (PK)</td>
    <td>room_id</td><td>UUID (PK)</td>
    <td>occupied_id</td><td>UUID (PK)</td>
    <td>image_id</td><td>UUID (PK)</td>
    <td>accommodation_id</td><td>UUID (PK)</td>
    <td>included_id</td><td>UUID (PK)</td>
    <td>information_id</td><td>UUID (PK)</td>
    <td>rule_id</td><td>UUID (PK)</td>
  </tr>
  <tr>
    <td>room_price</td><td>INT</td>
    <td>type_id</td><td>UUID (FK)</td>
    <td>room_id</td><td>UUID (FK)</td>
    <td>type_id</td><td>UUID (FK)</td>
    <td>accommodation</td><td>VARCHAR</td>
    <td>type_id</td><td>UUID (FK)</td>
    <td>type_id</td><td>UUID (FK)</td>
    <td>type_id</td><td>UUID (FK)</td>
  </tr>
  <tr>
    <td>room_name</td><td>VARCHAR</td>
    <td>room_number</td><td>INT</td>
    <td>type_id</td><td>UUID (FK)</td>
    <td>image_path</td><td>VARCHAR</td>
    <td>price</td><td>INT</td>
    <td>accommodation_id</td><td>UUID (FK)</td>
    <td>information</td><td>VARCHAR</td>
    <td>rule</td><td>VARCHAR</td>
  </tr>
  <tr>
    <td>capacity</td><td>INT</td>
    <td></td><td></td>
    <td>from_time</td><td>DATETIME</td>
    <td>image_number</td><td>INT</td>
    <td>possible_addon</td><td>BOOLEAN</td>
    <td>amount</td><td>INT</td>
    <td></td><td></td>
    <td>allowed</td><td>BOOLEAN</td>
  </tr>
  <tr>
    <td>double_beds</td><td>INT</td>
    <td></td><td></td>
    <td>to_time</td><td>DATETIME</td>
    <td>image_text</td><td>VARCHAR</td>
    <td></td><td></td>
    <td>show_amount</td><td>BOOLEAN</td>
    <td></td><td></td>
    <td></td><td></td>
  </tr>
  <tr>
    <td>single_beds</td><td>INT</td>
    <td></td><td></td>
    <td></td><td></td>
    <td>alt_text</td><td>VARCHAR</td>
    <td></td><td></td>
    <td></td><td></td>
    <td></td><td></td>
    <td></td><td></td>
  </tr>
</table>

![Relations of the databases and tables](project-images/hotel_booking_db_relations.png)