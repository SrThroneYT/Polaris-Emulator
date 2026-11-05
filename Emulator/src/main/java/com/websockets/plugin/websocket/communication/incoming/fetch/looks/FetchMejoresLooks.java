package com.websockets.plugin.websocket.communication.incoming.fetch.looks;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.websockets.plugin.youtube.RoomYoutube;
import com.websockets.plugin.youtube.VideoYoutube;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.SQLException;

public class FetchMejoresLooks extends IncomingMessage {

    public static final String REQUEST_HEADER = "FetchMejoresLooks";
    // Header que el frontend está esperando como respuesta
    public static final String RESPONSE_HEADER = "ListMejoresLooksResponse";

    public FetchMejoresLooks(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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

        // 4. Crear la respuesta estructurada
        THashMap<String, Object> responseData = new THashMap<>();
        responseData.put("header", RESPONSE_HEADER);

        // Contenido del array 'data' que el frontend espera
        THashMap<String, Object> levelData = new THashMap<>();
        // Puedes añadir más campos aquí si son necesarios

        // Envía la respuesta como un array de un solo objeto [ { level: 45, permisos: 1
        // } ]
        responseData.put("data", new Object[] { levelData });
        responseData.put("looks", WebsocketsManager.getMejoresLooksManager().getArray(habbo));

        // 5. Enviar la respuesta
        String jsonResponse = new Gson().toJson(responseData);
        wsclient.send(jsonResponse);

    }
}