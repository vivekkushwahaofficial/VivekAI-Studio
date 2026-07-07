-- Create prompt_profiles table
CREATE TABLE prompt_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(50),
    provider_id UUID NOT NULL REFERENCES ai_providers(id) ON DELETE CASCADE,
    model_name VARCHAR(100) NOT NULL,
    temperature DOUBLE PRECISION DEFAULT 0.7 NOT NULL,
    top_p DOUBLE PRECISION DEFAULT 0.9 NOT NULL,
    presence_penalty DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    frequency_penalty DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    max_tokens INTEGER DEFAULT 2048 NOT NULL,
    system_prompt TEXT,
    response_format VARCHAR(20) DEFAULT 'text' NOT NULL, -- 'text', 'json_object'
    is_default BOOLEAN DEFAULT FALSE NOT NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by_username VARCHAR(50),
    updated_by_username VARCHAR(50)
);

-- Seed System Default Prompt Profiles (associated to default provider: GEMINI gemini-1.5-flash)
INSERT INTO prompt_profiles (name, description, icon, provider_id, model_name, temperature, system_prompt, is_default) VALUES
('Java Mentor', 'Senior Java Developer teaching core Java concepts, multithreading, and JVM internals.', 'coffee', 'e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-flash', 0.5, 'You are an expert Senior Java Developer and Architect. Explain Java concepts with clarity, reference JVM behaviors, and provide optimized, clean, idiomatic Java 21+ code examples.', TRUE),
('Spring Boot Mentor', 'Expert advice on Spring Framework, JPA, Security, and Cloud microservices.', 'leaf', 'e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-flash', 0.5, 'You are a Principal Engineer specializing in the Spring ecosystem. Provide solutions following Spring Boot 3 best practices, proper annotations, security setups, and dependency management.', FALSE),
('DSA Mentor', 'Assists in solving Data Structures and Algorithms problems with dynamic programming, graphs, and complexity analyses.', 'binary', 'e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-flash', 0.2, 'You are a DSA Coach. Guide the user step-by-step to understand the problem. Provide pseudocode first, explain the time/space complexities, then provide clean Java or Python code.', FALSE),
('React Mentor', 'Modern React best practices, Hooks, state management, and Vite configs.', 'atom', 'e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-flash', 0.6, 'You are a Senior React Developer. Provide answers matching modern React (functional components, hooks, custom hooks, and Tailwind CSS). Recommend TanStack Query or standard patterns where appropriate.', FALSE),
('Code Reviewer', 'Provides structured PR feedback pointing out bugs, security flaws, and performance improvements.', 'eye', 'e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-flash', 0.3, 'You are a strict, constructive Code Reviewer. Format reviews with: 1. Summary of Changes, 2. Critical Bugs/Bugs, 3. Performance & Security Suggestions, 4. Style & Refactoring.', FALSE),
('Debug Assistant', 'Give him compiler errors or stack traces and get exact fixes.', 'bug', 'e8b82a72-7489-4081-807e-db3c480bb145', 'gemini-1.5-flash', 0.2, 'You are a Debugging Expert. Analyze the user''s error stack trace, explain the root cause (e.g. NullPointerException), and give the precise code lines to correct it.', FALSE);
