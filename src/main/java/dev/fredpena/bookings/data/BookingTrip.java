package dev.fredpena.bookings.data;

import java.time.LocalDate;

public class BookingTrip {

    private Long id;
    private String code;
    private String origin;
    private String destination;
    private LocalDate departOn;
    private LocalDate arrivalOn;
    private String durationLabel;
    private String status;
    private String vesselName;
    private String flightCode;
    private String cabinClass;
    private String summary;
    private BookingCustomer customer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartOn() {
        return departOn;
    }

    public void setDepartOn(LocalDate departOn) {
        this.departOn = departOn;
    }

    public LocalDate getArrivalOn() {
        return arrivalOn;
    }

    public void setArrivalOn(LocalDate arrivalOn) {
        this.arrivalOn = arrivalOn;
    }

    public String getDurationLabel() {
        return durationLabel;
    }

    public void setDurationLabel(String durationLabel) {
        this.durationLabel = durationLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVesselName() {
        return vesselName;
    }

    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(String cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public BookingCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(BookingCustomer customer) {
        this.customer = customer;
    }
}
