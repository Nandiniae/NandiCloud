package de.nandi.nandicloud.core.main;

import de.nandi.nandicloud.api.api.NandiCloudAPI;
import de.nandi.nandicloud.api.api.PrivateGeneralAPI;
import de.nandi.nandicloud.api.implantation.APIInstanceUtil;
import de.nandi.nandicloud.api.implantation.event.EventManager;
import de.nandi.nandicloud.api.objects.command.Command;
import de.nandi.nandicloud.api.objects.command.CommandSender;
import de.nandi.nandicloud.api.objects.message.MessageReceiver;
import de.nandi.nandicloud.api.objects.message.MessageType;
import de.nandi.nandicloud.api.objects.message.PluginMessage;
import de.nandi.nandicloud.core.api.CoreCoreAPIInstance;
import de.nandi.nandicloud.core.api.CoreMessageAPIInstance;
import de.nandi.nandicloud.core.api.CoreUniversalAPIInstance;
import de.nandi.nandicloud.core.commands.*;
import de.nandi.nandicloud.core.completers.ProxyGroupNameCompleter;
import de.nandi.nandicloud.core.completers.ServerGroupNameCompleter;
import de.nandi.nandicloud.core.completers.ServerNameCompleter;
import de.nandi.nandicloud.core.connecting.CoreServer;
import de.nandi.nandicloud.core.listener.CoreMessageListener;
import de.nandi.nandicloud.core.listener.EventCallListener;
import de.nandi.nandicloud.core.logger.LoggingPrintStream;
import de.nandi.nandicloud.core.servers.ServerManagerCore;
import de.nandi.nandicloud.core.util.ChatColorUtil;
import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class NandiCloud {

	public static final String PREFIX = PrivateGeneralAPI.PREFIX;
	public static CoreCoreAPIInstance coreAPIInstance;
	public static CoreMessageAPIInstance coreMessageAPIInstance;
	public static CommandSender console;
	private static boolean waitingForCommand;
	private static LineReader reader;
	private static Logger logger;

	public static void main(String[] args) {
		try {
			createLogger();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			nandiCloud();
		} catch (Exception e) {
			NandiCloud.printError(e);
		}
	}

	private static void createLogger() throws IOException {
		logger = Logger.getLogger("NandiCloudCore");
		logger.setUseParentHandlers(false);
		File logsDirectory = new File("logs");
		logsDirectory.mkdirs();
		FileHandler fileHandler = new FileHandler(logsDirectory.getCanonicalPath() + "/core-%g.log", 5242880, 100, false);
		SimpleFormatter formatter = new SimpleFormatter();
		fileHandler.setFormatter(formatter);
		logger.addHandler(fileHandler);
	}

	public static void printError(Throwable throwable) {
		throwable.printStackTrace(new LoggingPrintStream(NandiCloud::info));
	}

	public static void info(String message) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		message = "[" + dtf.format(now) + "] " + message;
		logger.info(ChatColorUtil.stripColors(message.replace(PREFIX, "")));
		message = ChatColorUtil.toLegacyText(ChatColorUtil.translateAlternateColorCodes('&', message) + "§r");
		if (reader == null) {
			System.out.println(message);
			return;
		}
		if (waitingForCommand)
			reader.callWidget(LineReader.CLEAR);
		reader.getTerminal().writer().println(message);
		if (waitingForCommand)
			reader.callWidget(LineReader.REDRAW_LINE);
		if (waitingForCommand)
			reader.callWidget(LineReader.REDISPLAY);
		reader.getTerminal().writer().flush();
	}

	private static void nandiCloud() {
		console = NandiCloud::info;
		console.sendMessage(PREFIX + "§9Core initialized.");
		CompletableFuture.runAsync(() -> {
			try {
				waitForCommands();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		ServerManagerCore serverManagerCore = null;
		try {
			serverManagerCore = new ServerManagerCore(console);
		} catch (IOException e) {
			printError(e);
		}
		CoreServer coreServer = new CoreServer();
		try {
			APIInstanceUtil.setCoreInstance(coreAPIInstance = new CoreCoreAPIInstance());
			APIInstanceUtil.setMessageInstance(coreMessageAPIInstance = new CoreMessageAPIInstance(coreServer));
			APIInstanceUtil.setUniversalInstance(new CoreUniversalAPIInstance(serverManagerCore));
			APIInstanceUtil.setEventInstance(new EventManager(true));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			printError(e);
		}
		NandiCloudAPI.getCoreAPI().registerCommand(new HelpCommand(coreAPIInstance));
		NandiCloudAPI.getCoreAPI().registerCommand(new AddServerCommand(serverManagerCore));
		NandiCloudAPI.getCoreAPI().registerCommand(new DeleteServerCommand(serverManagerCore));
		NandiCloudAPI.getCoreAPI().registerCommand(new EditServerGroupCommand(serverManagerCore));
		NandiCloudAPI.getCoreAPI().registerCommand(new ListServerGroupsCommand(serverManagerCore));
		NandiCloudAPI.getCoreAPI().registerCommand(new ExitCommand());
		NandiCloudAPI.getCoreAPI().registerCommand(new RestartCommand(coreServer, serverManagerCore));
		NandiCloudAPI.getMessageAPI().registerMessageListener(new CoreMessageListener(serverManagerCore));
		NandiCloudAPI.getMessageAPI().registerMessageListener(new EventCallListener());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_SERVER.name()),
						MessageReceiver.ALL_SERVERS.name());
				NandiCloudAPI.getMessageAPI().sendMessageToServer(new PluginMessage(MessageType.STOP_PROXY.name()),
						MessageReceiver.BUNGEE.name());
				coreServer.notReceiving();
				System.out.println(ChatColorUtil.toLegacyText(PREFIX + "§9Core stopped."));
			} catch (Exception e) {
				printError(e);
			}
		}, "Shutdown-thread"));

	}

	private static void waitForCommands() throws IOException {
		TerminalBuilder builder = TerminalBuilder.builder();
		builder.encoding(Charset.defaultCharset());
		builder.system(true);
		Terminal terminal = builder.build();
		Completer completer = new Completers.TreeCompleter(
				node("help"),
				node("add",
						node("server"),
						node("proxy")
				),
				node("restart",
						new Completers.TreeCompleter.Node(
								new AggregateCompleter(
										new ServerGroupNameCompleter(),
										new ProxyGroupNameCompleter(),
										new ServerNameCompleter()
								),
								Collections.emptyList()
						)
				),
				node("delete",
						new Completers.TreeCompleter.Node(
								new AggregateCompleter(
										new ServerGroupNameCompleter(),
										new ProxyGroupNameCompleter()
								),
								Collections.emptyList()
						)
				),
				node("edit",
						new Completers.TreeCompleter.Node(
								new AggregateCompleter(
										new ServerGroupNameCompleter(),
										new ProxyGroupNameCompleter()
								),
								Collections.emptyList()
						)
				),
				node("list"),
				node("exit")
		);
		Parser parser = new DefaultParser();
		String prompt = "> ";
		reader = LineReaderBuilder.builder()
				.terminal(terminal)
				.completer(completer)
				.parser(parser)
				.build();
		while (true) {
			waitingForCommand = true;
			String line = null;
			try {
				line = reader.readLine(prompt, null, (MaskingCallback) null, null);
			} catch (UserInterruptException e) {
				System.exit(0);
			} catch (EndOfFileException ignore) {
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (line == null) continue;
			waitingForCommand = false;
			String input = line.trim();
			if (input.isEmpty()) continue;
			String[] values = input.split(" ");
			if (values.length < 1) continue;
			String sendetCommandName = values[0];
			String[] args = Arrays.copyOfRange(values, 1, values.length);
			boolean isCommand = false;
			for (Command command : coreAPIInstance.getCommands()) {
				if (sendetCommandName.equalsIgnoreCase(command.getName())) {
					isCommand = true;
					try {
						command.onCommand(sendetCommandName, console, args);
					} catch (Exception e) {
						NandiCloud.printError(e);
					}
				} else
					for (String aliases : command.getAliases()) {
						if (!sendetCommandName.equalsIgnoreCase(aliases)) {
							continue;
						}
						isCommand = true;
						try {
							command.onCommand(sendetCommandName, console, args);
						} catch (Exception e) {
							NandiCloud.printError(e);
						}
					}
			}
			if (!isCommand) {
				console.sendMessage(PREFIX + "§cThis is not a Command. /help for all Commands");
			}
		}
	}

}
