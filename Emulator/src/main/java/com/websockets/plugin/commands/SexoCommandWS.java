package com.websockets.plugin.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class SexoCommandWS extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexoCommandWS.class);

    private static final THashMap<Habbo, Integer> habbosPeticiones = new THashMap<>();
    private static final THashMap<Habbo, Habbo> habbosObjetives = new THashMap<>();

    public SexoCommandWS(String permission, String[] keys) {
        super(permission, keys);
    }

    @Override
    public boolean handle(GameClient gameClient, String[] strings) throws Exception {
        try {
            final Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();

            if (strings.length >= 2) {
                Habbo habbo = room.getHabbo(strings[1]);

                if (habbo == null) {
                    return false;
                }

                if (habbo != gameClient.getHabbo()) {

                    if (habbosPeticiones.get(gameClient.getHabbo()) != null) {
                        habbosPeticiones.remove(gameClient.getHabbo());
                        habbosObjetives.remove(habbo);
                    }

                    habbosPeticiones.put(gameClient.getHabbo(), habbo.getHabboInfo().getId());

                    // Mostramos la burbuja visual (UI)
                    HabboClient wsclient = HabboClientManager.getHabboClient(habbo);

                    String rutaImagesNotifications = WebsocketsManager.getConfig()
                            .getValue("global_url.hotel.notifications") + "/websocketsBySrThrone";

                    this.showConfirmVentana(wsclient,
                            WebsocketsManager.getTexts().getValue(permission + ".peticion_sexo.confirm_title")
                                    .replace("%username%", gameClient.getHabbo().getHabboInfo().getUsername()),
                            WebsocketsManager.getTexts().getValue(permission + ".peticion_sexo.confirm_desc"),
                            rutaImagesNotifications + "/confirm/sex.png",
                            60000, "eventSexoCommandConfirm");
                }

            } else {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue(permission + ".error.no_user"));
            }

        } catch (Exception e) {
            LOGGER.error("Error while executing command", e);
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
            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showConfirmVentana,
                    arrayEventos));
        } catch (Exception e) {
        }

    }

    /**
     * Maneja peticiones directas desde el WebSocket
     */
    public static void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient == null || wsclient.getHabbo() == null)
                return;

            String name = (String) data.get("name");

            if (name != null && name.equalsIgnoreCase("eventSexoCommandConfirm")) {
                Habbo habboObjetivo = wsclient.getHabbo();
                Habbo habboRemitente = null;

                // Buscamos quién envió la petición
                for (java.util.Map.Entry<Habbo, Integer> entry : habbosPeticiones.entrySet()) {
                    if (entry.getValue() == habboObjetivo.getHabboInfo().getId()) {
                        habboRemitente = entry.getKey();
                        break;
                    }
                }

                if (habboRemitente != null) {
                    // Iniciamos la lógica separada
                    startSexRole(habboRemitente, habboObjetivo);
                    habbosPeticiones.remove(habboRemitente);
                }
            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en IncomingListActions: " + e.getMessage());
        }
    }

    /**
     * Lógica del rol segundo a segundo con textos aleatorios
     */
    private static void startSexRole(Habbo remitente, Habbo objetivo) {
        if (remitente.getHabboInfo().getCurrentRoom() != objetivo.getHabboInfo().getCurrentRoom())
            return;

        // Bloqueo de seguridad
        remitente.getRoomUnit().setCanWalk(false);
        objetivo.getRoomUnit().setCanWalk(false);

        // T = 0s
        sayRandom(remitente, objetivo, new String[] {
                "¡Acércate %u, te voy a dar lo tuyo!",
                "¡No sabes cuánto tiempo he deseado esto, %u!",
                "¡Prepárate %u, porque no te vas a olvidar de esto!"
        });

        // T = 1s
        runIn(1000, () -> sayRandom(objetivo, remitente, new String[] {
                "¡Ay sí... mírame a los ojos!",
                "¡Hazme lo que quieras, %u!",
                "¡Me encanta cuando te pones así de dominante!"
        }));

        // T = 2s
        runIn(2000, () -> sayRandom(remitente, objetivo, new String[] {
                "*Comienza a quitarle la ropa lentamente*",
                "*Te besa apasionadamente por todo el cuello*",
                "*Te acaricia con mucha intensidad mientras te mira*"
        }));

        // T = 3s
        runIn(3000, () -> sayRandom(objetivo, remitente, new String[] {
                "¡Mmm... qué bien se siente eso!",
                "¡Ohhh... continúa así, no pares!",
                "¡Tus manos me vuelven loca/o, %u!"
        }));

        // T = 4s (Clímax - Grito)
        runIn(4000, () -> shoutRandom(remitente, objetivo, new String[] {
                "¡ESTÁS QUE ARDES, %u!",
                "¡UFF, ESTO ESTÁ INCREÍBLE!",
                "¡DAME MÁS, NO PARES AHORA!"
        }));

        // T = 5s (Clímax - Grito)
        runIn(5000, () -> shoutRandom(objetivo, remitente, new String[] {
                "¡ESTOY LLEGANDO AL LÍMITE...!",
                "¡DÁMELO TODO, %u!",
                "¡OH SÍ, ASÍ, JUSTO AHÍ!"
        }));

        // T = 6s
        runIn(6000, () -> sayRandom(remitente, objetivo, new String[] {
                "¡Eso fue... de otro mundo!",
                "Uff... me dejaste sin aliento.",
                "Creo que rompimos la cama..."
        }));

        // T = 7s - Finalización
        runIn(7000, () -> {
            if (check(remitente, objetivo)) {
                objetivo.talk("Necesito una ducha fría urgentemente...");
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