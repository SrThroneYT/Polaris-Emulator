package com.websockets.plugin.habbo.BarraTop.Economia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;

public class EconomiaManager {

    private String info;

    public void registerConfig() {

        String data = ""
                + "<div style='height: 2px;margin: 0.5rem 0px auto;border-top: 1px solid rgb(201, 201, 201);border-bottom: 1px solid rgb(255, 255, 255);margin-bottom: 0.5rem;'></div>"

                + "<div style='padding:0.5rem;'>"
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'> ¡Economía del hotel!</span> <br>"
                + "</div>"
                + "<div style='height:2px; margin: auto 0; margin-top:0.5rem; border-top: 1px solid #c9c9c9; border-bottom: 1px solid #fff; margin-bottom: 0.5rem;'></div>"

                + "<section class='contentGif flex' style='align-items: center; justify-content: center;'>"
                + "<img class='gif' src='economia/base2.png?v2' style='width: 84px;border-radius: 12px;'/> <br>"
                + "<img class='gif' src='economia/-1.png?v2' style='width: 40px;border-radius: 12px;position: relative;margin-left: -3.8rem;margin-right: 1rem;'/> <br>"
                + "<div style='width: 80%;display: flex;flex-direction: column;'> "
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'>Creditos</span>"
                + "<span style='padding-left: 1rem;'><b>La moneda básica del hotel</b></br> Podrás obtenerlos en eventos abiertos por los Game Masters.<br> Puedes participar máximo: 3 veces <br> Obtendrás: 30 créditos en cada ronda. <br><br> Como también... Al estar conectado durante 10 minutos, te aparecerá una alerta de recompensa que te entregará cierta cantidad de creditos.</span> "
                + "</div> "
                + "</section> "

                + "<div style='height:2px; margin: auto 0; margin-top:0.5rem; border-top: 1px solid #c9c9c9; border-bottom: 1px solid #fff; margin-bottom: 0.5rem;'></div>"

                + "<section class='contentGif flex' style='align-items: center; justify-content: center;'>"
                + "<img class='gif' src='economia/base2.png?v2' style='width: 84px;border-radius: 12px;'/> <br>"
                + "<img class='gif' src='economia/5.png?v2' style='width: 40px;border-radius: 12px;position: relative;margin-left: -3.8rem;margin-right: 1rem;'/> <br>"
                + "<div style='width: 80%;display: flex;flex-direction: column;'> "
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'>HEVX</span>"
                + "<span style='padding-left: 1rem;'><b>La moneda más valiosa del hotel</b><br> la podrás obtener realizando un cambio con tus creditos.<br> 100 creditos = 1 HEVX</span> "
                + "</div> "
                + "</section> "

                + "<div style='height:2px; margin: auto 0; margin-top:0.5rem; border-top: 1px solid #c9c9c9; border-bottom: 1px solid #fff; margin-bottom: 0.5rem;'></div>"

                + "<section class='contentGif flex' style='align-items: center; justify-content: center;'>"
                + "<div style='width: 100%;display: flex;flex-direction: column;'> "
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'>¿Cómo puedo cambiarlas?</span>"
                + "<span style='padding-left: 1rem;'><b>Dirígete al banco oficial del hotel, encuentra al bot del cajero y habla con el.</b></br>Te mostrará una interface interactiva en la que podrás realizar tu cambio. <br/> Podrás cambiar de créditos a HEVX o viceversa.</span> "
                + "</div> "
                + "</section> "
                + "</div> "
                + "</br> "
                + "</br> "
                + "";

        WebsocketsManager.getConfig().register("ws_economia_info", data);
    }

    public EconomiaManager() {
        this.registerConfig();
        this.info = WebsocketsManager.getConfig().getValue("ws_economia_info");
    }

    /**
     * Maneja peticiones directas desde el WebSocket
     */
    public void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient == null)
                return;

            String name = (String) data.get("name");
            if (name != null && name.equalsIgnoreCase("showMenuEconomia")) {

                wsclient.send(new MessageManager("MessageManager", true,
                        Outcoming.showMenuEconomia,
                        this.getArray(wsclient.getHabbo())));

            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en IncomingListActions: " + e.getMessage());
        }
    }

    public JsonArray getArray(Habbo habbo) {
        JsonArray arrayEventos = new JsonArray();
        String defaultImage = "//i.imgur.com/FcLqEmy.png";
        String url_images_notifications = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications")
                + "websocketsBySrThrone/";

        // 1. Definimos los rangos con valores por defecto por si habbo es null
        int userRank = (habbo != null) ? habbo.getHabboInfo().getRank().getId() : 0;
        String username = (habbo != null) ? habbo.getHabboInfo().getUsername() : "";
        int minRank = 7; // Valor por defecto

        try {
            minRank = Integer.parseInt(WebsocketsManager.getConfig().getValue("min_rank_edit_eventos", "7"));
        } catch (Exception e) {
            // En caso de que la config no sea un número válido
        }

        // Creamos el objeto principal que contendrá la info y la lista
        JsonObject headerData = new JsonObject();
        headerData.addProperty("username", username);
        headerData.addProperty("url_images_notifications", url_images_notifications);
        headerData.addProperty("informacion", this.info);

        // Metemos este objeto único en el array principal
        arrayEventos.add(headerData);

        return arrayEventos;
    }

}
