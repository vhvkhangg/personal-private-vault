-- V2__add_search_support.sql
-- Enables PostgreSQL pg_trgm extension and adds trigram indexes for high-value short fields used in global search.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Tags
CREATE INDEX IF NOT EXISTS idx_tags_name_trgm ON tags USING gin (lower(name) gin_trgm_ops);

-- People
CREATE INDEX IF NOT EXISTS idx_persons_name_trgm ON persons USING gin (lower(name) gin_trgm_ops);

-- Fiction
CREATE INDEX IF NOT EXISTS idx_fictions_title_trgm ON fictions USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_fictions_original_title_trgm ON fictions USING gin (lower(original_title) gin_trgm_ops);

-- Film
CREATE INDEX IF NOT EXISTS idx_films_title_trgm ON films USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_films_original_title_trgm ON films USING gin (lower(original_title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_film_credits_character_name_trgm ON film_credits USING gin (lower(character_name) gin_trgm_ops);

-- Media
CREATE INDEX IF NOT EXISTS idx_albums_title_trgm ON albums USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_images_title_trgm ON images USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_images_location_text_trgm ON images USING gin (lower(location_text) gin_trgm_ops);

-- Location
CREATE INDEX IF NOT EXISTS idx_brands_name_trgm ON brands USING gin (lower(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_locations_name_trgm ON locations USING gin (lower(name) gin_trgm_ops);

-- Knowledge
CREATE INDEX IF NOT EXISTS idx_study_items_title_trgm ON study_items USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_information_items_title_trgm ON information_items USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_vocabulary_items_word_trgm ON vocabulary_items USING gin (lower(word) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_notes_title_trgm ON notes USING gin (lower(title) gin_trgm_ops);

-- Collection
CREATE INDEX IF NOT EXISTS idx_music_tracks_title_trgm ON music_tracks USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_shopping_items_name_trgm ON shopping_items USING gin (lower(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_software_items_name_trgm ON software_items USING gin (lower(name) gin_trgm_ops);

-- Account
CREATE INDEX IF NOT EXISTS idx_external_accounts_display_name_trgm ON external_accounts USING gin (lower(display_name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_external_accounts_username_trgm ON external_accounts USING gin (lower(username) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_external_accounts_owner_name_trgm ON external_accounts USING gin (lower(owner_name) gin_trgm_ops);

-- Feed
CREATE INDEX IF NOT EXISTS idx_saved_resources_title_trgm ON saved_resources USING gin (lower(title) gin_trgm_ops);
