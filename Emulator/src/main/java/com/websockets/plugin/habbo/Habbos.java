package com.websockets.plugin.habbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.BarraTop.BarraTop;
import com.websockets.plugin.habbo.Tareas.Habbo.HabboTareas;
import com.websockets.plugin.habbo.Tareas.Level.habbo.LevelHabbo;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.Habbo.SSOClientManager;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;

public class Habbos {

    private static final Logger LOGGER = LoggerFactory.getLogger(Habbos.class);

    private int id;
    private Properties data;
    public boolean loaded = false;
    private boolean isLoading = false;
    private HabboClient client;

    // Información basica del habboRP
    private Integer habboId;
    private String username;
    private LevelHabbo levelHabbo;

    private Habbo habbo = null;
    private int timerOnline;
    private int action_progress;

    private HabboTareas habboTareas;
    private BarraTop barraTop;

    private int enable;
    private int handitem;

    private THashMap<Integer, Integer> useTeleportTiles;

    private int VIP;
    private int vip_time;
    private int enable_vip_active;

    public Habbos(Integer habbo_id) {

        this.id = habbo_id;

        this.data = new Properties();
        this.loadFromDatabase(habbo_id);

        this.habboId = habbo_id;
        this.username = getHabboInfo(habbo_id).getUsername();

        this.checkSettings("volumen_sound", "0.8");
    }

    public void setLevel(Habbo habbo) {
        this.levelHabbo = new LevelHabbo(this.habboId);
    }

    public LevelHabbo getLevelHabbo() {
        return levelHabbo;
    }

    public THashMap<Integer, Integer> getUseTeleportTiles() {
        return useTeleportTiles;
    }

    public int getId() {
        return id;
    }

    public Boolean getVip() {
        if (VIP == 1) {
            return true;
        } else {
            return false;
        }
    }

    public int getVipTime() {
        return vip_time;
    }

    public void setBarraTop(Habbo habbo) {
        this.barraTop = new BarraTop(habbo);
    }

    public BarraTop getBarraTop() {
        return this.barraTop;
    }

    public void setHabbo(Habbo habbo) {
        this.habbo = habbo;
        this.enable = 0;
        this.handitem = 0;

        this.vip_time = habbo.getHabboInfo().getHabboStats().getClubExpireTimestamp();

        if (this.vip_time >= Emulator.getIntUnixTimestamp()) {
            this.VIP = 1;
        } else {
            this.VIP = 0;
        }

        this.checkSettings("enable_vip_active", "1");
        this.enable_vip_active = this.getInt("enable_vip_active");
    }

    public boolean getEnableVipActive() {
        return this.enable_vip_active == 1 ? true : false;
    }

    public void setEnableVipActive(int value) {
        this.updateData("enable_vip_active", value + "");
        this.enable_vip_active = value;
    }

    public void setHabboTareas(Habbo habbo) {
        this.habboTareas = new HabboTareas(habbo);
    }

    public HabboTareas getTareas() {
        return this.habboTareas;
    }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public void allowMove(Boolean value) {
        // Congelar al keko
        this.habbo.getRoomUnit().setCanWalk(value);
    }

    public void showMessage(String imagen, String title) {

        // EJEMPLO RUTA IMAGEN = /RoleplayWebsocketsRoomsInfo/tutorial/vida.png
        // RoleplayWebsocketsRoomsInfo.getTexts().getValue("pescar.cana.break").replace("%TYPE%",
        // "intermedia")
        try {
            String imagen_convert;
            if (imagen == "") {
                imagen_convert = imagen;
            } else {
                imagen_convert = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications")
                        + imagen;
            }

            JsonArray Array = new JsonArray();
            JsonObject jsonObject;
            jsonObject = new JsonObject();
            jsonObject.addProperty("img", imagen_convert);
            jsonObject.addProperty("title", title);
            Array.add(jsonObject);
            this.getHabboClient().send(new MessageManager("MessageManager", true, Outcoming.MessagePop, Array));

        } catch (Exception e) {
        }
    }

    public void ocultMessage() throws SQLException {
        JsonArray Array = new JsonArray();
        JsonObject jsonObject;
        jsonObject = new JsonObject();
        jsonObject.addProperty("img", "");
        jsonObject.addProperty("title", "");
        Array.add(jsonObject);
        this.getHabboClient().send(new MessageManager("MessageManager", false, Outcoming.MessagePop, Array));
    }

