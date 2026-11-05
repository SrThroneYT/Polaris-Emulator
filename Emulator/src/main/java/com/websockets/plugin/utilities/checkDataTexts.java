package com.websockets.plugin.utilities;

import com.eu.habbo.Emulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;
import java.nio.charset.StandardCharsets;

public class checkDataTexts {
    private static final Logger LOGGER = LoggerFactory.getLogger(checkDataTexts.class);

    private final Properties text;
    public boolean loaded = false;
    private boolean isLoading = false;

    public checkDataTexts() {
        this.text = new Properties();
    }

    /**
     * Carga los textos asegurando que el ResultSet use la codificación correcta
     */
    public void loadFromDatabase() {
        this.isLoading = true;
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                Statement statement = connection.createStatement()) {

            // Forzamos la sesión de la base de datos a UTF8MB4 para soportar todos los
            // caracteres
            statement.execute("SET NAMES utf8mb4");

            try (ResultSet set = statement.executeQuery("SELECT `key`, `value` FROM websocketsth_texts")) {
                this.text.clear();
                while (set.next()) {
                    String key = set.getString("key");
                    String value = set.getString("value");

                    if (key != null && value != null) {
                        this.text.put(key, value);
                    }
                }
            }
            this.loaded = true;
            LOGGER.info("[WEBSOCKETS] Textos cargados correctamente.");

        } catch (SQLException e) {
            LOGGER.error("[WEBSOCKETS BY SRTHRONE] Error crítico al cargar textos. ¡Apagando emulador!");
            Emulator.prepareShutdown();
            // Mantenemos la lógica de seguridad original
            Emulator.isShuttingDown = true;
            Emulator.isReady = false;
        } finally {
            this.isLoading = false;
        }
    }

    public void saveToDatabase(String value, String key) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            // Aseguramos UTF8 en la conexión de subida
            try (Statement st = connection.createStatement()) {
                st.execute("SET NAMES utf8mb4");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE websocketsth_texts SET `value` = ? WHERE `key` = ? LIMIT 1")) {
                statement.setString(1, value);
                statement.setString(2, key);
                statement.executeUpdate();

                this.update(key, value);
            }
        } catch (SQLException e) {
            LOGGER.error("Error al guardar texto con caracteres especiales", e);
        }
    }

    public void register(String key, String value) {
        if (this.text.containsKey(key))
            return;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (Statement st = connection.createStatement()) {
                st.execute("SET NAMES utf8mb4");
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO websocketsth_texts (`key`, `value`) VALUES (?, ?)")) {
                statement.setString(1, key);
                statement.setString(2, value);
                statement.execute();
            }
        } catch (SQLException e) {
            LOGGER.error("Error al registrar nueva key: " + key, e);
        }

        this.update(key, value);
    }

    // --- Métodos de utilidad (sin cambios significativos, solo limpieza) ---

    public String getValue(String key, String defaultValue) {
        if (this.isLoading)
            return defaultValue;
        return this.text.getProperty(key, defaultValue);
    }

    public String getValue(String key) {
        return this.getValue(key, "");
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (this.isLoading)
            return defaultValue;
        String val = this.getValue(key, String.valueOf(defaultValue)).toLowerCase();
        return val.equals("1") || val.equals("true") || val.equals("on");
    }

    public int getInt(String key, Integer defaultValue) {
        if (this.isLoading)
            return defaultValue;
        try {
            return Integer.parseInt(this.getValue(key, defaultValue.toString()));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void update(String key, String value) {
        this.text.setProperty(key, value);
    }

    public void reload() {
        if (loaded) {
            this.loadFromDatabase();
        }
    }
}