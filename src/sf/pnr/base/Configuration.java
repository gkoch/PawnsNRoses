package sf.pnr.base;

import sf.pnr.alg.EvalHashTable;
import sf.pnr.alg.PawnHashTable;
import sf.pnr.alg.TranspositionTable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Configuration {
    public static final Map<String, Configurable.Key> STORE = new HashMap<String, Configurable.Key>();

    private static Configuration INSTANCE;

    public static synchronized Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Configuration();
        }
        return INSTANCE;
    }

    private final Map<Configurable.Key, Field> keyToField = new HashMap<Configurable.Key, Field>();

    private Configuration() {
        initConfigurables(Engine.class);
        initConfigurables(Polyglot.class);
        initConfigurables(TranspositionTable.class);
        initConfigurables(EvalHashTable.class);
        initConfigurables(PawnHashTable.class);
        initConfigurables(Evaluation.class);
    }

    private void initConfigurables(final Class<?> clazz) {
        for (Field field: clazz.getDeclaredFields()) {
            final Configurable annotation = field.getAnnotation(Configurable.class);
            if (annotation != null) {
                final Configurable.Key key = annotation.value();
                field.setAccessible(true);
                keyToField.put(key, field);
            }
        }
    }

    public Class<?> getType(final Configurable.Key key) {
        final Field field = keyToField.get(key);
        return field.getType();
    }

    public String getString(final Configurable.Key key) {
        final Field field = keyToField.get(key);
        try {
            final Object value = field.get(null);
            final String valueStr;
            if (value == null) {
                valueStr = "null";
            } else {
                if (int[].class.equals(field.getType())) {
                    final int[] values = (int[]) value;
                    final StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < values.length; i++) {
                        if (i > 0) {
                            builder.append(", ");
                        }
                        builder.append(values[i]);
                    }
                    valueStr = builder.toString();
                } else {
                    valueStr = value.toString();
                }
            }
            return valueStr;
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e, "Failed to get value for key: " + key.getKey());
        }
    }

    private void setProperty(final String keyStr, final String value) {
        final Configurable.Key key = getKey(keyStr);
        if (key == null) {
            throw new IllegalArgumentException(String.format("Key '%s' is not recognised as valid key", keyStr));
        }
        setProperty(key, value);
    }

    public void setProperties(final Map<Configurable.Key, String> properties) {
        for (Map.Entry<Configurable.Key, String> entry: properties.entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
    }

    public void setProperty(final Configurable.Key key, final String value) {
        final Field field = keyToField.get(key);
        if (field == null) {
            throw new IllegalArgumentException(String.format("Cannot find field with configuration key '%s'", key));
        }
        try {
            final Class<?> type = field.getType();
            if (int.class.equals(type)) {
                field.setInt(null, Integer.parseInt(value));
            } else if (boolean.class.equals(type)) {
                field.setBoolean(null, Boolean.parseBoolean(value));
            } else if (long.class.equals(type)) {
                field.setLong(null, Long.parseLong(value));
            } else if (double.class.equals(type)) {
                field.setDouble(null, Double.parseDouble(value));
            } else if (String.class.equals(type)) {
                field.set(null, value);
            } else if (File.class.equals(type)) {
                if (value == null || value.length() == 0) {
                    field.set(null, null);
                } else {
                    field.set(null, new File(value));
                }
            } else if (int[].class.equals(type)) {
                final String[] valStrs = value.split(",");
                int[] oldValues = (int[]) field.get(null);
                if (oldValues == null) {
                    oldValues = new int[valStrs.length];
                    field.set(null, oldValues);
                } else if (oldValues.length != valStrs.length) {
                    throw new IllegalStateException(String.format(
                        "Trying to change the size of int array field '%s'. Old size: %d, new size: %d",
                        key, oldValues.length, valStrs.length));
                }
                for (int i = 0; i < valStrs.length; i++) {
                    oldValues[i] = Integer.parseInt(valStrs[i].trim());
                }
            } else {
                throw new IllegalStateException("Unsupported field type: " + type);
            }
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e, String.format("Failed to set value ('%s') for key '%s'", value, key));
        }
    }

    public void loadFromFile(final String fileName) throws IOException {
        final Properties properties = new Properties();
        final FileReader reader = new FileReader(fileName);
        try {
            properties.load(reader);
        } finally {
            reader.close();
        }
        setProperties(preprocess(properties));
    }

    public void loadFromSystemProperties() {
        final Properties properties = System.getProperties();
        setProperties(preprocess(properties, true));
    }

    public static Map<Configurable.Key, String> preprocess(final Properties properties) {
        return preprocess(properties, false);
    }

    public static Map<Configurable.Key, String> preprocess(final Properties properties, final boolean allowInvalidKeys) {
        final Map<Configurable.Key, String> result = new HashMap<Configurable.Key, String>();
        for (String keyStr: properties.stringPropertyNames()) {
            final Configurable.Key key = getKey(keyStr);
            if (key != null) {
                result.put(key, properties.getProperty(keyStr));
            } else if (!allowInvalidKeys) {
                throw new IllegalArgumentException("Invalid key: " + keyStr);
            }
        }
        return result;
    }

    public static Configurable.Key getKey(final String keyStr) {
        return STORE.get(keyStr);
    }
}