package com.websockets.plugin.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TocarCommandWS extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(TocarCommandWS.class);
    private static THashMap<Habbo, Integer> habbosPeticiones = new THashMap<>();

    public TocarCommandWS(String permission, String[] keys) {
        super(permission, keys);
    }

    @Override
    public boolean handle(GameClient gameClient, String[] strings) throws Exception {
        try {
            final Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();

            if (strings.length >= 2) {
                Habbo habbo = room.getHabbo(strings[1]);

                if (habbo == null || habbo == gameClient.getHabbo()) {
                    return false;
                }

                habbosPeticiones.put(gameClient.getHabbo(), habbo.getHabboInfo().getId());

                HabboClient wsclient = HabboClientManager.getHabboClient(habbo);
                String rutaImages = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications")
                        + "/websocketsBySrThrone";

                this.showConfirmVentana(wsclient,
                        "Petición de contacto",
                        gameClient.getHabbo().getHabboInfo().getUsername()
                                + " quiere tocarte de forma atrevida... ¿Aceptas?",
                        rutaImages + "/confirm/tocar.png",
                        60000, "eventTocarCommandConfirm");

            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue(permission + ".error.no_user"));
            }
        } catch (Exception e) {
            LOGGER.error("Error en TocarCommandWS", e);
        }
        return true;
    }

    public void showConfirmVentana(HabboClient wsclient, String title, String desc, String img, int time,
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
            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showConfirmVentana, arrayEventos));
        } catch (Exception ignored) {
        }
    }

    public static void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            String name = (String) data.get("name");

            if (name.equalsIgnoreCase("eventTocarCommandConfirm")) {

                Habbo objetivo = wsclient.getHabbo();
                Habbo remitente = null;

                for (java.util.Map.Entry<Habbo, Integer> entry : habbosPeticiones.entrySet()) {
                    if (entry.getValue() == objetivo.getHabboInfo().getId()) {
                        remitente = entry.getKey();
                        break;
                    }
                }

                if (remitente != null) {
                    startTouchRole(remitente, objetivo);
                    habbosPeticiones.remove(remitente);
                }
            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en Tocar: " + e.getMessage());
        }
    }

    private static void startTouchRole(Habbo remitente, Habbo objetivo) {
        if (!check(remitente, objetivo))
            return;

        remitente.getRoomUnit().setCanWalk(false);
        objetivo.getRoomUnit().setCanWalk(false);

        // T = 0s: El inicio (Remitente)
        sayRandom(remitente, objetivo, new String[] {
                "*Pasa sus manos lentamente por tu cintura, %u*",
                "*Te atrae hacia sí mientras acaricia tu espalda, %u*",
                "*Recorre tus hombros con sus dedos de forma juguetona, %u*",
                "*Se pega a ti y comienza a acariciar tus caderas suavemente*"
        });

        // T = 1s: Reacción (Objetivo)
        runIn(1000, () -> sayRandom(objetivo, remitente, new String[] {
                "¡Mmm... tus manos me ponen los pelos de punta, %u!",
                "¡Ahhh... no te detengas, sigue tocándome así!",
                "¡Siento cómo me queman tus manos por todo el cuerpo, %u!",
                "¡Me encanta que me toques con esa seguridad, %u...!"
        }));

        // T = 2s: Intensidad (Remitente)
        runIn(2000, () -> sayRandom(remitente, objetivo, new String[] {
                "*Baja sus manos con mucha picardía hacia tus muslos*",
                "*Te presiona con fuerza contra su cuerpo mientras te toca*",
                "*Acaricia cada curva de tu cuerpo con un deseo evidente*",
                "*Susurra a tu oído mientras sus manos no dejan de recorrerte*"
        }));

        // T = 3s: Clímax de la acción (Objetivo)
        runIn(3000, () -> shoutRandom(objetivo, remitente, new String[] {
                "¡UFF, %u! ¡Me vas a volver loco/a si sigues!",
                "¡Oh sí... justo ahí... me encanta!",
                "¡No sabía que tus manos podían hacerme sentir así!",
                "¡DAME MÁS, %u! ¡Tócame por todos lados!"
        }));

        // T = 4s: Cierre y liberación
        runIn(4000, () -> {
            if (check(remitente, objetivo)) {
                sayRandom(remitente, objetivo, new String[] {
                        "*Te da un último apretón juguetón antes de soltarte con picardía*",
                        "*Te mira con deseo y te suelta lentamente dejándote con ganas*",
                        "*Finaliza el contacto con una sonrisa traviesa mientras se aleja*"
                });
                remitente.getRoomUnit().setCanWalk(true);
                objetivo.getRoomUnit().setCanWalk(true);
            }
        });
    }

    private static void sayRandom(Habbo actor, Habbo target, String[] msgs) {
        if (check(actor, target)) {
            actor.talk(
                    msgs[Emulator.getRandom().nextInt(msgs.length)].replace("%u", target.getHabboInfo().getUsername()));
        }
    }

    private static void shoutRandom(Habbo actor, Habbo target, String[] msgs) {
        if (check(actor, target)) {
            actor.shout(
                    msgs[Emulator.getRandom().nextInt(msgs.length)].replace("%u", target.getHabboInfo().getUsername()));
        }
    }

    private static void runIn(int ms, Runnable action) {
        Emulator.getThreading().run(action, ms);
    }

    private static boolean check(Habbo r, Habbo o) {
        return r != null && o != null && r.getHabboInfo().getCurrentRoom() != null &&
                o.getHabboInfo().getCurrentRoom() != null &&
                r.getHabboInfo().getCurrentRoom() == o.getHabboInfo().getCurrentRoom();
    }
}