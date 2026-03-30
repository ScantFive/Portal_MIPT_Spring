package com.mipt.mainpage.model;

import java.net.URL;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Краткая информация об объявлении для отображения в списке. */
@Getter
@Setter
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
