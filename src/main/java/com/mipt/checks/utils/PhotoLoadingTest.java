package com.mipt.checks.utils;

import com.mipt.mainpage.model.ShortAdvert;
import com.mipt.search.model.SearchQuery;
import com.mipt.search.repository.SearchRepository;
import java.util.List;

/**
 * Тест для проверки загрузки фотографий при поисковом запросе
 */
public class PhotoLoadingTest {

  public static void main(String[] args) {
    System.out.println("=== Тест загрузки фотографий при поисковом запросе ===\n");

    try {
      // Создаем пустой поисковый запрос для получения всех объявлений
      SearchQuery query = new SearchQuery();

      // Получаем первые 10 объявлений
      List<ShortAdvert> adverts = SearchRepository.getAdverts(10, 0, query);

      System.out.println("Найдено объявлений: " + adverts.size());
      System.out.println();

      // Проверяем каждое объявление
      for (int i = 0; i < adverts.size(); i++) {
        ShortAdvert advert = adverts.get(i);
        System.out.println((i + 1) + ". Объявление: " + advert.getTitle());
        System.out.println("   ID: " + advert.getAdvertId());
        System.out.println("   Цена: " + advert.getPrice() + " руб.");

        if (advert.getPhotos() != null && !advert.getPhotos().isEmpty()) {
          System.out.println("   ✓ Загружено фотографий: " + advert.getPhotos().size());
          for (int j = 0; j < advert.getPhotos().size(); j++) {
            System.out.println("     " + (j + 1) + ") " + advert.getPhotos().get(j));
          }
        } else {
          System.out.println("   ✗ Фотографии отсутствуют");
        }
        System.out.println();
      }

      // Подсчет статистики
      long advertsWithPhotos = adverts.stream()
          .filter(a -> a.getPhotos() != null && !a.getPhotos().isEmpty())
          .count();

      long totalPhotos = adverts.stream()
          .filter(a -> a.getPhotos() != null)
          .mapToLong(a -> a.getPhotos().size())
          .sum();

      System.out.println("=== Статистика ===");
      System.out.println("Объявлений с фотографиями: " + advertsWithPhotos + " из " + adverts.size());
      System.out.println("Всего загружено фотографий: " + totalPhotos);

      if (advertsWithPhotos > 0) {
        System.out.println("\n✓ ТЕСТ ПРОЙДЕН: Фотографии успешно загружаются из таблицы advertisement_photos");
      } else {
        System.out.println("\n✗ ТЕСТ НЕ ПРОЙДЕН: Фотографии не загружаются (возможно, в базе нет фотографий)");
      }

    } catch (Exception e) {
      System.err.println("✗ ОШИБКА ПРИ ВЫПОЛНЕНИИ ТЕСТА:");
      e.printStackTrace();
    }
  }
}
