package io.freeze_dolphin.dolphin_utility.command;

import io.freeze_dolphin.dolphin_utility.PlugEntry;
import io.freeze_dolphin.dolphin_utility.command.task.FetchUrlThread;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tomlj.Toml;
import redempt.redlib.commandmanager.CommandHook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static redempt.redlib.misc.FormatUtils.color;

public class CommandBus {

    private final PlugEntry plug;

    public CommandBus(PlugEntry plug) {
        this.plug = plug;
    }

    @CommandHook("fetch")
    public void fetch_url(CommandSender sender, boolean ignore_check, String url, String target_path) {
        new FetchUrlThread(this.plug, sender, url, target_path, ignore_check).start();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @CommandHook("dump")
    public void dump(CommandSender sender) {
        Player plr = (Player) sender;
        File dumpdir = new File(plug.getDataFolder() + File.separator + "dumped_items");
        if (!dumpdir.exists()) dumpdir.mkdirs();
        ItemStack itm = plr.getInventory().getItemInMainHand();
        File dumpfile = new File(dumpdir.getPath() + File.separator + "dumped_item_" + System.currentTimeMillis() + ".yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dumpfile);
        yml.set("inf", itm);
        try {
            yml.save(dumpfile);
        } catch (IOException e) {
            sender.sendMessage(msg(color("&cError occurred while dumping item.")));
        }
    }

    @CommandHook("reload")
    public void reload_config(CommandSender sender) {
        File config_file = new File(this.plug.getDataFolder() + File.separator + "config.toml");
        if (!config_file.exists()) {
            this.plug.saveResource("config.toml", true);
        }

        try {
            this.plug.config = Toml.parse(new FileInputStream(config_file));
            if (sender instanceof Player) {
                this.plug.config.errors().forEach(error -> sender.sendMessage(msg(error.toString())));
            } else {
                this.plug.config.errors().forEach(error -> this.plug.getLogger().warning(error.toString()));
            }
            sender.sendMessage(msg(color("&bFinished.")));
        } catch (IOException e) {
            if (sender instanceof Player) {
                sender.sendMessage(msg(e.getMessage()));
            } else {
                e.printStackTrace();
            }
        }
    }

    private static String msg(String msg) {
        return "[" + PlugEntry.plug.getDescription().getPrefix() + "] " + msg;
    }

}
