package dev.fredpena.views.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
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
import com.vaadin.flow.component.slider.RangeSlider;
import com.vaadin.flow.component.slider.RangeSliderValue;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import dev.fredpena.admin.data.AdminCatalogService;
import dev.fredpena.admin.data.AdminUser;
import dev.fredpena.views.MainLayout;
import dev.fredpena.views.components.PrimarySidebar;
import dev.fredpena.views.components.SelectionToolbar;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@PageTitle("Admin - Users")
@Route(value = "admin/users/:userId?", layout = MainLayout.class)
@AnonymousAllowed
public class UsersView extends Div implements BeforeEnterObserver {

    private static final String ROUTE_USER_ID = "userId";

    private final AdminCatalogService adminCatalogService;
    private final AdminSecondaryNav adminSecondaryNav;
    private final Grid<AdminUser> usersGrid = new Grid<>();
    private final Div adminContent = new Div();
    private final Div detailsHost = new Div();
    private final Checkbox selectAllCheckbox = new Checkbox();
    private final TextField searchField = new TextField();
    private final SelectionToolbar selectionToolbar = new SelectionToolbar("user", "users", this::clearSelection);
    private final List<AdminUser> currentUsers = new ArrayList<>();
    private final ListDataProvider<AdminUser> usersDataProvider = new ListDataProvider<>(currentUsers);
    private final LinkedHashSet<Long> checkedUserIds = new LinkedHashSet<>();
    private Long selectedUserId;

    public UsersView(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
        this.adminSecondaryNav = new AdminSecondaryNav("users", adminCatalogService.getSidebarData());

        addClassNames("sp-shell", "sp-shell--admin");
        setSizeFull();

        add(new PrimarySidebar("admin"));
        add(adminSecondaryNav);

        adminContent.addClassName("sp-admin-content");
        adminContent.add(buildUsersPanel());

        detailsHost.setSizeFull();
        detailsHost.addClassName("sp-admin-content__detail-host");
        adminContent.add(detailsHost);

        add(adminContent);
        refreshUsersGrid();
        refreshDetailsPanel();
    }

    private Div buildUsersPanel() {
        Div panel = new Div();
        panel.addClassName("sp-users-panel");

        Div header = new Div();
        header.addClassName("sp-panel-header");

        H2 title = new H2("Users");
        title.addClassNames("sp-u-text-lg", "sp-u-font-bold", "sp-u-m-0");

        Button addBtn = new Button(VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        addBtn.addClickListener(e -> openUserDialog(null));
        header.add(title, addBtn);

        Div toolbar = new Div();
        toolbar.addClassName("sp-users-panel__toolbar");

        searchField.setPlaceholder("Search");
        searchField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addClassName("sp-inline-search");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> applySearchPredicate());
        toolbar.add(searchField);

        selectionToolbar.setActions(List.of(
                new SelectionToolbar.ActionConfig("Deactivate", VaadinIcon.POWER_OFF, null, this::deactivateSelectedUsers),
                new SelectionToolbar.ActionConfig("Email", VaadinIcon.ENVELOPE_O, null, this::emailSelectedUsers),
                new SelectionToolbar.ActionConfig("", VaadinIcon.TRASH, "sp-selection-toolbar__action--danger", this::deleteSelectedUsers)
        ));

        configureUsersGrid();

        panel.add(header, selectionToolbar, toolbar, usersGrid);
        return panel;
    }

