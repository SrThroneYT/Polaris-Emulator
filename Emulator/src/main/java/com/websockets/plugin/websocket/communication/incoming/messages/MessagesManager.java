package com.websockets.plugin.websocket.communication.incoming.messages;

import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

import java.sql.SQLException;

public class MessagesManager extends IncomingMessage {
    public MessagesManager(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {

        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);
        if (wsclient == null) {
            return;
        }

        // Obtener los datos recibidos y comprobar si existen acá

    }
}
