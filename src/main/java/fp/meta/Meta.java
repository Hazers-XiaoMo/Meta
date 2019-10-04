package fp.meta;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.logging.Level;
import java.util.regex.Pattern;

public final class Meta extends JavaPlugin {

    private static Chat chat;

    public static void refresh(Player player) {
        //Refresh prefix suffix and tag
        String prefix = chat.getPlayerPrefix(player).replace('&', '§');
        String suffix = chat.getPlayerSuffix(player).replace('&', '§');
        String displayName = ((prefix.equals("") || Pattern.matches("(§[^§])*", prefix) ? prefix : prefix + " ") + player.getName() + (suffix.equals("") || Pattern.matches("(§[^§])*", suffix) ? suffix : suffix + " "));
        if (I.getConfig().getBoolean("change.name"))
            player.setDisplayName(displayName);
        if (I.getConfig().getBoolean("change.list"))
            player.setPlayerListName(displayName);
        if (I.getConfig().getBoolean("change.tag"))
            API.setTag(player, (prefix.equals("") || Pattern.matches("(§[^§])*", prefix) ? prefix : prefix + " "), (suffix.equals("") || Pattern.matches("(§[^§])*", suffix) ? suffix : " " + suffix));
    }

    //Instance
    public static Meta I;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        I = this;
        if (!setupChat()) {
            getLogger().log(Level.WARNING, "Vault Chat Hook Error! Disabling ...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onLeave(PlayerQuitEvent event) {
                API.unregisterTag(event.getPlayer());
            }

            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                refresh(event.getPlayer());
            }

            @EventHandler
            public void onChat(AsyncPlayerChatEvent event) {
                refresh(event.getPlayer());
            }
        }, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        API.unregisterAll();
        for (Player p : getServer().getOnlinePlayers()) {
            p.setPlayerListName(p.getName());
            p.setDisplayName(p.getName());
        }
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }
}

class API {
    public static void setTag(Player p, String prefix, String suffix) {
        if (prefix.length() > 16) {
            prefix = prefix.substring(0, 16);
        }
        if (suffix.length() > 16) {
            suffix = suffix.substring(0, 16);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            Team t = board.getTeam(p.getName());
            if (t == null) {
                t = board.registerNewTeam(p.getName());
                t.setPrefix(prefix);
                t.setSuffix(suffix);
                t.addPlayer(p);
            } else {
                t = board.getTeam(p.getName());
                t.setPrefix(prefix);
                t.setSuffix(suffix);
                t.addPlayer(p);
            }
        }
    }

    public static void unregisterTag(Player p) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Scoreboard board = p.getScoreboard();
            board.getPlayerTeam(player).unregister();
        }
    }

    public static void unregisterAll() {
        for (Player o : Bukkit.getOnlinePlayers()) {
            unregisterTag(o);
        }
    }

}