package de.nandi.nandicloud.core.commands;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.objects.ServerGroupObject;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.command.Command;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.core.connecting.CoreServer;
import de.nandi.nandicloud.core.main.NandiCloud;
import de.nandi.nandicloud.core.servers.ServerManagerCore;
import de.nandi.nandicloud.core.util.JSONUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.stream.Collectors;

public class RestartCommand extends Command {


	private final ServerManagerCore serverManagerCore;

	public RestartCommand(CoreServer coreServer, ServerManagerCore serverManagerCore) {
		super("restart");
		this.serverManagerCore = serverManagerCore;
		setDescription("This command restarts a server/serverGroup/PROXY.");
	}

	@Override
	public void onCommand(String command, CommandSender sender, String[] args) {
		String usage = NandiCloud.PREFIX + "§crestart <serverGroup/server/PROXY>";
		if (args.length != 1) {
			sender.sendMessage(NandiCloud.PREFIX + "§cNot the right amount of arguments.");
			sender.sendMessage(usage);
			return;
		}
		if (args[0].equalsIgnoreCase("PROXY")) {
			sender.sendMessage(NandiCloud.PREFIX + "Proxy restarted.");
			if (serverManagerCore.isProxy)
				NandiCloudAPI.getMessageAPI().sendMessageToBungee(new PluginMessage(MessageType.STOP_PROXY.name()));
			else {
				try {
					serverManagerCore.startProxy();
				} catch (IOException e) {
					NandiCloud.printError(e);
				}
			}
			return;
		}
		{
			String name = args[0];
			JSONArray array = JSONUtil.getJSON(serverManagerCore.serversConfig);
			for (Object o : array) {
				JSONObject object = (JSONObject) o;
				String currentName = (String) object.get("name");
				if (name.equalsIgnoreCase(currentName)) {
					int onlineAmount = ((Number) object.get("onlineAmount")).intValue();
					int ram = ((Number) object.get("ram")).intValue();
					boolean staticB = ((boolean) object.get("static"));
					sender.sendMessage(NandiCloud.PREFIX + (staticB ? "Server " : "ServerGroup ") + name + " restarted.");
					if (staticB && onlineAmount > 1) {
						sender.sendMessage(NandiCloud.PREFIX + "§cCould not start Server "
								+ name + " because static is true and onlineAmount is not 0 or 1");
						continue;
					}
					for (int i = 1; i <= onlineAmount; i++) {
						final String serverName = name + (staticB ? "" : ("-" + i));
						if (serverManagerCore.serverObjects.getByName(serverName) != null)
							NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()), serverName);
						else {
							try {
								serverManagerCore.startServer(serverName, ram, name, staticB);
							} catch (IOException e) {
								NandiCloud.printError(e);
							}
						}
					}
					return;
				}
			}
		}
		//is not a serverGroup or a static server
		{
			String name = args[0];
			if (name.indexOf("-") + 1 == name.length() || !name.contains("-")) {
				sender.sendMessage(NandiCloud.PREFIX + "§cNot a valid name");
				noServerFound(sender);
				return;
			}
			int i;
			try {
				i = Integer.parseInt(name.substring(name.indexOf("-") + 1));
				if (i < 1) {
					sender.sendMessage(NandiCloud.PREFIX + "§cNot a valid server");
					noServerFound(sender);
					return;
				}
			} catch (NumberFormatException e) {
				sender.sendMessage(NandiCloud.PREFIX + "§cNot a valid name");
				noServerFound(sender);
				return;
			}
			String pathName = name.split("-")[0];
			JSONArray array = JSONUtil.getJSON(serverManagerCore.serversConfig);
			for (Object o : array) {
				JSONObject object = (JSONObject) o;
				String currentName = (String) object.get("name");
				if (pathName.equalsIgnoreCase(currentName)) {
					if (serverManagerCore.serverGroups.getByName(serverManagerCore.getGroup(pathName)).getMaxServers() < i) {
						sender.sendMessage(NandiCloud.PREFIX + "§cNot a valid server");
						noServerFound(sender);
						return;
					}
					int ram = ((Number) object.get("ram")).intValue();
					boolean staticB = ((boolean) object.get("static"));
					NandiCloud.console.sendMessage(NandiCloud.PREFIX + "Server " + name + " restarted.");
					if (serverManagerCore.serverObjects.getByName(name) != null)
						NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()), name);
					else {
						try {
							serverManagerCore.startServer(name, ram, pathName, staticB);
						} catch (IOException e) {
							NandiCloud.printError(e);
						}
					}
					return;
				}
			}
		}
		//no server found
		sender.sendMessage(NandiCloud.PREFIX + "No server with the name " + args[0] + " found.");
		noServerFound(sender);
	}

	private void noServerFound(CommandSender sender) {
		sender.sendMessage(NandiCloud.PREFIX + "Available ServerGroups: " + NandiCloudAPI.getUniversalAPI().getServerGroups().stream()
				.map(ServerGroupObject::getName).collect(Collectors.joining(", ")));
		sender.sendMessage(NandiCloud.PREFIX + "Available Servers: " + NandiCloudAPI.getUniversalAPI().getServers().stream()
				.map(ServerObject::getName).collect(Collectors.joining(", ")));
		sender.sendMessage(NandiCloud.PREFIX + "Available Proxy: " + (serverManagerCore.isProxyConfig ? "Proxy" : "§c-"));
	}
}
