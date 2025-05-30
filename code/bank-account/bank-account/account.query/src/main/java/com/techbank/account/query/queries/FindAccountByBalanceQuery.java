package com.techbank.account.query.queries;

import com.techbank.account.query.api.dto.EqualityType;
import com.techbank.cqrs.core.queries.BaseQuery;

public class FindAccountByBalanceQuery extends BaseQuery {
    private EqualityType equalityType;
    private double balance;
}
