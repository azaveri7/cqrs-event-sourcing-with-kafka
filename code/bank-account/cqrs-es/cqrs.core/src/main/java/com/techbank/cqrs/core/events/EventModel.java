package com.techbank.cqrs.core.events;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@Document(collation = "en")//this tells spring that this model can be persisted in the mongodb database
public class EventModel {
    @Id
    private String id;
    private Date timestamp;
    private String aggregateIdentifier;
    private String aggregateType;
    private int version;
    private String eventType;
    private BaseEvent eventData;
}
