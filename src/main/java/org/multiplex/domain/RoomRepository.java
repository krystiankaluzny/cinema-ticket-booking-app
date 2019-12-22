package org.multiplex.domain;

import org.springframework.data.repository.Repository;

interface RoomRepository extends Repository<Room, Integer> {
    Room save(Room rom);
}
