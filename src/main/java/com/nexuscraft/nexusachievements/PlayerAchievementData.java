package com.nexuscraft.nexusachievements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** One player's in-memory achievement state: which ids are actually earned (with the epoch millis
 *  they earned it), plus the running count toward every GOAL-framed achievement's target. */
public final class PlayerAchievementData {

    private final Set<String> earned = new HashSet<>();
    private final Map<String, Long> earnedAt = new HashMap<>();
    private final Map<String, Integer> progress = new HashMap<>();

    public boolean hasEarned(String id) {
        return earned.contains(id);
    }

    public long earnedAt(String id) {
        return earnedAt.getOrDefault(id, 0L);
    }

    public void markEarned(String id, long whenMillis) {
        earned.add(id);
        earnedAt.put(id, whenMillis);
    }

    public void unmarkEarned(String id) {
        earned.remove(id);
        earnedAt.remove(id);
        progress.remove(id);
    }

    public int progress(String id) {
        return progress.getOrDefault(id, 0);
    }

    /** Adds one to this id's progress counter and returns the new total. */
    public int incrementProgress(String id) {
        int next = progress(id) + 1;
        progress.put(id, next);
        return next;
    }

    public Set<String> earnedIds() {
        return earned;
    }
}
