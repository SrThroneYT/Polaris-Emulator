package com.websockets.plugin.habbo.BarraTop.VIP;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;

public class VipManager {

    public String infoV;

    public VipManager() {

        this.updateData();

    }

    public void updateData() {
        String infoV = ""
                + "<div style='height: 2px;margin: 0.5rem 0px auto;border-top: 1px solid rgb(201, 201, 201);border-bottom: 1px solid rgb(255, 255, 255);margin-bottom: 0.5rem;'></div>"

                + "<div style='padding:0.5rem;'>"
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'> Membresía VIP</span> <br>"
                + "</div>"
                + "<div style='height:2px; margin: auto 0; margin-top:0.5rem; border-top: 1px solid #c9c9c9; border-bottom: 1px solid #fff; margin-bottom: 0.5rem;'></div>"

                + "<section class='contentGif flex' style='align-items: center; justify-content: center;'>"
                + "<img class='gif' src='economia/vip.png?v2' style='width: 65px;height: 60px;object-fit: contain;border-radius: 12px;position: relative;margin-left: 0rem;margin-right: 0.2rem;'/> <br>"
                + "<div style='width: 80%;display: flex;flex-direction: column;'> "
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'>VIP's</span>"
                + "<span style='padding-left: 1rem;'><b>¡Usuarios con privilegios en el hotel!</b><br> Los usuarios vips, tienen la posibilidad de usar comandos especiales, realizar acciones privilegiadas y tener acceso a una gran parte del hotel.</span> "
                + "</div> "
                + "</section> "

                + "<div style='height:2px; margin: auto 0; margin-top:0.5rem; border-top: 1px solid #c9c9c9; border-bottom: 1px solid #fff; margin-bottom: 0.5rem;'></div>"

                + "<section class='contentGif flex' style='align-items: center; justify-content: center;'>"
                + "<div style='width: 100%;display: flex;flex-direction: column;'> "
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'>¿Qué beneficios tengo siendo VIP?</span>"
                + "<section style='display:flex; padding-left: 2rem;'>"
                + "<div style='width: 20%; margin-bottom: 0.2rem;'><span><b style='color: #d77707;'>Funciones especiales:</b></br> Al presionar tu usuario podrás: <br><br><ul><li>Armar porros</li><li>Fumar porros</li></ul></span></div>"
                + "<div style='width: 45%; margin-bottom: 0.2rem;'><span><b style='color: #d77707;'>Enables especiales:</b> <ul><li><b>Enable VIP:</b> Se habilitara un enable de VIP cada vez que entres a las salas.</li><li><b>Boton para VIPS:</b> Contarás con un boton único para los VIPS, en donde podrás ver la lista de beneficios y podrás configurar si quieres que se muestre tu enable cuando entres a una sala.</li> <li>Otros beneficios...</li></ul></div> "
                + "<div style='width: 30%; margin-bottom: 0.2rem;'><span><b style='color: #d77707;'>Recompensas:</b> <ul><li><b>Doble de monedas:</b> Al estar conectado durante 10 minutos, obtendras la cantidad de creditos establecidos x2. </li></ul></div> "
                + "</section>"
                + "</div>"
                + "</section> "
                + "</div> "

                + "<div style='height:2px; margin: auto 0; margin-top:0.5rem; border-top: 1px solid #c9c9c9; border-bottom: 1px solid #fff; margin-bottom: 0.5rem;'></div>"

                + "<section class='contentGif flex' style='align-items: center; justify-content: center;'>"
                + "<div style='width: 100%;display: flex;flex-direction: column;'> "
                + "<span style='color: #252525ff; padding: 7px 15px; font-weight: 800; font-size: 19px; margin-bottom: 4px; font-family: \"Ubuntu Condensed\", sans-serif; border-radius: 5px; text-transform:uppercase;'>¿Cómo puedo comprar VIP?</span>"
                + "<span style='padding-left: 1rem;'><b>Podrás comprarlo teniendo cierta cantidad de HEVX</b></br>Una vez consigas la cantidad de HEVX necesarias, dirigete al catalogo del hotel y busca la sección 'Membresía VIP', allí podrás comprarlo bien sea de forma mensual o trimestral.</span>"
                + "</br> "
                + "</br> "
                + "</div> "
                + "</section> "

                + "";

        WebsocketsManager.getConfig().register("ws_economia_info_vip", infoV);
        // WebsocketsManager.getConfig().updateData("ws_economia_info_vip", infoV);

        this.infoV = WebsocketsManager.getConfig().getValue("ws_economia_info_vip");
    }

    public JsonArray getArray(Habbo habbo) {
        JsonArray arrayEventos = null;
        try {

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

            arrayEventos = new JsonArray();

            String url_images_notifications = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications")
                    + "websocketsBySrThrone/";
            String url_look = WebsocketsManager.getConfig().getValue("global.look.ruta")
                    + "&headonly=0&size=l&gesture=sml&head_direction=2&action=std";

            // 1. Definimos los rangos con valores por defecto por si habbo es null
            int userVip = habbos.getVip() == true ? 1 : 0;
            String username = (habbo != null) ? habbo.getHabboInfo().getUsername() : "";

            // Creamos el objeto principal que contendrá la info y la lista
            JsonObject headerData = new JsonObject();
            headerData.addProperty("username", username);
            headerData.addProperty("look", url_look.replace("%LOOK%", habbo.getHabboInfo().getLook()));
            headerData.addProperty("userVip", userVip);
            headerData.addProperty("timeVip", habbos.getVipTime());
            headerData.addProperty("enableVipActive", habbos.getEnableVipActive() ? 1 : 0);
            headerData.addProperty("url_images_notifications", url_images_notifications);
            headerData.addProperty("image_vip_club", url_images_notifications + "vip/vip_club.png");
            headerData.addProperty("informacion_vip", this.infoV);

            // Metemos este objeto único en el array principal
            arrayEventos.add(headerData);

            return arrayEventos;

        } catch (Exception e) {
        }

        return arrayEventos;
    }

    public void showVentana(HabboClient wsclient) {

        if (wsclient == null)
            return;

        try {
            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showVentanaVIP,
                    this.getArray(wsclient.getHabbo())));
        } catch (Exception e) {
        }
    }

    /**
     * Maneja peticiones directas desde el WebSocket
     */
    public void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient == null)
                return;

            String name = (String) data.get("name");
            if (name != null && name.equalsIgnoreCase("showMenuVIPS")) {
                this.showVentana(wsclient);
            }
            if (name != null && name.equalsIgnoreCase("editEnableVIP")) {
                try {

                    int getValue = (int) ((double) data.get("action"));

                    Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());

                    habbos.setEnableVipActive(getValue);

                    this.showVentana(wsclient);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en IncomingListActions: " + e.getMessage());
        }
    }

}
