package com.mipt.mainpage.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteId implements Serializable {
  private UUID userId;
  private UUID advertisementId;
}
