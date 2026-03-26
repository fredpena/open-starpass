package dev.fredpena.bookings.data;

public record BookingSidebarData(
        long upcomingCount,
        long customerCount,
        long confirmedCount,
        long vipCustomerCount
) {
}
