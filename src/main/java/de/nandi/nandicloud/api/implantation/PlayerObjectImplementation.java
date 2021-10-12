package de.nandi.nandicloud.api.implantation;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.identifiable.LinkableObject;
import de.nandi.nandicloud.api.identifiable.PlayerObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerObjectLink;
import de.nandi.nandicloud.api.objects.PlayerObject;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;

import java.net.InetAddress;
import java.util.UUID;

public class PlayerObjectImplementation implements PlayerObject, LinkableObject<PlayerObject> {

	private final String name;
	private final UUID uuid;
	private final InetAddress ipAddress;
	private ServerObjectLink server;
	private boolean online;

	public PlayerObjectImplementation(String name, UUID uuid, ServerObjectLink server, InetAddress ipAddress, boolean online) {
		this.name = name;
		this.uuid = uuid;
		this.server = server;
		this.ipAddress = ipAddress;
		this.online = online;
	}

	@Override
	public String getId() {
		return uuid.toString();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}

	@Override
	public ServerObject getServer() {
		return server.resolve();
	}

	public void setServer(ServerObjectLink server) {
		this.server = server;
	}

	@Override
	public InetAddress getIpAddress() {
		return ipAddress;
	}

	@Override
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	@Override
	public void sendToServer(ServerObject serverObject) {
		NandiCloudAPI.getMessageAPI().sendMessageToCore(new PluginMessage(MessageType.PROXY_SEND_PLAYER.name()).
				set("player", uuid).set("server", serverObject.getName()));
	}

	@Override
	public void sendMessage(String message) {
		NandiCloudAPI.getMessageAPI().sendMessageToCore(new PluginMessage(MessageType.PROXY_SEND_MESSAGE.name()).
				set("player", uuid).set("message", message));
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public PlayerObjectLink toLink() {
		return new PlayerObjectLink(this);
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PlayerObjectImplementation that = (PlayerObjectImplementation) o;

		return uuid.equals(that.uuid);
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
}
