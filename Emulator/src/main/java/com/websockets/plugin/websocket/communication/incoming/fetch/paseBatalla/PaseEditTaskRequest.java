package com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla;

import java.sql.SQLException;
import java.util.Map;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Tarea;
import com.websockets.plugin.habbo.Tareas.TareaLevel;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

public class PaseEditTaskRequest extends IncomingMessage {

    public static final String REQUEST_HEADER = "PaseEditTaskRequest";

    public PaseEditTaskRequest(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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
            // Extraemos los campos principales enviados
            int level = ((Number) this.data.get("level")).intValue();
            String name = (String) this.data.get("name");

            System.out.println("[PaseEditTaskRequest] Procesando actualización directa para Nivel: " + level
                    + " | Tarea: " + name);

            // Buscamos la tarea principal
            Tarea tarea = WebsocketsManager.getTareasManager().getTarea(name);

            if (tarea == null) {
                System.out.println("[PaseEditTaskRequest] No se encontró la tarea con NAME: " + name);
                return;
            }

            // Accedemos directamente al nivel específico sin iterar listas
            TareaLevel tareaLevel = tarea.levels.get(level);

            if (tareaLevel != null) {
                // Actualizamos campos basados en el objeto recibido directamente en data
                if (this.data.containsKey("imagen") && this.data.get("imagen") != null) {
                    String subLevelImagen = ((String) this.data.get("imagen")).trim();

                    // Lógica de limpieza de ruta
                    String imagenOriginal = tareaLevel.getImagen();
                    if (imagenOriginal != null && !imagenOriginal.startsWith("http://")
                            && !imagenOriginal.startsWith("https://")) {
                        if (subLevelImagen.contains("/tareas/")) {
                            subLevelImagen = subLevelImagen.substring(subLevelImagen.indexOf("/tareas/"));
                        }
                    }
                    tareaLevel.setImagen(subLevelImagen);
                }

                if (this.data.containsKey("title") && this.data.get("title") != null) {
                    tareaLevel.setTitle((String) this.data.get("title"));
                }

                if (this.data.containsKey("descripcion") && this.data.get("descripcion") != null) {
                    tareaLevel.setDescripcion((String) this.data.get("descripcion"));
                }

                if (this.data.containsKey("xp") && this.data.get("xp") != null) {
                    tareaLevel.setXp(((Number) this.data.get("xp")).intValue());
                }

                if (this.data.containsKey("orden") && this.data.get("orden") != null) {
                    tareaLevel.setOrden(((Number) this.data.get("orden")).intValue());
                }

                // --- CAMPOS NUEVOS AÑADIDOS DEL JSON ---
                if (this.data.containsKey("reward_amount") && this.data.get("reward_amount") != null) {
                    tareaLevel.setRewardAmount(((Number) this.data.get("reward_amount")).intValue());
                }

                if (this.data.containsKey("reward_type") && this.data.get("reward_type") != null) {
                    tareaLevel.setRewardType(((Number) this.data.get("reward_type")).intValue());
                }

                if (this.data.containsKey("distintos_items") && this.data.get("distintos_items") != null) {
                    tareaLevel.setDistintosItems(((Number) this.data.get("distintos_items")).intValue());
                }

                if (this.data.containsKey("distintos_rooms") && this.data.get("distintos_rooms") != null) {
                    tareaLevel.setDistintosRooms(((Number) this.data.get("distintos_rooms")).intValue());
                }

                if (this.data.containsKey("button_visit_room") && this.data.get("button_visit_room") != null) {
                    tareaLevel.setButtonVisitRoom(((Number) this.data.get("button_visit_room")).intValue());
                }

                if (this.data.containsKey("required") && this.data.get("required") != null) {
                    tareaLevel.setProgress(((Number) this.data.get("required")).intValue());
                }
                // ----------------------------------------

                // Guardado en BD
                tareaLevel.update();
                System.out.println("[PaseEditTaskRequest] Nivel " + level + " actualizado correctamente.");
            } else {
                System.out.println("[PaseEditTaskRequest] No se encontró el TareaLevel para el índice: " + level);
            }

            // Recargar caché y refrescar UI
            WebsocketsManager.getTareasManager().update();

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
            if (habbos != null && habbos.getTareas() != null) {
                habbos.getTareas().showTareasWindow();
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[PaseEditTaskRequest] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}