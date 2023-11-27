package com.flowingcode.vaadin.addons.template;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Template Addon Demo")
@Route(value = "demo", layout = TemplateDemoView.class)
@SuppressWarnings("serial")
public class TemplateDemo extends Div {

  public TemplateDemo() {
    add(new TemplateAddon());
  }
}
