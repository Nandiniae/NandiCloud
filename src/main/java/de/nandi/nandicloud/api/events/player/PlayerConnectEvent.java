package de.nandi.nandicloud.api.events.player;

import de.nandi.nandicloud.api.objects.PlayerObject;
import de.nandi.nandicloud.api.objects.events.Event;

public interface PlayerConnectEvent extends Event {

	PlayerObject getPlayer();

}
