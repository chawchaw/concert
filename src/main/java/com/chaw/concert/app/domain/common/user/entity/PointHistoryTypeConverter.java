package com.chaw.concert.app.domain.common.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PointHistoryTypeConverter implements AttributeConverter<PointHistoryType, String> {

    @Override
    public String convertToDatabaseColumn(PointHistoryType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDbValue();
    }

    @Override
    public PointHistoryType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return PointHistoryType.fromDbValue(dbData);
    }
}
