
DROP TABLE IF EXISTS advertisement_photos CASCADE;

CREATE TABLE IF NOT EXISTS advertisement_photos (
    id               SERIAL      PRIMARY KEY,
    advertisement_id UUID        NOT NULL,
    photo_url        VARCHAR(500) NOT NULL,
    display_order    INTEGER     NOT NULL DEFAULT 0,
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_photo_advertisement FOREIGN KEY (advertisement_id) REFERENCES advertisements(id) ON DELETE CASCADE,
    CONSTRAINT chk_photo_url_length   CHECK (LENGTH(photo_url) <= 500),
    CONSTRAINT uq_display_order_per_ad UNIQUE (advertisement_id, display_order)
);

CREATE INDEX idx_photos_advertisement_id ON advertisement_photos(advertisement_id);
CREATE INDEX idx_photos_order            ON advertisement_photos(advertisement_id, display_order);
CREATE INDEX idx_photos_created_at       ON advertisement_photos(created_at);

-- MacBook Pro 14" M2
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e1111111-1111-1111-1111-111111111111'::uuid, 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&h=600&fit=crop&auto=format', 0),
  ('e1111111-1111-1111-1111-111111111111'::uuid, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&h=600&fit=crop&auto=format', 1);

-- Sony WH-1000XM5
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e2222222-2222-2222-2222-222222222222'::uuid, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=600&fit=crop&auto=format', 0),
  ('e2222222-2222-2222-2222-222222222222'::uuid, 'https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&h=600&fit=crop&auto=format', 1);

-- Монитор LG 27" 4K
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e3333333-3333-3333-3333-333333333333'::uuid, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&h=600&fit=crop&auto=format', 0),
  ('e3333333-3333-3333-3333-333333333333'::uuid, 'https://images.unsplash.com/photo-1593640495253-23196b27a87f?w=800&h=600&fit=crop&auto=format', 1);

-- Keychron K2 Pro
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e4444444-4444-4444-4444-444444444444'::uuid, 'https://images.unsplash.com/photo-1595225476474-63036a41e140?w=800&h=600&fit=crop&auto=format', 0),
  ('e4444444-4444-4444-4444-444444444444'::uuid, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&h=600&fit=crop&auto=format', 1);

-- iPad Air 5 (аукцион)
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e5555555-5555-5555-5555-555555555555'::uuid, 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&h=600&fit=crop&auto=format', 0),
  ('e5555555-5555-5555-5555-555555555555'::uuid, 'https://images.unsplash.com/photo-1611532736597-de2d4265fba3?w=800&h=600&fit=crop&auto=format', 1);

-- Электросамокат Xiaomi
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e6666666-6666-6666-6666-666666666666'::uuid, 'https://images.unsplash.com/photo-1601758125870-7e1c1c7d0e65?w=800&h=600&fit=crop&auto=format', 0),
  ('e6666666-6666-6666-6666-666666666666'::uuid, 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800&h=600&fit=crop&auto=format', 1);

-- Велосипед Stels
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e7777777-7777-7777-7777-777777777777'::uuid, 'https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=800&h=600&fit=crop&auto=format', 0),
  ('e7777777-7777-7777-7777-777777777777'::uuid, 'https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?w=800&h=600&fit=crop&auto=format', 1);

-- Задачник Демидовича
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e8888888-8888-8888-8888-888888888888'::uuid, 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800&h=600&fit=crop&auto=format', 0),
  ('e8888888-8888-8888-8888-888888888888'::uuid, 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=800&h=600&fit=crop&auto=format', 1);

-- Книги по ML
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('e9999999-9999-9999-9999-999999999999'::uuid, 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=800&h=600&fit=crop&auto=format', 0),
  ('e9999999-9999-9999-9999-999999999999'::uuid, 'https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&h=600&fit=crop&auto=format', 1);

-- Кресло Hara Chair
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('ea111111-1111-1111-1111-111111111111'::uuid, 'https://images.unsplash.com/photo-1580480055273-228ff5388ef8?w=800&h=600&fit=crop&auto=format', 0),
  ('ea111111-1111-1111-1111-111111111111'::uuid, 'https://images.unsplash.com/photo-1592078615290-033ee584e267?w=800&h=600&fit=crop&auto=format', 1);

-- Кофемашина DeLonghi
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('ea222222-2222-2222-2222-222222222222'::uuid, 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&h=600&fit=crop&auto=format', 0),
  ('ea222222-2222-2222-2222-222222222222'::uuid, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&h=600&fit=crop&auto=format', 1);

-- Nintendo Switch OLED
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('ea333333-3333-3333-3333-333333333333'::uuid, 'https://images.unsplash.com/photo-1589466342027-08e7bf2b1a67?w=800&h=600&fit=crop&auto=format', 0),
  ('ea333333-3333-3333-3333-333333333333'::uuid, 'https://images.unsplash.com/photo-1594652634010-275456c808d0?w=800&h=600&fit=crop&auto=format', 1);

-- Репетитор по математике (услуга)
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('ea444444-4444-4444-4444-444444444444'::uuid, 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=800&h=600&fit=crop&auto=format', 0);

-- Telegram-бот (услуга)
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('ea555555-5555-5555-5555-555555555555'::uuid, 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800&h=600&fit=crop&auto=format', 0);

-- Физика (услуга)
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('ea666666-6666-6666-6666-666666666666'::uuid, 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800&h=600&fit=crop&auto=format', 0);

-- Английский язык (услуга)
INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES
  ('ea777777-7777-7777-7777-777777777777'::uuid, 'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=800&h=600&fit=crop&auto=format', 0);
