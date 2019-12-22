package org.multiplex.domain;

import org.multiplex.domain.dto.ReservationDto;
import org.multiplex.domain.exception.NoSeatToReserveException;
import org.multiplex.domain.exception.SeatGapException;
import org.multiplex.domain.exception.SeatNotFoundException;
import org.multiplex.domain.exception.SeatReservedException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SeatsValidator {

    public void validate(List<ReservationDto.SeatToReserveDto> seatsToReserve, io.vavr.collection.List<Seat> reservedSeats, Room room) {

        if (seatsToReserve.isEmpty()) {
            throw new NoSeatToReserveException();
        }

        Map<Integer, Set<Integer>> toReserve = new HashMap<>();
        Map<Integer, Set<Integer>> reserved = new HashMap<>();

        seatsToReserve.forEach(seatToReserve -> {
            int row = seatToReserve.getRow();
            int column = seatToReserve.getColumn();
            toReserve.computeIfAbsent(row, i -> new HashSet<>()).add(column);
        });

        reservedSeats.forEach(seat -> {
            int row = seat.getRow();
            int column = seat.getColumn();
            reserved.computeIfAbsent(row, i -> new HashSet<>()).add(column);
        });

        toReserve.forEach((row, columns) -> {

            Set<Integer> reservedColumnsInRow = reserved.computeIfAbsent(row, i -> new HashSet<>());

            columns.forEach(column -> {
                if (row < 1 || row > room.getRowCount()
                        || column < 1 || column > room.getColumnCount()) {
                    throw new SeatNotFoundException(row, column, room.getName());
                }

                if (reservedColumnsInRow.contains(column)) {
                    throw new SeatReservedException(row, column);
                }

                //seat gap on the left from given column
                if (column > 2
                        && reservedColumnsInRow.contains(column - 2)
                        && !reservedColumnsInRow.contains(column - 1)
                        && !toReserve.computeIfAbsent(row, i -> new HashSet<>()).contains(column - 1)
                ) {
                    throw new SeatGapException(row, column);
                }

                //seat gap on the right from given column
                if (column < room.getColumnCount() - 1
                        && reservedColumnsInRow.contains(column + 2)
                        && !reservedColumnsInRow.contains(column + 1)
                        && !toReserve.computeIfAbsent(row, i -> new HashSet<>()).contains(column + 1)
                ) {
                    throw new SeatGapException(row, column);
                }

                reservedColumnsInRow.add(column);
            });
        });
    }
}
