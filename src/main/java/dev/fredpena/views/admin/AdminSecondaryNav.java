package dev.fredpena.views.admin;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.server.VaadinSession;
import dev.fredpena.admin.data.AdminSidebarData;

public class AdminSecondaryNav extends Div {

    private static final String FAVORITES_OPEN_KEY = "admin.favorites.open";
    private static final String FAVORITES_KIND_KEY = "admin.favorites.kind";
    private static final String SHOW_COUNTS_KEY = "admin.sidebar.showCounts";

    private final String active;
    private final Div nav = new Div();
    private final Div favList = new Div();
    private final Accordion favoritesAccordion = new Accordion();
    private final AccordionPanel favoritesPanel;
    private final Button settingsBtn = new Button(VaadinIcon.COG.create());
    private boolean restoringFavoritesState;

    public AdminSecondaryNav(String active, AdminSidebarData sidebarData) {
        this.active = active;
        addClassName("sp-secondary-sidebar");

        Div header = new Div();
        header.addClassName("sp-secondary-sidebar__header");
        Span title = new Span("Admin");
        title.addClassNames("sp-u-font-bold", "sp-u-text-md");
        settingsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
        configureSettingsPopover();
        header.add(title, settingsBtn);

        nav.addClassNames("sp-u-flex", "sp-u-flex-col", "sp-u-gap-xs");

        favList.addClassName("sp-favorites-list");
        favList.addClassNames("sp-u-flex", "sp-u-flex-col", "sp-u-gap-xs");

        favoritesAccordion.addClassName("sp-favorites-accordion");
        favoritesPanel = favoritesAccordion.add("Favorites", favList);
        favoritesPanel.addClassName("sp-favorites-panel");
        favoritesAccordion.addOpenedChangeListener(event -> {
            if (!restoringFavoritesState) {
                storeFavoritesState(event.getOpenedIndex().isPresent(), activeKind());
            }
        });
        restoreFavoritesState();

        add(header, nav, favoritesAccordion);
        refresh(sidebarData);
    }

    public void refresh(AdminSidebarData sidebarData) {
        nav.removeAll();
        nav.add(buildNavItem(VaadinIcon.USER.create(), "Users", String.valueOf(sidebarData.userCount()), "users".equals(active), "admin/users"));
        nav.add(buildNavItem(VaadinIcon.GLOBE.create(), "Planets", String.valueOf(sidebarData.planetCount()), "planets".equals(active), "admin/planets"));
        refreshCountVisibility();

        favList.removeAll();
        if (sidebarData.favoriteItems().isEmpty()) {
            Span emptyState = new Span("No favorites yet");
            emptyState.addClassName("sp-favorites-empty");
            favList.add(emptyState);
            return;
        }

        sidebarData.favoriteItems().forEach(item -> favList.add(buildFavItem(resolveFavoriteIcon(item.kind()), item.label(), item.href())));
    }

    private Icon resolveFavoriteIcon(String kind) {
        return "planet".equals(kind) ? VaadinIcon.GLOBE.create() : VaadinIcon.USER.create();
    }

    private Anchor buildNavItem(Icon icon, String label, String count, boolean active, String href) {
        Anchor anchor = new Anchor(href);
        anchor.addClassName("sp-secondary-nav-item");
        if (active) {
            anchor.addClassName("sp-secondary-nav-item--active");
        }

        Div left = new Div();
        left.addClassName("sp-secondary-nav-item__left");

        Div iconWrap = new Div();
        iconWrap.addClassName("sp-secondary-nav-item__icon");
        iconWrap.add(icon);
        left.add(iconWrap, new Span(label));

        Span countBadge = new Span(count);
        countBadge.addClassName("sp-count-badge");
        countBadge.setVisible(isShowCountsEnabled());

        anchor.add(left, countBadge);
        return anchor;
    }

    private Anchor buildFavItem(Icon icon, String label, String href) {
        Anchor anchor = new Anchor(href);
        anchor.addClassName("sp-fav-item");

        Div iconWrap = new Div();
        iconWrap.addClassName("sp-fav-item__icon");
        iconWrap.add(icon);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames("sp-u-text-sm");

        anchor.add(iconWrap, labelSpan);
        return anchor;
    }

