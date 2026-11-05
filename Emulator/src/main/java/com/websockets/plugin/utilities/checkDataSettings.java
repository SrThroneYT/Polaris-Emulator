package com.websockets.plugin.utilities;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

public class checkDataSettings {
    private static final Logger LOGGER = LoggerFactory.getLogger(checkDataSettings.class);

    private final Properties properties;

    public boolean loaded = false;
    private boolean isLoading = false;

    public checkDataSettings() {
        this.properties = new Properties();
        // Verificamos y ampliamos la columna al crear el objeto
        this.ensureColumnLength();
    }

    /**
     * Verifica si la columna 'value' tiene al menos 5000 caracteres.
     * Si es menor, realiza un ALTER TABLE.
     */
    private void ensureColumnLength() {
        String query = "ALTER TABLE `websocketsth_settings` MODIFY COLUMN `value` VARCHAR(5000)";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Verificamos la precisión (longitud) actual de la columna
            try (ResultSet resultSet = metaData.getColumns(null, null, "websocketsth_settings", "value")) {
                if (resultSet.next()) {
                    int currentSize = resultSet.getInt("COLUMN_SIZE");
                    if (currentSize < 5000) {
                        try (Statement statement = connection.createStatement()) {
                            statement.execute(query);
                            LOGGER.info(
                                    "[WEBSOCKETS] Column 'value' in 'websocketsth_settings' updated to VARCHAR(5000).");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("[WEBSOCKETS] Could not verify/update column length for 'websocketsth_settings'", e);
        }
    }

    public void loadFromDatabase() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            if (statement.execute("SELECT * FROM websocketsth_settings")) {
                try (ResultSet set = statement.getResultSet()) {
                    while (set.next()) {
                        this.properties.put(set.getString("key"), set.getString("value"));
                    }
                }

                this.loaded = true;
            }
        } catch (SQLException e) {
            LOGGER.error("[WEBSOCKETS BY SRTHRONE] plugin its illegally, stopping emulator!");
            Emulator.prepareShutdown();
            Emulator.isShuttingDown = true;
            Emulator.isReady = false;
        }
    }

    public void saveToDatabase(String value, String key) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("UPDATE websocketsth_settings SET `value` = ? WHERE `key` = ? LIMIT 1")) {

            statement.setString(1, value);
            statement.setString(2, key);
            statement.executeUpdate();

            this.update(key, value);

        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void updateData(String key, String value) {
        this.update(key, value);
        this.saveToDatabase(value, key);
    }

    public String checkSettings(String key, String default_value) {
        // Comprueba si no existe y lo registra
        if (this.getValue(key).equals("none")) {
            this.register(key, default_value);
            return default_value;
        } else {
            return this.getValue(key);
        }
    }

    public String getValue(String key) {
        return this.getValue(key, "none");
    }

    public String getValue(String key, String defaultValue) {
        if (this.isLoading)
            return defaultValue;

        if (!this.properties.containsKey(key)) {
            return defaultValue;
        }
        return this.properties.getProperty(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            String val = this.getValue(key, "0");
            return val.equals("1") || val.equalsIgnoreCase("true");
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type boolean.", key, this.getValue(key));
        }
        return defaultValue;
    }

    public int getInt(String key) {
        return this.getInt(key, 0);
    }

    public int getInt(String key, Integer defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            return Integer.parseInt(this.getValue(key, defaultValue.toString()));
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type integer.", key, this.getValue(key));
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return this.getDouble(key, 0.0);
    }

    public double getDouble(String key, Double defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            return Double.parseDouble(this.getValue(key, defaultValue.toString()));
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type double.", key, this.getValue(key));
        }

        return defaultValue;
    }

    public void update(String key, String value) {
        this.properties.setProperty(key, value);
    }

    public void register(String key, String value) {
        if (this.properties.getProperty(key, null) != null)
            return;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("INSERT INTO websocketsth_settings (`key`, `value`) VALUES (?, ?)")) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }

        this.update(key, value);
    }

    public void reload() {
        this.isLoading = true;
        this.properties.clear();

        if (loaded) {
            this.loadFromDatabase();
        }

        this.isLoading = false;
        LOGGER.info("Configuration Roleplay -> Loaded!");
    }
}