package com.websockets.plugin.habbo.BarraTop.Recompensas;

import com.eu.habbo.Emulator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;

public class RecompensasManager {

    public int intervalTimeRecompensas;
    public int type_moneda;
    public int cantidad_moneda;

    public RecompensasManager() {

        this.registerConfig();

        this.intervalTimeRecompensas = WebsocketsManager.getConfig().getInt("interval_recompensas_ws_time");
        this.type_moneda = WebsocketsManager.getConfig().getInt("interval_recompensas_ws_type_moneda");
        this.cantidad_moneda = WebsocketsManager.getConfig().getInt("interval_recompensas_ws_cantidad_moneda");

        // 1. Iniciar el Timer (Scheduler)
        TimerRecompensas timerRecompensas = new TimerRecompensas(this);
        Emulator.getThreading().run(timerRecompensas);

    }

    public void registerConfig() {
        WebsocketsManager.getConfig().register("interval_recompensas_ws_time", "1200");
        WebsocketsManager.getConfig().register("interval_recompensas_ws_type_moneda", "-1");
        WebsocketsManager.getConfig().register("interval_recompensas_ws_cantidad_moneda", "5");
    }

    public void showBubbleAlert(HabboClient wsclient, String title, int time) {

        if (wsclient == null)
            return;

        JsonArray arrayEventos = new JsonArray();

        JsonObject eventJson = new JsonObject();
        eventJson.addProperty("id", "");
        eventJson.addProperty("title", title);
        eventJson.addProperty("time", time);
        eventJson.addProperty("targetSelector", ".draggable_BarraTop");
        eventJson.addProperty("imgAlt", "Economia");
        arrayEventos.add(eventJson);

        try {
            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showBubbleEventos,
                    arrayEventos));
        } catch (Exception e) {
        }
    }

    public void showAlertRecompensa(HabboClient wsclient, String title, String desc, String img, int time,
            String label) {

        if (wsclient == null)
            return;

        JsonArray arrayEventos = new JsonArray();

        JsonObject eventJson = new JsonObject();
        eventJson.addProperty("id", "");
        eventJson.addProperty("title", title);
        eventJson.addProperty("desc", desc);
        eventJson.addProperty("img", img);
        eventJson.addProperty("time", time);
        eventJson.addProperty("targetSelector", ".draggable_" + label);
        eventJson.addProperty("event", label);
        arrayEventos.add(eventJson);

        try {
            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showConfirmVentana,
                    arrayEventos));
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
            if (name != null && name.equalsIgnoreCase("eventSuccessPoints")) {

                String stringMoneda = "seasonal.name." + this.type_moneda;
                String nameMoneda = Emulator.getTexts().getValue(stringMoneda);
                int cantidadMoneda = this.cantidad_moneda;
                int segundos = this.intervalTimeRecompensas;
                double minutos = segundos / 60.0;

                if (wsclient.getHabbo().getHabboInfo().getHabboStats().getClubExpireTimestamp() >= Emulator
                        .getIntUnixTimestamp()) {
                    cantidadMoneda = cantidadMoneda * 2;
                }

                if (this.type_moneda == -1) {
                    wsclient.getHabbo().giveCredits(cantidadMoneda);
                } else {
                    wsclient.getHabbo().givePoints(this.type_moneda, cantidadMoneda);
                }
            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en IncomingListActions: " + e.getMessage());
        }
    }

}
