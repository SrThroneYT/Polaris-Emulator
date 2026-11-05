package com.websockets.plugin.events.taskEvents;

import java.sql.SQLException;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.users.UserTakeStepEvent;
import com.websockets.plugin.clickFurnis.CajeroFurni;

public class StepOnTaskTileEvent implements EventListener {

    @EventHandler
    public static void onWalk(UserTakeStepEvent ev) throws SQLException {
        if (ev.isCancelled())
            return;

        CajeroFurni.ocultVentana(ev.habbo);

    }
}
