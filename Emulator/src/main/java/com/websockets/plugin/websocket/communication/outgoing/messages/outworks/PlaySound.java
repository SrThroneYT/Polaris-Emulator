package com.websockets.plugin.websocket.communication.outgoing.messages.outworks;

import com.eu.habbo.habbohotel.users.Habbo;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.communication.outgoing.OutgoingMessage;

import java.sql.SQLException;

public class PlaySound extends OutgoingMessage {
    public PlaySound(String header, boolean show, String sound, Habbo habbo) throws SQLException {

        super("PlaySound");

        this.data.addProperty("show_button", show);
        this.data.addProperty("sound", sound);
        this.data.addProperty("url_sound",
                WebsocketsManager.getConfig().getValue("global_url_images").replace("frank/", "") + "sounds/");
        this.data.addProperty("volumen_sound", 0.30);

        // this.data.add("tasks", tasksobj);
        this.dataArray.add(this.data);
        this.outgoing.addProperty("header", header);
        this.outgoing.add("data", this.dataArray);
    }

    @Override
    public String execute() {
        return this.outgoing.toString();
    }
}
