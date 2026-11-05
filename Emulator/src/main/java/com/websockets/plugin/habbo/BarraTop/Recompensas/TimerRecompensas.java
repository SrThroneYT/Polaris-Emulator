package com.websockets.plugin.habbo.BarraTop.Recompensas;

import com.eu.habbo.Emulator;
import com.eu.habbo.core.Scheduler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.BarraTop.Eventos.EventosManager;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.Habbo.HabboClientManager;
import com.websockets.plugin.websocket.communication.Outcoming;

public class TimerRecompensas extends Scheduler {

    private final RecompensasManager recompensasManager;
    private long lastWhisperTime = 0;

    public TimerRecompensas(RecompensasManager recompensasManager) {
        // Ejecución cada 30 segundos
        super(recompensasManager.intervalTimeRecompensas);
        this.recompensasManager = recompensasManager;
    }

    @Override
    public void run() {
        super.run();

        try {
            long currentTime = System.currentTimeMillis() / 1000L;

            // 3. Notificar a todos los usuarios
            WebsocketsManager.getHabbosManager().getHabbos().values().forEach(client -> {
                if (client != null && client.getHabbo() != null) {

                    // Mostramos la burbuja visual (UI)
                    HabboClient wsclient = HabboClientManager.getHabboClient(client.getHabbo());
                    if (wsclient != null) {
                        recompensasManager.showBubbleAlert(wsclient, "¡Confirma tu recompensa!", 5000);

                        String rutaImagesNotifications = WebsocketsManager.getConfig()
                                .getValue("global_url.hotel.notifications") + "/websocketsBySrThrone";

                        int segundos = recompensasManager.intervalTimeRecompensas;
                        String timeDisplay;

                        if (segundos < 3600) {
                            // Si el resto es 0, es un entero (ej: 10), si no, muestra 1 decimal (ej: 10.5)
                            double minutos = segundos / 60.0;
                            timeDisplay = (minutos == (int) minutos) ? String.valueOf((int) minutos)
                                    : String.valueOf(minutos);
                            timeDisplay += " minutos";
                        } else {
                            double horas = segundos / 3600.0;
                            timeDisplay = (horas == (int) horas) ? String.valueOf((int) horas) : String.valueOf(horas);
                            timeDisplay += " horas";
                        }

                        // Aplicación en el mensaje
                        String stringMoneda = "seasonal.name." + recompensasManager.type_moneda;
                        String nameMoneda = Emulator.getTexts().getValue(stringMoneda);
                        int cantidadMoneda = recompensasManager.cantidad_moneda;

                        if (wsclient.getHabbo().getHabboInfo().getHabboStats().getClubExpireTimestamp() >= Emulator
                                .getIntUnixTimestamp()) {
                            cantidadMoneda = cantidadMoneda * 2;
                        }

                        recompensasManager.showAlertRecompensa(wsclient,
                                "¿Quieres recibir estos %name_moneda%?".replace("%name_moneda%", nameMoneda),
                                "Confirma para recibir tus %cantidad% %name_moneda% de recompensa por estar conectado durante %time%"
                                        .replace("%cantidad%", String.valueOf(cantidadMoneda))
                                        .replace("%name_moneda%", nameMoneda)
                                        .replace("%time%", timeDisplay),
                                rutaImagesNotifications + "/economia/creditos.png",
                                60000, "eventSuccessPoints");
                    }

                    // Susurro en el hotel
                    // client.getHabbo()
                    // .whisper("¡Atención! El evento '" + finalTitulo + "' está ocurriendo ahora.
                    // ¡Únete!");
                }
            });

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