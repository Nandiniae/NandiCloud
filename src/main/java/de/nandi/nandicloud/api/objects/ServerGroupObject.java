package de.nandi.nandicloud.api.objects;

import de.nandi.nandicloud.api.identifiable.IdentifiableObject;

import java.util.Collection;

public interface ServerGroupObject extends IdentifiableObject {

	/**
	 * @return The group's name
	 */
	String getName();

	/**
	 * @return A list of {@link ServerObject} which contains all running servers
	 */
	Collection<ServerObject> getServers();

	/**
	 * @return The number of maximal running Servers
	 */
	Integer getMaxServers();

	/**
	 * @return The megabytes of ram every server of this group has
	 */
	Integer getRam();

	/**
	 * @return If the Group is Static
	 */
	boolean isStatic();

}
