package de.nandi.nandicloud.bukkit.connecting;

import de.nandi.nandicloud.api.api.PrivateGeneralAPI;
import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bukkit.main.NandiCloudBukkit;
import de.nandi.nandicloud.core.main.NandiCloud;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BukkitClient {

	private final NandiCloudBukkit plugin;
	private Socket client;
	private ObjectOutputStream clientOut;

	public BukkitClient(NandiCloudBukkit plugin) {
		this.plugin = plugin;
		connectAndListen();
	}

	@SuppressWarnings("unchecked")
	private void connectAndListen() {
		String host = "localhost";
		int portServer = 40010;
		try {
			client = new Socket(host, portServer);
			clientOut = new ObjectOutputStream(client.getOutputStream());
			plugin.setConnected(true);
			CompletableFuture.runAsync(() -> {
				try (ObjectInputStream clientIn = new ObjectInputStream(client.getInputStream())) {
					while (plugin.isReceiving()) {
						Object messageIn = clientIn.readObject();
						if (!(messageIn instanceof Map))
							continue;
						Map<String, Object> addressedData = (Map<String, Object>) messageIn;
						String recipient = (String) addressedData.get("recipient");
						String sender = (String) addressedData.get("sender");
						String serverName = plugin.getServername();
						boolean toAll = //if this message is send to all servers
								recipient.equals(MessageReceiver.ALL.name())
										|| recipient.equals(MessageReceiver.ALL_SERVERS.name())
										|| recipient.equals(MessageReceiver.ALL_AND_PROXY.name())
										|| recipient.equals(MessageReceiver.ALL_AND_CORE.name());
						boolean toThis = //if this message is for this server: when this is the recipient,
								//or it is for all servers and not from this.
								recipient.equalsIgnoreCase(serverName) || (toAll && !sender.equalsIgnoreCase(serverName));
						if (!toThis)
							continue;
						AddressedPluginMessage addressedPluginMessage = new AddressedPluginMessage(
								sender, recipient, new PluginMessage
								((String) addressedData.get("type"), (Map<String, Object>) addressedData.get("data")));
						plugin.getBukkitMessageAPIInstance().getListeners().values().forEach(type -> {
							for (String registeredTypes : type) {
								if (registeredTypes.equals(addressedData.get("type"))
										|| registeredTypes.equalsIgnoreCase(MessageType.ALL.name())) {
									try {
										PrivateGeneralAPI.getKeyFromValue(plugin.getBukkitMessageAPIInstance().getListeners(), type)
												.onPluginMessage(addressedPluginMessage);
									} catch (Exception e) {
										e.printStackTrace();
									}
									break;
								}
							}
						});
					}
				} catch (Exception e) {
					if (!("Connection reset".equals(e.getMessage()) || e instanceof EOFException))
						e.printStackTrace();
				} finally {
					try {
						clientOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			plugin.setConnected(false);
			if (!(e instanceof ConnectException)) {
				e.printStackTrace();
			} else {
				System.out.println(NandiCloud.PREFIX + "Stopping Server");
				System.exit(0);
			}
			if (clientOut != null) {
				try {
					clientOut.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (client != null) {
				try {
					client.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public void sendMessage(AddressedPluginMessage addressedPluginMessage) throws IOException {
		if (!plugin.isConnected())
			return;
		Map<String, Object> messageOut = new HashMap<>();
		messageOut.put("sender", addressedPluginMessage.getSender());
		messageOut.put("recipient", addressedPluginMessage.getRecipient());
		messageOut.put("type", addressedPluginMessage.getMessage().getType());
		messageOut.put("data", addressedPluginMessage.getMessage().getData());
		clientOut.writeObject(messageOut);
		clientOut.flush();
	}

}