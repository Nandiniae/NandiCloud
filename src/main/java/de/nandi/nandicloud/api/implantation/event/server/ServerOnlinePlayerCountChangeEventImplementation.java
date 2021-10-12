package de.nandi.nandicloud.api.implantation.event.server;

import de.nandi.nandicloud.api.events.server.ServerOnlinePlayerCountChangeEvent;
import de.nandi.nandicloud.api.implantation.event.ServerPropertyChangeEvent;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.EventType;

public class ServerOnlinePlayerCountChangeEventImplementation extends ServerPropertyChangeEvent<Integer> implements ServerOnlinePlayerCountChangeEvent {

	public ServerOnlinePlayerCountChangeEventImplementation(ServerObject instance, Integer oldValue, Integer newValue) {
		super(instance, oldValue, newValue);
	}

	@Override
	public EventType getType() {
		return EventType.SERVER_ONLINE_PLAYER_CHANGE;
	}
}
