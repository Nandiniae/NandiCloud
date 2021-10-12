package de.nandi.nandicloud.bungee.api;

import de.nandi.nandicloud.api.api.UniversalAPI;
import de.nandi.nandicloud.api.objects.PlayerObject;
import de.nandi.nandicloud.api.objects.ServerGroupObject;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.bungee.servers.StorageBungee;

import java.util.*;

public class BungeeUniversalAPIInstance implements UniversalAPI {

	private final StorageBungee serverManager;

	public BungeeUniversalAPIInstance(StorageBungee serverManager) {
		this.serverManager = serverManager;
	}


	@Override
	public Collection<ServerGroupObject> getServerGroups() {
		return Collections.unmodifiableSet(new HashSet<>(serverManager.serverGroups.values()));
	}

	@Override
	public ServerGroupObject getServerGroup(String identifier) {
		return serverManager.serverGroups.getByIdentifier(identifier);
	}

	@Override
	public ServerObject getServer(String identifier) {
		return serverManager.serverObjects.getByIdentifier(identifier);
	}

	@Override
	public Collection<ServerObject> getServers() {
		return Collections.unmodifiableSet(new HashSet<>(serverManager.serverObjects.values()));
	}

	@Override
	public PlayerObject getPlayer(UUID uuid) {
		return serverManager.playerObjects.getById(uuid.toString());
	}

	@Override
	public PlayerObject getPlayer(String name) {
		return serverManager.playerObjects.getByName(name);
	}

	@Override
	public Collection<PlayerObject> getPlayers() {
		return new ArrayList<>(serverManager.playerObjects.values());
	}


}
