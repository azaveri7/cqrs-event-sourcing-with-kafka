package com.techbank.account.query.infrastructure;

import com.techbank.cqrs.core.domain.BaseEntity;
import com.techbank.cqrs.core.infrastructure.QueryDispatcher;
import com.techbank.cqrs.core.queries.BaseQuery;
import com.techbank.cqrs.core.queries.QueryHandlerMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountQueryDispatcher implements QueryDispatcher {
    private final Map<Class<? extends BaseQuery>, List<QueryHandlerMethod>> routers = new HashMap<>();
    @Override
    public <T extends BaseQuery> void registerHandler(Class<T> type, QueryHandlerMethod<T> handler) {
        var handlers = routers.computeIfAbsent(type, c -> new LinkedList<>());
        handlers.add(handler);
    }

    @Override
    public <U extends BaseEntity> List<U> send(BaseQuery query) {
        var handlers = routers.get(query.getClass());
        if(Objects.isNull(handlers) || handlers.size() == 0) {
            throw new RuntimeException("No query handler was registered!");
        }
        if(handlers.size() > 1){
            throw new RuntimeException("Cannot send query to more than one handler!");
        }
        return handlers.get(0).handle(query);
    }
}
