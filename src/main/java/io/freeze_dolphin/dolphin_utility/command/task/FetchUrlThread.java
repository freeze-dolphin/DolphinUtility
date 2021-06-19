package io.freeze_dolphin.dolphin_utility.command.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.freeze_dolphin.dolphin_utility.PlugEntry;
import io.freeze_dolphin.dolphin_utility.config.ConfigGetter;
import io.freeze_dolphin.dolphin_utility.network.DownloadUtils;

public class FetchUrlThread extends Thread {

	private static final String CONFIG_PREFIX = "features.fetch-url.";

	private CommandSender sender;
	private String url;
	private String target_path;
	private boolean ignore_check;
	private PlugEntry plug;

	public FetchUrlThread(PlugEntry plug, CommandSender sender, String url, String target_path, boolean ignore_check) {
		this.url = url;
		this.plug = plug;
		this.sender = sender;
		this.target_path = target_path;
		this.ignore_check = ignore_check;
	}

	public void run() {
		File target = new File(target_path.replaceAll("\\|", Matcher.quoteReplacement(File.separator)));
		ConfigGetter cg = new ConfigGetter(this.plug);

		boolean check_pass = false;
		{ // check
			if (this.ignore_check) {
				check_pass = true;
			} else {
				if (target.exists() && target.isFile() && target.canRead() && target.canWrite()) {
					check_pass = true;
				}
			}
		}

		if (check_pass) {
			if (this.plug.config.contains(CONFIG_PREFIX + "limited-hosts")) {

				for (Object limited_host : this.plug.config.getArray(CONFIG_PREFIX + "limited-hosts").toList()) {
					if (url.matches("http.*\\/\\/" + limited_host + "\\/.*")) {
						sender.sendMessage(msg("This host is limited and you are not able to download files from it!"));
						this.interrupt();
						return;
					}
				}
			}
			if (this.plug.config.contains(CONFIG_PREFIX + "max-volume")) {
				try {
					boolean volume_check = DownloadUtils.get_file_length(this.url, cg.get_ua()) > cg.get_max_volume();
					sender.sendMessage(msg("Checking file volume..."));
					if (volume_check) {
						sender.sendMessage(msg("This file is too large!"));
						this.interrupt();
						return;
					}
				} catch (IOException e) {
					if (sender instanceof Player) {
						((Player) sender).sendMessage(msg(e.getMessage()));
					} else {
						e.printStackTrace();
					}
					this.interrupt();
					return;
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
					this.interrupt();
					return;
				} catch (Exception e) {
					if (sender instanceof Player) {
						((Player) sender).sendMessage(msg(ChatColor.translateAlternateColorCodes('&',
								"&cFailed! &rUnable to overwrite existed target file: " + target.getPath())));
					} else {
						e.printStackTrace();
					}
					this.interrupt();
					return;
				}
			}
		} else {
			sender.sendMessage(msg("Invaild target path!"));
			this.interrupt();
			return;
		}
	}

	private static String msg(String msg) {
		return "[" + PlugEntry.plug.getDescription().getPrefix() + "] " + msg;
	}

}
