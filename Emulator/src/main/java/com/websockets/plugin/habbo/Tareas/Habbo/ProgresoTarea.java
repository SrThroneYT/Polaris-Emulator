package com.websockets.plugin.habbo.Tareas.Habbo;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Tareas.Tarea;

public class ProgresoTarea {

    private int id;
    private Tarea tarea;
    private int progress;

    public ProgresoTarea(ResultSet set) throws SQLException {
        this.id = set.getInt("id");
        this.tarea = WebsocketsManager.getTareasManager().getTarea(set.getInt("tarea_id"));
        this.progress = set.getInt("progress");
    }


    public int getId() {
        return id;
    }

    public Tarea getTarea() {
        return tarea;
    }

    public int getProgress() {
        return progress;
    }

}