    private void configureUsersGrid() {
        usersGrid.addClassName("sp-admin-grid");
        usersGrid.setDataProvider(usersDataProvider);
        usersGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        usersGrid.setEmptyStateText("No record found.");
        usersGrid.setSelectionMode(Grid.SelectionMode.NONE);
        usersGrid.setAllRowsVisible(false);
        usersGrid.setHeightFull();
        usersGrid.setPartNameGenerator(user -> user.getId() != null && user.getId().equals(selectedUserId) ? "detail-active" : null);
        usersGrid.addItemClickListener(event -> selectUser(event.getItem().getId(), true));

        usersGrid.addColumn(new ComponentRenderer<>(this::buildSelectionCheckbox))
                .setHeader(configureSelectionHeaderCheckbox())
                .setWidth("64px")
                .setFlexGrow(0)
                .setSortable(false)
                .setAutoWidth(false);
        usersGrid.addColumn(new ComponentRenderer<>(this::buildGridUserCell))
                .setHeader("User")
                .setComparator(AdminUser::getName)
                .setSortable(true)
                .setFlexGrow(1)
                .setAutoWidth(true);
        usersGrid.addColumn(new ComponentRenderer<>(this::buildGridStatusCell))
                .setHeader("Status")
                .setComparator(AdminUser::getStatus)
                .setSortable(true)
                .setWidth("110px")
                .setFlexGrow(0);
        usersGrid.addColumn(new ComponentRenderer<>(this::buildGridDateCell))
                .setHeader("Joined")
                .setComparator(AdminUser::getJoinedOn)
                .setSortable(true)
                .setWidth("180px")
                .setFlexGrow(0);
    }

