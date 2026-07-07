-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(50),
    event_type VARCHAR(50) NOT NULL, -- e.g. 'AUTHENTICATION', 'SECURITY_BREACH', 'ROLE_CHANGE', 'PROVIDER_MODIFIED', 'USER_DELETED'
    action VARCHAR(255) NOT NULL, -- Short description of the exact action taken
    resource VARCHAR(100), -- Entity affected
    details TEXT, -- Additional contextual parameters or error message details
    client_ip VARCHAR(45), -- Holds IPv4/IPv6 client address
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
