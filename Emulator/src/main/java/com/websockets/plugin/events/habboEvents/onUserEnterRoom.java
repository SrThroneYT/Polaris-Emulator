package com.websockets.plugin.events.habboEvents;

import java.sql.SQLException;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserEnterRoomEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.bots.BotsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManagerDoubleObjet;

public class onUserEnterRoom implements EventListener {

    @EventHandler
    public static void UserEnterRoom(UserEnterRoomEvent event) throws SQLException {

        if (event.habbo != null) {

            final Habbo habbo = event.habbo;
            final Room room = event.room;

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

            if (habbos == null) {
                return;
            }

            final HabboClient client = habbos.getHabboClient();

            if (client == null || room == null) {
                return;
            }

            Emulator.getThreading().run(() -> {

                // WebsocketsManager.getYoutubeRoomManager().ocultYoutubeVentana(habbo);

                habbos.getTareas().showButtonTareas();

                try {
                    habbos.getHabboClient()
                            .send(new MessageManager("MessageManager", true, Outcoming.commandsList,
                                    habbos.getCommands()));

                    habbos.getHabboClient()
                            .send(new MessageManagerDoubleObjet("MessageManager", true, Outcoming.barraTop,
                                    habbos.getBarraTop().getHabboInfo(), habbos.getBarraTop().getArrayButtons()));

                    BotsManager.userEnterRoom(room, habbo);

                    if (habbos.getVipTime() > Emulator.getIntUnixTimestamp()) {
                        if (habbos.getEnableVipActive() == false) {
                            return;
                        }
                        room.giveEffect(habbo, 711, -1);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                WebsocketsManager.getHabboVisitor().registerVisitor(habbo, room);

            }, 500);

        }

    }
}
