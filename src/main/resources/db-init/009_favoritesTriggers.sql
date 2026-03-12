BEGIN;

-- Шаг 1: Убедиться, что колонка is_favorite существует
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'advertisements' AND column_name = 'is_favorite'
    ) THEN
        ALTER TABLE advertisements ADD COLUMN is_favorite BOOLEAN NOT NULL DEFAULT FALSE;
        RAISE NOTICE 'Добавлена колонка is_favorite в таблицу advertisements';
    END IF;
END $$;

-- Шаг 2: Создать индекс для is_favorite, если не существует
CREATE INDEX IF NOT EXISTS idx_advertisements_is_favorite
    ON advertisements(is_favorite) WHERE is_favorite = TRUE;

-- Шаг 3: Убедиться, что таблица favorites существует с правильной структурой
CREATE TABLE IF NOT EXISTS favorites (
    user_id          UUID NOT NULL
        REFERENCES users (id)
        ON DELETE CASCADE,
    advertisement_id UUID NOT NULL
        REFERENCES advertisements (id)
        ON DELETE CASCADE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, advertisement_id)
);

CREATE INDEX IF NOT EXISTS idx_favorites_user_id
    ON favorites (user_id);

CREATE INDEX IF NOT EXISTS idx_favorites_advertisement_id
    ON favorites (advertisement_id);

UPDATE advertisements a
SET is_favorite = TRUE
WHERE EXISTS (
    SELECT 1 FROM favorites f
    WHERE f.advertisement_id = a.id
);

CREATE OR REPLACE FUNCTION update_favorite_flag_on_insert()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE advertisements
    SET is_favorite = TRUE
    WHERE id = NEW.advertisement_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_favorites_insert ON favorites;
CREATE TRIGGER trg_favorites_insert
AFTER INSERT ON favorites
FOR EACH ROW
EXECUTE FUNCTION update_favorite_flag_on_insert();

CREATE OR REPLACE FUNCTION update_favorite_flag_on_delete()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM favorites WHERE advertisement_id = OLD.advertisement_id) THEN
        UPDATE advertisements
        SET is_favorite = FALSE
        WHERE id = OLD.advertisement_id;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Шаг 8: Создать триггер для DELETE, если не существует
DROP TRIGGER IF EXISTS trg_favorites_delete ON favorites;
CREATE TRIGGER trg_favorites_delete
AFTER DELETE ON favorites
FOR EACH ROW
EXECUTE FUNCTION update_favorite_flag_on_delete();

COMMIT;

DO $$
DECLARE
    fav_count INT;
    flag_count INT;
BEGIN
    SELECT COUNT(*) INTO fav_count FROM favorites;
    SELECT COUNT(*) INTO flag_count FROM advertisements WHERE is_favorite = TRUE;

    RAISE NOTICE '=================================================';
    RAISE NOTICE 'Миграция завершена успешно!';
    RAISE NOTICE 'Записей в таблице favorites: %', fav_count;
    RAISE NOTICE 'Объявлений с флагом is_favorite: %', flag_count;
    RAISE NOTICE '=================================================';
END $$;

