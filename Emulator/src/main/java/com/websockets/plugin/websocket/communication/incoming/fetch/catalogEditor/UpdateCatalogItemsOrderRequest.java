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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateCatalogItemsOrderRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "UpdateCatalogItemsOrderRequest";

    public UpdateCatalogItemsOrderRequest(String header, THashMap<String, Object> data, Channel client)
            throws SQLException {
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
            List<Object> itemsList = (ArrayList<Object>) this.data.get("items");

            if (itemsList == null || itemsList.isEmpty()) {
                return;
            }

            try (Connection connection = com.eu.habbo.Emulator.getDatabase().getDataSource().getConnection()) {
                connection.setAutoCommit(false);

                // OPTIMIZACIÓN SQL: Usamos "AND order_number <> ?"
                // Esto hace que la base de datos ignore la fila si el nuevo orden es igual al
                // que ya tiene.
                String query = "UPDATE `catalog_items` SET `order_number` = ? WHERE `id` = ? AND `order_number` <> ? LIMIT 1";

                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Object obj : itemsList) {
                        if (obj instanceof Map) {
                            Map<String, Object> itemData = (Map<String, Object>) obj;

                            int itemId = ((Number) itemData.get("id")).intValue();
                            int newOrder = ((Number) itemData.get("order")).intValue();

                            statement.setInt(1, newOrder);
                            statement.setInt(2, itemId);
                            statement.setInt(3, newOrder); // Solo actualiza si el valor es diferente
                            statement.addBatch();
                        }
                    }

                    int[] results = statement.executeBatch();
                    connection.commit();

                    int realChanges = 0;
                    for (int status : results) {
                        // En la mayoría de drivers JDBC, 1 o más significa fila cambiada.
                        if (status > 0)
                            realChanges++;
                    }

                    if (realChanges > 0) {
                        System.out.println("[CatalogEditor] Optimización: Solo " + realChanges
                                + " ítems requerían actualización de orden.");
                    }

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[UpdateCatalogItemsOrderRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}