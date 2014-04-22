package org.jatronizer.configurator;

/**
 * A ChangeEvent represents the change or addition of a single value
 */
public class ChangeEvent {
	public final String key;
	public final String value;

	public ChangeEvent(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String toString() {
		return key + "→" + value;
	}
}
