package de.nandi.nandicloud.api.objects.message;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {

	ALL,
	STOP_SERVER,
	STOP_SERVER_SILENT,
	STOP_PROXY,
	STOP_PROXY_SILENT,
	SEND_REGISTER,
	REGISTER_SERVER_CORE,
	REGISTER_PROXY_CORE,
	REGISTER_SERVER_PROXY,
	UNREGISTER_SERVER_CORE,
	UNREGISTER_PROXY_CORE,
	UNREGISTER_SERVER_PROXY,
	PROXY_SEND_PLAYER,
	PROXY_SEND_MESSAGE,
	CORE_ADD_SERVER,
	CORE_REMOVE_SERVER,
	CORE_ADD_SERVERGROUP,
	CORE_REMOVE_SERVERGROUP,
	ADD_PLAYER,
	REMOVE_PLAYER,
	PLAYER_CHANGE_SERVER,
	SERVER_CHANGE_STATE,
	SERVER_CHANGE_EXTRA,
	SERVERGROUP_CHANGE_MAXSERVERS,
	SERVERGROUP_CHANGE_STATIC,
	SERVERGROUP_CHANGE_RAM,
	EVENT;

	private static final Map<String, MessageType> byName;

	static {
		byName = new HashMap<>();
		for (int i = 0; i < values().length; ++i) {
			byName.put(values()[i] + "", values()[i]);
		}
	}

	public static MessageType getByName(String name) {
		return byName.get(name);
	}

}
