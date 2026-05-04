-- Создаём таблицу только если не существует — данные (telegram_chat_id и т.д.) сохраняются при перезапуске
CREATE TABLE IF NOT EXISTS users (
  id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  login             VARCHAR(50) UNIQUE NOT NULL,
  email             VARCHAR(50) NOT NULL,
  hashed_password   TEXT        NOT NULL,
  activated         BOOLEAN     DEFAULT FALSE,
  activation_token  VARCHAR(255),
  telegram_username VARCHAR(100) UNIQUE,
  telegram_chat_id  BIGINT UNIQUE
);

-- Добавляем новые колонки если их ещё нет (при обновлении схемы)
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_username VARCHAR(100) UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS telegram_chat_id BIGINT UNIQUE;

-- Seed-данные: вставляем только если пользователь ещё не существует
INSERT INTO users (id, login, email, hashed_password, activated, telegram_username) VALUES
  ('11111111-1111-1111-1111-111111111111'::uuid, 'timofei',  'timofei@phystech.edu',  '$2a$10$LawAR92eqe9jVoxmVfX2JOZUZ6Je.3v.rqwPhmu3HeE.Ab1VMAg2W', TRUE, 'timofei_mipt'),
  ('22222222-2222-2222-2222-222222222222'::uuid, 'vladimir', 'vladimir@phystech.edu', '$2a$10$aq6FdCM/igtuxf2NlXO0gOM./zWeR5gkTYi4VCZxCQuxXyRI86GGK', TRUE, 'vladimir_phystech'),
  ('33333333-3333-3333-3333-333333333333'::uuid, 'daniil',   'daniil@phystech.edu',   '$2a$10$1NW8yH1fw5E65.HvFZK23uVKDqH9xesBx0OMSi86DsabhHtFUoJaS', TRUE,  NULL),
  ('44444444-4444-4444-4444-444444444444'::uuid, 'gleb',     'gleb@phystech.edu',     '$2a$10$VuPilYCxaYSaaCMvBSjUdeEBT0eLyElD2HtWzc4n1O0HqLFsxWS2e', TRUE, 'gleb_phystech'),
  ('55555555-5555-5555-5555-555555555555'::uuid, 'anna',     'anna@phystech.edu',     '$2a$10$uSP3K63CyIwAWDBUd1ElVOnzWPEIzTyawmAxDRiL6pN8HOwA0KCxi', TRUE, 'anna_mipt'),
  ('66666666-6666-6666-6666-666666666666'::uuid, 'max',      'max@phystech.edu',      '$2a$10$DiO0UblQaIekjBFe1YgQ8eHLZO9X1M8byomzAx3Za9NGH4I6taCRS', TRUE,  NULL),
  ('77777777-7777-7777-7777-777777777777'::uuid, 'katya',    'katya@phystech.edu',    '$2a$10$VuPilYCxaYSaaCMvBSjUdeEBT0eLyElD2HtWzc4n1O0HqLFsxWS2e', TRUE, NULL),
  ('88888888-8888-8888-8888-888888888888'::uuid, 'sergei',   'sergei@phystech.edu',   '$2a$10$VuPilYCxaYSaaCMvBSjUdeEBT0eLyElD2HtWzc4n1O0HqLFsxWS2e', TRUE, NULL),
  ('99999999-9999-9999-9999-999999999999'::uuid, 'lera',     'lera@phystech.edu',     '$2a$10$VuPilYCxaYSaaCMvBSjUdeEBT0eLyElD2HtWzc4n1O0HqLFsxWS2e', TRUE, NULL)
ON CONFLICT (id) DO NOTHING;
