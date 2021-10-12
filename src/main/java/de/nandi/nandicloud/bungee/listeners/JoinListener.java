package de.nandi.nandicloud.bungee.listeners;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.implantation.PlayerObjectImplementation;
import de.nandi.nandicloud.api.implantation.ServerObjectImplementation;
import de.nandi.nandicloud.api.implantation.event.EventManager;
import de.nandi.nandicloud.api.implantation.event.player.PlayerConnectEventImplementation;
import de.nandi.nandicloud.api.implantation.event.player.PlayerDisconnectEventImplementation;
import de.nandi.nandicloud.api.implantation.event.server.ServerOnlinePlayerCountChangeEventImplementation;
import de.nandi.nandicloud.api.objects.events.EventType;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bungee.servers.StorageBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;

import static de.nandi.nandicloud.api.api.NandiCloudAPI.getUniversalAPI;

public class JoinListener implements Listener {
	private final Set<UUID> pending;
	private final Map<UUID, String> previousServer;
	private final StorageBungee serverManagerBungee;

	public JoinListener(StorageBungee serverManagerBungee) {
		this.serverManagerBungee = serverManagerBungee;
		pending = new HashSet<>();
		previousServer = new HashMap<>();
	}

	private boolean isPending(UUID uuid) {
		return pending.contains(uuid);
	}

	@EventHandler
	public void onPlayerConnect(PostLoginEvent event) {
		pending.add(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onServerSwitchEvent(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if (isPending(event.getPlayer().getUniqueId())) { // Join
			NandiCloudAPI.getMessageAPI().sendMessageToServer(
					new PluginMessage(MessageType.ADD_PLAYER.name())
							.set("name", player.getName())
							.set("uuid", player.getUniqueId())
							.set("server", player.getServer().getInfo().getName())
							.set("address", player.getAddress().getAddress())
							.set("online", true)
					, MessageReceiver.ALL_AND_CORE.name());
			PlayerObjectImplementation playerObject = new PlayerObjectImplementation(
					player.getName(),
					player.getUniqueId(),
					((ServerObjectImplementation) getUniversalAPI().getServer(player.getServer().getInfo().getName())).toLink(),
					player.getAddress().getAddress(),
					true
			);
			serverManagerBungee.playerObjects.add(playerObject);
			((ServerObjectImplementation) playerObject.getServer()).addPlayer(playerObject.toLink());
			pending.remove(event.getPlayer().getUniqueId());
			EventManager eventManager = (EventManager) NandiCloudAPI.getEventAPI();
			eventManager.callEvent(new ServerOnlinePlayerCountChangeEventImplementation(
					playerObject.getServer(),
					playerObject.getServer().getOnlinePlayerCount() - 1,
					playerObject.getServer().getOnlinePlayerCount()
			));
			NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
							.set("event", EventType.SERVER_ONLINE_PLAYER_CHANGE.name())
							.set("server", playerObject.getServer().getName())
							.set("old", playerObject.getServer().getOnlinePlayerCount() - 1)
							.set("new", playerObject.getServer().getOnlinePlayerCount()),
					MessageReceiver.ALL_AND_CORE.name());

			eventManager.callEvent(new PlayerConnectEventImplementation(playerObject));
			NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
							.set("event", EventType.PLAYER_CONNECT.name())
							.set("uuid", playerObject.getUuid()),
					MessageReceiver.ALL_AND_CORE.name());
		} else { // Server change
			PlayerObjectImplementation playerObject = serverManagerBungee.playerObjects.getById(player.getUniqueId().toString());
			ServerObjectImplementation serverFrom = serverManagerBungee.serverObjects.getByName(previousServer.get(player.getUniqueId()));
			ServerObjectImplementation serverTo = serverManagerBungee.serverObjects.getByName(player.getServer().getInfo().getName());
			serverFrom.removePlayer(playerObject.toLink());
			serverTo.addPlayer(playerObject.toLink());
			serverManagerBungee.playerObjects.update(playerObject);//NEEDED
			NandiCloudAPI.getMessageAPI().sendMessageToServer(
					new PluginMessage(MessageType.PLAYER_CHANGE_SERVER.name())
							.set("uuid", playerObject.getUuid().toString())
							.set("serverTo", serverTo.getName())
							.set("serverFrom", serverFrom.getName())
					, MessageReceiver.ALL_AND_CORE.name());
			EventManager eventManager = (EventManager) NandiCloudAPI.getEventAPI();
			eventManager.callEvent(new ServerOnlinePlayerCountChangeEventImplementation(
					serverFrom,
					serverFrom.getOnlinePlayerCount() + 1,
					serverFrom.getOnlinePlayerCount()
			));
			eventManager.callEvent(new ServerOnlinePlayerCountChangeEventImplementation(
					serverTo,
					serverTo.getOnlinePlayerCount() - 1,
					serverTo.getOnlinePlayerCount()
			));
			NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
							.set("event", EventType.SERVER_ONLINE_PLAYER_CHANGE.name())
							.set("server", serverFrom.getName())
							.set("old", serverFrom.getOnlinePlayerCount() + 1)
							.set("new", serverFrom.getOnlinePlayerCount()),
					MessageReceiver.ALL_AND_CORE.name());
			NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
							.set("event", EventType.SERVER_ONLINE_PLAYER_CHANGE.name())
							.set("server", serverTo.getName())
							.set("old", serverTo.getOnlinePlayerCount() - 1)
							.set("new", serverTo.getOnlinePlayerCount()),
					MessageReceiver.ALL_AND_CORE.name());
		}
		previousServer.put(player.getUniqueId(), player.getServer().getInfo().getName());
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerDisconnectEvent event) {
		if (isPending(event.getPlayer().getUniqueId()))
			return;
		PlayerObjectImplementation player = serverManagerBungee.playerObjects.getById(event.getPlayer().getUniqueId().toString());
		if (player == null)
			return;
		player.setOnline(false);
		((ServerObjectImplementation) player.getServer()).removePlayer(player.toLink());
		serverManagerBungee.playerObjects.remove(player);

		EventManager eventManager = (EventManager) NandiCloudAPI.getEventAPI();
		eventManager.callEvent(new ServerOnlinePlayerCountChangeEventImplementation(
				player.getServer(),
				player.getServer().getOnlinePlayerCount() + 1,
				player.getServer().getOnlinePlayerCount()
		));
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
						.set("event", EventType.SERVER_ONLINE_PLAYER_CHANGE.name())
						.set("server", player.getServer().getName())
						.set("old", player.getServer().getOnlinePlayerCount() + 1)
						.set("new", player.getServer().getOnlinePlayerCount()),
				MessageReceiver.ALL_AND_CORE.name());
		eventManager.callEvent(new PlayerDisconnectEventImplementation(player));
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
						.set("event", EventType.PLAYER_DISCONNECT.name())
						.set("uuid", player.getUuid()),
				MessageReceiver.ALL_AND_CORE.name());
		NandiCloudAPI.getMessageAPI().sendMessageToServer(
				new PluginMessage(MessageType.REMOVE_PLAYER.name())
						.set("uuid", player.getUuid().toString())
						.set("server", player.getServer().getName())
				, MessageReceiver.ALL_AND_CORE.name());
	}


}
