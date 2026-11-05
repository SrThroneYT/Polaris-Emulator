package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;
import java.util.Objects;

import com.eu.habbo.habbohotel.rooms.RoomChatType;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserExecuteCommandEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepUserExecuteCommandEvent implements EventListener {

    @EventHandler
    public static void onWalk(UserExecuteCommandEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);

        if (Objects.equals(ev.command.permission, "cmd_empty")) {
            if (ev.isSuccess()) {
                habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                        WebsocketsManager.getTareasManager().getTarea("HabboEmptyItems"));
            }
        }

        else

        if (Objects.equals(ev.command.permission, "cmd_pickall")) {
            if (ev.isSuccess()) {
                habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                        WebsocketsManager.getTareasManager().getTarea("HabboPickallRoom"));
            }
        }

    }

}
