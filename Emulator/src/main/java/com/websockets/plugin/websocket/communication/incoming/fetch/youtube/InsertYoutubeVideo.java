package com.websockets.plugin.websocket.communication.incoming.fetch.youtube;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.websockets.plugin.youtube.RoomYoutube;
import com.websockets.plugin.youtube.VideoYoutube; // Importante importar el modelo de Video
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.SQLException;

public class InsertYoutubeVideo extends IncomingMessage {

    public static final String REQUEST_HEADER = "InsertYoutubeVideo";
    public static final String RESPONSE_HEADER = "YoutubeDataResponse";

    public InsertYoutubeVideo(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        // El execute() se llama automáticamente si tu arquitectura de IncomingMessage
        // lo permite,
        // si no, dejarlo aquí.
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

        // 1. Obtener o crear el gestor de YouTube para la sala
        RoomYoutube roomYoutube = WebsocketsManager.getYoutubeRoomManager().getRoomYoutube(room);

        if (roomYoutube == null) {
            WebsocketsManager.getYoutubeRoomManager().setRoomYoutube(room);
            roomYoutube = WebsocketsManager.getYoutubeRoomManager().getRoomYoutube(room);
        }

        // 2. Extraer datos del JSON enviado por el cliente
        String url = (String) this.data.get("url");
        if (url == null || url.isEmpty())
            return;

        // 3. Crear el nuevo objeto VideoYoutube
        VideoYoutube video = new VideoYoutube(0); // ID 0 porque es nuevo
        video.setUrl(url);
        video.setTime(0); // Inicia en el segundo 0
        video.setState(1); // Estado inicial (ej: reproduciendo)
        video.setActive(1); // Marcar como activo

        // 4. Insertar usando el método que acomodamos anteriormente
        roomYoutube.insertVideo(video);

        // 5. Notificar a la sala o al cliente que la ventana debe actualizarse
        WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(habbo);
    }
}