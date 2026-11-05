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

public class UpdateItemBaseRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "UpdateItemBaseRequest";
    public static final String RESPONSE_HEADER = "UpdateItemBaseResponse";

    public UpdateItemBaseRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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

            // Mapeo seguro de ID (viene como 71036.0)
            int id = parseObjectToInt(this.data.get("id"));
            String publicName = (String) this.data.get("public_name");
            String itemName = (String) this.data.get("item_name");

            // Campos de interacción
            String interactionType = (String) this.data.get("interaction_type");

            // CORRECCIÓN: Manejo seguro de interaction_modes_count
            int interactionModesCount = parseObjectToInt(this.data.get("interaction_modes_count"));

            // PROCESAMIENTO DE VENDING_IDS
            Object vendingObj = this.data.get("vending_ids");
            String vendingIds = "";
            if (vendingObj != null) {
                // Convertimos a String y eliminamos {}, [], y espacios innecesarios
                vendingIds = String.valueOf(vendingObj)
                        .replaceAll("[\\{\\}\\[\\]]", "")
                        .replace(" ", "");
            }

            String multiheight = (String) this.data.get("multiheight");

            int width = parseObjectToInt(this.data.get("width"));
            int length = parseObjectToInt(this.data.get("length"));

            // stack_height suele ser Double, lo manejamos con cuidado
            double stackHeight = 0.0;
            if (this.data.get("stack_height") instanceof Number) {
                stackHeight = ((Number) this.data.get("stack_height")).doubleValue();
            }

            // Sincronización de permisos
            int allowStack = parseBooleanToInt(this.data.get("allow_stack"));
            int allowSit = parseBooleanToInt(this.data.get("allow_sit"));
            int allowLay = parseBooleanToInt(this.data.get("allow_lay"));
            int allowWalk = parseBooleanToInt(this.data.get("allow_walk"));
            int allowGift = parseBooleanToInt(this.data.get("allow_gift"));
            int allowTrade = parseBooleanToInt(this.data.get("allow_trade"));
            int allowMarketplace = parseBooleanToInt(this.data.get("allow_marketplace"));
            int allowInventoryStack = parseBooleanToInt(this.data.get("allow_inventory_stack"));

            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                    PreparedStatement statement = connection.prepareStatement(
                            "UPDATE `items_base` SET `public_name` = ?, `item_name` = ?, `width` = ?, `length` = ?, `stack_height` = ?, "
                                    +
                                    "`allow_stack` = ?, `allow_sit` = ?, `allow_lay` = ?, `allow_walk` = ?, `allow_gift` = ?, "
                                    +
                                    "`allow_trade` = ?, `allow_marketplace_sell` = ?, `allow_inventory_stack` = ?, " +
                                    "`interaction_type` = ?, `interaction_modes_count` = ?, `vending_ids` = ?, `multiheight` = ? "
                                    +
                                    "WHERE `id` = ? LIMIT 1")) {

                statement.setString(1, publicName);
                statement.setString(2, itemName);
                statement.setInt(3, width);
                statement.setInt(4, length);
                statement.setDouble(5, stackHeight);

                // Convertimos el int (0 o 1) a String ("0" o "1") para el ENUM
                statement.setString(6, String.valueOf(allowStack));
                statement.setString(7, String.valueOf(allowSit));
                statement.setString(8, String.valueOf(allowLay));
                statement.setString(9, String.valueOf(allowWalk));
                statement.setString(10, String.valueOf(allowGift));
                statement.setString(11, String.valueOf(allowTrade));
                statement.setString(12, String.valueOf(allowMarketplace));
                statement.setString(13, String.valueOf(allowInventoryStack));

                statement.setString(14, interactionType);
                statement.setInt(15, interactionModesCount);
                statement.setString(16, vendingIds);
                statement.setString(17, multiheight);
                statement.setInt(18, id);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("[ItemEditor] Item Base " + id + " actualizado correctamente.");
                }
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[UpdateItemBaseRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convierte de forma segura cualquier objeto numérico (Double de JS) a int.
     */
    private int parseObjectToInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return (int) Double.parseDouble((String) value);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    private int parseBooleanToInt(Object value) {
        if (value instanceof Boolean)
            return (Boolean) value ? 1 : 0;
        if (value instanceof Number)
            return ((Number) value).intValue() >= 1 ? 1 : 0;
        return 0;
    }
}