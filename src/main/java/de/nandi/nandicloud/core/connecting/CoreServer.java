package de.nandi.nandicloud.core.connecting;


import de.nandi.nandicloud.api.api.PrivateGeneralAPI;
import de.nandi.nandicloud.api.objects.message.AddressedPluginMessage;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.core.main.NandiCloud;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CoreServer {

	private final List<Socket> connections;
	private final List<ObjectOutputStream> serverOuts;
	private ServerSocket server;
	private boolean receiving;

	public CoreServer() {
		connections = new ArrayList<>();
		serverOuts = new ArrayList<>();
		receiving = true;
		connectAndListen();
	}

	@SuppressWarnings("unchecked")
	private void connectAndListen() {
		String host = "localhost";
		int portServer = 40010;
		try {
			server = new ServerSocket(portServer, 50, InetAddress.getByName(host));

			CompletableFuture.runAsync(() -> {
				while (receiving) {
					Socket connectionTemp = null;
					ObjectOutputStream serverOut;
					try {
						connections.add(connectionTemp = server.accept());
						serverOuts.add(serverOut = new ObjectOutputStream(connectionTemp.getOutputStream()));
						Socket connection = connectionTemp;
						CompletableFuture.runAsync(() -> {
							try (ObjectInputStream clientIn = new ObjectInputStream(connection.getInputStream())) {
								while (receiving) {
									Object messageIn = clientIn.readObject();
									if (!(messageIn instanceof Map))
										continue;
									Map<String, Object> addressedData = (Map<String, Object>) messageIn;
									String recipient = (String) addressedData.get("recipient");
									boolean forThis = recipient.equals(MessageReceiver.CORE.name()) ||
											recipient.equals(MessageReceiver.ALL_AND_CORE.name()) ||
											recipient.equals(MessageReceiver.ALL.name());
									boolean forOther = recipient.equals(MessageReceiver.ALL_AND_CORE.name()) ||
											recipient.equals(MessageReceiver.ALL.name());
									if (!forThis) {
										sendMessage(addressedData);
										continue;
									}
									if (forOther) {
										sendMessage(addressedData);
									}
									AddressedPluginMessage addressedPluginMessage = new AddressedPluginMessage(
											(String) addressedData.get("sender"), (String) addressedData.get("recipient"),
											new PluginMessage
													((String) addressedData.get("type"), (Map<String, Object>) addressedData.get("data")));
									NandiCloud.coreMessageAPIInstance.getListeners().values().forEach(type -> {
										for (String registeredTypes : type) {
											if (registeredTypes.equals(addressedData.get("type")) ||
													registeredTypes.equalsIgnoreCase(MessageType.ALL.name())) {
												try {
													PrivateGeneralAPI.getKeyFromValue(NandiCloud.coreMessageAPIInstance.getListeners(), type)
															.onPluginMessage(addressedPluginMessage);
												} catch (Exception e) {
													NandiCloud.printError(e);
												}
											}
										}
									});
								}
							} catch (Exception e) {
								if (!(e.getMessage().equals("Socket closed") || e.getMessage().equals("Connection reset")))
									NandiCloud.printError(e);
							} finally {
								try {
									serverOuts.remove(serverOut);
									serverOut.close();
								} catch (IOException e) {
									if (!e.getMessage().equals("Socket closed"))
										NandiCloud.printError(e);
								}
								try {
									connections.remove(connection);
									connection.close();
								} catch (IOException e) {
									if (!e.getMessage().equals("Socket closed"))
										NandiCloud.printError(e);
								}
							}
						});
					} catch (IOException e) {
						if (!e.getMessage().equals("Socket closed"))
							NandiCloud.printError(e);
						if (connectionTemp != null)
							try {
								connectionTemp.close();
							} catch (IOException ex) {
								if (!ex.getMessage().equals("Socket closed"))
									NandiCloud.printError(ex);
							}
					}
				}
				List<ObjectOutputStream> listCopy = (List<ObjectOutputStream>) new ArrayList<>(serverOuts).clone();
				for (ObjectOutputStream serverOut : serverOuts)
					if (serverOut != null) {
						try {
							listCopy.remove(serverOut);
							serverOut.close();
						} catch (IOException e) {
							if (!e.getMessage().equals("Socket closed"))
								NandiCloud.printError(e);
						}
					}
				serverOuts.clear();
				serverOuts.addAll(listCopy);
				List<Socket> socketList = (List<Socket>) new ArrayList<>(connections).clone();
				for (Socket connection : connections)
					if (connection != null) {
						try {
							socketList.remove(connection);
							connection.close();
						} catch (IOException e) {
							if (!e.getMessage().equals("Socket closed"))
								NandiCloud.printError(e);
						}
					}
				connections.clear();
				connections.addAll(socketList);
				try {
					server.close();
				} catch (IOException e) {
					if (!e.getMessage().equals("Socket closed"))
						NandiCloud.printError(e);
				}
			});
		} catch (IOException e) {
			if (!e.getMessage().equals("Socket closed"))
				NandiCloud.printError(e);
			if (server != null) {
				try {
					server.close();
				} catch (IOException ex) {
					if (!ex.getMessage().equals("Socket closed"))
						NandiCloud.printError(ex);
				}
			}
		}
	}

	public void sendMessage(AddressedPluginMessage addressedPluginMessage) {
		Map<String, Object> messageOut = new HashMap<>();
		messageOut.put("sender", addressedPluginMessage.getSender());
		messageOut.put("recipient", addressedPluginMessage.getRecipient());
		messageOut.put("type", addressedPluginMessage.getMessage().getType());
		messageOut.put("data", addressedPluginMessage.getMessage().getData());
		sendMessage(messageOut);
	}

	private void sendMessage(Map<String, Object> messageOut) {
		List<ObjectOutputStream> listCopy = (List<ObjectOutputStream>) new ArrayList<>(serverOuts).clone();
		for (ObjectOutputStream serverOut : serverOuts) {
			try {
				serverOut.writeObject(messageOut);
				serverOut.flush();
			} catch (IOException e) {
				if (!(e.getMessage().equals("Socket closed") || e.getMessage().contains("Datenübergabe unterbrochen")))
					NandiCloud.printError(e);
				try {
					listCopy.remove(serverOut);
					serverOut.close();
				} catch (IOException ex) {
					if (!(e.getMessage().equals("Socket closed") || e.getMessage().contains("Datenübergabe unterbrochen")))
						NandiCloud.printError(ex);
				}
			}
		}
		serverOuts.clear();
		serverOuts.addAll(listCopy);
	}

	public void notReceiving() {
		receiving = false;
	}
}
