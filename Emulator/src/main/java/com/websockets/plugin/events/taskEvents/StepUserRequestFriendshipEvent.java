package com.websockets.plugin.events.taskEvents;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.friends.UserRequestFriendshipEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepUserRequestFriendshipEvent implements EventListener {

    @EventHandler
    public static void onWalk(UserRequestFriendshipEvent ev) {
        if (ev.isCancelled())
            return;

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);
        habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("SendFriendRequest"));

    }

}
