package com.websockets.plugin.websocket.communication.incoming.messages;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.commands.SexoCommandWS;
import com.websockets.plugin.commands.TocarCommandWS;
import com.websockets.plugin.habbo.BarraTop.BarraTop;
import com.websockets.plugin.habbo.BarraTop.Economia.EconomiaManager;
import com.websockets.plugin.habbo.BarraTop.TopUsers.TopUsersManager;
import com.websockets.plugin.habbo.BarraTop.VIP.VipManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

import java.sql.SQLException;

public class RequestActionEvent extends IncomingMessage {
    public RequestActionEvent(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {

        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);
        if (wsclient == null) {
            return;
        }

        WebsocketsManager.getBotsManager().IncomingListActions(data, wsclient);

        WebsocketsManager.getTareasManager().IncomingListActions(data, wsclient);

        WebsocketsManager.getEventosManager().IncomingListActions(data, wsclient);

        TopUsersManager topUsersManager = new TopUsersManager();
        topUsersManager.IncomingListActions(data, wsclient);

        new VipManager().IncomingListActions(data, wsclient);

        new EconomiaManager().IncomingListActions(data, wsclient);
        WebsocketsManager.getRecompensasManager().IncomingListActions(data, wsclient);

        SexoCommandWS.IncomingListActions(data, wsclient);
        TocarCommandWS.IncomingListActions(data, wsclient);
        WebsocketsManager.getCatalogWindowManager().IncomingListActions(data, wsclient);

        BarraTop.IncomingListActions(data, wsclient);

    }

}
