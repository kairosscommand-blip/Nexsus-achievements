package com.nexuscraft.nexusachievements;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Draws the actual on-screen "you earned something" popup. Per the brief this plugin was built
 * to: a real vanilla advancement toast is client-rendered from the game's own baked-in textures
 * and can only be triggered by handing the client a real (if temporary) advancement through
 * Minecraft's internal, unversioned, un-public NMS layer -- there is no supported Bukkit/Paper API
 * for it. Actually doing that means reflecting into internals that get restructured release to
 * release (this project's own README already has one real, confirmed example of exactly that kind
 * of churn biting a different plugin -- {@code Attribute.GENERIC_MAX_HEALTH} silently becoming
 * {@code Attribute.MAX_HEALTH}), and this sandbox has no real server to develop or verify that
 * kind of reflection against at all. Rather than ship a large block of unverifiable NMS-poking
 * that would either be quietly wrong or quietly do nothing, this deliberately stays out of the
 * real advancement engine entirely -- exactly what was asked for -- and instead builds the best
 * mimic achievable from stable, real, public Bukkit/Paper API: a big centered title flash using
 * vanilla's own real header wording ("Advancement Made!"/"Goal Reached!"/"Challenge Complete!"),
 * vanilla's own real toast sound, and a formatted chat card with the achievement's icon, title,
 * description, and source plugin. If a future pass wants to chase the real native toast, this is
 * the one place that would change -- everything else in this plugin (the registry, the store, the
 * triggers, the API) is completely independent of how the popup itself is drawn.
 */
public final class ToastService {

    private int fadeInTicks = 10;
    private int stayTicks = 70;
    private int fadeOutTicks = 20;
    private boolean playSound = true;
    private boolean broadcastChallengeTier = true;

    public void loadSettings(FileConfiguration config) {
        fadeInTicks = config.getInt("toast.title-fade-in-ticks", 10);
        stayTicks = config.getInt("toast.title-stay-ticks", 70);
        fadeOutTicks = config.getInt("toast.title-fade-out-ticks", 20);
        playSound = config.getBoolean("toast.play-sound", true);
        broadcastChallengeTier = config.getBoolean("toast.broadcast-challenge-tier", true);
    }

    public void showToast(Player player, AchievementDefinition definition) {
        FrameType frame = definition.frame();

        String headerColor = frame == FrameType.CHALLENGE ? "§d§l" : "§6§l";
        String title = headerColor + frame.toastHeader();
        String subtitle = "§f" + definition.title();
        player.sendTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);

        if (playSound) {
            Sound sound = frame == FrameType.CHALLENGE ? Sound.UI_TOAST_CHALLENGE_COMPLETE : Sound.UI_TOAST_IN;
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            if (frame == FrameType.CHALLENGE) {
                // A little extra oomph on the rarest tier -- "amplify the feeling of being rewarded."
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }

        sendCard(player, definition);

        if (broadcastChallengeTier && frame == FrameType.CHALLENGE) {
            String announcement = "§d§l* §r§f" + player.getName() + " §7just earned §d"
                    + definition.title() + "§7!";
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(announcement);
            }
        }
    }

    private void sendCard(Player player, AchievementDefinition definition) {
        String prettyIcon = prettyMaterialName(definition.icon().name());
        String frameColor = definition.frame() == FrameType.CHALLENGE ? "§d" : "§6";

        player.sendMessage("§8§m                                        ");
        player.sendMessage(frameColor + "§l" + definition.frame().toastHeader() + "  §8[" + prettyIcon + "]");
        player.sendMessage("§f§l" + definition.title());
        if (!definition.description().isBlank()) {
            player.sendMessage("§7" + definition.description());
        }
        player.sendMessage("§8" + definition.sourcePlugin());
        player.sendMessage("§8§m                                        ");
    }

    private static String prettyMaterialName(String materialName) {
        String[] words = materialName.split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return ChatColor.translateAlternateColorCodes('&', out.toString());
    }
}
