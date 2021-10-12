package de.nandi.nandicloud.api.implantation.event.server;

import de.nandi.nandicloud.api.events.server.ServerStateChangeEvent;
import de.nandi.nandicloud.api.implantation.event.ServerPropertyChangeEvent;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.EventType;

public class ServerStateChangeEventImplementation extends ServerPropertyChangeEvent<String> implements ServerStateChangeEvent {

	public ServerStateChangeEventImplementation(ServerObject instance, String oldValue, String newValue) {
		super(instance, oldValue, newValue);
	}

	@Override
	public EventType getType() {
		return EventType.SERVER_STATE_CHANGE;
	}
}
