package com.websockets.plugin.habbo.Tareas;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gnu.trove.map.hash.THashMap;

public class TareaLevel {

    public int level;
    public int rewardAmount;
    public int rewardType;
    public int progress;

    public String name;
    public String title;
    public String descripcion;
    public int xp;

    private int id;
    private int id_type;
    private String imagen;

    private String type;

    private String event;
    private int orden;
    private String customparams;
    private int distintos_items;
    private int distintos_rooms;
    private int room_id;
    private int button_visit_room;
    private THashMap<String, Item> items = new THashMap<String, Item>();

    public TareaLevel(ResultSet set) throws SQLException {
        this.level = set.getInt("level");

        this.rewardAmount = set.getInt("reward_amount");
        this.rewardType = set.getInt("reward_type");

        this.progress = set.getInt("usage_account");

        this.name = set.getString("name");
        this.title = set.getString("title");
        this.descripcion = set.getString("descripcion");
        this.xp = set.getInt("xp");

        this.id = set.getInt("id");
        this.id_type = set.getInt("id_type");
        this.imagen = set.getString("imagen");

        this.type = set.getString("type");
        this.event = set.getString("event");
        this.orden = set.getInt("orden");
        this.customparams = set.getString("customparams");
        this.distintos_items = set.getInt("distintos_items");
        this.distintos_rooms = set.getInt("distintos_rooms");
        this.room_id = set.getInt("room_id");
        this.button_visit_room = set.getInt("button_visit_room");

        if (this.getType().equalsIgnoreCase("onclick")) {

            if (this.getCustomParams() == null || this.getCustomParams().isEmpty()
                    || this.getCustomParams().equals("[]")) {
                return;
            }

            JsonParser parser = new JsonParser();
            JsonArray gsonArr = parser
                    .parse(this.getCustomParams())
                    .getAsJsonArray();
            for (JsonElement obj : gsonArr) {
                // Object of array
                JsonObject gsonObj = obj.getAsJsonObject();
                // Primitives elements of object
                String nombre = gsonObj.get("item_name").getAsString();
                Item item = Emulator.getGameEnvironment().getItemManager().getItem(nombre);
                if (item != null) {
                    this.items.put(nombre, item);
                }
            }
        }
    }

    public void update() {
        try (java.sql.Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                java.sql.PreparedStatement statement = connection.prepareStatement(
                        "UPDATE `websocketsth_pase_tareas` SET " +
                                "`id_type` = ?, `title` = ?, `descripcion` = ?, `imagen` = ?, `name` = ?, " +
                                "`type` = ?, `usage_account` = ?, `event` = ?, `orden` = ?, `customparams` = ?, " +
                                "`distintos_items` = ?, `distintos_rooms` = ?, `room_id` = ?, `button_visit_room` = ?, "
                                +
                                "`reward_amount` = ?, `reward_type` = ?, `xp` = ? WHERE `id` = ?")) {

            statement.setInt(1, this.id_type);
            statement.setString(2, this.title);
            statement.setString(3, this.descripcion);
            statement.setString(4, this.imagen);
            statement.setString(5, this.name);
            statement.setString(6, this.type);
            statement.setInt(7, this.progress);
            statement.setString(8, this.event);
            statement.setInt(9, this.orden);
            statement.setString(10, this.customparams);
            statement.setInt(11, this.distintos_items);
            statement.setInt(12, this.distintos_rooms);
            statement.setInt(13, this.room_id);
            statement.setInt(14, this.button_visit_room);
            statement.setInt(15, this.rewardAmount);
            statement.setInt(16, this.rewardType);
            statement.setInt(17, this.xp);
            statement.setInt(18, this.id);

            statement.executeUpdate();

        } catch (SQLException e) {
            Emulator.getLogging().logSQLException(e);
        }
    }

    // --- GETTERS ---[cite: 5]

    public int getId() {
        return id;
    }

    public int getTypeId() {
        return id_type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public int getOrden() {
        return orden;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getEventName() {
        return event;
    }

    public String getCustomParams() {
        return customparams;
    }

    public THashMap<String, Item> getItems() {
        return items;
    }

    public boolean getDistintosItems() {
        return this.distintos_items == 1 ? true : false;
    }

    public boolean getDistintosRooms() {
        return this.distintos_rooms == 1 ? true : false;
    }

    public int getRoomId() {
        return this.room_id;
    }

    public int getButtonVisitRoom() {
        return this.button_visit_room;
    }

    public int getXp() {
        return this.xp;
    }

    // --- SETTERS ---

    public void setId(int id) {
        this.id = id;
    }

    public void setIdType(int id_type) {
        this.id_type = id_type;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setRewardAmount(int reward_amount) {
        this.rewardAmount = reward_amount;
    }

    public void setRewardType(int reward_type) {
        this.rewardType = reward_type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public void setCustomParams(String customparams) {
        this.customparams = customparams;
    }

    public void setItems(THashMap<String, Item> items) {
        this.items = items;
    }

    public void setDistintosItems(int num) {
        this.distintos_items = num;
    }

    public void setDistintosRooms(int num) {
        this.distintos_rooms = num;
    }

    public void setRoomId(int num) {
        this.room_id = num;
    }

    public void setButtonVisitRoom(int num) {
        this.button_visit_room = num;
    }

}
