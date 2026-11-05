package com.websockets.plugin.youtube;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.websockets.plugin.WebsocketsManager;

public class VideoYoutube {

    public int id;
    public int pause;
    public int room_id;
    public String url;
    public int time;
    public int state;
    public int active;

    public VideoYoutube(int room_id) {
        this.room_id = room_id;
        this.state = 0;
        this.active = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setPause(Boolean pause) {
        this.pause = pause == true ? 1 : 0;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public int getPause() {
        return pause;
    }

    public int getTime() {
        return time;
    }

    public int getState() {
        return state;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public void saveToDatabase() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE yt_rooms_data SET `url` = ?, `state`= ?, `time`= ?, `active`= ?  WHERE `id` = ? LIMIT 1")) {
            statement.setString(1, this.getUrl());
            statement.setInt(2, this.getState());
            statement.setInt(3, this.getTime());
            statement.setInt(4, this.getActive());
            statement.setInt(5, this.getId());

            statement.executeUpdate();
        } catch (SQLException e) {
        }
    }

}
