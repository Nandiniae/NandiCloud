package de.nandi.nandicloud.api.objects.events;

import java.util.HashMap;
import java.util.Map;

public enum EventType {

	PLAYER_CONNECT,
	PLAYER_DISCONNECT,
	SERVER_STATE_CHANGE,
	SERVER_EXTRA_CHANGE,
	SERVER_ONLINE_PLAYER_CHANGE,
	SERVER_REGISTER,
	SERVER_UNREGISTER;

	private static final Map<String, EventType> byName;

	static {
		byName = new HashMap<>();
		for (int i = 0; i < values().length; ++i) {
			byName.put(values()[i] + "", values()[i]);
		}
	}

	public static EventType getByName(String name) {
		return byName.get(name);
	}


}
