package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import com.google.gson.Gson;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SearchCatalogPageRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "SearchCatalogPageRequest";
    // Cambiado para diferenciar la lista de páginas del contenido de una página
    public static final String RESPONSE_HEADER = "CatalogPageDataResponse";

    public SearchCatalogPageRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        int pageId = -1;
        try {
            if (this.data.containsKey("pageId")) {
                pageId = Integer.parseInt(this.data.get("pageId").toString());
            } else if (this.data.containsKey("data")) {
                Object dataList = this.data.get("data");
                if (dataList instanceof List && !((List<?>) dataList).isEmpty()) {
                    Map<String, Object> firstItem = (Map<String, Object>) ((List<?>) dataList).get(0);
                    if (firstItem.containsKey("pageId")) {
                        pageId = Integer.parseInt(firstItem.get("pageId").toString());
                    }
                }
            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[SearchCatalogPageRequest] Error parsing pageId: " + e.getMessage());
        }

        if (pageId == -1)
            return;

        // 2. Obtener datos de la página e información de ItemBase
        Object pageData = WebsocketsManager.getCatalogWindowManager().getPageData(pageId);
        // Asumiendo que tienes un método similar para los items base
        Object itemBaseData = WebsocketsManager.getCatalogWindowManager().getItemBaseData(pageId);

        // 3. Estructurar la respuesta
        THashMap<String, Object> responseData = new THashMap<>();
        responseData.put("header", RESPONSE_HEADER);

        THashMap<String, Object> objetoInner = new THashMap<>();
        objetoInner.put("pageId", pageId);
        objetoInner.put("content", pageData);
        // Insertamos el nuevo objeto aquí
        objetoInner.put("itemBase", itemBaseData);

        THashMap<String, Object> dataItem = new THashMap<>();
        dataItem.put("objeto", objetoInner);

        responseData.put("data", new Object[] { dataItem });

        // 4. Enviar la respuesta
        String jsonResponse = new Gson().toJson(responseData);
        wsclient.send(jsonResponse);
    }
}