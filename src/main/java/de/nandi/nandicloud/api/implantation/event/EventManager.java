package de.nandi.nandicloud.api.implantation.event;

import de.nandi.nandicloud.api.api.EventAPI;
import de.nandi.nandicloud.api.objects.events.Event;
import de.nandi.nandicloud.api.objects.events.EventHandler;
import de.nandi.nandicloud.api.objects.events.Listener;
import de.nandi.nandicloud.core.main.NandiCloud;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;

public class EventManager implements EventAPI {

	private final Collection<Listener> listeners;
	private final boolean core;

	public EventManager(boolean core) {
		listeners = new LinkedHashSet<>();
		this.core = core;
	}

	@Override
	public void registerListener(Listener listener) {
		if (getListeners().contains(listener)) return;
		getListeners().add(listener);
	}

	@Override
	public void unregisterListener(Listener listener) {
		if (!getListeners().contains(listener)) return;
		getListeners().remove(listener);
	}

	public void callEvent(Event event) {
		fireEvent(event);
	}

	private void fireEvent(Event event) {
		for (Listener listener : getListeners()) {
			Class<? extends Listener> c = listener.getClass();
			final Method[] methods = c.getDeclaredMethods();
			for (Method method : methods) {
				try {
					EventHandler eventHandler = method.getAnnotation(EventHandler.class);
					if (eventHandler == null) continue;
					if (method.getParameterTypes().length != 1 || !method.getParameterTypes()[0].isAssignableFrom(event.getClass()))
						continue;
					method.invoke(listener, event);
				} catch (Exception e) {
					if (core)
						NandiCloud.printError(e);
					else
						e.printStackTrace();
				}
			}
		}
	}

	public Collection<Listener> getListeners() {
		return listeners;
	}

}
