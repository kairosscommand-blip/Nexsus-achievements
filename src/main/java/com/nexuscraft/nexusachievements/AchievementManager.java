package com.nexuscraft.nexusachievements;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * The single real entry point every trigger (vanilla listener, command listener, consume
 * listener, or another plugin through {@link com.nexuscraft.nexusachievements.api.NexusAchievementsApi})
 * goes through to actually award something. Handles the GOAL-progress counting, de-dupes an
 * already-earned achievement, persists the result, fires the toast, and fires
 * {@link NexusAchievementEarnedEvent} for anything else that wants to react.
 */
public final class AchievementManager {

    private final JavaPlugin plugin;
    private final AchievementRegistry registry;
    private final AchievementStore store;
    private final ToastService toastService;

    public AchievementManager(JavaPlugin plugin, AchievementRegistry registry, AchievementStore store,
                               ToastService toastService) {
        this.plugin = plugin;
        this.registry = registry;
        this.store = store;
        this.toastService = toastService;
    }

    /**
     * Advances the given achievement by one for this player. For a simple (goal target 1)
     * achievement this earns it outright the first time. For a GOAL-framed achievement with a
     * higher target, this only earns (and shows a toast) once progress reaches that target --
     * every call before then just quietly persists the new count. Does nothing if the id isn't a
     * real registered achievement, or if the player already has it. Returns true if this specific
     * call is the one that actually earned it.
     */
    public boolean grant(Player player, String id) {
        AchievementDefinition definition = registry.get(id);
        if (definition == null) {
            plugin.getLogger().warning("[NexusAchievements] Something tried to grant unknown achievement id '"
                    + id + "' -- ignoring. Check it's spelled exactly as in config.yml.");
            return false;
        }

        UUID uuid = player.getUniqueId();
        PlayerAchievementData data = store.dataFor(uuid);
        if (data.hasEarned(id)) {
            return false;
        }

        int newCount = data.incrementProgress(id);
        store.appendProgress(uuid, id, newCount);

        if (newCount < definition.goalTarget()) {
            return false;
        }

        long now = System.currentTimeMillis();
        data.markEarned(id, now);
        store.appendEarned(uuid, id, now);

        toastService.showToast(player, definition);
        plugin.getServer().getPluginManager().callEvent(new NexusAchievementEarnedEvent(player, definition));
        return true;
    }

    public boolean hasEarned(UUID uuid, String id) {
        return store.dataFor(uuid).hasEarned(id);
    }

    public int progress(UUID uuid, String id) {
        return store.dataFor(uuid).progress(id);
    }

    /** Admin-only correction tool -- not part of the normal earning flow. */
    public boolean revoke(UUID uuid, String id) {
        PlayerAchievementData data = store.dataFor(uuid);
        if (!data.hasEarned(id)) {
            return false;
        }
        data.unmarkEarned(id);
        store.rewriteAll();
        return true;
    }

    public AchievementRegistry registry() {
        return registry;
    }

    public AchievementStore store() {
        return store;
    }
}
