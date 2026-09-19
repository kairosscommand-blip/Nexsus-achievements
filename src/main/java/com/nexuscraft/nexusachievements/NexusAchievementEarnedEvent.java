package com.nexuscraft.nexusachievements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired the moment any achievement is actually earned (not on every progress tick toward a GOAL
 * achievement -- only the tick that crosses the goal). Purely a notification, same soft
 * cross-plugin pattern as everywhere else in this project: nothing has to listen for it, nothing
 * breaks if nothing does. A future Nexus plugin could hook this to, say, broadcast a server-wide
 * chat message for CHALLENGE-tier earns, without NexusAchievements needing to know that plugin
 * exists.
 *
 * <p>Confirmed against a real Paper build: real {@code org.bukkit.event.Event} declares
 * {@code getHandlers()} as abstract, backed by a static {@link HandlerList} every custom event
 * subclass has to provide itself (both the instance override and the static
 * {@code getHandlerList()} Bukkit's own event-registration reflection looks for by convention) --
 * this hand-written stub library's {@code Event} never modeled that machinery, since no earlier
 * plugin in this family had defined its own brand-new custom event before. Fixed here; if any
 * other custom event ever gets added to this family, it needs the same two members.
 */
public final class NexusAchievementEarnedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /** Static mirror of {@link #getHandlers()} -- Bukkit's own plugin manager looks this up by
     *  reflection when registering a listener method for this event type, per the real Bukkit
     *  custom-event convention. */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
