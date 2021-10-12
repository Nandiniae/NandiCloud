package de.nandi.nandicloud.api.events.server;

import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.Event;

public interface ServerUnregisterEvent extends Event {

	ServerObject getServer();

}
