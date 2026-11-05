package com.websockets.plugin.youtube;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

public final class YoutubeRoomManager {

    private final ConcurrentHashMap<Integer, RoomYoutube> rooms = new ConcurrentHashMap<>();

    public YoutubeRoomManager() {
        this.reload();
    }

    public void reload() {
        this.rooms.clear();
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT * FROM yt_rooms_data")) {
            if (statement.execute()) {
                try (ResultSet set = statement.getResultSet()) {
                    while (set.next()) {

                        RoomYoutube roomYoutube = new RoomYoutube(set.getInt("room_id"));
                        roomYoutube.setId(set.getInt("id"));
                        this.rooms.put(set.getInt("room_id"), roomYoutube);
                    }
                }
            }
        } catch (SQLException e) {
        }
    }

    public void setRoomYoutube(Room room) {
        if (room != null) {
            this.getYoutubeRooms().put(room.getId(), new RoomYoutube(room.getId()));
        }
    }

    public RoomYoutube getRoomYoutube(Room room) {
        return this.getYoutubeRooms().get(room.getId());
    }

    public ConcurrentHashMap<Integer, RoomYoutube> getYoutubeRooms() {
        return rooms;
    }

    public void showYoutubeVentana(Habbo habbo) {
        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
        Room room = habbo.getHabboInfo().getCurrentRoom();
        try {
            habbos.getHabboClient()
                    .send(new MessageManager("MessageManager", true, Outcoming.showYoutube,
                            this.getArrayRoom(room.getId(), habbo)));
        } catch (Exception e) {
        }
    }

    public void ocultYoutubeVentana(Habbo habbo) {

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
        Room room = habbo.getHabboInfo().getCurrentRoom();

        if (room == null) {
            return;
        }

        RoomYoutube roomYoutube = this.rooms.get(room.getId());
        if (roomYoutube != null) {
            roomYoutube.removeEspectador(habbo);
        }

        try {
            habbos.getHabboClient()
                    .send(new MessageManager("MessageManager", false, Outcoming.showYoutube,
                            null));
        } catch (Exception e) {
        }

        // 1. Recorrer todas las salas para encontrar y eliminar al usuario de cualquier
        // lista de espectadores
        for (RoomYoutube ry : this.rooms.values()) {
            // removeIf devuelve true si encontró y eliminó al elemento
            boolean eliminado = ry.getEspectadores().values().removeIf(espectador -> espectador.equals(habbo));

            if (eliminado) {
                WebsocketsManager.utilities()
                        .LOGGER("Usuario " + habbo.getHabboInfo().getUsername() + " removido de la sala: "
                                + ry.getId());

                // 2. Opcional: Notificar a los demás en esa sala que alguien salió
                for (Habbo restante : ry.getEspectadores().values()) {
                    WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(restante);
                }
            }
        }

        // 3. Lógica para la sala actual (la que se pasa por parámetro)
        RoomYoutube roomYoutubeActual = this.rooms.get(room.getId());
        if (roomYoutubeActual != null) {
            // Notificar a los espectadores de la sala actual si es necesario
            for (Habbo habboEspectador : roomYoutubeActual.getEspectadores().values()) {
                WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(habboEspectador);
            }
        }
    }

    public void sincroniceVideoData(Room room, Habbo habbo) {
        RoomYoutube roomYoutube = this.rooms.get(room.getId());

        if (roomYoutube == null) {
            return;
        }

        for (Habbo habboEspectador : roomYoutube.getEspectadores().values()) {
            if (habboEspectador == habbo) {
                continue;
            }
            // 4. Persistir cambios y notificar
            // Dependiendo de tu RoomYoutube, podrías necesitar un método 'updateVideo'
            // o simplemente refrescar la ventana para todos en la sala.
            WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(habboEspectador);
        }
    }

    public JsonArray getArrayRoom(int roomId, Habbo habbo) {
        JsonArray dataArray = new JsonArray(); // Este es el array principal que retornaremos
        JsonObject rootContent = new JsonObject(); // El objeto que contendrá todo

        JsonArray youtubeData = new JsonArray();
        JsonArray espectadoresData = new JsonArray();

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(roomId);

        if (room == null) {
            rootContent.add("youtubeData", youtubeData);
            rootContent.add("espectadoresData", espectadoresData);
            dataArray.add(rootContent);
            return dataArray;
        }

        RoomYoutube roomYoutube = this.getRoomYoutube(room);

        if (roomYoutube != null) {
            // 1. Lógica de Videos (Corregido para usar values() con forEach estándar)
            roomYoutube.getVideos().values().forEach(video -> {
                JsonObject videoJson = new JsonObject();
                videoJson.addProperty("room_id", roomId);
                videoJson.addProperty("video_id", video.getId());
                videoJson.addProperty("url", video.getUrl());
                videoJson.addProperty("time", video.getTime());
                videoJson.addProperty("state", video.getState());
                videoJson.addProperty("active", video.getActive());

                youtubeData.add(videoJson);
            });

            // 2. Lógica de Espectadores (espectadoresData)
            roomYoutube.getEspectadores().values().forEach(espectador -> {

                String userLook = WebsocketsManager.getConfig().getValue("global.look.ruta").replace("%LOOK%",
                        espectador.getHabboInfo().getLook()) + "&headonly=1&size=m&head_direction=2&gesture=sml";

                JsonObject usuarioJson = new JsonObject();
                usuarioJson.addProperty("user_id", espectador.getHabboInfo().getId());
                usuarioJson.addProperty("user_look", userLook);
                usuarioJson.addProperty("user_name", espectador.getHabboInfo().getUsername());

                espectadoresData.add(usuarioJson);
            });
        }

        // Unificamos todo en el objeto raíz
        rootContent.add("youtubeData", youtubeData);
        rootContent.add("espectadoresData", espectadoresData);

        // Verificación de permisos (dueño de sala)
        int permisos = (room.getOwnerId() == habbo.getHabboInfo().getId()) ? 1 : 0;
        rootContent.addProperty("permisos", permisos);

        // Agregamos el objeto al array final
        dataArray.add(rootContent);

        return dataArray;
    }

}