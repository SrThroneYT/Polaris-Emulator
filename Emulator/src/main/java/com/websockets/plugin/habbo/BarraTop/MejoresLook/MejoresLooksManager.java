package com.websockets.plugin.habbo.BarraTop.MejoresLook;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Tareas.TareasManager;
import com.websockets.plugin.habbo.Tareas.TimerPase;

import gnu.trove.map.hash.THashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MejoresLooksManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MejoresLooksManager.class);

    private final THashMap<Integer, LookHabboObjet> looks = new THashMap<>();
    public THashMap<Integer, CapituloLook> capitulos = new THashMap<>();

    private final int TIMERANKING = WebsocketsManager.getConfig().getInt("global.look.timeranking");
    public int capituloActual = 1;

    public int reinicio_temporada_seconds = 0;
    public int time_reset;

    public TimerMejoresLook timerMejoresLook;

    public MejoresLooksManager() {
        this.registerData();

        this.updateMejoresLooks();
        this.init();
    }

    private void init() {
        try {
            if (this.timerMejoresLook == null || this.timerMejoresLook.isDisposed()) {
                this.timerMejoresLook = new TimerMejoresLook();
                Emulator.getThreading().run(this.timerMejoresLook);
            }
        } catch (Exception e) {
            LOGGER.error("[ROLEPLAY WEBSOCKETS ROOM INFO] | Error al iniciar el Timer de Turnos: ", e);
        }
    }

    public void registerData() {
        WebsocketsManager.getConfig().register("global.look.capitulo_actual", "1");
        WebsocketsManager.getConfig().register("global.look.time_reset", "0");
        WebsocketsManager.getConfig().register("global.look.reinicio_temporada_seconds", "604800");
    }

    public void updateMejoresLooks() {

        this.capituloActual = WebsocketsManager.getConfig().getInt("global.look.capitulo_actual");
        this.capitulos.put(this.capituloActual, new CapituloLook(this.capituloActual));

        this.time_reset = WebsocketsManager.getConfig()
                .getInt("global.look.time_reset");
        this.reinicio_temporada_seconds = WebsocketsManager.getConfig()
                .getInt("global.look.reinicio_temporada_seconds");

        // Limpiamos el mapa actual antes de recargar
        this.looks.clear();

        // Consulta que obtiene los datos del look y cuenta los votos asociados en la
        // otra tabla
        String query = "SELECT l.id, l.user_id, l.look, l.timestamp, " +
                "(SELECT COUNT(*) FROM websocketsth_users_mejores_looks_votos WHERE look_id = l.id) as votos " +
                "FROM websocketsth_users_mejores_looks l";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet set = statement.executeQuery()) {

            while (set.next()) {
                LookHabboObjet look = new LookHabboObjet(
                        set.getInt("id"),
                        set.getInt("user_id"),
                        set.getString("look"),
                        set.getInt("votos"));
                // Opcional: Si necesitas el user_id en el objeto LookHabboObjet, asegúrate de
                // añadirlo al constructor
                looks.put(look.id, look);
            }
        } catch (SQLException e) {
            Emulator.getLogging().logPacketError(e);
        }
    }

    public int getReinicioTemporadaSeconds() {
        return reinicio_temporada_seconds;
    }

    public int getTimeTemporada() {
        return time_reset;
    }

    public void setTimeTemporada(int time) {
        this.time_reset = time;
        WebsocketsManager.getConfig().updateData("global.look.time_reset", time + "");
    }

    public THashMap<Integer, LookHabboObjet> getLooks() {
        return looks;
    }

    public void createLook(int userId, String look) {
        // 1. Primero comprobamos si el user_id ya existe
        String checkQuery = "SELECT COUNT(*) FROM websocketsth_users_mejores_looks WHERE user_id = ?";
        String insertQuery = "INSERT INTO websocketsth_users_mejores_looks (user_id, look, timestamp) VALUES (?, ?, ?)";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {

            // Comprobar existencia
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setInt(1, userId);
                try (ResultSet rs = checkStatement.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // El usuario ya tiene un look, salimos de la función
                        return;
                    }
                }
            }

            // 2. Si no existe, procedemos con la inserción
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setInt(1, userId);
                statement.setString(2, look);
                statement.setInt(3, (int) (System.currentTimeMillis() / 1000));
                statement.executeUpdate();

                this.updateMejoresLooks();
            }
        } catch (SQLException e) {
            Emulator.getLogging().logPacketError(e);
        }
    }

    public boolean getUserVoto(int userId, int lookId) {
        String query = "SELECT COUNT(*) FROM websocketsth_users_mejores_looks_votos WHERE user_id = ? AND look_id = ?";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, lookId);
            ResultSet set = statement.executeQuery();

            if (set.next()) {
                return set.getInt(1) > 0; // Si el conteo es mayor que 0, el usuario ya votó
            }
        } catch (SQLException e) {
            Emulator.getLogging().logPacketError(e);
        }
        return false;
    }

    public boolean hasUserVotedAny(int userId) {
        String query = "SELECT COUNT(*) FROM websocketsth_users_mejores_looks_votos WHERE user_id = ?";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getInt(1) > 0; // Si tiene al menos un registro, ya gastó su voto
                }
            }
        } catch (SQLException e) {
            Emulator.getLogging().logPacketError(e);
        }

        return false;
    }

    public JsonArray getArray(Habbo habbo) {
        JsonArray dataArray = new JsonArray();
        JsonObject rootContent = new JsonObject();
        JsonArray looksData = new JsonArray();

        int userId = habbo.getHabboInfo().getId();

        String ownerLook = WebsocketsManager.getConfig().getValue("global.look.ruta").replace("%LOOK%",
                habbo.getHabboInfo().getLook())
                + "&headonly=0&size=a&gesture=sml&head_direction=2&action=std";

        // 1. Verificamos si el usuario ya gastó su único voto en el concurso
        boolean yaVotoGeneral = this.hasUserVotedAny(userId);

        // 2. Agregamos la información del Habbo actual con su estatus de voto
        JsonObject habboData = new JsonObject();

        habboData.addProperty("habbo_user_id", userId);
        habboData.addProperty("src_imagen",
                WebsocketsManager.getConfig().getValue("global_url_images"));
        habboData.addProperty("habbo_username", habbo.getHabboInfo().getUsername());
        habboData.addProperty("habbo_look", ownerLook);
        habboData.addProperty("timestamp", this.time_reset);
        habboData.addProperty("capitulo", this.capituloActual);
        habboData.addProperty("ya_voto", yaVotoGeneral);

        // --- NUEVO: CARGAR HISTORIAL DE TEMPORADAS PASADAS ---
        JsonArray historyArray = new JsonArray();
        String queryHistory = "SELECT capitulo, puesto, votos, user_id, look, timestamp, reclaimed FROM websocketsth_users_mejores_looks_history ORDER BY CAST(capitulo AS UNSIGNED) DESC, CAST(puesto AS UNSIGNED) ASC";

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement stmtHistory = conn.prepareStatement(queryHistory);
                ResultSet rsHistory = stmtHistory.executeQuery()) {

            while (rsHistory.next()) {
                JsonObject historyJson = new JsonObject();
                int historyUserId = rsHistory.getInt("user_id");

                historyJson.addProperty("capitulo", rsHistory.getString("capitulo"));
                historyJson.addProperty("puesto", rsHistory.getString("puesto"));
                historyJson.addProperty("votos", rsHistory.getString("votos"));
                historyJson.addProperty("user_id", historyUserId);
                historyJson.addProperty("look", rsHistory.getString("look"));
                historyJson.addProperty("timestamp", rsHistory.getString("timestamp"));
                historyJson.addProperty("reclaimed", rsHistory.getInt("reclaimed"));

                // Opcional: Buscar el username del usuario offline si lo necesitas en el
                // frontend
                HabboInfo historicalInfo = HabboManager.getOfflineHabboInfo(historyUserId);
                if (historicalInfo != null) {
                    historyJson.addProperty("username", historicalInfo.getUsername());
                } else {
                    historyJson.addProperty("username", "Desconocido");
                }

                historyArray.add(historyJson);
            }
        } catch (SQLException e) {
            LOGGER.error("Error al cargar el historial de mejores looks: ", e);
        }

        habboData.add("historial", historyArray);
        // ----------------------------------------------------

        // Creamos el array de posiciones para este capítulo actual
        JsonArray posicionesArray = new JsonArray();

        java.util.List<CapituloLookPosition> sortedPositions = new java.util.ArrayList<>(
                this.capitulos.get(this.capituloActual).positions.values());
        sortedPositions.sort(java.util.Comparator.comparingInt(p -> p.position));

        for (CapituloLookPosition capituloPosition : sortedPositions) {
            JsonObject posicionJson = new JsonObject();
            posicionJson.addProperty("position", capituloPosition.position);

            JsonArray premiosArray = new JsonArray();
            java.util.List<PremiosCapitulos> sortedPremios = new java.util.ArrayList<>(
                    capituloPosition.premios.values());
            sortedPremios.sort(java.util.Comparator.comparingInt(PremiosCapitulos::getId));

            for (PremiosCapitulos iterable_element : sortedPremios) {
                JsonObject premioJson = new JsonObject();
                premioJson.addProperty("id", iterable_element.getId());
                premioJson.addProperty("nombre", iterable_element.getNombre());
                premioJson.addProperty("item_exist", iterable_element.getItemExist());

                if (iterable_element.getItemExist() == 1) {
                    premioJson.addProperty("img", WebsocketsManager.getConfig().getValue("global_url.hotel.furnis")
                            + "icons/%ITEM%_icon.png".replace("%ITEM%", iterable_element.getNombre()));
                } else {
                    premioJson.addProperty("img", WebsocketsManager.getConfig().getValue("global_url_nitro")
                            + "images/wallet/%ITEM%.png".replace("%ITEM%", iterable_element.getPointType() + ""));
                }

                premioJson.addProperty("cantidad", iterable_element.getCantidad());
                premioJson.addProperty("title", iterable_element.getTitle());
                premioJson.addProperty("descripcion", iterable_element.getDescripcion());

                premiosArray.add(premioJson);
            }

            posicionJson.add("premios", premiosArray);
            posicionesArray.add(posicionJson);
        }

        habboData.add("posiciones", posicionesArray);
        rootContent.add("habboData", habboData);

        for (LookHabboObjet look : this.looks.values()) {
            JsonObject lookJson = new JsonObject();
            lookJson.addProperty("id", look.id);
            lookJson.addProperty("user_id", look.user_id);

            HabboInfo habboInfo = HabboManager.getOfflineHabboInfo(look.user_id);
            if (habboInfo != null) {
                lookJson.addProperty("username", habboInfo.getUsername());
            }

            lookJson.addProperty("votado", this.getUserVoto(userId, look.id));
            lookJson.addProperty("look", look.look);
            lookJson.addProperty("votos", look.votos);

            looksData.add(lookJson);
        }

        rootContent.add("looksData", looksData);
        dataArray.add(rootContent);
        return dataArray;
    }
}