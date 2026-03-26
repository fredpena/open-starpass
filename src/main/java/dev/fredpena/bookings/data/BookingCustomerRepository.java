package dev.fredpena.bookings.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingCustomerRepository extends JpaRepository<BookingCustomer, Long> {

    List<BookingCustomer> findAllByOrderByNameAsc();
}
