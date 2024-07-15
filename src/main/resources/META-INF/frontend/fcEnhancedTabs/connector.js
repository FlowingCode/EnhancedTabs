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

(function () { 
  window.Vaadin.Flow.fcEnhancedTabsConnector = {
	initLazy: tabs => {

		if (tabs.fcEnhancedTabsConnector) return;
		tabs.fcEnhancedTabsConnector = true;
		
		let close = tabs._close;
		
		tabs._subMenu.addEventListener('opened-changed',ev=>{
			if (ev.detail.value) {
				let submenu = tabs._subMenu;
				let overlay = submenu._overlayElement || submenu.$.overlay;
				if (overlay.hasAttribute('end-aligned')) {
					tabs._close = ()=>{};
					if (tabs._preventCloseTimeout) clearTimeout(tabs._preventCloseTimeout);
					tabs._preventCloseTimeout = setTimeout(()=>tabs._close = close, 500);
				}
				requestAnimationFrame(()=>{
					let selectedTab = overlay.querySelector('vaadin-tab[selected]');
					if (selectedTab) {
						requestAnimationFrame(()=>{
							selectedTab.scrollIntoViewIfNeeded ? selectedTab.scrollIntoViewIfNeeded() : selectedTab.scrollIntoView();
						});
					}
				});
			}
		});
		
		const __detectOverflow = tabs.__detectOverflow.bind(tabs);

		tabs.__detectOverflow = function() {
			//restore the normal order of buttons
			var buttons  = tabs._buttons;
			const selectedButton = buttons.find(e=>e._position!==undefined);
			if (selectedButton) {
				buttons[0].parentElement.insertBefore(selectedButton, buttons[selectedButton._position+1]);
				__detectOverflow();
				selectedButton._position=undefined;
			}
		
			__detectOverflow();
			
			// move the selected tab out of the overflow button
			buttons  = tabs._buttons;
			const selectedIndex = buttons.findIndex(e=>e.item.component && e.item.component.querySelector('vaadin-tab[selected]'));
			let overflowIndex  = buttons.findIndex(e=>e.style.visibility);
			while (selectedIndex>=overflowIndex && overflowIndex>=0 && buttons[selectedIndex].style.visibility) {
				buttons[0].parentElement.insertBefore(buttons[selectedIndex], buttons[overflowIndex--]);
				__detectOverflow();
				buttons[selectedIndex]._position = selectedIndex;
			}
		};
	
	}	
  }
})();
