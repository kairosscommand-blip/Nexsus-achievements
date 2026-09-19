package com.nexuscraft.nexusachievements;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * The "Nexus Core" achievements -- the handful tied to plain vanilla milestones rather than
 * another Nexus plugin's own mechanic. These need zero soft-dependency, zero config-tunable
 * matching, and zero uncertainty: they're wired directly against real, stable, well-understood
 * Bukkit events, so they're live the moment this plugin is installed on its own.
 *
 * <p>The achievement ids referenced here ({@code core_first_blood}, {@code core_dragonslayer}, etc.)
 * must exist in {@code config.yml}'s default achievement list with {@code trigger-type: VANILLA}
 * -- this listener only ever calls {@link AchievementManager#grant}, it never invents a
 * definition. If an id below isn't in the registry (e.g. an admin deleted it from their config),
 * {@link AchievementManager#grant} just logs a warning and no-ops; nothing here breaks.
 */
public final class VanillaMilestoneListener implements Listener {

    private final AchievementManager manager;

    public VanillaMilestoneListener(AchievementManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) {
            return;
        }

        manager.grant(killer, "core_first_blood");

        // Real Bukkit's EntityDeathEvent doesn't expose the entity's own EntityType directly on
        // every version the same way -- LivingEntity does carry it via getType() on the real API.
        // This stub tree's Entity/LivingEntity model that the same way other plugins in this
        // family already rely on (see Mob#setType usage elsewhere).
        EntityType type = dead.getType();
        if (type == EntityType.ENDER_DRAGON) {
            manager.grant(killer, "core_dragonslayer");
        } else if (type == EntityType.WITHER) {
            manager.grant(killer, "core_witherslayer");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        manager.grant(event.getPlayer(), "core_not_today");
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        int level = event.getNewLevel();
        if (level >= 30) {
            manager.grant(event.getPlayer(), "core_seasoned");
        }
        if (level >= 100) {
            manager.grant(event.getPlayer(), "core_legendary");
        }
    }
}
