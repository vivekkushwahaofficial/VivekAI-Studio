-- Alter conversations table to link to immutable version snapshots
ALTER TABLE conversations 
DROP COLUMN prompt_profile_id,
ADD COLUMN prompt_profile_version_id UUID REFERENCES prompt_profile_versions(id) ON DELETE SET NULL;
