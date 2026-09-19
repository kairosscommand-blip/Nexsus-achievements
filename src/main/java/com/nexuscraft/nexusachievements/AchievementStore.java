package com.nexuscraft.nexusachievements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Deliberately not a database, same simplicity philosophy the rest of this no-network-dependency
 * plugin family already leans on -- a single flat, append-only log under the plugin's data
 * folder, replayed in full on startup. Every earn appends one {@code E|<uuid>|<id>|<epochMillis>}
 * line; every GOAL-achievement progress tick appends one {@code P|<uuid>|<id>|<count>} line (the
 * *last* P line for a given uuid+id wins on replay, so the file only ever grows, never needs an
 * in-place rewrite for the common case). A revoke is the one uncommon case that actually rewrites
 * the whole file from the current in-memory state, since correctly *removing* a line from an
 * append-only log means rewriting it anyway.
 */
public final class AchievementStore {

    private final File file;
    private final Logger logger;
    private final Map<UUID, PlayerAchievementData> cache = new HashMap<>();
    private BufferedWriter writer;

    public AchievementStore(File dataFolder, Logger logger) {
        this.file = new File(dataFolder, "playerdata.log");
        this.logger = logger;
    }

    public void load() {
        cache.clear();
        if (!file.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                applyLine(line);
            }
        } catch (IOException ex) {
            logger.warning("[NexusAchievements] Could not read playerdata.log -- starting with no "
                    + "saved achievement progress this boot: " + ex.getMessage());
        }
        openWriterForAppend();
    }

    private void applyLine(String line) {
        if (line.isBlank()) {
            return;
        }
        String[] parts = line.split("\\|", 4);
        if (parts.length != 4) {
            return;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(parts[1]);
        } catch (IllegalArgumentException ex) {
            return;
        }
        PlayerAchievementData data = cache.computeIfAbsent(uuid, u -> new PlayerAchievementData());
        String id = parts[2];
        switch (parts[0]) {
            case "E" -> {
                long when;
                try {
                    when = Long.parseLong(parts[3]);
                } catch (NumberFormatException ex) {
                    when = System.currentTimeMillis();
                }
                data.markEarned(id, when);
            }
            case "P" -> {
                try {
                    int count = Integer.parseInt(parts[3]);
                    while (data.progress(id) < count) {
                        data.incrementProgress(id);
                    }
                } catch (NumberFormatException ignored) {
                    // Malformed progress line -- skip it, same defensive stance as everywhere else.
                }
            }
            default -> {
                // Unknown record type (future format, or corruption) -- ignore, never fatal.
            }
        }
    }

    private void openWriterForAppend() {
        try {
            file.getParentFile().mkdirs();
            writer = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException ex) {
            logger.warning("[NexusAchievements] Could not open playerdata.log for writing -- "
                    + "achievement progress will not persist across a restart this boot: " + ex.getMessage());
            writer = null;
        }
    }

    public PlayerAchievementData dataFor(UUID uuid) {
        return cache.computeIfAbsent(uuid, u -> new PlayerAchievementData());
    }

    public void appendEarned(UUID uuid, String id, long whenMillis) {
        appendLine("E|" + uuid + "|" + id + "|" + whenMillis);
    }

    public void appendProgress(UUID uuid, String id, int newCount) {
        appendLine("P|" + uuid + "|" + id + "|" + newCount);
    }

    private void appendLine(String line) {
        if (writer == null) {
            return;
        }
        try {
            writer.write(line);
            writer.newLine();
            writer.flush();
        } catch (IOException ex) {
            logger.warning("[NexusAchievements] Failed writing to playerdata.log: " + ex.getMessage());
        }
    }

    /** The one operation that needs a real rewrite -- an admin revoking an achievement. Dumps
     *  every player currently cached in memory back out as a fresh set of E/P lines. Only players
     *  who have been online (or loaded) at least once this boot are in the cache; this is fine
     *  for a revoke, since it can only ever target a player whose data has already been touched. */
    public void rewriteAll() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // Best-effort close before the rewrite below.
            }
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file, false))) {
            for (Map.Entry<UUID, PlayerAchievementData> entry : cache.entrySet()) {
                PlayerAchievementData data = entry.getValue();
                for (String id : data.earnedIds()) {
                    out.write("E|" + entry.getKey() + "|" + id + "|" + data.earnedAt(id));
                    out.newLine();
                }
            }
        } catch (IOException ex) {
            logger.warning("[NexusAchievements] Failed rewriting playerdata.log after a revoke: "
                    + ex.getMessage());
        }
        openWriterForAppend();
    }

    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // Shutting down anyway.
            }
        }
    }
}
