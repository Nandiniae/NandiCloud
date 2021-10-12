package de.nandi.nandicloud.api.identifiable;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.objects.ServerGroupObject;

public class ServerGroupObjectLink extends IdentifiableLink<ServerGroupObject> {

	public ServerGroupObjectLink(ServerGroupObject serverGroupObject) {
		this(serverGroupObject.getId(), serverGroupObject.getName());
	}

	public ServerGroupObjectLink(String id, String name) {
		super(id, name);
	}

	@Override
	ServerGroupObject findTarget() {
		return NandiCloudAPI.getUniversalAPI().getServerGroup(getId());
	}

}
