package com.websockets.plugin.habbo.Tareas.Habbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.websockets.plugin.habbo.Habbos;

public class TopUser {

    private int id;
    private int level;
    private String look;
    private String username;
    private Habbos habbos;
    private int xp;

    public TopUser(Habbos habbos) {
        this.habbos = habbos;
        this.id = habbos.getId();
        this.look = HabboManager.getOfflineHabboInfo(habbos.getId()).getLook();
        this.username = HabboManager.getOfflineHabboInfo(habbos.getId()).getUsername();
        this.level = this.loadFromDatabase("level.level");
        this.xp = this.loadFromDatabase("level.xp");
    }

    public int loadFromDatabase(String string) {
        int value = 0;
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT * FROM websocketsth_users_settings WHERE user_id= ? AND `key`= ? ")) {
            statement.setInt(1, this.getId());
            statement.setString(2, string);
            if (statement.execute()) {
                try (ResultSet set = statement.getResultSet()) {
                    while (set.next()) {
                        value = set.getInt("value");
                        return value;
                    }
                }
            }
        } catch (SQLException e) {
        }
        return value;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public String getLook() {
        return look;
    }

    public String getUsername() {
        return username;
    }

    public Habbos getHabbos() {
        return habbos;
    }

    public int getXp() {
        return xp;
    }

}
