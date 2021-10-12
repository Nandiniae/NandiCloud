package de.nandi.nandicloud.api.implantation.event.server;

import de.nandi.nandicloud.api.events.server.ServerUnregisterEvent;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.EventType;

public class ServerUnregisterEventImplementation implements ServerUnregisterEvent {

	private final ServerObject serverObject;

	public ServerUnregisterEventImplementation(ServerObject serverObject) {
		this.serverObject = serverObject;
	}

	public ServerObject getServer() {
		return serverObject;
	}

	@Override
	public EventType getType() {
		return EventType.SERVER_UNREGISTER;
	}
}
