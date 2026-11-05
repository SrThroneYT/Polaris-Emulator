package com.websockets.plugin.utilities;

import com.eu.habbo.Emulator;
import com.websockets.plugin.WebsocketsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class checkDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(checkDatabase.class);

    public static void registerTexts() {

        Emulator.getTexts().register("commands.keys.cmd_update_ws_config", "update_ws_config;updatewsconfig");
        Emulator.getTexts().register("commands.description.cmd_update_ws_config",
                "<b>:update_ws_config</b> Actualiza las configuraciones del websocket");

        Emulator.getTexts().register("commands.keys.cmd_update_ws_text", "update_ws_text;update_ws_texts;updatewstext");
        Emulator.getTexts().register("commands.description.cmd_update_ws_text",
                "<b>:update_ws_text</b> Actualiza los textos del websocket");

        Emulator.getTexts().register("commands.keys.cmd_sexo_ws", "sexo;sex");
        Emulator.getTexts().register("commands.description.cmd_sexo_ws",
                "<b>:sexo [usuario]</b> Envia una petición para tener sexo con el usuario");
        Emulator.getTexts().register("cmd_sexo_ws.error.no_user",
                "Debes escribir el nombre de un usuario");
        WebsocketsManager.getTexts().register("cmd_sexo_ws.peticion_sexo.confirm_title",
                "%username% Te ha enviado una solicitud de sexo");
        WebsocketsManager.getTexts().register("cmd_sexo_ws.peticion_sexo.confirm_desc",
                "¿Quieres aceptar esta solicitud de sexo?");

        Emulator.getTexts().register("commands.keys.cmd_tocar_ws", "tocar");
        Emulator.getTexts().register("commands.description.cmd_tocar_ws",
                "<b>:tocar [usuario]</b> Envia una petición para tocar al usuario");
    }

    public static void ICheckDatabase() throws Exception {
        try {
            boolean reloadPermissions = true;

            registerTexts();

            reloadPermissions = registerPermission("cmd_update_ws_config", "'0', '1'", "0", reloadPermissions);
            reloadPermissions = registerPermission("cmd_update_ws_text", "'0', '1'", "0", reloadPermissions);

            reloadPermissions = registerPermission("cmd_sexo_ws", "'0', '1'", "1", reloadPermissions);
            reloadPermissions = registerPermission("cmd_tocar_ws", "'0', '1'", "1", reloadPermissions);

            if (reloadPermissions)
                Emulator.getGameEnvironment().getPermissionsManager().reload();

        } catch (Exception e) {
            LOGGER.error(" [WEBSOCKETS ROOMS INFO] Failed to check database for permissions", e);
        }
    }

    private static boolean registerPermission(String name, String options, String defaultValue, boolean defaultReturn) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("ALTER TABLE  `permissions` ADD  `" + name
                    + "` ENUM(  " + options + " ) NOT NULL DEFAULT  '" + defaultValue + "'")) {
                statement.execute();
                return true;
            }
        } catch (SQLException e) {
        }

        return defaultReturn;
    }

}
