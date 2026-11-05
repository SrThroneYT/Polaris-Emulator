package com.websockets.plugin.events.clases;

import gnu.trove.map.hash.THashMap;
import com.eu.habbo.habbohotel.rooms.Room;

public class Visitor {

    private THashMap<Integer, Room> rooms = new THashMap<>();

    public Visitor(Room room) {
        this.rooms.put(room.getId(), room);
    }

    public THashMap<Integer, Room> getRooms() {
        return rooms;
    }

}
