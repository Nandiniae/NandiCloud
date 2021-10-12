package de.nandi.nandicloud.core.api;

import de.nandi.nandicloud.api.api.MessageAPI;
import de.nandi.nandicloud.api.api.PrivateGeneralAPI;
import de.nandi.nandicloud.api.objects.message.*;
import de.nandi.nandicloud.core.connecting.CoreServer;
import de.nandi.nandicloud.core.main.NandiCloud;

import java.util.HashMap;
import java.util.Map;

public class CoreMessageAPIInstance implements MessageAPI {

	private final Map<MessageListener, String[]> listeners;
	private final CoreServer coreServer;

	public CoreMessageAPIInstance(CoreServer coreServer) {
		listeners = new HashMap<>();
		this.coreServer = coreServer;
	}

	public void sendMessage(AddressedPluginMessage message) {
		coreServer.sendMessage(message);
	}

	@Override
	public void sendMessageToServer(PluginMessage message, String serverName) {
		sendMessage(new AddressedPluginMessage(MessageReceiver.CORE.name(), serverName, message));
	}

	@Override
	public void sendMessageToBungee(PluginMessage message) {
		sendMessage(new AddressedPluginMessage(MessageReceiver.CORE.name(), MessageReceiver.BUNGEE.name(), message));
	}

	@Override
	public void sendMessageToCore(PluginMessage message) {
		listeners.values().forEach(type -> {
			for (String s : type) {
				if (!s.equals(message.getType()) && !s.equalsIgnoreCase(MessageType.ALL.name())) {
					continue;
				}
				try {
					PrivateGeneralAPI.getKeyFromValue(listeners, type).
							onPluginMessage(new AddressedPluginMessage(MessageReceiver.CORE.name(), MessageReceiver.CORE.name(), message));
				} catch (Exception e) {
					NandiCloud.printError(e);
				}
				break;
			}
		});
	}

	@Override
	public void registerMessageListener(MessageListener listener, String... supportedMessageTypes) {
		if (supportedMessageTypes == null || supportedMessageTypes.length == 0)
			listeners.put(listener, new String[]{MessageType.ALL.name()});
		else
			listeners.put(listener, supportedMessageTypes);
	}

	@Override
	public void unregisterMessageListener(MessageListener listener) {
		listeners.remove(listener);
	}

	public Map<MessageListener, String[]> getListeners() {
		return listeners;
	}
}
