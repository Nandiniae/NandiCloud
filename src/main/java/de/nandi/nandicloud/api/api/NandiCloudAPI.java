package de.nandi.nandicloud.api.api;

@SuppressWarnings("unused")
public class NandiCloudAPI {

	private static MessageAPI messageAPI;
	private static CoreAPI coreAPI;
	private static UniversalAPI universalAPI;
	private static EventAPI eventAPI;
	private static BukkitAPI bukkitAPI;

	private NandiCloudAPI() {
	}


	public static MessageAPI getMessageAPI() {
		return messageAPI;
	}

	public static CoreAPI getCoreAPI() {
		return coreAPI;
	}

	public static UniversalAPI getUniversalAPI() {
		return universalAPI;
	}

	public static EventAPI getEventAPI() {
		return eventAPI;
	}

	public static BukkitAPI getBukkitAPI() {
		return bukkitAPI;
	}
}
