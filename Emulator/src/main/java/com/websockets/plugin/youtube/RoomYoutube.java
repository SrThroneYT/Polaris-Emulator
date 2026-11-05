package com.websockets.plugin.youtube;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.websockets.plugin.WebsocketsManager;

import gnu.trove.map.hash.THashMap;

public class RoomYoutube {

    public int id;
    public int room_id;

    private THashMap<Integer, VideoYoutube> videos = new THashMap<Integer, VideoYoutube>();
    private THashMap<Integer, Habbo> espectadores = new THashMap<Integer, Habbo>();

    public RoomYoutube(int room_id) {
        this.room_id = room_id;
        this.reload();
    }

    public void reload() {
        this.videos.clear();
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT * FROM yt_rooms_data WHERE room_id = ?")) {
            statement.setInt(1, this.room_id);
            if (statement.execute()) {
                try (ResultSet set = statement.getResultSet()) {
                    while (set.next()) {
                        VideoYoutube videoYoutube = new VideoYoutube(set.getInt("id"));
                        videoYoutube.setId(set.getInt("id"));
                        videoYoutube.setUrl(set.getString("url"));
                        videoYoutube.setTime(set.getInt("time"));
                        videoYoutube.setState(set.getInt("state"));
                        videoYoutube.setActive(set.getInt("active"));
                        this.videos.put(set.getInt("id"), videoYoutube);
                    }
                }
            }
        } catch (SQLException e) {
        }
    }

    public void removeEspectador(Habbo habbo) {
        this.espectadores.remove(habbo.getHabboInfo().getId());
    }

    public void registerEspectador(Habbo habbo) {
        if (this.espectadores.get(habbo.getHabboInfo().getId()) == null) {

            this.espectadores.put(habbo.getHabboInfo().getId(), habbo);
            for (Habbo habboEspectador : this.getEspectadores().values()) {
                if (habboEspectador == habbo) {
                    continue;
                }
                // 4. Persistir cambios y notificar
                // Dependiendo de tu RoomYoutube, podrías necesitar un método 'updateVideo'
                // o simplemente refrescar la ventana para todos en la sala.
                WebsocketsManager.getYoutubeRoomManager().showYoutubeVentana(habboEspectador);
            }

            
        }
    }

    public THashMap<Integer, Habbo> getEspectadores() {
        return this.espectadores;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void insertVideo(VideoYoutube video) {
        String query = "INSERT INTO yt_rooms_data (room_id, url, time, state, active) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE url = VALUES(url), time = VALUES(time), state = VALUES(state), active = VALUES(active)";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, this.room_id);
            statement.setString(2, video.getUrl());
            statement.setInt(3, video.getTime());
            statement.setInt(4, video.getState());
            statement.setInt(5, video.getActive());

            int affectedRows = statement.executeUpdate();

            // Si es una inserción nueva, obtenemos el ID generado por la DB
            if (video.getId() <= 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        video.setId(generatedKeys.getInt(1));
                    }
                }
            }

            // Actualizamos el mapa local de esta instancia de RoomYoutube
            this.videos.put(video.getId(), video);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public THashMap<Integer, VideoYoutube> getVideos() {
        return videos;
    }

}
