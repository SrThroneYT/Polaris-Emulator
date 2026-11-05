package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserSavedMottoEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepUserSavedMottoEvent implements EventListener {

    @EventHandler
    public static void execute(UserSavedMottoEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);
        habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("ChangeMission"));

    }

}
