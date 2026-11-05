package com.websockets.plugin.events.habboEvents;

import java.sql.SQLException;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserDisconnectEvent;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.Habbo.SSOClientManager;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.TareasManager;

public class OnLogout implements EventListener {

    @EventHandler
    public static void eventLogout(UserDisconnectEvent event) throws SQLException {

        Habbo habbo = event.habbo;

        if (habbo == null) {
            return;
        }

        if (habbo != null)

            // vaciar caches
            habbo.getHabboStats().cache.clear();

        TareasManager.saveAchievements(event.habbo);

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(event.habbo);
        HabboClient client = habbos.getHabboClient();
        if (client != null) {
            HabboClientManager.killClient(client.getChannel().id());
        }

    }

}
