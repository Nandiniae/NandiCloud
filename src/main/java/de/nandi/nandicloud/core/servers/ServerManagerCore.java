package de.nandi.nandicloud.core.servers;

import de.nandi.nandicloud.api.identifiable.IdentifiableObjectStorage;
import de.nandi.nandicloud.api.implantation.PlayerObjectImplementation;
import de.nandi.nandicloud.api.implantation.ServerGroupImplementation;
import de.nandi.nandicloud.api.implantation.ServerObjectImplementation;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.core.main.NandiCloud;
import de.nandi.nandicloud.core.util.JSONUtil;
import de.nandi.nandicloud.core.util.RandomIdGenerator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ServerManagerCore {

	public final File configDirectory, proxyDirectory, proxyConfig, temporaryDirectory, serversConfig, staticDirectory, serverGroupDirectory;
	public final IdentifiableObjectStorage<ServerGroupImplementation> serverGroups;
	public final IdentifiableObjectStorage<ServerObjectImplementation> serverObjects;
	public final IdentifiableObjectStorage<PlayerObjectImplementation> playerObjects;
	public final Set<Integer> notFreePorts;
	private final CommandSender console;
	private final int START_PORT = 41000;
	private final int MAX_PORT = 42000;
	public boolean isProxy = false;
	public boolean isProxyConfig = false;
	private int currentPort = START_PORT;

	public ServerManagerCore(CommandSender console) throws IOException {
		this.console = console;
		configDirectory = new File("configs"); // create config directory
		proxyDirectory = new File("proxy"); // create proxy directory
		temporaryDirectory = new File("temporary"); //create temporary directory
		serverGroupDirectory = new File("servers/templates"); //create serverGroup directory
		staticDirectory = new File("servers/static"); //create static directory
		serversConfig = new File("configs", "servers.json");
		proxyConfig = new File("configs", "proxy.json");


		configDirectory.mkdir();
		serverGroupDirectory.mkdirs();
		staticDirectory.mkdirs();
		deleteFiles(temporaryDirectory);
		temporaryDirectory.mkdir();
		serversConfig.createNewFile();
		proxyDirectory.mkdir();
		proxyConfig.createNewFile();

		serverGroups = new IdentifiableObjectStorage<>();
		serverObjects = new IdentifiableObjectStorage<>();
		playerObjects = new IdentifiableObjectStorage<>();
		notFreePorts = new HashSet<>();
		try {
			startProxy();
		} catch (Exception e) {
			NandiCloud.printError(e);
			return;
		}
		startServers();
	}

	public void startProxy() throws IOException {
		int ram;
		int max_players;
		try {
			JSONArray array = JSONUtil.getJSON(proxyConfig);
			JSONObject object = (JSONObject) array.get(0);
			ram = ((Number) object.get("ram")).intValue();
			max_players = ((Number) object.get("max_players")).intValue();
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			console.sendMessage(NandiCloud.PREFIX + "§cCould not start Servers because no Proxy exists.");
			throw new NullPointerException("No Proxy exists");
		}
		String name = "Proxy";
		isProxyConfig = true;

		String proxyID = name + "_" + RandomIdGenerator.generateId(); //create Proxy-ID
		File bungeeJar = new File(proxyDirectory, "BungeeCord.jar");
		if (!bungeeJar.exists()) {
			console.sendMessage(NandiCloud.PREFIX + "§cCould not start Proxy because BungeeCord.jar does not exist. " +
					"Please make sure to have a file called 'BungeeCord.jar' in your template.");
			throw new NoSuchFileException("BungeeCord.jar does not exist");
		}
		File plugins = new File(proxyDirectory, "/plugins/");
		plugins.mkdirs();
		File plugin = new File(plugins, "NandiCloud.jar");
		if (plugin.exists()) plugin.delete();
		Files.copy(new File(NandiCloud.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toPath(), plugin.toPath());

		File configFile = new File(proxyDirectory, "config.yml");
		configFile.createNewFile();
		Yaml yaml = new Yaml();
		Map<String, Object> config = yaml.load(new FileReader(configFile));
		if (config == null) config = new LinkedHashMap<>();
		List<Map<String, Object>> listeners = (List<Map<String, Object>>) config.get("listeners");
		config.put("player_limit", max_players);
		config.put("ip_forward", true);
		if (listeners == null) listeners = new ArrayList<>();
		Map<String, Object> map = listeners.size() == 0 ? new LinkedHashMap<>() : listeners.get(0);
		map.put("force_default_server", false);
		map.put("host", "0.0.0.0:" + 40000);
		map.put("max_players", max_players);
		if (listeners.size() == 0) listeners.add(map);
		config.put("listeners", listeners);
		try (FileWriter writer = new FileWriter(configFile)) {
			DumperOptions dumperOptions = new DumperOptions();
			dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			new Yaml(dumperOptions).dump(config, writer);
		}

		new ProcessBuilder("/bin/sh", "-c",
				"screen -mdS " + proxyID + " /bin/sh -c" +
						" 'cd " + proxyDirectory.getAbsolutePath() + " && java -server" +
						" -Xmx" + ram + "M -Dcom.mojang.eula.agree=true -Dnandicloud-proxyid=" + proxyID +
						" -jar BungeeCord.jar'"
		).start(); // start server as screen with ram
		console.sendMessage(NandiCloud.PREFIX + "Successfully started Proxy");
	}

	private void startServers() {
		int servers = 0;
		JSONArray array = JSONUtil.getJSON(serversConfig); // load JSON File
		for (Object o : array) { //get each server and start it
			JSONObject object = (JSONObject) o;
			String name = (String) object.get("name");
			int ram = ((Number) object.get("ram")).intValue();
			int onlineAmount = ((Number) object.get("onlineAmount")).intValue();
			boolean staticB = ((boolean) object.get("static"));
			if (staticB && onlineAmount > 1) {
				console.sendMessage(NandiCloud.PREFIX + "§cCould not start Server "
						+ name + " because static is true and onlineAmount is not 0 or 1");
				continue;
			}
			serverGroups.add(new ServerGroupImplementation(name, name, new HashSet<>(), onlineAmount, staticB, ram));
			startServerGroup(name, ram, onlineAmount, staticB);
			servers++;
		}
		if (servers == 0)
			console.sendMessage(NandiCloud.PREFIX + "No Servers to start.");
	}

	public void startServerGroup(String name, int ram, int onlineAmount, boolean staticB) {
		for (int i = 1; i <= onlineAmount; i++) { //start servers
			try {
				startServer(name + (staticB ? "" : ("-" + i)), ram, name, staticB);
			} catch (IOException e) {
				NandiCloud.printError(e);
			}
		}
	}

	public void startServer(String name) {
		String pathName = name.split("-")[0];
		JSONArray array = JSONUtil.getJSON(serversConfig);
		for (Object o : array) {
			JSONObject object = (JSONObject) o;
			String currentName = (String) object.get("name");
			if (pathName.equalsIgnoreCase(currentName)) {
				for (File file : temporaryDirectory.listFiles()) {
					if (file.isDirectory() && file.getName().contains(name))
						deleteFiles(file);
				}
				int ram = ((Number) object.get("ram")).intValue();
				boolean staticB = ((boolean) object.get("static"));
				try {
					startServer(name, ram, pathName, staticB);
				} catch (IOException e) {
					NandiCloud.printError(e);
				}
				return;
			}
		}
	}

	public void startServer(String name, int ram, String pathName, boolean staticB) throws IOException {
		File server = new File(staticB ? staticDirectory : serverGroupDirectory, pathName);
		if (!server.exists() || !server.isDirectory()) {
			if (server.exists())
				server.delete();
			server.mkdir();
		}
		String serverID = name + "_" + RandomIdGenerator.generateId(); //create Server-ID
		File serverDirectory = server;
		if (!staticB) {
			File temporary = new File(temporaryDirectory, serverID);
			temporary.delete();
			temporary.mkdir();
			temporary.deleteOnExit();
			copyFiles(server, temporary);
			serverDirectory = temporary;
		}
		File spigotJar = new File(serverDirectory, "spigot.jar");
		if (!spigotJar.exists()) {
			console.sendMessage(NandiCloud.PREFIX + "§cCould not start server " + name + " because spigot.jar does not exist. " +
					"Please make sure to have a file called 'spigot.jar' in your template.");
			return;
		}
		File plugins = new File(serverDirectory, "/plugins/");
		plugins.mkdirs();
		File plugin = new File(plugins, "NandiCloud.jar");
		if (plugin.exists()) plugin.delete();
		Files.copy(new File(NandiCloud.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toPath(), plugin.toPath());
		File serverProperties = new File(serverDirectory, "server.properties");
		setProperty(serverProperties, "online-mode", "false");
		setProperty(serverProperties, "server-name", name);
		File configFile = new File(serverDirectory, "spigot.yml");
		configFile.createNewFile();
		Yaml yaml = new Yaml();
		Map<String, Object> config = yaml.load(new FileReader(configFile));
		if (config == null) config = new LinkedHashMap<>();
		Map<String, Object> settings = (Map<String, Object>) config.get("settings");
		if (settings == null) settings = new LinkedHashMap<>();
		final Boolean bungeeCordMode = (Boolean) settings.getOrDefault("bungeecord", false);

		if (!bungeeCordMode) {
			console.sendMessage(NandiCloud.PREFIX + "§c" + name + " is not in BungeeCord mode. To fix this, change bungeecord to true in spigot.yml");
		}
		Integer port = getFreePort();
		if (port == null) {
			console.sendMessage(NandiCloud.PREFIX + "§cCould not start server " + name + " because there is no free port.");
			return;
		}
		notFreePorts.add(port);
		setProperty(serverProperties, "server-port", String.valueOf(port));

		new ProcessBuilder("/bin/sh", "-c",
				"screen -mdS " + serverID + " /bin/sh -c " +
						"'cd " + serverDirectory.getAbsolutePath() + " && java -server" +
						" -Xmx" + ram + "M" +
						" -Dcom.mojang,eula.agree=true" +
						" -Dnandicloud-serverid=" + serverID +
						" -Dnandicloud-name=" + name +
						" -jar spigot.jar'"
		).start(); // start server as screen with ram
		console.sendMessage(NandiCloud.PREFIX + "Successfully started server " + name);
	}

	private void setProperty(File file, String property, String value) {
		try {
			file.createNewFile();

			FileInputStream in = new FileInputStream(file);
			Properties props = new Properties();
			props.load(in);
			in.close();

			FileOutputStream out = new FileOutputStream(file);
			props.setProperty(property, value);
			props.store(out, null);
			out.close();
		} catch (Exception e) {
			console.sendMessage("§cError while setting property '" + property + "' to value '"
					+ value + "' in file " + file.getAbsolutePath() + ":");
		}
	}

	private void copyFiles(File copy, File destination) throws IOException {
		for (File file : copy.listFiles()) {
			if (file.isFile()) {
				File currentDestination = new File(destination.getPath(), file.getName());
				currentDestination.createNewFile();
				Files.copy(file.toPath(), currentDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else if (file.isDirectory()) {
				File currentDestination = new File(destination.getPath(), file.getName());
				currentDestination.mkdir();
				copyFiles(file, currentDestination);
			}
		}
	}

	private void deleteFiles(File directory) {
		if (directory == null)
			return;
		if (!directory.exists())
			return;
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				deleteFiles(file);
			}
			try {
				Files.delete(file.toPath());
			} catch (IOException e) {
				NandiCloud.printError(e);
			}
		}
	}

	public String getGroup(String server) {
		return getServerName(server.split("-")[0]);
	}

	public boolean isServerJSON(String server) {
		return getServerName(server) != null;
	}

	public JSONObject getServerJSON(String server) {
		JSONArray array = JSONUtil.getJSON(serversConfig);
		for (Object o : array) {
			JSONObject object = (JSONObject) o;
			String currentName = (String) object.get("name");
			if (server.equalsIgnoreCase(currentName)) {
				return object;
			}
		}
		return null;
	}

	public String getServerName(String server) {
		JSONObject serverObject = getServerJSON(server);
		return serverObject == null ? null : (String) serverObject.get("name");
	}

	private Integer getFreePort() {
		if (currentPort == MAX_PORT - 1)
			currentPort = START_PORT;
		while (currentPort < MAX_PORT) {
			currentPort++;
			if (isPortFree(currentPort))
				return currentPort;
		}
		return null;
	}

	private boolean isPortFree(int port) {
		if (notFreePorts.contains(port))
			return false;
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (serverSocket != null) serverSocket.close();
			} catch (Exception e) {
				NandiCloud.printError(e);
			}
		}
	}

	public void setProxy(boolean proxy) {
		isProxy = proxy;
	}
}
