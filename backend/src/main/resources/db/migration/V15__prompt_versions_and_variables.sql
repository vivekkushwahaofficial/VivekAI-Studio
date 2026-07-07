-- Create prompt_profile_versions table
CREATE TABLE prompt_profile_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID NOT NULL REFERENCES prompt_profiles(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    system_prompt TEXT,
    temperature DOUBLE PRECISION DEFAULT 0.7 NOT NULL,
    max_tokens INTEGER DEFAULT 2048 NOT NULL,
    top_p DOUBLE PRECISION DEFAULT 0.9 NOT NULL,
    presence_penalty DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    frequency_penalty DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
    response_format VARCHAR(20) DEFAULT 'text' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE (profile_id, version_number)
);

-- Create prompt_variables table
CREATE TABLE prompt_variables (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID NOT NULL REFERENCES prompt_profiles(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    is_required BOOLEAN DEFAULT TRUE NOT NULL,
    default_value VARCHAR(255),
    type VARCHAR(20) DEFAULT 'STRING' NOT NULL, -- 'STRING', 'NUMBER', 'BOOLEAN', 'SELECT', 'MULTILINE'
    UNIQUE (profile_id, name)
);

-- Alter prompt_profiles to drop parameter columns (since they are now versioned)
ALTER TABLE prompt_profiles 
DROP COLUMN system_prompt,
DROP COLUMN temperature,
DROP COLUMN max_tokens,
DROP COLUMN top_p,
DROP COLUMN presence_penalty,
DROP COLUMN frequency_penalty,
DROP COLUMN response_format;
