package de.nandi.nandicloud.api.objects.message;

import java.util.HashMap;
import java.util.Map;

public enum MessageReceiver {

	CORE,
	BUNGEE,
	ALL_SERVERS,
	ALL_AND_PROXY,
	ALL_AND_CORE,
	ALL;

	private static final Map<String, MessageReceiver> byName;

	static {
		byName = new HashMap<>();
		for (int i = 0; i < values().length; ++i) {
			byName.put(values()[i] + "", values()[i]);
		}
	}

	public static MessageReceiver getByName(String name) {
		return byName.get(name);
	}

}
