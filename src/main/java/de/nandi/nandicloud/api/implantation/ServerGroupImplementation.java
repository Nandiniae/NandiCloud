package de.nandi.nandicloud.api.implantation;

import de.nandi.nandicloud.api.identifiable.LinkableObject;
import de.nandi.nandicloud.api.identifiable.ServerGroupObjectLink;
import de.nandi.nandicloud.api.identifiable.ServerObjectLink;
import de.nandi.nandicloud.api.objects.ServerGroupObject;
import de.nandi.nandicloud.api.objects.ServerObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerGroupImplementation implements ServerGroupObject, LinkableObject<ServerGroupObject> {

	private final String id;
	private final String name;
	private final Set<ServerObjectLink> servers;
	private int max_Servers;
	private int ram;
	private boolean staticB;

	public ServerGroupImplementation(String id, String name, Set<ServerObjectLink> servers, int max_Servers, boolean staticB, int ram) {
		this.id = id;
		this.name = name;
		this.servers = servers;
		this.max_Servers = max_Servers;
		this.staticB = staticB;
		this.ram = ram;
	}


	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<ServerObject> getServers() {
		return servers.stream().map(ServerObjectLink::resolve).collect(Collectors.toList());
	}

	@Override
	public Integer getMaxServers() {
		return max_Servers;
	}

	@Override
	public Integer getRam() {
		return ram;
	}

	public void setRam(int ram) {
		this.ram = ram;
	}

	@Override
	public boolean isStatic() {
		return staticB;
	}

	public void setStatic(boolean staticB) {
		this.staticB = staticB;
	}

	public void setMax_Servers(int max_Servers) {
		this.max_Servers = max_Servers;
	}

	public void addServer(ServerObjectLink server) {
		servers.add(server);
	}

	public void removeServer(ServerObjectLink server) {
		servers.remove(server);
	}

	public Set<ServerObjectLink> getServersLink() {
		return servers;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ServerGroupImplementation that = (ServerGroupImplementation) o;

		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}


	@Override
	public ServerGroupObjectLink toLink() {
		return new ServerGroupObjectLink(this);
	}
}
