package com.liseasons.util;

import com.liseasons.LISeasonsPlugin;

public final class PlatformUtil {
    private PlatformUtil() {
    }

    public static boolean isFolia(LISeasonsPlugin plugin) {
        String serverName = plugin.getServer().getName();
        if (serverName != null && serverName.toLowerCase(java.util.Locale.ROOT).contains("folia")) {
            return true;
        }
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
