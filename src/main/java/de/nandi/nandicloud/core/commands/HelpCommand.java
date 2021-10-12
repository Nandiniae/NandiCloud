package de.nandi.nandicloud.core.commands;

import de.nandi.nandicloud.api.objects.command.Command;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.core.api.CoreCoreAPIInstance;
import de.nandi.nandicloud.core.main.NandiCloud;

public class HelpCommand extends Command {

	private final CoreCoreAPIInstance coreAPIInstance;

	public HelpCommand(CoreCoreAPIInstance coreAPIInstance) {
		super("help", "?");
		this.coreAPIInstance = coreAPIInstance;
		setDescription("This command displays a list of all Commands.");
	}


	@Override
	public void onCommand(String notUsed, CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(NandiCloud.PREFIX + "All Commands:");
			for (Command command : coreAPIInstance.getCommands()) {
				String description = command.getDescription() == null ? ":" : (": §r" + command.getDescription());
				if (description.length() > 52)
					description = description.substring(0, 52) + "§r...";
				sender.sendMessage(NandiCloud.PREFIX + "§6" + command.getName() + description);
			}
		} else if (args.length == 1) {
			for (Command command : coreAPIInstance.getCommands()) {
				for (String aliases : command.getAliases()) {
					if (!(args[0].equalsIgnoreCase(aliases) || args[0].equalsIgnoreCase(command.getName()))) {
						continue;
					}
					sender.sendMessage(NandiCloud.PREFIX + "Help for §6" + command.getName());
					sender.sendMessage(NandiCloud.PREFIX + "§6Description: §r" +
							(command.getDescription() == null ? "" : command.getDescription()));
					if (command.getAliases() != null)
						sender.sendMessage(NandiCloud.PREFIX + "§6Aliases: §r" + String.join(", ", command.getAliases()));
					return;
				}
			}
			sender.sendMessage(NandiCloud.PREFIX + "§cThis is not a Command. §6/help§c for all Commands");
		}
	}
}
