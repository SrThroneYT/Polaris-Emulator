package com.websockets.plugin.habbo.BarraTop;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;
import com.websockets.plugin.youtube.RoomYoutube;

import gnu.trove.map.hash.THashMap;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;

public class BarraTop {

    private Habbo habbo;

    public BarraTop(Habbo habbo) {
        this.habbo = habbo;
    }

    public JsonArray getHabboInfo() {
        JsonArray habboInfo = new JsonArray();
        JsonObject jsonObject = new JsonObject();

        String look = WebsocketsManager.getConfig().getValue("global.look.ruta")
                + "&headonly=1&size=a&gesture=sml&head_direction=2&action=std";

        jsonObject.addProperty("look", look.replace("%LOOK%", habbo.getHabboInfo().getLook()));
        jsonObject.addProperty("username", habbo.getHabboInfo().getUsername());
        jsonObject.addProperty("user_id", habbo.getHabboInfo().getId());
        habboInfo.add(jsonObject);

        return habboInfo;
    }

    public JsonObject getArrayButtons() {
        String rutaImagesNotifications = WebsocketsManager.getConfig()
                .getValue("global_url.hotel.notifications") + "/websocketsBySrThrone";
        String rutaImagesBadges = WebsocketsManager.getConfig()
                .getValue("global_url.hotel.notifications").replace("/notifications/", "") + "/album1584/";

        JsonObject objetoPrincipal = new JsonObject();
        JsonArray arrayBotones = new JsonArray();
        JsonObject button;

        // --- BOTÓN: TOP USUARIOS ---
        button = new JsonObject();
        button.addProperty("title", "Comunidad");
        button.addProperty("img", rutaImagesNotifications + "/buttons/top.png");
        button.addProperty("img_width", "30");
        button.addProperty("img_height", "30");
        button.addProperty("event", "showMenuTopUsuarios");
        arrayBotones.add(button);

        // --- BOTÓN: YOUTUBE ---
        button = new JsonObject();
        button.addProperty("title", "Youtube");
        button.addProperty("img", rutaImagesNotifications + "/buttons/youtube.png");
        button.addProperty("img_width", "30");
        button.addProperty("img_height", "30");
        button.addProperty("event", "showYoutubeWindow");
        arrayBotones.add(button);

        // --- BOTÓN: EVENTOS ---
        button = new JsonObject();
        button.addProperty("title", "Eventos");
        button.addProperty("img", rutaImagesNotifications + "/buttons/events.gif");
        button.addProperty("img_width", "30");
        button.addProperty("img_height", "30");
        button.addProperty("event", "showMenuEventos");
        arrayBotones.add(button);

        // --- BOTÓN: ECONOMÍA ---
        button = new JsonObject();
        button.addProperty("title", "Economia");
        button.addProperty("img", rutaImagesNotifications + "/buttons/economia.png");
        button.addProperty("img_width", "33");
        button.addProperty("img_height", "38");
        button.addProperty("event", "showMenuEconomia");
        arrayBotones.add(button);

        // --- BOTÓN: VIP ---
        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
        button = new JsonObject();
        button.addProperty("title", "Menu VIP");
        button.addProperty("img", rutaImagesNotifications + "/buttons/VIP_Badge.png");
        button.addProperty("img_width", "30");
        button.addProperty("img_height", "30");
        button.addProperty("event", "showMenuVIPS");
        arrayBotones.add(button);

        // --- BOTÓN: STAFF (CON LISTA INTERNA) ---
        // Verificamos si el usuario tiene rango suficiente para ver opciones de Staff
        if (habbo.getHabboInfo().getRank().getId() >= WebsocketsManager.getConfig()
                .getInt("ws_settings_rank_min_personalice_catalog")) {

            button = new JsonObject();
            button.addProperty("title", "Staff");
            button.addProperty("img_width", "44");
            button.addProperty("img_height", "44");
            button.addProperty("img", rutaImagesBadges + "ADM.gif");
            button.addProperty("event", "toggleStaffMenu"); // El front usará esto para desplegar la lista

            // Creamos la lista de sub-botones
            JsonArray children = new JsonArray();

            // Sub-opción 1: Catálogo v2
            JsonObject sub1 = new JsonObject();
            sub1.addProperty("title", "Editor de Catálogo");
            sub1.addProperty("img_width", "44");
            sub1.addProperty("img_height", "44");
            sub1.addProperty("img", rutaImagesNotifications + "/buttons/catalogo.png");
            sub1.addProperty("event", "showCataloguesWindow");
            children.add(sub1);

            // Sub-opción 2: Panel de Control (Ejemplo)
            // JsonObject sub2 = new JsonObject();
            // sub2.addProperty("title", "Panel Control");
            // sub2.addProperty("img_width", "44");
            // sub2.addProperty("img_height", "44");
            // sub2.addProperty("img", rutaImagesNotifications + "/buttons/catalogo.png");
            // sub2.addProperty("event", "showAdminPanel");
            // children.add(sub2);

            // Sub-opción 3: Limpiar Salas (Ejemplo)
            // JsonObject sub3 = new JsonObject();
            // sub3.addProperty("title", "Limpiar Sala");
            // sub3.addProperty("img_width", "44");
            // sub3.addProperty("img_height", "44");
            // sub3.addProperty("img", rutaImagesNotifications + "/buttons/catalogo.png");
            // sub3.addProperty("event", "staffClearRoom");
            // children.add(sub3);

            // JsonObject sub2 = new JsonObject();
            // sub2.addProperty("title", "Panel Control");
            // sub2.addProperty("img_width", "44");
            // sub2.addProperty("img_height", "44");
            // sub2.addProperty("img", rutaImagesNotifications + "/buttons/catalogo.png");
            // sub2.addProperty("event", "showAdminPanel");
            // children.add(sub2);

            // // Sub-opción 3: Limpiar Salas (Ejemplo)
            // JsonObject sub3 = new JsonObject();
            // sub3.addProperty("title", "Limpiar Sala");
            // sub3.addProperty("img_width", "44");
            // sub3.addProperty("img_height", "44");
            // sub3.addProperty("img", rutaImagesNotifications + "/buttons/catalogo.png");
            // sub3.addProperty("event", "staffClearRoom");
            // children.add(sub3);


            // Añadimos la lista al botón padre
            button.add("children", children);

            arrayBotones.add(button);
        }

        objetoPrincipal.add("buttons", arrayBotones);
        return objetoPrincipal;
    }

    public static void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient != null) {
                String name = (String) data.get("name");
                if (name.equalsIgnoreCase("showYoutubeWindow")) {

                    RoomYoutube roomYoutube = WebsocketsManager.getYoutubeRoomManager()
                            .getRoomYoutube(wsclient.getHabbo().getHabboInfo().getCurrentRoom());

                    if (roomYoutube == null) {

                        WebsocketsManager.getYoutubeRoomManager()
                                .setRoomYoutube(wsclient.getHabbo().getHabboInfo().getCurrentRoom());
                        WebsocketsManager.getYoutubeRoomManager()
                                .getRoomYoutube(wsclient.getHabbo().getHabboInfo().getCurrentRoom())
                                .registerEspectador(wsclient.getHabbo());

                        WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(wsclient.getHabbo());
                    } else if (roomYoutube != null) {
                        roomYoutube.registerEspectador(wsclient.getHabbo());

                        WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(wsclient.getHabbo());
                    }

                    if (roomYoutube != null) {
                        roomYoutube.registerEspectador(wsclient.getHabbo());
                    }

                    WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(wsclient.getHabbo());

                }
            }
        } catch (Exception e) {
        }
    }
}