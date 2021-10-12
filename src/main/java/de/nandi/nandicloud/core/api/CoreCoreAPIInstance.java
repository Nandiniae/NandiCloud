package de.nandi.nandicloud.core.api;

import de.nandi.nandicloud.api.api.CoreAPI;
import de.nandi.nandicloud.api.objects.command.Command;

import java.util.ArrayList;
import java.util.List;

public class CoreCoreAPIInstance implements CoreAPI {

	private final List<Command> commands;

	public CoreCoreAPIInstance() {
		commands = new ArrayList<>();
	}


	@Override
	public void registerCommand(Command command) {
		this.commands.add(command);
	}

	@Override
	public void unregisterCommand(String name) {
		commands.removeIf(command -> command.getName().equals(name));
	}

	public List<Command> getCommands() {
		return commands;
	}
}
