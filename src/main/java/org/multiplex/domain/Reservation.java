package org.multiplex.domain;

import lombok.Value;

@Value
class Reservation {
    private int id;
    private int screeningId;
    private int row;
    private int column;
    private User user;

    @Value
    static class User {
        private String name;
        private String surname;
    }
}
