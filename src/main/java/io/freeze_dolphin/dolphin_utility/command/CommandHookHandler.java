package io.freeze_dolphin.dolphin_utility.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomlj.Toml;

import io.freeze_dolphin.dolphin_utility.PlugEntry;
import io.freeze_dolphin.dolphin_utility.command.task.FetchUrlThread;

import redempt.redlib.commandmanager.CommandHook;

public class CommandHookHandler {

	private PlugEntry plug;

	public CommandHookHandler(PlugEntry plug) {
		this.plug = plug;
	}

	@CommandHook("dolphin-util.fetch-url")
	public void fetch_url(CommandSender sender, boolean ignore_check, String url, String target_path) {
		new FetchUrlThread(this.plug, sender, url, target_path, ignore_check).start();
	}

	@CommandHook("dolphin-util.reload-config")
	public void reload_config(CommandSender sender) {
		File config_file = new File(this.plug.getDataFolder() + File.separator + "config.toml");
		if (!config_file.exists()) {
			this.plug.saveResource("config.toml", true);
		}

		try {
			this.plug.config = Toml.parse(new FileInputStream(config_file));
			if (sender instanceof Player) {
				this.plug.config.errors().forEach(error -> ((Player) sender).sendMessage(msg(error.toString())));
			} else {
				this.plug.config.errors().forEach(error -> this.plug.getLogger().warning(error.toString()));
			}
			sender.sendMessage(msg(ChatColor.translateAlternateColorCodes('&', "&bFinished.")));
		} catch (IOException e) {
			if (sender instanceof Player) {
				((Player) sender).sendMessage(msg(e.getMessage()));
			} else {
				e.printStackTrace();
			}
		}
	}

	private static String msg(String msg) {
		return "[" + PlugEntry.plug.getDescription().getPrefix() + "] " + msg;
	}

}
