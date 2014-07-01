package org.jatronizer.configurator;

import java.util.*;

/**
 * Manages multiple configure modules.
 */
final class MultiConfigurator implements Configurator {

	private static class Key {
		public final String key;
		public final int configurator;
		public Key(String key, int configurator) {
			this.key = key;
			this.configurator = configurator;
		}
	}

	public static MultiConfigurator configure(Configurator[] configurators) {
		configurators = configurators.clone();
		// configure sorted array of available keys
		ArrayList<Key> keyList = new ArrayList<Key>();
		for (int i = 0; i < configurators.length; i++) {
			Configurator configurator = configurators[i];
			String[] keys1 = configurator.keys();
			for (String key : keys1) {
				keyList.add(new Key(key, i));
			}
		}
		Key[] keys = keyList.toArray(new Key[keyList.size()]);
		Arrays.sort(keys, new Comparator<Key>() {
			public int compare(Key o1, Key o2) {
				return o1.key.compareTo(o2.key);
			}
		});
		// generate mapping from key to configurator and check uniqueness of keys
		int[] keyToConfigurator = new int[keys.length];
		String lastKey = null;
		for (int i = 0; i < keys.length; i++) {
			Key k = keys[i];
			if (k.key.equals(lastKey)) {
				throw new ConfigException("duplicate key " + k.key + " in keys");
			}
			lastKey = k.key;
			keyToConfigurator[i] = k.configurator;
		}
		// fetch names from keys
		String[] keyNames = new String[keys.length];
		for (int i = 0; i < keyNames.length; i++) {
			keyNames[i] = keys[i].key;
		}
		return new MultiConfigurator(configurators, keyNames, keyToConfigurator);
	}

	private final Configurator[] configurators;
	private final String[] keys;
	private final int[] configForKey; // key index to configurator index

	private MultiConfigurator(Configurator[] configurators, String[] keys, int[] configForKey) {
		this.configurators = configurators;
		this.keys = keys;
		this.configForKey = configForKey;
	}

	private Configurator moduleOf(String key) {
		int idx = Arrays.binarySearch(keys, key);
		if (idx < 0) {
			return null;
		}
		return configurators[configForKey[idx]];
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasKey(String key) {
		return Arrays.binarySearch(keys, key) > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] keys() {
		return keys.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfigParameter parameter(String key) {
		Configurator configurator = moduleOf(key);
		if (configurator == null) {
			return null;
		}
		return configurator.parameter(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public String value(String key) {
		int idx = Arrays.binarySearch(keys, key);
		if (idx < 0) {
			return null;
		}
		return configurators[configForKey[idx]].value(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public int set(String key, String value) {
		int idx = Arrays.binarySearch(keys, key);
		if (idx < 0) {
			return 0;
		}
		return configurators[configForKey[idx]].set(key, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public int set(Map<String, String> configuration) {
		int numSet = 0;
		for (Configurator configurator : configurators) {
			numSet += configurator.set(configuration);
		}
		return numSet;
	}

	/**
	 * {@inheritDoc}
	 */
	public int set(Properties configuration) {
		int numSet = 0;
		for (Configurator configurator : configurators) {
			numSet += configurator.set(configuration);
		}
		return numSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unsafe")
	public void walk(ConfigVisitor v) {
		for (Configurator conf : configurators) {
			conf.walk(v);
		}
	}
}
