package de.nandi.nandicloud.api.identifiable;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.objects.ServerObject;

public class ServerObjectLink extends IdentifiableLink<ServerObject> {

	public ServerObjectLink(ServerObject serverObject) {
		this(serverObject.getId(), serverObject.getName());
	}

	public ServerObjectLink(String id, String name) {
		super(id, name);
	}

	@Override
	ServerObject findTarget() {
		return NandiCloudAPI.getUniversalAPI().getServer(getId());
	}
}
