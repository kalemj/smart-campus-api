# Smart Campus API

This is my coursework for Smart Campus.

It is a JAX-RS api for rooms, sensors and readings.

I used Java, Jersey, Grizzly and Jackson.

There is no database, just in memory data.

Base url: `http://localhost:8080/api/v1`

---

## 1. API Overview

The resources are based on the campus structure.

| Path                                          | Verbs                | Purpose                                                         |
|-----------------------------------------------|----------------------|-----------------------------------------------------------------|
| `/api/v1`                                     | `GET`                | Discovery document (HATEOAS links + metadata)                   |
| `/api/v1/rooms`                               | `GET`, `POST`        | List rooms / create a new room                                  |
| `/api/v1/rooms/{roomId}`                      | `GET`, `DELETE`      | Fetch / decommission a single room                              |
| `/api/v1/sensors`                             | `GET`, `POST`        | List sensors (optional `?type=` filter) / register a new sensor |
| `/api/v1/sensors/{sensorId}`                  | `GET`                | Fetch a single sensor                                           |
| `/api/v1/sensors/{sensorId}/readings`         | `GET`, `POST`        | Sub-resource: history / append new reading                      |

### Notes

- `@ApplicationPath("/api/v1")` is used for the base path.
- data is kept in one shared `DataStore` object
- there is a small service layer so the resource classes are not doing everything
- `/sensors/{id}/readings` is done using a sub-resource
- exception mappers are used for the error responses
- `LoggingFilter` logs requests and responses

---

## 2. Build and Run

### Prerequisites
- JDK 11+
- Maven

### Command line

```bash
# 1. Clone
git clone https://github.com/<your-username>/smart-campus-api.git
cd smart-campus-api/smart-campus-api

# 2. Build
mvn clean package

# 3. Run

# with Maven
mvn exec:java

# or jar
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar

# custom port
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar 9090
```

When it runs it should show something like this:

```
===========================================================
 Smart Campus API is RUNNING
 Base URL : http://localhost:8080/api/v1
 Try     : curl http://localhost:8080/api/v1
 Stop    : Ctrl-C
===========================================================
```

### IntelliJ

1. open the folder with `pom.xml`
2. let maven load
3. run `Main.java`

### Seed data

There are already 2 rooms and 3 sensors in the store.
One of the sensors is in `MAINTENANCE`.

---

## 3. Sample curl Commands

These assume the server is running on localhost 8080.

### 3.1 Discovery

```bash
curl -i http://localhost:8080/api/v1
```

### 3.2 Create a Room

```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-205","name":"Engineering Lab","capacity":25}'
```

### 3.3 Register a Sensor with bad roomId

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"NEW-001","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"GHOST-999"}'
```

### 3.4 Filter Sensors by type

```bash
curl -i "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 3.5 Add a reading

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.7}'
```

### 3.6 Trigger `403 Forbidden`

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/OCC-007/readings \
  -H "Content-Type: application/json" \
  -d '{"value":5}'
```

### 3.7 Trigger `409 Conflict`

