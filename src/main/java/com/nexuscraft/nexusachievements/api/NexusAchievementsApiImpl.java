package com.nexuscraft.nexusachievements.api;

import com.nexuscraft.nexusachievements.AchievementManager;
import org.bukkit.entity.Player;

import java.util.UUID;

/** Thin wrapper handing the public {@link NexusAchievementsApi} surface off to the real internal
 *  {@link AchievementManager} -- the same manager every internal listener in this plugin already
 *  goes through, so an external caller and this plugin's own listeners behave identically. */
public final class NexusAchievementsApiImpl implements NexusAchievementsApi {

    private final AchievementManager manager;

    public NexusAchievementsApiImpl(AchievementManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean grant(Player player, String achievementId) {
        return manager.grant(player, achievementId);
    }

    @Override
    public boolean hasEarned(UUID playerId, String achievementId) {
        return manager.hasEarned(playerId, achievementId);
    }

    @Override
    public int progress(UUID playerId, String achievementId) {
        return manager.progress(playerId, achievementId);
    }

    @Override
    public int totalDefined() {
        return manager.registry().all().size();
    }
}
