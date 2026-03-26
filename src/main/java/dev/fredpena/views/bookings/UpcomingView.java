package dev.fredpena.views.bookings;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.fredpena.bookings.data.BookingCatalogService;
import dev.fredpena.bookings.data.BookingCustomer;
import dev.fredpena.bookings.data.BookingTrip;
import dev.fredpena.views.MainLayout;
import dev.fredpena.views.components.PrimarySidebar;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@PageTitle("Bookings - Upcoming")
@RouteAlias("")
@Route(value = "bookings/upcoming/:tripId?", layout = MainLayout.class)
@AnonymousAllowed
public class UpcomingView extends Div implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US);

    private final BookingCatalogService bookingCatalogService;
    private final Div bookingsContent = new Div();
    private final Div listScroller = new Div();
    private final Div detailHost = new Div();
    private final TextField searchField = new TextField();

    private Long selectedTripId;
    private String searchTerm = "";

    public UpcomingView(BookingCatalogService bookingCatalogService) {
        this.bookingCatalogService = bookingCatalogService;

        addClassNames("sp-shell", "sp-shell--bookings");
        setSizeFull();

        add(new PrimarySidebar("bookings"));
        add(new BookingSecondaryNav(bookingCatalogService, "upcoming"));

        bookingsContent.addClassName("sp-bookings-content");
        bookingsContent.addClassName("sp-bookings-content--upcoming");

        listScroller.addClassName("sp-booking-list-panel__scroller");
        detailHost.addClassNames("sp-bookings-content__detail-host", "sp-bookings-content__detail-host--hidden");

        bookingsContent.add(buildBookingListPanel(), detailHost);
        add(bookingsContent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedTripId = event.getRouteParameters().get("tripId")
                .flatMap(value -> {
                    try {
                        return java.util.Optional.of(Long.parseLong(value));
                    } catch (NumberFormatException _) {
                        return java.util.Optional.empty();
                    }
                })
                .orElse(null);
        refreshView();
    }

    private Div buildBookingListPanel() {
        Div panel = new Div();
        panel.addClassName("sp-booking-list-panel");

        Div header = new Div();
        header.addClassName("sp-booking-list-panel__header");

        Div titleBlock = new Div();
        titleBlock.addClassName("sp-booking-list-panel__title-block");

        Span eyebrow = new Span("Live itineraries");
        eyebrow.addClassName("sp-booking-list-panel__eyebrow");

        H2 title = new H2("Upcoming");
        title.addClassNames("sp-u-text-xl", "sp-u-font-bold", "sp-u-m-0");

        Paragraph summary = new Paragraph("High-value departures, boarding windows and passenger context in one operational surface.");
        summary.addClassName("sp-booking-list-panel__summary");

        titleBlock.add(eyebrow, title, summary);

        searchField.setPlaceholder("Search route, customer, booking code...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> {
            searchTerm = event.getValue() == null ? "" : event.getValue().trim().toLowerCase(Locale.ROOT);
            refreshView();
        });
        searchField.addClassName("sp-booking-list-panel__search");

        header.add(titleBlock, searchField);

        Div stats = new Div();
        stats.addClassName("sp-booking-strip");
        stats.add(buildStripCard("Confirmed", String.valueOf(bookingCatalogService.getSidebarData().confirmedCount())));
        stats.add(buildStripCard("Pending review", String.valueOf(filteredTrips().stream()
                .filter(trip -> "Pending".equals(trip.getStatus())).count())));
        stats.add(buildStripCard("Routes", String.valueOf(bookingCatalogService.getSidebarData().upcomingCount())));

        panel.add(header, stats, listScroller);
        return panel;
    }

    private Div buildStripCard(String label, String value) {
        Div card = new Div();
        card.addClassName("sp-booking-strip__card");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("sp-booking-strip__value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-booking-strip__label");

        card.add(valueSpan, labelSpan);
        return card;
    }

    private void refreshView() {
        refreshTripList();
        refreshDetailsPanel();
    }

    private void refreshTripList() {
        listScroller.removeAll();
        List<BookingTrip> trips = filteredTrips();
        for (BookingTrip trip : trips) {
            listScroller.add(buildBookingItem(trip));
        }

        if (trips.isEmpty()) {
            listScroller.add(buildEmptyListState());
        }
    }

    private Div buildEmptyListState() {
        Div state = new Div();
        state.addClassName("sp-booking-list-empty");

        Span title = new Span("No itineraries match this search");
        title.addClassName("sp-booking-list-empty__title");

        Span text = new Span("Try a different route, passenger or booking code.");
        text.addClassName("sp-booking-list-empty__text");

        state.add(title, text);
        return state;
    }

    private Div buildBookingItem(BookingTrip trip) {
        Div item = new Div();
        item.addClassName("sp-booking-item");
        if (trip.getId().equals(selectedTripId)) {
            item.addClassName("sp-booking-item--selected");
        }

        item.addClickListener(event -> navigateToTrip(trip));

        Div left = new Div();
        left.addClassName("sp-booking-item__left");

        Span bookingId = new Span(trip.getCode());
        bookingId.addClassName("sp-booking-item__code");

        Span fromName = new Span(trip.getOrigin());
        fromName.addClassName("sp-booking-item__place");

        Span passenger = new Span(trip.getCustomer().getName());
        passenger.addClassName("sp-booking-item__subtext");

        left.add(bookingId, fromName, passenger);

        Div center = new Div();
        center.addClassName("sp-booking-flight-line");

        Div flightRow = new Div();
        flightRow.addClassName("sp-booking-flight-row");
        flightRow.add(new Div(), buildFlightIcon(), new Div());
        flightRow.getChildren().filter(Div.class::isInstance)
                .forEach(component -> component.addClassName("sp-booking-flight-hr"));

        Span duration = new Span(trip.getDurationLabel());
        duration.addClassName("sp-booking-item__subtext");

        center.add(flightRow, duration);

        Div right = new Div();
        right.addClassName("sp-booking-item__right");

        Span statusBadge = new Span(trip.getStatus());
        statusBadge.getElement().getThemeList().add("badge " + ("Confirmed".equals(trip.getStatus()) ? "success" : "contrast"));

        Span toName = new Span(trip.getDestination());
        toName.addClassName("sp-booking-item__place");

        Span departDate = new Span(formatDate(trip.getDepartOn()));
        departDate.addClassName("sp-booking-item__subtext");

        right.add(statusBadge, toName, departDate);

        item.add(left, center, right);
        return item;
    }

    private Icon buildFlightIcon() {
        Icon icon = VaadinIcon.AIRPLANE.create();
        icon.addClassName("sp-booking-flight-row__icon");
        return icon;
    }

    private void refreshDetailsPanel() {
        BookingTrip selectedTrip = filteredTrips().stream()
                .filter(trip -> trip.getId().equals(selectedTripId))
                .findFirst()
                .orElse(null);

        detailHost.removeAll();

        boolean hasSelection = selectedTrip != null;
        bookingsContent.setClassName("sp-bookings-content--detail-hidden", !hasSelection);
        detailHost.setClassName("sp-bookings-content__detail-host--hidden", !hasSelection);

        if (!hasSelection) {
            return;
        }

        detailHost.add(buildBookingDetails(selectedTrip));
    }

    private Div buildBookingDetails(BookingTrip trip) {
        Div panel = new Div();
        panel.addClassName("sp-booking-details-panel");

        Div header = new Div();
        header.addClassName("sp-booking-details__header");

        Div titleBlock = new Div();
        titleBlock.addClassName("sp-booking-details__title-block");

        Span eyebrow = new Span("Upcoming transfer");
        eyebrow.addClassName("sp-booking-details__eyebrow");

        H3 title = new H3(trip.getOrigin() + " to " + trip.getDestination());
        title.addClassName("sp-booking-details__title");

        Span code = new Span(trip.getCode() + " • " + trip.getCustomer().getName());
        code.addClassName("sp-booking-details__meta");

        titleBlock.add(eyebrow, title, code);

        Div actions = new Div();
        actions.addClassName("sp-booking-details__actions");

        Button notifyBtn = new Button(VaadinIcon.ENVELOPE.create(), event ->
                Notification.show("Passenger update flow is ready for the next pass.", 2500, Notification.Position.BOTTOM_START));
        notifyBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);

        Button manageBtn = new Button("Update booking", event ->
                Notification.show("Booking operations editor comes next.", 2500, Notification.Position.BOTTOM_START));
        manageBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        manageBtn.addClassName("sp-booking-details__primary-action");

        actions.add(notifyBtn, manageBtn);
        header.add(titleBlock, actions);

        Div body = new Div();
        body.addClassName("sp-booking-details__body");

        Div hero = new Div();
        hero.addClassName("sp-booking-details__hero");
        hero.add(buildRouteChip(trip));
        hero.add(buildBookingSummary(trip));

        Div metricStrip = new Div();
        metricStrip.addClassName("sp-booking-details__metric-strip");
        metricStrip.add(metricCard("Departure", formatDate(trip.getDepartOn()), trip.getOrigin()));
        metricStrip.add(metricCard("Arrival", formatDate(trip.getArrivalOn()), trip.getDestination()));
        metricStrip.add(metricCard("Cabin", trip.getCabinClass(), trip.getStatus()));

        Div travelerCard = buildTravelerCard(trip.getCustomer());
        Div operationsCard = buildOperationsCard(trip);

        body.add(hero, metricStrip, travelerCard, operationsCard);
        panel.add(header, body);
        return panel;
    }

    private Div buildRouteChip(BookingTrip trip) {
        Div routeChip = new Div();
        routeChip.addClassName("sp-booking-route-chip");

        Span from = new Span(trip.getOrigin());
        from.addClassName("sp-booking-route-chip__stop");

        Icon arrow = VaadinIcon.ARROW_RIGHT.create();
        arrow.addClassName("sp-booking-route-chip__icon");

        Span to = new Span(trip.getDestination());
        to.addClassName("sp-booking-route-chip__stop");

        routeChip.add(from, arrow, to);
        return routeChip;
    }

    private Div buildBookingSummary(BookingTrip trip) {
        Div card = new Div();
        card.addClassName("sp-booking-details__card");

        Span title = new Span("Trip brief");
        title.addClassName("sp-booking-details__card-title");

        Paragraph summary = new Paragraph(trip.getSummary());
        summary.addClassName("sp-booking-details__summary");

        card.add(title, summary);
        return card;
    }

    private Div buildTravelerCard(BookingCustomer customer) {
        Div card = new Div();
        card.addClassName("sp-booking-details__card");

        Span title = new Span("Traveler");
        title.addClassName("sp-booking-details__card-title");

        Div row = new Div();
        row.addClassName("sp-booking-traveler");

        Avatar avatar = new Avatar(customer.getName());
        avatar.setColorIndex(customer.getColorIndex());
        avatar.addClassName("sp-booking-traveler__avatar");

        Div meta = new Div();
        meta.addClassName("sp-booking-traveler__meta");

        Span name = new Span(customer.getName());
        name.addClassName("sp-booking-traveler__name");

        Span titleLine = new Span(customer.getTitle());
        titleLine.addClassName("sp-booking-traveler__line");

        Span contact = new Span(customer.getEmail() + " • " + customer.getLocation());
        contact.addClassName("sp-booking-traveler__line");

        meta.add(name, titleLine, contact);

        row.add(avatar, meta);
        card.add(title, row);
        return card;
    }

    private Div buildOperationsCard(BookingTrip trip) {
        Div card = new Div();
        card.addClassName("sp-booking-details__card");

        Span title = new Span("Operations");
        title.addClassName("sp-booking-details__card-title");

        Div list = new Div();
        list.addClassName("sp-booking-info-list");
        list.add(infoRow(VaadinIcon.AIRPLANE.create(), "Flight code", trip.getFlightCode()));
        list.add(infoRow(VaadinIcon.ROCKET.create(), "Vessel", trip.getVesselName()));
        list.add(infoRow(VaadinIcon.CLOCK.create(), "Duration", trip.getDurationLabel()));

        card.add(title, list);
        return card;
    }

    private Div infoRow(Icon icon, String label, String value) {
        Div row = new Div();
        row.addClassName("sp-booking-info-row");

        Div iconWrap = new Div();
        iconWrap.addClassName("sp-booking-info-row__icon");
        iconWrap.add(icon);

        Div text = new Div();
        text.addClassName("sp-booking-info-row__text");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-booking-info-row__label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("sp-booking-info-row__value");

        text.add(labelSpan, valueSpan);
        row.add(iconWrap, text);
        return row;
    }

    private Div metricCard(String label, String value, String meta) {
        Div card = new Div();
        card.addClassName("sp-booking-metric-card");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-booking-metric-card__label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("sp-booking-metric-card__value");

        Span metaSpan = new Span(meta);
        metaSpan.addClassName("sp-booking-metric-card__meta");

        card.add(labelSpan, valueSpan, metaSpan);
        return card;
    }

    private List<BookingTrip> filteredTrips() {
        return bookingCatalogService.findAllUpcomingTrips().stream()
                .filter(trip -> matches(trip, searchTerm))
                .toList();
    }

    private boolean matches(BookingTrip trip, String term) {
        if (term == null || term.isBlank()) {
            return true;
        }
        String haystack = String.join(" ",
                trip.getCode(),
                trip.getOrigin(),
                trip.getDestination(),
                trip.getStatus(),
                trip.getCustomer().getName(),
                trip.getCustomer().getEmail(),
                trip.getFlightCode(),
                trip.getVesselName()).toLowerCase(Locale.ROOT);
        return haystack.contains(term);
    }

    private void navigateToTrip(BookingTrip trip) {
        getUI().ifPresent(ui -> {
            if (trip.getId().equals(selectedTripId)) {
                ui.navigate("bookings/upcoming");
            } else {
                ui.navigate("bookings/upcoming/" + trip.getId());
            }
        });
    }

    private String formatDate(java.time.LocalDate date) {
        return DATE_FORMATTER.format(date);
    }
}
