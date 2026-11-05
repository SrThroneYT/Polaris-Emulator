package com.websockets.plugin.websocket.communication.incoming.fetch.youtube;

import com.websockets.plugin.WebsocketsManager;
// Imports que ya utilizas o que son necesarios
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo; // Para acceder al usuario y sus datos

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import com.google.gson.Gson; // Para serializar la respuesta
import java.sql.SQLException;

public class GetRequestYoutubeDataResponse extends IncomingMessage {

    // Header que el frontend (LevelFetcherWS) usa para la solicitud
    public static final String REQUEST_HEADER = "GetRequestYoutubeDataResponse";
    // Header que el frontend está esperando como respuesta
    public static final String RESPONSE_HEADER = "YoutubeDataResponse";

    // 1. Constructor estándar
    public GetRequestYoutubeDataResponse(String header, THashMap<String, Object> data, Channel client)
            throws SQLException {
        super(header, data, client);
        this.execute(); // Se ejecuta inmediatamente
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
            return; // Seguridad adicional

        // 1. Preparamos el mapa de respuesta principal
        THashMap<String, Object> responseData = new THashMap<>();
        responseData.put("header", RESPONSE_HEADER);

        // 2. Preparamos el objeto interno que contiene toda la información
        THashMap<String, Object> messageContent = new THashMap<>();

        // Verificación de permisos (dueño de sala)
        int permisos = (room.getOwnerId() == habbo.getHabboInfo().getId()) ? 1 : 0;
        messageContent.put("permisos", permisos);

        // 3. Insertamos los datos de Youtube DENTRO del mismo objeto de contenido
        // Esto asegura que al frontend le llegue todo en el primer elemento del array
        // data
        messageContent.put("youtubeData", WebsocketsManager.getYoutubeRoomManager().getArrayRoom(room.getId(), habbo));

        // 4. Importante: Envolvemos el objeto en un array para la propiedad "data"
        // Esto es lo que el MessageManager suele iterar: data[0]
        responseData.put("data", new Object[] { messageContent });

        // 5. Serialización y envío
        String jsonResponse = new Gson().toJson(responseData);
        wsclient.send(jsonResponse);
    }
}