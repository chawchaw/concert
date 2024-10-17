package com.chaw.concert.app.domain.concert.reserve.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReserveStatusConverter implements AttributeConverter<ReserveStatus, String> {

    @Override
    public String convertToDatabaseColumn(ReserveStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDbValue();
    }

    @Override
    public ReserveStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ReserveStatus.fromDbValue(dbData);
    }
}
