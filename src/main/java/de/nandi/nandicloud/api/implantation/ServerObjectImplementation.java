package de.nandi.nandicloud.api.implantation;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.identifiable.LinkableObject;
import de.nandi.nandicloud.api.identifiable.PlayerObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerGroupObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerObjectLink;
import de.nandi.nandicloud.api.objects.PlayerObject;
import de.nandi.nandicloud.api.objects.ServerGroupObject;
import de.nandi.nandicloud.api.objects.ServerObject;
import de.nandi.nandicloud.api.objects.events.EventType;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerObjectImplementation implements ServerObject, LinkableObject<ServerObject> {

	private final String name;
	private final String id;
	private final Set<PlayerObjectLink> onlinePlayers;
	private final InetSocketAddress socketAddress;
	private final ServerGroupObjectLink group;
	private String state;
	private String extra;
	private int maxPlayerCount;

	public ServerObjectImplementation(
			String name, String id, Set<PlayerObjectLink> onlinePlayers, int maxPlayerCount,
			InetSocketAddress socketAddress, ServerGroupObjectLink group) {
		this.name = name;
		this.id = id;
		this.onlinePlayers = onlinePlayers;
		this.maxPlayerCount = maxPlayerCount;
		this.socketAddress = socketAddress;
		this.group = group;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ServerGroupObject getGroup() {
		return group.resolve();
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public void setState(String state) {
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.SERVER_CHANGE_STATE.name())
						.set("server", getName())
						.set("state", state),
				MessageReceiver.ALL.name());
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
						.set("event", EventType.SERVER_STATE_CHANGE.name())
						.set("server", getName())
						.set("old", this.state)
						.set("new", state),
				MessageReceiver.ALL.name());
		this.state = state;
	}

	@Override
	public String getExtra() {
		return extra;
	}

	@Override
	public void setExtra(String extra) {
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.SERVER_CHANGE_EXTRA.name())
						.set("server", getName())
						.set("extra", extra),
				MessageReceiver.ALL.name());
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.EVENT.name())
						.set("event", EventType.SERVER_EXTRA_CHANGE.name())
						.set("server", getName())
						.set("old", this.extra)
						.set("new", extra),
				MessageReceiver.ALL.name());
		this.extra = extra;
	}

	@Override
	public List<PlayerObject> getOnlinePlayers() {
		return onlinePlayers.stream().map(PlayerObjectLink::resolve).collect(Collectors.toList());
	}

	public void setOnlinePlayers(Set<PlayerObjectLink> onlinePlayers) {
		this.onlinePlayers.clear();
		this.onlinePlayers.addAll(onlinePlayers);
	}

	public void addPlayer(PlayerObjectLink player) {
		onlinePlayers.add(player);
	}

	public void removePlayer(PlayerObjectLink player) {
		onlinePlayers.remove(player);
	}

	@Override
	public int getOnlinePlayerCount() {
		return onlinePlayers.size();
	}

	@Override
	public int getMaxPlayerCount() {
		return maxPlayerCount;
	}

	@Override
	public InetSocketAddress getSocketAddress() {
		return socketAddress;
	}

	@Override
	public InetAddress getIpAddress() {
		return socketAddress.getAddress();
	}

	@Override
	public int getPort() {
		return socketAddress.getPort();
	}

	@Override
	public void stop() {
		NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()), name);
	}

	@Override
	public void sendPluginMessage(PluginMessage message) {
		NandiCloudAPI.getMessageAPI().sendMessageToServer(message, name);
	}

	public void setStateInternally(String state) {
		this.state = state;
	}

	public void setExtraInternally(String extra) {
		this.extra = extra;
	}

	public void setMaxPlayerCountInternally(int i) {
		this.maxPlayerCount = i;
	}

	public Set<PlayerObjectLink> getOnlinePlayersLink() {
		return onlinePlayers;
	}

	public ServerGroupObjectLink getGroupLink() {
		return group;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public ServerObjectLink toLink() {
		return new ServerObjectLink(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ServerObjectImplementation that = (ServerObjectImplementation) o;

		return id.equals(that.id);

	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
