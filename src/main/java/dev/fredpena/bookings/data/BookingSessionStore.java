package dev.fredpena.bookings.data;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Scope(value = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class BookingSessionStore {

    private final Map<Long, BookingCustomer> customersById = new LinkedHashMap<>();
    private final Map<Long, BookingTrip> tripsById = new LinkedHashMap<>();

    public BookingSessionStore() {
        List<BookingCustomer> customers = BookingSeedData.customers();
        customers.forEach(customer -> customersById.put(customer.getId(), customer));
        BookingSeedData.trips(customers).forEach(trip -> tripsById.put(trip.getId(), trip));
    }

    public List<BookingTrip> findAllUpcomingTrips() {
        return tripsById.values().stream()
                .sorted(Comparator.comparing(BookingTrip::getDepartOn))
                .toList();
    }

    public Optional<BookingTrip> findTrip(Long id) {
        return Optional.ofNullable(tripsById.get(id));
    }

    public List<BookingCustomer> findAllCustomers() {
        return customersById.values().stream()
                .sorted(Comparator.comparing(BookingCustomer::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Optional<BookingCustomer> findCustomer(Long id) {
        return Optional.ofNullable(customersById.get(id));
    }

    public List<BookingTrip> findTripsForCustomer(Long customerId) {
        return tripsById.values().stream()
                .filter(trip -> trip.getCustomer().getId().equals(customerId))
                .sorted(Comparator.comparing(BookingTrip::getDepartOn))
                .toList();
    }
}
