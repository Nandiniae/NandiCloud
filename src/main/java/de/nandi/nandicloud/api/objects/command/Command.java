package de.nandi.nandicloud.api.objects.command;

public abstract class Command {

	private final String name;
	private final String[] aliases;
	private String description;

	public Command(String name, String... aliases) {
		if (name == null)
			throw new NullPointerException("nam is not allowed to be null");
		this.name = name;
		this.aliases = aliases;
	}

	public abstract void onCommand(String command, CommandSender sender, String[] args);

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String[] getAliases() {
		return aliases;
	}
}
