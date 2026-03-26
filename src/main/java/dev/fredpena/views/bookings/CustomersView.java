package dev.fredpena.views.bookings;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.fredpena.bookings.data.BookingCatalogService;
import dev.fredpena.bookings.data.BookingCustomer;
import dev.fredpena.bookings.data.BookingTrip;
import dev.fredpena.views.MainLayout;
import dev.fredpena.views.components.PrimarySidebar;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@PageTitle("Bookings - Customers")
@Route(value = "bookings/customers/:customerId?", layout = MainLayout.class)
@AnonymousAllowed
public class CustomersView extends Div implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    private final BookingCatalogService bookingCatalogService;
    private final Div bookingsContent = new Div();
    private final Div listScroller = new Div();
    private final Div detailHost = new Div();
    private final TextField searchField = new TextField();

    private Long selectedCustomerId;
    private String searchTerm = "";

    public CustomersView(BookingCatalogService bookingCatalogService) {
        this.bookingCatalogService = bookingCatalogService;

        addClassNames("sp-shell", "sp-shell--bookings");
        setSizeFull();

        add(new PrimarySidebar("bookings"));
        add(new BookingSecondaryNav(bookingCatalogService, "customers"));

        bookingsContent.addClassName("sp-bookings-content");
        bookingsContent.addClassName("sp-bookings-content--customers");

        listScroller.addClassName("sp-customers-panel__scroller");
        detailHost.addClassNames("sp-bookings-content__detail-host", "sp-bookings-content__detail-host--hidden");

        bookingsContent.add(buildCustomerListPanel(), detailHost);
        add(bookingsContent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        selectedCustomerId = event.getRouteParameters().get("customerId")
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

    private Div buildCustomerListPanel() {
        Div panel = new Div();
        panel.addClassName("sp-customers-panel");

        Div header = new Div();
        header.addClassName("sp-customers-panel__header");

        Div titleBlock = new Div();
        titleBlock.addClassName("sp-customers-panel__title-block");

        Span eyebrow = new Span("Passenger relationships");
        eyebrow.addClassName("sp-customers-panel__eyebrow");

        H2 title = new H2("Customers");
        title.addClassNames("sp-u-text-xl", "sp-u-font-bold", "sp-u-m-0");

        Paragraph summary = new Paragraph("Profiles, current travel load and high-touch accounts tracked from one customer surface.");
        summary.addClassName("sp-customers-panel__summary");

        titleBlock.add(eyebrow, title, summary);

        searchField.setPlaceholder("Search customer, title, location...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.addValueChangeListener(event -> {
            searchTerm = event.getValue() == null ? "" : event.getValue().trim().toLowerCase(Locale.ROOT);
            refreshView();
        });
        searchField.addClassName("sp-customers-panel__search");

        header.add(titleBlock, searchField);

        Div statStrip = new Div();
        statStrip.addClassName("sp-booking-strip");
        statStrip.add(buildStripCard("Customers", String.valueOf(bookingCatalogService.getSidebarData().customerCount())));
        statStrip.add(buildStripCard("VIP", String.valueOf(bookingCatalogService.getSidebarData().vipCustomerCount())));
        statStrip.add(buildStripCard("Active", String.valueOf(filteredCustomers().stream()
                .filter(customer -> "Active".equals(customer.getStatus())).count())));

        panel.add(header, statStrip, listScroller);
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
        refreshCustomerList();
        refreshDetailsPanel();
    }

    private void refreshCustomerList() {
        listScroller.removeAll();
        Map<Long, Long> bookingCounts = bookingCatalogService.bookingCountsByCustomer();

        List<BookingCustomer> customers = filteredCustomers();
        for (BookingCustomer customer : customers) {
            listScroller.add(buildCustomerRow(customer, bookingCounts.getOrDefault(customer.getId(), 0L)));
        }

        if (customers.isEmpty()) {
            listScroller.add(buildEmptyState());
        }
    }

    private Div buildEmptyState() {
        Div state = new Div();
        state.addClassName("sp-booking-list-empty");

        Span title = new Span("No customers match this search");
        title.addClassName("sp-booking-list-empty__title");

        Span text = new Span("Try a passenger name, email, title or location.");
        text.addClassName("sp-booking-list-empty__text");

        state.add(title, text);
        return state;
    }

    private Div buildCustomerRow(BookingCustomer customer, long bookings) {
        Div row = new Div();
        row.addClassName("sp-customer-row");
        if (customer.getId().equals(selectedCustomerId)) {
            row.addClassName("sp-customer-row--selected");
        }
        row.addClickListener(event -> navigateToCustomer(customer));

        Div userCell = new Div();
        userCell.addClassName("sp-customer-row__info");

        Avatar avatar = new Avatar(customer.getName());
        avatar.setColorIndex(customer.getColorIndex());
        avatar.addClassName("sp-customer-row__avatar");

        Div meta = new Div();
        meta.addClassName("sp-customer-row__meta");

        Div top = new Div();
        top.addClassName("sp-customer-row__topline");

        Span name = new Span(customer.getName());
        name.addClassName("sp-customer-row__name");

        if (customer.isVip()) {
            Span vip = new Span("VIP");
            vip.getElement().getThemeList().add("badge contrast pill");
            top.add(name, vip);
        } else {
            top.add(name);
        }

        Span title = new Span(customer.getTitle());
        title.addClassName("sp-customer-row__line");

        Span email = new Span(customer.getEmail());
        email.addClassName("sp-customer-row__line");

        meta.add(top, title, email);
        userCell.add(avatar, meta);

        Span statusBadge = new Span(customer.getStatus());
        statusBadge.getElement().getThemeList().add("badge " + ("Active".equals(customer.getStatus()) ? "success" : "contrast"));
        statusBadge.addClassName("sp-customer-row__status");

        Span bookingsCount = new Span(bookings + (bookings == 1 ? " trip" : " trips"));
        bookingsCount.addClassName("sp-customer-row__count");

        row.add(userCell, statusBadge, bookingsCount);
        return row;
    }

    private void refreshDetailsPanel() {
        BookingCustomer selectedCustomer = filteredCustomers().stream()
                .filter(customer -> customer.getId().equals(selectedCustomerId))
                .findFirst()
                .orElse(null);

        detailHost.removeAll();

        boolean hasSelection = selectedCustomer != null;
        bookingsContent.setClassName("sp-bookings-content--detail-hidden", !hasSelection);
        detailHost.setClassName("sp-bookings-content__detail-host--hidden", !hasSelection);

        if (!hasSelection) {
            return;
        }

        detailHost.add(buildCustomerDetails(selectedCustomer));
    }

    private Div buildCustomerDetails(BookingCustomer customer) {
        List<BookingTrip> trips = bookingCatalogService.findTripsForCustomer(customer.getId());
        BookingTrip nextTrip = trips.stream().findFirst().orElse(null);

        Div panel = new Div();
        panel.addClassName("sp-booking-details-panel");

        Div header = new Div();
        header.addClassName("sp-booking-details__header");

        Div titleBlock = new Div();
        titleBlock.addClassName("sp-booking-details__title-block");

        Span eyebrow = new Span("Customer profile");
        eyebrow.addClassName("sp-booking-details__eyebrow");

        H3 title = new H3(customer.getName());
        title.addClassName("sp-booking-details__title");

        Span meta = new Span(customer.getTitle() + " • " + customer.getLocation());
        meta.addClassName("sp-booking-details__meta");

        titleBlock.add(eyebrow, title, meta);

        Div actions = new Div();
        actions.addClassName("sp-booking-details__actions");

        Button messageBtn = new Button(VaadinIcon.CHAT.create(), event ->
                Notification.show("Messaging workflow is queued for the next pass.", 2500, Notification.Position.BOTTOM_START));
        messageBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);

        Button profileBtn = new Button("Open account", event ->
                Notification.show("Customer account editor comes next.", 2500, Notification.Position.BOTTOM_START));
        profileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        profileBtn.addClassName("sp-booking-details__primary-action");

        actions.add(messageBtn, profileBtn);
        header.add(titleBlock, actions);

        Div body = new Div();
        body.addClassName("sp-booking-details__body");

        body.add(buildCustomerHero(customer, nextTrip));

        Div metricStrip = new Div();
        metricStrip.addClassName("sp-booking-details__metric-strip");
        metricStrip.add(metricCard("Trips", String.valueOf(trips.size()), trips.isEmpty() ? "No active routes" : "Active portfolio"));
        metricStrip.add(metricCard("Next departure", nextTrip == null ? "Unscheduled" : DATE_FORMATTER.format(nextTrip.getDepartOn()),
                nextTrip == null ? "No route assigned" : nextTrip.getDestination()));
        metricStrip.add(metricCard("Status", customer.getStatus(), customer.isVip() ? "VIP account" : "Standard account"));

        Div contactCard = new Div();
        contactCard.addClassName("sp-booking-details__card");
        Span contactTitle = new Span("Contact");
        contactTitle.addClassName("sp-booking-details__card-title");
        Div contactList = new Div();
        contactList.addClassName("sp-booking-info-list");
        contactList.add(infoRow(VaadinIcon.ENVELOPE.create(), "Email", customer.getEmail()));
        contactList.add(infoRow(VaadinIcon.PHONE.create(), "Phone", customer.getPhone()));
        contactList.add(infoRow(VaadinIcon.MAP_MARKER.create(), "Home base", customer.getLocation()));
        contactCard.add(contactTitle, contactList);

        Div tripsCard = new Div();
        tripsCard.addClassName("sp-booking-details__card");
        Span tripsTitle = new Span("Upcoming routes");
        tripsTitle.addClassName("sp-booking-details__card-title");
        Div tripList = new Div();
        tripList.addClassName("sp-customer-trip-list");
        if (trips.isEmpty()) {
            Span emptyTrips = new Span("No upcoming itineraries assigned.");
            emptyTrips.addClassName("sp-booking-details__meta");
            tripList.add(emptyTrips);
        } else {
            trips.stream().limit(3).forEach(trip -> tripList.add(buildTripPreview(trip)));
        }
        tripsCard.add(tripsTitle, tripList);

        body.add(metricStrip, contactCard, tripsCard);
        panel.add(header, body);
        return panel;
    }

    private Div buildCustomerHero(BookingCustomer customer, BookingTrip nextTrip) {
        Div hero = new Div();
        hero.addClassName("sp-booking-details__card");
        hero.addClassName("sp-customer-details__hero");

        Div top = new Div();
        top.addClassName("sp-customer-details__hero-top");

        Avatar avatar = new Avatar(customer.getName());
        avatar.setColorIndex(customer.getColorIndex());
        avatar.addClassName("sp-customer-details__avatar");

        Div identity = new Div();
        identity.addClassName("sp-customer-details__identity");

        Span name = new Span(customer.getName());
        name.addClassName("sp-customer-details__name");

        Span title = new Span(customer.getTitle());
        title.addClassName("sp-booking-details__meta");

        identity.add(name, title);
        top.add(avatar, identity);

        Paragraph summary = new Paragraph(nextTrip == null
                ? "This account is onboarded and ready for itinerary assignment."
                : "Next touchpoint: " + nextTrip.getOrigin() + " to " + nextTrip.getDestination() + " on " + DATE_FORMATTER.format(nextTrip.getDepartOn()) + ".");
        summary.addClassName("sp-booking-details__summary");

        hero.add(top, summary);
        return hero;
    }

    private Div buildTripPreview(BookingTrip trip) {
        Div row = new Div();
        row.addClassName("sp-customer-trip-preview");

        Span route = new Span(trip.getOrigin() + " to " + trip.getDestination());
        route.addClassName("sp-customer-trip-preview__route");

        Span meta = new Span(DATE_FORMATTER.format(trip.getDepartOn()) + " • " + trip.getStatus());
        meta.addClassName("sp-customer-trip-preview__meta");

        row.add(route, meta);
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

    private List<BookingCustomer> filteredCustomers() {
        return bookingCatalogService.findAllCustomers().stream()
                .filter(customer -> matches(customer, searchTerm))
                .toList();
    }

    private boolean matches(BookingCustomer customer, String term) {
        if (term == null || term.isBlank()) {
            return true;
        }
        String haystack = String.join(" ",
                customer.getName(),
                customer.getEmail(),
                customer.getTitle(),
                customer.getStatus(),
                customer.getLocation()).toLowerCase(Locale.ROOT);
        return haystack.contains(term);
    }

    private void navigateToCustomer(BookingCustomer customer) {
        getUI().ifPresent(ui -> {
            if (customer.getId().equals(selectedCustomerId)) {
                ui.navigate("bookings/customers");
            } else {
                ui.navigate("bookings/customers/" + customer.getId());
            }
        });
    }
}
