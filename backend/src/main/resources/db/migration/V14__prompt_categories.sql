-- Create prompt_categories table
CREATE TABLE prompt_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    is_system BOOLEAN DEFAULT TRUE NOT NULL
);

-- Seed categories
INSERT INTO prompt_categories (name, description, is_system) VALUES
('DEVELOPMENT', 'Software engineering, design patterns, and programming help.', TRUE),
('WRITING', 'Creative writing, editing, resume reviews, and blogging.', TRUE),
('ANALYSIS', 'Data analysis, statistics, and research workflows.', TRUE),
('EDUCATION', 'Teaching concepts, solving exercises, and exam preparation.', TRUE),
('INTERVIEW', 'Interview coaching, resume reviews, and technical screens.', TRUE),
('CODING', 'Algorithm puzzles, unit testing, and code reviews.', TRUE),
('BUSINESS', 'Project management, email drafting, and business plans.', TRUE),
('RESEARCH', 'Scientific reviews, literature synthesis, and paper notes.', TRUE),
('GENERAL', 'Generic chats and catch-all utilities.', TRUE),
('PRODUCTIVITY', 'Time management and automation helpers.', TRUE)
ON CONFLICT (name) DO NOTHING;

-- Alter prompt_profiles table
ALTER TABLE prompt_profiles 
ADD COLUMN category_id INTEGER REFERENCES prompt_categories(id) ON DELETE SET NULL,
ADD COLUMN visibility VARCHAR(20) DEFAULT 'PRIVATE' NOT NULL;
