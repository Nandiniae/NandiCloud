package de.nandi.nandicloud.api.identifiable;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.objects.PlayerObject;

import java.util.UUID;

public class PlayerObjectLink extends IdentifiableLink<PlayerObject> {

	public PlayerObjectLink(PlayerObject playerObject) {
		this(playerObject.getId(), playerObject.getName());
	}

	public PlayerObjectLink(String id, String name) {
		super(id, name);
	}

	@Override
	PlayerObject findTarget() {
		return NandiCloudAPI.getUniversalAPI().getPlayer(UUID.fromString(getId()));
	}

}
