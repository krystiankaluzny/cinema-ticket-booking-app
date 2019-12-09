package org.multiplex.domain;

import java.math.BigDecimal;

interface ReservationPricingPolicy {

    Price getPrice(ReservationType type);

    class Price {
        public static final Price ZERO = new Price(BigDecimal.ZERO);
        private BigDecimal value;

        private Price(BigDecimal value) {
            this.value = value;
        }

        public void add(Price priceToAdd) {
            value = value.add(priceToAdd.value);
        }

        public static Price from(BigDecimal value) {
            return new Price(value);
        }

        public BigDecimal getValue() {
            return value;
        }
    }
}
