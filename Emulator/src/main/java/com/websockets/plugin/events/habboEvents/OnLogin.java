package com.websockets.plugin.events.habboEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserLoginEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.HabbosManager;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.Habbo.SSOClientManager;

public class OnLogin implements EventListener {

    @EventHandler
    public static void onLogin(UserLoginEvent event) throws SQLException {

        if (event.habbo == null) {
            return;
        }

        if (event.habbo != null) {

            // Registrar el client a los SSO permitidos
            if (SSOClientManager.SSOclients.get(event.habbo.getHabboInfo().getSso()) != null) {
                new HabboClientManager().addClient(SSOClientManager.SSOclients.get(event.habbo.getHabboInfo().getSso()),
                        event.habbo);
            }

            HabbosManager habbosManager = WebsocketsManager.getHabbosManager();
            habbosManager.setHabbos(event.habbo);

            Habbos habbos = habbosManager.getHabbos(event.habbo);
            // Seteando el habbo a la clase
            habbos.setHabbo(event.habbo);

            habbos.setLevel(event.habbo);
            habbos.setHabboTareas(event.habbo);
            habbos.setBarraTop(event.habbo);

        }

    }
}
