package de.nandi.nandicloud.core.listener;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.implantation.PlayerObjectImplementation;
import de.nandi.nandicloud.api.implantation.event.EventManager;
import de.nandi.nandicloud.api.implantation.event.player.PlayerConnectEventImplementation;
import de.nandi.nandicloud.api.implantation.event.player.PlayerDisconnectEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerExtraChangeEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerOnlinePlayerCountChangeEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerStateChangeEventImplementation;
import de.nandi.nandicloud.api.objects.events.EventType;
import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageListener;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;

import java.util.UUID;

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
		if (eventType == null)
			return;
		EventManager eventManager = (EventManager) NandiCloudAPI.getEventAPI();
		switch (eventType) {
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
			case SERVER_ONLINE_PLAYER_CHANGE: {
				eventManager.callEvent(new ServerOnlinePlayerCountChangeEventImplementation(
						NandiCloudAPI.getUniversalAPI().getServer(pluginMessage.getString("server")),
						pluginMessage.getInteger("old"),
						pluginMessage.getInteger("new")
				));
				break;
			}
			case PLAYER_CONNECT: {
				eventManager.callEvent(new PlayerConnectEventImplementation(
						NandiCloudAPI.getUniversalAPI().getPlayer((UUID) pluginMessage.get("uuid"))));
				break;
			}
			case PLAYER_DISCONNECT: {
				PlayerObjectImplementation playerObjectImplementation =
						(PlayerObjectImplementation) NandiCloudAPI.getUniversalAPI().getPlayer((UUID) pluginMessage.get("uuid"));
				playerObjectImplementation.setOnline(false);
				eventManager.callEvent(new PlayerDisconnectEventImplementation(playerObjectImplementation));
				break;
			}
		}
	}
}