```bash
curl -i -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 3.8 Successful DELETE

```bash
curl -i -X DELETE http://localhost:8080/api/v1/rooms/ENG-205
```

---

## 4. Report

### Part 1 — Setup & Discovery

#### 1.1 JAX-RS Resource Lifecycle and in-memory state

By default JAX-RS resources are per request, so Jersey makes a new object each time.

This means if I stored data directly in the resource class it would not stay there between requests. For example if I wrote
`private Map<String, Room> rooms = new HashMap<>();` directly on
`SensorRoomResource`, the very next request would see a freshly-empty map —
data loss by design.

To keep the data, it needs to be somewhere shared, so I used one shared `DataStore` object.

The other problem is concurrency because more than one request can happen at once.
That is why I used simple `synchronized` methods in the store. It is basic but okay for this project.

#### 1.2 Why HATEOAS hypermedia matters

HATEOAS means the response gives links as well, not just data. My `/api/v1`
endpoint returns:

```json
{
  "links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors",
    "sensorReadings": "/api/v1/sensors/{sensorId}/readings"
  }
}
```

This is useful because a client can start from one place and see the main links.

### Part 2 — Room Management

#### 2.1 Returning IDs only vs full Room objects

There are pros and cons.

If the API only returns room IDs, the response is smaller, which saves bandwidth.
The downside is that the client then needs extra requests to get the room details.

If the API returns full room objects, it is easier for the client because all the
main room data is already there in one response. The downside is that the payload
is bigger.

I returned full room objects because it is easier to use and show.

#### 2.2 Is DELETE idempotent in this implementation?

Yes, it is idempotent. The main point is that it is about the final server
state, not whether the response code is always the same.

In this implementation:

- **Call 1:** `DELETE /api/v1/rooms/ENG-205` → `204 No Content`. The room
  is removed from `DataStore`.
- **Call 2:** `DELETE /api/v1/rooms/ENG-205` → `404 Not Found`. The map
  lookup returns `null`, so `NotFoundException` is thrown.
- **Call 3, 4, 5...** all return `404`.

The response changes, but after the first delete the room is already gone, so the
server state is not changing again.

### Part 3 — Sensors & Filtering

#### 3.1 Consequences of `@Consumes(APPLICATION_JSON)` mismatch

The `@Consumes` annotation says which media type the method accepts. JAX-RS checks this before the resource method runs.

If a client sends `text/plain` or `application/xml` to a method with `@Consumes(MediaType.APPLICATION_JSON)`, Jersey rejects it with `415 Unsupported Media Type`.

This is useful because the method only has to deal with JSON.

#### 3.2 Why `@QueryParam` beats path segments for filtering

Query parameters are better for filtering because the client is still asking
for the same collection, just with a condition added.

It also works better when there are multiple optional filters. Path segments are better when identifying one resource like `/sensors/TEMP-001`.

### Part 4 — Sub-Resources

#### 4.1 Architectural benefits of the Sub-Resource Locator pattern

A locator is a `@Path` method without `@GET` or `@POST` on it. It returns
another resource class, then Jersey carries on from there. In this project
that is used for `/sensors/{sensorId}/readings`.
The main benefits are:

- **Single responsibility.** `SensorResource` knows about sensor lifecycle;
  `SensorReadingResource` knows about reading history. Each class's reason to
  change is independent.
- **Per-request context capture.** The locator passes `sensorId` once into the
  child's constructor, so every child method can use it without re-parsing
  the path. With a flat controller, every method signature would need
  `@PathParam("sensorId") String id`.
- **Cleaner code.** It avoids putting too many nested routes in one big class.
- **Easier to extend later.** If more nested sensor features were added, they
  could be split into their own classes too.

### Part 5 — Error Handling & Logging

#### 5.2 Why HTTP 422 over 404 for missing references

`404 Not Found` is not really right here because the URL exists. The problem is the JSON refers to a room that does not exist.

For the client, `404` usually suggests the URL is wrong, but here the URL is
fine. The problem is inside the JSON body, so `422` fits better.

#### 5.4 Cybersecurity risks of leaked Java stack traces

A raw stack trace can expose too much internal information. An attacker could learn things like:

- **Language & runtime:** Java, identifiable from package/class names.
- **Framework and version surface:** `org.glassfish.jersey.server.*`
  tells them what framework is being used.
- **Internal package and class names**
- **File paths**
- **Code locations that can be triggered**

That is why the API should log the full error on the server side, but only return
a generic
`{"status":500,"error":"INTERNAL_SERVER_ERROR","message":"..."}`
message to the client.

#### 5.5 Why JAX-RS filters beat manual logging

Using a filter is better because logging applies to every endpoint, not just one.
If it was written by hand in every method, it would be repetitive and easy to forget.
A filter also sees the final response status code.

---

## 5. Video Demonstration

The video should show this:

1. Discovery endpoint and HATEOAS links.
2. Room CRUD: `POST` (with `201` + `Location`), `GET`, `GET /{id}`.
3. Room deletion safety: success (`204`) vs `409 Conflict` for occupied rooms.
4. Sensor registration with valid vs invalid `roomId` (`201` vs `422`).
5. `?type=` filter changing the result set live.
6. Sub-resource navigation: `/sensors/{id}/readings` `GET` then `POST`,
   followed by `GET /sensors/{id}` to show the updated `currentValue` side effect.
7. `403 Forbidden` when posting a reading to a `MAINTENANCE` sensor.
8. `500 Internal Server Error` (generic) when a runtime exception is forced.
9. Server console showing the log lines.
