DROP TABLE IF EXISTS messages CASCADE;

CREATE TABLE IF NOT EXISTS messages (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  chat         UUID REFERENCES chats(id) ON DELETE CASCADE,
  sender       UUID REFERENCES users(id) ON DELETE SET NULL,
  text         TEXT,
  sending_time TIMESTAMP DEFAULT now(),
  editing_time TIMESTAMP DEFAULT TO_TIMESTAMP('1970-01-01', 'YYYY-MM-DD'),
  is_read      BOOLEAN DEFAULT FALSE
);

INSERT INTO messages (id, chat, sender, text, sending_time, editing_time, is_read) 
SELECT gen_random_uuid(), c.id, '11111111-1111-1111-1111-111111111111'::uuid, 'Привет, интересует объявление?', now() - INTERVAL '2 hours', TO_TIMESTAMP('1970-01-01','YYYY-MM-DD'), TRUE
FROM chats c
WHERE c.owner = '11111111-1111-1111-1111-111111111111'::uuid AND c.member = '22222222-2222-2222-2222-222222222222'::uuid
UNION ALL
SELECT gen_random_uuid(), c.id, '22222222-2222-2222-2222-222222222222'::uuid, 'Да, в наличии. Когда удобно встретиться?', now() - INTERVAL '1 hour', TO_TIMESTAMP('1970-01-01','YYYY-MM-DD'), FALSE
FROM chats c
WHERE c.owner = '11111111-1111-1111-1111-111111111111'::uuid AND c.member = '22222222-2222-2222-2222-222222222222'::uuid
UNION ALL
SELECT gen_random_uuid(), c.id, '33333333-3333-3333-3333-333333333333'::uuid, 'Я могу забрать завтра', now(), TO_TIMESTAMP('1970-01-01','YYYY-MM-DD'), FALSE
FROM chats c
WHERE c.owner = '11111111-1111-1111-1111-111111111111'::uuid AND c.member = '33333333-3333-3333-3333-333333333333'::uuid;
