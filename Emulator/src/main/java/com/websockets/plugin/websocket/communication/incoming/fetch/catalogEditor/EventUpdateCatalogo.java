package com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor;

import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.catalog.*;
import com.eu.habbo.messages.outgoing.catalog.marketplace.MarketplaceConfigComposer;
import com.eu.habbo.messages.outgoing.habboway.nux.NuxAlertComposer;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventUpdateCatalogo extends IncomingMessage {

    public static final String REQUEST_HEADER = "EventUpdateCatalogo";
    public static final String RESPONSE_HEADER = "CatalogPageDataResponse";

    // Mapa para gestionar el cooldown: Key = Habbo ID, Value = Timestamp en
    // milisegundos
    private static final THashMap<Integer, Long> cooldowns = new THashMap<>();
    private static final long COOLDOWN_TIME = TimeUnit.SECONDS.toMillis(5);

    public EventUpdateCatalogo(String header, THashMap<String, Object> data, Channel client) throws SQLException {
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
                        "Debes esperar " + secondsLeft + " segundos para volver a actualizar el catálogo.",
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

            // Ocultar catálogo para todos los usuarios online (Broadcast local)
            for (Map.Entry<Integer, Habbo> map : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos()
                    .entrySet()) {
                Habbo habbo = map.getValue();
                if (habbo == null || !habbo.isOnline())
                    continue;
                habbo.getClient().sendResponse(new NuxAlertComposer("catalog/hide"));
            }

            // Reinicializar el catálogo en el emulador
            Emulator.getGameEnvironment().getCatalogManager().initialize();

            // Enviar paquetes de actualización a todos
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new CatalogUpdatedComposer());
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new CatalogModeComposer(0));
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new DiscountComposer());
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new MarketplaceConfigComposer());
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new GiftConfigurationComposer());
            Emulator.getGameServer().getGameClientManager().sendBroadcastResponse(new RecyclerLogicComposer());
            Emulator.getGameEnvironment().getCraftingManager().reload();

            wsclient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_catalog"),
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
            WebsocketsManager.utilities().LOGGER("[EventUpdateCatalogo] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}