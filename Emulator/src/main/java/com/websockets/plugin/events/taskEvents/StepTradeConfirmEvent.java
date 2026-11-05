package com.websockets.plugin.events.taskEvents;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.trading.TradeConfirmEvent;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;

public class StepTradeConfirmEvent implements EventListener {

    @EventHandler
    public static void onWalk(TradeConfirmEvent ev) {
        if (ev.isCancelled())
            return;

        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(ev.userOne.getHabbo());
        habbos.getTareas().progressAchievement(habbos.getHabbo().getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("HabboTradeConfirm"));

        Habbos habbos2 = WebsocketsManager.getHabbosManager().getHabbos(ev.userTwo.getHabbo());
        habbos.getTareas().progressAchievement(habbos2.getHabbo().getClient().getHabbo(),
                WebsocketsManager.getTareasManager().getTarea("HabboTradeConfirm"));
    }

}
