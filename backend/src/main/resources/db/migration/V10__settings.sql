-- Create user settings table
CREATE TABLE settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    theme VARCHAR(20) DEFAULT 'dark' NOT NULL,
    language VARCHAR(10) DEFAULT 'en' NOT NULL,
    font_size INTEGER DEFAULT 14 NOT NULL,
    response_style VARCHAR(20) DEFAULT 'balanced' NOT NULL, -- 'creative', 'balanced', 'precise'
    streaming_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    notifications_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    keyboard_shortcuts_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(50)
);
