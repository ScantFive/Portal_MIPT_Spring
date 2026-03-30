package com.mipt.checks.utils;

import com.mipt.advertisement.model.*;
import com.mipt.advertisement.repository.AdvertisementRepository;
import com.mipt.advertisement.repository.AdvertisementRep;

import java.time.Instant;
import java.util.*;

public class AdvertisementCheck {

  private static final AdvertisementRep repository = new AdvertisementRepository();
  private static final Scanner scanner = new Scanner(System.in);
  private static final Random random = new Random();

  public static void main(String[] args) {
    System.out.println("🎯 Advertisement Database Console (with BIGINT price)");
    System.out.println("====================================================");

    boolean running = true;
    while (running) {
      printMenu();
      System.out.print("Выберите действие: ");

      try {
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
          case 1 -> createAdvertisement();
          case 2 -> publishAdvertisement();
          case 3 -> findAdvertisementById();
          case 4 -> findByAuthorId();
          case 5 -> findByStatus();
          case 6 -> updatePrice();
          case 7 -> addPhoto();
          case 8 -> removePhoto();
          case 9 -> updateAdvertisement();
          case 10 -> pauseAdvertisement();
          case 11 -> toggleFavorite();
          case 12 -> showFavorites();
          case 13 -> showAllCategories();
          case 14 -> findByCategory();
          case 15 -> changeCategory();
          case 16 -> showAllAdvertisements();
          case 17 -> clearDatabase();
          case 18 -> testAllOperations();
          case 19 -> generateTestData();
          case 20 -> showStatistics();
          case 0 -> {
            running = false;
            System.out.println("👋 Выход...");
          }
          default -> System.out.println("❌ Неверный выбор");
        }
      } catch (Exception e) {
        System.out.println("❌ Ошибка: " + e.getMessage());
        scanner.nextLine(); // clear buffer
      }

      System.out.println();
    }

