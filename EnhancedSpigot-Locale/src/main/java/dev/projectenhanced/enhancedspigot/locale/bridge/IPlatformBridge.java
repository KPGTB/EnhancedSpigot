package dev.projectenhanced.enhancedspigot.locale.bridge;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface IPlatformBridge {
    void sendMessage(CommandSender sender, Component component);
    void sendActionBar(Player player, Component component);
    void sendTitle(Player player, Component title, Component subtitle);

    void sendRestrictedMessage(String permission, Component component);
    void sendRestrictedActionBar(String permission, Component component);
    void sendRestrictedTitle(String permission, Component title, Component subtitle);

    void sendMessageAll(Component component);
    void sendActionBarAll(Component component);
    void sendTitleAll(Component title, Component subtitle);

    Audience getAudience(CommandSender sender);

    void close();
}
