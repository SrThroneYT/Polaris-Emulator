package com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Tarea;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

public class EntregarTareaRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "EntregarTareaRequest";

    public EntregarTareaRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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

            // Extraer
            String username = (String) this.data.get("username");
            String taskName = (String) this.data.get("taskName");

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());

            // 1. Obtener la tarea activa real correspondiente al usuario actual
            Tarea tarea = WebsocketsManager.getTareasManager().getTarea(taskName);

            if (tarea == null) {
                wsclient.getHabbo().alert("[EntregarTareaRequest] No se encontró la tarea con NAME: " + taskName);
                return;
            }

            if(tarea.type_id == 1){
                wsclient.getHabbo().alert("[EntregarTareaRequest] Prohibido entregar la tarea: " + taskName);
                return;
            }

            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(username);

            if (habbo == null) {
                wsclient.getHabbo().alert("No se encontró el usuario: " + username + " puede que este desconectado.");
                return;
            }

            habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                    WebsocketsManager.getTareasManager().getTarea(tarea.name));

        } catch (Exception e) {
            WebsocketsManager.utilities()
                    .LOGGER("[EntregarTareaRequest] Error al intentar entregar la tarea: " + e.getMessage());
            e.printStackTrace();
        }
    }
}