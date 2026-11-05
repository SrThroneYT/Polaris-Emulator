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

public class PaseVisitRoom extends IncomingMessage {

    public static final String REQUEST_HEADER = "PaseVisitRoom";

    public PaseVisitRoom(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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
            int room_id = ((Number) this.data.get("room_id")).intValue();

            if(room_id == 0){
                wsclient.getHabbo().alert("La sala a la que pertenece esta tarea no se ha definido");
                return;
            }

            wsclient.getHabbo().goToRoom(room_id);

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[PaseVisitRoom] Error al visitar sala: " + e.getMessage());
            e.printStackTrace();
        }
    }
}