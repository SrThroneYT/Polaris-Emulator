package com.websockets.plugin.habbo.Tareas.Level;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Level.config.LevelConfig;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;

public class LevelManager {

    private THashMap<Integer, Level> levels = new THashMap<>();

    public LevelManager() throws SQLException {
        new LevelConfig().register();
        this.updateLevels();
    }

    public void updateLevels() throws SQLException {
        this.levels.clear();
        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(
                            "SELECT * FROM websocketsth_settings WHERE `key` LIKE '%level_config.amount_levels%'")) {
                ResultSet set = preparedStatement.executeQuery();
                while (set.next()) {
                    // Recorrera los niveles apartir del nivel 2
                    for (int i = 2; i <= set.getInt("value"); i++) {
                        this.levels.put(i, new Level(i));
                    }
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    public THashMap<Integer, Level> getLevels() {
        return this.levels;
    }

    public JsonArray getArrayLevels() {

        JsonArray Tarray = new JsonArray();
        JsonObject itemObj;

        for (Level level : getLevels().values()) {
            itemObj = new JsonObject();
            itemObj.addProperty("id", level.getLevel());
            itemObj.addProperty("xp", level.getXP());
            Tarray.add(itemObj);
        }

        return Tarray;
    }

    public Level getLevel(int level_id) throws SQLException {
        if (getLevels().get(level_id) != null) {
            return getLevels().get(level_id);
        } else {
            return null;
        }
    }

    public void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            String name = (String) data.get("name");

            if (name.equalsIgnoreCase("OpenMenuLevel")) {

                Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                habbos.getLevelHabbo().showWindow(habbos.getLevelHabbo().getArrayData());

            }

            else if (name.equalsIgnoreCase("OpenMenuLevelOther")) {

                int user_id = (int) ((double) data.get("action"));

                Habbos habbosMy = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                Habbos habbosOther = WebsocketsManager.getHabbosManager().getHabboRpOffline(user_id);

                habbosMy.getHabboClient().send(new MessageManager("MessageManager", true,
                        Outcoming.ViewLevelOtherHabboWindow, habbosOther.getLevelHabbo().getArrayData()));
            }
        } catch (Exception e) {

        }
    }

}