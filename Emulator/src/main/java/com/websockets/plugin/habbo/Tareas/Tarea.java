package com.websockets.plugin.habbo.Tareas;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.websockets.plugin.WebsocketsManager;

import gnu.trove.map.hash.THashMap;

public class Tarea {

    public int id;
    public int type_id;
    public int orden;
    public String name;
    public final THashMap<Integer, TareaLevel> levels;

    public Tarea(ResultSet set) throws SQLException {

        this.levels = new THashMap<>();

        this.addLevel(new TareaLevel(set));

        this.id = set.getInt("id");
        this.type_id = set.getInt("id_type");
        this.name = set.getString("name");
        this.orden = set.getInt("orden");
    }

    public void addLevel(TareaLevel level) {
        synchronized (this.levels) {
            this.levels.put(level.level, level);
        }
    }

    public TareaLevel getLevelForProgress(int progress) {
        TareaLevel l = null;
        if (progress > 0) {
            for (TareaLevel level : this.levels.values()) {
                if (progress >= level.progress) {
                    if (l != null) {
                        if (l.level > level.level) {
                            continue;
                        }
                    }

                    l = level;
                }
            }
        }
        return l;
    }

    public TareaLevel getNextLevel(int currentLevel) {

        for (TareaLevel level : this.levels.values()) {
            if (level.level == (currentLevel + 1))
                return level;
        }

        return null;
    }

    public TareaLevel firstLevel() {
        return this.levels.get(1);
    }

    public void clearLevels() {
        this.levels.clear();
    }


}