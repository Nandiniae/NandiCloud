package de.nandi.nandicloud.api.api;

import de.nandi.nandicloud.api.objects.events.Listener;

public interface EventAPI {

	/**
	 * Registers an event listener which will be notified on events
	 *
	 * @param listener The event listener which shall be registered
	 */
	void registerListener(Listener listener);

	/**
	 * Unregisters a registered event listener which will no longer be notified on events
	 *
	 * @param listener The event listener which shall be unregistered
	 */
	void unregisterListener(Listener listener);


}
