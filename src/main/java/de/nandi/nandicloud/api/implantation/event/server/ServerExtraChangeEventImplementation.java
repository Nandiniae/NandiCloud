package de.nandi.nandicloud.api.implantation.event.server;

import de.nandi.nandicloud.api.events.server.ServerExtraChangeEvent;
import de.nandi.nandicloud.api.implantation.event.ServerPropertyChangeEvent;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.EventType;

public class ServerExtraChangeEventImplementation extends ServerPropertyChangeEvent<String> implements ServerExtraChangeEvent {

	public ServerExtraChangeEventImplementation(ServerObject instance, String oldValue, String newValue) {
		super(instance, oldValue, newValue);
	}

	@Override
	public EventType getType() {
		return EventType.SERVER_EXTRA_CHANGE;
	}


}
