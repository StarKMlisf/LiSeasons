package com.liseasons.integration.placeholderapi;

import com.liseasons.LISeasonsPlugin;

public final class PlaceholderApiHook {
    private PlaceholderApiHook() {
    }

    public static void tryRegister(LISeasonsPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        try {
            Class<?> expansionClass = Class.forName("com.liseasons.integration.placeholderapi.LISeasonsPlaceholderExpansion");
            Object expansion = expansionClass.getConstructor(LISeasonsPlugin.class).newInstance(plugin);
            expansionClass.getMethod("register").invoke(expansion);
            plugin.getLogger().info("PlaceholderAPI 占位符已注册。");
        } catch (ReflectiveOperationException | LinkageError ex) {
            plugin.getLogger().warning("PlaceholderAPI 占位符注册失败: " + ex.getMessage());
        }
    }
}
