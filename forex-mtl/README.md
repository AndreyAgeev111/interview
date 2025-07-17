# Forex MTL Proxy

A local proxy service that retrieves exchange rates from the One-Frame API and caches them for up to 5 minutes to avoid exceeding the 1000 daily request limit.

---

## Prerequisites

- sbt
- Docker
- Scala 2.13
- Java 17+

---

## Running the Project

1. Clone the repo and navigate to the project root:

```bash
git clone ...
cd forex-mtl
```

2. Create a `.env` file in the root:

```env
ONE_FRAME_TOKEN=10dc303535874aeccc86a8251e6992f5
```

3. Run One-Frame and the proxy via Docker Compose:

```bash
docker-compose up --build
```

This runs:
- The One-Frame mock server on `localhost:8080`
- The Forex proxy app on `localhost:8081`

4. Test the proxy:

```bash
curl "http://localhost:8081/rates?from=USD&to=JPY"
```

5. Test the code:
```bash
sbt test
```

---

## Development (manual)

1. Start One-Frame manually:
```bash
docker run -p 8080:8080 paidyinc/one-frame
```

2. Run the app:
```bash
sbt run
```

---

## Project structure

- `forex.client` — HTTP client to One-Frame
- `forex.services.rates` — rate caching logic and request limit
- `forex.http` — HTTP endpoints via http4s
- `fore.util` - Utility for HTTP clients
- `application.conf` — configuration (reads env vars)