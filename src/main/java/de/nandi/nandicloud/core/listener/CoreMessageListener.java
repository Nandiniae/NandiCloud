package de.nandi.nandicloud.core.listener;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.identifiable.PlayerObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerObjectLink;
import de.nandi.nandicloud.api.implantation.PlayerObjectImplementation;
import de.nandi.nandicloud.api.implantation.ServerGroupImplementation;
import de.nandi.nandicloud.api.implantation.ServerObjectImplementation;
import de.nandi.nandicloud.api.implantation.event.EventManager;
import de.nandi.nandicloud.api.implantation.event.server.ServerRegisterEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerUnregisterEventImplementation;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.api.objects.events.EventType;
import de.nandi.nandicloud.api.objects.message.*;
import de.nandi.nandicloud.core.main.NandiCloud;
import de.nandi.nandicloud.core.servers.ServerManagerCore;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.UUID;

public class CoreMessageListener implements MessageListener {

	private final ServerManagerCore serverManager;
	private final CommandSender console;

	public CoreMessageListener(ServerManagerCore serverManager) {
		this.serverManager = serverManager;
		console = NandiCloud.console;
	}


	@Override
	public void onPluginMessage(AddressedPluginMessage addressedPluginMessage) {
		PluginMessage pluginMessage = addressedPluginMessage.getMessage();
		MessageType messageType = MessageType.getByName(pluginMessage.getType());
		if (messageType == null)
			return;
		switch (messageType) {
			case UNREGISTER_SERVER_CORE: {
				String name = pluginMessage.getString("name");
				ServerObjectImplementation serverObject = serverManager.serverObjects.getByName(name);
				serverManager.notFreePorts.remove(serverObject.getPort());
				serverManager.serverObjects.remove(serverObject);
				EventManager eventManager = (EventManager) NandiCloudAPI.getEventAPI();
				eventManager.callEvent(new ServerUnregisterEventImplementation(serverObject));
				NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
								.set("event", EventType.SERVER_UNREGISTER.name())
								.set("server", serverObject.getName()),
						MessageReceiver.ALL_AND_PROXY.name());
				NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.CORE_REMOVE_SERVER.name()).
						set("name", name), MessageReceiver.ALL_AND_PROXY.name());
				if (serverManager.getGroup(name) != null)
					serverManager.serverGroups.getByName(serverManager.getGroup(name)).removeServer(serverObject.toLink());
				console.sendMessage(NandiCloud.PREFIX + "Server " + name + " disconnected");
				ServerGroupImplementation serverGroup = serverManager.serverGroups.getByName(serverManager.getGroup(name));
				if (serverManager.getGroup(name) != null && serverGroup.getMaxServers() > serverGroup.getServers().size())
					serverManager.startServer(serverGroup.isStatic() ? name.split("-")[0] : name);
				break;
			}
			case REGISTER_SERVER_CORE: {
				String name = pluginMessage.getString("name");
				String id = pluginMessage.getString("id");
				if (serverManager.getGroup(name) == null) {
					NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER_SILENT.name())
							.set("id", id), name);
					return;
				}
				int max_player = pluginMessage.getInteger("max_players");
				int port = pluginMessage.getInteger("port");
				String address = pluginMessage.getString("address");
				String group = serverManager.getGroup(name);
				ServerGroupImplementation serverGroup = serverManager.serverGroups.getByName(group);
				if (serverGroup.isStatic() && name.contains("-")) {
					NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER_SILENT.name())
							.set("id", id), name);
					return;
				}
				if (serverGroup.getMaxServers() == serverGroup.getServers().size()) {
					NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER_SILENT.name())
							.set("id", id), name);
					console.sendMessage(NandiCloud.PREFIX + "More servers then allowed found, stopping them.");
					return;
				}
				ServerObjectImplementation serverObject = new ServerObjectImplementation
						(name, id, new HashSet<>(), max_player, new InetSocketAddress(address, port), serverGroup.toLink());
				serverGroup.addServer(serverObject.toLink());
				serverManager.serverObjects.add(serverObject);
				console.sendMessage(NandiCloud.PREFIX + "Server " + name + " registered");
				for (ServerGroupImplementation serverGroupImplementation : serverManager.serverGroups.values()) {
					PluginMessage messageServer = new PluginMessage(MessageType.CORE_ADD_SERVERGROUP.name());
					messageServer.
							set("id", serverGroupImplementation.getId()).
							set("name", serverGroupImplementation.getName()).
							set("servers", (HashSet<ServerObjectLink>) serverGroupImplementation.getServersLink()).
							set("max_servers", serverGroupImplementation.getMaxServers()).
							set("static", serverGroupImplementation.isStatic()).
							set("ram", serverGroupImplementation.getRam());
					NandiCloudAPI.getMessageAPI().sendMessageToServer(messageServer, name);
				}
				for (ServerObjectImplementation serverObjectImplementation : serverManager.serverObjects.values()) {
					if (serverObjectImplementation.equals(serverObject)) continue;
					PluginMessage messageServer = new PluginMessage(MessageType.CORE_ADD_SERVER.name());
					messageServer.
							set("name", serverObjectImplementation.getName()).
							set("id", serverObjectImplementation.getId()).
							set("players", (HashSet<PlayerObjectLink>) serverObjectImplementation.getOnlinePlayersLink()).
							set("max_players", serverObjectImplementation.getMaxPlayerCount()).
							set("address", serverObjectImplementation.getSocketAddress()).
							set("group", serverObjectImplementation.getGroupLink()).
							set("extra", serverObjectImplementation.getExtra()).
							set("state", serverObjectImplementation.getState());
					NandiCloudAPI.getMessageAPI().sendMessageToServer(messageServer, name);
				}
				PluginMessage messageAll = new PluginMessage(MessageType.CORE_ADD_SERVER.name());
				messageAll.
						set("name", serverObject.getName()).
						set("id", serverObject.getId()).
						set("players", (HashSet<PlayerObjectLink>) serverObject.getOnlinePlayersLink()).
						set("max_players", serverObject.getMaxPlayerCount()).
						set("address", serverObject.getSocketAddress()).
						set("group", serverObject.getGroupLink()).
						set("extra", serverObject.getExtra()).
						set("state", serverObject.getState());
				NandiCloudAPI.getMessageAPI().sendMessageToServer(messageAll, MessageReceiver.ALL_AND_PROXY.name());
				EventManager eventManager = (EventManager) NandiCloudAPI.getEventAPI();
				eventManager.callEvent(new ServerRegisterEventImplementation(serverObject));
				NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
								.set("event", EventType.SERVER_REGISTER.name())
								.set("server", serverObject.getName()),
						MessageReceiver.ALL_AND_PROXY.name());
				break;
			}
			case REGISTER_PROXY_CORE: {
				if (!serverManager.isProxyConfig) {
					NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_PROXY_SILENT.name())
							.set("id", pluginMessage.getString("id")), MessageReceiver.BUNGEE.name());
					return;
				}
				if (serverManager.isProxy) {
					NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_PROXY_SILENT.name())
							.set("id", pluginMessage.getString("id")), MessageReceiver.BUNGEE.name());
					console.sendMessage(NandiCloud.PREFIX + "Second Proxy found, stopping it.");
					return;
				}
				console.sendMessage(NandiCloud.PREFIX + "Proxy registered");
				for (ServerGroupImplementation serverGroupImplementation : serverManager.serverGroups.values()) {
					PluginMessage messageServer = new PluginMessage(MessageType.CORE_ADD_SERVERGROUP.name());
					messageServer.
							set("id", serverGroupImplementation.getId()).
							set("name", serverGroupImplementation.getName()).
							set("servers", (HashSet<ServerObjectLink>) serverGroupImplementation.getServersLink()).
							set("max_servers", serverGroupImplementation.getMaxServers()).
							set("static", serverGroupImplementation.isStatic()).
							set("ram", serverGroupImplementation.getRam());
					NandiCloudAPI.getMessageAPI().sendMessageToBungee(messageServer);
				}
				for (ServerObjectImplementation serverObjectImplementation : serverManager.serverObjects.values()) {
					PluginMessage messageServer = new PluginMessage(MessageType.CORE_ADD_SERVER.name());
					messageServer.
							set("name", serverObjectImplementation.getName()).
							set("id", serverObjectImplementation.getId()).
							set("players", (HashSet<PlayerObjectLink>) serverObjectImplementation.getOnlinePlayersLink()).
							set("max_players", serverObjectImplementation.getMaxPlayerCount()).
							set("address", serverObjectImplementation.getSocketAddress()).
							set("group", serverObjectImplementation.getGroupLink()).
							set("extra", serverObjectImplementation.getExtra()).
							set("state", serverObjectImplementation.getState());
					NandiCloudAPI.getMessageAPI().sendMessageToBungee(messageServer);
				}
				serverManager.setProxy(true);
				break;
			}
			case UNREGISTER_PROXY_CORE: {
				console.sendMessage(NandiCloud.PREFIX + "Proxy disconnected");
				try {
					if (serverManager.isProxyConfig)
						serverManager.startProxy();
				} catch (IOException e) {
					NandiCloud.printError(e);
				}
				serverManager.setProxy(false);
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
		}
	}
}
