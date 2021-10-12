package de.nandi.nandicloud.api.implantation.event.player;

import de.nandi.nandicloud.api.events.player.PlayerConnectEvent;
import de.nandi.nandicloud.api.objects.PlayerObject;
import de.nandi.nandicloud.api.objects.events.EventType;

public class PlayerConnectEventImplementation implements PlayerConnectEvent {

	private final PlayerObject playerObject;

	public PlayerConnectEventImplementation(PlayerObject playerObject) {
		this.playerObject = playerObject;
	}

	@Override
	public PlayerObject getPlayer() {
		return playerObject;
	}

	@Override
	public EventType getType() {
		return EventType.PLAYER_CONNECT;
	}
}
