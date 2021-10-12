package de.nandi.nandicloud.core.commands;

import de.nandi.nandicloud.api.objects.ServerGroupObject;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.command.Command;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.core.main.NandiCloud;
import de.nandi.nandicloud.core.servers.ServerManagerCore;
import de.nandi.nandicloud.core.util.JSONUtil;
import org.json.simple.JSONObject;

public class ListServerGroupsCommand extends Command {

	private final ServerManagerCore serverManagerCore;

	public ListServerGroupsCommand(ServerManagerCore serverManagerCore) {
		super("list", "listgroups");
		this.serverManagerCore = serverManagerCore;
		setDescription("This Command lists all ServerGroups and the Proxy.");
	}

	@Override
	public void onCommand(String command, CommandSender sender, String[] args) {
		String prefix = "    ";
		String halfPrefix = prefix.substring(prefix.length() / 2);
		for (ServerGroupObject group : serverManagerCore.serverGroups.values()) {
			sender.sendMessage(NandiCloud.PREFIX + "§cServerGroup§r: §e" + group.getName());
			sender.sendMessage(NandiCloud.PREFIX + prefix + "§cRam§r: §2" + group.getRam() +
					"§r, §cOnlineAmount§r: §2" + group.getMaxServers() + "§r, §cStatic§r: §2" + group.isStatic());
			sender.sendMessage(NandiCloud.PREFIX + halfPrefix + "§cServers: ");
			for (ServerObject serverObject : group.getServers()) {
				sender.sendMessage(NandiCloud.PREFIX + prefix + halfPrefix +
						"§cName§r: §2" + serverObject.getName() + "§r, " +
						"§cOnlinePlayers§r: §2" + serverObject.getOnlinePlayerCount() + "§r, " +
						"§cMaxPlayers§r: §2" + serverObject.getMaxPlayerCount() + "§r, " +
						"§cState§r: §2" + serverObject.getState() + "§r, " +
						"§cExtra§r: §2" + serverObject.getExtra());
			}
		}
		sender.sendMessage(NandiCloud.PREFIX);
		for (Object o : JSONUtil.getJSON(serverManagerCore.proxyConfig)) {
			JSONObject object = (JSONObject) o;
			int ram = ((Number) object.get("ram")).intValue();
			int max_players = ((Number) object.get("max_players")).intValue();
			sender.sendMessage(NandiCloud.PREFIX + "§eProxy ");
			sender.sendMessage(NandiCloud.PREFIX + prefix + "§cRam§r: §2" + ram + "§r, §cMaxPlayers: §2" + max_players);
		}
	}
}
