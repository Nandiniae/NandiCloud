package de.nandi.nandicloud.bukkit.main;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.implantation.APIInstanceUtil;
import de.nandi.nandicloud.api.implantation.event.EventManager;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bukkit.api.BukkitBukkitAPIInstance;
import de.nandi.nandicloud.bukkit.api.BukkitMessageAPIInstance;
import de.nandi.nandicloud.bukkit.api.BukkitUniversalAPIInstance;
import de.nandi.nandicloud.bukkit.connecting.BukkitClient;
import de.nandi.nandicloud.bukkit.listener.BukkitMessageListener;
import de.nandi.nandicloud.bukkit.listener.BukkitPreLoadListener;
import de.nandi.nandicloud.bukkit.listener.EventCallListener;
import de.nandi.nandicloud.bukkit.servers.StorageBukkit;
import de.nandi.nandicloud.core.main.NandiCloud;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NandiCloudBukkit extends JavaPlugin {

	private BukkitMessageAPIInstance bukkitMessageAPIInstance;
	private boolean receiving;
	private EventCallListener eventCallListener;
	private boolean stopSilent = false;
	private boolean connected;
	private boolean enabledPlugin;
	private boolean alreadyEnabled;
	private StorageBukkit serverManagerBukkit;

	@Override
	public void onLoad() {
		if (alreadyEnabled) {
			enabledPlugin = true;
			System.out.println(NandiCloud.PREFIX + "Preloaded NandiCloudBukkit.");
			return;
		}
		receiving = true;
		enabledPlugin = false;
		serverManagerBukkit = new StorageBukkit();
		try {
			APIInstanceUtil.setMessageInstance(bukkitMessageAPIInstance = new BukkitMessageAPIInstance(this, new BukkitClient(this)));
			APIInstanceUtil.setUniversalInstance(new BukkitUniversalAPIInstance(serverManagerBukkit));
			APIInstanceUtil.setEventInstance(new EventManager(false));
			APIInstanceUtil.setBukkitInstance(new BukkitBukkitAPIInstance(this));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		NandiCloudAPI.getMessageAPI().registerMessageListener(new BukkitPreLoadListener(this));
		System.out.println(NandiCloud.PREFIX + "Preloaded NandiCloudBukkit.");
	}

	@Override
	public void onEnable() {
		if (alreadyEnabled) {
			NandiCloudAPI.getMessageAPI().registerMessageListener(eventCallListener);
			System.out.println(NandiCloud.PREFIX + "Reloaded NandiCloudBukkit with id: " + getId());
			return;
		}
		enabledPlugin = true;
		System.out.println(NandiCloud.PREFIX + "Registered NandiCloudBukkit.");

		NandiCloudAPI.getMessageAPI().sendMessageToBungee(new PluginMessage(MessageType.REGISTER_SERVER_PROXY.name())
				.set("name", getServername()).set("port", Bukkit.getPort()));
		PluginMessage coreMessage = new PluginMessage(MessageType.REGISTER_SERVER_CORE.name()).
				set("name", getServername()).set("id", getId()).
				set("max_players", Bukkit.getMaxPlayers()).set("port", Bukkit.getPort()).
				set("address", "127.0.0.1");
		NandiCloudAPI.getMessageAPI().sendMessageToCore(coreMessage);

		NandiCloudAPI.getMessageAPI().registerMessageListener(new BukkitMessageListener(serverManagerBukkit, this));
		NandiCloudAPI.getMessageAPI().registerMessageListener(eventCallListener = new EventCallListener());
		System.out.println(NandiCloud.PREFIX + "Loaded NandiCloudBukkit with id: " + getId());
		Runtime.getRuntime().addShutdownHook(new Thread(this::stopped, "Shutdown-thread"));
		alreadyEnabled = true;
	}

	public String getId() {
		return System.getProperty("nandicloud-serverid");
	}

	public String getServername() {
		return System.getProperty("nandicloud-name");
	}

	public void stopped() {
		NandiCloudAPI.getMessageAPI().sendMessageToBungee(new PluginMessage(MessageType.UNREGISTER_SERVER_PROXY.name())
				.set("name", getServername()));
		if (!isStopSilent())
			NandiCloudAPI.getMessageAPI().sendMessageToCore(new PluginMessage(MessageType.UNREGISTER_SERVER_CORE.name())
					.set("name", getServername()));
		System.out.println(NandiCloud.PREFIX + "Disabled NandiCloudBukkit.");
		receiving = false;
	}

	@Override
	public void onDisable() {
		NandiCloudAPI.getMessageAPI().unregisterMessageListener(eventCallListener);
		enabledPlugin = false;
	}

	public boolean isReceiving() {
		return receiving;
	}

	public boolean isStopSilent() {
		return stopSilent;
	}

	public void setStopSilent(boolean stopSilent) {
		this.stopSilent = stopSilent;
	}

	public BukkitMessageAPIInstance getBukkitMessageAPIInstance() {
		return bukkitMessageAPIInstance;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isEnabledPlugin() {
		return enabledPlugin;
	}

	public void setEnabledPlugin(boolean enabledPlugin) {
		this.enabledPlugin = enabledPlugin;
	}
}
