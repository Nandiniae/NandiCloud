package de.nandi.nandicloud.bukkit.listener;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.identifiable.PlayerObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerGroupObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerObjectLink;
import de.nandi.nandicloud.api.implantation.PlayerObjectImplementation;
import de.nandi.nandicloud.api.implantation.ServerGroupImplementation;
import de.nandi.nandicloud.api.implantation.ServerObjectImplementation;
import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageListener;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bukkit.main.NandiCloudBukkit;
import de.nandi.nandicloud.bukkit.servers.StorageBukkit;
import org.bukkit.Bukkit;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;

public class BukkitMessageListener implements MessageListener {

	private final StorageBukkit serverManager;
	private final NandiCloudBukkit plugin;

	public BukkitMessageListener(StorageBukkit serverManager, NandiCloudBukkit plugin) {
		this.serverManager = serverManager;
		this.plugin = plugin;
	}

	@Override
	public void onPluginMessage(AddressedPluginMessage addressedPluginMessage) {
		MessageType messageType = MessageType.getByName(addressedPluginMessage.getMessage().getType());
		if (messageType == null)
			return;
		PluginMessage pluginMessage = addressedPluginMessage.getMessage();
		switch (messageType) {
			case SEND_REGISTER: {
				NandiCloudAPI.getMessageAPI().sendMessageToBungee(new PluginMessage(MessageType.REGISTER_SERVER_PROXY.name())
						.set("name", plugin.getServername()).set("port", Bukkit.getPort()));
				break;
			}
			case CORE_ADD_SERVER: {
				ServerObjectImplementation server = new ServerObjectImplementation(
						pluginMessage.getString("name"),
						pluginMessage.getString("id"),
						(Set<PlayerObjectLink>) pluginMessage.getObject("players"),
						pluginMessage.getInteger("max_players"),
						(InetSocketAddress) pluginMessage.getObject("address"),
						(ServerGroupObjectLink) pluginMessage.getObject("group"));
				server.setExtraInternally(pluginMessage.getString("extra"));
				server.setStateInternally(pluginMessage.getString("state"));
				serverManager.serverObjects.add(server);
				((ServerGroupImplementation) server.getGroup()).addServer(server.toLink());
				serverManager.serverObjects.add(server);
				break;
			}
			case CORE_ADD_SERVERGROUP: {
				serverManager.serverGroups.add(new ServerGroupImplementation(
						pluginMessage.getString("id"),
						pluginMessage.getString("name"),
						(Set<ServerObjectLink>) pluginMessage.getObject("servers"),
						pluginMessage.getInteger("max_servers"),
						pluginMessage.getBoolean("static"),
						pluginMessage.getInteger("ram")
				));
				break;
			}
			case CORE_REMOVE_SERVER: {
				ServerObjectImplementation server = serverManager.serverObjects.getByIdentifier(pluginMessage.getString("name"));
				if (server.getGroup() != null)
					((ServerGroupImplementation) server.getGroup()).removeServer(server.toLink());
				serverManager.serverObjects.remove(server);
				break;
			}
			case CORE_REMOVE_SERVERGROUP: {
				serverManager.serverGroups.removeByIdentifier(pluginMessage.getString("name"));
				break;
			}
			case ADD_PLAYER: {
				PlayerObjectImplementation player = new PlayerObjectImplementation(
						pluginMessage.getString("name"),
						(UUID) pluginMessage.get("uuid"),
						serverManager.serverObjects.getByIdentifier(pluginMessage.getString("server")).toLink(),
						(InetAddress) pluginMessage.getObject("address"),
						pluginMessage.getBoolean("online")
				);
				serverManager.playerObjects.add(player);
				serverManager.serverObjects.getByIdentifier(pluginMessage.getString("server")).addPlayer(player.toLink());
				break;
			}
			case REMOVE_PLAYER: {
				PlayerObjectImplementation player = serverManager.playerObjects.getById(pluginMessage.getString("uuid"));
				serverManager.playerObjects.remove(player);
				ServerObjectImplementation server = serverManager.serverObjects.getByIdentifier(pluginMessage.getString("server"));
				if (server != null) {
					server.removePlayer(player.toLink());
				}
				player.setOnline(false);
				break;
			}
			case PLAYER_CHANGE_SERVER: {
				PlayerObjectImplementation player = serverManager.playerObjects.getById(pluginMessage.getString("uuid"));
				player.setServer(serverManager.serverObjects.getByIdentifier(pluginMessage.getString("serverTo")).toLink());
				serverManager.playerObjects.update(player);//NEEDED
				serverManager.serverObjects.getByIdentifier(pluginMessage.getString("serverTo")).addPlayer(player.toLink());
				serverManager.serverObjects.getByIdentifier(pluginMessage.getString("serverFrom")).removePlayer(player.toLink());
				break;
			}
			case SERVER_CHANGE_EXTRA:
			case SERVER_CHANGE_STATE: {
				ServerObjectImplementation server = serverManager.serverObjects.getByIdentifier(pluginMessage.getString("server"));
				if (pluginMessage.getString("state") != null)
					server.setStateInternally(pluginMessage.getString("state"));
				if (pluginMessage.getString("extra") != null)
					server.setExtraInternally(pluginMessage.getString("extra"));
				break;
			}
			case SERVERGROUP_CHANGE_MAXSERVERS: {
				serverManager.serverGroups.getByName(pluginMessage.getString("name"))
						.setMax_Servers(pluginMessage.getInteger("onlineAmount"));
				break;
			}
			case SERVERGROUP_CHANGE_STATIC: {
				serverManager.serverGroups.getByName(pluginMessage.getString("name"))
						.setStatic(pluginMessage.getBoolean("static"));
				break;
			}
			case SERVERGROUP_CHANGE_RAM: {
				serverManager.serverGroups.getByName(pluginMessage.getString("name"))
						.setRam(pluginMessage.getInteger("ram"));
				break;
			}
		}
	}
}
