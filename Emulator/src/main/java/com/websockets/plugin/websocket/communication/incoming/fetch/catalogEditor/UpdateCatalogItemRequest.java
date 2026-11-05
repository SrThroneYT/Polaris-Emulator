package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import com.google.gson.Gson;
import java.sql.SQLException;

public class UpdateCatalogItemRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "UpdateCatalogItemRequest";

    public UpdateCatalogItemRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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
            // El ID se extrae solo para identificar el registro, NO para cambiarlo
            int itemId = ((Number) this.data.get("id")).intValue();
            int pageId = ((Number) this.data.get("page_id")).intValue();

            // Campos de texto
            String name = (String) this.data.get("name");

            // Campos numéricos
            int costCredits = ((Number) this.data.get("cost_credits")).intValue();
            int costPoints = ((Number) this.data.get("cost_points")).intValue();
            int pointsType = ((Number) this.data.get("points_type")).intValue();
            int order = ((Number) this.data.get("order")).intValue();

            // Campos de ofertas especiales (Booleanos convertidos a 0/1 en el front)
            int offerId = ((Number) this.data.get("offer_id")).intValue();
            int clubOnly = ((Number) this.data.get("club_only")).intValue();
            int haveOffer = ((Number) this.data.get("have_offer")).intValue();
            String extradata = (String) this.data.get("extradata");

            // Ejecutar la actualización en la DB
            // Mantenemos el ID fuera del SET para que sea imposible modificarlo
            try (java.sql.Connection connection = com.eu.habbo.Emulator.getDatabase().getDataSource().getConnection();
                    java.sql.PreparedStatement statement = connection.prepareStatement(
                            "UPDATE `catalog_items` SET `catalog_name` = ?, `cost_credits` = ?, `cost_points` = ?, " +
                                    "`points_type` = ?, `order_number` = ?, `offer_id` = ?, `club_only` = ?, " +
                                    "`have_offer` = ?, `extradata` = ? WHERE `id` = ? LIMIT 1")) {

                statement.setString(1, name);
                statement.setInt(2, costCredits);
                statement.setInt(3, costPoints);
                statement.setInt(4, pointsType);
                statement.setInt(5, order);
                statement.setInt(6, offerId);
                statement.setString(7, clubOnly == 1 ? "1" : "0"); // Dependiendo de tu DB si es Enum o Int
                statement.setString(8, haveOffer == 1 ? "1" : "0");
                statement.setString(9, extradata);
                statement.setInt(10, itemId); // El ID solo se usa para el WHERE

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("[CatalogEditor] DB Actualizada: Item " + itemId + " en Página " + pageId);
                }
            }


        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[UpdateCatalogItemRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}