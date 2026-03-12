package com.mipt.advertisement.repository;

import com.mipt.advertisement.model.Advertisement;
import com.mipt.advertisement.model.AdvertisementStatus;
import com.mipt.advertisement.model.Category;
import com.mipt.advertisement.model.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AdvertisementRepository implements AdvertisementRep {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Advertisement> advertisementRowMapper = (rs, rowNum) -> {
        Advertisement advertisement = new Advertisement(
            UUID.fromString(rs.getString("id")),
            Type.valueOf(rs.getString("type")),
            UUID.fromString(rs.getString("author")),
            rs.getString("name"),
            rs.getString("description"),
            rs.getTimestamp("created_at").toInstant()
        );

        advertisement.setStatus(AdvertisementStatus.valueOf(rs.getString("status")));

        long price = rs.getLong("price");
        if (!rs.wasNull()) {
            advertisement.setPrice(price);
        }

        String categoryStr = rs.getString("category");
        if (categoryStr != null && !categoryStr.trim().isEmpty()) {
            advertisement.setCategory(Category.fromNameSafe(categoryStr));
        }

        advertisement.setFavorite(rs.getBoolean("is_favorite"));

        return advertisement;
    };

    @Override
    @Transactional
    public Advertisement save(Advertisement advertisement) {
        log.debug("Saving advertisement: {}", advertisement.getId());

        if (advertisement.getId() == null) {
            advertisement.setId(UUID.randomUUID());
        }

        String sql = """
            INSERT INTO advertisements (id, status, author, type, category, name, price, description, is_favorite, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
            advertisement.getId(),
            advertisement.getStatus().name(),
            advertisement.getAuthorId(),
            advertisement.getType().name(),
            advertisement.getCategory() != null ?
                advertisement.getCategory().getDisplayName() : null, // Сохраняем displayName, не name()
            advertisement.getName(),
            advertisement.getPrice(),
            advertisement.getDescription(),
            advertisement.isFavorite(),
            Timestamp.from(advertisement.getCreatedAt())
        );
        savePhotos(advertisement);

        return findById(advertisement.getId())
            .orElseThrow(() -> new RuntimeException("Failed to save advertisement"));
    }

    @Override
    public Optional<Advertisement> findById(UUID id) {
        String sql = "SELECT * FROM advertisements WHERE id = ?";

        try {
            Advertisement advertisement = jdbcTemplate.queryForObject(sql, advertisementRowMapper, id);
            if (advertisement != null) {
                loadPhotos(advertisement);
            }
            return Optional.ofNullable(advertisement);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Advertisement> findAll() {
        String sql = "SELECT * FROM advertisements ORDER BY created_at DESC";
        List<Advertisement> advertisements = jdbcTemplate.query(sql, advertisementRowMapper);
        advertisements.forEach(this::loadPhotos);
        return advertisements;
    }

    @Override
    public List<Advertisement> findByAuthorId(UUID authorId) {
        String sql = "SELECT * FROM advertisements WHERE author = ? ORDER BY created_at DESC";
        List<Advertisement> advertisements = jdbcTemplate.query(sql, advertisementRowMapper, authorId);
        advertisements.forEach(this::loadPhotos);
        return advertisements;
    }

    @Override
    public List<Advertisement> findByStatus(AdvertisementStatus status) {
        String sql = "SELECT * FROM advertisements WHERE status = ? ORDER BY created_at DESC";
        List<Advertisement> advertisements = jdbcTemplate.query(sql, advertisementRowMapper, status.name());
        advertisements.forEach(this::loadPhotos);
        return advertisements;
    }

    @Override
    public List<Advertisement> findByCategory(Category category) {
        String sql = "SELECT * FROM advertisements WHERE category = ? ORDER BY created_at DESC";
        List<Advertisement> advertisements = jdbcTemplate.query(sql, advertisementRowMapper, category.name());
        advertisements.forEach(this::loadPhotos);
        return advertisements;
    }

    @Override
    public List<Advertisement> findByType(Type type) {
        String sql = "SELECT * FROM advertisements WHERE type = ? ORDER BY created_at DESC";
        List<Advertisement> advertisements = jdbcTemplate.query(sql, advertisementRowMapper, type.name());
        advertisements.forEach(this::loadPhotos);
        return advertisements;
    }

    @Override
    public List<Advertisement> findFavorites() {
        String sql = "SELECT * FROM advertisements WHERE is_favorite = true ORDER BY created_at DESC";
        List<Advertisement> advertisements = jdbcTemplate.query(sql, advertisementRowMapper);
        advertisements.forEach(this::loadPhotos);
        return advertisements;
    }

    @Override
    @Transactional
    public Advertisement update(Advertisement advertisement) {
        log.debug("Updating advertisement: {}", advertisement.getId());

        String sql = """
            UPDATE advertisements 
            SET status = ?, type = ?, category = ?, name = ?, price = ?, 
                description = ?, is_favorite = ?
            WHERE id = ?
            """;

        int updated = jdbcTemplate.update(sql,
            advertisement.getStatus().name(),
            advertisement.getType().name(),
            advertisement.getCategory() != null ?
                advertisement.getCategory().getDisplayName() : null, // Здесь тоже displayName
            advertisement.getName(),
            advertisement.getPrice(),
            advertisement.getDescription(),
            advertisement.isFavorite(),
            advertisement.getId()
        );

        if (updated == 0) {
            throw new IllegalArgumentException("Advertisement not found: " + advertisement.getId());
        }

        // Обновляем фото (удаляем старые и добавляем новые)
        deletePhotos(advertisement.getId());
        savePhotos(advertisement);

        return findById(advertisement.getId())
            .orElseThrow(() -> new RuntimeException("Failed to update advertisement"));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        log.debug("Deleting advertisement: {}", id);
        deletePhotos(id);
        jdbcTemplate.update("DELETE FROM advertisements WHERE id = ?", id);
    }

    @Override
    @Transactional
    public Advertisement addPhoto(UUID advertisementId, String photoUrl) {
        log.debug("Adding photo to advertisement: {}", advertisementId);

        Integer maxOrder = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(display_order), -1) FROM advertisement_photos WHERE advertisement_id = ?",
            Integer.class, advertisementId);

        int nextOrder = (maxOrder != null ? maxOrder : -1) + 1;

        jdbcTemplate.update(
            "INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES (?, ?, ?)",
            advertisementId, photoUrl, nextOrder
        );

        return findById(advertisementId)
            .orElseThrow(() -> new RuntimeException("Advertisement not found"));
    }

    @Override
    @Transactional
    public Advertisement removePhoto(UUID advertisementId, String photoUrl) {
        log.debug("Removing photo from advertisement: {}", advertisementId);

        int deleted = jdbcTemplate.update(
            "DELETE FROM advertisement_photos WHERE advertisement_id = ? AND photo_url = ?",
            advertisementId, photoUrl
        );

        if (deleted == 0) {
            throw new IllegalArgumentException("Photo not found: " + photoUrl);
        }

        // Переупорядочиваем оставшиеся фото
        reorderPhotos(advertisementId);

        return findById(advertisementId)
            .orElseThrow(() -> new RuntimeException("Advertisement not found"));
    }

    @Override
    @Transactional
    public Advertisement updatePrice(UUID advertisementId, Long price) {
        log.debug("Updating price for advertisement: {}", advertisementId);

        jdbcTemplate.update(
            "UPDATE advertisements SET price = ? WHERE id = ?",
            price, advertisementId
        );

        return findById(advertisementId)
            .orElseThrow(() -> new RuntimeException("Advertisement not found"));
    }

    @Override
    @Transactional
    public Advertisement setFavorite(UUID advertisementId, boolean favorite) {
        log.debug("Setting favorite={} for advertisement: {}", favorite, advertisementId);

        jdbcTemplate.update(
            "UPDATE advertisements SET is_favorite = ? WHERE id = ?",
            favorite, advertisementId
        );

        return findById(advertisementId)
            .orElseThrow(() -> new RuntimeException("Advertisement not found"));
    }

    @Override
    @Transactional
    public Advertisement toggleFavorite(UUID advertisementId) {
        log.debug("Toggling favorite for advertisement: {}", advertisementId);

        jdbcTemplate.update(
            "UPDATE advertisements SET is_favorite = NOT is_favorite WHERE id = ?",
            advertisementId
        );

        return findById(advertisementId)
            .orElseThrow(() -> new RuntimeException("Advertisement not found"));
    }

    @Override
    public List<Category> getAllCategories() {
        return Arrays.asList(Category.values());
    }

    @Override
    public List<String> getAllCategoryGroups(Type type) {
        return Category.getGroupsForType(type);
    }

    @Override
    public boolean existsById(UUID id) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM advertisements WHERE id = ?",
            Integer.class, id
        );
        return count != null && count > 0;
    }

    // Вспомогательные методы

    private void savePhotos(Advertisement advertisement) {
        String sql = "INSERT INTO advertisement_photos (advertisement_id, photo_url, display_order) VALUES (?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();
        int order = 0;
        for (String photoUrl : advertisement.getPhotoUrls()) {
            batchArgs.add(new Object[]{advertisement.getId(), photoUrl, order++});
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void loadPhotos(Advertisement advertisement) {
        String sql = "SELECT photo_url FROM advertisement_photos WHERE advertisement_id = ? ORDER BY display_order";

        List<String> photos = jdbcTemplate.queryForList(sql, String.class, advertisement.getId());
        advertisement.getPhotoUrls().clear();
        advertisement.getPhotoUrls().addAll(photos);
    }

    private void deletePhotos(UUID advertisementId) {
        jdbcTemplate.update("DELETE FROM advertisement_photos WHERE advertisement_id = ?", advertisementId);
    }

    private void reorderPhotos(UUID advertisementId) {
        String sql = """
            WITH ordered AS (
                SELECT id, ROW_NUMBER() OVER (ORDER BY display_order) - 1 as new_order
                FROM advertisement_photos 
                WHERE advertisement_id = ?
            )
            UPDATE advertisement_photos ap
            SET display_order = o.new_order
            FROM ordered o
            WHERE ap.id = o.id
            """;

        jdbcTemplate.update(sql, advertisementId);
    }
}