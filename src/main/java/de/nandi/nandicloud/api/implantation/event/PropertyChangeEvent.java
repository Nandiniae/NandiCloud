package de.nandi.nandicloud.api.implantation.event;

import de.nandi.nandicloud.api.identifiable.IdentifiableObject;
import de.nandi.nandicloud.api.objects.events.Event;

public abstract class PropertyChangeEvent<I extends IdentifiableObject, T> implements Event {

	private String instanceId;
	private T oldValue;
	private T newValue;

	public PropertyChangeEvent() {

	}

	public PropertyChangeEvent(String instanceId) {
		this.instanceId = instanceId;
	}

	public PropertyChangeEvent(I instance) {
		this(instance.getId());
	}

	public PropertyChangeEvent(String instanceId, T oldValue, T newValue) {
		this(instanceId);
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public PropertyChangeEvent(I instance, T oldValue, T newValue) {
		this(instance.getId(), oldValue, newValue);
	}

	protected String getInstanceId() {
		return instanceId;
	}

	public abstract I getInstance();

	public T getOldValue() {
		return oldValue;
	}

	public T getNewValue() {
		return newValue;
	}
}
