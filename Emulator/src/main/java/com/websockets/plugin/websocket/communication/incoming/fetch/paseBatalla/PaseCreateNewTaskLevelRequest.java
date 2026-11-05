package com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla;

import java.lang.annotation.Target;
import java.sql.SQLException;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Tarea;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

public class PaseCreateNewTaskLevelRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "PaseCreateNewTaskLevelRequest";

    public PaseCreateNewTaskLevelRequest(String header, THashMap<String, Object> data, Channel client)
            throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        try {
            // Extraer los campos que vienen desde el Formulario React
            String name = (String) this.data.get("name");

            Tarea tarea = WebsocketsManager.getTareasManager().getTarea(name);

            if(tarea == null){
                return;
            }

            int level = ((Number) this.data.get("level")).intValue();

            String title = (String) this.data.get("title");
            String descripcion = (String) this.data.get("description");
            String imagen = (String) this.data.get("imagen");

            int usageAccount = ((Number) this.data.get("required")).intValue();
            int xp = ((Number) this.data.get("xp")).intValue();
            
            int distintos_items = ((Number) this.data.get("distintos_items")).intValue();
            int distintos_rooms = ((Number) this.data.get("distintos_rooms")).intValue();
            int button_visit_room = ((Number) this.data.get("button_visit_room")).intValue();

            int typeId = tarea.type_id; // El ID de la categoría activa

            // Valores fijos por defecto
            String type = "event_success";
            String event = "increment";
            int levelRequired = 1;
            String customparams = "";

            try (java.sql.Connection connection = com.eu.habbo.Emulator.getDatabase().getDataSource().getConnection()) {

                // 1. Buscar de forma dinámica el siguiente orden real para esta categoría
                // (id_type)
                int orden = 1;
                try (java.sql.PreparedStatement orderStmt = connection.prepareStatement(
                        "SELECT MAX(orden) AS max_orden FROM `websocketsth_pase_tareas` WHERE `id_type` = ?")) {
                    orderStmt.setInt(1, typeId);
                    try (java.sql.ResultSet rs = orderStmt.executeQuery()) {
                        if (rs.next()) {
                            orden = rs.getInt("max_orden") + 1;
                        }
                    }
                }

                // 2. Ejecutar la inserción en la base de datos
                try (java.sql.PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO `websocketsth_pase_tareas` " +
                                "(`id_type`, `name`, `level`, `usage_account`, `title`, `descripcion`, `imagen`, `type`, `xp`, `event`, `level_required`, `orden`, `distintos_items`,`distintos_rooms`, `button_visit_room`, `customparams`) "
                                +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, typeId);
                    statement.setString(2, name);
                    statement.setInt(3, level);
                    statement.setInt(4, usageAccount);
                    statement.setString(5, title);
                    statement.setString(6, descripcion);
                    statement.setString(7, imagen);
                    statement.setString(8, type);
                    statement.setInt(9, xp);
                    statement.setString(10, event);
                    statement.setInt(11, levelRequired);
                    statement.setInt(12, orden); // Se asigna el orden calculado dinámicamente
                    statement.setInt(13, distintos_items);
                    statement.setInt(14, distintos_rooms);
                    statement.setInt(15, button_visit_room);
                    statement.setString(16, customparams);

                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected > 0) {
                        try (java.sql.ResultSet generatedKeys = statement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int newId = generatedKeys.getInt(1);
                                System.out.println("[PaseTareas] Nueva tarea insertada con ID: " + newId + " (" + name
                                        + ") con orden: " + orden);

                                WebsocketsManager.getTareasManager().update();

                                Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                                habbos.getTareas().showTareasWindow();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[PaseCreateNewTaskLevelRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}