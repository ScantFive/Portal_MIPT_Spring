DROP TABLE IF EXISTS favorites CASCADE;

CREATE TABLE IF NOT EXISTS favorites (
    user_id          UUID NOT NULL
        REFERENCES users (id)
        ON DELETE CASCADE,
    advertisement_id UUID NOT NULL
        REFERENCES advertisements (id)
        ON DELETE CASCADE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, advertisement_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_user_id
    ON favorites (user_id);

CREATE INDEX IF NOT EXISTS idx_favorites_advertisement_id
    ON favorites (advertisement_id);


