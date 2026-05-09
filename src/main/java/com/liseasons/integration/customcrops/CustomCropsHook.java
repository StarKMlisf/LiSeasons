package com.liseasons.integration.customcrops;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.bukkit.World;

public final class CustomCropsHook {
    private static boolean registered;

    private CustomCropsHook() {
    }

    public static void tryRegister(LISeasonsPlugin plugin) {
        if (registered || !plugin.getLiConfig().customCropsEnabled()) {
            return;
        }
        if (plugin.getServer().getPluginManager().getPlugin("CustomCrops") == null) {
            return;
        }

        try {
            ClassLoader classLoader = plugin.getServer().getPluginManager().getPlugin("CustomCrops").getClass().getClassLoader();
            Class<?> providerInterface = Class.forName("net.momirealms.customcrops.api.integration.SeasonProvider", true, classLoader);
            Class<?> pluginClass = Class.forName("net.momirealms.customcrops.api.BukkitCustomCropsPlugin", true, classLoader);
            Class<?> targetSeasonEnum = Class.forName("net.momirealms.customcrops.api.core.world.Season", true, classLoader);

            Object provider = Proxy.newProxyInstance(
                    classLoader,
                    new Class<?>[]{providerInterface},
                    new ProviderHandler(plugin, targetSeasonEnum)
            );

            Method getInstance = pluginClass.getMethod("getInstance");
            Object customCrops = getInstance.invoke(null);
            Method getIntegrationManager = pluginClass.getMethod("getIntegrationManager");
            Object integrationManager = getIntegrationManager.invoke(customCrops);
            Method registerSeasonProvider = integrationManager.getClass().getMethod("registerSeasonProvider", providerInterface);
            registerSeasonProvider.invoke(integrationManager, provider);

            plugin.getLogger().info("已接入 CustomCrops 季节提供器。");
            registered = true;
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().warning("接入 CustomCrops 失败: " + ex.getMessage());
        }
    }

    private static final class ProviderHandler implements InvocationHandler {
        private final LISeasonsPlugin plugin;
        private final Class<?> targetSeasonEnum;

        private ProviderHandler(LISeasonsPlugin plugin, Class<?> targetSeasonEnum) {
            this.plugin = plugin;
            this.targetSeasonEnum = targetSeasonEnum;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("identifier".equals(name)) {
                return "LISeasons";
            }
            if ("getSeason".equals(name) && args != null && args.length == 1 && args[0] instanceof World world) {
                SeasonState state = this.plugin.getSeasonManager().getState(world);
                Season season = state == null ? Season.SPRING : state.season();
                @SuppressWarnings({"unchecked", "rawtypes"})
                Object enumValue = Enum.valueOf((Class<? extends Enum>) this.targetSeasonEnum.asSubclass(Enum.class), season.name());
                return enumValue;
            }
            if ("toString".equals(name)) {
                return "LISeasonsCustomCropsProvider";
            }
            return null;
        }
    }
}
