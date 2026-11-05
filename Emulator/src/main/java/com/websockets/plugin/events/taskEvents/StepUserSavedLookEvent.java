package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserSavedLookEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepUserSavedLookEvent implements EventListener {

    @EventHandler
    public static void onWalk(UserSavedLookEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);
        habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("ChangeLook"));

    }
}
