package de.nandi.nandicloud.bungee.api;

import de.nandi.nandicloud.api.api.MessageAPI;
import de.nandi.nandicloud.api.api.PrivateGeneralAPI;
import de.nandi.nandicloud.api.objects.message.*;
import de.nandi.nandicloud.bungee.connecting.BungeeClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BungeeMessageAPIInstance implements MessageAPI {

	private final Map<MessageListener, String[]> listeners;
	private final BungeeClient bungeeClient;

	public BungeeMessageAPIInstance(BungeeClient bungeeClient) {
		listeners = new HashMap<>();
		this.bungeeClient = bungeeClient;
	}

	public void sendMessage(AddressedPluginMessage message) {
		try {
			bungeeClient.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageToServer(PluginMessage message, String server) {
		sendMessage(new AddressedPluginMessage(MessageReceiver.BUNGEE.name(), server, message));
	}

	@Override
	public void sendMessageToBungee(PluginMessage message) {
		listeners.values().forEach(type -> {
			for (String s : type) {
				if (!s.equals(message.getType()) && !s.equalsIgnoreCase(MessageType.ALL.name())) {
					continue;
				}
				try {
					PrivateGeneralAPI.getKeyFromValue(listeners, type).
							onPluginMessage(new AddressedPluginMessage(MessageReceiver.BUNGEE.name(), MessageReceiver.BUNGEE.name(), message));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		});
	}

	@Override
	public void sendMessageToCore(PluginMessage message) {
		sendMessage(new AddressedPluginMessage(MessageReceiver.BUNGEE.name(), MessageReceiver.CORE.name(), message));
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
