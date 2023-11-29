/*-
 * #%L
 * Enhanced Tabs Add-on
 * %%
 * Copyright (C) 2023 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.addons.enhancedtabs;

import com.flowingcode.vaadin.addons.demo.DemoSource;
import com.flowingcode.vaadin.addons.enhancedtabs.EnhancedTabs;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@DemoSource
@PageTitle("Enhanced Tabs Demo")
@Route(value = "enhanced-tabs/demo", layout = EnhancedTabsDemoView.class)
@SuppressWarnings("serial")
public class EnhancedTabsDemo extends Div {

  public EnhancedTabsDemo() {
    setWidthFull();

    // #if vaadin eq 0
    add(
        new Html(
            "<div><code>EnhancedTabs</code> is like Vaadin <code>Tabs</code>, but tabs that don't fit into the current width automatically collapse into an overflow menu at the end:</div>"));
    // #endif

    EnhancedTabs tabs = new EnhancedTabs();
    for (int i = 1; i <= 30; i++) {
      tabs.add(new Tab("Tab " + i));
    }

    add(tabs);

    tabs.addSelectedChangeListener(
        ev -> {
          Notification.show("Tab selected: " + ev.getSelectedTab().getElement().getText());
        });

    // #if vaadin eq 0
    Div div = new Div();
    div.setHeight("3em");
    add(div);
    add(new Html("<div>This is a Vaadin <code>Tabs</code> component, for comparison: </div>"));
    Tabs vaadinTabs = new Tabs();
    for (int i = 1; i <= 30; i++) {
      vaadinTabs.add(new Tab("Tab " + i));
    }
    vaadinTabs.addSelectedChangeListener(
        ev -> {
          Notification.show("Tab selected: " + ev.getSelectedTab().getElement().getText());
        });
    add(vaadinTabs);
    // #endif
  }
}
