package com.websockets.plugin.websocket.communication.incoming.fetch.looks;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.BarraTop.MejoresLook.LookHabboObjet;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.eu.habbo.habbohotel.users.Habbo;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import java.sql.SQLException;

public class EventVotarLook extends IncomingMessage {

    public static final String REQUEST_HEADER = "EventVotarLook";

    public EventVotarLook(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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

        int lookID = ((Number) this.data.get("look_id")).intValue();

        LookHabboObjet look = WebsocketsManager.getMejoresLooksManager().getLooks().get(lookID);
        if (look != null) {
            look.insertVotoDb(habbo.getHabboInfo().getId());
            look.loadVotosCount();
        }

    }
}