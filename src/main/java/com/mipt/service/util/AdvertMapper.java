package com.mipt.service.util;

import com.mipt.model.mainpage.ShortAdvert;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Утилитный класс для преобразования данных из базы данных в модели приложения. */
public final class AdvertMapper {

  private static final int DEFAULT_TRUNCATE_SUFFIX_LENGTH = 3;
  private static final String TRUNCATE_SUFFIX = "...";

  private AdvertMapper() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Обрезает текст до указанной длины, добавляя многоточие.
   *
   * @param text текст для обрезки
   * @param maxLength максимальная длина результата
   * @return обрезанный текст или null, если входной текст null
   */
  public static String truncate(String text, int maxLength) {
    if (text == null) {
      return null;
    }

    if (text.length() <= maxLength) {
      return text;
    }

    int endIndex = maxLength - DEFAULT_TRUNCATE_SUFFIX_LENGTH;
    if (endIndex < 0) {
      endIndex = 0;
    }

    return text.substring(0, endIndex) + TRUNCATE_SUFFIX;
  }

  /**
   * Преобразует массив строк в список URL. Невалидные URL пропускаются с логированием ошибки.
   *
   * @param urlStrings массив строк с URL
   * @return список валидных URL
   */
  public static List<URL> getURLsFromStrings(String[] urlStrings) {
    List<URL> urls = new ArrayList<>();
    if (urlStrings == null) {
      return urls;
    }

    for (String urlString : urlStrings) {
      if (urlString == null || urlString.isBlank()) {
        continue;
      }

      try {
        urls.add(new URI(urlString).toURL());
      } catch (URISyntaxException | MalformedURLException e) {
        System.err.println("Невалидный URL: " + urlString + " - " + e.getMessage());
        // В production лучше использовать logger
      }
    }

    return urls;
  }

  /**
   * Преобразует строку результата SQL-запроса в объект ShortAdvert.
   *
   * @param resultSet результат SQL-запроса
   * @param descriptionPreviewSize максимальная длина предпросмотра описания
   * @return объект ShortAdvert
   * @throws SQLException если произошла ошибка при чтении данных
   */
  public static ShortAdvert rowAdverToShortAdvert(ResultSet resultSet, int descriptionPreviewSize)
      throws SQLException {
    UUID advertId = (UUID) resultSet.getObject("id");
    UUID authorId = (UUID) resultSet.getObject("author");
    String title = resultSet.getString("name");
    String description = resultSet.getString("description");
    String descriptionPreview = truncate(description, descriptionPreviewSize);
    long price = resultSet.getLong("price");

    // Получаем массив URL фотографий из БД, если такая колонка есть
    List<URL> photos = new ArrayList<>();
    try {
      java.sql.Array photosArray = resultSet.getArray("photos");
      if (photosArray != null) {
        Object arrayData = photosArray.getArray();
        if (arrayData instanceof String[] strings) {
          photos = getURLsFromStrings(strings);
        } else if (arrayData instanceof Object[] objArray) {
          // Преобразуем Object[] в String[]
          String[] stringArray = new String[objArray.length];
          for (int i = 0; i < objArray.length; i++) {
            stringArray[i] = objArray[i] != null ? objArray[i].toString() : null;
          }
          photos = getURLsFromStrings(stringArray);
        }
      }
    } catch (SQLException e) {
      // Колонка photos может отсутствовать в некоторых запросах
    }

    // Флаг isFavorite устанавливается отдельно в репозитории
    boolean isFavorite = false;

    return ShortAdvert.builder()
        .advertId(advertId)
        .authorId(authorId)
        .title(title)
        .descriptionPreview(descriptionPreview)
        .price(price)
        .photos(photos)
        .isFavorite(isFavorite)
        .build();
  }
}
