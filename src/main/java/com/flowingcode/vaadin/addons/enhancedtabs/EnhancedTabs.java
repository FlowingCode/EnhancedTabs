/*-
 * #%L
 * Enhanced Tabs Add-on
 * %%
 * Copyright (C) 2023-2024 Flowing Code
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

/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.flowingcode.vaadin.addons.enhancedtabs;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Tabs are used to organize and group content into sections that the user can navigate between. Use
 * Tabs when you want to allow in-place navigation within a certain part of the UI, instead of
 * showing everything at once or forcing the user to navigate between different views.
 *
 * <p>{@link Tab} components can be added to this component with the {@link #add(Tab...)} method or
 * the {@link #Tabs(Tab...)} constructor. The Tab components added to it can be selected with the
 * {@link #setSelectedIndex(int)} or {@link #setSelectedTab(Tab)} methods. The first added {@link
 * Tab} component will be automatically selected, firing a {@link SelectedChangeEvent}, unless
 * autoselection is explicitly disabled with {@link #Tabs(boolean, Tab...)}, or {@link
 * #setAutoselect(boolean)}. Removing the selected tab from the component changes the selection to
 * the next available tab.
 *
 * <p><strong>Note:</strong> Adding or removing Tab components via the Element API, eg. {@code
 * tabs.getElement().insertChild(0, tab.getElement()); }, doesn't update the selected index, so it
 * may cause the selected tab to change unexpectedly.
 */
@JsModule("./fcEnhancedTabs/connector.js")
@CssImport("./fcEnhancedTabs/fc-enhanced-tabs.css")
@CssImport(value = "./fcEnhancedTabs/fc-enhanced-tabs-legacy.css")
@CssImport(
    value = "./fcEnhancedTabs/vaadin-menu-bar-button-legacy.css",
    themeFor = "vaadin-menu-bar-button")
public class EnhancedTabs extends Composite<MenuBar> implements HasEnabled, HasSize, HasStyle {

  private transient Tab selectedTab;

  private boolean autoselect = true;

  private int selectedIndex = -1;

  /** Constructs an empty new object. */
  public EnhancedTabs() {
    setSelectedIndex(-1);
    getElement().getThemeList().add("fc-enhanced-tabs");
    getContent().setOpenOnHover(true);
  }

  /**
   * Constructs a new object enclosing the given tabs.
   *
   * <p>The first added {@link Tab} component will be automatically selected. Any selection change
   * listener added afterwards will not be notified about the auto-selected tab.
   *
   * @param tabs the tabs to enclose
   */
  public EnhancedTabs(Tab... tabs) {
    this();
    add(tabs);
  }

  @Override
  protected void onAttach(AttachEvent event) {
    event
        .getUI()
        .getElement()
        .executeJs("window.Vaadin.Flow.fcEnhancedTabsConnector.initLazy($0)", getElement());
  }

  /**
   * Sets whether the submenu opens by clicking or hovering on the overflow buttons. Defaults to
   * {@code true}.
   *
   * @param openOnHover {@code true} to make the sub menus open on hover (mouseover), {@code false}
   *     to make them openable by clicking
   */
  public void setOpenOnHover(boolean openOnHover) {
    getContent().setOpenOnHover(openOnHover);
  }

  /**
   * Gets whether the submenu opens by clicking or hovering on the overflow buttons.
   *
   * @return {@code true} if the sub menus open by hovering on the root level buttons, {@code false}
   *     if they open by clicking
   */
  public boolean isOpenOnHover() {
    return getElement().getProperty("openOnHover", false);
  }

  /**
   * Constructs a new object enclosing the given autoselect option and tabs,.
   *
   * <p>Unless auto-select is disabled, the first added {@link Tab} component will be automatically
   * selected. Any selection change listener added afterwards will not be notified about the
   * auto-selected tab.
   *
   * @param autoselect {@code true} to automatically select the first added tab, {@code false} to
   *     leave tabs unselected
   * @param tabs the tabs to enclose
   */
  public EnhancedTabs(boolean autoselect, Tab... tabs) {
    this();
    this.autoselect = autoselect;
    add(tabs);
  }

