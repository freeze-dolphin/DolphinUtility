package io.freeze_dolphin.dolphin_utility.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.tomlj.Toml;

import io.freeze_dolphin.dolphin_utility.PlugEntry;
import io.freeze_dolphin.dolphin_utility.network.DownloadUtils;
import redempt.redlib.commandmanager.CommandHook;

public class CommandHookHandler {

	private PlugEntry plug;

	public CommandHookHandler(PlugEntry plug) {
		this.plug = plug;
	}

	@CommandHook("dolphin-util.fetch-url")
	public void fetch_url(CommandSender sender, String url, String targetPath) {
		new FetchUrlThread(this.plug, sender, url, targetPath).start();
	}

	private static class FetchUrlThread extends Thread {

		private static final String CONFIG_PREFIX = "features.fetch-url.";

		private CommandSender sender;
		private String url;
		private String targetPath;
		private PlugEntry plug;

		public FetchUrlThread(PlugEntry plug, CommandSender sender, String url, String targetPath) {
			this.url = url;
			this.plug = plug;
			this.sender = sender;
			this.targetPath = targetPath;
		}

		public void run() {
			File target = new File(targetPath.replaceAll("\\|", Matcher.quoteReplacement(File.separator)));
			if (target.isFile() && target.canRead() && target.canWrite()) {
				ConfigGetter cg = new ConfigGetter(this.plug);
				if (this.plug.config.contains(CONFIG_PREFIX + "limited-hosts")) {
					for (Object limited_host : this.plug.config.getArray(CONFIG_PREFIX + "limited-hosts").toList()) {
						if (url.matches("http.*\\/\\/" + limited_host + "\\/.*")) {
							sender.sendMessage(
									msg("This host is limited and you are not able to download files from it!"));
							return;
						}
					}
				}
				if (this.plug.config.contains(CONFIG_PREFIX + "max-volume")) {
					try {
						boolean volume_check = DownloadUtils.get_file_length(this.url, cg.get_ua()) > cg
								.get_max_volume();
						sender.sendMessage(msg("Checking file volume..."));
						if (volume_check) {
							sender.sendMessage(msg("This file is too large!"));
							return;
						}
					} catch (IOException e) {
						if (sender instanceof Player) {
							((Player) sender).sendMessage(msg(e.getMessage()));
						} else {
							e.printStackTrace();
						}
					}
				}

				{
					try {

						if (!target.exists()) {
							target.createNewFile();
							sender.sendMessage(msg("Target file does not exist, created an empty file."));
						}

						FileOutputStream fos = new FileOutputStream(target, false);
						sender.sendMessage(msg("Getting access to the target file..."));
						InputStream is = DownloadUtils.download_from(url, cg.get_ua(), cg.get_time_out());
						sender.sendMessage(msg("Downloading..."));
						fos.write(is.readAllBytes());
						sender.sendMessage(msg("Overwriting..."));
						fos.flush();
						fos.close();
						sender.sendMessage(msg(ChatColor.translateAlternateColorCodes('&',
								"&aSucceeded! &rYour file has been saved into: " + target.getPath())));
					} catch (Exception e) {
						if (sender instanceof Player) {
							((Player) sender)
									.sendMessage(msg("Unable to overwrite existed target file: " + target.getPath()));
						} else {
							e.printStackTrace();
						}
					}
				}

			} else {
				sender.sendMessage(msg("Invaild target path!"));
				return;
			}
		}

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

	public static String msg(String msg) {
		return "[" + PlugEntry.plug.getDescription().getPrefix() + "] " + msg;
	}

}
