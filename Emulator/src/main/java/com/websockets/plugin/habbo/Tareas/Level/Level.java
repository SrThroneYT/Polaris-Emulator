package com.websockets.plugin.habbo.Tareas.Level;

import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.websockets.plugin.WebsocketsManager;

import gnu.trove.map.hash.THashMap;

public class Level {

    private int level;
    private int XP;
    THashMap<Integer, Reward> rewards = new THashMap<Integer, Reward>();
    THashMap<Integer, Reward> rewardsVip = new THashMap<Integer, Reward>();

    public Level(int level_id) throws SQLException {
        this.level = level_id;
        this.XP = WebsocketsManager.getConfig().getInt("level_" + level_id + ".xp_required");

        JsonParser parser = new JsonParser();
        JsonArray gsonArr = parser
                .parse(WebsocketsManager.getConfig().getValue("level_" + level_id + ".premios_json_array"))
                .getAsJsonArray();
        int increment = 0;
        for (JsonElement obj : gsonArr) {
            increment++;
            // Object of array
            JsonObject gsonObj = obj.getAsJsonObject();
            // Primitives elements of object
            String nombre = gsonObj.get("nombre").getAsString();
            int item_exist = gsonObj.get("item_exist").getAsInt();
            int cantidad = gsonObj.get("cantidad").getAsInt();
            String title = gsonObj.get("title").getAsString();
            String descripcion = gsonObj.get("descripcion").getAsString();
            // Insertar
            this.rewards.put(increment, new Reward(nombre, item_exist, cantidad, title, descripcion));
        }

        JsonArray gsonArr2 = parser
                .parse(WebsocketsManager.getConfig()
                        .getValue("level_" + level_id + "_vip.premios_json_array"))
                .getAsJsonArray();
        int increment2 = 0;
        for (JsonElement obj : gsonArr2) {
            increment2++;
            // Object of array
            JsonObject gsonObj = obj.getAsJsonObject();
            // Primitives elements of object
            String nombre = gsonObj.get("nombre").getAsString();
            int item_exist = gsonObj.get("item_exist").getAsInt();
            int cantidad = gsonObj.get("cantidad").getAsInt();
            String title = gsonObj.get("title").getAsString();
            String descripcion = gsonObj.get("descripcion").getAsString();
            // Insertar
            this.rewardsVip.put(increment2, new Reward(nombre, item_exist, cantidad, title, descripcion));
        }
    }

    public int getLevel() {
        return this.level;
    }

    public int getXP() {
        return this.XP;
    }

    public THashMap<Integer, Reward> getRewards() {
        return rewards;
    }

    public THashMap<Integer, Reward> getRewardsVip() {
        return rewardsVip;
    }

}
