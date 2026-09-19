package com.nexuscraft.nexusachievements;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Config-driven: every {@code trigger-type: COMMAND} achievement in {@code config.yml} carries a
 * {@code trigger-value} that's the exact real command literal (e.g. {@code /house create}, taken
 * straight from that plugin's own documented usage -- see this plugin's README for the full list
 * and where each one came from). Fires the first time (or, for a GOAL-framed one, every time) a
 * player's chat input starts with that literal.
 *
 * <p>Honest limitation: {@link PlayerCommandPreprocessEvent} only sees that the player *typed* the
 * command, not whether the target plugin's own executor actually accepted it -- {@code /house
 * create} with a name that's already taken, say, would still count here even though NexusHouses
 * itself would reject it. Acceptable for what this is (a flavor achievement, not an audit log),
 * documented the same way every other "best-effort, not functionally verified" piece of this
 * plugin family is.
 */
public final class CommandTriggerListener implements Listener {

    private final AchievementManager manager;
    private final Map<String, List<AchievementDefinition>> byCommandPrefix = new HashMap<>();

    public CommandTriggerListener(AchievementManager manager) {
        this.manager = manager;
    }

    public void rebuild() {
        byCommandPrefix.clear();
        for (AchievementDefinition definition : manager.registry().all().values()) {
            if (definition.triggerType() != AchievementDefinition.TriggerType.COMMAND) {
                continue;
            }
            String prefix = definition.triggerValue().trim().toLowerCase();
            if (prefix.isEmpty()) {
                continue;
            }
            byCommandPrefix.computeIfAbsent(prefix, p -> new ArrayList<>()).add(definition);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (byCommandPrefix.isEmpty()) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        for (Map.Entry<String, List<AchievementDefinition>> entry : byCommandPrefix.entrySet()) {
            if (message.startsWith(entry.getKey())) {
                for (AchievementDefinition definition : entry.getValue()) {
                    manager.grant(event.getPlayer(), definition.id());
                }
            }
        }
    }
}
