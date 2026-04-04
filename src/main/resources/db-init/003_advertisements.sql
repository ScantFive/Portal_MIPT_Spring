CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP TABLE IF EXISTS advertisements CASCADE;

CREATE TABLE IF NOT EXISTS advertisements (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('ACTIVE', 'DRAFT', 'PAUSED', 'DELETED')),
    author      UUID NOT NULL,
    type        VARCHAR(20) NOT NULL CHECK (type IN ('OBJECTS', 'SERVICES')),
    category    TEXT DEFAULT '',
    name        TEXT NOT NULL,
    price       BIGINT,
    description TEXT,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_name_length CHECK (LENGTH(name) >= 3 AND LENGTH(name) <= 255),
    CONSTRAINT chk_description_length CHECK (LENGTH(description) <= 5000),
    CONSTRAINT chk_price_positive CHECK (price IS NULL OR price > 0),
    CONSTRAINT chk_category_length CHECK (LENGTH(category) <= 1000)
);

CREATE INDEX idx_advertisements_author ON advertisements(author);
CREATE INDEX idx_advertisements_status ON advertisements(status);
CREATE INDEX idx_advertisements_type ON advertisements(type);
CREATE INDEX idx_advertisements_category ON advertisements(category);
CREATE INDEX idx_advertisements_created_at ON advertisements(created_at);
CREATE INDEX idx_advertisements_status_created ON advertisements(status, created_at DESC);
CREATE INDEX idx_advertisements_price ON advertisements(price) WHERE price IS NOT NULL;
CREATE INDEX idx_advertisements_favorite ON advertisements(is_favorite) WHERE is_favorite = TRUE;
CREATE INDEX idx_advertisements_name ON advertisements(name);

ALTER TABLE advertisements ADD COLUMN search_vector tsvector
  GENERATED ALWAYS AS (
    setweight(to_tsvector('russian', coalesce(name, '')), 'A') ||
    setweight(to_tsvector('russian', coalesce(description, '')), 'B') ||
    setweight(to_tsvector('russian', coalesce(category, '')), 'C')
  ) STORED;


CREATE INDEX idx_advertisements_search_vector ON advertisements USING GIN(search_vector);
CREATE INDEX idx_advertisements_name_trgm ON advertisements USING gin(name gin_trgm_ops);

