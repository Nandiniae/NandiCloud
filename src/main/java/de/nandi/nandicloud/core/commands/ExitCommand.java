package de.nandi.nandicloud.core.commands;

import de.nandi.nandicloud.api.objects.command.Command;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.core.main.NandiCloud;

public class ExitCommand extends Command {


	public ExitCommand() {
		super("exit", "stop");
		setDescription("Stops the Core application.");
	}

	@Override
	public void onCommand(String command, CommandSender sender, String[] args) {
		sender.sendMessage(NandiCloud.PREFIX + "§4§lStopping Core");
		System.exit(0);
	}
}
