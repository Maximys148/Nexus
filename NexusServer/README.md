# Nexus Server

Multi-provider AI gateway server with billing, authentication, and n8n integration.

## Features

- **Multi-Provider AI Support**: OpenAI, Anthropic, Gemini
- **Google OAuth2 Authentication**: Secure user login
- **Billing System**: Token-based balance management
- **n8n Integration**: Mini-assistant workflow delegation
- **PostgreSQL Database**: Persistent storage for users, transactions, and requests
- **Docker Support**: Easy deployment with docker-compose

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Client    │────▶│ NexusServer  │────▶│  AI Providers│
│  (React/Vue)│     │ (Spring Boot)│     │ (OpenAI, etc)│
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │   PostgreSQL │
                    └──────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │      n8n     │
                    │  (Workflows) │
                    └──────────────┘
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Google OAuth2 credentials
- API keys for AI providers (OpenAI, Anthropic, Gemini)

## Quick Start

### 1. Clone and Configure

```bash
cd NexusServer
cp .env.example .env
```

Edit `.env` and add your credentials:
- Google OAuth2 Client ID and Secret
- OpenAI API Key
- Anthropic API Key
- Gemini API Key
- JWT Secret (min 32 characters)

### 2. Run with Docker Compose

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Nexus Server (port 8080)
- n8n workflow engine (port 5678)

### 3. Verify Services

- Server: http://localhost:8080
- n8n: http://localhost:5678 (admin/admin)
- PostgreSQL: localhost:5432

## API Endpoints

### Authentication
- `GET /api/auth/google` - Initiate Google login
- `GET /api/auth/success` - Login success callback
- `GET /api/auth/failure` - Login failure callback
- `GET /api/auth/logout` - Logout

### User
- `GET /api/user/profile` - Get user profile
- `GET /api/user/balance` - Get user balance

### Chat
- `POST /api/chat` - Send chat request to AI provider
- `POST /api/chat/assistant` - Send request to n8n assistant

## Request Examples

### Chat Request

```json
POST /api/chat
{
  "provider": "openai",
  "model": "gpt-3.5-turbo",
  "message": "Hello, how are you?",
  "stream": false
}
```

### Assistant Request

```json
POST /api/chat/assistant
{
  "message": "Help me with my task",
  "workflowId": "my-workflow-123"
}
```

## Development

### Build from source

```bash
mvn clean package
```

### Run locally

```bash
mvn spring-boot:run
```

### Run tests

```bash
mvn test
```

## Database Schema

### Users
- id, googleId, email, name, avatarUrl, balance, enabled, createdAt, updatedAt

### Transactions
- id, userId, amount, type, status, description, providerTransactionId, createdAt

### Chat Requests
- id, userId, provider, model, message, response, tokensUsed, cost, status, createdAt, completedAt

## Configuration

Key configuration in `application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nexusdb
    username: nexus
    password: nexus_password

nexus:
  ai:
    providers:
      openai:
        api-key: ${OPENAI_API_KEY}
      anthropic:
        api-key: ${ANTHROPIC_API_KEY}
      gemini:
        api-key: ${GEMINI_API_KEY}
    n8n:
      webhook-url: ${N8N_WEBHOOK_URL}
```

## Next Steps

1. **Payment Integration**: Add Stripe/PayPal for balance top-up
2. **Streaming Support**: Implement SSE/WebSocket for real-time responses
3. **Rate Limiting**: Add request throttling per user
4. **Analytics**: Dashboard for usage statistics
5. **More Providers**: Add support for additional AI providers
6. **Audio/Video**: Extend support for audio and video processing

## License

MIT
