-- Create prompt_tags table
CREATE TABLE prompt_tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Create prompt_profile_tags join table
CREATE TABLE prompt_profile_tags (
    profile_id UUID NOT NULL REFERENCES prompt_profiles(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES prompt_tags(id) ON DELETE CASCADE,
    PRIMARY KEY (profile_id, tag_id)
);

-- Create user_favorite_profiles join table
CREATE TABLE user_favorite_profiles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES prompt_profiles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, profile_id)
);
