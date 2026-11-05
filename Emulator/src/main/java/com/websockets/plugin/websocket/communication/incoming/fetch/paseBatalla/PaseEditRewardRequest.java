package com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Level.Level;
import com.websockets.plugin.habbo.Tareas.Level.Reward;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

public class PaseEditRewardRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "PaseEditRewardRequest";

    public PaseEditRewardRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    // Método auxiliar para parsear de forma segura objetos a enteros
    private int parseIntSafe(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            // Si viene como String, lo parseamos a double primero por si tiene punto
            // decimal, y luego a entero
            return (int) Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        try {
            WebsocketsManager.utilities().LOGGER("LLEGA ACA x0");

            // Extraemos y sanitizamos todos los campos correspondientes a Reward usando
            // parseIntSafe
            int levelId = parseIntSafe(this.data.get("levelId"));

            WebsocketsManager.utilities().LOGGER("LEVELID: " + levelId);

            String type = (String) this.data.get("type"); // "normal" o "vip"
            String name = (String) this.data.get("name");
            int itemExist = parseIntSafe(this.data.get("item_exist"));
            String title = (String) this.data.get("title");
            String imagen = (String) this.data.get("imagen");
            int cantidad = parseIntSafe(this.data.get("cantidad"));

            WebsocketsManager.utilities().LOGGER("LLEGA ACA x1");

            // Buscar el nivel correspondiente en la memoria activa
            Level level = WebsocketsManager.getLevelManager().getLevels().get(levelId);

            if (level != null) {

                WebsocketsManager.utilities().LOGGER("LLEGA ACA x2");

                THashMap<Integer, Reward> targetRewards;
                String configKey;

                // Determinar si editamos la lista Normal o VIP
                if ("vip".equalsIgnoreCase(type)) {
                    targetRewards = level.getRewardsVip();
                    configKey = "level_" + levelId + "_vip.premios_json_array";
                } else {
                    targetRewards = level.getRewards();
                    configKey = "level_" + levelId + ".premios_json_array";
                }

                if (targetRewards != null) {
                    // 1. Actualizamos el mapa en la memoria activa del emulador (clave 1 por ser el
                    // primero)
                    Reward reward = targetRewards.get(1);
                    if (reward != null) {
                        reward.setNombre(name);
                        reward.setItemExist(itemExist);
                        reward.setCantidad(cantidad);
                        reward.setTitle(title);
                        reward.setDescripcion(title);
                    } else {
                        // Si no existía en el mapa, lo instanciamos
                        targetRewards.put(1, new Reward(name, itemExist, cantidad, title, imagen));
                    }

                    // 2. Reconstruir el JSON Array para guardarlo de forma persistente
                    JsonArray updatedJsonArray = new JsonArray();
                    for (Reward r : targetRewards.values()) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("nombre", r.getNombre());
                        obj.addProperty("item_exist", r.getItemExist());
                        obj.addProperty("cantidad", r.getCantidad());
                        obj.addProperty("title", r.getTitle());
                        obj.addProperty("descripcion", r.getDescripcion()); // URL de la imagen
                        updatedJsonArray.add(obj);
                    }

                    // Guardar de vuelta en la base de datos o archivo de configuración
                    WebsocketsManager.getConfig().updateData(configKey, updatedJsonArray.toString());

                    System.out.println("[PaseTareas] Reward tipo " + type.toUpperCase() + " del Nivel " + levelId
                            + " actualizado correctamente.");
                }
            }

            // Actualizar todos los niveles en caché global
            WebsocketsManager.getLevelManager().updateLevels();

            // Refrescar la vista del usuario para ver los cambios reflejados de inmediato
            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
            if (habbos != null && habbos.getTareas() != null) {
                habbos.getTareas().showTareasWindow();
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[PaseEditRewardRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}