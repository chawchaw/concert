package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TicketTypeConverter implements AttributeConverter<TicketType, String> {

    @Override
    public String convertToDatabaseColumn(TicketType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDbValue();
    }

    @Override
    public TicketType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TicketType.fromDbValue(dbData);
    }
}
