package de.nandi.nandicloud.bungee.listeners;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.objects.ServerGroupObject;
import de.nandi.nandicloud.api.objects.ServerObject;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.stream.Collectors;

public class LobbyJoinListener implements Listener {
	private final Set<UUID> pending;

	public LobbyJoinListener() {
		pending = new HashSet<>();
	}

	private boolean isPending(UUID uuid) {
		return pending.contains(uuid);
	}

	@EventHandler
	public void onPlayerConnect(PostLoginEvent event) {
		pending.add(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onServerConnect(ServerConnectEvent event) {
		if (!isPending(event.getPlayer().getUniqueId())) return;

		ProxiedPlayer player = event.getPlayer();
		ServerObject lobby = getFreeLobby(player);
		if (lobby == null) {
			player.disconnect(new TextComponent("§cNo LobbyServer found"));
			pending.remove(player.getUniqueId());
			event.setCancelled(true);
			return;
		}
		ServerInfo info = ProxyServer.getInstance().getServerInfo(lobby.getName());
		if (info == null) {
			player.disconnect(new TextComponent("§cNo LobbyServer found"));
			pending.remove(player.getUniqueId());
			event.setCancelled(true);
			return;
		}
		event.setTarget(info);
		pending.remove(player.getUniqueId());
	}

	private ServerObject getFreeLobby(ProxiedPlayer player) {
		try {
			/*
			 *Note: this comparator imposes orderings that are inconsistent with equals
			 *Returns wich server has more players
			 */
			Comparator<ServerObject> c = (o1, o2) -> Integer.compare(o2.getOnlinePlayerCount(), o1.getOnlinePlayerCount());
			ServerGroupObject lobbyGroup = NandiCloudAPI.getUniversalAPI().getServerGroup("Lobby");
			if (lobbyGroup != null
					&& lobbyGroup.getServers().size() != 0) {
				List<ServerObject> servers = NandiCloudAPI.getUniversalAPI().getServerGroup("Lobby").getServers()
						.stream().sorted(c).collect(Collectors.toList());
				ServerObject s = null;
				for (ServerObject server : servers) {
					if (server.getOnlinePlayerCount() < server.getMaxPlayerCount() || player.hasPermission("lobby.join"))
						return server;
					s = server;
				}
				return s;
			}
			return NandiCloudAPI.getUniversalAPI().getServers().stream().findFirst().get();
		} catch (NullPointerException | NoSuchElementException e) {
			return null;
		}
	}

	@EventHandler
	public void onServerKick(ServerKickEvent event) {
		event.setCancelled(true);
		final ServerObject freeLobby = getFreeLobby(event.getPlayer());
		if (freeLobby == null) return;
		event.setCancelServer(ProxyServer.getInstance().getServerInfo(freeLobby.getName()));
	}


}