    private void refreshUsersGrid() {
        List<AdminUser> fetchedUsers = adminCatalogService.findAllUsers();
        currentUsers.clear();
        currentUsers.addAll(fetchedUsers);
        checkedUserIds.retainAll(currentUsers.stream().map(AdminUser::getId).collect(Collectors.toSet()));
        applySearchPredicate();
        usersDataProvider.refreshAll();

        syncSelectionWithVisibleUsers();

        updateSelectionToolbar();
        adminSecondaryNav.refresh(adminCatalogService.getSidebarData());
        usersGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private void applySearchPredicate() {
        usersDataProvider.clearFilters();
        usersDataProvider.setFilter(buildSearchPredicate(searchField.getValue()));
        syncSelectionWithVisibleUsers();
        usersGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private SerializablePredicate<AdminUser> buildSearchPredicate(String query) {
        if (query == null || query.isBlank()) {
            return user -> true;
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return user -> user.getName().toLowerCase(Locale.ROOT).contains(normalized)
                || user.getEmail().toLowerCase(Locale.ROOT).contains(normalized)
                || user.getRole().toLowerCase(Locale.ROOT).contains(normalized)
                || user.getUsername().toLowerCase(Locale.ROOT).contains(normalized);
    }

    private void updateSelectionToolbar() {
        selectionToolbar.setCount(checkedUserIds.size());
        syncHeaderCheckbox();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long routeUserId = event.getRouteParameters()
                .get(ROUTE_USER_ID)
                .flatMap(this::parseUserId)
                .orElse(null);

        if (routeUserId != null && !searchField.isEmpty()) {
            searchField.clear();
            applySearchPredicate();
        }

        selectedUserId = routeUserId;
        syncSelectionWithVisibleUsers();
        usersGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private void selectUser(Long userId, boolean updateRoute) {
        if (userId == null || userId.equals(selectedUserId)) {
            selectedUserId = null;
            if (updateRoute) {
                getUI().ifPresent(ui -> ui.navigate("admin/users"));
            }
        } else {
            selectedUserId = userId;
            if (updateRoute) {
                getUI().ifPresent(ui -> ui.navigate(UsersView.class, new RouteParameters(ROUTE_USER_ID, userId.toString())));
            }
        }
        usersGrid.getDataProvider().refreshAll();
        refreshDetailsPanel();
    }

    private void refreshDetailsPanel() {
        detailsHost.removeAll();
        AdminUser selectedUser = getSelectedUser();
        if (selectedUser != null) {
            adminContent.removeClassName("sp-admin-content--detail-hidden");
            detailsHost.removeClassName("sp-admin-content__detail-host--hidden");
            detailsHost.add(buildDetailsPanel(selectedUser));
        } else {
            adminContent.addClassName("sp-admin-content--detail-hidden");
            detailsHost.addClassName("sp-admin-content__detail-host--hidden");
        }
    }

    private AdminUser getSelectedUser() {
        if (selectedUserId == null) {
            return null;
        }
        return adminCatalogService.findUser(selectedUserId).orElse(null);
    }

    private List<AdminUser> visibleUsers() {
        SerializablePredicate<AdminUser> predicate = buildSearchPredicate(searchField.getValue());
        return currentUsers.stream().filter(predicate::test).toList();
    }

    private void syncSelectionWithVisibleUsers() {
        List<AdminUser> visibleUsers = visibleUsers();
        if (selectedUserId != null && visibleUsers.stream().noneMatch(user -> user.getId().equals(selectedUserId))) {
            selectedUserId = null;
        }
    }

    private Checkbox configureSelectionHeaderCheckbox() {
        selectAllCheckbox.addClassName("sp-grid-select-checkbox");
        stopClickPropagation(selectAllCheckbox);
        selectAllCheckbox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                toggleAllVisibleUsers(event.getValue());
            }
        });
        syncHeaderCheckbox();
        return selectAllCheckbox;
    }

    private Checkbox buildSelectionCheckbox(AdminUser user) {
        Checkbox checkbox = new Checkbox();
        checkbox.addClassName("sp-grid-select-checkbox");
        stopClickPropagation(checkbox);
        checkbox.setValue(user.getId() != null && checkedUserIds.contains(user.getId()));
        checkbox.addValueChangeListener(event -> {
            if (!event.isFromClient() || user.getId() == null) {
                return;
            }
            if (event.getValue()) {
                checkedUserIds.add(user.getId());
            } else {
                checkedUserIds.remove(user.getId());
            }
            updateSelectionToolbar();
            usersGrid.getDataProvider().refreshAll();
        });
        return checkbox;
    }

    private void toggleAllVisibleUsers(boolean checked) {
        List<Long> visibleIds = visibleUsers().stream()
                .map(AdminUser::getId)
                .toList();
        if (checked) {
            checkedUserIds.addAll(visibleIds);
        } else {
            checkedUserIds.removeAll(visibleIds);
        }
        updateSelectionToolbar();
        usersGrid.getDataProvider().refreshAll();
    }

    private void syncHeaderCheckbox() {
        List<Long> visibleIds = visibleUsers().stream()
                .map(AdminUser::getId)
                .toList();
        boolean allVisibleChecked = !visibleIds.isEmpty() && visibleIds.stream().allMatch(checkedUserIds::contains);
        selectAllCheckbox.setValue(allVisibleChecked);
    }

    private void stopClickPropagation(Checkbox checkbox) {
        checkbox.getElement().executeJs(
                "this.addEventListener('click', e => e.stopPropagation());" +
                        "this.addEventListener('pointerdown', e => e.stopPropagation());"
        );
    }

    private java.util.Optional<Long> parseUserId(String value) {
        try {
            return java.util.Optional.of(Long.parseLong(value));
        } catch (NumberFormatException _) {
            return java.util.Optional.empty();
        }
    }

    private Div buildGridUserCell(AdminUser user) {
        Div card = new Div();
        card.addClassName("sp-user-row__card");

        Div userCell = new Div();
        userCell.addClassName("sp-user-row__user");

        Div avatar = buildInitialsAvatar(user.getInitials(), false);
        avatar.addClassName("sp-user-row__avatar");
        applyColorTheme(avatar, user.getColorIndex(), false);

        Div meta = new Div();
        meta.addClassName("sp-user-row__meta");

        Div nameRow = new Div();
        nameRow.addClassName("sp-user-row__name-row");

        Span name = new Span(user.getName());
        name.addClassName("sp-user-row__name");

        if (user.isFavorite()) {
            Icon favoriteIcon = VaadinIcon.STAR.create();
            favoriteIcon.addClassName("sp-user-row__favorite-icon");
            nameRow.add(name, favoriteIcon);
        } else {
            nameRow.add(name);
        }

        Span role = new Span(user.getRole());
        role.addClassName("sp-user-row__role");

        Span email = new Span(user.getEmail());
        email.addClassName("sp-user-row__email");

        meta.add(nameRow, role, email);
        userCell.add(avatar, meta);
        card.add(userCell);
        return card;
    }

    private Div buildGridStatusCell(AdminUser user) {
        Span status = new Span(user.getStatus());
        status.addClassName("sp-status-pill");
        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            status.addClassName("sp-status-pill--inactive");
        }

        Div wrap = new Div(status);
        wrap.addClassName("sp-user-row__aux");
        return wrap;
    }

