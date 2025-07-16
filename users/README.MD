# Users API

An HTTP & JSON API for managing user accounts — built in Scala using `http4s`, `cats-effect`, and `circe`.
This project implements the interface (algebra) of a user service and exposes it through a RESTful API, designed for
both **admin-level user management** and **end-user self-service**.

---

## 🔌 API Endpoints

### 👤 User endpoints

| Method | Endpoint                         | Description                        |
|--------|----------------------------------|------------------------------------|
| POST   | `/users`                         | Register a new user                |
| GET    | `/users/{userId}`                | Get user details by ID             |
| PUT    | `/users/{userId}/email`          | Update email of a specific user    |
| PUT    | `/users/{userId}/password`       | Update password of a specific user |
| POST   | `/users/{userId}/reset-password` | Reset password for a specific user |

> ⚠️ No authentication layer is implemented in this version. All user actions operate with full access to any `userId`.

---

### 🛠 Admin endpoints

| Method | Endpoint                        | Description             |
|--------|---------------------------------|-------------------------|
| GET    | `/admin/users`                  | Get a list of all users |
| POST   | `/admin/users/{userId}/block`   | Block a user            |
| POST   | `/admin/users/{userId}/unblock` | Unblock a user          |
| DELETE | `/admin/users/{userId}`         | Delete a user           |

---

## Design Considerations

### Access Control

* **Admin routes** (e.g. list users) are isolated under `/admin/*`.
* **User self-service routes** are grouped under `/user/self`.

### Data Exposure

* Admins have access to all user data.
* End users can only access and modify their own information.
* Sensitive fields (e.g. passwords) are not exposed in any response payloads.

### Error Handling

* Errors are consistently mapped to HTTP status codes and JSON responses:

  ```json
  {
    "error": "InvalidEmailFormat",
    "message": "Provided email is not valid"
  }
  ```

### Consistency

* Request/response schemas use consistent naming and structure.
* All endpoints follow REST conventions.

### Backward Compatibility

* The API is versionless for now, but can be extended to `/v1/...` structure.
* DTOs are separated from domain logic for future evolution.

---

## 🛠 Tech Stack

* **Scala 2.13**
* [http4s](https://http4s.org/) — Functional HTTP server
* [Cats Effect](https://typelevel.org/cats-effect/) — Async and resource safety
* [Circe](https://circe.github.io/circe/) — JSON encoding/decoding
* [Pureconfig](https://github.com/pureconfig/pureconfig) — Configuration loader
* [Logback + log4cats](https://typelevel.org/log4cats/) — Logging

---

## ▶️ Running the App

### Requirements

* JDK 17+
* sbt 1.8+

### Run locally

```bash
sbt run
```

### Run tests

```bash
sbt test
```

---