  /**
   * Adds the given tabs to the component.
   *
   * <p>The first added {@link Tab} component will be automatically selected, unless auto-selection
   * is explicitly disabled with {@link #Tabs(boolean, Tab...)}, or {@link #setAutoselect(boolean)}.
   * If a selection change listener has been added before adding the tabs, it will be notified with
   * the auto-selected tab.
   *
   * @param tabs the tabs to enclose
   */
  public void add(Tab... tabs) {
    Objects.requireNonNull(tabs, "Tabs should not be null");
    boolean wasEmpty = getComponentCount() == 0;

    for (Tab tab : tabs) {
      Objects.requireNonNull(tab, "Tab to add cannot be null");
      MenuItem item = getContent().addItem(tab);
      item.addClickListener(ev -> setSelectedTab(getTab(ev.getSource()), ev.isFromClient()));

      // this is a workaround for
      // https://github.com/vaadin/web-components/blob/c805b384cf9c3691b8310af30de85af07fced5f7/packages/context-menu/src/vaadin-contextmenu-items-mixin.js#L225
      tab.addAttachListener(ev -> tab.getElement().executeJs("this._item = {children:{}};"));

      // close on click
      // Vaadin 23: vaadin-context-menu-overlay
      // Vaadin 24: vaadin-menu-bar-overlay
      tab.addAttachListener(
          ev ->
              tab.getElement()
                  .executeJs(
                      "this.addEventListener('click', ()=>{let overlay = this.closest('vaadin-menu-bar-overlay, vaadin-context-menu-overlay'); overlay && overlay.close();})"));
    }

    if (tabs.length == 0) {
      return;
    }
    if (wasEmpty && autoselect) {
      assert getSelectedIndex() == -1;
      setSelectedIndex(0);
    } else {
      updateSelectedTab(false);
    }
  }

  private static final Method UI_navigate;
  static {
    try {
      UI_navigate = UI.class.getMethod("navigate", Class.class);
    } catch (NoSuchMethodException e) {
      throw new NoSuchMethodError("UI.navigate(Class)");
    }
  }

