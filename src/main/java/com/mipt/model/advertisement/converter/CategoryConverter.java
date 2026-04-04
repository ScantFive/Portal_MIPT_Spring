package com.mipt.model.advertisement.converter;

import com.mipt.model.advertisement.Category;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryConverter implements AttributeConverter<Category, String> {

    @Override
    public String convertToDatabaseColumn(Category category) {
        if (category == null) {
            return null;
        }
        return category.getDisplayName();
    }

    @Override
    public Category convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        Category category = Category.fromNameSafe(dbData);
        if (category == null) {
            throw new IllegalArgumentException("Unknown category in database: " + dbData);
        }
        return category;
    }
}