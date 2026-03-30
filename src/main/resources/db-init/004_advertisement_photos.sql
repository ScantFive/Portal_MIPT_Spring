
DROP TABLE IF EXISTS advertisement_photos CASCADE;

CREATE TABLE IF NOT EXISTS advertisement_photos (

    id SERIAL PRIMARY KEY,
    advertisement_id UUID NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_photo_advertisement FOREIGN KEY (advertisement_id) REFERENCES advertisements(id) ON DELETE CASCADE,
    CONSTRAINT chk_photo_url_length CHECK (LENGTH(photo_url) <= 500),
    CONSTRAINT uq_display_order_per_ad UNIQUE (advertisement_id, display_order)
);

CREATE INDEX idx_photos_advertisement_id ON advertisement_photos(advertisement_id);
CREATE INDEX idx_photos_order ON advertisement_photos(advertisement_id, display_order);
CREATE INDEX idx_photos_created_at ON advertisement_photos(created_at);

INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 'https://example.com/sql-book-cover.jpg', 0),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 'https://example.com/sql-book-back.jpg', 1),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, 'https://example.com/screwdrivers-set.jpg', 0);
