package dev.fredpena.views.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.VaadinSession;

public class PrimarySidebar extends Div {

    private static final String PRIMARY_SIDEBAR_COMPACT_KEY = "primary.sidebar.compact";

    private final Button collapseBtn = new Button();
    private boolean compact;

    public PrimarySidebar(String section) {
        compact = Boolean.TRUE.equals(VaadinSession.getCurrent().getAttribute(PRIMARY_SIDEBAR_COMPACT_KEY));
        addClassName("sp-primary-sidebar");
        add(buildBrand(), buildSearch(), buildNav(section), buildFooter());
        syncCompactState();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        applyShellCompactState();
    }

    private Div buildBrand() {
        Div brand = new Div();
        brand.addClassName("sp-brand");

        Div logo = new Div(VaadinIcon.ROCKET.create());
        logo.addClassName("sp-primary-sidebar__logo");

        Span name = new Span("StarPass");
        name.addClassNames("sp-u-font-bold", "sp-u-text-md");
        name.setVisible(!compact);

        collapseBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        collapseBtn.addClassName("sp-primary-sidebar__collapse");
        collapseBtn.addClickListener(event -> {
            name.setVisible(compact);
            compact = !compact;
            VaadinSession.getCurrent().setAttribute(PRIMARY_SIDEBAR_COMPACT_KEY, compact);
            syncCompactState();
            applyShellCompactState();
        });

        brand.add(logo, name, collapseBtn);
        return brand;
    }

    private Div buildSearch() {
        Div search = new Div();
        search.addClassName("sp-primary-sidebar__search");

        Span icon = new Span(VaadinIcon.SEARCH.create());
        icon.addClassName("sp-primary-sidebar__search-icon");

        Span text = new Span("Search");
        text.addClassName("sp-primary-sidebar__search-text");

        Span shortcut = new Span("⌘K");
        shortcut.addClassName("sp-primary-sidebar__shortcut");

        search.add(icon, text, shortcut);
        return search;
    }

    private Div buildNav(String section) {
        Div nav = new Div();
        nav.addClassNames("sp-u-flex", "sp-u-flex-col", "sp-u-gap-xs", "sp-u-flex-1");

        nav.add(buildNavItem(VaadinIcon.GLOBE.create(), "Bookings", "bookings/upcoming", "bookings".equals(section)));
        nav.add(buildNavItem(VaadinIcon.COG.create(), "Admin", "admin/users", "admin".equals(section)));

        return nav;
    }

    private Anchor buildNavItem(Icon icon, String label, String href, boolean active) {
        Anchor anchor = new Anchor(href);
        anchor.addClassName("sp-nav-item");
        if (active) anchor.addClassName("sp-nav-item--active");

        Div iconWrap = new Div();
        iconWrap.addClassName("sp-nav-item__icon");
        iconWrap.add(icon);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames("sp-u-text-sm", "sp-u-font-medium");

        anchor.add(iconWrap, labelSpan);
        return anchor;
    }

    private Div buildFooter() {
        Div footer = new Div();
        footer.addClassName("sp-primary-sidebar__footer");

        Anchor sourceLink = new Anchor("https://github.com/fredpena/open-starpass");
        sourceLink.setTarget("_blank");
        sourceLink.addClassNames("sp-nav-item", "sp-primary-sidebar__footer-link");

        Div sourceIconWrap = new Div();
        sourceIconWrap.addClassName("sp-nav-item__icon");
        sourceIconWrap.add(VaadinIcon.CODE.create());

        Span sourceLabel = new Span("Source code");
        sourceLabel.addClassNames("sp-u-text-sm");

        sourceLink.add(sourceIconWrap, sourceLabel);

        Anchor notifLink = new Anchor("#");
        notifLink.addClassNames("sp-nav-item", "sp-primary-sidebar__footer-link");

        Div notifIconWrap = new Div();
        notifIconWrap.addClassName("sp-nav-item__icon");
        notifIconWrap.add(VaadinIcon.BELL.create());

        Span notifLabel = new Span("Notifications");
        notifLabel.addClassNames("sp-u-text-sm");

        notifLink.add(notifIconWrap, notifLabel);

        Anchor profileCard = new Anchor("#");
        profileCard.addClassName("sp-primary-sidebar__profile");

        Div avatarWrap = new Div();
        avatarWrap.addClassName("sp-primary-sidebar__avatar");

        Span initials = new Span("FP");
        avatarWrap.add(initials);

        Div statusDot = new Div();
        statusDot.addClassName("sp-primary-sidebar__status-dot");
        avatarWrap.add(statusDot);

        Div meta = new Div();
        meta.addClassName("sp-primary-sidebar__profile-meta");

        Span userName = new Span("Fred Peña");
        userName.addClassName("sp-primary-sidebar__profile-name");

        Span userRole = new Span("Workspace owner");
        userRole.addClassName("sp-primary-sidebar__profile-role");

        meta.add(userName, userRole);

        profileCard.add(avatarWrap, meta);

        footer.add(sourceLink, notifLink, profileCard);
        return footer;
    }

    private void syncCompactState() {
        if (compact) {
            addClassName("sp-primary-sidebar--compact");
            collapseBtn.setIcon(VaadinIcon.ANGLE_RIGHT.create());
            collapseBtn.getElement().setProperty("title", "Expand sidebar");
        } else {
            removeClassName("sp-primary-sidebar--compact");
            collapseBtn.setIcon(VaadinIcon.ANGLE_LEFT.create());
            collapseBtn.getElement().setProperty("title", "Collapse sidebar");
        }
    }

    private void applyShellCompactState() {
        getElement().executeJs("""
            const shell = this.closest('.sp-shell');
            if (!shell) {
              return;
            }
            shell.classList.toggle('sp-shell--primary-compact', $0);
            shell.style.setProperty('--sp-primary-sidebar-w', $0 ? '84px' : '190px');
            """, compact);
    }
}
