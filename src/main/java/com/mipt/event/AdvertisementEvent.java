package com.mipt.event;

import com.mipt.model.advertisement.Advertisement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementEvent {

    private String eventType;
    private UUID advertisementId;
    private UUID authorId;
    private String advertisementName;
    private String category;
    private Long price;
    private String status;
    private Instant timestamp;
    private String details;

    // Событие: объявление создано
    public static AdvertisementEvent created(Advertisement ad) {
        return AdvertisementEvent.builder()
                .eventType("ADVERTISEMENT_CREATED")
                .advertisementId(ad.getId())
                .authorId(ad.getAuthorId())
                .advertisementName(ad.getName())
                .category(ad.getCategory() != null ? ad.getCategory().getDisplayName() : null)
                .price(ad.getPrice())
                .status(ad.getStatus().name())
                .timestamp(Instant.now())
                .details("Объявление создано в статусе DRAFT")
                .build();
    }

    // Событие: объявление опубликовано
    public static AdvertisementEvent published(Advertisement ad) {
        return AdvertisementEvent.builder()
                .eventType("ADVERTISEMENT_PUBLISHED")
                .advertisementId(ad.getId())
                .authorId(ad.getAuthorId())
                .advertisementName(ad.getName())
                .category(ad.getCategory() != null ? ad.getCategory().getDisplayName() : null)
                .price(ad.getPrice())
                .status(ad.getStatus().name())
                .timestamp(Instant.now())
                .details("Объявление опубликовано и активно")
                .build();
    }

    // Событие: объявление обновлено
    public static AdvertisementEvent updated(Advertisement ad) {
        return AdvertisementEvent.builder()
                .eventType("ADVERTISEMENT_UPDATED")
                .advertisementId(ad.getId())
                .authorId(ad.getAuthorId())
                .advertisementName(ad.getName())
                .category(ad.getCategory() != null ? ad.getCategory().getDisplayName() : null)
                .price(ad.getPrice())
                .status(ad.getStatus().name())
                .timestamp(Instant.now())
                .details("Объявление обновлено")
                .build();
    }

    // Событие: объявление приостановлено
    public static AdvertisementEvent paused(Advertisement ad) {
        return AdvertisementEvent.builder()
                .eventType("ADVERTISEMENT_PAUSED")
                .advertisementId(ad.getId())
                .authorId(ad.getAuthorId())
                .advertisementName(ad.getName())
                .category(ad.getCategory() != null ? ad.getCategory().getDisplayName() : null)
                .price(ad.getPrice())
                .status(ad.getStatus().name())
                .timestamp(Instant.now())
                .details("Объявление приостановлено")
                .build();
    }

    // Событие: объявление удалено
    public static AdvertisementEvent deleted(Advertisement ad) {
        return AdvertisementEvent.builder()
                .eventType("ADVERTISEMENT_DELETED")
                .advertisementId(ad.getId())
                .authorId(ad.getAuthorId())
                .advertisementName(ad.getName())
                .category(ad.getCategory() != null ? ad.getCategory().getDisplayName() : null)
                .price(ad.getPrice())
                .status(ad.getStatus().name())
                .timestamp(Instant.now())
                .details("Объявление удалено")
                .build();
    }

    // Событие: цена изменена
    public static AdvertisementEvent priceChanged(UUID id, String name, UUID authorId, Long oldPrice, Long newPrice) {
        return AdvertisementEvent.builder()
                .eventType("ADVERTISEMENT_PRICE_CHANGED")
                .advertisementId(id)
                .authorId(authorId)
                .advertisementName(name)
                .price(newPrice)
                .timestamp(Instant.now())
                .details(String.format("Цена изменена с %d на %d", oldPrice, newPrice))
                .build();
    }

    // Событие: фото добавлено
    public static AdvertisementEvent photoAdded(UUID id, String name, UUID authorId, String photoUrl) {
        return AdvertisementEvent.builder()
                .eventType("PHOTO_ADDED")
                .advertisementId(id)
                .authorId(authorId)
                .advertisementName(name)
                .timestamp(Instant.now())
                .details(String.format("Добавлено фото: %s", photoUrl))
                .build();
    }

    // Событие: фото удалено
    public static AdvertisementEvent photoRemoved(UUID id, String name, UUID authorId, String photoUrl) {
        return AdvertisementEvent.builder()
                .eventType("PHOTO_REMOVED")
                .advertisementId(id)
                .authorId(authorId)
                .advertisementName(name)
                .timestamp(Instant.now())
                .details(String.format("Удалено фото: %s", photoUrl))
                .build();
    }

    // Событие: добавлено/удалено из избранного
    public static AdvertisementEvent favoriteToggled(UUID id, String name, UUID authorId, boolean isFavorite) {
        return AdvertisementEvent.builder()
                .eventType("FAVORITE_TOGGLED")
                .advertisementId(id)
                .authorId(authorId)
                .advertisementName(name)
                .timestamp(Instant.now())
                .details(isFavorite ? "Объявление добавлено в избранное" : "Объявление удалено из избранного")
                .build();
    }
}