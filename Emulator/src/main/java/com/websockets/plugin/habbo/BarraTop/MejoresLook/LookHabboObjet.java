package com.websockets.plugin.habbo.BarraTop.MejoresLook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.eu.habbo.Emulator;

public class LookHabboObjet {

    public int id;
    public int user_id;
    public String look;
    public int votos;

    public LookHabboObjet(int id, int user_id, String look, int votos) {
        this.id = id;
        this.user_id = user_id;
        this.look = look;
        this.votos = votos;
    }

    public int getVotos() {
        return this.votos;
    }

    public void setVotos(int votos) {
        this.votos = votos;
    }

    public void insertVotoDb(int userId) {
        // La consulta ahora es un INSERT en la tabla de votos
        String query = "INSERT INTO websocketsth_users_mejores_looks_votos (user_id, look_id) VALUES (?, ?)";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, this.id); // Asumiendo que 'id' es el ID del look en este objeto

            statement.executeUpdate();

        } catch (SQLException e) {
            // Usamos el log de errores del emulador para debuguear si algo falla
            Emulator.getLogging().logPacketError(e);
        }
    }

    public void loadVotosCount() {
        String query = "SELECT COUNT(*) AS total FROM websocketsth_users_mejores_looks_votos WHERE look_id = ?";

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, this.id); // El ID del look actual

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int totalVotos = resultSet.getInt("total");
                    // Usamos el setter que mencionaste
                    this.setVotos(totalVotos);
                }
            }

        } catch (SQLException e) {
            Emulator.getLogging().logPacketError(e);
        }
    }

}
