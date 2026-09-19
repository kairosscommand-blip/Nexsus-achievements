package com.nexuscraft.nexusachievements.api;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Small, stable public surface other Nexus plugins can call into without NexusAchievements as a
 * compile dependency -- registered with Bukkit's ServicesManager on enable, same soft pattern
 * every cross-plugin surface in this project uses (see e.g. NexusHousesApi's own javadoc, which
 * documents the convention this follows):
 *
 *   ServicesManager services = Bukkit.getServer().getServicesManager();
 *   NexusAchievementsApi api = services.load(NexusAchievementsApi.class);
 *   if (api != null) {
 *       api.grant(player, "survival_first_cure");
 *   }
 *
 * A plugin without this interface on its own classpath can still reach it purely by reflection
 * (Class.forName("com.nexuscraft.nexusachievements.api.NexusAchievementsApi"), pull the
 * registration off the ServicesManager, invoke grant(Player, String) by Method.invoke) -- entirely
 * optional, entirely soft: if NexusAchievements isn't installed, {@code services.load(...)} just
 * returns null and a caller following the pattern above simply skips it.
 *
 * <p>This is genuinely the single most important integration point in this whole plugin. Every
 * achievement in the default config whose {@code trigger-type} is {@code API} (most of the
 * plugin-specific ones -- NexusSurvival's cure, NexusFamily's baby, NexusHouses' succession, and
 * so on; see the README for the full list) is fully defined -- id, title, description, icon,
 * frame, hidden flag -- and just waiting for exactly one line like the snippet above to be dropped
 * into that plugin's own real source the next time this project is looking at its actual code.
 */
public interface NexusAchievementsApi {

    /**
     * Advances the given achievement id by one for this player. For a simple one-shot achievement
     * this earns it (and shows the toast) immediately; for a GOAL-framed achievement with a
     * higher target, this just adds one tick of progress until the target is reached. Silently
     * does nothing if the id isn't a real registered achievement (logs a warning server-side so a
     * typo is easy to catch) or if the player already has it. Returns true only if this specific
     * call is the one that actually earned it.
     */
    boolean grant(Player player, String achievementId);

    boolean hasEarned(UUID playerId, String achievementId);

    /** Current progress toward a GOAL-framed achievement's target (0 if never started, or if the
     *  id isn't real/isn't a GOAL achievement). */
    int progress(UUID playerId, String achievementId);

    /** Total number of achievements currently defined in config -- useful for a caller that wants
     *  to show its own "x/N Nexus achievements earned" summary without needing the full registry. */
    int totalDefined();
}
