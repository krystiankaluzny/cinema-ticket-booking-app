package org.multiplex.domain;

import java.math.BigDecimal;

class StandardPricingPolicy implements ReservationPricingPolicy {

    private static final Price ADULT = Price.from(BigDecimal.valueOf(25));
    private static final Price STUDENT = Price.from(BigDecimal.valueOf(18));
    private static final Price CHILD = Price.from(BigDecimal.valueOf(12.50));

    @Override
    public Price getPrice(ReservationType type) {
        switch (type) {
            case ADULT:
                return ADULT;
            case STUDENT:
                return STUDENT;
            case CHILD:
                return CHILD;
        }

        throw new IllegalArgumentException("Unknown reservation type:" + type);
    }
}