INSERT INTO advertisements (id, status, author, type, category, name, price, description) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 'ACTIVE', '11111111-1111-1111-1111-111111111111'::uuid, 'OBJECTS',  'Товары/Книги/Учебники', 'Учебники по SQL', 1000, 'Книги по SQL с примерами.'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, 'ACTIVE', '22222222-2222-2222-2222-222222222222'::uuid, 'OBJECTS',  'Товары/Инструменты/Отвертки', 'Набор отверток', 150,  'Продаю отвертки!'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, 'ACTIVE', '33333333-3333-3333-3333-333333333333'::uuid, 'SERVICES', 'Услуги/Образование/Программирование', 'Репетитор по программированию', 2500, 'Я - профессионал)'),
  ('d1111111-1111-1111-1111-111111111111'::uuid, 'ACTIVE', '11111111-1111-1111-1111-111111111111'::uuid, 'SERVICES', 'Услуги/Образование/Математика', 'Репетитор по математике', 2000, 'Помощь с подготовкой к экзаменам по математике и физике. Опыт работы 5 лет.'),
  ('d2222222-2222-2222-2222-222222222222'::uuid, 'ACTIVE', '22222222-2222-2222-2222-222222222222'::uuid, 'OBJECTS',  'Товары/Электроника/Ноутбуки', 'Ноутбук для программирования', 45000, 'Мощный ноутбук для разработки. Intel i7, 16GB RAM, SSD 512GB.'),
  ('d3333333-3333-3333-3333-333333333333'::uuid, 'ACTIVE', '33333333-3333-3333-3333-333333333333'::uuid, 'SERVICES', 'Услуги/Образование/Физика', 'Помощь с физикой', 1800, 'Репетитор по физике для студентов и школьников. Подготовка к ЕГЭ.'),
  ('d4444444-4444-4444-4444-444444444444'::uuid, 'ACTIVE', '11111111-1111-1111-1111-111111111111'::uuid, 'OBJECTS',  'Товары/Книги/Научные', 'Книги по Java', 800, 'Учебники по Java для начинающих и продвинутых. Head First Java, Effective Java.'),
  ('d5555555-5555-5555-5555-555555555555'::uuid, 'ACTIVE', '22222222-2222-2222-2222-222222222222'::uuid, 'SERVICES', 'Услуги/IT/Веб-разработка', 'Разработка веб-сайтов', 50000, 'Создание современных веб-приложений на React и Spring Boot.'),
  ('d6666666-6666-6666-6666-666666666666'::uuid, 'ACTIVE', '33333333-3333-3333-3333-333333333333'::uuid, 'OBJECTS',  'Товары/Книги/Учебники', 'Учебники по Python', 1200, 'Книги по Python и машинному обучению. Автоматизация рутинных задач.'),
  ('d7777777-7777-7777-7777-777777777777'::uuid, 'ACTIVE', '11111111-1111-1111-1111-111111111111'::uuid, 'SERVICES', 'Услуги/Образование/Языки', 'Репетитор английского языка', 2200, 'Подготовка к IELTS, TOEFL. Разговорный английский для программистов.'),
  ('d8888888-8888-8888-8888-888888888888'::uuid, 'ACTIVE', '22222222-2222-2222-2222-222222222222'::uuid, 'OBJECTS',  'Товары/Электроника/Клавиатуры', 'Механическая клавиатура', 5500, 'Игровая механическая клавиатура с подсветкой RGB. Идеально для программирования.'),
  ('d9999999-9999-9999-9999-999999999999'::uuid, 'ACTIVE', '33333333-3333-3333-3333-333333333333'::uuid, 'SERVICES', 'Услуги/Консультации/Программирование', 'Консультации по Java', 3000, 'Помощь с выполнением домашних заданий по Java. Code review и менторинг.'),
  ('da111111-1111-1111-1111-111111111111'::uuid, 'ACTIVE', '11111111-1111-1111-1111-111111111111'::uuid, 'OBJECTS',  'Товары/Мебель/Столы', 'Компьютерный стол', 8000, 'Удобный стол для работы за компьютером. Регулируемая высота.'),
  ('da222222-2222-2222-2222-222222222222'::uuid, 'ACTIVE', '22222222-2222-2222-2222-222222222222'::uuid, 'SERVICES', 'Услуги/Образование/Химия', 'Репетитор по химии', 1900, 'Помощь студентам с органической и неорганической химией.'),
  ('da333333-3333-3333-3333-333333333333'::uuid, 'ACTIVE', '33333333-3333-3333-3333-333333333333'::uuid, 'OBJECTS',  'Товары/Книги/Учебники', 'Учебники по алгоритмам', 1500, 'Книги по алгоритмам и структурам данных. Кормен, Седжвик.'),
  ('da444444-4444-4444-4444-444444444444'::uuid, 'ACTIVE', '11111111-1111-1111-1111-111111111111'::uuid, 'SERVICES', 'Услуги/IT/Мобильная разработка', 'Разработка мобильных приложений', 60000, 'Создание приложений для Android и iOS. Kotlin, Swift, Flutter.'),
  ('da555555-5555-5555-5555-555555555555'::uuid, 'ACTIVE', '22222222-2222-2222-2222-222222222222'::uuid, 'OBJECTS',  'Товары/Электроника/Мыши', 'Беспроводная мышь', 1500, 'Эргономичная беспроводная мышь для длительной работы.'),
  ('da666666-6666-6666-6666-666666666666'::uuid, 'ACTIVE', '33333333-3333-3333-3333-333333333333'::uuid, 'SERVICES', 'Услуги/Образование/Программирование', 'Курсы по C++', 4000, 'Обучение программированию на C++. Подготовка к олимпиадам и экзаменам.');