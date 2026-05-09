package com.liseasons.calendar;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class CalendarMenuHolder implements InventoryHolder {
    private final CalendarPage page;
    private final int pageIndex;
    private Inventory inventory;

    public CalendarMenuHolder(CalendarPage page) {
        this(page, 0);
    }

    public CalendarMenuHolder(CalendarPage page, int pageIndex) {
        this.page = page;
        this.pageIndex = Math.max(0, pageIndex);
    }

    public CalendarPage page() {
        return this.page;
    }

    public int pageIndex() {
        return this.pageIndex;
    }

    public void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (this.inventory == null) {
            throw new IllegalStateException("日历菜单容器尚未绑定库存实例");
        }
        return this.inventory;
    }
}
