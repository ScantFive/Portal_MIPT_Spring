package com.mipt.advertisement.event;

import com.mipt.advertisement.model.Advertisement;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

  public static AdvertisementEvent created(Advertisement ad) {
    return base(ad, "ADVERTISEMENT_CREATED", "Advertisement created in DRAFT status");
  }

  public static AdvertisementEvent published(Advertisement ad) {
    return base(ad, "ADVERTISEMENT_PUBLISHED", "Advertisement published and active");
  }

  public static AdvertisementEvent updated(Advertisement ad) {
    return base(ad, "ADVERTISEMENT_UPDATED", "Advertisement updated");
  }

  public static AdvertisementEvent paused(Advertisement ad) {
    return base(ad, "ADVERTISEMENT_PAUSED", "Advertisement paused");
  }

  public static AdvertisementEvent deleted(Advertisement ad) {
    return base(ad, "ADVERTISEMENT_DELETED", "Advertisement deleted");
  }

  public static AdvertisementEvent priceChanged(
      UUID id,
      String name,
      UUID authorId,
      Long oldPrice,
      Long newPrice) {
    return AdvertisementEvent.builder()
        .eventType("ADVERTISEMENT_PRICE_CHANGED")
        .advertisementId(id)
        .authorId(authorId)
        .advertisementName(name)
        .price(newPrice)
        .timestamp(Instant.now())
        .details(String.format("Price changed from %s to %s", oldPrice, newPrice))
        .build();
  }

  public static AdvertisementEvent photoAdded(UUID id, String name, UUID authorId, String photoUrl) {
    return AdvertisementEvent.builder()
        .eventType("PHOTO_ADDED")
        .advertisementId(id)
        .authorId(authorId)
        .advertisementName(name)
        .timestamp(Instant.now())
        .details("Photo added: " + photoUrl)
        .build();
  }

  public static AdvertisementEvent photoRemoved(UUID id, String name, UUID authorId, String photoUrl) {
    return AdvertisementEvent.builder()
        .eventType("PHOTO_REMOVED")
        .advertisementId(id)
        .authorId(authorId)
        .advertisementName(name)
        .timestamp(Instant.now())
        .details("Photo removed: " + photoUrl)
        .build();
  }

  public static AdvertisementEvent favoriteToggled(UUID id, String name, UUID authorId, boolean isFavorite) {
    return AdvertisementEvent.builder()
        .eventType("FAVORITE_TOGGLED")
        .advertisementId(id)
        .authorId(authorId)
        .advertisementName(name)
        .timestamp(Instant.now())
        .details(isFavorite ? "Advertisement added to favorites" : "Advertisement removed from favorites")
        .build();
  }

  private static AdvertisementEvent base(Advertisement ad, String type, String details) {
    return AdvertisementEvent.builder()
        .eventType(type)
        .advertisementId(ad.getId())
        .authorId(ad.getAuthorId())
        .advertisementName(ad.getName())
        .category(ad.getCategory() != null ? ad.getCategory().getDisplayName() : null)
        .price(ad.getPrice())
        .status(ad.getStatus() != null ? ad.getStatus().name() : null)
        .timestamp(Instant.now())
        .details(details)
        .build();
  }
}