    public HabboClient getHabboClient() {

        this.client = HabboClientManager.getHabboClient(habbo);

        if (this.client == null) {
            if (habbo != null) {
                if (SSOClientManager.SSOclients.get(habbo.getHabboInfo().getSso()) != null) {
                    new HabboClientManager().addClient(SSOClientManager.SSOclients.get(habbo.getHabboInfo().getSso()),
                            habbo);
                    this.client = HabboClientManager.getHabboClient(habbo);
                }
            }
        }
        return this.client;
    }

    public HabboInfo getHabboInfo(Integer habbo_id) {
        return HabboManager.getOfflineHabboInfo(habbo_id);
    }

    public Integer getHabboId() {
        return habboId;
    }

    public String getUsername() {
        return username;
    }

    public void givePoints(int cantidad) {
        habbo.givePoints(
                WebsocketsManager.getConfig().getInt("seasonal.rol.primary.type"),
                cantidad);

        // Mostrar alerta arriba
        THashMap<String, String> keys = new THashMap<>();
        keys.put("display", "BUBBLE");
        keys.put("image", "${image.library.url}notifications/RoleplayWebsocketsRoomsInfo/cash/0.png");
        keys.put("message",
                WebsocketsManager.getTexts().getValue("action_give_points_cash")
                        .replace("%AMOUNT%", cantidad + "")
                        .replace("%TYPE%", WebsocketsManager.getTexts().getValue("seasonal.name.rol")));
        habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, keys));

        // Mostrar alerta abajo
        this.showMessage("/RoleplayWebsocketsRoomsInfo/cash/0.png",
                WebsocketsManager.getTexts().getValue("action_give_points_cash")
                        .replace("%AMOUNT%", cantidad + "")
                        .replace("%TYPE%", WebsocketsManager.getTexts().getValue("seasonal.name.rol")));
    }

    public void loadFromDatabase(Integer habbo_id) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT * FROM websocketsth_users_settings WHERE user_id= ?")) {
            statement.setInt(1, habbo_id);
            if (statement.execute()) {
                try (ResultSet set = statement.getResultSet()) {
                    while (set.next()) {
                        this.data.put(set.getString("key"), set.getString("value"));
                    }
                }
                this.loaded = true;
            }
        } catch (SQLException e) {
        }
    }

    public void saveToDatabase(String value, String key) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE websocketsth_users_settings SET `value` = ? WHERE `key` = ? AND `user_id`= ? LIMIT 1")) {
            statement.setString(1, value);
            statement.setString(2, key);
            statement.setInt(3, this.getHabboId());
            statement.executeUpdate();
            this.update(key, value);
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }
    }

    public void updateData(String key, String value) {
        this.saveToDatabase(value, key);
        this.update(key, value);
    }

    public String checkSettings(String key, String default_value) {
        String value = default_value;
        // Comprueba si no existe y lo registra
        if (this.getValue(key) == "none") {
            this.register(this.getHabboId(), key, value);
            return value;
        } else {
            return value = this.getValue(key);
        }
    }

    public int checkSettings(String key, int default_value) {

        // Comprueba si no existe y lo registra
        if (this.getValue(key) == "none") {
            this.register(this.getHabboId(), key, default_value + "");
            return default_value;
        } else {
            return default_value = this.getInt(key);
        }
    }

    public String getValue(String key) {
        return this.getValue(key, "none");
    }

    public String getValue(String key, String defaultValue) {
        if (this.isLoading)
            return defaultValue;

        if (!this.data.containsKey(key)) {
            return "none";
        }
        return this.data.getProperty(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        if (this.isLoading)
            return defaultValue;
        try {
            return (this.getValue(key, "0").equals("1")) || (this.getValue(key, "false").equals("true"));
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type boolean.", key, this.getValue(key));
        }
        return defaultValue;
    }

    public int getInt(String key) {
        return this.getInt(key, 0);
    }

    public int getInt(String key, Integer defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            return Integer.parseInt(this.getValue(key, defaultValue.toString()));
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type integer.", key, this.getValue(key));
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return this.getDouble(key, 0.0);
    }

    public double getDouble(String key, Double defaultValue) {
        if (this.isLoading)
            return defaultValue;

        try {
            return Double.parseDouble(this.getValue(key, defaultValue.toString()));
        } catch (Exception e) {
            LOGGER.error("Failed to parse key {} with value '{}' to type double.", key, this.getValue(key));
        }

        return defaultValue;
    }

    public void update(String key, String value) {
        this.data.setProperty(key, value);
    }

    public void register(int habbo, String key, String value) {
        if (this.data.getProperty(key, null) != null)
            return;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("INSERT INTO websocketsth_users_settings VALUES (?, ?, ?)")) {
            statement.setInt(1, habbo);
            statement.setString(2, key);
            statement.setString(3, value);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }

        this.update(key, value);
    }

    public void remove(String key) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement(
                                "DELETE FROM websocketsth_users_settings WHERE `user_id` = ? AND `key` = ?")) {
            statement.setInt(1, this.habboId);
            statement.setString(2, key);
            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Caught SQL exception", e);
        }

        data.clear();
        this.loadFromDatabase(this.habboId);
    }

    public boolean getProgressPendiente() {
        Boolean value;
        if (this.checkSettings("global_progress_pendiente", 0) == 1) {
            value = true;
        } else {
            value = false;
        }
        return value;
    }

    public int setProgressPendiente(Boolean value) {
        int valor;
        if (value == true) {
            valor = 1;
        } else {
            valor = 0;
        }
        this.action_progress = valor;
        this.update("global_progress_pendiente", valor + "");
        this.saveToDatabase(valor + "", "global_progress_pendiente");
        return this.action_progress;
    }

    public int getTimeOnline() {
        return this.timerOnline;
    }

    public void setTimerOnline(int number) {
        int operacion = this.getTimeOnline() + number;
        this.timerOnline = operacion;
        this.update("global.online.time", operacion + "");
        this.saveToDatabase(operacion + "", "global.online.time");
    }

    public void setHanditem(int number) {
        this.handitem = number;
    }

    public void setEnable(int number) {
        this.enable = number;
    }

    public int getEnable() {
        return enable;
    }

    public int getHanditem() {
        return handitem;
    }

    public void registerCache(String key, String value) {
        habbo.getHabboStats().cache.put(key, value);
    }

    public void removeCache(String key) {
        if (habbo.getHabboStats().cache.containsKey(key)) {
            habbo.getHabboStats().cache.put(key, "none");
        }
    }

    public String getCacheString(String key) {
        if (habbo.getHabboStats().cache.containsKey(key)) {
            return (String) habbo.getHabboStats().cache.get(key);
        } else {
            return "none";
        }
    }

    public int getCacheInt(String key) {
        int defaultValue = 0;

        if (habbo.getHabboStats().cache.containsKey(key)) {
            try {
                return Integer.parseInt(habbo.getHabboStats().cache.get(key).toString());
            } catch (Exception e) {
                LOGGER.error("Failed to parse key {} with value '{}' to type integer.", key, this.getValue(key));
            }
        }
        return defaultValue;
    }

    public String getValueOffline(String key) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement(
                                "SELECT * FROM websocketsth_users_settings WHERE `user_id` = ? AND `key` = ? LIMIT 1")) {
            statement.setInt(1, this.getHabboId());
            statement.setString(2, key);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getString("value");
                }
            }
        } catch (SQLException e) {
            WebsocketsManager.utilities().LOGGER(e + "");
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER(e + "");
        }
        return "none";
    }

    public JsonArray getCommands() {

        List<Command> commands = Emulator.getGameEnvironment().getCommandHandler()
                .getCommandsForRank(this.getHabbo().getHabboInfo().getRank().getId());

        JsonArray buttonInfo = new JsonArray();
        for (Command c : commands) {
            JsonObject jsonObject = new JsonObject();
            String allKeys = String.join("; ", c.keys);
            jsonObject.addProperty("key", allKeys);
            jsonObject.addProperty("description", Emulator.getTexts().getValue("commands.description." + c.permission,
                    "commands.description." + c.permission));
            buttonInfo.add(jsonObject);
        }

        return buttonInfo;
    }
}
