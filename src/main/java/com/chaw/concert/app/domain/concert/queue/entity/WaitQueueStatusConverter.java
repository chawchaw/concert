package com.chaw.concert.app.domain.concert.queue.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WaitQueueStatusConverter implements AttributeConverter<WaitQueueStatus, String> {

    @Override
    public String convertToDatabaseColumn(WaitQueueStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDbValue();
    }

    @Override
    public WaitQueueStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return WaitQueueStatus.fromDbValue(dbData);
    }
}
