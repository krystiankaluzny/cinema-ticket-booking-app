package org.multiplex.domain;

import lombok.Value;

@Value
class Room {
    private int id;
    private String name;
    private int rowCount;
    private int seatsInRowCount;
}
