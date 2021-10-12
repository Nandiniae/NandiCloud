package de.nandi.nandicloud.api.api;

import de.nandi.nandicloud.api.objects.PlayerObject;
import de.nandi.nandicloud.api.objects.ServerGroupObject;
import de.nandi.nandicloud.api.objects.ServerObject;

import java.util.Collection;
import java.util.UUID;

public interface UniversalAPI {

	/**
	 * @return A collection of {@link ServerGroupObject} which contains all server groups
	 */
	Collection<ServerGroupObject> getServerGroups();

	/**
	 * Use this to get a server group by name
	 *
	 * @param identifier The group's name or id, case-sensitive
	 * @return A {@link ServerGroupObject} corresponding to the given name
	 */
	ServerGroupObject getServerGroup(String identifier);

	/**
	 * Use this to get a server by name
	 *
	 * @param identifier The server's name or id, case-sensitive
	 * @return A {@link ServerObject} corresponding to the given name or id
	 */
	ServerObject getServer(String identifier);

	/**
	 * @return A collection of all servers in the network
	 */
	Collection<ServerObject> getServers();

	/**
	 * @param uuid The player's Minecraft UUID
	 * @return If the player is online, this will return a PlayerObject, else null
	 */
	PlayerObject getPlayer(UUID uuid);

	/**
	 * @param name The player's Minecraft name
	 * @return If the player is online, this will return a PlayerObject, else null
	 */
	PlayerObject getPlayer(String name);

	/**
	 * @return A collection of all players in the network
	 */
	Collection<PlayerObject> getPlayers();


}
