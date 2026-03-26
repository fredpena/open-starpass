package dev.fredpena.bookings.data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class BookingSeedData {

    private BookingSeedData() {
    }

    static List<BookingCustomer> customers() {
        return List.of(
                customer(1L, "Emma Executive", "emma.executive@starpass.net", "Explorer in Residence",
                        "Active", "Moon Base One", "+1 809 550 1100", 1, true),
                customer(2L, "Alice Smith", "alice.smith@apex.net", "Research Lead",
                        "Active", "New London Station", "+1 809 550 1101", 2, true),
                customer(3L, "Bob Johnson", "bob.johnson@starpass.net", "Product Strategist",
                        "Active", "Mars Dock Alpha", "+1 809 550 1102", 3, false),
                customer(4L, "Emily Davis", "emily.davis@gmail.com", "Experience Designer",
                        "Active", "Europa Hub", "+1 809 550 1103", 4, false),
                customer(5L, "Michael Brown", "michael.brown@apex.net", "Partnership Director",
                        "Pending", "Orbital City", "+1 809 550 1104", 5, false),
                customer(6L, "Sophia Miller", "sophia.miller@gmail.com", "Finance Officer",
                        "Active", "Titan Port", "+1 809 550 1105", 1, false)
        );
    }

    static List<BookingTrip> trips(List<BookingCustomer> customers) {
        Map<Long, BookingCustomer> customersById = customers.stream()
                .collect(Collectors.toMap(BookingCustomer::getId, Function.identity()));
        return List.of(
                trip(1L, "IRST-289163384", "Earth", "Kepler-22b", LocalDate.parse("2026-04-14"),
                        LocalDate.parse("2026-09-10"), "149 Earth days", "Confirmed",
                        "Galactic Pioneer", "ISV-2789", "Observation Suite",
                        "Long-haul scientific route with concierge support and extended orbital prep.", customersById.get(1L)),
                trip(2L, "IRST-102938475", "Earth", "Mars", LocalDate.parse("2026-05-02"),
                        LocalDate.parse("2026-07-11"), "70 Earth days", "Confirmed",
                        "Red Horizon", "ISV-1042", "Priority Cabin",
                        "High-frequency frontier route designed for executive travelers and partner teams.", customersById.get(2L)),
                trip(3L, "IRST-556677889", "Moon Base", "Titan", LocalDate.parse("2026-05-18"),
                        LocalDate.parse("2026-08-28"), "102 Earth days", "Pending",
                        "Celestial Arrow", "ISV-3301", "Deep Space Cabin",
                        "Extended methane-atmosphere mission with climate-adaptive living quarters.", customersById.get(3L)),
                trip(4L, "IRST-334455667", "Earth", "Europa", LocalDate.parse("2026-06-05"),
                        LocalDate.parse("2026-09-17"), "104 Earth days", "Confirmed",
                        "Ice Pioneer", "ISV-4412", "Research Deck",
                        "Premium research itinerary with lab modules and under-ice observation access.", customersById.get(4L)),
                trip(5L, "IRST-778899001", "Mars", "Jupiter Orbit", LocalDate.parse("2026-06-22"),
                        LocalDate.parse("2026-09-19"), "89 Earth days", "Pending",
                        "Jupiter Express", "ISV-5503", "Orbit Lounge",
                        "Fast-transfer corridor for orbital meetings and gravitational fly-by experiences.", customersById.get(5L)),
                trip(6L, "IRST-447733118", "Earth", "Saturn Ring Gate", LocalDate.parse("2026-07-08"),
                        LocalDate.parse("2026-11-03"), "118 Earth days", "Confirmed",
                        "Aurora Veil", "ISV-6124", "Vista Class",
                        "Signature leisure expedition centered on ring-view suites and premium hospitality.", customersById.get(6L))
        );
    }

    private static BookingCustomer customer(Long id, String name, String email, String title, String status,
                                            String location, String phone, int colorIndex, boolean vip) {
        BookingCustomer customer = new BookingCustomer();
        customer.setId(id);
        customer.setName(name);
        customer.setEmail(email);
        customer.setTitle(title);
        customer.setStatus(status);
        customer.setLocation(location);
        customer.setPhone(phone);
        customer.setColorIndex(colorIndex);
        customer.setVip(vip);
        return customer;
    }

    private static BookingTrip trip(Long id, String code, String origin, String destination, LocalDate departOn,
                                    LocalDate arrivalOn, String durationLabel, String status, String vesselName,
                                    String flightCode, String cabinClass, String summary, BookingCustomer customer) {
        BookingTrip trip = new BookingTrip();
        trip.setId(id);
        trip.setCode(code);
        trip.setOrigin(origin);
        trip.setDestination(destination);
        trip.setDepartOn(departOn);
        trip.setArrivalOn(arrivalOn);
        trip.setDurationLabel(durationLabel);
        trip.setStatus(status);
        trip.setVesselName(vesselName);
        trip.setFlightCode(flightCode);
        trip.setCabinClass(cabinClass);
        trip.setSummary(summary);
        trip.setCustomer(customer);
        return trip;
    }
}
