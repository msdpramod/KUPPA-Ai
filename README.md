# KUPPA AI

**Principle:** Think like me. Act only with my approval.

KUPPA AI is a human-in-the-loop personal AI. The AI may understand, recommend and propose actions, but the execution layer refuses any action that has not been explicitly approved.

## MVP implemented

- Chat REST API and minimal web UI
- Persistent persona memory
- Proposed-action model with risk level
- Approve / Reject workflow
- Hard execution gate (`APPROVED` required in code)
- Audit events for proposal, approval, rejection, blocked execution and execution
- H2 local persistence by default
- PostgreSQL Docker configuration
- Safe local planner requiring no API key
- Adapter boundary (`Planner`) ready for a real LLM
- Unit tests for the approval boundary

## Architecture

```text
User -> ChatController -> ChatService -> Planner
                                  |        |
                                  |        +-> Persona Memory
                                  v
                           ProposedAction
                                  |
                        PENDING_APPROVAL
                          /            \
                      Reject          Approve
                        |                |
                       STOP       ApprovalService
                                         |
                                 Execution Gate
                                         |
                              Tool Adapter (future)
                                         |
                                     Audit Log
```

## API

```text
POST /api/chat
GET  /api/actions
POST /api/actions/{id}/approve
POST /api/actions/{id}/reject
POST /api/actions/{id}/execute
GET  /api/memory
POST /api/memory
GET  /api/audit
GET  /actuator/health
```

Example:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"Send a reply to the recruiter saying I am interested"}'
```

The response can contain a proposed action in `PENDING_APPROVAL`. Calling `/execute` before `/approve` is rejected by the server.

## Run

Requires Java 21 and Maven 3.9+.

```bash
mvn test
mvn spring-boot:run
```

Open `http://localhost:8080`.

For PostgreSQL:

```bash
mvn clean package
docker compose up --build
```

## Next build

1. Real LLM adapter with structured output.
2. Authentication/owner identity and signed approvals.
3. Gmail + Calendar tool adapters behind the same execution gate.
4. Redis short-term context and idempotency.
5. Semantic memory / embeddings with provenance and memory approval.
6. Tool-specific risk policies and action expiry.
7. WebSocket/SSE live UI.
8. Secrets management, rate limits, OpenTelemetry, Prometheus/Grafana.

No external connector should bypass `ApprovalService` / the execution gate.
