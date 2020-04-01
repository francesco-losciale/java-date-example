## Learning the java.time API

Please read the `JavaTimePackageTest` file to learn about the Java date/time api. 
Test methods should be enough to understand the context.

### How to run

Postgres database
```bash
cd java-date-example
docker-compose up
```
Build and test:
```bash
./gradlew clean build
```

### Best practices

* When you need to generate a date with `now` and `OffsetDateTime`, please use `OffsetDateTime.now(UTC)` which will generate the date in UTC format without +01:00 offset DST. If you use `OffsetDateTime.now()` you could mess things up when manipulating values. 

* When sending datetime values over network or saving them in database tables, be sure to save the values in UTC 
format which can always be converted back to a local datetime of a specific timezone. 

* Be careful when values you save WITH ZONE are in the Daylight Saving Time period
 which is from 31 March to 27 October in UK.
 In this range the UTC offset is of one hour more.
 Example:
   * 2019-12-27T11:00:00.000+01:00[Europe/Paris]
   * 2019-06-27T11:00:00.000+02:00[Europe/Paris]
 
   In June the UTC offset from Paris is +02:00 hours instead of +01:00

* If you convert a LocalDateTime such as 2020-03-31T00:00:00.000 to UTC OffsetDateTime, the new value becomes 2020-03-30T23:00:00.000, ie. the day before!!! Be careful with date value manipulation then! You should first convert UTC to BST to be sure you are not getting things wrong.


* Use timestamp WITH ZONE in the table column so that even the local datetime values without explicit zone are 
converted to UTC format from your database before saving them.
 
* Always use OffsetDateTime to save a LocalDateTime+Offset and TIMESTAMP WITH ZONE.
Let the clients deal with the conversion (front-end, JVM code, etc).
   
* When you convert an OffsetDateTime to LocalDateTime, the offset is discarded.

* When you convert a ZonedDateTime to LocalDateTime, the zone/offset is discarded

* Test methods in JavaTimePackageTest that starts with `when_Reading_DateTime...` regard
the database. Following some considerations.

* When you read a value from a column WITH ZONE, then the value can be converted to 
the current zone. 
  
    In the test which makes the local zone to be Asia/Tokyo for example, we read:
    ```
    2019-12-27T19:00:00.000+08:00[Asia/Tokyo]
    ```
    Which is the same instant we saved in the Europe/Paris timezone, ie.:
    ```
    2019-12-27T11:00:00.000+01:00[Europe/Paris]
    ```

* When you read a value from a column WITHOUT ZONE, then the value can't be converted to 
the current zone properly. 
  
  * In the test which simulate ZoneId to be Asia/Tokyo for example, we read:
    ```
    2019-12-27T11:00:00.000
    ```
    Which is not the same instant at time below:
    ```
    2019-12-27T11:00:00.000+01:00[Europe/Paris]
    ```
    We expect to read the following in the Asia/Tokyo timezone:
    ```
    2019-12-27T19:00:00.000
    ```
