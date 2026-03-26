package dev.fredpena.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Layout
@AnonymousAllowed
public class MainLayout extends Div implements RouterLayout {
    public MainLayout() {
        setSizeFull();
        getStyle().set("overflow", "hidden");
    }
}
