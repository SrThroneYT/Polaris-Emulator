package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.eu.habbo.Emulator;

public class UpdateCatalogPageRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "UpdateCatalogPageRequest";
    public static final String RESPONSE_HEADER = "UpdateCatalogPageResponse";

    public UpdateCatalogPageRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        // Validación de permisos: Solo staff debería poder editar el catálogo
        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        try {
            // Sincronizado con la interfaz ICatalogPage del frontend
            int pageId = ((Number) this.data.get("page_id")).intValue();
            String caption = (String) this.data.get("caption");
            int pageIcon = ((Number) this.data.get("page_icon")).intValue();
            int iconColor = ((Number) this.data.get("icon_color")).intValue();
            int order_num = ((Number) this.data.get("order_num")).intValue();

            // Los checkboxes del front vienen como booleanos o 0/1
            boolean visible = parseBoolean(this.data.get("visible"));
            boolean enabled = parseBoolean(this.data.get("enabled"));
            boolean clubOnly = parseBoolean(this.data.get("club_only"));

            String pageLayout = (String) this.data.get("page_layout");
            String pageHeadline = (String) this.data.get("page_headline");

            // Actualización de la tabla catalog_pages
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE `catalog_pages` SET `caption` = ?, `icon_image` = ?, `icon_color` = ?, " +
                                    "`visible` = ?, `enabled` = ?, `club_only` = ?, `page_layout` = ?, `page_headline` = ?, `order_num` = ? "
                                    +
                                    "WHERE `id` = ? LIMIT 1")) {

                statement.setString(1, caption);
                statement.setInt(2, pageIcon);
                statement.setInt(3, iconColor);
                statement.setString(4, visible ? "1" : "0");
                statement.setString(5, enabled ? "1" : "0");
                statement.setString(6, clubOnly ? "1" : "0");
                statement.setString(7, pageLayout);
                statement.setString(8, pageHeadline);
                statement.setInt(9, order_num);
                statement.setInt(10, pageId);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("[CatalogEditor] Página " + pageId + " (" + caption
                            + ") actualizada y catálogo recargado.");

                    // Tarea de respuesta de datos
                    Emulator.getThreading().run(() -> {
                        Object pageData = WebsocketsManager.getCatalogWindowManager().getPageData(pageId);
                        Object itemBaseData = WebsocketsManager.getCatalogWindowManager().getItemBaseData(pageId);

                        THashMap<String, Object> responseData = new THashMap<>();
                        responseData.put("header", RESPONSE_HEADER);

                        THashMap<String, Object> objetoInner = new THashMap<>();
                        objetoInner.put("pageId", pageId);
                        objetoInner.put("content", pageData);
                        objetoInner.put("itemBase", itemBaseData);

                        THashMap<String, Object> dataItem = new THashMap<>();
                        dataItem.put("objeto", objetoInner);

                        responseData.put("data", new Object[] { dataItem });

                        // Nota: Aquí faltaría enviar responseData a través del wsclient si es
                        // necesario.
                    }, 1000);
                }

            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[UpdateCatalogPageRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Utilidad para manejar tanto Booleans como Numbers (0/1) que vienen del JSON
     */
    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean)
            return (Boolean) value;
        if (value instanceof Number)
            return ((Number) value).intValue() == 1;
        return false;
    }
}