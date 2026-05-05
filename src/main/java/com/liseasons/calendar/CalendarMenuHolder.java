package com.liseasons.calendar;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class CalendarMenuHolder implements InventoryHolder {
    private final CalendarPage page;
    private Inventory inventory;

    public CalendarMenuHolder(CalendarPage page) {
        this.page = page;
    }

    public CalendarPage page() {
        return this.page;
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
