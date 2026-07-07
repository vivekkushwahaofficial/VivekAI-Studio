# Architecture Documentation

## Database Schema & Domain Model

VivekAI Studio uses PostgreSQL as the central datastore, managed using Flyway migrations.

### Architecture Design Decisions

1. **UUID Primary Keys**: Primary identifiers of entities interacting with front-facing REST endpoints (Users, Workspaces, Conversations, Messages, Prompt Profiles, Usage logs) are RFC 4122 UUIDs. This prevents ID enumeration attacks.
2. **Provider & Models Split**: `ai_providers` maps high-level API namespaces. `ai_models` keeps tracks of individual model specs (e.g. streaming options, image/vision support, tokens, reasoning capabilities).
3. **Auditing Lifecycle Columns**: Track `created_at`, `updated_at`, `created_by`, and `updated_by` via JPA Auditing (`AuditConfig.java`).
4. **Separated Usage, Audit, and System Logs**:
   - `usage_logs`: Analytical statistics tracking token ingestion and estimated costs.
   - `audit_logs`: Detailed tracking of security events, role changes, and provider status alterations.
5. **No Key Storage in Database**: Third-party API keys are defined solely via environment variables and loaded via the backend configuration.
