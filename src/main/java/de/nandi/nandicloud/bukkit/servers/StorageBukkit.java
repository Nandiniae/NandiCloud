package de.nandi.nandicloud.bukkit.servers;

import de.nandi.nandicloud.api.identifiable.IdentifiableObjectStorage;
import de.nandi.nandicloud.api.implantation.PlayerObjectImplementation;
import de.nandi.nandicloud.api.implantation.ServerGroupImplementation;
import de.nandi.nandicloud.api.implantation.ServerObjectImplementation;

public class StorageBukkit {
	public final IdentifiableObjectStorage<ServerGroupImplementation> serverGroups;
	public final IdentifiableObjectStorage<ServerObjectImplementation> serverObjects;
	public final IdentifiableObjectStorage<PlayerObjectImplementation> playerObjects;


	public StorageBukkit() {
		serverObjects = new IdentifiableObjectStorage<>();
		serverGroups = new IdentifiableObjectStorage<>();
		playerObjects = new IdentifiableObjectStorage<>();
	}
}
