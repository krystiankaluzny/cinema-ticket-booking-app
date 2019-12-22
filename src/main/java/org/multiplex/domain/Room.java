package org.multiplex.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
class Room {
    @Id
    @GeneratedValue
    private int id;
    private String name;
    private int rowCount;
    private int columnCount;
}
