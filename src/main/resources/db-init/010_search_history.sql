DROP TABLE IF EXISTS search_history CASCADE;

CREATE TABLE IF NOT EXISTS search_history (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  search_text     TEXT,
  search_type     VARCHAR(20),
  categories      TEXT[], -- Массив категорий
  filters_json    JSONB, -- JSON с фильтрами для гибкости
  sort_order      VARCHAR(50),
  results_count   INTEGER DEFAULT 0,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_search_history_user_created
  ON search_history(user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_search_history_search_text
  ON search_history USING gin(to_tsvector('russian', search_text));

