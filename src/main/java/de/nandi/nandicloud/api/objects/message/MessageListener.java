package de.nandi.nandicloud.api.objects.message;

public interface MessageListener {

	void onPluginMessage(AddressedPluginMessage addressedPluginMessage);
}
