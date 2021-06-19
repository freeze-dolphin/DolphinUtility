package io.freeze_dolphin.dolphin_utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import io.freeze_dolphin.dolphin_utility.command.CommandHookHandler;

import redempt.redlib.commandmanager.CommandParser;

public final class PlugEntry extends JavaPlugin {

	public TomlParseResult config;
	public static Plugin plug;

	@Override
	public void onEnable() {
		plug = this;

		File config_file = new File(this.getDataFolder() + Matcher.quoteReplacement(File.separator) + "config.toml");
		if (!config_file.exists()) {
			this.saveResource("config.toml", true);
		}

		try {
			config = Toml.parse(new FileInputStream(config_file));
			config.errors().forEach(error -> this.getLogger().warning(error.toString()));
		} catch (IOException e) {
			e.printStackTrace();
			this.getLogger().severe("Unable to initialize configuration, disabling...");
			this.setEnabled(false);
		}

		new CommandParser(this.getResource("commands.rdcml")).parse().register("dolphin-utility",
				new CommandHookHandler(this));

	}

}
