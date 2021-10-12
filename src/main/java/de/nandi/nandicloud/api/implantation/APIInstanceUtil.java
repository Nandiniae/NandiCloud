package de.nandi.nandicloud.api.implantation;

import de.nandi.nandicloud.api.api.*;

import java.lang.reflect.Field;

public class APIInstanceUtil {

	public static void setMessageInstance(MessageAPI instance) throws NoSuchFieldException, IllegalAccessException {
		Field field = NandiCloudAPI.class.getDeclaredField("messageAPI");
		field.setAccessible(true);
		field.set(null, instance);
	}

	public static void setCoreInstance(CoreAPI instance) throws NoSuchFieldException, IllegalAccessException {
		Field field = NandiCloudAPI.class.getDeclaredField("coreAPI");
		field.setAccessible(true);
		field.set(null, instance);
	}

	public static void setUniversalInstance(UniversalAPI instance) throws NoSuchFieldException, IllegalAccessException {
		Field field = NandiCloudAPI.class.getDeclaredField("universalAPI");
		field.setAccessible(true);
		field.set(null, instance);
	}

	public static void setEventInstance(EventAPI instance) throws NoSuchFieldException, IllegalAccessException {
		Field field = NandiCloudAPI.class.getDeclaredField("eventAPI");
		field.setAccessible(true);
		field.set(null, instance);
	}

	public static void setBukkitInstance(BukkitAPI instance) throws NoSuchFieldException, IllegalAccessException {
		Field field = NandiCloudAPI.class.getDeclaredField("bukkitAPI");
		field.setAccessible(true);
		field.set(null, instance);
	}


}
