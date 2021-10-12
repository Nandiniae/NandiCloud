package de.nandi.nandicloud.api.events.server;

import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.Event;

public interface ServerExtraChangeEvent extends Event {

	ServerObject getServer();

	String getOldValue();

	String getNewValue();


}
