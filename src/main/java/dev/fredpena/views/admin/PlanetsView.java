package dev.fredpena.views.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.fredpena.admin.data.AdminCatalogService;
import dev.fredpena.admin.data.AdminPlanet;
import dev.fredpena.views.MainLayout;
import dev.fredpena.views.components.PrimarySidebar;
import dev.fredpena.views.components.SelectionToolbar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@PageTitle("Admin - Planets")
@Route(value = "admin/planets/:planetId?", layout = MainLayout.class)
@AnonymousAllowed
public class PlanetsView extends Div implements BeforeEnterObserver {

    private static final String ROUTE_PLANET_ID = "planetId";

    private final AdminCatalogService adminCatalogService;
    private final AdminSecondaryNav adminSecondaryNav;
    private final Grid<AdminPlanet> planetsGrid = new Grid<>();
    private final Div adminContent = new Div();
    private final Div detailsHost = new Div();
    private final Checkbox selectAllCheckbox = new Checkbox();
    private final TextField searchField = new TextField();
    private final SelectionToolbar selectionToolbar = new SelectionToolbar("planet", "planets", this::clearSelection);
    private final List<AdminPlanet> currentPlanets = new ArrayList<>();
    private final ListDataProvider<AdminPlanet> planetsDataProvider = new ListDataProvider<>(currentPlanets);
    private final LinkedHashSet<Long> checkedPlanetIds = new LinkedHashSet<>();
    private Long selectedPlanetId;

    public PlanetsView(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
        this.adminSecondaryNav = new AdminSecondaryNav("planets", adminCatalogService.getSidebarData());

        addClassNames("sp-shell", "sp-shell--admin");
        setSizeFull();

        add(new PrimarySidebar("admin"));
        add(adminSecondaryNav);

        adminContent.addClassName("sp-admin-content");
        adminContent.add(buildPlanetsPanel());

        detailsHost.setSizeFull();
        detailsHost.addClassNames("sp-admin-content__detail-host", "sp-planets-detail-host");
        adminContent.add(detailsHost);

        add(adminContent);
        refreshPlanetsGrid();
        refreshDetailsPanel();
    }

    private Div buildPlanetsPanel() {
        Div panel = new Div();
        panel.addClassName("sp-planets-panel");

        Div header = new Div();
        header.addClassName("sp-panel-header");

        H2 title = new H2("Planets");
        title.addClassNames("sp-u-text-lg", "sp-u-font-bold", "sp-u-m-0");

        Button addBtn = new Button(VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        addBtn.addClickListener(event -> openPlanetDialog(null));
        header.add(title, addBtn);

        Div toolbar = new Div();
        toolbar.addClassName("sp-users-panel__toolbar");

        searchField.setPlaceholder("Search planets");
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassNames("sp-inline-search", "sp-planets-search");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> applySearchPredicate());
        toolbar.add(searchField);

        selectionToolbar.setActions(List.of(
                new SelectionToolbar.ActionConfig("Favorite", VaadinIcon.STAR, null, () -> setSelectedFavorite(true)),
                new SelectionToolbar.ActionConfig("Unfavorite", VaadinIcon.STAR_O, null, () -> setSelectedFavorite(false)),
                new SelectionToolbar.ActionConfig("", VaadinIcon.TRASH, "sp-selection-toolbar__action--danger", this::deleteSelectedPlanets)
        ));

        configurePlanetsGrid();

        panel.add(header, selectionToolbar, toolbar, planetsGrid);
        return panel;
    }

