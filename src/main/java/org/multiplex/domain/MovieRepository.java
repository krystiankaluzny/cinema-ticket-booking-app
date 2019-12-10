package org.multiplex.domain;

import org.springframework.data.repository.Repository;

interface MovieRepository extends Repository<Movie, Integer> {
    Movie save(Movie movie);
}
