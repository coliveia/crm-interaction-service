# CRM Interaction Service

Microservice for managing all customer interactions (calls, emails, chats, notes) with AI-powered features.

## Features

- **Interaction Management**: Create, read, and track all customer interactions
- **AI-Powered Summarization**: Automatic summary generation using LLM
- **Sentiment Analysis**: Analyze customer sentiment (positive, neutral, negative)
- **Speech-to-Text**: Transcribe audio interactions
- **Timeline View**: Unified customer interaction history
- **Attachment Support**: Upload and manage files
- **Event-Driven**: Kafka integration for real-time updates
- **Oracle Duality Views**: CQRS-native data access

## Tech Stack

- **Java 21** + **Spring Boot 3.2.1**
- **Spring WebFlux** (reactive)
- **Oracle Autonomous Database 26ai**
- **Kafka** (event streaming)
- **OpenAI API** (AI features)
- **Swagger/OpenAPI** (documentation)
- **Prometheus** (metrics)

## API Endpoints

### Interactions

```
POST   /api/interactions                    - Create interaction
GET    /api/interactions/{id}               - Get interaction by ID
GET    /api/interactions/case/{caseId}      - Get case interactions
GET    /api/interactions/customer/{id}/timeline - Get customer timeline
GET    /api/interactions/customer/{id}/recent   - Get recent interactions
POST   /api/interactions/{id}/transcription - Add transcription
POST   /api/interactions/{id}/summary       - Add summary
POST   /api/interactions/{id}/sentiment     - Analyze sentiment
GET    /api/interactions/case/{id}/stats    - Get interaction stats
```

## Configuration

### Environment Variables

```bash
# Database
DB_HOST=localhost
DB_PORT=1521
DB_SERVICE=FREEPDB1
DB_USERNAME=crm_user
DB_PASSWORD=crm_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# OpenAI
OPENAI_API_KEY=your_openai_api_key_here
OPENAI_API_URL=https://api.openai.com/v1
```

## Running Locally

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or with Java
java -jar target/crm-interaction-service-1.0.0-SNAPSHOT.jar
```

## Docker

```bash
# Build image
docker build -t crm-interaction-service .

# Run container
docker run -p 8084:8084 \
  -e DB_HOST=host.docker.internal \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  -e OPENAI_API_KEY=your_key \
  crm-interaction-service
```

## API Documentation

Swagger UI: http://localhost:8084/swagger-ui.html

OpenAPI Spec: http://localhost:8084/api-docs

## Kafka Events

### Consumed Topics

- `case-events`: Case lifecycle events (CASE_CREATED, CASE_STATUS_CHANGED, etc.)

### Produced Topics

- `interaction-events`: Interaction events (INTERACTION_CREATED, SENTIMENT_ANALYZED, etc.)

## AI Features

### Summarization

Automatically generates 2-3 sentence summaries of interactions using GPT-4.

```json
POST /api/interactions/{id}/summary
```

### Sentiment Analysis

Analyzes customer sentiment and assigns a score:
- POSITIVE (0.7-1.0)
- NEUTRAL (0.4-0.6)
- NEGATIVE (0.0-0.3)

```json
POST /api/interactions/{id}/sentiment
```

## Database Schema

### Interactions Table

- `id`: UUID primary key
- `case_id`: Associated case
- `customer_id`: Customer identifier
- `agent_id`: Agent identifier
- `type`: Interaction type (CALL, EMAIL, CHAT, NOTE)
- `channel`: Communication channel
- `content`: Interaction content
- `transcription`: Audio transcription
- `summary`: AI-generated summary
- `sentiment`: Sentiment classification
- `sentiment_score`: Sentiment confidence score
- `duration_seconds`: Interaction duration
- `audio_url`: Audio file URL
- `attachment_count`: Number of attachments
- `metadata`: JSON metadata
- `created_at`, `updated_at`: Timestamps

### Attachments Table

- `id`: UUID primary key
- `interaction_id`: Foreign key to interactions
- `file_name`, `file_type`, `file_size`
- `file_url`: File storage URL
- `uploaded_by`: User identifier
- `created_at`: Timestamp

### Duality Views

- `interaction_dv`: JSON view for interactions with nested attachments
- `customer_timeline_dv`: Aggregated customer timeline

## Integration with Other Services

### Case Management Service

- Receives case events via Kafka
- Creates interactions when cases are created
- Links interactions to cases

### Agent Portal

- Provides timeline view of customer interactions
- Displays sentiment analysis
- Shows AI-generated summaries

### Copilot Service

- Uses interaction history for context
- Provides next-action suggestions
- Analyzes conversation patterns

## Monitoring

### Health Check

```
GET /actuator/health
```

### Metrics

```
GET /actuator/metrics
GET /actuator/prometheus
```

## Development

### Project Structure

```
src/main/java/com/vivo/crm/interaction/
├── domain/
│   ├── model/          # Entities
│   └── repository/     # JPA repositories
├── application/
│   ├── service/        # Business logic
│   └── dto/            # Data transfer objects
├── infrastructure/
│   ├── config/         # Configuration
│   └── messaging/      # Kafka consumers
└── interfaces/
    └── rest/           # REST controllers
```

### Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## License

Proprietary - Vivo CRM+
