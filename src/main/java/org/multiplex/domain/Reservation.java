package org.multiplex.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
class Reservation {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name = "fk_screening_id")
    private Screening screening;

    private String bookingUserName;
    private String bookingUserSurname;
    private OffsetDateTime expirationTime;

    @OneToMany(cascade= CascadeType.ALL)
    @JoinColumn(name = "fk_reservation_id")
    private Set<ReservedSeat> reservedSeats;

    private boolean paid;
}

