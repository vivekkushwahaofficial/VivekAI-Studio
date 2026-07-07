-- Create ai_providers table
CREATE TABLE ai_providers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Seed providers
INSERT INTO ai_providers (id, name, is_enabled) VALUES
('4c7a522e-bf7d-411a-8c29-873b22b64a51', 'OPENAI', TRUE),
('e8b82a72-7489-4081-807e-db3c480bb145', 'GEMINI', TRUE),
('3e16b9b1-e28e-4a6c-94eb-0d3ee787db8c', 'CLAUDE', TRUE),
('f7cb78e4-8fbb-49e0-82a8-12d711c210d3', 'DEEPSEEK', TRUE),
('7df56214-e59e-4c07-b3ea-151052601ad8', 'GROQ', TRUE),
('9a6c9cf1-02a8-4e1b-87b3-241ad8b1825b', 'OPENROUTER', TRUE),
('3b6c2dbf-6d4b-4b2a-89a1-77df98f6d610', 'OLLAMA', TRUE)
ON CONFLICT (name) DO NOTHING;
