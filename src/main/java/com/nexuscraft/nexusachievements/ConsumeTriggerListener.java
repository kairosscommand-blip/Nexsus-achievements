package com.nexuscraft.nexusachievements;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Config-driven: every {@code trigger-type: CONSUME} achievement's {@code trigger-value} is a
 * comma-separated list of substrings to match (case-insensitively) against the display name of
 * whatever item a player just finished eating/drinking. Falls back to matching the item's plain
 * Material name if it has no custom display name at all.
 *
 * <p>Honest limitation, same as this plugin's README spells out: for an achievement like "Clean
 * Bill of Health" (NexusSurvival's disease cure), the exact real display name of that plugin's
 * cure item was never re-confirmed against its actual source this pass -- {@code trigger-value}
 * ships with a best-guess set of likely names ({@code Cure,Antidote,Remedy,Medicine}). If none of
 * them match the real item, this achievement simply never fires (fails silently, exactly like an
 * unmatched command prefix would) until the value is corrected in config -- it can never fire on
 * the *wrong* thing, only fail to fire on the right one.
 */
public final class ConsumeTriggerListener implements Listener {

    private final AchievementManager manager;
    private final List<AchievementDefinition> consumeTriggered = new ArrayList<>();

    public ConsumeTriggerListener(AchievementManager manager) {
        this.manager = manager;
    }

    public void rebuild() {
        consumeTriggered.clear();
        for (AchievementDefinition definition : manager.registry().all().values()) {
            if (definition.triggerType() == AchievementDefinition.TriggerType.CONSUME
                    && !definition.triggerValue().isBlank()) {
                consumeTriggered.add(definition);
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (consumeTriggered.isEmpty()) {
            return;
        }
        String name = displayNameOf(event.getItem()).toLowerCase();
        for (AchievementDefinition definition : consumeTriggered) {
            for (String candidate : definition.triggerValue().split(",")) {
                String trimmed = candidate.trim().toLowerCase();
                if (!trimmed.isEmpty() && name.contains(trimmed)) {
                    manager.grant(event.getPlayer(), definition.id());
                    break;
                }
            }
        }
    }

    private static String displayNameOf(ItemStack item) {
        if (item == null) {
            return "";
        }
        if (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace('_', ' ');
    }
}
