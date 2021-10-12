package de.nandi.nandicloud.api.identifiable;

public interface LinkableObject<T extends IdentifiableObject> {

	IdentifiableLink<T> toLink();

}
