package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

import com.eu.habbo.messages.incoming.handshake.SecureLoginEvent;
import com.eu.habbo.messages.outgoing.habboway.nux.NuxAlertComposer;
import com.google.gson.Gson;
import java.sql.SQLException;

public class EventOpenCatalogo extends IncomingMessage {

    public static final String REQUEST_HEADER = "EventOpenCatalogo";

    public EventOpenCatalogo(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        try {
            Object idValue = this.data.get("offer_id");
            if (idValue == null) {
                WebsocketsManager.utilities()
                        .LOGGER("[EventOpenCatalogo] Error: No se encontró el offer_id en los datos.");
                return;
            }
            int pageId = ((Number) idValue).intValue();
            wsclient.getHabbo().getClient().sendResponse(new NuxAlertComposer("catalog/open/offerId/" + pageId));
        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[EventOpenCatalogo] Error crítico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}