CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP TABLE IF EXISTS advertisements CASCADE;

CREATE TABLE IF NOT EXISTS advertisements (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('ACTIVE', 'DRAFT', 'PAUSED', 'DELETED')),
    author            UUID        NOT NULL,
    type              VARCHAR(20) NOT NULL CHECK (type IN ('OBJECTS', 'SERVICES')),
    category          TEXT        DEFAULT '',
    name              TEXT        NOT NULL,
    price             BIGINT,
    description       TEXT,
    is_favorite       BOOLEAN     NOT NULL DEFAULT FALSE,
    is_auction        BOOLEAN     NOT NULL DEFAULT FALSE,
    auction_ends_at   TIMESTAMPTZ,
    auction_closed_at TIMESTAMPTZ,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_name_length        CHECK (LENGTH(name) >= 3 AND LENGTH(name) <= 255),
    CONSTRAINT chk_description_length CHECK (LENGTH(description) <= 5000),
    CONSTRAINT chk_price_positive     CHECK (price IS NULL OR price > 0),
    CONSTRAINT chk_category_length    CHECK (LENGTH(category) <= 1000)
);

CREATE INDEX idx_advertisements_author         ON advertisements(author);
CREATE INDEX idx_advertisements_status         ON advertisements(status);
CREATE INDEX idx_advertisements_type           ON advertisements(type);
CREATE INDEX idx_advertisements_category       ON advertisements(category);
CREATE INDEX idx_advertisements_created_at     ON advertisements(created_at);
CREATE INDEX idx_advertisements_status_created ON advertisements(status, created_at DESC);
CREATE INDEX idx_advertisements_price          ON advertisements(price) WHERE price IS NOT NULL;
CREATE INDEX idx_advertisements_favorite       ON advertisements(is_favorite) WHERE is_favorite = TRUE;
CREATE INDEX idx_advertisements_is_favorite    ON advertisements(is_favorite) WHERE is_favorite = TRUE;
CREATE INDEX idx_advertisements_name           ON advertisements(name);

ALTER TABLE advertisements ADD COLUMN search_vector tsvector
  GENERATED ALWAYS AS (
    setweight(to_tsvector('russian', coalesce(name, '')), 'A') ||
    setweight(to_tsvector('russian', coalesce(description, '')), 'B') ||
    setweight(to_tsvector('russian', coalesce(category, '')), 'C')
  ) STORED;

CREATE INDEX idx_advertisements_search_vector ON advertisements USING GIN(search_vector);
CREATE INDEX idx_advertisements_name_trgm     ON advertisements USING gin(name gin_trgm_ops);

