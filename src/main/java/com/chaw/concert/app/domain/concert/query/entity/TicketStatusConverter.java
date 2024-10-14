package com.chaw.concert.app.domain.concert.query.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TicketStatusConverter implements AttributeConverter<TicketStatus, String> {

    @Override
    public String convertToDatabaseColumn(TicketStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDbValue();
    }

    @Override
    public TicketStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TicketStatus.fromDbValue(dbData);
    }
}
