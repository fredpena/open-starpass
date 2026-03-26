package dev.fredpena.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.List;

public class SelectionToolbar extends Div {

    public record ActionConfig(String label, VaadinIcon icon, String className, Runnable handler) {
    }

    private final String singularLabel;
    private final String pluralLabel;
    private final Span countLabel = new Span();
    private final Div actions = new Div();

    public SelectionToolbar(String singularLabel, String pluralLabel, Runnable clearHandler) {
        this.singularLabel = singularLabel;
        this.pluralLabel = pluralLabel;

        addClassName("sp-selection-toolbar");
        getElement().setAttribute("hidden", true);

        countLabel.addClassName("sp-selection-toolbar__count");
        actions.addClassName("sp-selection-toolbar__actions");

        Button clearButton = new Button(VaadinIcon.CLOSE_SMALL.create());
        clearButton.addClassNames("sp-selection-toolbar__clear");
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        clearButton.setAriaLabel("Clear selection");
        clearButton.addClickListener(e -> clearHandler.run());

        add(countLabel, actions, clearButton);
    }

    public void setActions(List<ActionConfig> actionConfigs) {
        actions.removeAll();
        for (ActionConfig actionConfig : actionConfigs) {
            actions.add(buildActionButton(actionConfig));
        }
    }

    public void setCount(int count) {
        countLabel.setText(count + " " + (count == 1 ? singularLabel : pluralLabel) + " selected");
        if (count > 0) {
            getElement().removeAttribute("hidden");
        } else {
            getElement().setAttribute("hidden", true);
        }
    }

    private Component buildActionButton(ActionConfig actionConfig) {
        Button button;
        Icon icon = actionConfig.icon() != null ? actionConfig.icon().create() : null;
        if (actionConfig.label() != null && !actionConfig.label().isBlank()) {
            button = icon == null ? new Button(actionConfig.label()) : new Button(actionConfig.label(), icon);
        } else {
            button = new Button(icon);
        }
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        button.addClassName("sp-selection-toolbar__action");
        if (actionConfig.className() != null && !actionConfig.className().isBlank()) {
            button.addClassName(actionConfig.className());
        }
        button.addClickListener(e -> actionConfig.handler().run());
        return button;
    }
}
