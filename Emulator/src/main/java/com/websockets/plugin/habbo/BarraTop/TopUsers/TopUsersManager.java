package com.websockets.plugin.habbo.BarraTop.TopUsers;

import com.eu.habbo.Emulator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManagerDoubleObjet;
import gnu.trove.map.hash.THashMap;

import java.sql.*;

public class TopUsersManager {

        private final String LOOK_BASE;
        private final String LOOK_PRIMARY_BASE;
        private final String URL_CURRENCY;
        private final String URL_NOTIFICATIONS;

        public TopUsersManager() {
                this.LOOK_BASE = WebsocketsManager.getConfig().getValue("global.look.ruta")
                                + "&headonly=1&size=a&gesture=sml&head_direction=2&action=std";
                this.LOOK_PRIMARY_BASE = WebsocketsManager.getConfig().getValue("global.look.ruta")
                                + "&headonly=0&size=l&gesture=sml&head_direction=2&action=std";
                this.URL_CURRENCY = WebsocketsManager.getConfig().getValue("global_url_nitro");
                this.URL_NOTIFICATIONS = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications");
        }

        public void IncomingListActions(THashMap<String, Object> data, HabboClient wsclient) {
                try {
                        if (wsclient != null && "showMenuTopUsuarios".equalsIgnoreCase((String) data.get("name"))) {
                                Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());
                                wsclient.send(new MessageManagerDoubleObjet("MessageManager", true,
                                                Outcoming.showMenuTopUsuarios,
                                                habbos.getBarraTop().getHabboInfo(),
                                                this.getTopData()));
                        }
                } catch (Exception e) {
                        WebsocketsManager.utilities().LOGGER("Error en TopUsersManager: " + e.getMessage());
                }
        }

        public JsonObject getTopData() throws SQLException {
                JsonObject mainObj = new JsonObject();

                // Botón por defecto
                JsonArray arrayPrincipal = new JsonArray();
                JsonObject searchBtn = new JsonObject();
                searchBtn.addProperty("event", "eventSearchCataPageId");
                searchBtn.addProperty("user_look_default", LOOK_BASE.replace("%LOOK%", ""));
                arrayPrincipal.add(searchBtn);

                try (Connection connection = Emulator.getDatabase().getDataSource().getConnection()) {

                        // --- NUEVA SECCIÓN: FOTOS DE LA COMUNIDAD ---
                        JsonObject photosObj = new JsonObject();
                        JsonArray photosList = new JsonArray();

                        // Consulta para obtener id, user_id (con su look), room_id, timestamp y url
                        String photoSql = "SELECT c.id, c.user_id, u.look, u.username, c.room_id, c.timestamp, c.url " +
                                        "FROM camera_web c " +
                                        "INNER JOIN users u ON c.user_id = u.id " +
                                        "ORDER BY c.id DESC LIMIT 10"; // Limitado a las últimas 10 fotos

                        try (PreparedStatement stmt = connection.prepareStatement(photoSql);
                                        ResultSet set = stmt.executeQuery()) {
                                while (set.next()) {
                                        JsonObject photo = new JsonObject();
                                        photo.addProperty("id", set.getInt("id"));
                                        photo.addProperty("user_id", set.getInt("user_id"));
                                        // Generar el look del usuario para la foto
                                        photo.addProperty("user_look",
                                                        LOOK_BASE.replace("%LOOK%", set.getString("look")));
                                        photo.addProperty("username",
                                                        set.getString("username"));
                                        photo.addProperty("room_id", set.getInt("room_id"));
                                        photo.addProperty("timestamp", set.getInt("timestamp"));
                                        photo.addProperty("url", set.getString("url"));
                                        photosList.add(photo);
                                }
                        }
                        photosObj.add("community_photos", photosList);
                        arrayPrincipal.add(photosObj); // Se agrega al array principal según lo solicitado
                        // --------------------------------------------

                        mainObj.add("arrayPrincipal", arrayPrincipal);

                        // 1. Top Créditos
                        mainObj.add("topCredits", queryTop(connection,
                                        "SELECT id, username, look, credits as points FROM users ORDER BY credits DESC LIMIT 5",
                                        "creditos", URL_CURRENCY + "/images/wallet/-1.png", false));

                        // 2. Top Diamantes
                        int currencyType = WebsocketsManager.getConfig().getInt("top_currency_type");
                        String currencyName = Emulator.getTexts().getValue("seasonal.name." + currencyType);
                        mainObj.add("topDiamonds", queryTop(connection,
                                        "SELECT u.id, u.username, u.look, c.amount as points FROM users_currency c INNER JOIN users u ON c.user_id = u.id WHERE c.type = ? ORDER BY c.amount DESC LIMIT 5",
                                        currencyName, URL_CURRENCY + "/images/wallet/" + currencyType + ".png", true,
                                        currencyType));

                        // 3. Top Respetos
                        mainObj.add("topRespets", queryTop(connection,
                                        "SELECT u.id, u.username, u.look, s.respects_received as points FROM users_settings s INNER JOIN users u ON s.user_id = u.id ORDER BY s.respects_received DESC LIMIT 5",
                                        "respetos", URL_NOTIFICATIONS + "websocketsBySrThrone/top/respects.png",
                                        false));

                        // 4. Top Tiempo Online
                        mainObj.add("topTimeOnline", queryTop(connection,
                                        "SELECT u.id, u.username, u.look, s.online_time as points FROM users_settings s INNER JOIN users u ON s.user_id = u.id ORDER BY s.online_time DESC LIMIT 5",
                                        "tiempo online", URL_NOTIFICATIONS + "websocketsBySrThrone/top/online_icon.png",
                                        true, -99));

                        // 5. Top Recompensas
                        mainObj.add("topAchievementScore", queryTop(connection,
                                        "SELECT u.id, u.username, u.look, s.achievement_score as points FROM users_settings s INNER JOIN users u ON s.user_id = u.id ORDER BY s.achievement_score DESC LIMIT 5",
                                        "recompensas", URL_NOTIFICATIONS + "websocketsBySrThrone/top/score.png",
                                        false));
                }
                return mainObj;
        }

        private JsonArray queryTop(Connection conn, String sql, String titleTag, String icon, boolean hasParam,
                        Object... params) throws SQLException {
                JsonArray array = new JsonArray();
                String titleFormat = WebsocketsManager.getTexts().getValue("top_titles_format").replace("%TOP%",
                                titleTag);

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        if (hasParam && params.length > 0) {
                                if (params[0] instanceof Integer && (int) params[0] != -99)
                                        stmt.setInt(1, (int) params[0]);
                        }

                        try (ResultSet set = stmt.executeQuery()) {
                                while (set.next()) {
                                        JsonObject row = new JsonObject();
                                        row.addProperty("title_top", titleFormat);
                                        row.addProperty("image_currency", icon);
                                        row.addProperty("user_id", set.getInt("id"));
                                        row.addProperty("username", set.getString("username"));
                                        row.addProperty("user_look",
                                                        LOOK_BASE.replace("%LOOK%", set.getString("look")));
                                        row.addProperty("user_look_primary",
                                                        LOOK_PRIMARY_BASE.replace("%LOOK%", set.getString("look")));

                                        // Manejo especial para tiempo online
                                        // Manejo especial para tiempo online
                                        if (hasParam && params.length > 0 && (int) params[0] == -99) {
                                                long totalSeconds = set.getLong("points");

                                                // CAMBIA A 'true' SI TU BASE DE DATOS GUARDA MINUTOS EN VEZ DE SEGUNDOS
                                                boolean isMinutes = false;
                                                if (isMinutes) {
                                                        totalSeconds = totalSeconds * 60L;
                                                }

                                                long days = totalSeconds / 86400L;
                                                long hours = (totalSeconds % 86400L) / 3600L;
                                                long minutes = (totalSeconds % 3600L) / 60L;

                                                String timeFormatted;
                                                if (days > 0) {
                                                        timeFormatted = days + (days == 1 ? " día" : " días")
                                                                        + (hours > 0 ? " y " + hours + " hrs" : "");
                                                } else if (hours > 0) {
                                                        timeFormatted = hours + (hours == 1 ? " hora" : " horas")
                                                                        + (minutes > 0 ? " y " + minutes + " min" : "");
                                                } else {
                                                        timeFormatted = minutes + " min";
                                                }

                                                // Guardamos el texto formateado para mostrarlo en la interfaz
                                                row.addProperty("points", timeFormatted);
                                                // Guardamos los segundos numéricos puros para poder ordenar en React
                                                // correctamente
                                                row.addProperty("raw_seconds", totalSeconds);

                                        } else {
                                                row.addProperty("points", set.getInt("points"));
                                        }

                                        array.add(row);
                                }
                        }
                }
                return array;
        }
}