    private void configurePlanetsGrid() {
        planetsGrid.addClassName("sp-admin-grid");
        planetsGrid.setDataProvider(planetsDataProvider);
        planetsGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        planetsGrid.setSelectionMode(Grid.SelectionMode.NONE);
        planetsGrid.setAllRowsVisible(false);
        planetsGrid.setHeightFull();
        planetsGrid.setEmptyStateText("No planets found.");
        planetsGrid.setPartNameGenerator(planet ->
                planet.getId() != null && planet.getId().equals(selectedPlanetId) ? "detail-active" : null);
        planetsGrid.addItemClickListener(event -> selectPlanet(event.getItem().getId(), true));

        planetsGrid.addColumn(new ComponentRenderer<>(this::buildSelectionCheckbox))
                .setHeader(configureSelectionHeaderCheckbox())
                .setWidth("64px")
                .setFlexGrow(0)
                .setSortable(false)
                .setAutoWidth(false);
        planetsGrid.addColumn(new ComponentRenderer<>(this::buildPlanetNameCell))
                .setHeader("Name")
                .setComparator(AdminPlanet::getName)
                .setSortable(true)
                .setWidth("220px")
                .setFlexGrow(0);
        planetsGrid.addColumn(new ComponentRenderer<>(this::buildPlanetClimateCell))
                .setHeader("Climate")
                .setComparator(AdminPlanet::getClimate)
                .setSortable(true)
                .setFlexGrow(1);
        planetsGrid.addColumn(new ComponentRenderer<>(this::buildPlanetGravityCell))
                .setHeader("Gravity")
                .setComparator(AdminPlanet::getGravityPct)
                .setSortable(true)
                .setWidth("170px")
                .setFlexGrow(0);
        planetsGrid.addColumn(new ComponentRenderer<>(this::buildPlanetDistanceCell))
                .setHeader("Distance")
                .setComparator(AdminPlanet::getDistanceLabel)
                .setSortable(true)
                .setWidth("120px")
                .setFlexGrow(0);
    }

