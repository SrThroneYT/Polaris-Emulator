package com.websockets.plugin.habbo.Tareas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Level.Level;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TareasManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TareasManager.class);

    private THashMap<Integer, TareaType> types = new THashMap<>();
    private THashMap<String, Tarea> tareas = new THashMap<>();

    public TimerPase timerPase;

    public int capituloActual = 1;
    public int temporadaActual = 1;

    public int reinicio_temporada_seconds;
    public int time_reset_pase;

    public TareasManager() {
        this.init();
        this.loadData();
    }

    private void init() {
        try {
            if (this.timerPase == null || this.timerPase.isDisposed()) {
                this.timerPase = new TimerPase();
                Emulator.getThreading().run(this.timerPase);
            }
        } catch (Exception e) {
            LOGGER.error("[ROLEPLAY WEBSOCKETS ROOM INFO] | Error al iniciar el Timer de Turnos: ", e);
        }
    }

    public void loadData() {
        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement("SELECT * FROM websocketsth_pase_global WHERE id = 1")) {
                ResultSet set = preparedStatement.executeQuery();
                while (set.next()) {

                    this.reinicio_temporada_seconds = set.getInt("reinicio_temporada_segundos");
                    this.time_reset_pase = set.getInt("timestamp_reset");

                    this.capituloActual = set.getInt("capitulo");
                    this.temporadaActual = set.getInt("temporada");

                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void setTimeTemporada(int value){
        this.time_reset_pase = value;
    }

    public int getTimeTemporada(){
        return this.time_reset_pase;
    }

    public int getTimeFilter(){
        int currentTimestamp = Emulator.getIntUnixTimestamp();
        int timestampReset = this.getTimeTemporada();
        int segundosRestantes = Math.max(0, timestampReset - currentTimestamp);

        return segundosRestantes;
    }

    public void update() throws SQLException {
        this.types.clear();
        this.loadTypes();
    }

    public void loadTypes() throws SQLException {

        for (Tarea tarea : this.tareas.values()) {
            tarea.clearLevels();
        }

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement("SELECT * FROM websocketsth_pase_tareas_types ORDER BY id ASC")) {
                ResultSet set = preparedStatement.executeQuery();
                while (set.next()) {

                    TareaType newTareaType = new TareaType(set);
                    types.put(set.getInt("id"), newTareaType);

                    try (Connection connTarea = Emulator.getDatabase().getDataSource().getConnection()) {
                        try (PreparedStatement preparedStatementTarea = conn.prepareStatement(
                                "SELECT * FROM websocketsth_pase_tareas WHERE `id_type`= ? ORDER BY `orden` ASC")) {
                            preparedStatementTarea.setInt(1, set.getInt("id"));
                            ResultSet setTarea = preparedStatementTarea.executeQuery();
                            while (setTarea.next()) {

                                if (!this.tareas.containsKey(setTarea.getString("name"))) {

                                    this.tareas.put(setTarea.getString("name"), new Tarea(setTarea));
                                    newTareaType.createTarea(setTarea);

                                } else {
                                    TareaLevel newLevel = new TareaLevel(setTarea);
                                    this.tareas.get(setTarea.getString("name")).addLevel(newLevel);
                                }
                            }
                        } catch (SQLException exception) {
                            exception.printStackTrace();
                        }
                    }

                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static JsonArray sortJsonArrayInPlaceAscending(JsonArray jsonArray, String key) {
        List<JsonElement> jsonList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonList.add(jsonArray.get(i));
        }

        jsonList.sort(Comparator.comparingInt(o -> o.getAsJsonObject().get(key).getAsInt()));

        // Limpiar el JsonArray original
        while (jsonArray.size() > 0) {
            jsonArray.remove(0);
        }

        // Agregar los elementos ordenados de nuevo al JsonArray original
        for (JsonElement element : jsonList) {
            jsonArray.add(element);
        }

        return jsonArray;
    }

    public THashMap<Integer, TareaType> getTypesTareas() {
        return types;
    }

    public void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient != null) {

                String name = (String) data.get("name");

                if (name.equalsIgnoreCase("closeTareasWindow")) {
                    Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                    habbos.removeCache("windowTareasActive");
                } else if (name.equalsIgnoreCase("viewTareasWindow")) {

                    Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                    habbos.getTareas().showTareasWindow();

                    habbos.registerCache("windowTareasActive", "1");

                } else if (name.equalsIgnoreCase("OpenMenuLevelOtherTop")) {
                    // int user_id = (int) ((double) data.get("action"));

                    Habbos habbosMy = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                    Habbos habbosOther = WebsocketsManager.getHabbosManager()
                            .getHabboRpOffline(wsclient.getHabbo().getHabboInfo().getId());

                    habbosMy.getHabboClient().send(new MessageManager("MessageManager", true,
                            Outcoming.ViewLevelOtherHabboWindow, habbosOther.getLevelHabbo().getArrayOffline()));

                } else if (name.equalsIgnoreCase("OpenObtenerPremiosLevel")) {

                    int level_id = (int) ((double) data.get("action"));
                    Level level = WebsocketsManager.getLevelManager().getLevel(level_id);

                    Habbos habboRp = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                    habboRp.getLevelHabbo().obtenerRewards(level, false);

                } else if (name.equalsIgnoreCase("OpenObtenerPremiosLevelVIP")) {

                    int level_id = (int) ((double) data.get("action"));
                    Level level = WebsocketsManager.getLevelManager().getLevel(level_id);

                    Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                    habbos.getLevelHabbo().obtenerRewards(level, true);

                } else if (name.equalsIgnoreCase("openAjustesPase")) {

                    Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());

                    habbos.getHabboClient().send(new MessageManager("MessageManager", true,
                            Outcoming.ajustesPase, this.getArrayAjustes()));

                }
            }
        } catch (Exception e) {
        }
    }

    public JsonArray getArrayAjustes() {

        JsonArray Array = new JsonArray();
        JsonObject jsonObject2;
        jsonObject2 = new JsonObject();

        jsonObject2.addProperty("look", "");

        Array.add(jsonObject2);
        return Array;
    }

    public THashMap<String, Tarea> getItemsClickeables() {
        THashMap<String, Tarea> itemsList = (THashMap<String, Tarea>) new THashMap<String, Tarea>();
        for (Tarea tareas : this.getTareas().values()) {
            if (tareas instanceof Tarea) {
                Tarea tarea = (Tarea) tareas;
                // if (tarea.getType().equals("onclick")) {
                // for (Item itemstask : tarea.getItems().values()) {
                // itemsList.put(itemstask.getName(), tarea);
                // }
                // }
            }
        }

        return itemsList;
    }

    public THashMap<String, Tarea> getTareas() {
        return this.tareas;
    }

    public Tarea getTarea(String name) {
        return this.tareas.get(name);
    }

    public Tarea getTarea(int id) {
        synchronized (this.tareas) {
            for (Map.Entry<String, Tarea> set : this.tareas.entrySet()) {
                if (set.getValue().id == id) {
                    return set.getValue();
                }
            }
        }

        return null;
    }

    public static void saveAchievements(Habbo habbo) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE websocketsth_pase_progresos SET progress = ? WHERE tarea_name = ? AND user_id = ? LIMIT 1")) {
            statement.setInt(3, habbo.getHabboInfo().getId());

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

            for (Map.Entry<Tarea, Integer> map : habbos.getTareas().getAchievementProgress().entrySet()) {
                statement.setInt(1, map.getValue());
                statement.setString(2, map.getKey().name);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            WebsocketsManager.LOGGER.error("Caught SQL exception", e);
        }
    }

    public static int getAchievementProgressForHabbo(int userId, Tarea achievement) {
        if (achievement == null) {
            return 0;
        }

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT progress FROM websocketsth_pase_progresos WHERE user_id = ? AND tarea_name = ? LIMIT 1")) {
            statement.setInt(1, userId);
            statement.setString(2, achievement.name);
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getInt("progress");
                }
            }
        } catch (SQLException e) {
            WebsocketsManager.LOGGER.error("Caught SQL exception", e);
        }

        return 0;
    }

}
