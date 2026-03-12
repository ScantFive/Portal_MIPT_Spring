DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS users (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  login           VARCHAR(50) UNIQUE NOT NULL,
  email           VARCHAR(50) NOT NULL,
  hashed_password TEXT NOT NULL,
  activated       BOOLEAN DEFAULT FALSE
);

INSERT INTO users (id, login, email, hashed_password, activated) VALUES
  ('11111111-1111-1111-1111-111111111111'::uuid, 'timofei',  'timofei@phystech.edu',  '$2a$10$LawAR92eqe9jVoxmVfX2JOZUZ6Je.3v.rqwPhmu3HeE.Ab1VMAg2W', TRUE),
  ('22222222-2222-2222-2222-222222222222'::uuid, 'vladimir', 'vladimir@phystech.edu', '$2a$10$aq6FdCM/igtuxf2NlXO0gOM./zWeR5gkTYi4VCZxCQuxXyRI86GGK', TRUE),
  ('33333333-3333-3333-3333-333333333333'::uuid, 'daniil',   'daniil@phystech.edu',   '$2a$10$1NW8yH1fw5E65.HvFZK23uVKDqH9xesBx0OMSi86DsabhHtFUoJaS', TRUE),
  ('44444444-4444-4444-4444-444444444444'::uuid, 'gleb',     'gleb@phystech.edu',     '$2a$10$12pMllZvNSFXalhHo57sLOUOcv1abB31ai2TacqusBLbTaUAw2Asm', TRUE),
  ('55555555-5555-5555-5555-555555555555'::uuid, 'anna',     'anna@phystech.edu',     '$2a$10$uSP3K63CyIwAWDBUd1ElVOnzWPEIzTyawmAxDRiL6pN8HOwA0KCxi', TRUE),
  ('66666666-6666-6666-6666-666666666666'::uuid, 'max',      'max@phystech.edu',      '$2a$10$DiO0UblQaIekjBFe1YgQ8eHLZO9X1M8byomzAx3Za9NGH4I6taCRS', TRUE);
