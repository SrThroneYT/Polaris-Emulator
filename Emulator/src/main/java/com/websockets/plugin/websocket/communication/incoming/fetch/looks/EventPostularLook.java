package com.websockets.plugin.websocket.communication.incoming.fetch.looks;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.eu.habbo.habbohotel.users.Habbo;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.SQLException;

public class EventPostularLook extends IncomingMessage {

    public static final String REQUEST_HEADER = "EventPostularLook";

    public EventPostularLook(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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

        String Look = WebsocketsManager.getConfig().getValue("global.look.ruta").replace("%LOOK%",
                habbo.getHabboInfo().getLook())
                + "&headonly=0&size=a&gesture=sml&head_direction=2&action=std";

        WebsocketsManager.getMejoresLooksManager().createLook(habbo.getHabboInfo().getId(),
                Look);

    }
}