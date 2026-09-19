package com.nexuscraft.nexusachievements;

import com.nexuscraft.nexusachievements.api.NexusAchievementsApi;
import com.nexuscraft.nexusachievements.api.NexusAchievementsApiImpl;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class NexusAchievements extends JavaPlugin {

    private AchievementRegistry registry;
    private AchievementStore store;
    private ToastService toastService;
    private AchievementManager manager;
    private CommandTriggerListener commandTriggerListener;
    private ConsumeTriggerListener consumeTriggerListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registry = new AchievementRegistry(getLogger());
        registry.load(getConfig());

        store = new AchievementStore(getDataFolder(), getLogger());
        store.load();

        toastService = new ToastService();
        toastService.loadSettings(getConfig());

        manager = new AchievementManager(this, registry, store, toastService);

        commandTriggerListener = new CommandTriggerListener(manager);
        commandTriggerListener.rebuild();
        consumeTriggerListener = new ConsumeTriggerListener(manager);
        consumeTriggerListener.rebuild();

        getServer().getPluginManager().registerEvents(new VanillaMilestoneListener(manager), this);
        getServer().getPluginManager().registerEvents(commandTriggerListener, this);
        getServer().getPluginManager().registerEvents(consumeTriggerListener, this);

        PluginCommand command = getCommand("achievements");
        if (command != null) {
            AchievementCommand executor = new AchievementCommand(this, manager, this::reloadEverything);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getServer().getServicesManager().register(NexusAchievementsApi.class,
                new NexusAchievementsApiImpl(manager), this, ServicePriority.Normal);

        getLogger().info("[NexusAchievements] Enabled with " + registry.all().size()
                + " achievements defined across the Nexus family.");
    }

    @Override
    public void onDisable() {
        if (store != null) {
            store.close();
        }
        getServer().getServicesManager().unregisterAll(this);
    }

    private void reloadEverything() {
        reloadConfig();
        registry.load(getConfig());
        toastService.loadSettings(getConfig());
        commandTriggerListener.rebuild();
        consumeTriggerListener.rebuild();
    }
}
