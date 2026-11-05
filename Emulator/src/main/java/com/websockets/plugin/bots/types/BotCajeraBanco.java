package com.websockets.plugin.bots.types;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.plugin.events.bots.BotServerItemEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BotCajeraBanco extends Bot {

    public static Map<Set<String>, Integer> serverKeywords = new ConcurrentHashMap<>();
    public static Bot viewBot; // Guarda la referencia para actualizar la ventana tras el canje

    public BotCajeraBanco(ResultSet set) throws SQLException {
        super(set);
    }

    public BotCajeraBanco(Bot bot) {
        super(bot);
    }

    public static void initialise() {
        if (serverKeywords == null)
            serverKeywords = new ConcurrentHashMap<>();

        serverKeywords.clear();

        JsonParser parser = new JsonParser();
        JsonArray gsonArr = parser
                .parse(WebsocketsManager.getConfig().getValue("cajeras_keys_json_array"))
                .getAsJsonArray();

        for (JsonElement obj : gsonArr) {
            JsonObject gsonObj = obj.getAsJsonObject();
            String keysString = gsonObj.get("keys").getAsString();
            int id = gsonObj.get("id").getAsInt();

            String[] keys = keysString.split(";");
            Set<String> ks = new HashSet<>();
            Collections.addAll(ks, keys);
            serverKeywords.put(ks, id);
        }
    }

    public static void dispose() {
        serverKeywords.clear();
    }

    @Override
    public void onUserSay(final RoomChatMessage message) {
        if (this.getRoomUnit().hasStatus(RoomUnitStatus.MOVE) || this.getRoom() == null) {
            return;
        }

        double distance = this.getRoomUnit().getCurrentLocation()
                .distance(message.getHabbo().getRoomUnit().getCurrentLocation());

        if (distance <= Emulator.getConfig().getInt("hotel.bot.butler.commanddistance")) {
            if (message.getUnfilteredMessage() != null) {
                String msgLower = message.getUnfilteredMessage().toLowerCase();

                for (Map.Entry<Set<String>, Integer> set : serverKeywords.entrySet()) {
                    for (String keyword : set.getKey()) {

                        // CORRECCIÓN: Uso de doble barra \\b para Regex en Java
                        if (msgLower.matches(".*\\b" + keyword.toLowerCase() + "\\b.*")) {

                            BotServerItemEvent serveEvent = new BotServerItemEvent(this, message.getHabbo(),
                                    set.getValue());
                            if (Emulator.getPluginManager().fireEvent(serveEvent).isCancelled()) {
                                return;
                            }

                            this.lookAt(serveEvent.habbo);
                            final Bot bot = this;
                            final String currentKey = keyword.toLowerCase();

                            // Tareas a ejecutar
                            List<Runnable> tasks = new ArrayList<>();

                            if (currentKey.equals("hola")) {
                                tasks.add(() -> {
                                    String botMsg = WebsocketsManager.getTexts().getValue("keys_botCajeraBanco_hola")
                                            .replace("%username%", serveEvent.habbo.getHabboInfo().getUsername());
                                    bot.talk(botMsg);
                                });
                            } else if (currentKey.equals("cambiar")) {
                                tasks.add(() -> {
                                    String botMsg = WebsocketsManager.getTexts().getValue("keys_botCajeraBanco_cambiar")
                                            .replace("%username%", serveEvent.habbo.getHabboInfo().getUsername());
                                    bot.talk(botMsg);
                                    showVentana(serveEvent.habbo, bot);
                                });
                            }

                            if (distance <= 2) {
                                // Ejecución
                                for (Runnable task : tasks) {
                                    Emulator.getThreading().run(task, 500);
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private int mensaje = 0;
    private int timer = 0;

    @Override
    public void cycle(boolean allowBotsWalk) {
        super.cycle(allowBotsWalk);
        timer++;
        if (timer >= 120) {
            timer = 0;
            mensaje = (mensaje == 0) ? 1 : 0;
            String[] msgs = WebsocketsManager.getTexts().getValue("keys_botCajeraBanco_msg_actions").split(";");
            if (msgs.length > mensaje) {
                this.talk(msgs[mensaje]);
            }
        }
    }

    public void onUserEnter(Habbo habbo) {
        if (habbo.getHabboInfo().getCurrentRoom() != null) {
            final Bot bot = this;
            Emulator.getThreading().run(() -> {
                try {
                    bot.whisper(WebsocketsManager.getTexts().getValue("keys_botCajeraBanco_visitor")
                            .replace("%username%", habbo.getHabboInfo().getUsername()), habbo);
                } catch (Exception ignored) {
                }
            }, 1000);
        }
    }

    // CORRECCIÓN: Método estático para poder llamarlo desde IncomingListActions
    public static void showVentana(Habbo habbo, Bot bot) {
        try {
            if (habbo == null || bot == null)
                return;
            viewBot = bot;

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
            String lookRuta = WebsocketsManager.getConfig().getValue("global.look.ruta")
                    + "&headonly=0&size=l&gesture=sml&head_direction=3&action=std,wave&img_format=gif";

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
                    "<img style='margin-top: -2.5rem;' src='" + lookRuta.replace("%LOOK%", bot.getFigure()) + "'/>");

            eventJson.addProperty("titulo_header", bot.getName());
            eventJson.addProperty("titulo", "<h3>¿Cuál será tu trámite?</h3>");
            eventJson.addProperty("descripcion", "<span>Voy a ayudarte a realizar tus cambios de monedas...</span>");
            eventJson.addProperty("event", "eventChangeMonedasCajeraBot");

            arrayEventos.add(eventJson);

            habbos.getHabboClient().send(new MessageManager("MessageManager", true,
                    Outcoming.showVentanaCambioBotCajero, arrayEventos));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void IncomingListActions(Map<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient == null || wsclient.getHabbo() == null)
                return;

            String name = (String) data.get("name");
            if (name != null && name.equalsIgnoreCase("eventChangeMonedasCajeraBot")) {

                // double distance = viewBot.getRoomUnit().getCurrentLocation()
                // .distance(wsclient.getHabbo().getRoomUnit().getCurrentLocation());
                // if (distance >= 2) {
                // wsclient.getHabbo()
                // .whisper("Debes estar cerca de la cajera del banco para realizar cualquier
                // cambio.");
                // return;
                // }

                int moneda_primary = (int) ((double) data.get("moneda_primary"));
                int moneda_secundary = (int) ((double) data.get("moneda_secundary"));
                int cantidadRecibir = (int) ((double) data.get("action"));

                int factor = 100;
                Habbo habbo = wsclient.getHabbo();
                boolean exito = false;

                // Caso 1: Recibir Puntos (id 5), Pagar Créditos (id -1)
                if (moneda_secundary == 5 && moneda_primary == -1) {
                    int costoPagar = cantidadRecibir * factor;
                    if (habbo.getHabboInfo().getCredits() >= costoPagar) {
                        habbo.giveCredits(-costoPagar);
                        habbo.givePoints(moneda_secundary, cantidadRecibir);
                        exito = true;
                    }
                }
                // Caso 2: Recibir Créditos (id -1), Pagar Puntos (id 5)
                else if (moneda_secundary == -1 && moneda_primary == 5) {
                    int costoPagar = cantidadRecibir / factor;
                    if (habbo.getHabboInfo().getCurrencyAmount(moneda_primary) >= costoPagar) {
                        habbo.givePoints(moneda_primary, -costoPagar);
                        habbo.giveCredits(cantidadRecibir);
                        exito = true;
                    }
                }

                if (exito) {
                    // Actualizar la ventana de Websockets para mostrar nuevos saldos
                    if (viewBot != null) {
                        showVentana(habbo, viewBot);
                    }
                }
            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en IncomingListActions: " + e.getMessage());
        }
    }
}