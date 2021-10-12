package de.nandi.nandicloud.bungee.listeners;

import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageListener;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bungee.main.NandiCloudBungee;
import de.nandi.nandicloud.core.main.NandiCloud;
import de.nandi.nandicloud.core.util.ChatColorUtil;
import net.md_5.bungee.api.ProxyServer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BungeePreLoadListener implements MessageListener {

	private final NandiCloudBungee plugin;

	public BungeePreLoadListener(NandiCloudBungee plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPluginMessage(AddressedPluginMessage addressedPluginMessage) {
		PluginMessage pluginMessage = addressedPluginMessage.getMessage();
		MessageType messageType = MessageType.getByName(pluginMessage.getType());
		if (messageType == null)
			return;
		switch (messageType) {
			case STOP_PROXY:
				System.out.println(NandiCloud.PREFIX + ChatColorUtil.toLegacyText("§cStopping Server"));
				if (plugin.isEnabledPlugin())
					ProxyServer.getInstance().stop();
				else
					System.exit(0);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						System.exit(0);
					}
				}, TimeUnit.SECONDS.toMillis(15));
				break;
			case STOP_PROXY_SILENT:
				if (!plugin.getId().equals(pluginMessage.getString("id")))
					return;
				System.out.println(NandiCloud.PREFIX + ChatColorUtil.toLegacyText("§cStopping Server silent"));
				plugin.setSilentStop(true);
				if (plugin.isEnabledPlugin())
					ProxyServer.getInstance().stop();
				else
					System.exit(0);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						System.exit(0);
					}
				}, TimeUnit.SECONDS.toMillis(15));
				break;
		}
	}
}
