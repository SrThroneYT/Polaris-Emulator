package com.websockets.plugin.events.habboEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserExitRoomEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManagerDoubleObjet;

public class onUserExitRoom implements EventListener {

    @EventHandler
    public static void eventUserExitRoom(UserExitRoomEvent event) throws SQLException {

        HabboClient client = HabboClientManager.getHabboClient(event.habbo);
        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(event.habbo);

        if (client == null) {
            return;
        }

        if (client != null)

            // Reset progresos pendientes
            habbos.setProgressPendiente(false);

        habbos.getHabboClient()
                .send(new MessageManagerDoubleObjet("MessageManager", false, Outcoming.barraTop,
                        null, null));

        if (client != null) {

            // // Ocultar flecha de frank y al mismo frank
            // habbos.getHabboClient().send(new MessageManager("MessageManager", false,
            // Outcoming.FlechaTutorial, null));

        }
    }
}
