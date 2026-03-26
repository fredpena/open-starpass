package dev.fredpena.bookings.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "booking_trips")
public class BookingTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(name = "depart_on", nullable = false)
    private LocalDate departOn;

    @Column(name = "arrival_on", nullable = false)
    private LocalDate arrivalOn;

    @Column(name = "duration_label", nullable = false)
    private String durationLabel;

    @Column(nullable = false)
    private String status;

    @Column(name = "vessel_name", nullable = false)
    private String vesselName;

    @Column(name = "flight_code", nullable = false)
    private String flightCode;

    @Column(name = "cabin_class", nullable = false)
    private String cabinClass;

    @Column(nullable = false, length = 600)
    private String summary;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private BookingCustomer customer;

    public Long getId() {
        return id;
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
