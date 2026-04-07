CREATE TABLE IF NOT EXISTS reviews
(
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    advertisement_id     UUID REFERENCES advertisements (id) ON DELETE CASCADE,
    seller_id            UUID    NOT NULL,
    buyer_id             UUID    NOT NULL,
    rating               INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment              TEXT    NOT NULL,
    is_verified_purchase BOOLEAN          DEFAULT FALSE,
    is_anonymous         BOOLEAN          DEFAULT FALSE,
    created_at           TIMESTAMP        DEFAULT NOW(),
    updated_at           TIMESTAMP
);

CREATE INDEX idx_reviews_seller_id ON reviews (seller_id);
CREATE INDEX idx_reviews_advertisement_id ON reviews (advertisement_id);
CREATE INDEX idx_reviews_rating ON reviews (rating);
CREATE INDEX idx_reviews_created_at ON reviews (created_at DESC);