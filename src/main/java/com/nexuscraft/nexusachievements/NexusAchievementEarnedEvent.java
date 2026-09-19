package com.nexuscraft.nexusachievements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Fired the moment any achievement is actually earned (not on every progress tick toward a GOAL
 * achievement -- only the tick that crosses the goal). Purely a notification, same soft
 * cross-plugin pattern as everywhere else in this project: nothing has to listen for it, nothing
 * breaks if nothing does. A future Nexus plugin could hook this to, say, broadcast a server-wide
 * chat message for CHALLENGE-tier earns, without NexusAchievements needing to know that plugin
 * exists.
 */
public final class NexusAchievementEarnedEvent extends Event {

    private final Player player;
    private final AchievementDefinition definition;

    public NexusAchievementEarnedEvent(Player player, AchievementDefinition definition) {
        this.player = player;
        this.definition = definition;
    }

    public Player getPlayer() {
        return player;
    }

    public AchievementDefinition getDefinition() {
        return definition;
    }
}
