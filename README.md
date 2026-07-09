# Catalog Service

A microservice dedicated to serving the video catalog experience, exposing feed and watch endpoints while keeping video metadata synchronized through Kafka events.

## 🏗️ Architecture

This service acts as the read-facing catalog for videos. It stores catalog metadata, serves only ready videos in the feed, generates CloudFront signed cookies for playback, and listens to upload/processing events to keep video status up to date.

* **API:** Spring Boot REST endpoints for feed and watch flows
* **Database:** PostgreSQL with primary/replica routing
* **Cache:** Redis for video lookup caching
* **Messaging:** Apache Kafka consumers for video upload and processing progress events
* **CDN Access:** AWS CloudFront signed cookies
* **Mapping:** MapStruct

### Main Flow

1. Upload/processing services publish Kafka events.
2. Catalog consumes upload events and creates video records when they do not already exist.
3. Catalog consumes progress events and updates video status:
   * `0%` -> `PROCESSING`
   * `100%` -> `READY`, with manifest and thumbnail URLs
   * Intermediate progress is ignored to avoid unnecessary writes.
4. Feed requests return paginated `READY` videos.
5. Watch requests return video metadata and set CloudFront cookies for secure playback.

## 🚀 Getting Started

### Prerequisites

* Java 21
* Docker
* Maven 3.x, or the included Maven wrapper
* PostgreSQL
* Redis
* Apache Kafka
* A CloudFront key pair/private key for signed-cookie generation

### Running Locally

1. Start the required infrastructure: PostgreSQL, Redis, and Kafka.
2. Configure the environment variables listed below, or rely on the local defaults.
3. Run the application:

This repo ships a service `dockerfile`, but no local `docker-compose.yml` yet.

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### Docker Build

```bash
docker build -f dockerfile -t catalog-service .
docker run --rm -p 8080:8080 catalog-service
```

## 📡 API

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/videos/feed?size={size}&page={page}` | Returns a paginated feed of ready videos. |
| `GET` | `/api/videos/{videoId}/watch` | Returns video playback metadata and sets CloudFront signed cookies. |

## 📨 Kafka

| Topic Property | Default | Purpose |
| :--- | :--- | :--- |
| `kafka.topic.upload` | `xtube.video.progress` | Receives uploaded-video events and creates catalog records. |
| `kafka.topic.processing` | `xtube.video.progress` | Receives processing progress events and updates catalog status. |

Kafka messages use JSON payloads and are deserialized through Spring Kafka with error handling and dead-letter publishing to `{topic}.DLT`.

## 🧪 Testing

We use unit tests for command behavior and Testcontainers for infrastructure-backed integration tests.

* **Run all tests:** `./mvnw test`
* **Windows:** `.\mvnw.cmd test`
* **Note:** Tests require a running Docker daemon. They automatically start PostgreSQL and Redis containers.

Current coverage focuses on:

* Video command status transitions
* Paginated video feed queries
* Redis caching for video lookups
* CloudFront signed-cookie generation

## 🔭 Observability

This service exposes operational health and metrics through Spring Boot Actuator and Prometheus.

| Pillar | Implementation | Purpose |
| :--- | :--- | :--- |
| **Logs** | SLF4J application logs | Tracks catalog, database, Kafka, and cookie-generation activity. |
| **Metrics** | Spring Boot Actuator + Prometheus registry | Tracks request, JVM, and application metrics. |
| **Traces** | Soon | Distributed tracing is not wired yet. |

### Accessing Observability Tools

* **Actuator Health:** `GET /actuator/health`
* **Prometheus Metrics:** `GET /actuator/prometheus`
* **Application Metrics:** `GET /actuator/metrics`
* **Distributed Traces:** `Soon`

## ⚙️ Configuration

| Variable | Default | Description |
| :--- | :--- | :--- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address. |
| `KAFKA_TOPIC_VIDEO_UPLOADED` | `xtube.video.progress` | Topic used for uploaded-video events. |
| `KAFKA_TOPIC_VIDEO_PROCESSING` | `xtube.video.progress` | Topic used for processing progress events. |
| `SPRING_KAFKA_CONSUMER_GROUP_ID` | `catalog-group` | Kafka consumer group ID. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/catalog_db` | PostgreSQL JDBC URL placeholder used by the datasource config when supplied. |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | PostgreSQL username. |
| `SPRING_DATASOURCE_PASSWORD` | `password` | PostgreSQL password. |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis host. |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis port. |
| `CLOUDFRONT_URL` | `example.com` | CloudFront resource URL used for signed-cookie policy generation. |
| `CLOUDFRONT_PRIVATE_KEY` | `private-key-example.pem` | Path to the CloudFront private key. |
| `CLOUDFRONT_KEY_ID` | `123` | CloudFront key pair ID. |

## 🧩 Notes

* Read-only transactions are routed to the replica datasource; write transactions use the primary datasource.
* When `SPRING_DATASOURCE_URL` is not supplied, the primary datasource defaults to port `5432` and the replica datasource defaults to port `5433`.
* Redis caches video lookups under the `videos` cache with a 60-minute TTL.
* The watch endpoint sets `CloudFront-Policy`, `CloudFront-Signature`, and `CloudFront-Key-Pair-Id` cookies with `HttpOnly`, `Secure`, and `SameSite=Strict`.
