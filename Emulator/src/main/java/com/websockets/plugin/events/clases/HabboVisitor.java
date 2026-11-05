package com.websockets.plugin.events.clases;

import com.eu.habbo.habbohotel.users.Habbo;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;

import gnu.trove.map.hash.THashMap;

public class HabboVisitor {

    private THashMap<Habbo, Visitor> visitors = new THashMap<>();

    public HabboVisitor() {

    }

    public THashMap<Habbo, Visitor> getVisitors() {
        return visitors;
    }

    public void registerVisitor(Habbo habbo, Room room) {
        if (!this.visitors.containsKey(habbo)) {
            this.visitors.put(habbo, new Visitor(room));
            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
            habbos.getTareas().progressAchievement(habbo.getClient().getHabbo(), WebsocketsManager.getTareasManager().getTarea("RoomVisit"));
            this.comprobateVisitor(habbo, room);
        } else {
            this.comprobateVisitor(habbo, room);
        }
    }

    public void comprobateVisitor(Habbo habbo, Room room) {
        if (this.visitors.containsKey(habbo)) {

            Visitor visitor = this.visitors.get(habbo);

            if (!visitor.getRooms().containsKey(room.getId())) {
                visitor.getRooms().put(room.getId(), room);

                Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
                habbos.getTareas().progressAchievement(habbo.getClient().getHabbo(), WebsocketsManager.getTareasManager().getTarea("RoomVisit"));
            }

        }
    }

}
