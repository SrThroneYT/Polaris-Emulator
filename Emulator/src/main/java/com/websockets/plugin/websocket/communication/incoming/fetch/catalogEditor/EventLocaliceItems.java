package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

import com.eu.habbo.messages.incoming.handshake.SecureLoginEvent;
import com.eu.habbo.messages.outgoing.habboway.nux.NuxAlertComposer;
import com.google.gson.Gson;
import java.sql.SQLException;

public class EventLocaliceItems extends IncomingMessage {

    public static final String REQUEST_HEADER = "EventLocaliceItems";

    public EventLocaliceItems(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());

        try {
            habbos.getHabboClient()
                    .send(new MessageManager("MessageManager", false,
                            Outcoming.showCataloguesWindow, null));
        } catch (Exception e) {
        }

        if (habbos.getCacheInt("localiceItemActiveCatalogue") == 0) {
            habbos.getHabbo().whisper("Se ha activado el modo localizar objeto en catalogo");
            habbos.getHabbo().whisper("Presiona el furni");

            habbos.registerCache("localiceItemActiveCatalogue", "1");
        } else {
            habbos.getHabbo().whisper("Se ha desactivado el modo localizar objeto");
            habbos.registerCache("localiceItemActiveCatalogue", "0");

            WebsocketsManager.getCatalogWindowManager().setItemId(0);
            WebsocketsManager.getCatalogWindowManager().setPageId(0);
        }

    }
}