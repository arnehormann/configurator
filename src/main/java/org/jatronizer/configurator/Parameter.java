package org.jatronizer.configurator;

import java.lang.reflect.Field;

final class Parameter<VT> implements Comparable<Parameter<VT>> {

	// NOTE:
	// in some places in this class, setAccessible(true) is called
	// but not set back to false because keeping the value accessible speeds up later operations.
	// It is viable in this case because the configuration should only have a single instance
	// a single Configurator is solely responsible for.

	public final String key;
	public final String defval;
	public final String desc;
	private Field field;
	private Converter<VT> converter;

	static Parameter create(Object base, Field field, Config config, String prefix) {
		String key = config.key();
		if ("".equals(key)) {
			key = field.getName();
		}
		key = prefix + key;
		Converter converter = null;
		if (config.convert() == DefaultConverter.class) {
			converter = DefaultConverter.getFor(field.getType());
		}
		String defval;
		if (converter == null) {
			try {
				converter = config.convert().newInstance();
			} catch (Exception e) {
				throw new ReflectionException(e);
			}
		}
		synchronized (base) {
			Object value;
			try {
				value = field.get(base);
			} catch (IllegalAccessException ie) {
				try {
					field.setAccessible(true);
					value = field.get(base);
					// not setting it back to false, see NOTE
				} catch (Exception ex2) {
					throw new ReflectionException(ex2);
				}
			}
			defval = converter.toString(value);
		}
		return new Parameter(
				key,
				field,
				defval,
				config.desc(),
				converter
		);
	}

	private Parameter(String key, Field field, String defval, String desc, Converter<VT> converter) {
		this.key = key;
		this.field = field;
		this.defval = defval;
		this.desc = desc;
		this.converter = converter;
	}

	public String get(Object base) {
		synchronized (base) {
			try {
				return converter.toString((VT) (field.get(base)));
			} catch (Exception e) {
				try {
					synchronized (base) {
						field.setAccessible(true);
						String value = converter.toString((VT) (field.get(base)));
						// not setting it back to false, see NOTE
						return value;
					}
				} catch (Exception ex2) {
					throw new ReflectionException(ex2);
				}
			}
		}
	}

	public void set(Object base, String value) {
		Object v = converter.valueOf(value);
		try {
			synchronized (base) {
				field.set(base, v);
			}
		} catch (IllegalAccessException ex) {
			try {
				synchronized (base) {
					field.setAccessible(true);
					field.set(base, v);
					// not setting it back to false, see NOTE
				}
			} catch (Exception ex2) {
				throw new ReflectionException(ex2);
			}
		}
	}

	public int compareTo(Parameter o) {
		return key.compareTo(o.key);
	}

	public String toString() {
		return key + " (" + defval + "): " + desc;
	}
}
