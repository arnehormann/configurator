package org.jatronizer.configurator;

public class ChangeEvent {
	public final String key;
	public final String value;

	public ChangeEvent(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String toString() {
		return key + "->" + value;
	}
}
