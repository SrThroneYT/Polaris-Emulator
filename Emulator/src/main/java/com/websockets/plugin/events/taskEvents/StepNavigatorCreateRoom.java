package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.navigator.NavigatorRoomCreatedEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepNavigatorCreateRoom implements EventListener {

    @EventHandler
    public static void onWalk(NavigatorRoomCreatedEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);

        habbos.getTareas().progressAchievement(ev.habbo.getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("CreateRooms"));

    }
}
