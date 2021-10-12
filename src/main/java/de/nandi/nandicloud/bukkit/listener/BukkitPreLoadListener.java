package de.nandi.nandicloud.bukkit.listener;

import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageListener;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bukkit.main.NandiCloudBukkit;
import de.nandi.nandicloud.core.main.NandiCloud;
import de.nandi.nandicloud.core.util.ChatColorUtil;
import org.bukkit.Bukkit;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BukkitPreLoadListener implements MessageListener {

	private final NandiCloudBukkit plugin;

	public BukkitPreLoadListener(NandiCloudBukkit plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onPluginMessage(AddressedPluginMessage addressedPluginMessage) {
		MessageType messageType = MessageType.getByName(addressedPluginMessage.getMessage().getType());
		if (messageType == null)
			return;
		PluginMessage pluginMessage = addressedPluginMessage.getMessage();
		switch (messageType) {
			case STOP_SERVER:
				System.out.println(NandiCloud.PREFIX + ChatColorUtil.toLegacyText("§cStopping Server"));
				if (plugin.isEnabledPlugin())
					Bukkit.shutdown();
				else
					System.exit(0);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						System.exit(0);
					}
				}, TimeUnit.SECONDS.toMillis(15));
				break;
			case STOP_SERVER_SILENT:
				if (!plugin.getId().equals(pluginMessage.getString("id")))
					return;
				System.out.println(NandiCloud.PREFIX + ChatColorUtil.toLegacyText("§cStopping Server silent"));
				plugin.setStopSilent(true);
				if (plugin.isEnabledPlugin())
					Bukkit.shutdown();
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