    scanner.close();
  }

  private static void printMenu() {
    System.out.println("\n📋 Меню:");
    System.out.println("1. Создать объявление");
    System.out.println("2. Опубликовать объявление");
    System.out.println("3. Найти объявление по ID");
    System.out.println("4. Найти объявления автора");
    System.out.println("5. Найти объявления по статусу");
    System.out.println("6. Обновить цену");
    System.out.println("7. Добавить фото");
    System.out.println("8. Удалить фото");
    System.out.println("9. Обновить объявление");
    System.out.println("10. Приостановить объявление");
    System.out.println("11. Поставить/Убрать лайк");
    System.out.println("12. Показать избранные");
    System.out.println("13. Показать все категории");
    System.out.println("14. Найти по категории");
    System.out.println("15. Изменить категорию");
    System.out.println("16. Показать все объявления");
    System.out.println("17. Очистить базу данных");
    System.out.println("18. Тест всех операций");
    System.out.println("19. Сгенерировать тестовые данные");
    System.out.println("20. Показать статистику");
    System.out.println("0. Выход");
  }

  private static Type selectType() {
    while (true) {
      System.out.print("Тип (OBJECTS/SERVICES): ");
      String input = scanner.nextLine().toUpperCase();

      try {
        return Type.valueOf(input);
      } catch (IllegalArgumentException e) {
        System.out.println("❌ Неверный тип. Допустимые значения: OBJECTS, SERVICES");
      }
    }
  }



  private static void createAdvertisement() {
    System.out.println("\n➕ Создание объявления:");

    Type type = selectType();

    System.out.print("Автор ID (или нажмите Enter для случайного): ");
    String authorInput = scanner.nextLine();
    UUID authorId = authorInput.isEmpty() ? UUID.randomUUID() : UUID.fromString(authorInput);

    System.out.print("Название (3-255 символов): ");
    String name = scanner.nextLine();

    System.out.print("Описание (до 5000 символов, Enter чтобы пропустить): ");
    String description = scanner.nextLine();
    if (description.isEmpty()) {
      description = null;
    }

    Advertisement advertisement = new Advertisement(
        UUID.randomUUID(),
        type,
        authorId,
        name,
        description,
        Instant.now()
    );

    // Двухуровневый выбор категории
    System.out.println("\n🎯 Выбор категории (2 этапа):");
    Category category = selectCategoryTwoStep(type);
    advertisement.setCategory(category);

    System.out.print("Добавить цену сейчас? (y/n): ");
    if (scanner.nextLine().equalsIgnoreCase("y")) {
      setPriceInteractive(advertisement);
    }

    System.out.print("Добавить фото? (y/n): ");
    if (scanner.nextLine().equalsIgnoreCase("y")) {
      addPhotosInteractive(advertisement);
    }

    try {
      repository.create(advertisement);
      System.out.println("✅ Объявление создано: " + advertisement.getId());
      printAdvertisement(advertisement);
    } catch (Exception e) {
      System.out.println("❌ Ошибка при создании: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static Category selectCategoryTwoStep(Type type) {
    System.out.println("\n=== ЭТАП 1: Выберите группу категорий ===");

    // Получаем группы категорий для выбранного типа
    List<String> groups = Category.getGroupsForType(type);

    if (groups.isEmpty()) {
      throw new IllegalStateException("Нет доступных групп категорий для типа: " + type);
    }

    // Показываем группы
    for (int i = 0; i < groups.size(); i++) {
      System.out.printf("%2d. %s\n", i + 1, groups.get(i));
    }

    // Выбор группы
    int groupChoice;
    while (true) {
      System.out.print("\nВыберите группу (введите номер): ");
      String input = scanner.nextLine();

      try {
        groupChoice = Integer.parseInt(input);
        if (groupChoice >= 1 && groupChoice <= groups.size()) {
          break;
        } else {
          System.out.println("❌ Некорректный номер. Выберите от 1 до " + groups.size());
        }
      } catch (NumberFormatException e) {
        System.out.println("❌ Введите число");
      }
    }

    String selectedGroup = groups.get(groupChoice - 1);
    System.out.println("✅ Выбрана группа: " + selectedGroup);

    System.out.println("\n=== ЭТАП 2: Выберите конкретную категорию ===");

    // Получаем категории в выбранной группе
    List<Category> categoriesInGroup = Category.getCategoriesInGroup(selectedGroup);

    if (categoriesInGroup.isEmpty()) {
      throw new IllegalStateException("Нет категорий в группе: " + selectedGroup);
    }

    // Показываем категории
    for (int i = 0; i < categoriesInGroup.size(); i++) {
      Category cat = categoriesInGroup.get(i);
      System.out.printf("%2d. %s\n", i + 1, cat.getDisplayName());
    }

    // Выбор конкретной категории
    int categoryChoice;
    while (true) {
      System.out.print("\nВыберите категорию (введите номер): ");
      String input = scanner.nextLine();

      try {
        categoryChoice = Integer.parseInt(input);
        if (categoryChoice >= 1 && categoryChoice <= categoriesInGroup.size()) {
          break;
        } else {
          System.out.println("❌ Некорректный номер. Выберите от 1 до " + categoriesInGroup.size());
        }
      } catch (NumberFormatException e) {
        System.out.println("❌ Введите число");
      }
    }

    Category selectedCategory = categoriesInGroup.get(categoryChoice - 1);
    System.out.println("✅ Выбрана категория: " + selectedCategory.getDisplayName());

    return selectedCategory;
  }

  private static void setPriceInteractive(Advertisement advertisement) {
    System.out.print("Цена (целое число, например 9999): ");
    Long priceValue = scanner.nextLong();
    scanner.nextLine(); // consume newline
    advertisement.setPrice(priceValue);
    System.out.println("✅ Цена установлена: " + priceValue);
  }

  private static void addPhotosInteractive(Advertisement advertisement) {
    boolean adding = true;
    int count = 1;
    while (adding) {
      System.out.print("URL фото " + count + " (до 500 символов): ");
      String photoUrl = scanner.nextLine();
      advertisement.getPhotoUrls().add(photoUrl);

      System.out.print("Добавить еще фото? (y/n): ");
      adding = scanner.nextLine().equalsIgnoreCase("y");
      count++;
    }
  }

  private static void publishAdvertisement() {
    System.out.print("ID объявления для публикации: ");
    UUID id = UUID.fromString(scanner.nextLine());

    var ad = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

    try {
      repository.publish(ad);
      System.out.println("✅ Объявление опубликовано");
    } catch (Exception e) {
      System.out.println("❌ Ошибка при публикации: " + e.getMessage());
    }
  }

  private static void findAdvertisementById() {
    System.out.print("ID объявления: ");
    UUID id = UUID.fromString(scanner.nextLine());

    var ad = repository.findById(id);
    if (ad.isPresent()) {
      printAdvertisement(ad.get());
    } else {
      System.out.println("❌ Объявление не найдено");
    }
  }

  private static void findByAuthorId() {
    System.out.print("ID автора: ");
    UUID authorId = UUID.fromString(scanner.nextLine());

    var ads = repository.findByAuthorId(authorId);
    System.out.println("📊 Найдено объявлений: " + ads.size());
    ads.forEach(AdvertisementCheck::printAdvertisement);
  }

  private static void findByStatus() {
    System.out.print("Статус (ACTIVE/DRAFT/PAUSED/DELETED): ");
    AdvertisementStatus status = AdvertisementStatus.valueOf(scanner.nextLine().toUpperCase());

    var ads = repository.findByStatus(status);
    System.out.println("📊 Найдено объявлений: " + ads.size());
    ads.forEach(AdvertisementCheck::printAdvertisement);
  }

  private static void updatePrice() {
    System.out.print("ID объявления: ");
    UUID adId = UUID.fromString(scanner.nextLine());

    System.out.print("Новая цена (целое число): ");
    Long priceValue = scanner.nextLong();
    scanner.nextLine(); // consume newline

    try {
      repository.updatePrice(adId, priceValue);
      System.out.println("✅ Цена обновлена");
    } catch (Exception e) {
      System.out.println("❌ Ошибка: " + e.getMessage());
    }
  }

  private static void addPhoto() {
    System.out.print("ID объявления: ");
    UUID adId = UUID.fromString(scanner.nextLine());

    System.out.print("URL фото: ");
    String photoUrl = scanner.nextLine();

    try {
      repository.addPhotoUrl(adId, photoUrl);
      System.out.println("✅ Фото добавлено");
    } catch (Exception e) {
      System.out.println("❌ Ошибка: " + e.getMessage());
    }
  }

  private static void changeCategory() {
    System.out.print("ID объявления: ");
    UUID adId = UUID.fromString(scanner.nextLine());

    try {
      // Получаем текущее объявление
      var ad = repository.findById(adId)
          .orElseThrow(() -> new IllegalArgumentException("Объявление не найдено"));

      System.out.println("Текущая категория: " +
          (ad.getCategory() != null ? ad.getCategory().getDisplayName() : "не указана"));

      System.out.println("Выберите новую категорию:");
      Category newCategory = selectCategoryTwoStep(ad.getType());

      repository.setCategory(adId, newCategory);
      System.out.println("✅ Категория изменена на: " + newCategory.getDisplayName());
    } catch (Exception e) {
      System.out.println("❌ Ошибка: " + e.getMessage());
    }
  }

  private static void removePhoto() {
    System.out.print("ID объявления: ");
    UUID adId = UUID.fromString(scanner.nextLine());

    System.out.print("URL фото для удаления: ");
    String photoUrl = scanner.nextLine();

    try {
      repository.removePhotoUrl(adId, photoUrl);
      System.out.println("✅ Фото удалено");
    } catch (Exception e) {
      System.out.println("❌ Ошибка: " + e.getMessage());
    }
  }

  private static void updateAdvertisement() {
    System.out.print("ID объявления для обновления: ");
    UUID id = UUID.fromString(scanner.nextLine());

    var ad = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

    System.out.println("Текущий тип: " + ad.getType());
    System.out.print("Изменить тип? (y/n): ");
    if (scanner.nextLine().equalsIgnoreCase("y")) {
      Type newType = selectType();
      ad.setType(newType);

      // При изменении типа нужно изменить и категорию
      System.out.println("При изменении типа необходимо выбрать новую категорию:");
      Category newCategory = selectCategoryTwoStep(newType);
      ad.setCategory(newCategory);
    }

    System.out.println("Текущая категория: " +
        (ad.getCategory() != null ? ad.getCategory().getDisplayName() : "не указана"));
    System.out.print("Изменить категорию? (y/n): ");
    if (scanner.nextLine().equalsIgnoreCase("y")) {
      Category newCategory = selectCategoryTwoStep(ad.getType());
      ad.setCategory(newCategory);
    }

    System.out.print("Новое название (оставьте пустым чтобы не менять): ");
    String name = scanner.nextLine();
    if (!name.isEmpty()) {
      ad.setName(name);
    }

    System.out.print("Новое описание (оставьте пустым чтобы не менять): ");
    String desc = scanner.nextLine();
    if (!desc.isEmpty()) {
      ad.setDescription(desc);
    }

    try {
      repository.update(ad);
      System.out.println("✅ Объявление обновлено");
    } catch (Exception e) {
      System.out.println("❌ Ошибка при обновлении: " + e.getMessage());
    }
  }

  private static void pauseAdvertisement() {
    System.out.print("ID объявления для приостановки: ");
    UUID id = UUID.fromString(scanner.nextLine());

    var ad = repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Объявление не найдено"));

    try {
      repository.pause(ad);
      System.out.println("✅ Объявление приостановлено");
    } catch (Exception e) {
      System.out.println("❌ Ошибка при приостановке: " + e.getMessage());
    }
  }

  private static void toggleFavorite() {
    System.out.print("ID объявления для добавления/удаления из избранного: ");
    UUID id = UUID.fromString(scanner.nextLine());

    try {
      repository.toggleFavorite(id);
      System.out.println("✅ Статус избранного изменен");
    } catch (Exception e) {
      System.out.println("❌ Ошибка: " + e.getMessage());
    }
  }

  private static void showFavorites() {
    System.out.println("\n❤️ Избранные объявления:");
    var favorites = repository.findFavorites();
    System.out.println("Найдено избранных: " + favorites.size());
    favorites.forEach(AdvertisementCheck::printAdvertisement);
  }

  private static void showAllCategories() {
    System.out.println("\n📊 Все категории в базе:");
    Set<Category> categories = repository.getAllCategories();

    if (categories.isEmpty()) {
      System.out.println("Категорий пока нет");
    } else {
      categories.forEach(cat ->
          System.out.println("  • " + cat.name() + " - " + cat.getDisplayName()));
      System.out.println("Всего категорий: " + categories.size());
    }
  }

  private static Category selectCategorySimple(Type type) {
    System.out.println("\nВыберите категорию:");

    List<Category> categories = Category.getCategoriesForType(type);

    for (int i = 0; i < categories.size(); i++) {
      Category cat = categories.get(i);
      System.out.printf("%3d. %s\n", i + 1, cat.getDisplayName());
      if ((i + 1) % 3 == 0) {
        System.out.println();
      }
    }

    while (true) {
      System.out.print("\nВыберите категорию (введите номер): ");
      String input = scanner.nextLine();

      try {
        int index = Integer.parseInt(input) - 1;
        if (index >= 0 && index < categories.size()) {
          return categories.get(index);
        }
      } catch (NumberFormatException e) {
        System.out.println("❌ Введите число");
      }
    }
  }

  // Использовать в методе findByCategory():
  private static void findByCategory() {
    System.out.println("Выберите тип объявления для поиска:");
    Type type = selectType();

    System.out.println("Выберите категорию для поиска:");
    Category category = selectCategorySimple(type);

    var ads = repository.findByCategory(category);
    System.out.println(
        "📊 Найдено объявлений по категории \"" + category.getDisplayName() + "\": " + ads.size());
    ads.forEach(AdvertisementCheck::printAdvertisement);
  }

  private static void showAllAdvertisements() {
    System.out.println("\n📋 Все объявления:");
    var allActive = repository.findByStatus(AdvertisementStatus.ACTIVE);
    var allDraft = repository.findByStatus(AdvertisementStatus.DRAFT);
    var allPaused = repository.findByStatus(AdvertisementStatus.PAUSED);
    var allDeleted = repository.findByStatus(AdvertisementStatus.DELETED);

    System.out.println("Активные (" + allActive.size() + "):");
    allActive.forEach(ad -> System.out.println("  " + ad.getId() + " - " + ad.getName() +
        " [" + ad.getType() + ", цена: " + (ad.getPrice() != null ? ad.getPrice() : "нет") + "]"));

    System.out.println("\nЧерновики (" + allDraft.size() + "):");
    allDraft.forEach(ad -> System.out.println("  " + ad.getId() + " - " + ad.getName() +
        " [" + ad.getType() + ", цена: " + (ad.getPrice() != null ? ad.getPrice() : "нет") + "]"));

    System.out.println("\nПриостановленные (" + allPaused.size() + "):");
    allPaused.forEach(ad -> System.out.println("  " + ad.getId() + " - " + ad.getName() +
        " [" + ad.getType() + ", цена: " + (ad.getPrice() != null ? ad.getPrice() : "нет") + "]"));

    System.out.println("\nУдаленные (" + allDeleted.size() + "):");
    allDeleted.forEach(ad -> System.out.println("  " + ad.getId() + " - " + ad.getName() +
        " [" + ad.getType() + ", цена: " + (ad.getPrice() != null ? ad.getPrice() : "нет") + "]"));
  }

  private static void clearDatabase() {
    System.out.print("⚠ ВНИМАНИЕ: Это удалит ВСЕ данные! Продолжить? (y/n): ");
    if (scanner.nextLine().equalsIgnoreCase("y")) {
      try {
        repository.clear();
        System.out.println("✅ База данных очищена");
      } catch (Exception e) {
        System.out.println("❌ Ошибка при очистке: " + e.getMessage());
      }
    } else {
      System.out.println("❌ Отменено");
    }
  }

  private static void testAllOperations() {
    System.out.println("\n🧪 Запуск теста всех операций...");

    try {
      // 1. Очистка
      System.out.print("1. Очистка базы... ");
      repository.clear();
      System.out.println("OK");

      // 2. Создание тестового пользователя
      UUID testAuthorId = UUID.randomUUID();
      System.out.println("2. Тестовый автор ID: " + testAuthorId);

      // 3. Создание объявления OBJECTS с категорией "Товары/Книги/Учебники"
      System.out.print("3. Создание объявления OBJECTS с категорией... ");
      Advertisement ad1 = new Advertisement(
          UUID.randomUUID(),
          Type.OBJECTS,
          testAuthorId,
          "Продажа учебников по программированию",
          "Сборник лучших учебников по Java, Python и SQL для начинающих",
          Instant.now()
      );
      // Используем новую категорию: ТОВАРЫ_КНИГИ_УЧЕБНИКИ
      ad1.setCategory(Category.ТОВАРЫ_КНИГИ_УЧЕБНИКИ);
      ad1.setPrice(2500L);
      ad1.getPhotoUrls().addAll(Arrays.asList(
          "https://example.com/books1.jpg",
          "https://example.com/books2.jpg"
      ));
      repository.create(ad1);
      System.out.println("OK, ID: " + ad1.getId());

      // 4. Публикация первого объявления
      System.out.print("4. Публикация первого объявления... ");
      repository.publish(ad1);
      System.out.println("OK");

      // 5. Создание объявления SERVICES с категорией "Услуги/Образование/Программирование"
      System.out.print("5. Создание объявления SERVICES с категорией... ");
      Advertisement ad2 = new Advertisement(
          UUID.randomUUID(),
          Type.SERVICES,
          testAuthorId,
          "Репетитор по программированию на Java",
          "Индивидуальные занятия по Java для студентов и начинающих разработчиков",
          Instant.now()
      );
      // Используем новую категорию: УСЛУГИ_ОБРАЗОВАНИЕ_ПРОГРАММИРОВАНИЕ
      ad2.setCategory(Category.УСЛУГИ_ОБРАЗОВАНИЕ_ПРОГРАММИРОВАНИЕ);
      ad2.setPrice(1500L);
      ad2.getPhotoUrls().add("https://example.com/tutor_java.jpg");
      repository.create(ad2);
      repository.publish(ad2);
      System.out.println("OK, ID: " + ad2.getId());

      // 6. Создание черновика с категорией "Товары/Инструменты/Отвертки"
      System.out.print("6. Создание черновика с категорией... ");
      Advertisement ad3 = new Advertisement(
          UUID.randomUUID(),
          Type.OBJECTS,
          testAuthorId,
          "Набор профессиональных отверток",
          "Комплект из 24 отверток для различных типов работ",
          Instant.now()
      );
      // Используем новую категорию: ТОВАРЫ_ИНСТРУМЕНТЫ_ОТВЕРТКИ
      ad3.setCategory(Category.ТОВАРЫ_ИНСТРУМЕНТЫ_ОТВЕРТКИ);
      ad3.setPrice(1200L);
      repository.create(ad3);
      System.out.println("OK, ID: " + ad3.getId());

      // 7. Создание объявления с категорией "Услуги/IT/Веб-разработка"
      System.out.print("7. Создание объявления IT-услуг... ");
      Advertisement ad4 = new Advertisement(
          UUID.randomUUID(),
          Type.SERVICES,
          testAuthorId,
          "Разработка корпоративных сайтов",
          "Создание современных веб-приложений на React и Spring Boot",
          Instant.now()
      );
      // Используем новую категорию: УСЛУГИ_IT_ВЕБ_РАЗРАБОТКА
      ad4.setCategory(Category.УСЛУГИ_IT_ВЕБ_РАЗРАБОТКА);
      ad4.setPrice(50000L);
      ad4.getPhotoUrls().add("https://example.com/web_dev.jpg");
      repository.create(ad4);
      repository.publish(ad4);
      System.out.println("OK, ID: " + ad4.getId());

      // 8. Создание объявления с категорией "Товары/Электроника/Ноутбуки"
      System.out.print("8. Создание объявления с электроникой... ");
      Advertisement ad5 = new Advertisement(
          UUID.randomUUID(),
          Type.OBJECTS,
          testAuthorId,
          "Игровой ноутбук ASUS ROG",
          "Мощный игровой ноутбук с RTX 4060, 32GB RAM, 1TB SSD",
          Instant.now()
      );
      // Используем новую категорию: ТОВАРЫ_ЭЛЕКТРОНИКА_НОУТБУКИ
      ad5.setCategory(Category.ТОВАРЫ_ЭЛЕКТРОНИКА_НОУТБУКИ);
      ad5.setPrice(85000L);
      ad5.getPhotoUrls().addAll(Arrays.asList(
          "https://example.com/laptop1.jpg",
          "https://example.com/laptop2.jpg",
          "https://example.com/laptop3.jpg"
      ));
      repository.create(ad5);
      repository.publish(ad5);
      System.out.println("OK, ID: " + ad5.getId());

      // 9. Создание объявления с категорией "Услуги/Консультации/Программирование"
      System.out.print("9. Создание объявления консультаций... ");
      Advertisement ad6 = new Advertisement(
          UUID.randomUUID(),
          Type.SERVICES,
          testAuthorId,
          "Консультации по архитектуре приложений",
          "Code review, помощь с проектированием систем, менторинг",
          Instant.now()
      );
      // Используем новую категорию: УСЛУГИ_КОНСУЛЬТАЦИИ_ПРОГРАММИРОВАНИЕ
      ad6.setCategory(Category.УСЛУГИ_КОНСУЛЬТАЦИИ_ПРОГРАММИРОВАНИЕ);
      ad6.setPrice(3000L);
      repository.create(ad6);
      System.out.println("OK, ID: " + ad6.getId());

      // 10. Поиск по ID
      System.out.print("10. Поиск по ID... ");
      var foundAd = repository.findById(ad1.getId());
      if (foundAd.isPresent()) {
        System.out.println("OK, найдено: " + foundAd.get().getName());
      } else {
        System.out.println("FAIL");
      }

      // 11. Обновление цены
      System.out.print("11. Обновление цены... ");
      repository.updatePrice(ad1.getId(), 2800L);
      System.out.println("OK");

      // 12. Добавление фото
      System.out.print("12. Добавление фото... ");
      repository.addPhotoUrl(ad1.getId(), "https://example.com/books3.jpg");
      System.out.println("OK");

      // 13. Изменение категории (меняем на "Товары/Электроника/Клавиатуры")
      System.out.print("13. Изменение категории... ");
      repository.setCategory(ad1.getId(), Category.ТОВАРЫ_ЭЛЕКТРОНИКА_КЛАВИАТУРЫ);
      // Обновляем название для соответствия новой категории
      ad1.setName("Продажа механической клавиатуры");
      ad1.setDescription("Игровая механическая клавиатура с RGB подсветкой");
      repository.update(ad1);
      System.out.println("OK (изменено с книг на клавиатуру)");

      // 14. Поиск по автору
      System.out.print("14. Поиск по автору... ");
      var authorAds = repository.findByAuthorId(testAuthorId);
      System.out.println("Найдено: " + authorAds.size());

      // 15. Поиск по статусу
      System.out.print("15. Поиск активных объявлений... ");
      var activeAds = repository.findByStatus(AdvertisementStatus.ACTIVE);
      System.out.println("Найдено: " + activeAds.size());

      // 16. Тестирование приостановки
      System.out.print("16. Тестирование приостановки объявления... ");
      repository.pause(ad1);
      System.out.println("OK");

      // 17. Тестирование избранного
      System.out.print("17. Тестирование избранного... ");
      repository.toggleFavorite(ad1.getId());
      repository.toggleFavorite(ad2.getId());
      System.out.println("OK (добавлено 2 в избранное)");

      // 18. Тестирование удаления фото
      System.out.print("18. Тестирование удаления фото... ");
      var updatedAd = repository.findById(ad1.getId());
      if (updatedAd.isPresent() && updatedAd.get().getPhotoUrls()
          .contains("https://example.com/books3.jpg")) {
        repository.removePhotoUrl(ad1.getId(), "https://example.com/books3.jpg");
        System.out.println("OK");
      } else {
        System.out.println("⚠ Пропущено (фото не найдено)");
      }

      // 19. Публикация приостановленного объявления
      System.out.print("19. Публикация приостановленного объявления... ");
      repository.publish(ad1);
      System.out.println("OK");

      // 20. Тестирование поиска по категории "Товары/Электроника/Клавиатуры"
      System.out.print("20. Поиск по категории электроники... ");
      var keyboardAds = repository.findByCategory(Category.ТОВАРЫ_ЭЛЕКТРОНИКА_КЛАВИАТУРЫ);
      System.out.println("Найдено: " + keyboardAds.size());

      // 21. Тестирование поиска по категории "Услуги/Образование/Программирование"
      System.out.print("21. Поиск по категории программирования... ");
      var programmingAds = repository.findByCategory(Category.УСЛУГИ_ОБРАЗОВАНИЕ_ПРОГРАММИРОВАНИЕ);
      System.out.println("Найдено: " + programmingAds.size());

      // 22. Тестирование получения всех категорий
      System.out.print("22. Получение всех категорий... ");
      var allCategories = repository.getAllCategories();
      System.out.println("Найдено: " + allCategories.size() + " категорий");

      // 23. Тестирование получения категории объявления
      System.out.print("23. Получение категории объявления... ");
      Category category = repository.getCategory(ad1.getId());
      System.out.println("Категория: " + (category != null ? category.getDisplayName() : "нет"));

      // 24. Тестирование смены типа с автоматической сменой категории
      System.out.print("24. Тестирование смены типа объявления... ");
      ad3.setType(Type.SERVICES); // Меняем тип с OBJECTS на SERVICES
      // При смене типа нужно изменить категорию
      ad3.setCategory(Category.УСЛУГИ_ОБРАЗОВАНИЕ_ПРОГРАММИРОВАНИЕ);
      ad3.setName("Услуга: Репетитор по программированию");
      ad3.setDescription("Индивидуальные занятия по программированию для начинающих");
      repository.update(ad3);
      System.out.println("OK (тип и категория изменены)");

      // 25. Тестирование двухуровневого выбора категорий
      System.out.print("25. Тестирование двухуровневого выбора... ");
      List<String> objectGroups = Category.getGroupsForType(Type.OBJECTS);
      List<String> serviceGroups = Category.getGroupsForType(Type.SERVICES);
      System.out.println("OK (групп товаров: " + objectGroups.size() +
          ", групп услуг: " + serviceGroups.size() + ")");

      // 26. Тестирование получения категорий в группе
      System.out.print("26. Тестирование получения категорий в группе... ");
      List<Category> electronicsCategories = Category.getCategoriesInGroup("Товары/Электроника");
      List<Category> educationCategories = Category.getCategoriesInGroup("Услуги/Образование");
      System.out.println("OK (электроника: " + electronicsCategories.size() +
          ", образование: " + educationCategories.size() + ")");

      // 27. Тестирование поиска избранных
      System.out.print("27. Поиск избранных объявлений... ");
      var favorites = repository.findFavorites();
      System.out.println("Найдено: " + favorites.size());

      // 28. Тестирование валидации при публикации
      System.out.print("28. Тестирование валидации... ");
      try {
        ad3.validateToPublish(); // Черновик должен пройти валидацию
        System.out.println("OK (валидация пройдена)");
      } catch (Exception e) {
        System.out.println("⚠ Валидация не пройдена: " + e.getMessage());
      }

      System.out.println("\n✅ Все тесты пройдены успешно!");

      // Подробная статистика
      System.out.println("\n📊 Итоговая статистика теста:");
      System.out.println("Создано объявлений: " + authorAds.size());
      System.out.println("Активных: " + activeAds.size());
      System.out.println(
          "Черновиков: " + repository.findByStatus(AdvertisementStatus.DRAFT).size());
      System.out.println("Избранных: " + favorites.size());
      System.out.println("Уникальных категорий: " + allCategories.size());

      System.out.println("\n🏷️ Использованные категории:");
      Set<String> usedCategories = new TreeSet<>();
      for (Advertisement ad : authorAds) {
        if (ad.getCategory() != null) {
          usedCategories.add(ad.getCategory().getDisplayName());
        }
      }
      for (String cat : usedCategories) {
        System.out.println("  • " + cat);
      }

      // Показываем информацию о тестовых объявлениях
      System.out.println("\n📋 Созданные тестовые объявления:");
      System.out.println("1. " + ad1.getName() + " [" + ad1.getType() + ", " +
          (ad1.getCategory() != null ? ad1.getCategory().getDisplayName() : "нет категории") + "]");
      System.out.println("2. " + ad2.getName() + " [" + ad2.getType() + ", " +
          (ad2.getCategory() != null ? ad2.getCategory().getDisplayName() : "нет категории") + "]");
      System.out.println("3. " + ad3.getName() + " [" + ad3.getType() + ", " +
          (ad3.getCategory() != null ? ad3.getCategory().getDisplayName() : "нет категории") + "]");
      System.out.println("4. " + ad4.getName() + " [" + ad4.getType() + ", " +
          (ad4.getCategory() != null ? ad4.getCategory().getDisplayName() : "нет категории") + "]");
      System.out.println("5. " + ad5.getName() + " [" + ad5.getType() + ", " +
          (ad5.getCategory() != null ? ad5.getCategory().getDisplayName() : "нет категории") + "]");
      System.out.println("6. " + ad6.getName() + " [" + ad6.getType() + ", " +
          (ad6.getCategory() != null ? ad6.getCategory().getDisplayName() : "нет категории") + "]");

    } catch (Exception e) {
      System.out.println("\n❌ Тест провален: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void generateTestData() {
    System.out.print("Сколько тестовых объявлений создать? (1-200): ");
    int count = scanner.nextInt();
    scanner.nextLine();

    if (count < 1 || count > 200) {
      System.out.println("❌ Некорректное количество. Допустимо от 1 до 200");
      return;
    }

    System.out.print("Удалить существующие данные? (y/n): ");
    boolean clearFirst = scanner.nextLine().equalsIgnoreCase("y");

    double objectsRatio = 0.5; // По умолчанию смешанное распределение

    try {
      if (clearFirst) {
        repository.clear();
        System.out.println("✅ База очищена");
      }

      List<UUID> authorIds = Arrays.asList(
          UUID.randomUUID(),
          UUID.randomUUID(),
          UUID.randomUUID(),
          UUID.randomUUID(),
          UUID.randomUUID()
      );

      // Категории для товаров с примерными названиями
      Map<Category, String[]> objectCategoriesMap = new HashMap<>();

      // Товары/Книги
      objectCategoriesMap.put(Category.ТОВАРЫ_КНИГИ_УЧЕБНИКИ, new String[]{
          "Учебники по программированию", "Учебники математики", "Учебники физики",
          "Сборники задач", "Пособия для студентов", "Учебники иностранных языков"
      });
      objectCategoriesMap.put(Category.ТОВАРЫ_КНИГИ_ХУДОЖЕСТВЕННЫЕ, new String[]{
          "Классическая литература", "Современные романы", "Детективы",
          "Фэнтези", "Научная фантастика", "Поэзия"
      });
      objectCategoriesMap.put(Category.ТОВАРЫ_КНИГИ_НАУЧНЫЕ, new String[]{
          "Монографии", "Научные статьи", "Исследовательские работы",
          "Диссертации", "Справочники", "Энциклопедии"
      });

      // Товары/Электроника
      objectCategoriesMap.put(Category.ТОВАРЫ_ЭЛЕКТРОНИКА_НОУТБУКИ, new String[]{
          "Игровой ноутбук", "Ультрабук для работы", "Ноутбук для программирования",
          "Макбук", "Ноутбук для учебы", "Игровой ноутбук с RTX"
      });
      objectCategoriesMap.put(Category.ТОВАРЫ_ЭЛЕКТРОНИКА_ТЕЛЕФОНЫ, new String[]{
          "Смартфон Apple", "Android телефон", "Игровой телефон",
          "Смартфон с камерой", "Телефон для бизнеса", "Бюджетный смартфон"
      });
      objectCategoriesMap.put(Category.ТОВАРЫ_ЭЛЕКТРОНИКА_КЛАВИАТУРЫ, new String[]{
          "Механическая клавиатура", "Мембранная клавиатура", "Игровая клавиатура",
          "Беспроводная клавиатура", "Клавиатура для программирования", "Мини-клавиатура"
      });
      objectCategoriesMap.put(Category.ТОВАРЫ_ЭЛЕКТРОНИКА_МЫШИ, new String[]{
          "Игровая мышь", "Беспроводная мышь", "Эргономичная мышь",
          "Мышь для дизайнеров", "Мышь с кнопками", "Компактная мышь"
      });
      objectCategoriesMap.put(Category.ТОВАРЫ_ЭЛЕКТРОНИКА_АКСЕССУАРЫ, new String[]{
          "Наушники", "Внешний жесткий диск", "Флеш-накопитель",
          "Веб-камера", "Колонки", "Игровой коврик"
      });

      // Товары/Инструменты
      objectCategoriesMap.put(Category.ТОВАРЫ_ИНСТРУМЕНТЫ_ОТВЕРТКИ, new String[]{
          "Набор отверток", "Профессиональные отвертки", "Магнитные отвертки",
          "Отвертки для электроники", "Универсальный набор", "Отвертки с насадками"
      });

      // Товары/Мебель
      objectCategoriesMap.put(Category.ТОВАРЫ_МЕБЕЛЬ_СТОЛЫ, new String[]{
          "Компьютерный стол", "Офисный стол", "Стол для работы",
          "Стол с регулировкой высоты", "Угловой стол", "Мини-стол"
      });
      objectCategoriesMap.put(Category.ТОВАРЫ_МЕБЕЛЬ_СТУЛЬЯ, new String[]{
          "Офисное кресло", "Игровое кресло", "Эргономичный стул",
          "Кресло для работы", "Стул для дома", "Складной стул"
      });

      // Товары/Бытовая техника
      objectCategoriesMap.put(Category.ТОВАРЫ_БЫТОВАЯ_ТЕХНИКА_КУХОННАЯ, new String[]{
          "Электрический чайник", "Микроволновая печь", "Блендер",
          "Кофеварка", "Тостер", "Мультиварка"
      });

      // Категории для услуг с примерными названиями
      Map<Category, String[]> serviceCategoriesMap = new HashMap<>();

      // Услуги/Образование
      serviceCategoriesMap.put(Category.УСЛУГИ_ОБРАЗОВАНИЕ_РЕПЕТИТОРСТВО, new String[]{
          "Репетитор по математике", "Репетитор по физике", "Репетитор по химии",
          "Репетитор по английскому", "Репетитор по русскому", "Репетитор по истории"
      });
      serviceCategoriesMap.put(Category.УСЛУГИ_ОБРАЗОВАНИЕ_ПРОГРАММИРОВАНИЕ, new String[]{
          "Репетитор по Java", "Репетитор по Python", "Репетитор по C++",
          "Обучение веб-разработке", "Обучение мобильной разработке", "Обучение алгоритмам"
      });
      serviceCategoriesMap.put(Category.УСЛУГИ_ОБРАЗОВАНИЕ_МАТЕМАТИКА, new String[]{
          "Подготовка к ЕГЭ по математике", "Высшая математика", "Математический анализ",
          "Линейная алгебра", "Теория вероятностей", "Дискретная математика"
      });
      serviceCategoriesMap.put(Category.УСЛУГИ_ОБРАЗОВАНИЕ_ФИЗИКА, new String[]{
          "Физика для студентов", "Подготовка к ЕГЭ по физике", "Теоретическая физика",
          "Квантовая физика", "Механика", "Электричество и магнетизм"
      });
      serviceCategoriesMap.put(Category.УСЛУГИ_ОБРАЗОВАНИЕ_ХИМИЯ, new String[]{
          "Органическая химия", "Неорганическая химия", "Аналитическая химия",
          "Физическая химия", "Биохимия", "Химия для школьников"
      });
      serviceCategoriesMap.put(Category.УСЛУГИ_ОБРАЗОВАНИЕ_ЯЗЫКИ, new String[]{
          "Английский для IT", "Разговорный английский", "Подготовка к IELTS",
          "Подготовка к TOEFL", "Деловой английский", "Английский с носителем"
      });

      // Услуги/IT
      serviceCategoriesMap.put(Category.УСЛУГИ_IT_ВЕБ_РАЗРАБОТКА, new String[]{
          "Разработка сайта-визитки", "Создание интернет-магазина", "Веб-приложение",
          "Корпоративный портал", "Landing page", "Веб-сервис"
      });
      serviceCategoriesMap.put(Category.УСЛУГИ_IT_МОБИЛЬНАЯ_РАЗРАБОТКА, new String[]{
          "Приложение для Android", "Приложение для iOS", "Кроссплатформенное приложение",
          "Игровое приложение", "Бизнес-приложение", "Образовательное приложение"
      });
      serviceCategoriesMap.put(Category.УСЛУГИ_IT_ПРОГРАММИРОВАНИЕ, new String[]{
          "Разработка ПО", "Создание скриптов", "Автоматизация процессов",
          "Разработка библиотек", "Создание API", "Разработка алгоритмов"
      });

      // Услуги/Консультации
      serviceCategoriesMap.put(Category.УСЛУГИ_КОНСУЛЬТАЦИИ_ПРОГРАММИРОВАНИЕ, new String[]{
          "Консультация по архитектуре", "Code review", "Техническое собеседование",
          "Консультация по выбору технологий", "Оптимизация кода", "Декомпозиция задач"
      });

      // Услуги/Репетиторство
      serviceCategoriesMap.put(Category.УСЛУГИ_ОБРАЗОВАНИЕ_КУРСЫ, new String[]{
          "Курс по программированию", "Интенсив по алгоритмам", "Практикум по ООП",
          "Курс по базам данных", "Обучение тестированию", "Курс по DevOps"
      });

      // Преобразуем мапы в списки для удобного доступа
      List<Category> objectCategories = new ArrayList<>(objectCategoriesMap.keySet());
      List<Category> serviceCategories = new ArrayList<>(serviceCategoriesMap.keySet());

      int created = 0;
      int objectsCount = 0;
      int servicesCount = 0;

      Map<String, Integer> categoryStats = new HashMap<>();
      Map<String, Integer> groupStats = new HashMap<>();

      System.out.println("\n🚀 Генерация тестовых данных...");

      for (int i = 0; i < count; i++) {
        try {
          // Выбираем тип в соответствии с распределением
          Type type;
          type = random.nextDouble() < objectsRatio ? Type.OBJECTS : Type.SERVICES;

          // Выбираем категорию и название
          Category selectedCategory;
          String name;
          String[] availableNames;

          if (type == Type.OBJECTS && !objectCategories.isEmpty()) {
            selectedCategory = objectCategories.get(random.nextInt(objectCategories.size()));
            availableNames = objectCategoriesMap.get(selectedCategory);
            name = availableNames[random.nextInt(availableNames.length)] + " #" + (i + 1);
          } else if (type == Type.SERVICES && !serviceCategories.isEmpty()) {
            selectedCategory = serviceCategories.get(random.nextInt(serviceCategories.size()));
            availableNames = serviceCategoriesMap.get(selectedCategory);
            name = availableNames[random.nextInt(availableNames.length)] + " #" + (i + 1);
          } else {
            // Если нет категорий для выбранного типа, пропускаем
            continue;
          }

          UUID authorId = authorIds.get(random.nextInt(authorIds.size()));

          // Создаем описание в зависимости от категории
          String description = createDescriptionForCategory(selectedCategory, name);

          Advertisement ad = new Advertisement(
              UUID.randomUUID(),
              type,
              authorId,
              name,
              description,
              Instant.now()
          );

          // Устанавливаем категорию
          ad.setCategory(selectedCategory);

          // Добавляем цену в зависимости от категории
          long price = generatePriceForCategory(selectedCategory);
          ad.setPrice(price);

          // Добавляем фото (0-4 фото)
          int photoCount = random.nextInt(5);
          for (int j = 0; j < photoCount; j++) {
            ad.getPhotoUrls().add("https://example.com/test/" +
                selectedCategory.name().toLowerCase().replace("_", "-") +
                "_" + (i + 1) + "_" + (j + 1) + ".jpg");
          }

          // Устанавливаем статус
          AdvertisementStatus status = generateStatusForAd(type, random);
          ad.setStatus(status);

          // Случайно добавляем в избранное (25% шанс)
          if (random.nextDouble() < 0.25) {
            ad.setFavorite(true);
          }

          repository.create(ad);
          created++;

          // Статистика
          if (type == Type.OBJECTS) {
            objectsCount++;
          } else {
            servicesCount++;
          }

          // Статистика по категориям
          String categoryName = selectedCategory.name();
          categoryStats.put(categoryName, categoryStats.getOrDefault(categoryName, 0) + 1);

          // Статистика по группам
          String group = selectedCategory.getGroup();
          groupStats.put(group, groupStats.getOrDefault(group, 0) + 1);

          // Прогресс
          if ((i + 1) % 20 == 0 || i == count - 1) {
            System.out.printf("Создано %d из %d объявлений...\n", i + 1, count);
          }

        } catch (Exception e) {
          System.out.println("⚠ Ошибка при создании объявления " + (i + 1) + ": " + e.getMessage());
        }
      }

      System.out.println("\n✅ Создано " + created + " тестовых объявлений из " + count);
      System.out.println("📊 Успешно: " + created + " / " + count + " (" +
          (count > 0 ? created * 100 / count : 0) + "%)");
      System.out.println("\n🎯 Примеры созданных объявлений:");
      var allAds = repository.findByStatus(AdvertisementStatus.ACTIVE);
      if (!allAds.isEmpty()) {
        for (int i = 0; i < Math.min(5, allAds.size()); i++) {
          Advertisement ad = allAds.get(i);
          System.out.printf("  %d. %-40s [%s, %s]\n",
              i + 1,
              ad.getName().length() > 40 ? ad.getName().substring(0, 37) + "..." : ad.getName(),
              ad.getType(),
              ad.getCategory() != null ?
                  ad.getCategory().getDisplayName().split("/")[2] : "нет категории"
          );
        }
      }

    } catch (Exception e) {
      System.out.println("❌ Ошибка при генерации данных: " + e.getMessage());
      e.printStackTrace();
    }
  }

// Вспомогательные методы для генерации данных

  private static String createDescriptionForCategory(Category category, String name) {
    String group = category.getGroup();
    String subcategory = category.getSubcategory();

    Map<String, String> descriptionTemplates = new HashMap<>();

    // Шаблоны для товаров
    descriptionTemplates.put("Товары/Книги",
        "Качественное издание '" + name + "'. Идеальное состояние, " +
            "без пометок. Отлично подходит для учебы и самообразования.");
    descriptionTemplates.put("Товары/Электроника", "Новое современное устройство: " + name + ". " +
        "Полная комплектация, гарантия производителя. Идеально для работы и учебы.");
    descriptionTemplates.put("Товары/Инструменты", "Профессиональный инструмент: " + name + ". " +
        "Высокое качество, надежность. Подходит для домашнего использования и профессиональных работ.");
    descriptionTemplates.put("Товары/Мебель", "Удобная и практичная мебель: " + name + ". " +
        "Современный дизайн, качественные материалы. Отличное состояние.");
    descriptionTemplates.put("Товары/Бытовая техника", "Бытовая техника: " + name + ". " +
        "Энергоэффективная, современная модель. Все функции работают исправно.");

    // Шаблоны для услуг
    descriptionTemplates.put("Услуги/Образование", "Профессиональные услуги: " + name + ". " +
        "Индивидуальный подход, современные методики обучения. Помощь в освоении материала.");
    descriptionTemplates.put("Услуги/IT", "IT-услуги: " + name + ". " +
        "Профессиональный подход, качественное выполнение работ. Современные технологии и методологии.");
    descriptionTemplates.put("Услуги/Консультации", "Консультационные услуги: " + name + ". " +
        "Опытный специалист, практические советы, помощь в решении сложных задач.");
    descriptionTemplates.put("Услуги/Репетиторство", "Услуги репетитора: " + name + ". " +
        "Индивидуальные занятия, подготовка к экзаменам, помощь с домашними заданиями.");

    String template = descriptionTemplates.getOrDefault(group,
        "Тестовое объявление для '" + name + "'. Категория: " + category.getDisplayName() + ". " +
            "Это автоматически сгенерированное объявление для проверки функциональности системы.");

    return template + " Создано: " + Instant.now();
  }

  private static long generatePriceForCategory(Category category) {
    String group = category.getGroup();

    // Цены в зависимости от группы категорий
    Map<String, int[]> priceRanges = new HashMap<>();
    priceRanges.put("Товары/Книги", new int[]{100, 5000});
    priceRanges.put("Товары/Электроника", new int[]{1000, 150000});
    priceRanges.put("Товары/Инструменты", new int[]{500, 20000});
    priceRanges.put("Товары/Мебель", new int[]{1000, 50000});
    priceRanges.put("Товары/Бытовая техника", new int[]{500, 30000});
    priceRanges.put("Услуги/Образование", new int[]{500, 10000});
    priceRanges.put("Услуги/IT", new int[]{5000, 100000});
    priceRanges.put("Услуги/Консультации", new int[]{1000, 50000});
    priceRanges.put("Услуги/Репетиторство", new int[]{800, 20000});

    int[] range = priceRanges.getOrDefault(group, new int[]{100, 10000});
    return (long) (random.nextInt(range[1] - range[0]) + range[0]);
  }

  private static AdvertisementStatus generateStatusForAd(Type type, Random random) {
    double rand = random.nextDouble();
      if (rand < 0.6) {
        return AdvertisementStatus.ACTIVE;
      } else if (rand < 0.85) {
        return AdvertisementStatus.DRAFT;
      } else {
        return AdvertisementStatus.PAUSED;
      }
  }

  private static void showStatistics() {
    System.out.println("\n📊 Статистика базы данных:");

    var active = repository.findByStatus(AdvertisementStatus.ACTIVE).size();
    var draft = repository.findByStatus(AdvertisementStatus.DRAFT).size();
    var paused = repository.findByStatus(AdvertisementStatus.PAUSED).size();
    var deleted = repository.findByStatus(AdvertisementStatus.DELETED).size();
    var favorites = repository.findFavorites().size();
    var allCategories = repository.getAllCategories().size();

    int total = active + draft + paused + deleted;

    System.out.println("📈 Общая статистика:");
    System.out.println("  Всего объявлений: " + total);
    System.out.println(
        "  Активные: " + active + " (" + (total > 0 ? active * 100 / total : 0) + "%)");
    System.out.println(
        "  Черновики: " + draft + " (" + (total > 0 ? draft * 100 / total : 0) + "%)");
    System.out.println(
        "  Приостановленные: " + paused + " (" + (total > 0 ? paused * 100 / total : 0) + "%)");
    System.out.println(
        "  Удаленные: " + deleted + " (" + (total > 0 ? deleted * 100 / total : 0) + "%)");
    System.out.println(
        "  Избранные: " + favorites + " (" + (total > 0 ? favorites * 100 / total : 0) + "%)");
    System.out.println("  Уникальных категорий: " + allCategories);

    // Статистика по типам
    var allAds = new ArrayList<Advertisement>();
    allAds.addAll(repository.findByStatus(AdvertisementStatus.ACTIVE));
    allAds.addAll(repository.findByStatus(AdvertisementStatus.DRAFT));
    allAds.addAll(repository.findByStatus(AdvertisementStatus.PAUSED));
    allAds.addAll(repository.findByStatus(AdvertisementStatus.DELETED));

    long objectsCount = allAds.stream().filter(ad -> ad.getType() == Type.OBJECTS).count();
    long servicesCount = allAds.stream().filter(ad -> ad.getType() == Type.SERVICES).count();

    System.out.println("\n📦 Распределение по типам:");
    System.out.println(
        "  Товары (OBJECTS): " + objectsCount + " (" + (total > 0 ? objectsCount * 100 / total : 0)
            + "%)");
    System.out.println(
        "  Услуги (SERVICES): " + servicesCount + " (" + (total > 0 ? servicesCount * 100 / total
            : 0) + "%)");

    // Статистика по группам категорий
    Map<String, Integer> groupStats = new HashMap<>();
    for (Advertisement ad : allAds) {
      if (ad.getCategory() != null) {
        String group = ad.getCategory().getGroup();
        groupStats.put(group, groupStats.getOrDefault(group, 0) + 1);
      }
    }

    if (!groupStats.isEmpty()) {
      System.out.println("\n🏷️ Распределение по группам категорий:");
      groupStats.entrySet().stream()
          .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
          .forEach(entry -> {
            int percent = total > 0 ? entry.getValue() * 100 / total : 0;
            System.out.printf("  %-30s: %d (%d%%)\n", entry.getKey(), entry.getValue(), percent);
          });
    }

    // Статистика по ценам
    if (!allAds.isEmpty()) {
      var adsWithPrice = allAds.stream()
          .filter(ad -> ad.getPrice() != null && ad.getPrice() > 0)
          .toList();

      if (!adsWithPrice.isEmpty()) {
        double avgPrice = adsWithPrice.stream()
            .mapToLong(Advertisement::getPrice)
            .average()
            .orElse(0);

        long maxPrice = adsWithPrice.stream()
            .mapToLong(Advertisement::getPrice)
            .max()
            .orElse(0);

        long minPrice = adsWithPrice.stream()
            .mapToLong(Advertisement::getPrice)
            .min()
            .orElse(0);

        System.out.println("\n💰 Статистика по ценам:");
        System.out.println("  Объявлений с ценой: " + adsWithPrice.size() + " (" +
            (adsWithPrice.size() * 100 / allAds.size()) + "%)");
        System.out.println("  Средняя цена: " + String.format("%.0f", avgPrice) + " руб.");
        System.out.println("  Минимальная цена: " + minPrice + " руб.");
        System.out.println("  Максимальная цена: " + maxPrice + " руб.");
      }
    }

    // Статистика по фото
    int totalPhotos = allAds.stream()
        .mapToInt(ad -> ad.getPhotoUrls().size())
        .sum();

    double avgPhotos = total > 0 ? (double) totalPhotos / total : 0;

    System.out.println("\n📷 Статистика по фото:");
    System.out.println("  Всего фото: " + totalPhotos);
    System.out.println("  Среднее фото на объявление: " + String.format("%.1f", avgPhotos));

    long adsWithPhotos = allAds.stream()
        .filter(ad -> !ad.getPhotoUrls().isEmpty())
        .count();

    System.out.println("  Объявлений с фото: " + adsWithPhotos + " (" +
        (total > 0 ? adsWithPhotos * 100 / total : 0) + "%)");
  }

  private static void printAdvertisement(Advertisement ad) {
    System.out.println("\n📄 Объявление #" + ad.getId());
    System.out.println("─────────────────────────────────────");
    System.out.println("Тип: " + ad.getType());
    System.out.println("Статус: " + ad.getStatus());
    System.out.println("Автор: " + ad.getAuthorId());
    System.out.println("Название: " + ad.getName());
    System.out.println(
        "Описание: " + (ad.getDescription() != null ? ad.getDescription() : "(нет)"));
    System.out.println(
        "Цена: " + (ad.getPrice() != null ? ad.getPrice() + " руб." : "(не указана)"));
    System.out.println("Категория: " +
        (ad.getCategory() != null ? ad.getCategory().getDisplayName() : "(не указана)"));
    System.out.println("❤️ Избранное: " + (ad.isFavorite() ? "ДА" : "нет"));
    System.out.println("Создано: " + ad.getCreatedAt());

    System.out.println("\n📷 Фото (" + ad.getPhotoUrls().size() + "):");
    int photoNum = 1;
    for (String url : ad.getPhotoUrls()) {
      System.out.println("  " + photoNum + ". " + url);
      photoNum++;
    }
    System.out.println();
  }
}