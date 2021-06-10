package io.freeze_dolphin.dolphin_utility.config;

import io.freeze_dolphin.dolphin_utility.PlugEntry;

public class ConfigGetter {

	private PlugEntry plug;

	public ConfigGetter(PlugEntry plug) {
		this.plug = plug;
	}

	public String get_ua() {
		return (this.plug.config.contains("features.fetch-url.ua") && this.plug.config.isString("features.fetch-url.ua")
				? this.plug.config.getString("features.fetch-url.ua")
				: "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.77 Safari/537.36 Edg/91.0.864.41");
	}

	public long get_time_out() {
		return (this.plug.config.contains("features.fetch-url.time-out")
				&& this.plug.config.isLong("features.fetch-url.time-out")
						? this.plug.config.getLong("features.fetch-url.time-out")
						: 9000L);
	}

	public long get_max_volume() {
		return (this.plug.config.contains("features.fetch-url.max-volume")
				&& this.plug.config.isLong("features.fetch-url.max-volume")
						? this.plug.config.getLong("features.fetch-url.max-volume")
						: 10485760L);
	}

}
