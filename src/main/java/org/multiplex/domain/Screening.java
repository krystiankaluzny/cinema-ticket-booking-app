package org.multiplex.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
class Screening {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name = "fk_movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "fk_room_id")
    private Room room;

    private OffsetDateTime startScreeningTime;

}
