package com.websockets.plugin.habbo.BarraTop.MejoresLook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.Scheduler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class TimerMejoresLook extends Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimerMejoresLook.class);
    private int tickCounter = 0; // Controla el chat cada 10 segundos
    private int minuteCounter = 0; // Controla la verificación cada 1 minuto

    public TimerMejoresLook() {
        super(1);
        this.reloadConfig();
    }

    public void reloadConfig() {
        if (this.disposed) {
            this.disposed = false;
            this.run();
        }
    }

    @Override
    public void run() {
        super.run();

        // Lógica de verificación y actualización de tiempo (60 segundos)
        minuteCounter++;
        if (minuteCounter >= 60) {
            this.verificarEstadoPase();
            minuteCounter = 0;
        }
    }

    public void verificarEstadoPase() {
        int currentTimestamp = Emulator.getIntUnixTimestamp();
        int timestampResetFinal = WebsocketsManager.getMejoresLooksManager().getTimeTemporada();

        // Si el tiempo de reinicio es 0 o ya pasó (menor o igual a 0), inicializamos o
        // reseteamos de una vez
        if (timestampResetFinal <= 0 || currentTimestamp >= timestampResetFinal) {
            // Si nunca se ha seteado un tiempo válido, lo configuramos con el tiempo actual
            // + los segundos de la temporada
            if (timestampResetFinal <= 0) {
                int nuevoTimestampReset = currentTimestamp
                        + WebsocketsManager.getMejoresLooksManager().getReinicioTemporadaSeconds();
                WebsocketsManager.getMejoresLooksManager().setTimeTemporada(nuevoTimestampReset);
                timestampResetFinal = nuevoTimestampReset;
            }

            // Si ya se cumplió el tiempo real de la temporada, ejecutamos el reseteo
            // completo
            if (currentTimestamp >= timestampResetFinal) {
                this.checkAndResetPase();
            }
        } else {
            // Si no se reinicia, restamos 1 minuto (60 segundos) al tiempo restante
            int nuevoTimestamp = timestampResetFinal - 60;

            // Actualizar en memoria
            WebsocketsManager.getMejoresLooksManager().setTimeTemporada(nuevoTimestamp);

            // WebsocketsManager.LOGGER.info("[MEJORES LOOKS] | Temporada actual: "
            // + WebsocketsManager.getMejoresLooksManager().capituloActual + " | Tiempo
            // restante para reinicio: "
            // + (nuevoTimestamp - currentTimestamp) + " segundos.");
        }
    }

    private void checkAndResetPase() {
        int currentTimestamp = Emulator.getIntUnixTimestamp();

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {
            int capituloActualNum = WebsocketsManager.getMejoresLooksManager().capituloActual;

            // Agrupamos solo por look_id y hacemos un JOIN para traernos el user_id y el
            // look real de la tabla de looks
            String queryGanadores = "SELECT m.user_id, m.look, COUNT(v.look_id) as total_votos " +
                    "FROM websocketsth_users_mejores_looks_votos v " +
                    "JOIN websocketsth_users_mejores_looks m ON v.look_id = m.id " +
                    "GROUP BY v.look_id " +
                    "ORDER BY total_votos DESC LIMIT 3";

            String insertHistory = "INSERT INTO websocketsth_users_mejores_looks_history (capitulo, puesto, votos, user_id, look, timestamp, reclaimed) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmtGanadores = conn.prepareStatement(queryGanadores);
                    PreparedStatement insertStmt = conn.prepareStatement(insertHistory);
                    ResultSet rs = stmtGanadores.executeQuery()) {

                int puestoActual = 1;

                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String look = rs.getString("look");
                    int totalVotos = rs.getInt("total_votos");

                    if (userId > 0) {
                        insertStmt.setInt(1, capituloActualNum);
                        insertStmt.setInt(2, puestoActual);
                        insertStmt.setInt(3, totalVotos);
                        insertStmt.setInt(4, userId);
                        insertStmt.setString(5, look);
                        insertStmt.setInt(6, currentTimestamp);
                        insertStmt.setInt(7, 0);

                        insertStmt.executeUpdate();
                        puestoActual++;
                    }
                }
            }

            // 3. Incrementamos el capítulo
            int nuevoCapitulo = capituloActualNum + 1;

            WebsocketsManager.getConfig().updateData("global.look.capitulo_actual", nuevoCapitulo + "");
            WebsocketsManager.getMejoresLooksManager().capituloActual = nuevoCapitulo;

            // 4. Limpieza de votos
            try (PreparedStatement deleteStmt = conn
                    .prepareStatement("DELETE FROM websocketsth_users_mejores_looks_votos")) {
                deleteStmt.execute();
            }

            // 5. Limpieza de looks
            try (PreparedStatement deleteStmtNew = conn
                    .prepareStatement("DELETE FROM websocketsth_users_mejores_looks")) {
                deleteStmtNew.execute();
            }

            int nuevoTimestampReset = currentTimestamp
                    + WebsocketsManager.getMejoresLooksManager().reinicio_temporada_seconds;
            WebsocketsManager.getMejoresLooksManager().setTimeTemporada(nuevoTimestampReset);
            WebsocketsManager.getMejoresLooksManager().updateMejoresLooks();

            WebsocketsManager.LOGGER.info("[MEJORES LOOKS] | Temporada reiniciada. Capítulo actual: " + nuevoCapitulo
                    + " | Nuevo tiempo de reinicio: " + nuevoTimestampReset);

        } catch (SQLException e) {
            LOGGER.error("Error al resetear el pase y guardar el podio: ", e);
        }
    }

    private void resetAllUsersSettings() {
        String query = "DELETE FROM websocketsth_users_settings WHERE `key` IN ('level.level', 'level.xp', 'premios_obt_json_array', 'premios_VIP_obt_json_array')";

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement deleteStmt = conn.prepareStatement(query)) {
            int rowsAffected = deleteStmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error al limpiar los ajustes de los usuarios en la base de datos: ", e);
        }
    }
}