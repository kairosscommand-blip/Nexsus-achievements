package com.nexuscraft.nexusachievements;

/**
 * Mirrors vanilla Minecraft's own three real advancement frame shapes -- not a NexusAchievements
 * invention. This is the whole reason {@link ToastService}'s real-toast path (see its own doc
 * comment) is worth the risk over any hand-drawn chat/title mimic: granting an actual, if
 * temporary, advancement of the matching frame type gets the *exact* vanilla header text, border
 * art, and color for free, straight from the client itself.
 */
public enum FrameType {

    /** Plain square frame, olive border. The vanilla default -- "you did a thing," no more. */
    TASK("Advancement Made!", "task"),

    /** Rounded frame, same olive border. Vanilla's own convention for a cumulative/counted goal
     *  rather than a one-off action -- used here for every GOAL-framed {@link AchievementDefinition}
     *  (a count that has to reach {@code goalTarget} before it actually earns). */
    GOAL("Goal Reached!", "goal"),

    /** Spiky frame, purple border, its own distinct toast sound. Vanilla reserves this for its
     *  genuinely hardest/rarest advancements -- used sparingly here for the same reason. */
    CHALLENGE("Challenge Complete!", "challenge");

    private final String toastHeader;
    private final String advancementFrameName;

    FrameType(String toastHeader, String advancementFrameName) {
        this.toastHeader = toastHeader;
        this.advancementFrameName = advancementFrameName;
    }

    /** The exact header text vanilla itself prints above the achievement title in the real toast. */
    public String toastHeader() {
        return toastHeader;
    }

    /** The real vanilla advancement JSON/NMS frame identifier ("task"/"goal"/"challenge") --
     *  what {@link ToastService} actually hands the reflection layer so the client renders the
     *  correct shape/color/sound on its own. */
    public String advancementFrameName() {
        return advancementFrameName;
    }
}