-- ============================================================
-- Seed data — объявления студентов МФТИ
-- Авторы: timofei(1), vladimir(2), daniil(3), gleb(4), anna(5), max(6)
-- ============================================================
INSERT INTO advertisements (id, status, author, type, category, name, price, description, is_auction, auction_ends_at) VALUES

  -- Электроника: ноутбуки
  ('e1111111-1111-1111-1111-111111111111'::uuid, 'ACTIVE',
   '11111111-1111-1111-1111-111111111111'::uuid,
   'OBJECTS', 'Товары/Электроника/Ноутбуки',
   'MacBook Pro 14" M2 (2023)',
   95000,
   'Продаю MacBook Pro 14" на чипе Apple M2, 16 ГБ оперативной памяти, SSD 512 ГБ. Куплен в марте 2023, в идеальном состоянии — ни царапины. В комплекте оригинальный блок питания 67W USB-C и коробка. Причина продажи — перехожу на стационарник. Торг уместен.',
   FALSE, NULL),

  -- Электроника: аудио
  ('e2222222-2222-2222-2222-222222222222'::uuid, 'ACTIVE',
   '22222222-2222-2222-2222-222222222222'::uuid,
   'OBJECTS', 'Товары/Электроника/Аудиотехника',
   'Наушники Sony WH-1000XM5',
   16500,
   'Беспроводные наушники Sony WH-1000XM5 с лучшим в классе шумоподавлением. Брал для учёбы в библиотеке — теперь работаю в основном из дома. Состояние 9/10, небольшие следы использования на оголовье. В комплекте кейс, кабель USB-C и кабель 3.5мм.',
   FALSE, NULL),

  -- Электроника: мониторы
  ('e3333333-3333-3333-3333-333333333333'::uuid, 'ACTIVE',
   '33333333-3333-3333-3333-333333333333'::uuid,
   'OBJECTS', 'Товары/Электроника/Компьютеры',
   'Монитор LG 27" IPS 4K (27UK650)',
   22000,
   'Монитор LG 27UK650, IPS-панель, разрешение 4K UHD (3840×2160), 60 Гц. Подключение: USB-C (60W PD), HDMI ×2, DisplayPort. Яркость 350 нит, охват sRGB 99%. Идеален для программирования и дизайна. Без битых пикселей, подсветка равномерная.',
   FALSE, NULL),

  -- Электроника: клавиатуры
  ('e4444444-4444-4444-4444-444444444444'::uuid, 'ACTIVE',
   '44444444-4444-4444-4444-444444444444'::uuid,
   'OBJECTS', 'Товары/Электроника/Клавиатуры',
   'Механическая клавиатура Keychron K2 Pro',
   7800,
   'Keychron K2 Pro — беспроводная 75% клавиатура с горячей заменой свитчей. Установлены Gateron Red (линейные, тихие). Раскладка ANSI, подсветка RGB. Работает по Bluetooth (до 3 устройств) и по кабелю USB-C. Использовал 6 месяцев, состояние отличное.',
   FALSE, NULL),

  -- Электроника: планшеты (АУКЦИОН)
  ('e5555555-5555-5555-5555-555555555555'::uuid, 'ACTIVE',
   '55555555-5555-5555-5555-555555555555'::uuid,
   'OBJECTS', 'Товары/Электроника/Планшеты',
   'iPad Air 5 Wi-Fi 256 ГБ — Аукцион',
   28000,
   'Выставляю на аукцион iPad Air 5 (2022), чип M1, 256 ГБ, цвет «синий». Состояние идеальное, использовался только дома. В комплекте: кабель USB-C, защитное стекло PaperFeel и чехол-книжка. Стартовая ставка — 28 000 токенов.',
   TRUE, NOW() + INTERVAL '5 days'),

  -- Транспорт: самокаты
  ('e6666666-6666-6666-6666-666666666666'::uuid, 'ACTIVE',
   '66666666-6666-6666-6666-666666666666'::uuid,
   'OBJECTS', 'Товары/Транспорт/Скутеры',
   'Электросамокат Xiaomi Scooter 4 Pro',
   21000,
   'Xiaomi Electric Scooter 4 Pro, запас хода до 55 км, максимальная скорость 25 км/ч. Складная конструкция, вес 14.6 кг. Использовал один сезон (весна–лето), пробег ~800 км. Всё работает исправно, тормоза проверены. Идеально для поездок между корпусами.',
   FALSE, NULL),

  -- Транспорт: велосипеды
  ('e7777777-7777-7777-7777-777777777777'::uuid, 'ACTIVE',
   '11111111-1111-1111-1111-111111111111'::uuid,
   'OBJECTS', 'Товары/Транспорт/Велосипеды',
   'Городской велосипед Stels Navigator 310',
   8500,
   'Городской велосипед Stels Navigator 310 Gent, колёса 28", 7 скоростей Shimano, стальная рама. Куплен два года назад, регулярно обслуживался. Состояние хорошее: цепь и тросики заменены весной. Есть багажник, крылья, звонок. Удобен для поездок по кампусу.',
   FALSE, NULL),

  -- Книги: учебники
  ('e8888888-8888-8888-8888-888888888888'::uuid, 'ACTIVE',
   '22222222-2222-2222-2222-222222222222'::uuid,
   'OBJECTS', 'Товары/Книги/Учебники',
   'Задачник Демидовича + Краткий справочник',
   400,
   'Продаю сборник задач по математическому анализу Демидовича (5-е издание, 2014) в хорошем состоянии — пометки только карандашом. В комплекте «Краткий справочник по высшей математике» Кудрявцева. Незаменимые вещи для 1–2 курса. Сдал все зачёты — отдам с радостью.',
   FALSE, NULL),

  -- Книги: научные
  ('e9999999-9999-9999-9999-999999999999'::uuid, 'ACTIVE',
   '33333333-3333-3333-3333-333333333333'::uuid,
   'OBJECTS', 'Товары/Книги/Научные',
   'Комплект книг по ML: Николенко, Гудфеллоу',
   3200,
   'Продаю два учебника по машинному обучению: «Глубокое обучение» Николенко, Кадурина, Архангельской (рус.) и «Deep Learning» Гудфеллоу (англ., бумажное издание). Оба в отличном состоянии, без пометок. Брал для дипломной работы, теперь есть в PDF.',
   FALSE, NULL),

  -- Мебель: кресла
  ('ea111111-1111-1111-1111-111111111111'::uuid, 'ACTIVE',
   '44444444-4444-4444-4444-444444444444'::uuid,
   'OBJECTS', 'Товары/Мебель/Стулья',
   'Компьютерное кресло Hara Chair Coper',
   11000,
   'Эргономичное компьютерное кресло Hara Chair Coper. Поясничная поддержка с регулировкой, регулировка высоты и наклона спинки, подлокотники 4D. Состояние 8/10 — небольшие следы от джинсов на сиденье, механика в порядке. Самовывоз с Долгопрудного.',
   FALSE, NULL),

  -- Бытовая техника: кофемашина
  ('ea222222-2222-2222-2222-222222222222'::uuid, 'ACTIVE',
   '55555555-5555-5555-5555-555555555555'::uuid,
   'OBJECTS', 'Товары/Бытовая техника/Кухонная',
   'Рожковая кофемашина DeLonghi EC235.BK',
   5500,
   'Кофемашина DeLonghi EC235 в отличном состоянии. Давление 15 бар, встроенный капучинатор, поддон для чашек с подогревом. Использовала полтора года, регулярно чистила. В комплекте мерная ложечка и тампер. Готовит эспрессо и капучино ничуть не хуже кафе.',
   FALSE, NULL),

  -- Хобби: игровые приставки
  ('ea333333-3333-3333-3333-333333333333'::uuid, 'ACTIVE',
   '66666666-6666-6666-6666-666666666666'::uuid,
   'OBJECTS', 'Товары/Хобби/Настольные игры',
   'Nintendo Switch OLED + 4 игры',
   27500,
   'Nintendo Switch OLED (белый) + 4 картриджа: The Legend of Zelda: Tears of the Kingdom, Mario Kart 8 Deluxe, Hollow Knight, Stardew Valley. Консоль в идеальном состоянии, куплена год назад. Продаю из-за нехватки времени. В комплекте оригинальная докстанция и два Joy-Con.',
   FALSE, NULL),

  -- Услуги: репетитор математика
  ('ea444444-4444-4444-4444-444444444444'::uuid, 'ACTIVE',
   '11111111-1111-1111-1111-111111111111'::uuid,
   'SERVICES', 'Услуги/Образование/Математика',
   'Репетитор по высшей математике (1–3 курс)',
   1500,
   'Студент 5 курса ФИВТ, специализация — математическое моделирование. Помогаю с матанализом, линейной алгеброй, дифференциальными уравнениями и ТФКП. Работаю с первокурсниками и студентами старших курсов. Занятия онлайн или в библиотеке кампуса. Цена за академический час.',
   FALSE, NULL),

  -- Услуги: IT/программирование
  ('ea555555-5555-5555-5555-555555555555'::uuid, 'ACTIVE',
   '22222222-2222-2222-2222-222222222222'::uuid,
   'SERVICES', 'Услуги/IT/Программирование',
   'Telegram-бот под заказ (Python / aiogram)',
   6000,
   'Разрабатываю Telegram-ботов на Python + aiogram 3. Интеграция с базами данных (PostgreSQL, SQLite), платёжные системы, уведомления, FSM. Примеры работ — по запросу. Срок: 3–7 дней в зависимости от сложности. Первая консультация бесплатно. Цена — за простого бота с базовым функционалом.',
   FALSE, NULL),

  -- Услуги: физика
  ('ea666666-6666-6666-6666-666666666666'::uuid, 'ACTIVE',
   '33333333-3333-3333-3333-333333333333'::uuid,
   'SERVICES', 'Услуги/Образование/Физика',
   'Физика: механика, термодинамика, электродинамика',
   1800,
   'Студент 4 курса ФОПФ. Помогу разобраться с общим курсом физики: механика, термодинамика и молекулярная физика, электродинамика, оптика. Готовлю к коллоквиумам и экзаменам. Умею объяснять сложное простым языком. Занятия онлайн или лично в Долгопрудном.',
   FALSE, NULL),

  -- Услуги: английский
  ('ea777777-7777-7777-7777-777777777777'::uuid, 'ACTIVE',
   '55555555-5555-5555-5555-555555555555'::uuid,
   'SERVICES', 'Услуги/Образование/Языки',
   'Технический английский и подготовка к IELTS',
   2000,
   'B2/C1 по IELTS (8.0). Помогаю с техническим английским для айтишников: чтение документации, написание README, подготовка к техническому интервью на английском. Также подготовка к IELTS/TOEFL с нуля. Занятия по 60 минут, онлайн. Первое занятие — пробное, бесплатно.',
   FALSE, NULL);
