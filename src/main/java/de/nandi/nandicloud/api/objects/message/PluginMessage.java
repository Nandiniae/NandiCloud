package de.nandi.nandicloud.api.objects.message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PluginMessage {

	private final Map<String, Object> data;
	private String type;

	public PluginMessage(String type) {
		this(type, new HashMap<>());
	}

	public PluginMessage(String type, Map<String, Object> data) {
		this.type = type;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public PluginMessage setType(String type) {
		this.type = type;
		return this;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Object get(String key) {
		return getData().get(key);
	}

	public Object getObject(String key) {
		return get(key);
	}

	public Set<String> getKeys() {
		return getData().keySet();
	}

	public boolean containsProperty(String key) {
		return getData().containsKey(key);
	}

	public Boolean getBoolean(String key) {
		Object object = getObject(key);
		if (object == null) return null;
		return (Boolean) object;
	}

	public Integer getInteger(String key) {
		Object object = getObject(key);
		if (object == null) return null;
		return ((Number) object).intValue();
	}

	public Long getLong(String key) {
		Object object = getObject(key);
		if (object == null) return null;
		return ((Number) object).longValue();
	}

	public Short getShort(String key) {
		Object object = getObject(key);
		if (object == null) return null;
		return ((Number) object).shortValue();
	}

	public Double getDouble(String key) {
		Object object = getObject(key);
		if (object == null) return null;
		return ((Number) object).doubleValue();
	}

	public Float getFloat(String key) {
		Object object = getObject(key);
		if (object == null) return null;
		return ((Number) object).floatValue();
	}

	public String getString(String key) {
		return (String) getObject(key);
	}

	public PluginMessage set(String key, Serializable value) {
		getData().put(key, value);
		return this;
	}

	public PluginMessage setIfCondition(String key, Serializable value, boolean condition) {
		if (!condition) return this;
		return set(key, value);
	}

	public PluginMessage setIfNotNull(String key, Serializable value) {
		return setIfCondition(key, value, value != null);
	}

	public PluginMessage setIfAbsent(String key, Serializable value) {
		return setIfCondition(key, value, !containsProperty(key));
	}
}
