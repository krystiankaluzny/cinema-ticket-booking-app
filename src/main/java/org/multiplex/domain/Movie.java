package org.multiplex.domain;

import lombok.Value;

import java.time.Duration;

@Value
class Movie {
    private int id;
    private String title;
    private Duration duration;
}
