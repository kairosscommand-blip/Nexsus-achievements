package com.nexuscraft.nexusachievements;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads every {@link AchievementDefinition} out of {@code config.yml}'s {@code achievements:}
 * list. A malformed entry -- an unknown {@code icon}/{@code frame}/{@code trigger-type}, a
 * missing {@code id}/{@code title} -- is skipped individually with a logged warning, never fatal
 * to the rest of the list. Same defensive stance every config parser in this plugin family takes
 * (WildcardConfig's event list, GuardianConfig's question bank, etc.).
 */
public final class AchievementRegistry {

    private final Map<String, AchievementDefinition> byId = new LinkedHashMap<>();
    private final Logger logger;

    public AchievementRegistry(Logger logger) {
        this.logger = logger;
    }

    public void load(FileConfiguration config) {
        byId.clear();
        for (Map<?, ?> raw : config.getMapList("achievements")) {
            AchievementDefinition definition = parseOne(raw);
            if (definition != null) {
                if (byId.containsKey(definition.id())) {
                    logger.warning("[NexusAchievements] Duplicate achievement id '" + definition.id()
                            + "' -- keeping the first one, skipping this later entry.");
                    continue;
                }
                byId.put(definition.id(), definition);
            }
        }
        logger.info("[NexusAchievements] Loaded " + byId.size() + " achievement definitions.");
    }

    private AchievementDefinition parseOne(Map<?, ?> raw) {
        String id = str(raw, "id", null);
        String title = str(raw, "title", null);
        if (id == null || id.isBlank() || title == null || title.isBlank()) {
            logger.warning("[NexusAchievements] Skipping an achievement entry missing 'id' or 'title'.");
            return null;
        }

        String sourcePlugin = str(raw, "source", "Nexus Core");
        String description = str(raw, "description", "");
        boolean hidden = bool(raw, "hidden", false);
        int goal = intVal(raw, "goal", 1);

        Material icon;
        try {
            icon = Material.valueOf(str(raw, "icon", "STONE").toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.warning("[NexusAchievements] Achievement '" + id + "' has an unknown icon material -- "
                    + "defaulting to STONE.");
            icon = Material.STONE;
        }

        FrameType frame;
        try {
            frame = FrameType.valueOf(str(raw, "frame", "TASK").toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.warning("[NexusAchievements] Achievement '" + id + "' has an unknown frame type -- "
                    + "defaulting to TASK.");
            frame = FrameType.TASK;
        }

        AchievementDefinition.TriggerType triggerType;
        try {
            triggerType = AchievementDefinition.TriggerType.valueOf(str(raw, "trigger-type", "API").toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.warning("[NexusAchievements] Achievement '" + id + "' has an unknown trigger-type -- "
                    + "defaulting to API (armed, needs a manual grant() call).");
            triggerType = AchievementDefinition.TriggerType.API;
        }
        String triggerValue = str(raw, "trigger-value", "");

        return new AchievementDefinition(id, sourcePlugin, title, description, icon, frame, hidden, goal,
                triggerType, triggerValue);
    }

    public AchievementDefinition get(String id) {
        return byId.get(id);
    }

    public Map<String, AchievementDefinition> all() {
        return byId;
    }

    private static String str(Map<?, ?> raw, String key, String def) {
        Object value = raw.get(key);
        return value == null ? def : String.valueOf(value);
    }

    private static boolean bool(Map<?, ?> raw, String key, boolean def) {
        Object value = raw.get(key);
        return value == null ? def : Boolean.parseBoolean(String.valueOf(value));
    }

    private static int intVal(Map<?, ?> raw, String key, int def) {
        Object value = raw.get(key);
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return def;
        }
    }
}
