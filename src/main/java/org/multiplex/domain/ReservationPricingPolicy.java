package org.multiplex.domain;

import java.math.BigDecimal;

class ReservationPricingPolicy {

    private static final Price ADULT = Price.from(BigDecimal.valueOf(25));
    private static final Price STUDENT = Price.from(BigDecimal.valueOf(18));
    private static final Price CHILD = Price.from(BigDecimal.valueOf(12.50));

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

    static class Price {
        public static final Price ZERO = new Price(BigDecimal.ZERO);
        private final BigDecimal value;

        private Price(BigDecimal value) {
            this.value = value;
        }

        public Price add(Price priceToAdd) {
            return from(value.add(priceToAdd.value));
        }

        public static Price from(BigDecimal value) {
            return new Price(value);
        }

        public BigDecimal getValue() {
            return value;
        }
    }
}
