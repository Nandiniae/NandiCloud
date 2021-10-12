package de.nandi.nandicloud.api.objects;

import de.nandi.nandicloud.api.identifiable.IdentifiableObject;

import java.net.InetAddress;
import java.util.UUID;

public interface PlayerObject extends IdentifiableObject {

	/**
	 * @return The player's Minecraft name
	 */
	String getName();

	/**
	 * @return The player's Minecraft UUID
	 */
	UUID getUuid();

	/**
	 * @return The server the player currently is connected to
	 */
	ServerObject getServer();

	/**
	 * @return The player's IP address
	 */
	InetAddress getIpAddress();

	/**
	 * Normally, a Player is always online if you are able to get its PlayerObject.
	 * However, when the Player is currently disconnecting, this will return false
	 */
	boolean isOnline();

	/**
	 * @param serverObject The server the player shall be sent to
	 */
	void sendToServer(ServerObject serverObject);

	/**
	 * @param message the message which is sent to the player
	 */
	void sendMessage(String message);


}
