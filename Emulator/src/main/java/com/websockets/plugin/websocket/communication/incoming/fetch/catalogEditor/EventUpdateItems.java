package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.catalog.*;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceConfigComposer;
import com.eu.habbo.messages.outgoing.habboway.nux.NuxAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomRelativeMapComposer;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventUpdateItems extends IncomingMessage {

    public static final String REQUEST_HEADER = "EventUpdateItems";
    public static final String RESPONSE_HEADER = "CatalogPageDataResponse";

    // Mapa para gestionar el cooldown: Key = Habbo ID, Value = Timestamp en
    // milisegundos
    private static final THashMap<Integer, Long> cooldowns = new THashMap<>();
    private static final long COOLDOWN_TIME = TimeUnit.SECONDS.toMillis(5);

    public EventUpdateItems(String header, THashMap<String, Object> data, Channel client) throws SQLException {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() throws SQLException {
        HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

        if (wsclient == null || wsclient.getHabbo() == null) {
            return;
        }

        int habboId = wsclient.getHabbo().getHabboInfo().getId();
        long currentTime = System.currentTimeMillis();

        // --- VERIFICACIÓN DE COOLDOWN ---
        if (cooldowns.containsKey(habboId)) {
            long lastExecution = cooldowns.get(habboId);
            if (currentTime - lastExecution < COOLDOWN_TIME) {
                long secondsLeft = (COOLDOWN_TIME - (currentTime - lastExecution)) / 1000;
                wsclient.getHabbo().whisper(
                        "Debes esperar " + secondsLeft + " segundos para volver a actualizar los items.",
                        RoomChatMessageBubbles.ALERT);
                return;
            }
        }
        // Actualizar el tiempo de la última ejecución
        cooldowns.put(habboId, currentTime);
        // -------------------------------

        try {
            Object idValue = this.data.get("offer_id");
            if (idValue == null) {
                WebsocketsManager.utilities()
                        .LOGGER("[EventOpenCatalogo] Error: No se encontró el offer_id en los datos.");
                return;
            }

            int pageId = ((Number) idValue).intValue();

            Emulator.getGameEnvironment().getItemManager().loadItems();
            Emulator.getGameEnvironment().getItemManager().loadCrackable();
            Emulator.getGameEnvironment().getItemManager().loadSoundTracks();

            synchronized (Emulator.getGameEnvironment().getRoomManager().getActiveRooms()) {
                for (Room room : Emulator.getGameEnvironment().getRoomManager().getActiveRooms()) {
                    if (room.isLoaded() && room.getUserCount() > 0 && room.getLayout() != null) {
                        room.sendComposer(new RoomRelativeMapComposer(room).compose());
                    }
                }
            }

            wsclient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_items"),
                            RoomChatMessageBubbles.ALERT);

            // Reabrir la página específica tras un breve delay
            Emulator.getThreading().run(() -> {
                wsclient.getHabbo().getClient().sendResponse(new NuxAlertComposer("catalog/open/offerId/" + pageId));
            }, 1000);

            // Tarea de respuesta de datos
            Emulator.getThreading().run(() -> {
                Object pageData = WebsocketsManager.getCatalogWindowManager().getPageData(pageId);
                Object itemBaseData = WebsocketsManager.getCatalogWindowManager().getItemBaseData(pageId);

                THashMap<String, Object> responseData = new THashMap<>();
                responseData.put("header", RESPONSE_HEADER);

                THashMap<String, Object> objetoInner = new THashMap<>();
                objetoInner.put("pageId", pageId);
                objetoInner.put("content", pageData);
                objetoInner.put("itemBase", itemBaseData);

                THashMap<String, Object> dataItem = new THashMap<>();
                dataItem.put("objeto", objetoInner);

                responseData.put("data", new Object[] { dataItem });

                // Nota: Aquí faltaría enviar responseData a través del wsclient si es
                // necesario.
            }, 1500);

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("[EventUpdateItems] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}