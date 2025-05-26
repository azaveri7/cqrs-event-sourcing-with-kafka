package com.techbank.account.cmd.infrastructure;

import com.techbank.account.cmd.domain.AccountAggregate;
import com.techbank.account.cmd.domain.EventStoreRepository;
import com.techbank.account.cmd.exceptions.AggregateNotFoundException;
import com.techbank.account.cmd.exceptions.ConcurrencyException;
import com.techbank.cqrs.core.events.BaseEvent;
import com.techbank.cqrs.core.events.EventModel;
import com.techbank.cqrs.core.infrastructure.EventStore;
import com.techbank.cqrs.core.producers.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountEventStore implements EventStore {
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private EventStoreRepository eventStoreRepository;

    @Override
    public void saveEvents(String aggregateId, Iterable<BaseEvent> events, int expectedVersion) {
        var eventStream = eventStoreRepository.findByAggregateIdentifier(aggregateId);
        if(expectedVersion != -1 && eventStream.get(eventStream.size() - 1).getVersion() != expectedVersion) {
            throw new ConcurrencyException();
        }
        int version = expectedVersion;
        for(var event: events) {
            version += 1;
            event.setVersion(version);
            var eventModel = EventModel.builder()
                                .timestamp(new Date())
                                .aggregateIdentifier(aggregateId)
                                .aggregateType(AccountAggregate.class.getName())
                                .version(version)
                                .eventType(event.getClass().getTypeName())
                                .eventData(event)
                                .build();
            var persistedEvent = eventStoreRepository.save(eventModel);
            if(!persistedEvent.getId().isEmpty()) {
                eventProducer.produce(event.getClass().getSimpleName(), event);
            }
        }
    }

    @Override
    public List<BaseEvent> getEvents(String aggregateId) {
        var eventStream = eventStoreRepository.findByAggregateIdentifier(aggregateId);
        if(Objects.isNull(eventStream) || eventStream.isEmpty()) {
            throw new AggregateNotFoundException("Incorrect account Id provided");
        }
        return eventStream.stream()
                .map(e -> e.getEventData())
                .collect(Collectors.toList());
    }
}
