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

public class EditarEventoEvent extends IncomingMessage {

    public EditarEventoEvent(String header, THashMap<String, Object> data, Channel client) {
        super(header, data, client);
        this.execute();
    }

    @Override
    public void execute() {
        try {
            // 1. Extraer el ID del evento
            int id = 0;
            Object idObj = this.data.get("id");
            if (idObj instanceof Number) {
                id = ((Number) idObj).intValue();
            } else if (idObj instanceof String) {
                id = Integer.parseInt((String) idObj);
            }

            // 2. Extraer el resto de los datos actualizados
            String title = (String) this.data.get("title");
            String texto = (String) this.data.get("texto");
            String image = (String) this.data.get("image");

            long timestamp = 0;
            Object tsObj = this.data.get("timestamp");
            if (tsObj instanceof Number) {
                timestamp = ((Number) tsObj).longValue();
            } else if (tsObj instanceof String) {
                timestamp = Long.parseLong((String) tsObj);
            }

            // 3. Actualizar el evento en el manager
            EventosManager.getInstance().actualizarEvento(id, title, texto, image, timestamp);

            // --- NUEVA LÓGICA DE ALERTA PROXIMIDAD (< 5 MINUTOS) ---
            long tiempoActual = System.currentTimeMillis();
            // 5 minutos en milisegundos: 5 * 60 * 1000 = 300000
            long diferenciaMs = timestamp - tiempoActual;

            if (diferenciaMs > 0 && diferenciaMs <= 300000) {
                WebsocketsManager.getHabbosManager().getHabbos().values().forEach(client -> {
                    if (client != null && client.getHabbo() != null) {
                        HabboClient wsclientver = HabboClientManager.getHabboClient(client.getHabbo());
                        if (wsclientver != null) {
                            EventosManager.getInstance().showBubbleAlert(wsclientver, "¡Evento disponible en breve!",
                                    15000);
                        }
                    }
                });
            }
            // -------------------------------------------------------

            // 4. Notificar éxito al cliente que realiza la edición
            JsonObject response = new JsonObject();
            response.addProperty("header", "EventoEditadoExito");
            response.addProperty("data", "¡El evento ha sido actualizado correctamente!");

            HabboClient wsclient = HabboClientManager.getHabboClient(this.client);

            // 5. Refrescar la lista de eventos en el cliente
            wsclient.send(new MessageManager("MessageManager", true, Outcoming.showMenuEventos,
                    EventosManager.getInstance().getProximosEventos(wsclient.getHabbo())));

            this.client.writeAndFlush(new BinaryWebSocketFrame(
                    this.client.alloc().buffer().writeBytes(response.toString().getBytes(StandardCharsets.UTF_8))));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}