    private Div buildGridDateCell(AdminUser user) {
        Div dateCell = new Div();
        dateCell.addClassName("sp-user-row__date");
        dateCell.add(new Span("Since " + user.getJoinedOn()), buildMutedLine(sinceAgo(user.getJoinedOn())));

        Div wrap = new Div(dateCell);
        wrap.addClassName("sp-user-row__aux");
        return wrap;
    }

    private Div buildDetailsPanel(AdminUser user) {
        Div panel = new Div();
        panel.addClassName("sp-details-panel");

        Div header = new Div();
        header.addClassName("sp-panel-header");

        H2 title = new H2("User Details");
        title.addClassNames("sp-u-text-lg", "sp-u-font-bold", "sp-u-m-0");

        Div actions = new Div();
        actions.addClassName("sp-header-actions");

        Button favBtn = new Button(VaadinIcon.STAR.create());
        favBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        if (user.isFavorite()) {
            favBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            favBtn.addClassName("sp-favorite-toggle--active");
            favBtn.getElement().setProperty("title", "Remove from favorites");
        } else {
            favBtn.addClassName("sp-favorite-toggle");
            favBtn.getElement().setProperty("title", "Add to favorites");
        }
        favBtn.addClickListener(e -> {
            adminCatalogService.toggleFavorite(user.getId());
            refreshUsersGrid();
            refreshDetailsPanel();
        });

        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        editBtn.addClickListener(e -> openUserDialog(user));

        Button moreBtn = new Button(VaadinIcon.ELLIPSIS_DOTS_H.create());
        moreBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        moreBtn.getElement().setProperty("title", "More actions");

        Popover morePopover = new Popover();
        morePopover.setTarget(moreBtn);
        morePopover.setOpenOnClick(true);
        morePopover.addClassName("sp-overflow-popover");

        Button deleteAction = new Button("Delete user", VaadinIcon.TRASH.create());
        deleteAction.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        deleteAction.addClassNames("sp-overflow-action", "sp-overflow-action--danger");
        deleteAction.addClickListener(e -> {
            confirmDeleteUser(user);
            morePopover.close();
        });

        morePopover.add(deleteAction);

        actions.add(favBtn, editBtn, moreBtn);
        header.add(title, actions);

        Div cover = new Div();
        cover.addClassName("sp-details-panel__cover");
        applyColorTheme(cover, user.getColorIndex(), true);

        Div content = new Div();
        content.addClassNames("sp-u-flex-1", "sp-u-overflow-auto");

        Div profileHeader = new Div();
        profileHeader.addClassName("sp-details-panel__profile-header");

        Div largeAvatar = buildInitialsAvatar(user.getInitials(), true);
        applyColorTheme(largeAvatar, user.getColorIndex(), false);

        Div heroBody = new Div();
        heroBody.addClassName("sp-details-panel__hero-body");

        Span eyebrow = new Span("Crew Profile");
        eyebrow.addClassName("sp-details-panel__eyebrow");

        H3 userName = new H3(user.getName());
        userName.addClassNames("sp-u-text-xl", "sp-u-font-extrabold", "sp-u-m-0");

        Span userRole = new Span(user.getRole());
        userRole.addClassName("sp-details-panel__subtitle");

        Span statusBadge = new Span(user.getStatus());
        statusBadge.addClassName("sp-status-pill");
        if ("Inactive".equalsIgnoreCase(user.getStatus())) {
            statusBadge.addClassName("sp-status-pill--inactive");
        }

        Span joinedLabel = new Span("Joined " + user.getJoinedOn());
        joinedLabel.addClassName("sp-details-panel__joined");

        heroBody.add(eyebrow, userName, userRole, statusBadge, joinedLabel);
        profileHeader.add(largeAvatar, heroBody);

        Div metaStrip = new Div();
        metaStrip.addClassName("sp-details-panel__meta-strip");
        metaStrip.add(
                buildUserMetaCard("Username", user.getUsername(), "Identity"),
                buildUserMetaCard("Location", user.getLocation(), "Base"),
                buildUserMetaCard("Favorite", user.isFavorite() ? "Pinned" : "Normal", "Sidebar state")
        );

        Div contactSection = new Div();
        contactSection.addClassName("sp-details-panel__section-card");

        H4 contactTitle = new H4("Contact Information");
        contactTitle.addClassNames("sp-u-text-md", "sp-u-font-bold", "sp-u-m-0");

        contactSection.add(contactTitle);

        contactSection.add(buildContactItem(VaadinIcon.ENVELOPE_O.create(), "Email Address", user.getEmail(), true));
        contactSection.add(buildContactItem(VaadinIcon.PHONE.create(), "Phone", user.getPhone(), true));
        contactSection.add(buildContactItem(VaadinIcon.MAP_MARKER.create(), "Location", user.getLocation(), false));

        Div accessSection = new Div();
        accessSection.addClassName("sp-details-panel__section-card");

        H4 accessTitle = new H4("Account & Access");
        accessTitle.addClassNames("sp-u-text-md", "sp-u-font-bold", "sp-u-m-0");

        accessSection.add(accessTitle);
        accessSection.add(buildContactItem(VaadinIcon.AT.create(), "Username", user.getUsername(), false));
        accessSection.add(buildContactItem(VaadinIcon.STAR.create(), "Favorite", user.isFavorite() ? "Pinned in favorites" : "Not pinned", false));
        accessSection.add(buildContactItem(VaadinIcon.CALENDAR.create(), "Member Since", user.getJoinedOn().toString(), false));

        Div body = new Div();
        body.addClassName("sp-details-panel__body");
        body.add(profileHeader, metaStrip, contactSection, accessSection);

        content.add(body);
        panel.add(header, cover, content);
        return panel;
    }