    private void refreshPlanetsGrid() {
        List<AdminPlanet> fetchedPlanets = adminCatalogService.findAllPlanets();
        currentPlanets.clear();
        currentPlanets.addAll(fetchedPlanets);
        checkedPlanetIds.retainAll(currentPlanets.stream().map(AdminPlanet::getId).collect(Collectors.toSet()));
        applySearchPredicate();
        planetsDataProvider.refreshAll();

        syncSelectionWithVisiblePlanets();

        updateSelectionToolbar();
        adminSecondaryNav.refresh(adminCatalogService.getSidebarData());
        planetsGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private void applySearchPredicate() {
        planetsDataProvider.clearFilters();
        planetsDataProvider.setFilter(buildSearchPredicate(searchField.getValue()));
        syncSelectionWithVisiblePlanets();
        planetsGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private SerializablePredicate<AdminPlanet> buildSearchPredicate(String query) {
        if (query == null || query.isBlank()) {
            return planet -> true;
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return planet -> planet.getName().toLowerCase(Locale.ROOT).contains(normalized)
                || planet.getClimate().toLowerCase(Locale.ROOT).contains(normalized)
                || planet.getDistanceLabel().toLowerCase(Locale.ROOT).contains(normalized)
                || planet.getSummary().toLowerCase(Locale.ROOT).contains(normalized);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long routePlanetId = event.getRouteParameters()
                .get(ROUTE_PLANET_ID)
                .flatMap(this::parsePlanetId)
                .orElse(null);

        if (routePlanetId != null && !searchField.isEmpty()) {
            searchField.clear();
            applySearchPredicate();
        }

        selectedPlanetId = routePlanetId;
        syncSelectionWithVisiblePlanets();
        planetsGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private void selectPlanet(Long planetId, boolean updateRoute) {
        if (planetId == null || planetId.equals(selectedPlanetId)) {
            selectedPlanetId = null;
            if (updateRoute) {
                getUI().ifPresent(ui -> ui.navigate("admin/planets"));
            }
        } else {
            selectedPlanetId = planetId;
            if (updateRoute) {
                getUI().ifPresent(ui -> ui.navigate(PlanetsView.class,
                        new RouteParameters(ROUTE_PLANET_ID, planetId.toString())));
            }
        }
        planetsGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private void refreshDetailsPanel() {
        detailsHost.removeAll();
        AdminPlanet selectedPlanet = getSelectedPlanet();
        if (selectedPlanet != null) {
            adminContent.removeClassName("sp-admin-content--detail-hidden");
            detailsHost.removeClassName("sp-admin-content__detail-host--hidden");
            detailsHost.add(buildPlanetDetails(selectedPlanet));
        } else {
            adminContent.addClassName("sp-admin-content--detail-hidden");
            detailsHost.addClassName("sp-admin-content__detail-host--hidden");
        }
    }

    private AdminPlanet getSelectedPlanet() {
        if (selectedPlanetId == null) {
            return null;
        }
        return adminCatalogService.findPlanet(selectedPlanetId).orElse(null);
    }

    private List<AdminPlanet> visiblePlanets() {
        SerializablePredicate<AdminPlanet> predicate = buildSearchPredicate(searchField.getValue());
        return currentPlanets.stream().filter(predicate::test).toList();
    }

    private void syncSelectionWithVisiblePlanets() {
        List<AdminPlanet> visiblePlanets = visiblePlanets();
        if (selectedPlanetId != null && visiblePlanets.stream().noneMatch(planet -> planet.getId().equals(selectedPlanetId))) {
            selectedPlanetId = null;
        }
    }

    private void updateSelectionToolbar() {
        selectionToolbar.setCount(checkedPlanetIds.size());
        syncHeaderCheckbox();
    }

    private Checkbox configureSelectionHeaderCheckbox() {
        selectAllCheckbox.addClassName("sp-grid-select-checkbox");
        stopClickPropagation(selectAllCheckbox);
        selectAllCheckbox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                toggleAllVisiblePlanets(event.getValue());
            }
        });
        syncHeaderCheckbox();
        return selectAllCheckbox;
    }

    private Checkbox buildSelectionCheckbox(AdminPlanet planet) {
        Checkbox checkbox = new Checkbox();
        checkbox.addClassName("sp-grid-select-checkbox");
        stopClickPropagation(checkbox);
        checkbox.setValue(planet.getId() != null && checkedPlanetIds.contains(planet.getId()));
        checkbox.addValueChangeListener(event -> {
            if (!event.isFromClient() || planet.getId() == null) {
                return;
            }
            if (event.getValue()) {
                checkedPlanetIds.add(planet.getId());
            } else {
                checkedPlanetIds.remove(planet.getId());
            }
            updateSelectionToolbar();
            planetsGrid.getDataProvider().refreshAll();
        });
        return checkbox;
    }

    private void toggleAllVisiblePlanets(boolean checked) {
        List<Long> visibleIds = visiblePlanets().stream().map(AdminPlanet::getId).toList();
        if (checked) {
            checkedPlanetIds.addAll(visibleIds);
        } else {
            checkedPlanetIds.removeAll(visibleIds);
        }
        updateSelectionToolbar();
        planetsGrid.getDataProvider().refreshAll();
    }

    private void syncHeaderCheckbox() {
        List<Long> visibleIds = visiblePlanets().stream().map(AdminPlanet::getId).toList();
        boolean allVisibleChecked = !visibleIds.isEmpty() && visibleIds.stream().allMatch(checkedPlanetIds::contains);
        selectAllCheckbox.setValue(allVisibleChecked);
    }

    private void stopClickPropagation(Checkbox checkbox) {
        checkbox.getElement().executeJs(
                "this.addEventListener('click', e => e.stopPropagation());" +
                        "this.addEventListener('pointerdown', e => e.stopPropagation());"
        );
    }

    private Optional<Long> parsePlanetId(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException _) {
            return Optional.empty();
        }
    }

    private Div buildPlanetNameCell(AdminPlanet planet) {
        Div cell = new Div();
        cell.addClassNames("sp-planet-row", "sp-planet-row--name-cell");

        Div nameCell = new Div();
        nameCell.addClassNames("sp-planet-row__name", "sp-u-flex", "sp-u-align-center", "sp-u-gap-sm");

        Div avatar = new Div();
        avatar.addClassName("sp-planet-row__avatar");
        avatar.getStyle().set("background-color", planet.getColorHex());
        avatar.setText(buildPlanetInitial(planet.getName()));

        Span name = new Span(planet.getName());
        name.addClassNames("sp-u-font-semibold");

        if (planet.isFavorite()) {
            Icon favoriteIcon = VaadinIcon.STAR.create();
            favoriteIcon.addClassName("sp-user-row__favorite-icon");
            nameCell.add(avatar, name, favoriteIcon);
        } else {
            nameCell.add(avatar, name);
        }

        cell.add(nameCell);
        return cell;
    }

    private Div buildPlanetClimateCell(AdminPlanet planet) {
        Div cell = new Div();
        cell.addClassNames("sp-planet-row", "sp-planet-row--climate-cell");

        Span climate = new Span(planet.getClimate());
        climate.addClassNames("sp-planet-row__climate", "sp-u-text-sm");
        cell.add(climate);
        return cell;
    }

    private Div buildPlanetGravityCell(AdminPlanet planet) {
        Div gravityBlock = new Div();
        gravityBlock.addClassNames("sp-planet-row__gravity-block", "sp-u-flex", "sp-u-flex-col", "sp-u-gap-xs", "sp-u-w-full");

        Span value = new Span(planet.getGravityPct() + "%");
        value.addClassNames("sp-planet-row__gravity-value", "sp-u-text-sm", "sp-u-font-semibold");
        gravityBlock.add(value, buildProgressBar(Math.min(planet.getGravityPct(), 100)));

        Div cell = new Div();
        cell.addClassNames("sp-planet-row", "sp-planet-row--gravity-cell");
        cell.add(gravityBlock);
        return cell;
    }

    private Div buildPlanetDistanceCell(AdminPlanet planet) {
        Div cell = new Div();
        cell.addClassNames("sp-planet-row", "sp-planet-row--distance-cell");
        Span distance = new Span(planet.getDistanceLabel());
        distance.addClassName("sp-planet-row__distance");
        cell.add(distance);
        return cell;
    }

    static Div buildProgressBar(int pct) {
        Div track = new Div();
        track.addClassName("sp-progress");

        Div fill = new Div();
        fill.addClassName("sp-progress__fill");
        if (pct > 80) {
            fill.addClassName("sp-progress__fill--dark");
        }
        fill.getStyle().set("width", pct + "%");

        track.add(fill);
        return track;
    }

    private Div buildPlanetDetails(AdminPlanet planet) {
        Div panel = new Div();
        panel.addClassNames("sp-planet-details-panel", "sp-u-flex", "sp-u-flex-col");

        Div header = new Div();
        header.addClassName("sp-panel-header");

        Span detailTitle = new Span("Planet Details");
        detailTitle.addClassNames("sp-u-font-bold", "sp-u-text-md");

        Div headerBtns = new Div();
        headerBtns.addClassNames("sp-u-flex", "sp-u-align-center", "sp-u-gap-xs");

        Button favBtn = new Button(VaadinIcon.STAR.create());
        favBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        if (planet.isFavorite()) {
            favBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            favBtn.addClassName("sp-favorite-toggle--active");
        }
        favBtn.addClickListener(event -> {
            adminCatalogService.togglePlanetFavorite(planet.getId());
            refreshPlanetsGrid();
        });

        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        editBtn.addClickListener(event -> openPlanetDialog(planet));

        Button moreBtn = new Button(VaadinIcon.ELLIPSIS_DOTS_H.create());
        moreBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        moreBtn.getElement().setProperty("title", "More actions");

        Popover morePopover = new Popover();
        morePopover.setTarget(moreBtn);
        morePopover.setOpenOnClick(true);
        morePopover.addClassName("sp-overflow-popover");

        Button deleteAction = new Button("Delete planet", VaadinIcon.TRASH.create());
        deleteAction.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteAction.addClassNames("sp-overflow-action", "sp-overflow-action--danger");
        deleteAction.addClickListener(e -> {
            confirmDeletePlanet(planet);
            morePopover.close();
        });

        morePopover.add(deleteAction);

        headerBtns.add(favBtn, editBtn, moreBtn);
        header.add(detailTitle, headerBtns);

        Div cover = new Div();
        cover.addClassName("sp-planet-cover");
        cover.getStyle().set("--sp-planet-color", planet.getColorHex());

        Div content = new Div();
        content.addClassNames("sp-u-flex-1", "sp-u-overflow-auto", "sp-u-flex", "sp-u-flex-col");

        Div hero = new Div();
        hero.addClassName("sp-planet-details__hero");

        Div planetAvatar = new Div();
        planetAvatar.addClassName("sp-planet-avatar");
        planetAvatar.getStyle().set("background-color", planet.getColorHex());
        planetAvatar.setText(buildPlanetInitial(planet.getName()));

        Div heroBody = new Div();
        heroBody.addClassNames("sp-planet-details__hero-body", "sp-u-flex", "sp-u-flex-col", "sp-u-gap-sm");

        Span eyebrow = new Span("Planet Profile");
        eyebrow.addClassName("sp-planet-details__eyebrow");

        Div headline = new Div();
        headline.addClassName("sp-planet-details__headline");

        H2 planetName = new H2(planet.getName());
        planetName.addClassNames("sp-u-text-2xl", "sp-u-font-extrabold", "sp-u-m-0");

        Span summary = new Span(planet.getSummary());
        summary.addClassName("sp-planet-details__summary");

        Span climateBadge = new Span(planet.getClimate());
        climateBadge.addClassName("sp-planet-details__climate-badge");

        headline.add(planetName, climateBadge, summary);

        heroBody.add(eyebrow, headline);
        hero.add(planetAvatar, heroBody);

        Div metricStrip = new Div();
        metricStrip.addClassName("sp-planet-details__metric-strip");
        metricStrip.add(
                buildPlanetMetricCard("Distance", planet.getDistanceLabel(), "From Earth"),
                buildPlanetMetricCard("Gravity", planet.getGravityPct() + "%", "Earth baseline"),
                buildPlanetMetricCard("Favorite", planet.isFavorite() ? "Pinned" : "Normal", "Sidebar state")
        );

        Div insightsCard = new Div();
        insightsCard.addClassName("sp-planet-details__card");
        H4 insightsTitle = new H4("Surface Insights");
        insightsTitle.addClassNames("sp-u-text-md", "sp-u-font-bold", "sp-u-m-0");

        Div infoList = new Div();
        infoList.addClassNames("sp-u-flex", "sp-u-flex-col", "sp-u-gap-md");
        infoList.add(buildPlanetInfoItem(VaadinIcon.GLOBE.create(), "Distance from Earth:", planet.getDistanceLabel(), null, 0));
        infoList.add(buildPlanetInfoItem(VaadinIcon.DASHBOARD.create(), "Gravity:", planet.getGravityPct() + "% of Earth's gravity", null, planet.getGravityPct()));
        infoList.add(buildPlanetInfoItem(VaadinIcon.CLOUD.create(), "Climate profile:", planet.getClimate(), null, 0));
        insightsCard.add(insightsTitle, infoList);

        Div notesCard = new Div();
        notesCard.addClassName("sp-planet-details__card");
        H4 notesTitle = new H4("Operational Notes");
        notesTitle.addClassNames("sp-u-text-md", "sp-u-font-bold", "sp-u-m-0");
        Span notesText = new Span(planet.getSummary());
        notesText.addClassName("sp-planet-details__notes");
        notesCard.add(notesTitle, notesText);

        Div body = new Div();
        body.addClassName("sp-planet-details__body");
        body.add(hero, metricStrip, insightsCard, notesCard);

        content.add(body);

        Div footer = new Div();
        footer.addClassName("sp-planet-details__footer");

        Button createBtn = new Button("Create booking");
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClassNames("sp-u-w-full", "sp-planet-details__primary-action");
        createBtn.addClickListener(event -> {
            getUI().ifPresent(ui -> ui.navigate("bookings/upcoming"));
            Notification.show("Booking flow opened for " + planet.getName());
        });
        footer.add(createBtn);

        panel.add(header, cover, content, footer);
        return panel;
    }

    private Div buildPlanetInfoItem(Icon icon, String label, String value, String sub, int progressPct) {
        Div item = new Div();
        item.addClassName("sp-planet-info-item");

        Div iconBox = new Div();
        iconBox.addClassName("sp-planet-info-item__icon");
        iconBox.add(icon);

        Div textBlock = new Div();
        textBlock.addClassNames("sp-u-flex", "sp-u-flex-col", "sp-u-gap-xs", "sp-u-flex-1");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-planet-info-item__label");
        textBlock.add(labelSpan);

        if (value != null) {
            Span valueSpan = new Span(value);
            valueSpan.addClassName("sp-planet-info-item__value");
            textBlock.add(valueSpan);
        }

        if (progressPct > 0) {
            textBlock.add(buildProgressBar(Math.min(progressPct, 100)));
            if (sub != null) {
                Span subSpan = new Span(sub);
                subSpan.addClassName("sp-planet-info-item__sub");
                textBlock.add(subSpan);
            }
        }

        item.add(iconBox, textBlock);
        return item;
    }

    private Div buildPlanetMetricCard(String label, String value, String meta) {
        Div card = new Div();
        card.addClassName("sp-planet-metric-card");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-planet-metric-card__label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("sp-planet-metric-card__value");

        Span metaSpan = new Span(meta);
        metaSpan.addClassName("sp-planet-metric-card__meta");

        card.add(labelSpan, valueSpan, metaSpan);
        return card;
    }

    private void openPlanetDialog(AdminPlanet existingPlanet) {
        boolean editing = existingPlanet != null;
        AdminPlanet source = editing ? existingPlanet : buildDraftPlanet();
        Binder<AdminPlanet> binder = new Binder<>(AdminPlanet.class);

        Dialog dialog = new Dialog();
        dialog.addClassName("sp-user-dialog");
        dialog.setHeaderTitle(editing ? "Edit Planet" : "Add Planet");
        dialog.setWidth("640px");

        Div hero = new Div();
        hero.addClassName("sp-user-dialog__hero");

        Div preview = new Div();
        preview.addClassName("sp-planet-dialog__preview");
        preview.getStyle().set("background-color", source.getColorHex());

        Div heroCopy = new Div();
        heroCopy.addClassNames("sp-user-dialog__hero-copy", "sp-u-flex", "sp-u-flex-col", "sp-u-gap-xs");
        Span eyebrow = new Span(editing ? "Planet Editor" : "New Destination");
        eyebrow.addClassName("sp-user-dialog__eyebrow");
        H3 heroTitle = new H3(editing ? source.getName() : "Create a new planet");
        heroTitle.addClassName("sp-user-dialog__hero-title");
        Span helperText = new Span(editing ? "Keep the detail panel and favorites fully in sync."
                : "Add a new destination that appears immediately in the planets catalog.");
        helperText.addClassName("sp-user-dialog__hero-text");
        heroCopy.add(eyebrow, heroTitle, helperText);
        hero.add(preview, heroCopy);

        TextField nameField = new TextField("Name");
        nameField.setValue(source.getName());
        nameField.setRequiredIndicatorVisible(true);
        nameField.setErrorMessage("Name is required");

        TextField climateField = new TextField("Climate");
        climateField.setValue(source.getClimate());
        climateField.setRequiredIndicatorVisible(true);

        TextField distanceField = new TextField("Distance");
        distanceField.setValue(source.getDistanceLabel());
        distanceField.setRequiredIndicatorVisible(true);

        NumberField gravityField = new NumberField("Gravity %");
        gravityField.setValue((double) source.getGravityPct());
        gravityField.setStepButtonsVisible(true);
        gravityField.setMin(1);
        gravityField.setMax(300);
        gravityField.setRequiredIndicatorVisible(true);

        Select<String> colorField = new Select<>();
        colorField.setLabel("Planet Color");
        colorField.setItems("#6b6b6b", "#c79c4a", "#4a7cf7", "#c1440e", "#9b59b6", "#b0c4de");
        colorField.setItemLabelGenerator(this::planetColorLabel);
        colorField.setValue(source.getColorHex());
        colorField.setRequiredIndicatorVisible(true);
        colorField.setHelperText("Choose a controlled accent for the planet.");

        TextArea summaryField = new TextArea("Summary");
        summaryField.setValue(source.getSummary());
        summaryField.setRequiredIndicatorVisible(true);
        summaryField.setMinHeight("140px");

        Checkbox favoriteField = new Checkbox("Favorite in sidebar");
        favoriteField.setValue(source.isFavorite());

        styleDialogField(nameField);
        styleDialogField(climateField);
        styleDialogField(distanceField);
        gravityField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        gravityField.addClassName("sp-user-dialog__field");
        colorField.addThemeVariants(SelectVariant.LUMO_SMALL);
        colorField.addClassName("sp-user-dialog__field");
        summaryField.addThemeVariants(TextAreaVariant.LUMO_SMALL);
        summaryField.addClassName("sp-user-dialog__field");

        nameField.addValueChangeListener(event -> heroTitle.setText(event.getValue().isBlank() ? "Create a new planet" : event.getValue()));
        colorField.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                preview.getStyle().set("background-color", event.getValue());
            }
        });

        binder.forField(nameField)
                .asRequired("Name is required")
                .withValidator(value -> !adminCatalogService.planetNameExists(value.trim(), editing ? existingPlanet.getId() : null),
                        "Planet already exists")
                .bind(AdminPlanet::getName, AdminPlanet::setName);
        binder.forField(climateField)
                .asRequired("Climate is required")
                .bind(AdminPlanet::getClimate, AdminPlanet::setClimate);
        binder.forField(distanceField)
                .asRequired("Distance is required")
                .bind(AdminPlanet::getDistanceLabel, AdminPlanet::setDistanceLabel);
        binder.forField(gravityField)
                .asRequired("Gravity is required")
                .withValidator(value -> value != null && value > 0, "Gravity must be greater than 0")
                .withConverter(Double::intValue, Integer::doubleValue, "Gravity is required")
                .bind(AdminPlanet::getGravityPct, AdminPlanet::setGravityPct);
        binder.forField(colorField)
                .asRequired("Color is required")
                .bind(AdminPlanet::getColorHex, AdminPlanet::setColorHex);
        binder.forField(summaryField)
                .asRequired("Summary is required")
                .bind(AdminPlanet::getSummary, AdminPlanet::setSummary);

        FormLayout form = new FormLayout();
        form.addClassName("sp-user-dialog__form");
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2)
        );
        form.add(nameField, climateField, distanceField, gravityField, colorField, summaryField, favoriteField);
        form.setColspan(summaryField, 2);
        form.setColspan(favoriteField, 2);

        Div card = new Div();
        card.addClassNames("sp-user-dialog__card", "sp-u-flex", "sp-u-flex-col");
        Span cardTitle = new Span("Planet Profile");
        cardTitle.addClassName("sp-user-dialog__card-title");
        card.add(cardTitle, form);

        Div body = new Div();
        body.addClassNames("sp-user-dialog__body", "sp-u-flex", "sp-u-flex-col", "sp-u-gap-md");
        body.add(hero, card);
        dialog.add(body);

        Button deleteBtn = null;
        if (editing) {
            deleteBtn = new Button("Delete Planet");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClassName("sp-user-dialog__delete");
            deleteBtn.addClickListener(event -> confirmDeletePlanet(existingPlanet, dialog));
            dialog.getFooter().add(deleteBtn);
        }

        Button closeBtn = new Button("Close");
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.addClassName("sp-user-dialog__secondary-action");
        closeBtn.addClickListener(event -> dialog.close());

        Button saveBtn = new Button(editing ? "Save changes" : "Create planet");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClassName("sp-user-dialog__primary-action");
        Button finalDeleteBtn = deleteBtn;
        saveBtn.addClickListener(event -> {
            var validation = binder.validate();
            if (!validation.isOk()) {
                return;
            }

            saveBtn.setEnabled(false);
            closeBtn.setEnabled(false);
            if (finalDeleteBtn != null) {
                finalDeleteBtn.setEnabled(false);
            }

            AdminPlanet target = editing
                    ? adminCatalogService.findPlanet(existingPlanet.getId()).orElseThrow()
                    : new AdminPlanet();
            try {
                binder.writeBean(target);
            } catch (ValidationException _) {
                saveBtn.setEnabled(true);
                closeBtn.setEnabled(true);
                if (finalDeleteBtn != null) {
                    finalDeleteBtn.setEnabled(true);
                }
                return;
            }

            target.setFavorite(favoriteField.getValue());
            try {
                AdminPlanet savedPlanet = adminCatalogService.savePlanet(target);
                selectedPlanetId = savedPlanet.getId();
                refreshPlanetsGrid();
                dialog.close();
                getUI().ifPresent(ui -> ui.navigate(PlanetsView.class,
                        new RouteParameters(ROUTE_PLANET_ID, savedPlanet.getId().toString())));
                Notification.show(editing ? "Planet updated" : "Planet created");
            } finally {
                saveBtn.setEnabled(true);
                closeBtn.setEnabled(true);
                if (finalDeleteBtn != null) {
                    finalDeleteBtn.setEnabled(true);
                }
            }
        });

        dialog.getFooter().add(closeBtn, saveBtn);
        dialog.open();
    }

    private AdminPlanet buildDraftPlanet() {
        AdminPlanet planet = new AdminPlanet();
        planet.setName("");
        planet.setClimate("");
        planet.setGravityPct(100);
        planet.setDistanceLabel("");
        planet.setColorHex("#4a7cf7");
        planet.setSummary("");
        planet.setFavorite(false);
        return planet;
    }

    private <T extends Component & HasThemeVariant<TextFieldVariant>> void styleDialogField(T field) {
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.getElement().getClassList().add("sp-user-dialog__field");
    }

    private String planetColorLabel(String colorHex) {
        return switch (colorHex) {
            case "#6b6b6b" -> "Mercury Gray";
            case "#c79c4a" -> "Venus Gold";
            case "#4a7cf7" -> "Earth Blue";
            case "#c1440e" -> "Mars Red";
            case "#9b59b6" -> "Proxima Violet";
            case "#b0c4de" -> "Europa Ice";
            default -> "Custom";
        };
    }

    private String buildPlanetInitial(String name) {
        if (name == null || name.isBlank()) {
            return "P";
        }
        return name.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private void setSelectedFavorite(boolean favorite) {
        if (checkedPlanetIds.isEmpty()) {
            return;
        }
        checkedPlanetIds.forEach(id -> {
            AdminPlanet planet = adminCatalogService.findPlanet(id).orElse(null);
            if (planet != null && planet.isFavorite() != favorite) {
                adminCatalogService.togglePlanetFavorite(id);
            }
        });
        refreshPlanetsGrid();
        Notification.show(favorite ? "Planets added to favorites" : "Planets removed from favorites");
    }

    private void deleteSelectedPlanets() {
        if (checkedPlanetIds.isEmpty()) {
            return;
        }
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete selected planets?");
        dialog.setText("This action will permanently remove " + checkedPlanetIds.size() + " planets.");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            adminCatalogService.deletePlanets(checkedPlanetIds);
            checkedPlanetIds.clear();
            selectedPlanetId = null;
            refreshPlanetsGrid();
            getUI().ifPresent(ui -> ui.navigate("admin/planets"));
            Notification.show("Planets deleted");
        });
        dialog.open();
    }

    private void clearSelection() {
        checkedPlanetIds.clear();
        updateSelectionToolbar();
        planetsGrid.getDataProvider().refreshAll();
    }

    private void confirmDeletePlanet(AdminPlanet planet, Dialog editDialog) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete planet?");
        dialog.setText("This action will permanently remove " + planet.getName() + ".");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            adminCatalogService.deletePlanet(planet.getId());
            checkedPlanetIds.remove(planet.getId());
            if (selectedPlanetId != null && selectedPlanetId.equals(planet.getId())) {
                selectedPlanetId = null;
                getUI().ifPresent(ui -> ui.navigate("admin/planets"));
            }
            refreshPlanetsGrid();
            editDialog.close();
            Notification.show("Planet deleted");
        });
        dialog.open();
    }

    private void confirmDeletePlanet(AdminPlanet planet) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete planet?");
        dialog.setText("This action will permanently remove " + planet.getName() + ".");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            adminCatalogService.deletePlanet(planet.getId());
            checkedPlanetIds.remove(planet.getId());
            if (selectedPlanetId != null && selectedPlanetId.equals(planet.getId())) {
                selectedPlanetId = null;
                getUI().ifPresent(ui -> ui.navigate("admin/planets"));
            }
            refreshPlanetsGrid();
            refreshDetailsPanel();
            Notification.show("Planet deleted");
        });
        dialog.open();
    }
}
