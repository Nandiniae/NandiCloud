package de.nandi.nandicloud.core.commands;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
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

import java.util.Locale;

public class EditServerGroupCommand extends Command {

	private final ServerManagerCore serverManagerCore;

	public EditServerGroupCommand(ServerManagerCore serverManagerCore) {
		super("edit");
		this.serverManagerCore = serverManagerCore;
		setDescription("Edits a serverGroup or proxy.");
	}

	@Override
	public void onCommand(String command, CommandSender sender, String[] args) {
		String usage_server =
				NandiCloud.PREFIX + "§cedit <serverGroup> <RAM/ONLINEAMOUNT/STATIC> <ram(int)/onlineAmount(int)/static(boolean)>";
		String usage_proxy = NandiCloud.PREFIX + "§cedit <PROXY> <RAM/MAX_PLAYERS>  <ram(int)/max_players(int)>";
		if (args.length != 3) {
			sender.sendMessage(NandiCloud.PREFIX + "§cNot the right amount of arguments.");
			sender.sendMessage(usage_server + " §6or");
			sender.sendMessage(usage_proxy);
			return;
		}
		if (args[0].equalsIgnoreCase("proxy")) {
			if (!serverManagerCore.isProxyConfig) {
				sender.sendMessage(NandiCloud.PREFIX + "§cNo Proxy exists.");
				return;
			}
			JSONArray array = JSONUtil.getJSON(serverManagerCore.proxyConfig);
			JSONObject proxyJSON = (JSONObject) array.get(0);
			switch (args[1].toLowerCase(Locale.ROOT)) {
				case "ram": {
					int ram;
					try {
						ram = Integer.parseUnsignedInt(args[2]);
						if (ram <= 0)
							throw new NumberFormatException();
					} catch (NumberFormatException e) {
						sender.sendMessage(NandiCloud.PREFIX + "§cRam must be an integer over 0.");
						return;
					}
					if (ram < 50) {
						ram = ram * 1024;
					}
					proxyJSON.put("ram", ram);
					JSONUtil.saveJSON(array, serverManagerCore.proxyConfig);
					sender.sendMessage(NandiCloud.PREFIX + "Ram of Proxy changed to " + ram + "mb.");
					break;
				}
				case "max_players": {
					int max_players;
					try {
						max_players = Integer.parseUnsignedInt(args[2]);
					} catch (NumberFormatException e) {
						sender.sendMessage(NandiCloud.PREFIX + "§cMax_players must be an positive integer.");
						return;
					}
					proxyJSON.put("max_players", max_players);
					JSONUtil.saveJSON(array, serverManagerCore.proxyConfig);
					sender.sendMessage(NandiCloud.PREFIX + "Max_Players of Proxy changed to " + max_players + ".");
				}
				default: {
					sender.sendMessage(NandiCloud.PREFIX + "§cNot the right usage. ");
					sender.sendMessage(usage_proxy);
					break;
				}
			}
		} else {
			if (!serverManagerCore.isServerJSON(args[0])) {
				sender.sendMessage(NandiCloud.PREFIX + "§cNo ServerGroup with the name " + args[0] + " exists.");
				return;
			}
			String serverName = serverManagerCore.getServerName(args[0]);
			JSONObject serverJSON = serverManagerCore.getServerJSON(serverName);
			switch (args[1].toLowerCase(Locale.ROOT)) {
				case "ram": {
					int ram;
					try {
						ram = Integer.parseUnsignedInt(args[2]);
						if (ram <= 0)
							throw new NumberFormatException();
					} catch (NumberFormatException e) {
						sender.sendMessage(NandiCloud.PREFIX + "§cRam must be an integer over 0.");
						return;
					}
					if (ram < 50) {
						ram = ram * 1024;
					}
					serverJSON.put("ram", ram);
					saveEdit(serverName, serverJSON);
					serverManagerCore.serverGroups.getByName(serverName).setRam(ram);
					sender.sendMessage(NandiCloud.PREFIX + "Ram of ServerGroup " + serverName + " changed to " + ram + "mb.");
					NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.SERVERGROUP_CHANGE_RAM.name())
							.set("name", serverName)
							.set("ram", ram), MessageReceiver.ALL_AND_PROXY.name());
					break;
				}
				case "onlineamount": {
					int onlineAmount_new;
					try {
						onlineAmount_new = Integer.parseUnsignedInt(args[2]);
					} catch (NumberFormatException e) {
						sender.sendMessage(NandiCloud.PREFIX + "§cOnlineAmount must be an positive integer.");
						return;
					}
					int onlineAmount_old = ((Number) serverJSON.get("onlineAmount")).intValue();
					boolean staticB = (boolean) serverJSON.get("static");
					if (staticB && onlineAmount_new > 1) {
						sender.sendMessage(NandiCloud.PREFIX + "§cWhen static is true onlineAmount must be 1 or 0.");
						return;
					}
					serverJSON.put("onlineAmount", onlineAmount_new);
					saveEdit(serverName, serverJSON);
					serverManagerCore.serverGroups.getByName(serverName).setMax_Servers(onlineAmount_new);
					if (onlineAmount_new != onlineAmount_old)
						NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.SERVERGROUP_CHANGE_MAXSERVERS.name())
								.set("name", serverName)
								.set("onlineAmount", onlineAmount_new), MessageReceiver.ALL_AND_PROXY.name());
					sender.sendMessage(NandiCloud.PREFIX + "OnlineAmount of ServerGroup " + serverName + " changed from " +
							onlineAmount_old + " to " + onlineAmount_new + ".");
					if (onlineAmount_new < onlineAmount_old) {
						if (!staticB)
							for (int i = onlineAmount_new + 1; i <= onlineAmount_old; i++)
								NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()),
										serverName + "-" + i);
						else
							NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()),
									serverName);
					} else if (onlineAmount_new > onlineAmount_old) {
						if (!staticB)
							for (int i = onlineAmount_old + 1; i <= onlineAmount_new; i++) {
								serverManagerCore.startServer(serverName + "-" + i);
							}
						else
							serverManagerCore.startServer(serverName);
					}
					break;
				}
				case "static": {
					boolean static_new;
					boolean static_old = (boolean) serverJSON.get("static");
					if (args[2].equalsIgnoreCase("true"))
						static_new = true;
					else if (args[2].equalsIgnoreCase("false"))
						static_new = false;
					else {
						sender.sendMessage(NandiCloud.PREFIX + "§cStatic must be an boolean.");
						return;
					}
					int onlineAmount = ((Number) serverJSON.get("onlineAmount")).intValue();
					if (onlineAmount > 1 && static_new) {
						sender.sendMessage(NandiCloud.PREFIX + "§cWhen static is true onlineAmount must be 1 or 0.");
						return;
					}
					serverJSON.put("static", static_new);
					saveEdit(serverName, serverJSON);
					serverManagerCore.serverGroups.getByName(serverName).setStatic(static_new);
					sender.sendMessage(NandiCloud.PREFIX + "Static of ServerGroup " + serverName + " changed from " +
							static_old + " to " + static_new + ".");
					if (static_new == static_old)
						return;
					NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.SERVERGROUP_CHANGE_STATIC.name())
							.set("name", serverName)
							.set("static", static_new), MessageReceiver.ALL_AND_PROXY.name());
					if (static_old && onlineAmount == 1)
						if (serverManagerCore.serverObjects.getByName(serverName) != null)
							NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()), serverName);
						else
							serverManagerCore.startServer(serverName + "-1");
					else if (serverManagerCore.serverObjects.getByName(serverName + "-1") != null)
						NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()),
								serverName + "-1");
					else if (onlineAmount == 1)
						serverManagerCore.startServer(serverName);

					break;
				}
				default: {
					sender.sendMessage(NandiCloud.PREFIX + "§cNot the right usage.");
					sender.sendMessage(usage_server);
					break;
				}
			}
		}
	}

	private void saveEdit(String server, JSONObject settings) {
		JSONArray array = JSONUtil.getJSON(serverManagerCore.serversConfig);
		JSONArray saving = new JSONArray();
		saving.add(settings);
		for (Object o : array) {
			JSONObject object = (JSONObject) o;
			String currentName = (String) object.get("name");
			if (!server.equalsIgnoreCase(currentName)) {
				saving.add(object);
			}
		}
		JSONUtil.saveJSON(saving, serverManagerCore.serversConfig);
	}
}
