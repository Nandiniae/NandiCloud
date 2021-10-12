package de.nandi.nandicloud.bukkit.api;

import de.nandi.nandicloud.api.api.MessageAPI;
import de.nandi.nandicloud.api.objects.message.*;
import de.nandi.nandicloud.bukkit.connecting.BukkitClient;
import de.nandi.nandicloud.bukkit.main.NandiCloudBukkit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BukkitMessageAPIInstance implements MessageAPI {
	private final NandiCloudBukkit plugin;
	private final Map<MessageListener, String[]> listeners;
	private final BukkitClient bukkitClient;

	public BukkitMessageAPIInstance(NandiCloudBukkit plugin, BukkitClient bukkitClient) {
		this.plugin = plugin;
		listeners = new HashMap<>();
		this.bukkitClient = bukkitClient;
	}

	public void sendMessage(AddressedPluginMessage message) {
		try {
			bukkitClient.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageToBungee(PluginMessage message) {
		sendMessage(new AddressedPluginMessage(plugin.getServername(), MessageReceiver.BUNGEE.name(), message));
	}

	@Override
	public void sendMessageToCore(PluginMessage message) {
		sendMessage(new AddressedPluginMessage(plugin.getServername(), MessageReceiver.CORE.name(), message));
	}

	public void sendMessageToServer(PluginMessage message, String server) {
		sendMessage(new AddressedPluginMessage(plugin.getServername(), server, message));
	}

	public void registerMessageListener(MessageListener listener, String... supportedMessageTypes) {
		if (supportedMessageTypes == null || supportedMessageTypes.length == 0)
			listeners.put(listener, new String[]{MessageType.ALL.name()});
		else
			listeners.put(listener, supportedMessageTypes);
	}

	public void unregisterMessageListener(MessageListener listener) {
		listeners.remove(listener);
	}

	public Map<MessageListener, String[]> getListeners() {
		return listeners;
	}
}