    private void restoreFavoritesState() {
        VaadinSession session = VaadinSession.getCurrent();
        Boolean open = (Boolean) session.getAttribute(FAVORITES_OPEN_KEY);
        String kind = (String) session.getAttribute(FAVORITES_KIND_KEY);
        restoringFavoritesState = true;
        try {
            if (Boolean.TRUE.equals(open) && activeKind().equals(kind)) {
                favoritesAccordion.open(0);
            } else {
                favoritesAccordion.close();
            }
        } finally {
            restoringFavoritesState = false;
        }
    }

    private void storeFavoritesState(boolean open, String kind) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(FAVORITES_OPEN_KEY, open);
        session.setAttribute(FAVORITES_KIND_KEY, kind);
    }

    private void configureSettingsPopover() {
        settingsBtn.getElement().setProperty("title", "Sidebar preferences");

        Popover popover = new Popover();
        popover.setTarget(settingsBtn);
        popover.setOpenOnClick(true);
        popover.addClassName("sp-overflow-popover");

        Div content = new Div();
        content.addClassNames("sp-u-flex", "sp-u-flex-col");
        content.addClassName("sp-sidebar-settings");

        Div header = new Div();
        header.addClassName("sp-sidebar-settings__header");
        Span title = new Span("Sidebar Preferences");
        title.addClassName("sp-sidebar-settings__title");
        Span subtitle = new Span("Control visibility and quick actions for this section.");
        subtitle.addClassName("sp-sidebar-settings__subtitle");
        header.add(title, subtitle);

        Div preferencesSection = new Div();
        preferencesSection.addClassName("sp-sidebar-settings__section");

        Div countsRow = new Div();
        countsRow.addClassName("sp-sidebar-settings__toggle-row");

        Checkbox showCounts = new Checkbox("Show counts");
        showCounts.addClassName("sp-sidebar-settings__checkbox");
        showCounts.setValue(isShowCountsEnabled());
        showCounts.addValueChangeListener(event -> {
            storeShowCounts(event.getValue());
            refreshCountVisibility();
        });
        Div countsCopy = new Div();
        countsCopy.addClassName("sp-sidebar-settings__copy");
        Span countsTitle = new Span("Show counters");
        countsTitle.addClassName("sp-sidebar-settings__row-title");
        Span countsMeta = new Span("Display item totals next to Users and Planets.");
        countsMeta.addClassName("sp-sidebar-settings__row-meta");
        countsCopy.add(countsTitle, countsMeta);
        countsRow.add(countsCopy, showCounts);
        preferencesSection.add(countsRow);

        Div actionsSection = new Div();
        actionsSection.addClassName("sp-sidebar-settings__section");

        Span actionsLabel = new Span("Quick Actions");
        actionsLabel.addClassName("sp-sidebar-settings__section-label");

        Button collapseFavorites = new Button("Collapse favorites", VaadinIcon.ANGLE_UP.create());
        collapseFavorites.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        collapseFavorites.addClassName("sp-overflow-action");
        collapseFavorites.addClickListener(event -> {
            favoritesAccordion.close();
            storeFavoritesState(false, activeKind());
            popover.close();
        });

        Button manageFavorites = new Button("Manage favorites", VaadinIcon.STAR.create());
        manageFavorites.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        manageFavorites.addClassName("sp-overflow-action");
        manageFavorites.addClickListener(event -> {
            storeFavoritesState(true, activeKind());
            getUI().ifPresent(ui -> ui.navigate(activeRoute()));
            popover.close();
        });

        actionsSection.add(actionsLabel, collapseFavorites, manageFavorites);

        content.add(header, preferencesSection, actionsSection);
        popover.add(content);
    }

    private boolean isShowCountsEnabled() {
        Object value = VaadinSession.getCurrent().getAttribute(SHOW_COUNTS_KEY);
        return value == null || Boolean.TRUE.equals(value);
    }

    private void storeShowCounts(boolean enabled) {
        VaadinSession.getCurrent().setAttribute(SHOW_COUNTS_KEY, enabled);
    }

    private void refreshCountVisibility() {
        refreshCountVisibility(nav);
    }

    private void refreshCountVisibility(Div container) {
        boolean visible = isShowCountsEnabled();
        container.getChildren().filter(Anchor.class::isInstance).map(Anchor.class::cast).forEach(anchor -> anchor.getChildren().filter(child -> child instanceof Span && child.getClassNames().contains("sp-count-badge")).forEach(child -> child.setVisible(visible)));
    }

    private String activeKind() {
        return "planets".equals(active) ? "planet" : "user";
    }

    private String activeRoute() {
        return "planets".equals(active) ? "admin/planets" : "admin/users";
    }
}
