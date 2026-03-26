package dev.fredpena.bookings.data;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BookingDataInitializer implements CommandLineRunner {

    private final BookingCustomerRepository bookingCustomerRepository;
    private final BookingTripRepository bookingTripRepository;

    public BookingDataInitializer(BookingCustomerRepository bookingCustomerRepository,
                                  BookingTripRepository bookingTripRepository) {
        this.bookingCustomerRepository = bookingCustomerRepository;
        this.bookingTripRepository = bookingTripRepository;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (bookingCustomerRepository.count() > 0 || bookingTripRepository.count() > 0) {
            return;
        }

        BookingCustomer emma = bookingCustomerRepository.save(customer(
                "Emma Executive", "emma.executive@starpass.net", "Explorer in Residence",
                "Active", "Moon Base One", "+1 809 550 1100", 1, true));
        BookingCustomer alice = bookingCustomerRepository.save(customer(
                "Alice Smith", "alice.smith@apex.net", "Research Lead",
                "Active", "New London Station", "+1 809 550 1101", 2, true));
        BookingCustomer bob = bookingCustomerRepository.save(customer(
                "Bob Johnson", "bob.johnson@starpass.net", "Product Strategist",
                "Active", "Mars Dock Alpha", "+1 809 550 1102", 3, false));
        BookingCustomer emily = bookingCustomerRepository.save(customer(
                "Emily Davis", "emily.davis@gmail.com", "Experience Designer",
                "Active", "Europa Hub", "+1 809 550 1103", 4, false));
        BookingCustomer michael = bookingCustomerRepository.save(customer(
                "Michael Brown", "michael.brown@apex.net", "Partnership Director",
                "Pending", "Orbital City", "+1 809 550 1104", 5, false));
        BookingCustomer sophia = bookingCustomerRepository.save(customer(
                "Sophia Miller", "sophia.miller@gmail.com", "Finance Officer",
                "Active", "Titan Port", "+1 809 550 1105", 1, false));

        bookingTripRepository.saveAll(List.of(
                trip("IRST-289163384", "Earth", "Kepler-22b", LocalDate.parse("2026-04-14"),
                        LocalDate.parse("2026-09-10"), "149 Earth days", "Confirmed",
                        "Galactic Pioneer", "ISV-2789", "Observation Suite",
                        "Long-haul scientific route with concierge support and extended orbital prep.", emma),
                trip("IRST-102938475", "Earth", "Mars", LocalDate.parse("2026-05-02"),
                        LocalDate.parse("2026-07-11"), "70 Earth days", "Confirmed",
                        "Red Horizon", "ISV-1042", "Priority Cabin",
                        "High-frequency frontier route designed for executive travelers and partner teams.", alice),
                trip("IRST-556677889", "Moon Base", "Titan", LocalDate.parse("2026-05-18"),
                        LocalDate.parse("2026-08-28"), "102 Earth days", "Pending",
                        "Celestial Arrow", "ISV-3301", "Deep Space Cabin",
                        "Extended methane-atmosphere mission with climate-adaptive living quarters.", bob),
                trip("IRST-334455667", "Earth", "Europa", LocalDate.parse("2026-06-05"),
                        LocalDate.parse("2026-09-17"), "104 Earth days", "Confirmed",
                        "Ice Pioneer", "ISV-4412", "Research Deck",
                        "Premium research itinerary with lab modules and under-ice observation access.", emily),
                trip("IRST-778899001", "Mars", "Jupiter Orbit", LocalDate.parse("2026-06-22"),
                        LocalDate.parse("2026-09-19"), "89 Earth days", "Pending",
                        "Jupiter Express", "ISV-5503", "Orbit Lounge",
                        "Fast-transfer corridor for orbital meetings and gravitational fly-by experiences.", michael),
                trip("IRST-447733118", "Earth", "Saturn Ring Gate", LocalDate.parse("2026-07-08"),
                        LocalDate.parse("2026-11-03"), "118 Earth days", "Confirmed",
                        "Aurora Veil", "ISV-6124", "Vista Class",
                        "Signature leisure expedition centered on ring-view suites and premium hospitality.", sophia)
        ));
    }

    private BookingCustomer customer(String name, String email, String title, String status,
                                     String location, String phone, int colorIndex, boolean vip) {
        BookingCustomer customer = new BookingCustomer();
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

    private BookingTrip trip(String code, String origin, String destination, LocalDate departOn,
                             LocalDate arrivalOn, String durationLabel, String status,
                             String vesselName, String flightCode, String cabinClass,
                             String summary, BookingCustomer customer) {
        BookingTrip trip = new BookingTrip();
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
