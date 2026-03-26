package dev.fredpena.bookings.data;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingCatalogService {

    private final BookingTripRepository bookingTripRepository;
    private final BookingCustomerRepository bookingCustomerRepository;

    public BookingCatalogService(BookingTripRepository bookingTripRepository,
                                 BookingCustomerRepository bookingCustomerRepository) {
        this.bookingTripRepository = bookingTripRepository;
        this.bookingCustomerRepository = bookingCustomerRepository;
    }

    public List<BookingTrip> findAllUpcomingTrips() {
        return bookingTripRepository.findAllByOrderByDepartOnAsc();
    }

    public Optional<BookingTrip> findTrip(Long id) {
        return bookingTripRepository.findById(id);
    }

    public List<BookingCustomer> findAllCustomers() {
        return bookingCustomerRepository.findAllByOrderByNameAsc();
    }

    public Optional<BookingCustomer> findCustomer(Long id) {
        return bookingCustomerRepository.findById(id);
    }

    public List<BookingTrip> findTripsForCustomer(Long customerId) {
        return bookingTripRepository.findAllByCustomerIdOrderByDepartOnAsc(customerId);
    }

    public Map<Long, Long> bookingCountsByCustomer() {
        return bookingTripRepository.findAll().stream()
                .collect(Collectors.groupingBy(trip -> trip.getCustomer().getId(), Collectors.counting()));
    }

    public Map<Long, BookingTrip> nextTripByCustomer() {
        return bookingTripRepository.findAllByOrderByDepartOnAsc().stream()
                .collect(Collectors.toMap(trip -> trip.getCustomer().getId(), Function.identity(), (left, right) -> left));
    }

    public BookingSidebarData getSidebarData() {
        List<BookingTrip> trips = bookingTripRepository.findAll();
        List<BookingCustomer> customers = bookingCustomerRepository.findAll();
        long confirmedCount = trips.stream().filter(trip -> "Confirmed".equals(trip.getStatus())).count();
        long vipCustomerCount = customers.stream().filter(BookingCustomer::isVip).count();
        return new BookingSidebarData(
                trips.size(),
                customers.size(),
                confirmedCount,
                vipCustomerCount
        );
    }

    public List<BookingTrip> recentTripsForCustomer(Long customerId, int limit) {
        return bookingTripRepository.findAllByCustomerIdOrderByDepartOnAsc(customerId).stream()
                .sorted(Comparator.comparing(BookingTrip::getDepartOn))
                .limit(limit)
                .toList();
    }
}
