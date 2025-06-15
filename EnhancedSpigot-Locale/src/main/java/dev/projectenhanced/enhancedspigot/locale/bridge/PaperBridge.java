package dev.projectenhanced.enhancedspigot.locale.bridge;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public class PaperBridge implements IPlatformBridge {
    @Override
    public void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        player.sendActionBar(component);
    }

    @Override
    public void sendTitle(Player player, Component title, Component subtitle) {
        player.showTitle(Title.title(title,subtitle));
    }

    @Override
    public void sendRestrictedMessage(String permission, Component component) {
        this.getRestrictedAudience(permission).forEach(aud -> aud.sendMessage(component));
    }

    @Override
    public void sendRestrictedActionBar(String permission, Component component) {
        this.getRestrictedAudience(permission).forEach(aud -> aud.sendActionBar(component));
    }

    @Override
    public void sendRestrictedTitle(String permission, Component title, Component subtitle) {
        this.getRestrictedAudience(permission).forEach(aud -> aud.showTitle(Title.title(title,subtitle)));
    }

    @Override
    public void sendMessageAll(Component component) {
        this.getAllAudience().forEach(aud -> aud.sendMessage(component));
    }

    @Override
    public void sendActionBarAll(Component component) {
        this.getAllAudience().forEach(aud -> aud.sendActionBar(component));
    }

    @Override
    public void sendTitleAll(Component title, Component subtitle) {
        this.getAllAudience().forEach(aud -> aud.showTitle(Title.title(title,subtitle)));
    }

    @Override
    public Audience getAudience(CommandSender sender) {
        return sender;
    }

    private Stream<Audience> getRestrictedAudience(String permission) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(permission))
                .map(p -> (Audience) p);
    }

    private Stream<Audience> getAllAudience() {
        return Bukkit.getOnlinePlayers().stream()
                .map(p -> (Audience) p);
    }

    @Override
    public void close() {}
}