  private static void navigate(UI ui, Class<? extends Component> target) {
    try {
      UI_navigate.invoke(ui, target);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public RouterLink addRouterLink(String text, Class<? extends Component> target) {
    RouterLink routerLink = new RouterLink(text, target);
    routerLink.getElement().executeJs(
        "this.addEventListener('click', e => {\n"+
        "e.preventDefault();\n"+
        "this.dispatchEvent(new CustomEvent('client-side-click'));\n"+
        "});\n");

    routerLink.getElement().addEventListener("client-side-click", event -> {
      navigate(UI.getCurrent(), target);
    });

    add(new Tab(routerLink));
    return routerLink;
  }

  /**
   * Removes the given child tabs from this component.
   *
   * @param tabs the tabs to remove
   * @throws IllegalArgumentException if there is a tab whose non {@code null} parent is not this
   *     component
   *     <p>Removing tabs before the selected tab will decrease the {@link #getSelectedIndex()
   *     selected index} to avoid changing the selected tab. Removing the selected tab will select
   *     the next available tab if autoselect is true, otherwise no tab will be selected.
   */
  public void remove(Tab... tabs) {
    int selectedIndex = getSelectedIndex();
    int lowerIndices =
        (int)
            Stream.of(tabs)
                .map(this::indexOf)
                .filter(index -> index >= 0 && index < selectedIndex)
                .count();

    Tab selectedTab = getSelectedTab();
    boolean isSelectedTab = selectedTab == null || Stream.of(tabs).anyMatch(selectedTab::equals);

    doRemoveTabs(tabs);

    // Prevents changing the selected tab
    int newSelectedIndex = getSelectedIndex() - lowerIndices;

    // In case the last tab was removed
    if (newSelectedIndex > 0 && newSelectedIndex >= getComponentCount()) {
      newSelectedIndex = getComponentCount() - 1;
    }

    if (getComponentCount() == 0 || (isSelectedTab && !isAutoselect())) {
      newSelectedIndex = -1;
    }

    if (newSelectedIndex != getSelectedIndex()) {
      setSelectedIndex(newSelectedIndex);
    } else {
      updateSelectedTab(false);
    }
  }

  private void doRemoveTabs(Tab... tabs) {
    List<MenuItem> toRemove = new ArrayList<>(tabs.length);
    for (Tab tab : tabs) {
      Objects.requireNonNull(tab, "Tab to remove cannot be null");
      getMenuItem(tab).ifPresent(toRemove::add);
    }
    getContent().remove(toRemove.toArray(new MenuItem[0]));
  }

  /**
   * Removes all tabs from this component. It also removes the children that were added only at the
   * client-side.
   *
   * <p>This will reset the {@link #getSelectedIndex() selected index} to zero.
   */
  public void removeAll() {
    getElement().removeAllChildren();
    if (getSelectedIndex() > -1) {
      setSelectedIndex(-1);
    } else {
      updateSelectedTab(false);
    }
  }

  /**
   * Replaces the tab in the container with another one without changing position. This method
   * replaces tab with another one is such way that the new tab overtakes the position of the old
   * tab. If the old tab is not in the container, the new tab is added to the container. If the both
   * tabs are already in the container, their positions are swapped. Tab attach and detach events
   * should be taken care as with add and remove.
   *
   * @param oldTab the old tab that will be replaced. Can be <code>null</code>, which will make the
   *     newTab to be added to the layout without replacing any other
   * @param newTab the new tab to be replaced. Can be <code>null</code>, which will make the oldTab
   *     to be removed from the layout without adding any other
   *     <p>Replacing the currently selected tab will make the new tab selected.
   */
  public void replace(Tab oldTab, Tab newTab) {
    if (oldTab == null && newTab == null) {
      // NO-OP
    } else if (oldTab == null) {
      add(newTab);
    } else if (newTab == null) {
      remove(oldTab);
    } else {
      doReplace(oldTab, newTab);
    }
    updateSelectedTab(false);
  }

  private void doReplace(Tab oldTab, Tab newTab) {
    MenuItem oldItem = getMenuItem(oldTab).orElse(null);
    MenuItem newItem = getMenuItem(newTab).orElse(null);

    if (oldItem != null && newItem != null) {
      replaceTab(oldItem, newTab);
      replaceTab(newItem, oldTab);
    } else if (oldItem != null) {
      replaceTab(oldItem, newTab);
    } else {
      add(newTab);
    }
  }

  private void replaceTab(MenuItem item, Tab tab) {
    item.removeAll();
    item.add(tab);
  }

  /** An event to mark that the selected tab has changed. */
  public static class SelectedChangeEvent extends ComponentEvent<EnhancedTabs> {
    private final Tab selectedTab;
    private final Tab previousTab;
    private final boolean initialSelection;

    /**
     * Creates a new selected change event.
     *
     * @param source The tabs that fired the event.
     * @param previousTab The previous selected tab.
     * @param fromClient <code>true</code> for client-side events, <code>false</code> otherwise.
     */
    public SelectedChangeEvent(EnhancedTabs source, Tab previousTab, boolean fromClient) {
      super(source, fromClient);
      selectedTab = source.getSelectedTab();
      initialSelection = source.isAutoselect() && previousTab == null && !fromClient;
      this.previousTab = previousTab;
    }

    /**
     * Get selected tab for this event. Can be {@code null} when autoselect is set to false.
     *
     * @return the selected tab for this event
     */
    public Tab getSelectedTab() {
      return selectedTab;
    }

    /**
     * Get previous selected tab for this event. Can be {@code null} when autoselect is set to
     * false.
     *
     * @return the selected tab for this event
     */
    public Tab getPreviousTab() {
      return previousTab;
    }

    /**
     * Checks if this event is initial tabs selection.
     *
     * @return <code>true</code> if the event is initial tabs selection, <code>false</code>
     *     otherwise
     */
    public boolean isInitialSelection() {
      return initialSelection;
    }
  }

  /**
   * Adds a listener for {@link SelectedChangeEvent}.
   *
   * @param listener the listener to add, not <code>null</code>
   * @return a handle that can be used for removing the listener
   */
  public Registration addSelectedChangeListener(
      ComponentEventListener<SelectedChangeEvent> listener) {
    return addListener(SelectedChangeEvent.class, listener);
  }

  /**
   * Gets the zero-based index of the currently selected tab.
   *
   * @return the zero-based index of the selected tab, or -1 if none of the tabs is selected
   */
  public int getSelectedIndex() {
    return selectedIndex;
  }

  /**
   * Selects a tab based on its zero-based index.
   *
   * @param selectedIndex the zero-based index of the selected tab, -1 to unselect all
   */
  public void setSelectedIndex(int selectedIndex) {
    setSelectedIndex(selectedIndex, false);
  }

  private void setSelectedIndex(int selectedIndex, boolean changedFromClient) {
    this.selectedIndex = selectedIndex;
    updateSelectedTab(changedFromClient);
  }

  /**
   * Gets the currently selected tab.
   *
   * @return the selected tab, or {@code null} if none is selected
   */
  public Tab getSelectedTab() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex < 0) {
      return null;
    }
    return getTabAt(selectedIndex);
  }

  /**
   * Selects the given tab.
   *
   * @param selectedTab the tab to select, {@code null} to unselect all
   * @throws IllegalArgumentException if {@code selectedTab} is not a child of this component
   */
  public void setSelectedTab(Tab selectedTab) {
    setSelectedTab(selectedTab, false);
  }

