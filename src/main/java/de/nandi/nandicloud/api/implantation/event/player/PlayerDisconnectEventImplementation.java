package de.nandi.nandicloud.api.implantation.event.player;

import de.nandi.nandicloud.api.events.player.PlayerDisconnectEvent;
import de.nandi.nandicloud.api.objects.PlayerObject;
import de.nandi.nandicloud.api.objects.events.EventType;

public class PlayerDisconnectEventImplementation implements PlayerDisconnectEvent {

	private final PlayerObject playerObject;

	public PlayerDisconnectEventImplementation(PlayerObject playerObject) {
		this.playerObject = playerObject;
	}

	@Override
	public PlayerObject getPlayer() {
		return playerObject;
	}

	@Override
	public EventType getType() {
		return EventType.PLAYER_DISCONNECT;
	}
}
