package com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla;

import java.sql.SQLException;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

public class PaseCreateNewTaskRequest extends IncomingMessage {

    // Cambiado para coincidir con el JSON enviado desde React
    public static final String REQUEST_HEADER = "PaseCreateNewTaskRequest";

    public PaseCreateNewTaskRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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
            String title = (String) this.data.get("title");
            String descripcion = (String) this.data.get("description");
            String imagen = (String) this.data.get("imagen");

            int usageAccount = ((Number) this.data.get("usage_account")).intValue();
            int xp = ((Number) this.data.get("xp")).intValue();
            int typeId = ((Number) this.data.get("type")).intValue(); // El ID de la categoría activa
            int distintos_items = ((Number) this.data.get("distintos_items")).intValue();
            int distintos_rooms = ((Number) this.data.get("distintos_rooms")).intValue();
            int button_visit_room = ((Number) this.data.get("button_visit_room")).intValue();

            // Valores fijos por defecto basados en tu ejemplo SQL
            int level = 1;

            String type = "event_success";
            String event = "increment";
            int levelRequired = 1;
            int orden = 1;
            String customparams = "";

            // Ejecutar la inserción en la base de datos (Excluyendo la columna cash que no
            // existe en la tabla)
            try (java.sql.Connection connection = com.eu.habbo.Emulator.getDatabase().getDataSource().getConnection();
                    java.sql.PreparedStatement statement = connection.prepareStatement(
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
                statement.setInt(12, orden);
                statement.setInt(13, distintos_items);
                statement.setInt(14, distintos_rooms);
                statement.setInt(15, button_visit_room);
                statement.setString(16, customparams);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    // Opcional: Obtener el ID autogenerado si lo necesitas a futuro
                    try (java.sql.ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int newId = generatedKeys.getInt(1);
                            System.out
                                    .println("[PaseTareas] Nueva tarea insertada con ID: " + newId + " (" + name + ")");

                            WebsocketsManager.getTareasManager().update();

                            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                            habbos.getTareas().showTareasWindow();

                        }
                    }
                }
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[PaseCreateNewTaskRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}