    private Div buildEmptyDetailsPanel() {
        Div panel = new Div();
        panel.addClassNames("sp-details-panel", "sp-details-panel--empty");

        Div empty = new Div();
        empty.addClassName("sp-details-panel__empty-state");
        H3 title = new H3("No user selected");
        Span text = new Span("Pick a user from the list or create a new one to see the full profile here.");
        Button createBtn = new Button("Add User");
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.addClickListener(e -> openUserDialog(null));
        empty.add(title, text, createBtn);
        panel.add(empty);
        return panel;
    }

    private Div buildContactItem(Icon icon, String label, String value, boolean isLink) {
        Div item = new Div();
        item.addClassName("sp-contact-item");

        Div iconBox = new Div();
        iconBox.addClassName("sp-contact-item__icon");
        iconBox.add(icon);

        Div textBlock = new Div();
        textBlock.addClassName("sp-contact-item__text");
        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-contact-item__label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("sp-contact-item__value");
        if (isLink) {
            valueSpan.addClassName("sp-contact-item__value--link");
        }

        textBlock.add(labelSpan, valueSpan);
        item.add(iconBox, textBlock);
        return item;
    }

    private Div buildUserMetaCard(String label, String value, String meta) {
        Div card = new Div();
        card.addClassName("sp-user-meta-card");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("sp-user-meta-card__label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("sp-user-meta-card__value");

        Span metaSpan = new Span(meta);
        metaSpan.addClassName("sp-user-meta-card__meta");

        card.add(labelSpan, valueSpan, metaSpan);
        return card;
    }

    private void openUserDialog(AdminUser existingUser) {
        boolean editing = existingUser != null;
        AdminUser source = editing ? existingUser : buildDraftUser();
        Binder<AdminUser> binder = new Binder<>(AdminUser.class);

        Dialog dialog = new Dialog();
        dialog.addClassName("sp-user-dialog");
        dialog.setHeaderTitle(editing ? "Edit User" : "Add User");
        dialog.setWidth("640px");

        Div avatarRow = new Div();
        avatarRow.addClassName("sp-user-dialog__hero");
        Div avatar = buildInitialsAvatar(source.getInitials(), false);
        avatar.addClassName("sp-user-dialog__avatar");
        applyColorTheme(avatar, source.getColorIndex(), false);

        Div heroCopy = new Div();
        heroCopy.addClassName("sp-user-dialog__hero-copy");
        Span eyebrow = new Span(editing ? "Profile Editor" : "New Crew Member");
        eyebrow.addClassName("sp-user-dialog__eyebrow");
        H3 heroTitle = new H3(editing ? source.getName() : "Create a new user");
        heroTitle.addClassName("sp-user-dialog__hero-title");
        Span helperText = new Span(editing ? "Update the selected profile and keep the detail panel in sync." : "Create a new profile that will appear immediately in the users list.");
        helperText.addClassName("sp-user-dialog__hero-text");
        heroCopy.add(eyebrow, heroTitle, helperText);
        avatarRow.add(avatar, heroCopy);

        TextField usernameField = new TextField("Username");
        usernameField.setValue(source.getUsername());
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setErrorMessage("Username is required");

        DatePicker joinedField = new DatePicker("Joined");
        joinedField.setValue(source.getJoinedOn());
        joinedField.setRequiredIndicatorVisible(true);

        TextField fullNameField = new TextField("Full Name");
        fullNameField.setValue(source.getName());
        fullNameField.setRequiredIndicatorVisible(true);
        fullNameField.setErrorMessage("Full name is required");

        EmailField emailField = new EmailField("Email Address");
        emailField.setValue(source.getEmail());
        emailField.setRequiredIndicatorVisible(true);

        TextField titleField = new TextField("Title");
        titleField.setValue(source.getRole());
        titleField.setRequiredIndicatorVisible(true);

        TextField phoneField = new TextField("Phone");
        phoneField.setValue(source.getPhone());
        phoneField.setRequiredIndicatorVisible(true);

        TextField locationField = new TextField("Location");
        locationField.setValue(source.getLocation());
        locationField.setRequiredIndicatorVisible(true);

        Select<String> statusField = new Select<>();
        statusField.setLabel("Status");
        statusField.setItems("Active", "Inactive");
        statusField.setValue(source.getStatus());
        statusField.setRequiredIndicatorVisible(true);

        Select<Integer> colorField = new Select<>();
        colorField.setLabel("Profile Color");
        colorField.setItems(1, 2, 3, 4, 5);
        colorField.setItemLabelGenerator(this::colorLabel);
        colorField.setValue(source.getColorIndex());
        colorField.setRequiredIndicatorVisible(true);
        colorField.setHelperText("Choose one of five profile tones.");

        Checkbox favoriteField = new Checkbox("Favorite in sidebar");
        favoriteField.setValue(source.isFavorite());

        styleDialogField(usernameField);
        styleDialogField(fullNameField);
        styleDialogField(emailField);
        styleDialogField(titleField);
        styleDialogField(phoneField);
        styleDialogField(locationField);
        statusField.addThemeVariants(SelectVariant.LUMO_SMALL);
        statusField.addClassName("sp-user-dialog__field");
        colorField.addThemeVariants(SelectVariant.LUMO_SMALL);
        colorField.addClassName("sp-user-dialog__field");
        joinedField.addThemeVariants(DatePickerVariant.LUMO_SMALL);
        joinedField.addClassName("sp-user-dialog__field");

        fullNameField.addValueChangeListener(event -> avatar.setText(buildInitials(event.getValue())));
        colorField.addValueChangeListener(event -> {
            Integer colorIndex = event.getValue();
            if (colorIndex != null) {
                applyColorTheme(avatar, colorIndex, false);
            }
        });

        binder.forField(usernameField)
                .asRequired("Username is required")
                .withValidator(value -> !adminCatalogService.usernameExists(value.trim(), editing ? existingUser.getId() : null),
                        "Username already exists")
                .bind(AdminUser::getUsername, AdminUser::setUsername);
        binder.forField(joinedField)
                .asRequired("Joined date is required")
                .bind(AdminUser::getJoinedOn, AdminUser::setJoinedOn);
        binder.forField(emailField)
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Enter a valid email"))
                .bind(AdminUser::getEmail, AdminUser::setEmail);
        binder.forField(titleField)
                .asRequired("Title is required")
                .bind(AdminUser::getRole, AdminUser::setRole);
        binder.forField(phoneField)
                .asRequired("Phone is required")
                .bind(AdminUser::getPhone, AdminUser::setPhone);
        binder.forField(locationField)
                .asRequired("Location is required")
                .bind(AdminUser::getLocation, AdminUser::setLocation);
        binder.forField(statusField)
                .asRequired("Status is required")
                .bind(AdminUser::getStatus, AdminUser::setStatus);
        binder.forField(colorField)
                .asRequired("Color is required")
                .bind(AdminUser::getColorIndex, AdminUser::setColorIndex);

        FormLayout identityForm = new FormLayout();
        identityForm.addClassName("sp-user-dialog__form");
        identityForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2)
        );
        identityForm.add(usernameField, joinedField, fullNameField);
        identityForm.setColspan(fullNameField, 2);

