package de.nandi.nandicloud.bungee.listeners;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.implantation.event.EventManager;
import de.nandi.nandicloud.api.implantation.event.server.ServerExtraChangeEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerRegisterEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerStateChangeEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerUnregisterEventImplementation;
import de.nandi.nandicloud.api.objects.events.EventType;
import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageListener;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;

public class EventCallListener implements MessageListener {

	@Override
	public void onPluginMessage(AddressedPluginMessage addressedPluginMessage) {
		PluginMessage pluginMessage = addressedPluginMessage.getMessage();
		MessageType messageType = MessageType.getByName(pluginMessage.getType());
		if (messageType == null)
			return;
		if (messageType != MessageType.EVENT)
			return;
		EventType eventType = EventType.getByName(pluginMessage.getString("event"));
		EventManager eventManager = (EventManager) NandiCloudAPI.getEventAPI();
		switch (eventType) {
			case SERVER_REGISTER: {
				eventManager.callEvent(new ServerRegisterEventImplementation(
						NandiCloudAPI.getUniversalAPI().getServer(pluginMessage.getString("server"))
				));
				break;
			}
			case SERVER_UNREGISTER: {
				eventManager.callEvent(new ServerUnregisterEventImplementation(
						NandiCloudAPI.getUniversalAPI().getServer(pluginMessage.getString("server"))
				));
				break;
			}
			case SERVER_EXTRA_CHANGE: {
				eventManager.callEvent(new ServerExtraChangeEventImplementation(
						NandiCloudAPI.getUniversalAPI().getServer(pluginMessage.getString("server")),
						pluginMessage.getString("old"),
						pluginMessage.getString("new")
				));
				break;
			}
			case SERVER_STATE_CHANGE: {
				eventManager.callEvent(new ServerStateChangeEventImplementation(
						NandiCloudAPI.getUniversalAPI().getServer(pluginMessage.getString("server")),
						pluginMessage.getString("old"),
						pluginMessage.getString("new")
				));
				break;
			}
		}
	}
}
