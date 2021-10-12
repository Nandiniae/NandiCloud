package de.nandi.nandicloud.bungee.listeners;

import de.nandi.nandicloud.api.identifiable.PlayerObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerGroupObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerObjectLink;
import de.nandi.nandicloud.api.implantation.ServerGroupImplementation;
import de.nandi.nandicloud.api.implantation.ServerObjectImplementation;
import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageListener;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bungee.main.NandiCloudBungee;
import de.nandi.nandicloud.bungee.servers.StorageBungee;
import de.nandi.nandicloud.core.main.NandiCloud;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.util.Set;

public class BungeeMessageListener implements MessageListener {

	private final NandiCloudBungee plugin;
	private final StorageBungee serverManager;

	public BungeeMessageListener(NandiCloudBungee plugin, StorageBungee serverManager) {
		this.plugin = plugin;
		this.serverManager = serverManager;
	}

	@Override
	public void onPluginMessage(AddressedPluginMessage addressedPluginMessage) {
		PluginMessage pluginMessage = addressedPluginMessage.getMessage();
		MessageType messageType = MessageType.getByName(pluginMessage.getType());
		if (messageType == null)
			return;
		switch (messageType) {
			case REGISTER_SERVER_PROXY:
				if (ProxyServer.getInstance().getServerInfo(pluginMessage.getString("name")) == null) {
					ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
							pluginMessage.getString("name"),
							InetSocketAddress.createUnresolved("127.0.0.1", pluginMessage.getInteger("port")),
							"A SERVER MOTD",
							false);
					ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
					System.out.println(NandiCloud.PREFIX + "§9Server " + serverInfo.getName() + " registered.");
				}
				break;
			case UNREGISTER_SERVER_PROXY:
				ProxyServer.getInstance().getServers().remove(pluginMessage.getString("name"));
				break;
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
			case SERVER_CHANGE_EXTRA:
			case SERVER_CHANGE_STATE: {
				ServerObjectImplementation server = serverManager.serverObjects.getByIdentifier(pluginMessage.getString("server"));
				if (pluginMessage.getString("state") != null)
					server.setStateInternally(pluginMessage.getString("state"));
				if (pluginMessage.getString("extra") != null)
					server.setExtraInternally(pluginMessage.getString("extra"));
				break;
			}
			case PROXY_SEND_PLAYER: {
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pluginMessage.getString("player"));
				ServerInfo server = ProxyServer.getInstance().getServerInfo(pluginMessage.getString("server"));
				player.connect(server);
				break;
			}
			case PROXY_SEND_MESSAGE: {
				ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pluginMessage.getString("player"));
				player.sendMessage(TextComponent.fromLegacyText(pluginMessage.getString("message")));
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
