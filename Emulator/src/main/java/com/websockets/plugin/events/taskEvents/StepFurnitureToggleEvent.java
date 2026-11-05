package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.furniture.FurnitureToggleEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.clickFurnis.CajeroFurni;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Tarea;

public class StepFurnitureToggleEvent implements EventListener {

    @EventHandler
    public static void onWalk(FurnitureToggleEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        Tarea task = WebsocketsManager.getTareasManager().getItemsClickeables()
                .get(ev.furniture.getBaseItem().getName());
        if (task != null) {
            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);
            habbos.getTareas().progressAchievement(ev.habbo.getClient().getHabbo(),
                    WebsocketsManager.getTareasManager().getTarea(task.name));
        }

        WebsocketsManager.getCatalogWindowManager().localizarItemEvent(ev.habbo, ev.furniture);
        CajeroFurni.eventToggleFurni(ev.habbo, ev.furniture);

    }
}
