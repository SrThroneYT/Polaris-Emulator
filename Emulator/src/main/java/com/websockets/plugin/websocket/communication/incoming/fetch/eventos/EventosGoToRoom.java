package com.websockets.plugin.websocket.communication.incoming.fetch.eventos;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.websockets.plugin.youtube.RoomYoutube;
import com.websockets.plugin.youtube.VideoYoutube;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.SQLException;

public class EventosGoToRoom extends IncomingMessage {

    public static final String REQUEST_HEADER = "EventosGoToRoom";

    public EventosGoToRoom(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        Habbo habbo = wsclient.getHabbo();

        int room_id = ((Number) this.data.get("room_id")).intValue();

        Emulator.getGameEnvironment().getRoomManager().loadRoom(room_id);
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(room_id);

        if (habbo.getHabboInfo().getCurrentRoom() != null
                && habbo.getHabboInfo().getCurrentRoom().getId() == room.getId()) {
            habbo.whisper("¡Ya estás en la sala del evento!");
            return;
        }

        
        habbo.goToRoom(room.getId());

    }
}