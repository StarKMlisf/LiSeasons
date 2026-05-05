package com.liseasons.util;

import com.liseasons.LISeasonsPlugin;

public final class PlatformUtil {
    private static Boolean folia;

    private PlatformUtil() {
    }

    public static boolean isFolia(LISeasonsPlugin plugin) {
        Boolean cached = folia;
        if (cached != null) {
            return cached;
        }
        boolean detected = detectFolia(plugin);
        folia = detected;
        return detected;
    }

    private static boolean detectFolia(LISeasonsPlugin plugin) {
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
