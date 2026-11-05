package com.websockets.plugin.websocket.communication.incoming.messages;

import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.BarraTop.Eventos.EventosManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.incoming.IncomingMessage;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.nio.charset.StandardCharsets;

public class CrearEventoEvent extends IncomingMessage {

    public CrearEventoEvent(String header, THashMap<String, Object> data, Channel client) {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() {
        try {
            // Extraer datos del mapa que viene del JSON de React
            // Nota: Dependiendo de tu parseador, los números pueden venir como Double o
            // String
            String title = (String) this.data.get("title");
            String texto = (String) this.data.get("texto");
            String image = (String) this.data.get("image");

            // Conversión segura de timestamp (puede venir como Double desde Gson)
            long timestamp = 0;
            Object tsObj = this.data.get("timestamp");
            if (tsObj instanceof Number) {
                timestamp = ((Number) tsObj).longValue();
            } else if (tsObj instanceof String) {
                timestamp = Long.parseLong((String) tsObj);
            }

            HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

            // 1. Guardar el evento en el Manager (que lo meterá en la lista y/o DB)
            EventosManager.getInstance().crearNuevoEvento(title, texto, image, timestamp, wsclient.getHabbo());

            // 2. Opcional: Responder al cliente que se creó con éxito
            JsonObject response = new JsonObject();
            response.addProperty("header", "EventoCreadoExito");
            response.addProperty("data", "¡El evento ha sido programado!");

            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showMenuEventos,
                    EventosManager.getInstance().getProximosEventos(wsclient.getHabbo())));

            WebsocketsManager.getHabbosManager().getHabbos().values().forEach(client -> {
                if (client != null && client.getHabbo() != null) {

                    // Mostramos la burbuja visual (UI)
                    HabboClient wsclientver = HabboClientManager.getHabboClient(client.getHabbo());
                    if (wsclientver != null) {
                        EventosManager.getInstance().showBubbleAlert(wsclientver, "¡Nuevo evento disponible!", 15000);
                    }
                }
            });

            this.client.writeAndFlush(new BinaryWebSocketFrame(
                    this.client.alloc().buffer().writeBytes(response.toString().getBytes(StandardCharsets.UTF_8))));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}