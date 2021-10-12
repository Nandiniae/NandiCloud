package de.nandi.nandicloud.api.implantation.event;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.objects.ServerObject;

public abstract class ServerPropertyChangeEvent<T> extends PropertyChangeEvent<ServerObject, T> {

	public ServerPropertyChangeEvent(ServerObject instance, T oldValue, T newValue) {
		super(instance, oldValue, newValue);
	}

	public ServerPropertyChangeEvent(String instanceId, T oldValue, T newValue) {
		super(instanceId, oldValue, newValue);
	}

	@Override
	public ServerObject getInstance() {
		return NandiCloudAPI.getUniversalAPI().getServer(getInstanceId());
	}

	public ServerObject getServer() {
		return getInstance();
	}
}
