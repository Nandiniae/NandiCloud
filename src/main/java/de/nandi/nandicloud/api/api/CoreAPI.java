package de.nandi.nandicloud.api.api;

import de.nandi.nandicloud.api.objects.command.Command;

public interface CoreAPI {

	void registerCommand(Command command);

	void unregisterCommand(String name);

}
