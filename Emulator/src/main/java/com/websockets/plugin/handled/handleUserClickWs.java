package com.websockets.plugin.handled;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.messenger.Messenger;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.users.ProfileFriendsComposer;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.handled.events.onClickBot;

public class handleUserClickWs implements ICallable {

    private int increment = 0;

    @Override
    public void call(MessageHandler event) {

        ClientMessage packetCopy;
        int user_id = 0;
        try {
            packetCopy = event.packet.clone();
            // Usar la función de búsqueda que propuse:
            int valid_user_id = readNonZeroInt(packetCopy);
            user_id = valid_user_id;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        GameClient client = event.client;

        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(user_id);
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();

        if (habbo != null) {
            client.sendResponse(new ProfileFriendsComposer(habbo));
        } else {

            Bot bot = room.getBot(Math.abs(user_id));

            // Cuando se clickee a un bot
            if (bot != null) {

                increment++;
                if (increment >= 2) {
                    increment = 0;
                    return;
                }

                new onClickBot(bot);
            } else {
                client.sendResponse(new ProfileFriendsComposer(Messenger.getFriends(user_id), user_id));
            }
        }

    }

    // Función helper para leer el primer entero no nulo (de una copia)
    private int readNonZeroInt(ClientMessage packet) {
        final int MAX_ATTEMPTS = 4;
        int nonZeroId = 0;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            try {
                int currentId = packet.readInt();
                if (currentId != 0) {
                    nonZeroId = currentId;
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return nonZeroId;
    }
}