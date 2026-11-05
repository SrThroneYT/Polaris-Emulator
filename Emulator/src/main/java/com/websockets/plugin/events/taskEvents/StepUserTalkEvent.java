package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;

import com.eu.habbo.habbohotel.rooms.RoomChatType;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserTalkEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepUserTalkEvent implements EventListener {

    @EventHandler
    public static void onWalk(UserTalkEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.habbo);

        if (ev.chatType == RoomChatType.WHISPER) {

            habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                    WebsocketsManager.getTareasManager().getTarea("HabboWhisper"));

        } else if (ev.chatType == RoomChatType.TALK) {

            habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                    WebsocketsManager.getTareasManager().getTarea("HabboTalk"));
        } else if (ev.chatType == RoomChatType.SHOUT) {

            habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                    WebsocketsManager.getTareasManager().getTarea("HabboShout"));
        }

    }

}
