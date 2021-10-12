package de.nandi.nandicloud.bungee.main;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.implantation.APIInstanceUtil;
import de.nandi.nandicloud.api.implantation.event.EventManager;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bungee.api.BungeeMessageAPIInstance;
import de.nandi.nandicloud.bungee.api.BungeeUniversalAPIInstance;
import de.nandi.nandicloud.bungee.commands.StopCommand;
import de.nandi.nandicloud.bungee.connecting.BungeeClient;
import de.nandi.nandicloud.bungee.listeners.*;
import de.nandi.nandicloud.bungee.servers.StorageBungee;
import de.nandi.nandicloud.core.main.NandiCloud;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class NandiCloudBungee extends Plugin {
	final boolean disableAllServer = false;
	private BungeeMessageAPIInstance bungeeMessageAPIInstance;
	private boolean receiving;
	private boolean silentStop = false;
	private StorageBungee serverManagerBungee;
	private boolean connected;
	private boolean enabledPlugin;

	@Override
	public void onLoad() {
		enabledPlugin = false;
		receiving = true;
		serverManagerBungee = new StorageBungee();
		try {
			APIInstanceUtil.setMessageInstance(bungeeMessageAPIInstance = new BungeeMessageAPIInstance(new BungeeClient(this)));
			APIInstanceUtil.setUniversalInstance(new BungeeUniversalAPIInstance(serverManagerBungee));
			APIInstanceUtil.setEventInstance(new EventManager(false));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		NandiCloudAPI.getMessageAPI().registerMessageListener(new BungeePreLoadListener(this));
		System.out.println(NandiCloud.PREFIX + "Preloaded NandiCloudBungee.");
	}

	@Override
	public void onEnable() {
		enabledPlugin = true;
		System.out.println(NandiCloud.PREFIX + "Registered NandiCloudBungee.");
		ProxyServer.getInstance().getServers().clear();
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new StopCommand());
		ProxyServer.getInstance().getPluginManager().registerListener(this, new JoinListener(serverManagerBungee));
		ProxyServer.getInstance().getPluginManager().registerListener(this, new LobbyJoinListener());
		NandiCloudAPI.getMessageAPI().registerMessageListener(new BungeeMessageListener(this, serverManagerBungee));
		NandiCloudAPI.getMessageAPI().registerMessageListener(new EventCallListener());

		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.SEND_REGISTER.name()),
				MessageReceiver.ALL_SERVERS.name());
		NandiCloudAPI.getMessageAPI().sendMessageToCore(new PluginMessage(MessageType.REGISTER_PROXY_CORE.name())
				.set("id", getId()));

		System.out.println(NandiCloud.PREFIX + "Loaded NandiCloudBungee.");
	}

	public String getId() {
		return System.getProperty("nandicloud-proxyid");
	}

	@Override
	public void onDisable() {
		if (!isSilentStop())
			NandiCloudAPI.getMessageAPI().sendMessageToCore(new PluginMessage(MessageType.UNREGISTER_PROXY_CORE.name()));
		receiving = false;
		System.out.println(NandiCloud.PREFIX + "Disabled NandiCloudBungee.");
	}

	public boolean isReceiving() {
		return receiving;
	}

	public BungeeMessageAPIInstance getBungeeMessageAPIInstance() {
		return bungeeMessageAPIInstance;
	}

	public boolean isSilentStop() {
		return silentStop;
	}

	public void setSilentStop(boolean silentStop) {
		this.silentStop = silentStop;
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
