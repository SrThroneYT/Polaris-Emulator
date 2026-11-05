package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserRespectedEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepUserRespectedEvent implements EventListener {

    @EventHandler
    public static void onWalk(UserRespectedEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        Habbos habbos_from_respected = WebsocketsManager.getHabbosManager().getHabbos(ev.from);
        habbos_from_respected.getTareas().progressAchievement(habbos_from_respected.getHabbo().getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("GiveRespects"));

        Habbos habbos_receibed_respected = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);
        habbos_from_respected.getTareas().progressAchievement(habbos_receibed_respected.getHabbo().getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("TakeRespects"));

    }
}
