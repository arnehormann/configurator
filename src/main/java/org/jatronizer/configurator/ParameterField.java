package org.jatronizer.configurator;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

final class ParameterField<T> implements Comparable<ParameterField<T>> {

	private static void forceAccessible(AccessibleObject...objects) {
		for (AccessibleObject ao : objects) {
			if (!ao.isAccessible()) {
				ao.setAccessible(true);
			}
		}
	}

	// NOTE:
	// in some places in this class, setAccessible(true) is called
	// but not set back to false because keeping the value accessible speeds up later operations.
	// It is viable in this case because the configuration should only have a single instance
	// a single Configurator is solely responsible for.

	public final String key;
	public final String defaultValue;
	public final String description;
	private final Converter<T> converter;
	private final Field field;
	private final String[] enumNames;
	private final Field[] enumFields;

	static ParameterField create(Object base, Field field, Parameter parameter, String prefix) {
		String description = Configurator.description(field);
		if (description == null) {
			description = "";
		}
		String key = parameter.key();
		if ("".equals(key)) {
			key = field.getName();
		}
		key = prefix + key;
		Converter converter = null;
		if (parameter.convert() == DefaultConverter.class) {
			converter = DefaultConverter.getFor(field.getType());
		}
		String defaultValue;
		if (converter == null) {
			try {
				converter = parameter.convert().newInstance();
			} catch (Exception e) {
				throw new ConfigurationException(e);
			}
		}
		String[] enumValues = new String[0];
		Field[] enumFields = new Field[0];
		Class c = field.getType();
		if (c.isEnum()) {
			try {
				Field[] fields = c.getDeclaredFields();
				int i = 0;
				for (int j = 0; j < fields.length; j++) {
					if (fields[j].isEnumConstant()) {
						fields[i] = fields[j];
						i++;
					}
				}
				enumFields = new Field[i];
				System.arraycopy(fields, 0, enumFields, 0, enumFields.length);
				// make them accessible to use them later
				forceAccessible(enumFields);
				// sort alphabetically by name to enable binarySearch
				Arrays.sort(enumFields, new Comparator<Field>() {
					public int compare(Field o1, Field o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				enumValues = new String[enumFields.length];
				for (int j = 0; j < i; j++) {
					enumValues[j] = enumFields[j].getName();
				}
			} catch (Exception e) {
				throw new ConfigurationException("Could not access enum values of " + c, e);
			}
		}
		synchronized (base) {
			Object value;
			try {
				forceAccessible(field);
				value = field.get(base);
			} catch (Exception e) {
				throw new ConfigurationException(field.toString() + " could not be accessed", e);
			}
			defaultValue = converter.toString(value);
		}
		return new ParameterField(
				key,
				field,
				defaultValue,
				description,
				converter,
				enumValues,
				enumFields
		);
	}

	private ParameterField(String key, Field field, String defaultValue, String description, Converter<T> converter,
	                       String[] enumNames, Field[] enumFields) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.description = description;
		this.field = field;
		this.converter = converter;
		this.enumNames = enumNames;
		this.enumFields = enumFields;
	}

	public String get(Object base) {
		synchronized (base) {
			try {
				return converter.toString((T) (field.get(base)));
			} catch (Exception e) {
				try {
					synchronized (base) {
						field.setAccessible(true);
						String value = converter.toString((T) (field.get(base)));
						// not setting it back to false, see NOTE
						return value;
					}
				} catch (Exception ex2) {
					throw new ConfigurationException(ex2);
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
				throw new ConfigurationException(ex2);
			}
		}
	}

	public Class<T> type() {
		return (Class<T>) field.getType();
	}

	public String[] enumNames() {
		return enumNames.clone();
	}

	public Field enumValue(String name) {
		int i = Arrays.binarySearch(enumNames, name);
		if (i < 0) {
			return null;
		}
		return enumFields[i];
	}

	public int compareTo(ParameterField o) {
		return key.compareTo(o.key);
	}

	public String toString() {
		return key + " (" + defaultValue + "): " + description;
	}
}
