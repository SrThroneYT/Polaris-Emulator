package com.websockets.plugin.habbo.BarraTop.Eventos;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.Scheduler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.Outcoming;

public class TimerEventos extends Scheduler {

    private final EventosManager eventosManager;
    private long lastWhisperTime = 0;

    public TimerEventos(EventosManager manager) {
        // Ejecución cada 10 segundos
        super(10);
        this.eventosManager = manager;
    }

    @Override
    public void run() {
        super.run();

        try {
            long currentTime = System.currentTimeMillis() / 1000L;

            String tituloEvento = "";
            boolean hayEventoValidoParaAnunciar = false;

            EventoObjet eventoActivo = null;

            if (eventosManager.getEventos().size() == 0) {
                return; // No hay eventos, salimos
            }

            // 2. Hacemos un recorrido
            for (EventoObjet eventoObjet : eventosManager.getEventos().values()) {

                long eventTimestamp = eventoObjet.getHora();

                // ¿Empezó hace menos de 10 minutos?
                if (currentTime >= eventTimestamp && currentTime <= (eventTimestamp + 600)) {

                    eventoActivo = eventoObjet;

                    hayEventoValidoParaAnunciar = true;
                    tituloEvento = eventoObjet.getTitle();
                    continue;
                }
            }

            // 2. Control de frecuencia (Cada 10 segundos según tu código, aunque es muy
            // rápido)
            if (hayEventoValidoParaAnunciar && (currentTime - lastWhisperTime >= 30)) {

                this.lastWhisperTime = currentTime;
                // 3. Notificar a todos los usuarios
                WebsocketsManager.getHabbosManager().getHabbos().values().forEach(client -> {
                    if (client != null && client.getHabbo() != null) {
                        // Mostramos la burbuja visual (UI)
                        HabboClient wsclient = HabboClientManager.getHabboClient(client.getHabbo());
                        if (wsclient != null) {
                            eventosManager.showBubbleAlert(wsclient, "¡Evento en curso!", 10000);
                        }
                    }
                });
            }

        } catch (Exception e) {
            WebsocketsManager.utilities().LOGGER("Error en TimerEventos: " + e.getMessage());
        }
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public void setDisposed(boolean disposed) {
        this.disposed = disposed;
    }
}