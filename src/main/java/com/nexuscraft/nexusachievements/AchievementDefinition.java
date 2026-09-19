package com.nexuscraft.nexusachievements;

import org.bukkit.Material;

/**
 * One entry from {@code config.yml}'s {@code achievements:} list -- purely data, no behavior.
 * What actually grants it is one of the listeners in this package (or another Nexus plugin
 * calling {@link com.nexuscraft.nexusachievements.api.NexusAchievementsApi#grant}); a definition
 * on its own is just the toast's contents plus how it's tracked.
 */
public final class AchievementDefinition {

    private final String id;
    private final String sourcePlugin;
    private final String title;
    private final String description;
    private final Material icon;
    private final FrameType frame;
    private final boolean hidden;
    private final int goalTarget;
    private final TriggerType triggerType;
    private final String triggerValue;

    public AchievementDefinition(String id, String sourcePlugin, String title, String description,
                                  Material icon, FrameType frame, boolean hidden, int goalTarget,
                                  TriggerType triggerType, String triggerValue) {
        this.id = id;
        this.sourcePlugin = sourcePlugin;
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.frame = frame;
        this.hidden = hidden;
        this.goalTarget = Math.max(1, goalTarget);
        this.triggerType = triggerType;
        this.triggerValue = triggerValue;
    }

    public String id() {
        return id;
    }

    /** Display label for which Nexus plugin this achievement belongs to (e.g. "NexusSurvival",
     *  or "Nexus Core" for the handful tied to plain vanilla milestones rather than another
     *  plugin's own mechanic). Purely cosmetic, shown in {@code /achievements info}. */
    public String sourcePlugin() {
        return sourcePlugin;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public Material icon() {
        return icon;
    }

    public FrameType frame() {
        return frame;
    }

    /** If true, shows as "???" in {@code /achievements list} until actually earned -- matches
     *  vanilla's own convention for its more surprising advancements. */
    public boolean hidden() {
        return hidden;
    }

    /** 1 for a simple one-shot achievement; greater than 1 for a GOAL-framed achievement that
     *  needs this many {@link com.nexuscraft.nexusachievements.api.NexusAchievementsApi#grant}
     *  calls (or internal listener fires) before it actually earns and shows a toast. */
    public int goalTarget() {
        return goalTarget;
    }

    public TriggerType triggerType() {
        return triggerType;
    }

    /** Meaning depends on {@link #triggerType()}: the literal command prefix for
     *  {@code COMMAND}, a comma-separated list of item-display-name substrings for
     *  {@code CONSUME}, an internal hard-coded name for {@code VANILLA}, or unused (empty) for
     *  {@code API}. */
    public String triggerValue() {
        return triggerValue;
    }

    /**
     * How an achievement actually gets granted. {@code COMMAND} and {@code CONSUME} are
     * config-tunable and always live. {@code VANILLA} is wired by hand in
     * {@link VanillaMilestoneListener} against real, certain vanilla events. {@code API} means
     * nothing in this plugin fires it yet -- it's armed and waiting for a single
     * {@code NexusAchievementsApi.grant(player, id)} call to be dropped into the source plugin's
     * own real code the next time this project is looking at that plugin's actual source; see
     * this plugin's README for the full list of which achievements are which.
     */
    public enum TriggerType {
        VANILLA,
        COMMAND,
        CONSUME,
        API
    }
}
