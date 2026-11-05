package com.websockets.plugin.habbo.BarraTop.Eventos;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.BarraTop.MejoresLook.LookHabboObjet;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;

public class EventosManager {

    private static EventosManager instance;

    private final THashMap<Integer, EventoObjet> eventos = new THashMap<>();

    public EventosManager() {
        // 1. Verificar/Crear tabla al iniciar
        this.checkDatabase();

        // 2. Cargar eventos existentes al iniciar el plugin
        this.updateEvents();
        
        // 3. Iniciar el Timer (Scheduler)
        TimerEventos timerEventos = new TimerEventos(this);
        Emulator.getThreading().run(timerEventos);
    }

    public void updateEvents() {
        String query = "SELECT * FROM cms_events ORDER BY id ASC";
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet set = statement.executeQuery()) {
            while (set.next()) {
                int id = set.getInt("id");
                this.eventos.put(id, new EventoObjet(set));
            }
        } catch (SQLException e) {
            Emulator.getLogging().logPacketError(e);
        }
    }

    public THashMap<Integer, EventoObjet> getEventos() {
        return eventos;
    }

    // Método Singleton para acceso global
    public static EventosManager getInstance() {
        if (instance == null) {
            instance = new EventosManager();
        }
        return instance;
    }

    /**
     * Crea un nuevo evento desde el Frontend (React)
     */
    public void crearNuevoEvento(String title, String texto, String image, long timestamp, Habbo habbo) {
        // Usamos 'cms_events' y el campo 'hora' para el timestamp para mantener
        // consistencia
        try (Connection cn = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement pstmt = cn.prepareStatement(
                        "INSERT INTO cms_events (title, texto, image, hora, user, room_id) VALUES (?, ?, ?, ?, ?, ?)")) {

            pstmt.setString(1, title);
            pstmt.setString(2, texto);
            pstmt.setString(3, image);
            pstmt.setLong(4, timestamp);
            pstmt.setString(5, habbo.getHabboInfo().getId() + ""); // O el nombre del staff si lo envías desde React
            pstmt.setInt(6, habbo.getHabboInfo().getCurrentRoom().getId()); // ID de la habitación donde se crea el
                                                                            // evento
            pstmt.executeUpdate();

            WebsocketsManager.utilities().LOGGER("Nuevo evento guardado: " + title);

        } catch (SQLException e) {
            WebsocketsManager.utilities().LOGGER("Error al crearNuevoEvento: " + e.getMessage());
        }
    }

    /**
     * Actualiza un evento existente en la base de datos
     */
    public void actualizarEvento(int id, String title, String texto, String image, long timestamp) {
        try (Connection cn = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement pstmt = cn.prepareStatement(
                        "UPDATE cms_events SET title = ?, texto = ?, image = ?, hora = ? WHERE id = ?")) {

            pstmt.setString(1, title);
            pstmt.setString(2, texto);
            pstmt.setString(3, image);
            pstmt.setLong(4, timestamp);
            pstmt.setInt(5, id); // El ID que identifica qué evento editar

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                WebsocketsManager.utilities()
                        .LOGGER("Evento actualizado correctamente ID: " + id + " - Título: " + title);
            } else {
                WebsocketsManager.utilities().LOGGER("No se encontró el evento con ID: " + id + " para actualizar.");
            }

        } catch (SQLException e) {
            WebsocketsManager.utilities().LOGGER("Error al actualizarEvento: " + e.getMessage());
        }
    }

    /**
     * Verifica la base de datos. He ajustado los campos para que coincidan
     * con la lógica de tu proyecto.
     */
    private void checkDatabase() {
        String query = "CREATE TABLE IF NOT EXISTS `cms_events` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `title` varchar(255) DEFAULT NULL," +
                "  `type` varchar(255) DEFAULT 'event'," +
                "  `hora` bigint(20) NOT NULL," + // Cambiado a bigint para timestamps largos
                "  `user` varchar(255) NOT NULL DEFAULT ''," +
                "  `texto` varchar(255) DEFAULT NULL," +
                "  `image` varchar(255) DEFAULT NULL," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(query);
        } catch (SQLException e) {
            WebsocketsManager.utilities().LOGGER("Error al verificar base de datos cms_events: " + e.getMessage());
        }
    }

    public JsonArray getProximosEventos(Habbo habbo) {
        JsonArray arrayEventos = new JsonArray();

        // 1. Definimos el arreglo de imágenes por defecto
        String[] defaultImagesList = {
                "https://i.imgur.com/9uIlA1j.png",
                "https://i.imgur.com/p7Alp79.png",
                "https://i.imgur.com/GzqaPil.png",
                "https://i.imgur.com/GtfgHBm.png",
                "https://i.imgur.com/J3Ch8Ds.png",
                "https://i.imgur.com/fOyer7R.png",
                "https://i.imgur.com/xCQtKNt.png",
                "https://i.imgur.com/O5OztxF.png",
                "https://i.imgur.com/BPRx2aL.png",
                "https://i.imgur.com/Vp1aiM1.png",
                "https://i.imgur.com/9KkqOKJ.png",
                "https://i.imgur.com/mXfqru3.png",
                "https://i.imgur.com/iIngsZ0.png",
                "https://i.imgur.com/hag5MC8.png",
                "https://i.imgur.com/FZH1ma4.png",
                "https://i.imgur.com/n3BgVvy.png",
                "https://i.imgur.com/2oN7ONS.png",
                "https://i.imgur.com/TIcm5ks.png",
                "https://i.imgur.com/Bjdd0Td.png"
        };

        JsonArray defaultImagesJson = new JsonArray();
        for (String img : defaultImagesList) {
            defaultImagesJson.add(img);
        }

        int userRank = (habbo != null) ? habbo.getHabboInfo().getRank().getId() : 0;
        int minRank = 7;
        try {
            minRank = Integer.parseInt(WebsocketsManager.getConfig().getValue("min_rank_edit_eventos", "7"));
        } catch (Exception e) {
        }

        long filterTime = (System.currentTimeMillis() / 1000L) - 600;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM cms_events WHERE hora >= ? ORDER BY hora ASC LIMIT 10")) {
            statement.setLong(1, filterTime);
            try (ResultSet set = statement.executeQuery()) {
                JsonObject headerData = new JsonObject();
                headerData.addProperty("user_rank", userRank);
                headerData.addProperty("min_rank", minRank);

                if (habbo != null) {
                    Room room = habbo.getHabboInfo().getCurrentRoom();

                    if (room != null) {
                        headerData.addProperty("room_name", room.getName());
                        headerData.addProperty("room_id", room.getId());
                    }

                }

                // 2. Agregamos el arreglo de imágenes a las props que se envían al front
                headerData.add("default_images", defaultImagesJson);

                JsonArray listaView = new JsonArray();

                while (set.next()) {
                    JsonObject eventJson = new JsonObject();

                    String dbImage = set.getString("image");
                    String texto = set.getString("texto");

                    eventJson.addProperty("id", set.getInt("id"));
                    eventJson.addProperty("title", set.getString("title"));

                    eventJson.addProperty("id", set.getInt("id"));
                    eventJson.addProperty("title", set.getString("title"));

                    Emulator.getGameEnvironment().getRoomManager().loadRoom(set.getInt("room_id"));
                    Room roomEvent = Emulator.getGameEnvironment().getRoomManager().getRoom(set.getInt("room_id"));
                    if (roomEvent != null) {
                        eventJson.addProperty("event_room_name", roomEvent.getName());
                        eventJson.addProperty("event_room_id", roomEvent.getId());
                    }

                    HabboInfo habboOfflineInfoEvent = HabboManager.getOfflineHabboInfo(set.getInt("user"));
                    if (habboOfflineInfoEvent != null) {

                        String ownerLook = WebsocketsManager.getConfig().getValue("global.look.ruta").replace("%LOOK%",
                                habboOfflineInfoEvent.getLook())
                                + "&headonly=1&size=a&gesture=sml&head_direction=2&action=std";

                        eventJson.addProperty("event_owner_look", ownerLook);
                        eventJson.addProperty("event_owner_username", habboOfflineInfoEvent.getUsername());

                    }

                    // Si no hay imagen en DB, podemos mandar null o vacío
                    // para que el Front decida cuál del arreglo usar
                    eventJson.addProperty("image", (dbImage == null || dbImage.isEmpty()) ? "" : dbImage);

                    eventJson.addProperty("texto",
                            (texto == null || texto.equalsIgnoreCase("<MEMO>")) ? "Sin descripción" : texto);
                    eventJson.addProperty("timestamp", set.getLong("hora"));

                    listaView.add(eventJson);
                }

                headerData.add("view", listaView);
                arrayEventos.add(headerData);
            }
        } catch (SQLException e) {
            WebsocketsManager.utilities().LOGGER("Error en getProximosEventos: " + e.getMessage());
        }

        return arrayEventos;
    }

    /**
     * Maneja peticiones directas desde el WebSocket
     */
    public void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient == null)
                return;

            String name = (String) data.get("name");
            if (name != null && name.equalsIgnoreCase("showMenuEventos")) {

                wsclient.send(new MessageManager("MessageManager", true, Outcoming.showMenuEventos,
                        this.getProximosEventos(wsclient.getHabbo())));

            }
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en IncomingListActions: " + e.getMessage());
        }
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
        eventJson.addProperty("imgAlt", "Eventos");
        arrayEventos.add(eventJson);

        try {
            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showBubbleEventos,
                    arrayEventos));
        } catch (Exception e) {
        }
    }

    // El método getDatabaseInfo se mantiene igual para compatibilidad con otros
    // módulos
    private TreeMap<Integer, HabboInfo> getDatabaseInfo(String table, String orderBy, String columnId, String order) {
        TreeMap<Integer, HabboInfo> habbosMap = new TreeMap<>();
        String query = "SELECT " + columnId + " FROM `" + table + "` ORDER BY `" + orderBy + "` " + order + " LIMIT 10";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet set = statement.executeQuery()) {

            while (set.next()) {
                HabboInfo info = HabboManager.getOfflineHabboInfo(set.getInt(columnId));
                if (info != null) {
                    habbosMap.put(info.getId(), info);
                }
            }
        } catch (SQLException e) {
            WebsocketsManager.utilities().LOGGER("Error en getDatabaseInfo: " + e);
        }
        return habbosMap;
    }
}