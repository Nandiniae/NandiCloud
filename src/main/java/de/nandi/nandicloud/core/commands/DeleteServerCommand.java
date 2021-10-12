package de.nandi.nandicloud.core.commands;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.implantation.ServerGroupImplementation;
import de.nandi.nandicloud.api.objects.command.Command;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.core.main.NandiCloud;
import de.nandi.nandicloud.core.servers.ServerManagerCore;
import de.nandi.nandicloud.core.util.JSONUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;

public class DeleteServerCommand extends Command {

	private final ServerManagerCore serverManagerCore;


	public DeleteServerCommand(ServerManagerCore serverManagerCore) {
		super("delete", "remove");
		setDescription("Removes a serverGroup or proxy.");
		this.serverManagerCore = serverManagerCore;
	}

	@Override
	public void onCommand(String command, CommandSender sender, String[] args) {
		String usage = NandiCloud.PREFIX + "§cdelete <serverGroup/PROXY>";
		if (args.length != 1) {
			sender.sendMessage(NandiCloud.PREFIX + "§cNot the right amount of arguments.");
			sender.sendMessage(usage);
			return;
		}
		if (args[0].equalsIgnoreCase("proxy")) {
			if (!serverManagerCore.isProxyConfig) {
				sender.sendMessage(NandiCloud.PREFIX + "§cNo Proxy exists.");
				return;
			}
			try {
				serverManagerCore.proxyConfig.delete();
				serverManagerCore.isProxyConfig = false;
				serverManagerCore.proxyConfig.createNewFile();
			} catch (IOException e) {
				NandiCloud.printError(e);
				return;
			}
			sender.sendMessage(NandiCloud.PREFIX + "Successfully deleted Proxy.");
			if (serverManagerCore.isProxy)
				NandiCloudAPI.getMessageAPI().sendMessageToBungee(new PluginMessage(MessageType.STOP_PROXY.name()));
			return;
		}
		String name = args[0];
		if (!serverManagerCore.isServerJSON(name)) {
			sender.sendMessage(NandiCloud.PREFIX + "§cNo ServerGroup with the name " + name + " exists.");
			return;
		}
		name = serverManagerCore.getServerName(name);
		JSONArray array = JSONUtil.getJSON(serverManagerCore.serversConfig);
		String finalName = name;
		array.removeIf(o1 -> ((String) ((JSONObject) o1).get("name")).equalsIgnoreCase(finalName));
		JSONUtil.saveJSON(array, serverManagerCore.serversConfig);
		ServerGroupImplementation serverGroupObject = serverManagerCore.serverGroups.getByIdentifier(name);
		if (!serverGroupObject.isStatic()) {
			for (int i = 1; i <= serverGroupObject.getMaxServers(); i++)
				NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()),
						name + "-" + i);
		} else if (serverGroupObject.getMaxServers() == 1)
			NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()), name);
		serverManagerCore.serverGroups.remove(serverGroupObject);
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.CORE_REMOVE_SERVERGROUP.name()).
				set("name", name), MessageReceiver.ALL_AND_PROXY.name());
		NandiCloud.console.sendMessage(NandiCloud.PREFIX + "Successfully deleted ServerGroup " + name + ".");

	}
}
