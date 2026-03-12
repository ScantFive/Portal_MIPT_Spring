DROP TABLE IF EXISTS operations CASCADE;

CREATE TABLE IF NOT EXISTS operations (
  id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  client    UUID REFERENCES users(id) ON DELETE SET NULL,
  performer UUID REFERENCES users(id) ON DELETE SET NULL,
  amount    BIGINT,
  type      TEXT DEFAULT '',
  time      TIMESTAMP DEFAULT now(),
  title     TEXT
);

INSERT INTO operations (id, client, performer, amount, type, time, title) VALUES
  (gen_random_uuid(), '33333333-3333-3333-3333-333333333333'::uuid, '11111111-1111-1111-1111-111111111111'::uuid, 1000, 'PAY',  now(),                    'Оплата за книгу'),
  (gen_random_uuid(), '33333333-3333-3333-3333-333333333333'::uuid, '22222222-2222-2222-2222-222222222222'::uuid, 150,  'PAY',  now() - INTERVAL '1 day', 'Оплата за отвертку');
