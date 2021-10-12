package de.nandi.nandicloud.bungee.connecting;

import de.nandi.nandicloud.api.api.PrivateGeneralAPI;
import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.bungee.main.NandiCloudBungee;
import de.nandi.nandicloud.core.main.NandiCloud;
import net.md_5.bungee.api.ProxyServer;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BungeeClient {

	private final NandiCloudBungee plugin;
	private Socket client;
	private ObjectOutputStream clientOut;

	public BungeeClient(NandiCloudBungee plugin) {
		this.plugin = plugin;
		connectAndListen(plugin);
	}

	@SuppressWarnings("unchecked")
	private void connectAndListen(NandiCloudBungee plugin) {
		String host = "localhost";
		int portServer = 40010;
		try {
			client = new Socket(host, portServer);
			clientOut = new ObjectOutputStream(client.getOutputStream());
			plugin.setConnected(true);
			ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
				try (ObjectInputStream clientIn = new ObjectInputStream(client.getInputStream())) {
					while (plugin.isReceiving()) {
						Object messageIn = clientIn.readObject();
						if (!(messageIn instanceof Map))
							continue;
						Map<String, Object> addressedData = (Map<String, Object>) messageIn;
						if (!(
								addressedData.get("recipient").equals(MessageReceiver.BUNGEE.name()) ||
										addressedData.get("recipient").equals(MessageReceiver.ALL_AND_PROXY.name()) ||
										addressedData.get("recipient").equals(MessageReceiver.ALL.name())
						))
							continue;
						AddressedPluginMessage addressedPluginMessage = new AddressedPluginMessage(
								(String) addressedData.get("sender"), (String) addressedData.get("recipient"),
								new PluginMessage((String) addressedData.get("type"), (Map<String, Object>) addressedData.get("data")));
						plugin.getBungeeMessageAPIInstance().getListeners().values().forEach(type -> {
							for (String registeredTypes : type) {
								if (registeredTypes.equals(addressedData.get("type")) ||
										registeredTypes.equalsIgnoreCase(MessageType.ALL.name())) {
									try {
										PrivateGeneralAPI.getKeyFromValue(plugin.getBungeeMessageAPIInstance().getListeners(), type)
												.onPluginMessage(addressedPluginMessage);
									} catch (Exception e) {
										e.printStackTrace();
									}
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
				System.out.println(NandiCloud.PREFIX + "Stopping Bungee");
				System.exit(0);
				return;
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