  public void setSelectedTab(Tab selectedTab, boolean changedFromClient) {
    if (selectedTab == null) {
      setSelectedIndex(-1, changedFromClient);
      return;
    }

    int selectedIndex = indexOf(selectedTab);
    if (selectedIndex < 0) {
      throw new IllegalArgumentException("Tab to select must be a child: " + selectedTab);
    }
    setSelectedIndex(selectedIndex, changedFromClient);
  }

  /**
   * Specify that the tabs should be automatically selected. When autoselect is false, no tab will
   * be selected when the component load and it will not select any others tab when removing
   * currently selected tab. The default value is true.
   *
   * @param autoselect {@code true} to autoselect tab, {@code false} to not.
   */
  public void setAutoselect(boolean autoselect) {
    this.autoselect = autoselect;
  }

  /**
   * Gets whether the tabs should be automatically selected. The default value is true.
   *
   * @return <code>true</code> if autoselect is active, <code>false</code> otherwise
   * @see #setAutoselect(boolean)
   */
  public boolean isAutoselect() {
    return autoselect;
  }

  private void updateSelectedTab(boolean changedFromClient) {
    if (getSelectedIndex() < -1) {
      setSelectedIndex(-1);
      return;
    }

    Tab currentlySelected = getSelectedTab();
    Tab previousTab = selectedTab;

    if (Objects.equals(currentlySelected, selectedTab)) {
      return;
    }

    if (currentlySelected == null || currentlySelected.getElement().getNode().isEnabledSelf()) {
      selectedTab = currentlySelected;
      getTabs().forEach(tab -> tab.setSelected(false));

      if (selectedTab != null) {
        selectedTab.setSelected(true);
      }

      fireEvent(new SelectedChangeEvent(this, previousTab, changedFromClient));
    } else {
      updateEnabled(currentlySelected);
      setSelectedTab(selectedTab);
    }
  }

  private void updateEnabled(Tab tab) {
    boolean enabled = tab.getElement().getNode().isEnabledSelf();
    Serializable rawValue = tab.getElement().getPropertyRaw("disabled");
    if (rawValue instanceof Boolean) {
      // convert the boolean value to a String to force update the
      // property value. Otherwise since the provided value is the same as
      // the current one the update don't do anything.
      tab.getElement().setProperty("disabled", enabled ? null : Boolean.TRUE.toString());
    } else {
      tab.setEnabled(enabled);
    }
  }

  /**
   * Returns the index of the given tab.
   *
   * @param tab the tab to look up, can not be <code>null</code>
   * @return the index of the tab or -1 if the tab is not a child
   */
  public int indexOf(Tab tab) {
    if (tab == null) {
      throw new IllegalArgumentException("The 'tab' parameter cannot be null");
    }
    Iterator<Tab> it = getTabs().sequential().iterator();
    int index = 0;
    while (it.hasNext()) {
      Component next = it.next();
      if (tab.equals(next)) {
        return index;
      }
      index++;
    }
    return -1;
  }

  /**
   * Gets the number of children tabs.
   *
   * @return the number of tabs
   */
  public int getComponentCount() {
    return (int) getTabs().count();
  }

  /**
   * Returns the tab at the given position.
   *
   * @param index the position of the tab, must be greater than or equals to 0 and less than the
   *     number of children tabs
   * @return The tab at the given index
   * @throws IllegalArgumentException if the index is less than 0 or greater than or equals to the
   *     number of children tabs
   */
  public Tab getTabAt(int index) {
    if (index < 0) {
      throw new IllegalArgumentException(
          "The 'index' argument should be greater than or equal to 0. It was: " + index);
    }
    return getTabs()
        .sequential()
        .skip(index)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "The 'index' argument should not be greater than or equals to the number of children tabs. It was: "
                        + index));
  }

  public int getTabCount() {
    return (int) getTabs().count();
  }

  private Stream<Tab> getTabs() {
    return getContent().getItems().stream().map(EnhancedTabs::getTab).filter(Objects::nonNull);
  }

  private static Tab getTab(MenuItem item) {
    return (Tab) item.getChildren().findFirst().filter(Tab.class::isInstance).orElse(null);
  }

  private Optional<MenuItem> getMenuItem(Tab tab) {
    Component item = tab.getParent().orElse(null);
    if (!(item instanceof MenuItem)) {
      return Optional.empty();
    }

    if (item.getParent().orElse(null) == this) {
      return Optional.of((MenuItem) item);
    } else {
      throw new IllegalArgumentException("The given tab (" + tab + ") is not a child of this tab");
    }
  }
}
