package de.nandi.nandicloud.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class StopCommand extends Command {


	public StopCommand() {
		super("exit");
	}

	@Override
	public void execute(CommandSender commandSender, String[] strings) {
		if (commandSender.hasPermission("nandicloud.bungee.stop"))
			ProxyServer.getInstance().stop();
		else
			commandSender.sendMessage(new TextComponent("§cThis Command does not exist"));
	}
}
