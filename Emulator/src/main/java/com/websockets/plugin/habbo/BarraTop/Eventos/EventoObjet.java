package com.websockets.plugin.habbo.BarraTop.Eventos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventoObjet {

    public int id;
    public String title;
    public String type;
    public int hora;
    public int user_id;
    public String texto;
    public String image;
    public int room_id;
    public boolean active = false;

    public EventoObjet(ResultSet set) throws SQLException {
        this.id = set.getInt("id");
        this.title = set.getString("title");
        this.type = set.getString("type");
        this.hora = set.getInt("hora");
        this.user_id = set.getInt("user");
        this.texto = set.getString("texto");
        this.image = set.getString("image");
        this.room_id = set.getInt("room_id");
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public int getHora() {
        return hora;
    }

    public int getUserId() {
        return user_id;
    }

    public String getTexto() {
        return texto;
    }

    public String getImage() {
        return image;
    }

    public int getRoomId() {
        return room_id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
