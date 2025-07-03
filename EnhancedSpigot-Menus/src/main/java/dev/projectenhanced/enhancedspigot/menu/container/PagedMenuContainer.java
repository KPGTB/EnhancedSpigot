/*
 * Copyright 2025 KPG-TB
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.projectenhanced.enhancedspigot.menu.container;

import dev.projectenhanced.enhancedspigot.menu.EnhancedMenu;
import dev.projectenhanced.enhancedspigot.menu.item.MenuItem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Paged menu container contains specific box of items from GUI with pages system
 */
@Getter public class PagedMenuContainer extends MenuContainer {
	public static final int UNLIMITED_PAGES = Integer.MAX_VALUE;

	private final List<MenuContainer> containers;
	private int page;

	/**
	 * Constructor for container that is part of GUI
	 *
	 * @param menu   Instance of KGui
	 * @param x      X position in KGui (0-8)
	 * @param y      Y position in KGui (0-[KGui rows - 1])
	 * @param width  Width of container (1-9)
	 * @param height Height of container (1-[KGui rows])
	 */
	public PagedMenuContainer(EnhancedMenu menu, int x, int y, int width, int height) {
		super(menu, x, y, width, height);
		this.containers = new ArrayList<>();
		this.page = 0;
	}

	/**
	 * Add page to container
	 *
	 * @param container Container that is part of PagedMenuContainer
	 */
	public void addPage(MenuContainer container) {
		if (container.getPagedMenuContainer() != this) {
			throw new IllegalStateException(
				"Container is not a part of this paged container!");
		}
		this.containers.add(container);
		// If this is a first container then update it
		if (this.containers.size() == 1) {
			update();
		}
	}

	/**
	 * Remove page from container
	 *
	 * @param container Container that is part of PagedMenuContainer
	 */
	public void removePage(MenuContainer container) {
		this.containers.remove(container);
		if (this.page >= this.containers.size()) {
			this.page = this.containers.size() - 1;
			if (this.page < 0) {
				this.page = 0;
			}
		}
	}

	/**
	 * Clear packages from container
	 */
	public void clearPages() {
		this.containers.clear();
		this.page = 0;
	}

	/**
	 * Update items in paged container
	 */
	public void update() {
		super.getItems()
			.clear();
		super.getItems()
			.putAll(this.containers.get(page)
				.getItems());
		super.getMenu()
			.update();
	}

	/**
	 * Set page index and auto update container
	 *
	 * @param page new page index
	 */
	public void setPage(int page) {
		this.page = page;
		this.update();
	}

	/**
	 * Change page to previous
	 */
	public void previousPage() {
		if (page > 0) {
			setPage(page - 1);
		}
	}

	/**
	 * Change page to next
	 */
	public void nextPage() {
		if ((this.containers.size() - 1) > page) {
			setPage(page + 1);
		}
	}

	/**
	 * Fill items using patter
	 *
	 * @param items   List of GuiItems
	 * @param pattern Array of lines. One string represents one line of container. Use space to disable slot. Use any other char to enable slot.
	 */
	public void fillPatternWithItems(List<MenuItem> items, String... pattern) {
		fillPatternWithItems(items, UNLIMITED_PAGES, pattern);
	}

	/**
	 * Fill items using patter
	 *
	 * @param items      List of GuiItems
	 * @param pagesLimit Limit of pages
	 * @param pattern    Array of lines. One string represents one line of container. Use space to disable slot. Use any other char to enable slot.
	 */
	public void fillPatternWithItems(List<MenuItem> items, int pagesLimit, String... pattern) {
		clearPages();
		List<MenuContainer> newPages = new ArrayList<>();
		newPages.add(new MenuContainer(this));

		int x = -1;
		int y = 0;

		for (MenuItem item : items) {
			do {
				x++;
				if (x >= this.getWidth() || x >= pattern[y].length()) {
					x = 0;
					y += 1;
				}
				if (y >= this.getHeight() || y >= pattern.length) {
					x = 0;
					y = 0;
					if (pagesLimit > newPages.size()) {
						newPages.add(new MenuContainer(this));
					} else {
						break;
					}
				}
			} while (pattern[y].charAt(x) == ' ');

			MenuContainer lastPage = newPages.get(newPages.size() - 1);
			lastPage.setItem(x, y, item);
		}

		newPages.forEach(this::addPage);
	}

	/**
	 * Fill paged container with items
	 *
	 * @param items      List of GuiItems
	 * @param pagesLimit Limit of pages
	 * @param startX     start X
	 * @param startY     start Y
	 * @param offsetX    x offset
	 * @param offsetY    y offset
	 */
	public void fillWithItems(List<MenuItem> items, int pagesLimit, int startX, int startY, int offsetX, int offsetY) {
		clearPages();
		List<MenuContainer> newPages = new ArrayList<>();
		newPages.add(new MenuContainer(this));

		int x = startX;
		int y = startY;

		for (MenuItem item : items) {
			if (x >= this.getWidth()) {
				x = startX;
				y += offsetY;
			}
			if (y >= this.getHeight()) {
				x = startX;
				y = startY;
				if (pagesLimit > newPages.size()) {
					newPages.add(new MenuContainer(this));
				} else {
					break;
				}
			}
			MenuContainer lastPage = newPages.get(newPages.size() - 1);
			lastPage.setItem(x, y, item);

			x += offsetX;
		}

		newPages.forEach(this::addPage);
	}

	/**
	 * Fill paged container with items
	 *
	 * @param items      List of GuiItems
	 * @param pagesLimit Limit of pages
	 * @since 2.0.0
	 */
	public void fillWithItems(List<MenuItem> items, int pagesLimit) {
		this.fillWithItems(items, pagesLimit, 0, 0, 1, 1);
	}

	/**
	 * Fill paged container with items
	 *
	 * @param items List of GuiItems
	 * @since 2.0.0
	 */
	public void fillWithItems(List<MenuItem> items) {
		this.fillWithItems(items, UNLIMITED_PAGES, 0, 0, 1, 1);
	}
}
