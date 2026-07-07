-- Create ai_models table
CREATE TABLE ai_models (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    provider_id UUID NOT NULL REFERENCES ai_providers(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    supports_stream BOOLEAN DEFAULT TRUE NOT NULL,
    supports_image BOOLEAN DEFAULT FALSE NOT NULL,
    supports_reasoning BOOLEAN DEFAULT FALSE NOT NULL,
    max_tokens INTEGER DEFAULT 4096 NOT NULL,
    default_temperature DOUBLE PRECISION DEFAULT 0.7 NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    UNIQUE (provider_id, name)
);

-- Seed OpenAI Models
INSERT INTO ai_models (provider_id, name, display_name, supports_image, supports_reasoning, max_tokens) VALUES
('4c7a522e-bf7d-411a-8c29-873b22b64a51', 'gpt-4o', 'GPT-4o', TRUE, FALSE, 4096),
('4c7a522e-bf7d-411a-8c29-873b22b64a51', 'gpt-4o-mini', 'GPT-4o Mini', TRUE, FALSE, 16384),
('4c7a522e-bf7d-411a-8c29-873b22b64a51', 'o1-mini', 'o1 Mini', FALSE, TRUE, 65536);

-- Seed Gemini Models
INSERT INTO ai_models (provider_id, name, display_name, supports_image, supports_reasoning, max_tokens) VALUES
('e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-pro', 'Gemini 1.5 Pro', TRUE, FALSE, 8192),
('e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-flash', 'Gemini 1.5 Flash', TRUE, FALSE, 8192);

-- Seed Claude Models
INSERT INTO ai_models (provider_id, name, display_name, supports_image, supports_reasoning, max_tokens) VALUES
('3e16b9b1-e28e-4a6c-94eb-0d3ee787db8c', 'claude-3-5-sonnet-latest', 'Claude 3.5 Sonnet', TRUE, FALSE, 8192),
('3e16b9b1-e28e-4a6c-94eb-0d3ee787db8c', 'claude-3-haiku-20240307', 'Claude 3 Haiku', TRUE, FALSE, 4096);

-- Seed DeepSeek Models
INSERT INTO ai_models (provider_id, name, display_name, supports_image, supports_reasoning, max_tokens) VALUES
('f7cb78e4-8fbb-49e0-82a8-12d711c210d3', 'deepseek-chat', 'DeepSeek-V3', FALSE, FALSE, 8192),
('f7cb78e4-8fbb-49e0-82a8-12d711c210d3', 'deepseek-reasoner', 'DeepSeek-R1 (Reasoning)', FALSE, TRUE, 8192);

-- Seed Groq Models
INSERT INTO ai_models (provider_id, name, display_name, supports_image, supports_reasoning, max_tokens) VALUES
('7df56214-e59e-4c07-b3ea-151052601ad8', 'llama-3.1-70b-versatile', 'Llama 3.1 70B', FALSE, FALSE, 8192),
('7df56214-e59e-4c07-b3ea-151052601ad8', 'mixtral-8x7b-32768', 'Mixtral 8x7B', FALSE, FALSE, 32768);

-- Seed OpenRouter Models
INSERT INTO ai_models (provider_id, name, display_name, supports_image, supports_reasoning, max_tokens) VALUES
('9a6c9cf1-02a8-4e1b-87b3-241ad8b1825b', 'meta-llama/llama-3-8b-instruct', 'Llama 3 8B (via OpenRouter)', FALSE, FALSE, 8192);

-- Seed Ollama Models
INSERT INTO ai_models (provider_id, name, display_name, supports_image, supports_reasoning, max_tokens) VALUES
('3b6c2dbf-6d4b-4b2a-89a1-77df98f6d610', 'llama3', 'Llama 3 (Local)', FALSE, FALSE, 4096);
