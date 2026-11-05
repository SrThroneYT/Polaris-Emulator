package com.websockets.plugin.websocket.communication.incoming.fetch.youtube;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.websockets.plugin.youtube.RoomYoutube;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.SQLException;

public class SetVideoClose extends IncomingMessage {

    public static final String REQUEST_HEADER = "SetVideoClose";

    public SetVideoClose(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        Habbo habbo = wsclient.getHabbo();
        Room room = habbo.getHabboInfo().getCurrentRoom();

        if (room == null)
            return;

        // 1. Obtener el gestor de YouTube de la sala
        RoomYoutube roomYoutube = WebsocketsManager.getYoutubeRoomManager().getRoomYoutube(room);

        if (roomYoutube == null) {
            return; // Si no hay gestor, no hay videos que activar
        }

        WebsocketsManager.getYoutubeRoomManager().ocultYoutubeVentana(habbo);

        // Enviar datos de sincronizacion a todos
        WebsocketsManager.getYoutubeRoomManager().sincroniceVideoData(room, habbo);

    }
}