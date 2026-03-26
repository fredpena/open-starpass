package dev.fredpena.views.bookings;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.popover.Popover;
import dev.fredpena.bookings.data.BookingCatalogService;
import dev.fredpena.bookings.data.BookingSidebarData;

public class BookingSecondaryNav extends Div {

    public BookingSecondaryNav(BookingCatalogService bookingCatalogService, String active) {
        BookingSidebarData sidebarData = bookingCatalogService.getSidebarData();

        addClassName("sp-secondary-sidebar");
        addClassName("sp-booking-sidebar");

        Div header = new Div();
        header.addClassName("sp-secondary-sidebar__header");

        Div titleBlock = new Div();
        titleBlock.addClassName("sp-booking-sidebar__title-block");

        Span eyebrow = new Span("Mission board");
        eyebrow.addClassName("sp-booking-sidebar__eyebrow");

        Span title = new Span("Bookings");
        title.addClassNames("sp-u-font-bold", "sp-u-text-md");

        titleBlock.add(eyebrow, title);

        Button quickActionsBtn = new Button(VaadinIcon.PLUS.create());
        quickActionsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        quickActionsBtn.addClassName("sp-booking-sidebar__actions-trigger");
        configureQuickActions(quickActionsBtn);

        header.add(titleBlock, quickActionsBtn);

        Div heroCard = new Div();
        heroCard.addClassName("sp-booking-sidebar__hero-card");
        heroCard.add(metric("Upcoming routes", String.valueOf(sidebarData.upcomingCount())));
        heroCard.add(metric("Confirmed", String.valueOf(sidebarData.confirmedCount())));
        heroCard.add(metric("VIP customers", String.valueOf(sidebarData.vipCustomerCount())));

        Div nav = new Div();
        nav.addClassNames("sp-u-flex", "sp-u-flex-col", "sp-u-gap-xs");
        nav.add(buildNavItem("Upcoming", "All live itineraries and boarding windows", String.valueOf(sidebarData.upcomingCount()),
                VaadinIcon.CALENDAR_CLOCK.create(), "upcoming".equals(active), "bookings/upcoming"));
        nav.add(buildNavItem("Customers", "Passenger profiles and travel relationships", String.valueOf(sidebarData.customerCount()),
                VaadinIcon.USERS.create(), "customers".equals(active), "bookings/customers"));

        add(header, heroCard, nav);
    }

    private void configureQuickActions(Button trigger) {
        Popover popover = new Popover();
        popover.setTarget(trigger);
        popover.setModal(true);
        popover.setOpenOnClick(true);
        popover.addClassName("sp-overflow-popover");

        Div content = new Div();
        content.addClassName("sp-booking-sidebar__popover");

        Span title = new Span("Quick actions");
        title.addClassName("sp-booking-sidebar__popover-title");

        Button newBooking = sidebarAction("Create booking", VaadinIcon.CALENDAR_USER.create(),
                "Booking creation flow is the next step for this module.");
        Button addCustomer = sidebarAction("Add customer", VaadinIcon.USER_CARD.create(),
                "Customer onboarding flow is staged after the list/detail pass.");

        content.add(title, newBooking, addCustomer);
        popover.add(content);
    }

    private Button sidebarAction(String label, Icon icon, String message) {
        Button button = new Button(label, icon, event -> Notification.show(message, 2500, Notification.Position.BOTTOM_START));
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        button.addClassName("sp-overflow-action");
        button.setWidthFull();
        return button;
    }

    private Div metric(String label, String value) {
        Div metric = new Div();
        metric.addClassName("sp-booking-sidebar__metric");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("sp-booking-sidebar__metric-value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-booking-sidebar__metric-label");

        metric.add(valueSpan, labelSpan);
        return metric;
    }

    private Anchor buildNavItem(String label, String description, String count, Icon icon, boolean active, String href) {
        Anchor anchor = new Anchor(href);
        anchor.addClassName("sp-secondary-nav-item");
        anchor.addClassName("sp-booking-nav-item");
        if (active) {
            anchor.addClassName("sp-secondary-nav-item--active");
        }

        Div left = new Div();
        left.addClassName("sp-secondary-nav-item__left");

        Div iconWrap = new Div();
        iconWrap.addClassName("sp-secondary-nav-item__icon");
        iconWrap.add(icon);

        Div textBlock = new Div();
        textBlock.addClassName("sp-booking-nav-item__text");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-booking-nav-item__label");

        Span descriptionSpan = new Span(description);
        descriptionSpan.addClassName("sp-booking-nav-item__description");

        textBlock.add(labelSpan, descriptionSpan);
        left.add(iconWrap, textBlock);

        Span countBadge = new Span(count);
        countBadge.addClassName("sp-count-badge");

        anchor.add(left, countBadge);
        return anchor;
    }
}
