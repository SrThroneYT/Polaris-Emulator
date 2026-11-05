package com.websockets.plugin.handled;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.websockets.plugin.WebsocketsManager;

public class handledFurniClick implements ICallable {

    private int increment = 0;
    GameClient client;

    @Override
    public void call(MessageHandler event) {

        this.client = event.client;

        ClientMessage packetCopy;
        int furni = event.packet.readInt(); // si se obtiene segundo parametro
        try {
            packetCopy = event.packet.clone();
            furni = packetCopy.readInt();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        increment++;
        if (increment >= 2) {
            increment = 0;
            return;
        }

        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null)
            return;

        HabboItem item = room.getHabboItem(furni);
        if (item == null)
            return;

        WebsocketsManager.getCatalogWindowManager().localizarItemEvent(this.client.getHabbo(), item);

    }
}