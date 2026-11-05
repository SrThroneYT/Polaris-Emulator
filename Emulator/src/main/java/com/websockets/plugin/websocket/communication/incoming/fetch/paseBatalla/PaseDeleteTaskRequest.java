package com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Tarea;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

public class PaseDeleteTaskRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "PaseDeleteTaskRequest";

    public PaseDeleteTaskRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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
            // Extraer el name enviado desde el Formulario React
            String name = (String) this.data.get("name");

            if (name == null || name.trim().isEmpty()) {
                System.out.println("[PaseDeleteTaskRequest] El campo NAME llegó vacío desde el cliente.");
                return;
            }

            // Verificar si la tarea ya existe en el caché global buscando por su 'name'
            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());

            // 1. Obtener la tarea activa real correspondiente al usuario actual
            Tarea tarea = WebsocketsManager.getTareasManager().getTarea(name);

            if (tarea == null) {
                System.out.println("[PaseDeleteTaskRequest] No se encontró la tarea con NAME: " + name);
                return;
            }

            // Realizar el DELETE en la base de datos apuntando al id de la tarea encontrada
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                    PreparedStatement statement = connection
                            .prepareStatement("DELETE FROM websocketsth_pase_tareas WHERE id = ?")) {

                statement.setInt(1, tarea.id);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("[PaseDeleteTaskRequest] Tarea eliminada de la DB con ID: " + tarea.id
                            + " (NAME: " + name + ")");
                } else {
                    System.out.println(
                            "[PaseDeleteTaskRequest] Advertencia: No se borró ninguna fila de la DB para el ID: "
                                    + tarea.id);
                }
            }

            // Recargar o actualizar el caché global del manager de tareas para removerla
            // por completo
            WebsocketsManager.getTareasManager().update();

            // Refrescar la ventana de tareas al usuario en tiempo real para reflejar la
            // eliminación
            if (habbos != null && habbos.getTareas() != null) {
                habbos.getTareas().showTareasWindow();
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[PaseDeleteTaskRequest] Error al eliminar tarea: " + e.getMessage());
            e.printStackTrace();
        }
    }
}