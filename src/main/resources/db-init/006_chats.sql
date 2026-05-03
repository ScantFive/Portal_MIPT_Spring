DROP TABLE IF EXISTS chats CASCADE;

CREATE TABLE IF NOT EXISTS chats (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  owner       UUID REFERENCES users(id) ON DELETE CASCADE,
  member      UUID REFERENCES users(id) ON DELETE CASCADE,
  last_update TIMESTAMP DEFAULT now(),
  UNIQUE(owner, member)
);

-- Сокращения: 11=timofei 22=vladimir 33=daniil 44=gleb 55=anna 66=max 77=katya 88=sergei 99=lera
INSERT INTO chats (id, owner, member, last_update) VALUES
  -- lera спрашивает про MacBook (99→11)
  ('c1111111-1111-1111-1111-111111111111'::uuid,
   '99999999-9999-9999-9999-999999999999'::uuid,
   '11111111-1111-1111-1111-111111111111'::uuid,
   NOW() - INTERVAL '25 minutes'),

  -- sergei смотрит клавиатуру Keychron (88→44)
  ('c2222222-2222-2222-2222-222222222222'::uuid,
   '88888888-8888-8888-8888-888888888888'::uuid,
   '44444444-4444-4444-4444-444444444444'::uuid,
   NOW() - INTERVAL '3 hours'),

  -- katya про аукцион iPad (77→55)
  ('c3333333-3333-3333-3333-333333333333'::uuid,
   '77777777-7777-7777-7777-777777777777'::uuid,
   '55555555-5555-5555-5555-555555555555'::uuid,
   NOW() - INTERVAL '1 hour'),

  -- katya хочет самокат (77→66)
  ('c4444444-4444-4444-4444-444444444444'::uuid,
   '77777777-7777-7777-7777-777777777777'::uuid,
   '66666666-6666-6666-6666-666666666666'::uuid,
   NOW() - INTERVAL '5 hours'),

  -- lera про кофемашину (99→55)
  ('c5555555-5555-5555-5555-555555555555'::uuid,
   '99999999-9999-9999-9999-999999999999'::uuid,
   '55555555-5555-5555-5555-555555555555'::uuid,
   NOW() - INTERVAL '40 minutes'),

  -- sergei заказывает Telegram-бота (88→22)
  ('c6666666-6666-6666-6666-666666666666'::uuid,
   '88888888-8888-8888-8888-888888888888'::uuid,
   '22222222-2222-2222-2222-222222222222'::uuid,
   NOW() - INTERVAL '2 hours'),

  -- gleb ищет репетитора по матану (44→11)
  ('c7777777-7777-7777-7777-777777777777'::uuid,
   '44444444-4444-4444-4444-444444444444'::uuid,
   '11111111-1111-1111-1111-111111111111'::uuid,
   NOW() - INTERVAL '6 hours'),

  -- katya про Nintendo Switch (77→33)
  ('c8888888-8888-8888-8888-888888888888'::uuid,
   '77777777-7777-7777-7777-777777777777'::uuid,
   '33333333-3333-3333-3333-333333333333'::uuid,
   NOW() - INTERVAL '4 hours'),

  -- max занимается английским (66→55)
  ('c9999999-9999-9999-9999-999999999999'::uuid,
   '66666666-6666-6666-6666-666666666666'::uuid,
   '55555555-5555-5555-5555-555555555555'::uuid,
   NOW() - INTERVAL '50 minutes'),

  -- sergei берёт книги по ML (88→33)
  ('ca111111-1111-1111-1111-111111111111'::uuid,
   '88888888-8888-8888-8888-888888888888'::uuid,
   '33333333-3333-3333-3333-333333333333'::uuid,
   NOW() - INTERVAL '7 hours');
