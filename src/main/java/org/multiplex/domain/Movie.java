package org.multiplex.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Duration;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
class Movie {
    @Id
    @GeneratedValue
    private int id;
    private String title;
    private Duration duration;
}
