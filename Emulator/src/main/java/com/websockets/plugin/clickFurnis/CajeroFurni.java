package com.websockets.plugin.clickFurnis;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

public class CajeroFurni {

    public CajeroFurni() {

    }

    public static void eventToggleFurni(Habbo habbo, HabboItem item) {
        // atm_moneymachine

        if (item.getBaseItem().getName().equalsIgnoreCase("atm_moneymachine")) {
            if (comprobateCerca(item, habbo.getHabboInfo().getCurrentRoom(), habbo)) {
                showVentana(habbo, item);
            }
        }

    }

    public static boolean comprobateCerca(HabboItem item, Room room, Habbo habbo) {
        if (RoomLayout.tilesAdjecent(habbo.getRoomUnit().getCurrentLocation(),
                room.getLayout().getTile(item.getX(), item.getY()))
                || (habbo.getRoomUnit().getCurrentLocation().x == item.getX()
                        && habbo.getRoomUnit().getCurrentLocation().y == item.getY())) {
            return true;
        } else {
            return false;
        }
    }

    // CORRECCIÓN: Método estático para poder llamarlo desde IncomingListActions
    public static void showVentana(Habbo habbo, HabboItem item) {
        try {
            if (habbo == null)
                return;

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
            String furniRuta = WebsocketsManager.getConfig().getValue("global_url.hotel.furnis")
                    + "icons/%name_furni%_icon.png";

            JsonArray arrayEventos = new JsonArray();
            JsonObject eventJson = new JsonObject();

            eventJson.addProperty("username", habbo.getHabboInfo().getUsername());

            int seasonal_primary_id = Emulator.getConfig().getInt("seasonal.primary.type"); // Ej: 5
            int seasonal_credits_id = -1;

            String name_seasonal_primary = Emulator.getTexts().getValue("seasonal.name." + seasonal_primary_id);
            String name_seasonal_credits = Emulator.getTexts().getValue("seasonal.name.-1");

            // Moneda A (Créditos)
            eventJson.addProperty("seasonal_credits", habbo.getHabboInfo().getCredits());
            eventJson.addProperty("name_seasonal_credits", name_seasonal_credits);
            eventJson.addProperty("id_seasonal_credits", seasonal_credits_id);

            // Moneda B (Puntos/Seasonal)
            eventJson.addProperty("seasonal_primary", habbo.getHabboInfo().getCurrencyAmount(seasonal_primary_id));
            eventJson.addProperty("name_seasonal_primary", name_seasonal_primary);
            eventJson.addProperty("id_seasonal_primary", seasonal_primary_id);

            eventJson.addProperty("min_change_conversion_primary", 100);
            eventJson.addProperty("look_bot",
                    "<img src='" + furniRuta.replace("%name_furni%", item.getBaseItem().getName())
                            + "' style='margin-top: 1rem;zoom: 2;'/>");
            eventJson.addProperty("titulo_header", "Cajero automático");
            eventJson.addProperty("titulo", "<h3> Trámites en el cajero</h3>");
            eventJson.addProperty("descripcion",
                    "<span>El cajero te ayudará a realizar tus cambios de monedas...</span>");
            eventJson.addProperty("event", "eventChangeMonedasCajeraBot");

            arrayEventos.add(eventJson);

            if (habbos == null || habbos.getHabboClient() == null) {
                return;
            }

            habbos.getHabboClient().send(new MessageManager("MessageManager", true,
                    Outcoming.showVentanaCambioBotCajero, arrayEventos));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ocultVentana(Habbo habbo) {
        try {
            if (habbo == null) {
                return;
            }

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
            if (habbos == null || habbos.getHabboClient() == null) {
                return;
            }

            habbos.getHabboClient().send(new MessageManager("MessageManager", false,
                    Outcoming.showVentanaCambioBotCajero, null));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
