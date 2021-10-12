package de.nandi.nandicloud.api.api;

import de.nandi.nandicloud.core.util.ChatColorUtil;

import java.util.Map;
import java.util.Set;

public class PrivateGeneralAPI {

	public static final String PREFIX = ChatColorUtil.toLegacyText("§8[§cNandi§9Cloud§8] §r");

	public static <K, V> K getKeyFromValue(Map<K, V> map, Object value) {
		Set<K> keys = map.keySet();
		for (K key : keys) {
			if (map.get(key).equals(value)) {
				return key;
			}
		}
		return null;
	}


}
