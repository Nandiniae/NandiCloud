package de.nandi.nandicloud.api.api;

import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageListener;
import de.nandi.nandicloud.api.objects.message.PluginMessage;

public interface MessageAPI {

	void sendMessage(AddressedPluginMessage message);

	void sendMessageToServer(PluginMessage message, String serverName);

	void sendMessageToBungee(PluginMessage message);

	void sendMessageToCore(PluginMessage message);

	void registerMessageListener(MessageListener listener, String... supportedMessageTypes);

	void unregisterMessageListener(MessageListener listener);


}
