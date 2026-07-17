# Database scemas
This document contains the tables (with which content) the databases should have.

<table>
  <tr>
    <th rowspan="7">User DB</th>
    <th colspan="2">user_table</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>user_id</td><td>PK, ObjectId</td>
  </tr>
  <tr>
    <td>first_name</td><td>String</td>
  </tr>
  <tr>
    <td>last_name</td><td>String</td>
  </tr>
  <tr>
    <td>country_code</td><td>Int -&gt; 0047</td>
  </tr>
  <tr>
    <td>phone_number</td><td>Int</td>
  </tr>
  <tr>
    <td>account_active</td><td>Boolean</td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="6">Auth DB</th>
    <th colspan="2">user_table</th>
    <th colspan="2">session_table</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>user_id</td><td>PK, ObjectId</td>
    <td>user_id</td><td>FK, ObjectId</td>
  </tr>
  <tr>
    <td>email</td><td>String</td>
    <td>session_id</td><td>PK, ObjectId</td>
  </tr>
  <tr>
    <td>password</td><td>String</td>
    <td>issued_at</td><td>date-time</td>
  </tr>
  <tr>
    <td>permission_level</td><td>Int</td>
    <td>expires_at</td><td>Int</td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="8">Payment DB</th>
    <th colspan="2">payment_table</th>
    <th colspan="2">refund_table</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>user_id</td><td>FK, ObjectId</td>
    <td>refund_id</td><td>PK, ObjectId</td>
  </tr>
  <tr>
    <td>payment_id</td><td>PK, ObjectId</td>
    <td>payment_id</td><td>FK, ObjectId</td>
  </tr>
  <tr>
    <td>booking_id</td><td>FK, ObjectId</td>
    <td>amount</td><td>Int</td>
  </tr>
  <tr>
    <td>price</td><td>Int</td>
    <td>refund_time</td><td>Date-time</td>
  </tr>
  <tr>
    <td>refunded</td><td>boolean</td>
    <td></td><td></td>
  </tr>
  <tr>
    <td>payment_time</td><td>Date-time</td>
    <td></td><td></td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="8">Booking DB</th>
    <th colspan="2">bookings_table</th>
    <th colspan="2">cancellation_table</th>
    <th colspan="2">items_table</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>booking_id</td><td>PK, ObjectId</td>
    <td>booking_id</td><td>FK, ObjectId</td>
    <td>booking_id</td><td>FK, ObjectId</td>
  </tr>
  <tr>
    <td>user_id</td><td>ObjectId</td>
    <td>cancellation_id</td><td>PK, ObjectId</td>
    <td>item_id</td><td>PK, ObjectId</td>
  </tr>
  <tr>
    <td>price</td><td>Int</td>
    <td>cancellation_time</td><td>date-time</td>
    <td>room_id</td><td>ObjectId</td>
  </tr>
  <tr>
    <td>start_date</td><td>Date</td>
    <td>refunded</td><td>Boolean</td>
    <td>room_type_id</td><td>ObjectId</td>
  </tr>
  <tr>
    <td>end_date</td><td>Date</td>
    <td></td><td></td>
    <td>checked_id</td><td>Boolean</td>
  </tr>
  <tr>
    <td>completed</td><td>boolean</td>
    <td></td><td></td>
    <td>breakfast_included</td><td>Boolean</td>
  </tr>
</table>

<table>
  <tr>
    <th rowspan="9">Inventory DB</th>
    <th colspan="2">room_type_table</th>
    <th colspan="2">room_number_table</th>
    <th colspan="2">occupied_table</th>
    <th colspan="2">images_table</th>
    <th colspan="2">accommodation_table</th>
  </tr>
  <tr>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
    <th>Column name</th><th>Data type</th>
  </tr>
  <tr>
    <td>type_id</td><td>PK, ObjectId</td>
    <td>type_id</td><td>FK, ObjectId</td>
    <td>type_id</td><td>FK, ObjectId</td>
    <td>type_id</td><td>FK, ObjectId</td>
    <td>type_id</td><td>FK, ObjectId</td>
  </tr>
  <tr>
    <td>room_price</td><td>Int</td>
    <td>room_id</td><td>PK, ObjectId</td>
    <td>room_id</td><td>FK, ObjectId</td>
    <td>image_id</td><td>PK, ObjectId</td>
    <td>accommodation_id</td><td>PK, ObjectId</td>
  </tr>
  <tr>
    <td>room_name</td><td>String</td>
    <td>room_number</td><td>Integer</td>
    <td>from_date</td><td>Date</td>
    <td>image_path</td><td>String</td>
    <td>accommodation</td><td>String</td>
  </tr>
  <tr>
    <td>persons</td><td>Int</td>
    <td></td><td></td>
    <td>to_date</td><td>Date</td>
    <td>image_number</td><td>Int</td>
    <td>Amount</td><td>Int</td>
  </tr>
  <tr>
    <td>double_bed_amount</td><td>Int</td>
    <td></td><td></td>
    <td></td><td></td>
    <td>image_text</td><td>String</td>
    <td>Included</td><td>Boolean</td>
  </tr>
  <tr>
    <td>single_bed_amount</td><td>Int</td>
    <td></td><td></td>
    <td></td><td></td>
    <td>alt_text</td><td>String</td>
    <td></td><td></td>
  </tr>
  <tr>
    <td>breakfast_price</td><td>Integer</td>
    <td></td><td></td>
    <td></td><td></td>
    <td></td><td></td>
    <td></td><td></td>
  </tr>
</table>