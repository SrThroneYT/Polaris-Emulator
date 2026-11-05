package com.websockets.plugin.habbo.Tareas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.Scheduler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class TimerPase extends Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimerPase.class);
    private int tickCounter = 0; // Controla el chat cada 10 segundos
    private int minuteCounter = 0; // Controla la verificación cada 1 minuto

    public TimerPase() {
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

        // Lógica de chat (60 segundos)
        boolean hayUsuariosOnline = !Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().isEmpty();
        if (hayUsuariosOnline) {
            tickCounter++;
            if (tickCounter >= 60) {
                int currentTimestamp = Emulator.getIntUnixTimestamp();

                int timestampReset = WebsocketsManager.getTareasManager().getTimeTemporada();
                int segundosRestantes = Math.max(0, timestampReset - currentTimestamp);

                int dias = segundosRestantes / 86400;
                int horas = (segundosRestantes % 86400) / 3600;
                int minutos = (segundosRestantes % 3600) / 60;

                for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
                    if (habbo != null && habbo.isOnline() && habbo.getHabboInfo().getCurrentRoom() != null) {

                        // habbo.talk("Faltan " + dias + " días, " + horas + " horas y " + minutos
                        // + " minutos para el reinicio de temporada.");

                        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
                        habbos.getTareas().progressAchievement(habbo,
                                WebsocketsManager.getTareasManager().getTarea("MinutesOnline"));
                    }
                }
                tickCounter = 0;
            }
        }

        // Lógica de verificación y actualización de tiempo (60 segundos)
        minuteCounter++;
        if (minuteCounter >= 60) {
            this.verificarEstadoPase();
            minuteCounter = 0;
        }
    }

    private void verificarEstadoPase() {
        int currentTimestamp = Emulator.getIntUnixTimestamp();
        int timestampResetFinal = WebsocketsManager.getTareasManager().getTimeTemporada();

        // Si ya pasó el tiempo O está en negativo, reiniciamos de una vez
        if (timestampResetFinal <= 0 || currentTimestamp >= timestampResetFinal) {
            this.checkAndResetPase();
        } else {
            int nuevoTimestamp = timestampResetFinal - 60;
            WebsocketsManager.getTareasManager().setTimeTemporada(nuevoTimestamp);

            try (Connection conn = Emulator.getDatabase().getDataSource().getConnection();
                    PreparedStatement ps = conn
                            .prepareStatement("UPDATE websocketsth_pase_global SET timestamp_reset = ? WHERE id = 1")) {
                ps.setInt(1, nuevoTimestamp);
                ps.execute();
            } catch (SQLException e) {
                LOGGER.error("Error al actualizar el timestamp_reset del pase en curso: ", e);
            }
        }
    }

    private void checkAndResetPase() {
        int currentTimestamp = Emulator.getIntUnixTimestamp();

        try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {
            int capitulo = WebsocketsManager.getTareasManager().capituloActual;
            int temporada = WebsocketsManager.getTareasManager().temporadaActual;

            // Incrementar
            capitulo++;
            if (capitulo > 10) {
                capitulo = 1;
                temporada++;
            }

            WebsocketsManager.getTareasManager().capituloActual = capitulo;
            WebsocketsManager.getTareasManager().temporadaActual = temporada;

            // Limpieza y actualización
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM websocketsth_pase_progresos")) {
                deleteStmt.execute();
            }

            int nuevoTimestampReset = currentTimestamp
                    + WebsocketsManager.getTareasManager().reinicio_temporada_seconds;
            try (PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE websocketsth_pase_global SET timestamp_reset = ?, capitulo = ?, temporada = ? WHERE id = 1")) {
                updateStmt.setInt(1, nuevoTimestampReset);
                updateStmt.setInt(2, capitulo);
                updateStmt.setInt(3, temporada);
                updateStmt.execute();
            }

            // Actualizar memoria
            WebsocketsManager.getTareasManager().time_reset_pase = nuevoTimestampReset;

            for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {

                this.resetAllUsersSettings();

                if (habbo != null && habbo.isOnline()) {
                    Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

                    habbos.updateData("level.level", "1");
                    habbos.updateData("level.xp", "0");
                    habbos.updateData("premios_obt_json_array", "[]");
                    habbos.updateData("premios_VIP_obt_json_array", "[]");

                    habbos.getTareas().update();
                    habbos.getTareas().loadAchievementProgressHabbo();
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Error al resetear el pase: ", e);
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