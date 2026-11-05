package com.websockets.plugin.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.websockets.plugin.WebsocketsManager;

public class UpdateWSConfig extends Command {

    public UpdateWSConfig(String permission, String[] keys) {
        super(permission, keys);
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {

        WebsocketsManager.getConfig().reload();

        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_ws_config"),
                RoomChatMessageBubbles.ALERT);

        return true;
    }
}
