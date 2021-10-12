package de.nandi.nandicloud.bukkit.api;

import de.nandi.nandicloud.api.api.BukkitAPI;
import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.bukkit.main.NandiCloudBukkit;

public class BukkitBukkitAPIInstance implements BukkitAPI {
	private final NandiCloudBukkit plugin;

	public BukkitBukkitAPIInstance(NandiCloudBukkit plugin) {
		this.plugin = plugin;
	}

	@Override
	public ServerObject getThisServer() {
		return NandiCloudAPI.getUniversalAPI().getServer(plugin.getServername());
	}
}
