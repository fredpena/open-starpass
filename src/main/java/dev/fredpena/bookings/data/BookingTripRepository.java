package dev.fredpena.bookings.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingTripRepository extends JpaRepository<BookingTrip, Long> {

    List<BookingTrip> findAllByOrderByDepartOnAsc();

    List<BookingTrip> findAllByCustomerIdOrderByDepartOnAsc(Long customerId);

    long countByCustomerId(Long customerId);
}
