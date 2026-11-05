package com.websockets.plugin.websocket.communication.incoming.fetch.youtube;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.websockets.plugin.youtube.RoomYoutube;
import com.websockets.plugin.youtube.VideoYoutube;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.SQLException;

public class SetVideoTime extends IncomingMessage {

    public static final String REQUEST_HEADER = "SetVideoTime";

    public SetVideoTime(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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

        // 2. Extraer la URL o ID del video enviado por el cliente React
        String url = (String) this.data.get("url");
        if (url == null || url.isEmpty())
            return;

        int time = ((Number) this.data.get("time")).intValue();

        // 3. Lógica de Activación
        // Buscamos el video en la lista actual de la sala
        VideoYoutube videoToActivate = null;
        for (VideoYoutube video : roomYoutube.getVideos().values()) {
            if (video.getUrl().equals(url)) {
                videoToActivate = video;
                break;
            }
        }

        if (videoToActivate != null) {

            // Activamos el video seleccionado
            videoToActivate.setTime(time);
            videoToActivate.saveToDatabase();

            // Enviar datos de sincronizacion a todos
            WebsocketsManager.getYoutubeRoomManager().sincroniceVideoData(room, habbo);

        }
    }
}