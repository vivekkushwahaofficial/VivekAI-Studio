-- Create messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL, -- 'SYSTEM', 'USER', 'ASSISTANT'
    content TEXT NOT NULL,
    provider_id UUID REFERENCES ai_providers(id) ON DELETE SET NULL,
    model_used VARCHAR(100),
    token_input INTEGER DEFAULT 0,
    token_output INTEGER DEFAULT 0,
    latency_ms BIGINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'SUCCESS' NOT NULL, -- 'SUCCESS', 'FAILED', 'IN_PROGRESS'
    error_message TEXT,
    attachments TEXT, -- Stores JSON-serialized array of attachment file metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by_username VARCHAR(50),
    updated_by_username VARCHAR(50)
);
