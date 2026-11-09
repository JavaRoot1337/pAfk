package org.flaudz122.pafk.cmd;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.flaudz122.pafk.PAFK;

public final class AfkReload implements CommandExecutor, TabCompleter {
    private final PAFK plugin;

    public AfkReload(PAFK plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("afkzone.admin")) {
            this.plugin.reloadPlugin();
            sender.sendMessage(this.plugin.getCfg().getReloadSuccessMessage());
        } else {
            sender.sendMessage(this.plugin.getCfg().getNoPermissionMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
