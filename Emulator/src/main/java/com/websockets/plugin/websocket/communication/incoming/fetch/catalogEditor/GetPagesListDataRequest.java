package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
// Imports que ya utilizas o que son necesarios
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.eu.habbo.Emulator; // Asumimos que Emulator proporciona acceso a los datos de nivel
import com.eu.habbo.habbohotel.users.Habbo; // Para acceder al usuario y sus datos
import com.eu.habbo.habbohotel.users.HabboStats; // Para acceder a las estadísticas (donde podría estar el nivel)

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import com.google.gson.Gson; // Para serializar la respuesta
import java.sql.SQLException;

public class GetPagesListDataRequest extends IncomingMessage {

    // Header que el frontend (LevelFetcherWS) usa para la solicitud
    public static final String REQUEST_HEADER = "GetPagesListDataRequest";
    // Header que el frontend está esperando como respuesta
    public static final String RESPONSE_HEADER = "PagesListDataResponse";

    // 1. Constructor estándar
    public GetPagesListDataRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute(); // Se ejecuta inmediatamente
    }

    @Override
    public void execute() throws SQLException {
        // 2. Obtener el cliente de WebSocket y el usuario Habbo
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            // El cliente no está autenticado o la conexión no es válida.
            System.err.println("Advertencia: Cliente WS no encontrado o sin Habbo asociado.");
            return;
        }

        Habbo habbo = wsclient.getHabbo();

        // 3. ⭐️ Obtener el Nivel y Datos de Permisos del Usuario ⭐️
        int userLevel = 0;
        int userRankId = habbo.getHabboInfo().getRank().getId(); // Para permisos

        // Asumiendo que su sistema de niveles guarda el nivel en HabboStats
        HabboStats stats = habbo.getHabboStats();

        // Aquí deberías tener la lógica para obtener el nivel real:
        // Por ejemplo, si el nivel está en un campo llamado 'level' en HabboStats:
        // userLevel = stats.getLevel();

        // Dado que no tengo el campo, usaré una simulación basada en el rango para
        // demostrar:
        if (stats != null) {
            // 💡 Reemplaza esto con la lógica real para obtener el nivel del usuario.
            // Ejemplo: userLevel = stats.getLevelFromYourSystem();
            userLevel = 1; // Nivel de demostración
        }

        // Definir permisos: 1 si el rango es 7 o más, 0 si es menor
        int permisos = (userRankId >= 7) ? 1 : 0;

        // 4. Crear la respuesta estructurada
        THashMap<String, Object> responseData = new THashMap<>();
        responseData.put("header", RESPONSE_HEADER); // -> "LevelDataResponse"

        // Contenido del array 'data' que el frontend espera
        THashMap<String, Object> levelData = new THashMap<>();
        levelData.put("level", userLevel);
        levelData.put("permisos", permisos);
        // Puedes añadir más campos aquí si son necesarios

        // Envía la respuesta como un array de un solo objeto [ { level: 45, permisos: 1
        // } ]
        responseData.put("data", new Object[] { levelData });

        responseData.put("pages", WebsocketsManager.getCatalogWindowManager().initializeCatalogPages());

        // 5. Enviar la respuesta
        String jsonResponse = new Gson().toJson(responseData);
        wsclient.send(jsonResponse);
    }
}