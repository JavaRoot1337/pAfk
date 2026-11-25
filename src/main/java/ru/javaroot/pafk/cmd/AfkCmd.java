package ru.javaroot.pafk.cmd;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import java.util.function.LongSupplier;
import org.bukkit.command.CommandSender;
import ru.javaroot.pafk.cfg.CfgLoad;
import ru.javaroot.pafk.plat.txt.Txt;
import ru.javaroot.pafk.sess.SessMgr;
import ru.javaroot.pafk.zone.Zones;

public final class AfkCmd {
    private static final String ADMIN_PERM = "afkzone.admin";

    private final CfgLoad cfgLoad;
    private final Zones zones;
    private final SessMgr sessions;
    private final LongSupplier reloadAction;

    public AfkCmd(CfgLoad cfgLoad, Zones zones, SessMgr sessions, LongSupplier reloadAction) {
        this.cfgLoad = cfgLoad;
        this.zones = zones;
        this.sessions = sessions;
        this.reloadAction = reloadAction;
    }

    public void register(Commands commands) {
        commands.register(
                Commands.literal("pafk")
                        .requires(src -> src.getSender().hasPermission(ADMIN_PERM))
                        .then(Commands.literal("reload").executes(ctx -> executeReload(ctx.getSource().getSender())))
                        .then(Commands.literal("status").executes(ctx -> executeStatus(ctx.getSource().getSender())))
                        .executes(ctx -> executeStatus(ctx.getSource().getSender()))
                        .build(),
                "Управление АФК-зонами"
        );

        commands.register(
                Commands.literal("afkreload")
                        .requires(src -> src.getSender().hasPermission(ADMIN_PERM))
                        .executes(ctx -> executeReload(ctx.getSource().getSender()))
                        .build(),
                "Перезагрузка конфига pAfk"
        );
    }

    private int executeReload(CommandSender snd) {
        if (!snd.hasPermission(ADMIN_PERM)) {
            snd.sendMessage(cfgLoad.cfg().noPermMsg());
            return Command.SINGLE_SUCCESS;
        }
        long ms = reloadAction.getAsLong();
        Txt.send(snd, cfgLoad.cfg().reloadSuccessRaw(), Txt.text("%ms%", ms));
        return Command.SINGLE_SUCCESS;
    }

    private int executeStatus(CommandSender snd) {
        if (!snd.hasPermission(ADMIN_PERM)) {
            snd.sendMessage(cfgLoad.cfg().noPermMsg());
            return Command.SINGLE_SUCCESS;
        }
        Zones.ZoneStatus st = zones.status();
        Txt.send(snd, "&6pAfk &7— активных сессий: &f%count%", Txt.text("%count%", sessions.count()));
        Txt.send(
                snd,
                "&7Мир: &f%world% &7(%w%), обычная: %norm%&7, премиум: %prem%",
                Txt.text("%world%", st.worldName()),
                Txt.color("%w%", st.worldLoaded() ? "&aок" : "&cнет"),
                Txt.color("%norm%", st.normalLoaded() ? "&aок" : "&cнет"),
                Txt.color("%prem%", st.premiumLoaded() ? "&aок" : "&cнет")
        );
        return Command.SINGLE_SUCCESS;
    }
}
