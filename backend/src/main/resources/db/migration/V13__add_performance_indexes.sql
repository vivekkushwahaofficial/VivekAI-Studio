-- Indexes for users table queries
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Indexes for active refresh tokens search
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- Indexes for model selections and provider queries
CREATE INDEX idx_ai_models_provider ON ai_models(provider_id);

-- Indexes for prompt profiles
CREATE INDEX idx_prompt_profiles_provider ON prompt_profiles(provider_id);
CREATE INDEX idx_prompt_profiles_creator ON prompt_profiles(created_by);

-- Indexes for workspaces
CREATE INDEX idx_workspaces_owner ON workspaces(owner_id);

-- Indexes for active folders
CREATE INDEX idx_folders_workspace ON folders(workspace_id);

-- Indexes for chats
CREATE INDEX idx_conversations_workspace ON conversations(workspace_id);
CREATE INDEX idx_conversations_creator ON conversations(created_by);
CREATE INDEX idx_conversations_folder ON conversations(folder_id);

-- Indexes for messages
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_provider ON messages(provider_id);

-- Indexes for usage logs analytics
CREATE INDEX idx_usage_logs_user ON usage_logs(user_id);
CREATE INDEX idx_usage_logs_created_at ON usage_logs(created_at);

-- Indexes for audit security events
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_event_type ON audit_logs(event_type);
