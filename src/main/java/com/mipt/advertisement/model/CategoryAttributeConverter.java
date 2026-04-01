package com.mipt.advertisement.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CategoryAttributeConverter implements AttributeConverter<Category, String> {

 @Override
 public String convertToDatabaseColumn(Category attribute) {
  return attribute == null ? null : attribute.name();
 }

 @Override
 public Category convertToEntityAttribute(String dbData) {
  if (dbData == null || dbData.isBlank()) {
   return null;
  }
  Category parsed = Category.fromDbValue(dbData);
  if (parsed == null) {
   throw new IllegalArgumentException("Unknown category value in DB: " + dbData);
  }
  return parsed;
 }
}
