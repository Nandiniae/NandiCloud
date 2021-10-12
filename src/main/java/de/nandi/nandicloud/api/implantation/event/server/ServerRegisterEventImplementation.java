package de.nandi.nandicloud.api.implantation.event.server;

import de.nandi.nandicloud.api.events.server.ServerRegisterEvent;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.EventType;

public class ServerRegisterEventImplementation implements ServerRegisterEvent {

	private final ServerObject serverObject;

	public ServerRegisterEventImplementation(ServerObject serverObject) {
		this.serverObject = serverObject;
	}

	public ServerObject getServer() {
		return serverObject;
	}

	@Override
	public EventType getType() {
		return EventType.SERVER_REGISTER;
	}
}
