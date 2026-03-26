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

    private final BookingSessionStore sessionStore;

    public BookingCatalogService(BookingSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public List<BookingTrip> findAllUpcomingTrips() {
        return sessionStore.findAllUpcomingTrips();
    }

    public Optional<BookingTrip> findTrip(Long id) {
        return sessionStore.findTrip(id);
    }

    public List<BookingCustomer> findAllCustomers() {
        return sessionStore.findAllCustomers();
    }

    public Optional<BookingCustomer> findCustomer(Long id) {
        return sessionStore.findCustomer(id);
    }

    public List<BookingTrip> findTripsForCustomer(Long customerId) {
        return sessionStore.findTripsForCustomer(customerId);
    }

    public Map<Long, Long> bookingCountsByCustomer() {
        return sessionStore.findAllUpcomingTrips().stream()
                .collect(Collectors.groupingBy(trip -> trip.getCustomer().getId(), Collectors.counting()));
    }

    public Map<Long, BookingTrip> nextTripByCustomer() {
        return sessionStore.findAllUpcomingTrips().stream()
                .collect(Collectors.toMap(trip -> trip.getCustomer().getId(), Function.identity(), (left, right) -> left));
    }

    public BookingSidebarData getSidebarData() {
        List<BookingTrip> trips = sessionStore.findAllUpcomingTrips();
        List<BookingCustomer> customers = sessionStore.findAllCustomers();
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
        return sessionStore.findTripsForCustomer(customerId).stream()
                .sorted(Comparator.comparing(BookingTrip::getDepartOn))
                .limit(limit)
                .toList();
    }
}