        Div identityCard = new Div();
        identityCard.addClassName("sp-user-dialog__card");
        Span identityTitle = new Span("Identity");
        identityTitle.addClassName("sp-user-dialog__card-title");
        identityCard.add(identityTitle, identityForm);

        FormLayout detailsForm = new FormLayout();
        detailsForm.addClassName("sp-user-dialog__form");
        detailsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("520px", 2)
        );
        detailsForm.add(emailField, titleField, phoneField, locationField, statusField, colorField, favoriteField);
        detailsForm.setColspan(favoriteField, 2);

        Div detailsCard = new Div();
        detailsCard.addClassName("sp-user-dialog__card");
        Span detailsTitle = new Span("Contact & Access");
        detailsTitle.addClassName("sp-user-dialog__card-title");
        detailsCard.add(detailsTitle, detailsForm);

        Div body = new Div();
        body.addClassName("sp-user-dialog__body");
        body.add(avatarRow, identityCard, detailsCard);
        dialog.add(body);

        Button deleteBtn = null;
        if (editing) {
            deleteBtn = new Button("Delete User");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClassName("sp-user-dialog__delete");
            deleteBtn.addClickListener(e -> confirmDeleteUser(existingUser, dialog));
            dialog.getFooter().add(deleteBtn);
        }

        Button closeBtn = new Button("Close");
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeBtn.addClassName("sp-user-dialog__secondary-action");
        closeBtn.addClickListener(e -> dialog.close());

        Button saveBtn = new Button(editing ? "Save changes" : "Create user");
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClassName("sp-user-dialog__primary-action");
        Button finalDeleteBtn = deleteBtn;
        saveBtn.addClickListener(e -> {
            String fullName = fullNameField.getValue().trim();
            boolean invalidFullName = fullName.isBlank();
            fullNameField.setInvalid(invalidFullName);

            var validation = binder.validate();
            if (invalidFullName || !validation.isOk()) {
                return;
            }

            saveBtn.setEnabled(false);
            closeBtn.setEnabled(false);
            if (finalDeleteBtn != null) {
                finalDeleteBtn.setEnabled(false);
            }

            AdminUser target = editing
                    ? adminCatalogService.findUser(existingUser.getId()).orElseThrow()
                    : new AdminUser();
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

            target.setInitials(buildInitials(fullName));
            target.setName(fullName);
            target.setFavorite(favoriteField.getValue());
            try {
                AdminUser savedUser = adminCatalogService.saveUser(target);
                selectedUserId = savedUser.getId();
                refreshUsersGrid();
                dialog.close();
                getUI().ifPresent(ui -> ui.navigate(UsersView.class, new RouteParameters(ROUTE_USER_ID, savedUser.getId().toString())));
                Notification.show(editing ? "User updated" : "User created");
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

    private AdminUser buildDraftUser() {
        AdminUser user = new AdminUser();
        user.setUsername("");
        user.setInitials("NU");
        user.setName("");
        user.setRole("");
        user.setEmail("");
        user.setPhone("");
        user.setLocation("");
        user.setStatus("Active");
        user.setJoinedOn(LocalDate.now());
        user.setColorIndex(adminCatalogService.nextColorIndex());
        user.setFavorite(false);
        return user;
    }

    private <T extends Component & HasThemeVariant<TextFieldVariant>> void styleDialogField(T field) {
        field.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        field.getElement().getClassList().add("sp-user-dialog__field");
    }

    private Div buildInitialsAvatar(String initials, boolean large) {
        Div avatar = new Div();
        avatar.addClassName("sp-initials-avatar");
        if (large) {
            avatar.addClassName("sp-initials-avatar--large");
        }
        avatar.setText(initials);
        return avatar;
    }

    private void applyColorTheme(Div component, int colorIndex, boolean cover) {
        component.removeClassNames(
                "sp-cover-tone--rose", "sp-cover-tone--blue", "sp-cover-tone--amber", "sp-cover-tone--emerald", "sp-cover-tone--violet",
                "sp-avatar-tone--rose", "sp-avatar-tone--blue", "sp-avatar-tone--amber", "sp-avatar-tone--emerald", "sp-avatar-tone--violet"
        );
        String suffix = switch (Math.max(1, Math.min(colorIndex, 5))) {
            case 1 -> "rose";
            case 2 -> "blue";
            case 3 -> "amber";
            case 4 -> "emerald";
            default -> "violet";
        };
        component.addClassName((cover ? "sp-cover-tone--" : "sp-avatar-tone--") + suffix);
    }

    private String colorLabel(Integer colorIndex) {
        return switch (colorIndex) {
            case 1 -> "Rose";
            case 2 -> "Blue";
            case 3 -> "Amber";
            case 4 -> "Emerald";
            case 5 -> "Violet";
            default -> "Default";
        };
    }

    private Span buildMutedLine(String text) {
        Span muted = new Span(text);
        muted.addClassNames("sp-u-text-tertiary");
        muted.getStyle().set("display", "block");
        return muted;
    }

    private String sinceAgo(LocalDate since) {
        Period period = Period.between(since, LocalDate.now());
        if (period.getYears() > 0) {
            return period.getYears() + (period.getYears() == 1 ? " year ago" : " years ago");
        }
        if (period.getMonths() > 0) {
            return period.getMonths() + (period.getMonths() == 1 ? " month ago" : " months ago");
        }
        int days = Math.max(period.getDays(), 0);
        return days + (days == 1 ? " day ago" : " days ago");
    }

    private String buildInitials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) {
            return "NA";
        }
        String initials = parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        if (parts.length > 1 && !parts[1].isBlank()) {
            initials += parts[1].substring(0, 1).toUpperCase(Locale.ROOT);
        }
        return initials;
    }

    private void deactivateSelectedUsers() {
        if (checkedUserIds.isEmpty()) {
            return;
        }
        adminCatalogService.deactivateUsers(checkedUserIds);
        checkedUserIds.clear();
        refreshUsersGrid();
        refreshDetailsPanel();
        Notification.show("Users deactivated");
    }

    private void emailSelectedUsers() {
        if (checkedUserIds.isEmpty()) {
            return;
        }
        Notification.show("Prepared " + checkedUserIds.size() + " email recipients");
    }

    private void deleteSelectedUsers() {
        if (checkedUserIds.isEmpty()) {
            return;
        }
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete selected users?");
        dialog.setText("This action will permanently remove " + checkedUserIds.size() + " users.");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            adminCatalogService.deleteUsers(checkedUserIds);
            checkedUserIds.clear();
            selectedUserId = null;
            refreshUsersGrid();
            refreshDetailsPanel();
            getUI().ifPresent(ui -> ui.navigate("admin/users"));
            Notification.show("Users deleted");
        });
        dialog.open();
    }

    private void clearSelection() {
        checkedUserIds.clear();
        usersGrid.deselectAll();
        updateSelectionToolbar();
    }

    private void confirmDeleteUser(AdminUser user, Dialog editDialog) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete user?");
        dialog.setText("This action will permanently remove " + user.getName() + ".");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            adminCatalogService.deleteUser(user.getId());
            checkedUserIds.remove(user.getId());
            if (selectedUserId != null && selectedUserId.equals(user.getId())) {
                selectedUserId = null;
                getUI().ifPresent(ui -> ui.navigate("admin/users"));
            }
            refreshUsersGrid();
            refreshDetailsPanel();
            editDialog.close();
            Notification.show("User deleted");
        });
        dialog.open();
    }

    private void confirmDeleteUser(AdminUser user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete user?");
        dialog.setText("This action will permanently remove " + user.getName() + ".");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(event -> {
            adminCatalogService.deleteUser(user.getId());
            checkedUserIds.remove(user.getId());
            if (selectedUserId != null && selectedUserId.equals(user.getId())) {
                selectedUserId = null;
                getUI().ifPresent(ui -> ui.navigate("admin/users"));
            }
            refreshUsersGrid();
            refreshDetailsPanel();
            Notification.show("User deleted");
        });
        dialog.open();
    }
}
