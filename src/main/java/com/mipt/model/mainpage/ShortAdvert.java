package com.mipt.model.mainpage;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Data;


import java.net.URL;
import java.util.List;
import java.util.UUID;

/** Краткая информация об объявлении для отображения в списке. */
@Data
@Entity
@Builder
public class ShortAdvert {
  private UUID advertId;
  private UUID authorId;
  private String title;
  private String descriptionPreview;
  private long price;
  private List<URL> photos;
  private boolean isFavorite;
}
