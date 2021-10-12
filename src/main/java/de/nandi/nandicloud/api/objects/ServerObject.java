package de.nandi.nandicloud.api.objects;

import de.nandi.nandicloud.api.identifiable.IdentifiableObject;
import de.nandi.nandicloud.api.objects.message.PluginMessage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;

public interface ServerObject extends IdentifiableObject {

	/**
	 * @return The server's name
	 */
	String getName();

	/**
	 * @return The server's unique id
	 */
	String getId();

	/**
	 * @return The group the server is part of
	 */
	ServerGroupObject getGroup();

	/**
	 * @return The server's state
	 */
	String getState();

	/**
	 * Sets the server's state and sends it to TimoCloud BungeeCord
	 *
	 * @param state The state, e.g. 'INGAME' or 'FULL'
	 */
	void setState(String state);

	/**
	 * An extra is a custom value users can set per API. An example use case would be 'Teaming' or 'NoTeaming'
	 */
	String getExtra();

	/**
	 * An extra is a custom value users can set per API. An example use case would be 'Teaming' or 'NoTeaming'
	 */
	void setExtra(String extra);


	/**
	 * @return A collection of all online players
	 */
	Collection<PlayerObject> getOnlinePlayers();

	/**
	 * The server's current online player count
	 */
	int getOnlinePlayerCount();

	/**
	 * The server's maximum player count
	 */
	int getMaxPlayerCount();

	/**
	 * @return The server's IP address and port players can connect to
	 */
	InetSocketAddress getSocketAddress();

	/**
	 * @return The server's IP address
	 */
	InetAddress getIpAddress();

	/**
	 * @return The server's port
	 */
	int getPort();

	/**
	 * Stops the server
	 */
	void stop();

	/**
	 * Send a plugin message to the server
	 *
	 * @param message The message which shall be sent
	 */
	void sendPluginMessage(PluginMessage message);

}
