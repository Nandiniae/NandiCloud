package de.nandi.nandicloud.api.objects.message;

public class AddressedPluginMessage {

	private final String sender;
	private final String recipient;
	private final PluginMessage message;


	public AddressedPluginMessage(String sender, String recipient, PluginMessage message) {
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
	}

	public String getSender() {
		return sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public PluginMessage getMessage() {
		return message;
	}

}
