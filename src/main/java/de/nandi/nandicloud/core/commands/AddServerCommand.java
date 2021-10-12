package de.nandi.nandicloud.core.commands;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.identifiable.ServerObjectLink;
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class AddServerCommand extends Command {

	private final ServerManagerCore serverManagerCore;


	public AddServerCommand(ServerManagerCore serverManagerCore) {
		super("add", "addserver", "addproxy");
		this.serverManagerCore = serverManagerCore;
		setDescription("Adds a server,serverGroup or proxy.");
	}

	@Override
	public void onCommand(String command, CommandSender sender, String[] args) {
		String usage_server = NandiCloud.PREFIX + "§cadd server <name> <ram> <onlineAmount> <static>";
		String usage_proxy = NandiCloud.PREFIX + "§cadd proxy <ram> <max_players>";
		if (!(
				(args.length == 5 && command.equalsIgnoreCase("add")) ||
						(args.length == 3 && command.equalsIgnoreCase("add")) ||
						(command.equalsIgnoreCase("addproxy") && args.length == 2) ||
						(command.equalsIgnoreCase("addserver") && args.length == 4)
		)) {
			sender.sendMessage(NandiCloud.PREFIX + "§cNot the right amount of arguments.");
			sender.sendMessage(usage_server + " §6or");
			sender.sendMessage(usage_proxy);
			return;
		}
		if (args[0].equalsIgnoreCase("server") || command.equalsIgnoreCase("addserver")) {
			if (args[0].equalsIgnoreCase("server"))
				args = Arrays.copyOfRange(args, 1, args.length);
			String name = args[0];
			if (serverManagerCore.isServerJSON(name)) {
				sender.sendMessage(NandiCloud.PREFIX + "§cServerGroup already exists.");
				return;
			}
			int ram;
			int onlineAmount;
			boolean staticB;
			try {
				ram = Integer.parseUnsignedInt(args[1]);
				if (ram <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				sender.sendMessage(NandiCloud.PREFIX + "§cRam must be an integer over 0.");
				return;
			}
			try {
				onlineAmount = Integer.parseUnsignedInt(args[2]);
			} catch (NumberFormatException e) {
				sender.sendMessage(NandiCloud.PREFIX + "§cOnlineAmount must be an positive integer.");
				return;
			}
			if (args[3].equalsIgnoreCase("true"))
				staticB = true;
			else if (args[3].equalsIgnoreCase("false"))
				staticB = false;
			else {
				sender.sendMessage(NandiCloud.PREFIX + "§cStatic must be an boolean.");
				return;
			}
			if (staticB && onlineAmount > 1) {
				sender.sendMessage(NandiCloud.PREFIX + "§cWhen static is true onlineAmount must be 1 or 0.");
				return;
			}

			if (ram < 50) {
				ram = ram * 1024;
			}
			if (containsAny(name, '\\', '/', '.', '#', '-', '*', '?', '\"', '<', '>', '|', ':')) {
				sender.sendMessage(NandiCloud.PREFIX +
						"§cName must not contain any of this characters: \\ / . # - * ? \" < > | : ");
				return;
			}
			JSONArray array = JSONUtil.getJSON(serverManagerCore.serversConfig);
			for (Object o : array) {
				JSONObject object = (JSONObject) o;
				String currentName = (String) object.get("name");
				if (name.equalsIgnoreCase(currentName)) {
					boolean staticN = (boolean) object.get("static");
					sender.sendMessage(NandiCloud.PREFIX + (staticN ? "§cServer " : "§cServerGroup ") + name + " already exists.");
					return;
				}
			}
			JSONObject object = new JSONObject();
			object.put("name", name);
			object.put("ram", ram);
			object.put("onlineAmount", onlineAmount);
			object.put("static", staticB);
			JSONUtil.saveJSON(object, serverManagerCore.serversConfig);
			File serverDirectory = new File(staticB ? serverManagerCore.staticDirectory : serverManagerCore.serverGroupDirectory, name);
			serverDirectory.mkdirs();
			ServerGroupImplementation serverGroupImplementation =
					new ServerGroupImplementation(name, name, new HashSet<>(), onlineAmount, staticB, ram);
			serverManagerCore.serverGroups.add(serverGroupImplementation);
			PluginMessage messageServer = new PluginMessage(MessageType.CORE_ADD_SERVERGROUP.name());
			messageServer.
					set("id", serverGroupImplementation.getId()).
					set("name", serverGroupImplementation.getName()).
					set("servers", (HashSet<ServerObjectLink>) serverGroupImplementation.getServersLink()).
					set("max_servers", serverGroupImplementation.getMaxServers()).
					set("static", serverGroupImplementation.isStatic()).
					set("ram", serverGroupImplementation.getRam());
			NandiCloudAPI.getMessageAPI().sendMessageToServer(messageServer, MessageReceiver.ALL_AND_PROXY.name());
			sender.sendMessage(NandiCloud.PREFIX + "The server" + (staticB ? " " : "Group ") + name + "§r was successfully created.");
			serverManagerCore.startServerGroup(name, ram, onlineAmount, staticB);
		} else if (args[0].equalsIgnoreCase("proxy") || command.equalsIgnoreCase("addproxy")) {
			if (args[0].equalsIgnoreCase("proxy"))
				args = Arrays.copyOfRange(args, 1, args.length);
			int ram;
			int max_players;
			try {
				ram = Integer.parseUnsignedInt(args[0]);
				if (ram <= 0)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				sender.sendMessage(NandiCloud.PREFIX + "§cRam must be an integer over 0.");
				return;
			}
			try {
				max_players = Integer.parseUnsignedInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(NandiCloud.PREFIX + "§cMax_players must be an positive integer.");
				return;
			}
			if (ram < 50) {
				ram = ram * 1024;
			}
			JSONObject object = new JSONObject();
			object.put("ram", ram);
			object.put("max_players", max_players);
			JSONArray array = new JSONArray();
			array.add(object);
			JSONUtil.saveJSON(array, serverManagerCore.proxyConfig);
			sender.sendMessage(NandiCloud.PREFIX + "The Proxy was successfully created.");
			try {
				serverManagerCore.startProxy();
			} catch (IOException e) {
				NandiCloud.printError(e);
			}
		} else {
			sender.sendMessage(NandiCloud.PREFIX + "§cFirst argument is not SERVER or PROXY.");
			sender.sendMessage(usage_server + " §6or");
			sender.sendMessage(usage_proxy);
		}
	}

	private boolean containsAny(String string, char... contains) {
		for (char a : contains) {
			if (string.contains(String.valueOf(a)))
				return true;
		}
		return false;
	}
}
