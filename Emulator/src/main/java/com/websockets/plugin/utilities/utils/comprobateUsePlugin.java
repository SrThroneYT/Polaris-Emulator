package com.websockets.plugin.utilities.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.eu.habbo.Emulator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.websockets.plugin.WebsocketsManager;

import gnu.trove.map.hash.THashMap;

public class comprobateUsePlugin {

    public static THashMap<String, String> listaPermitida = new THashMap<String, String>();

    public void loadHotelesPermited() {

        String url = Emulator.getConfig().getValue("camera.url");
        int terceraBarra = url.indexOf("/", url.indexOf("/", url.indexOf("/") + 1) + 1);
        String linkCameraHotel = "";
        if (terceraBarra != -1) {
            linkCameraHotel = url.substring(0, url.indexOf("/", url.indexOf("/", url.indexOf("/") + 1) + 1));
        } else {
            int segundaBarra = url.indexOf("/", url.indexOf("/") + 1);
            if (segundaBarra != -1) {
                linkCameraHotel = url.substring(0, segundaBarra);
            }
        }

        if (linkCameraHotel.equalsIgnoreCase("http://localhost")
                || linkCameraHotel.equalsIgnoreCase("http://26.49.211.101")) {
            listaPermitida.put(linkCameraHotel, linkCameraHotel);
            return;
        }

        try {
            URL urlObj = new URL("https://srthroneyt.github.io/websockets_hoteles/hoteles.json");
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Error al obtener datos de la URL. Código de respuesta: " + responseCode);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();
            String jsonResponse = response.toString();
            JsonElement jsonElement = JsonParser.parseString(jsonResponse);
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();

                for (JsonElement element : jsonArray) {
                    JsonObject hotelesJson = element.getAsJsonObject();
                    if (hotelesJson.get("url").getAsString().equalsIgnoreCase(linkCameraHotel)) {
                        listaPermitida.put(linkCameraHotel, linkCameraHotel);
                    }
                }

            } else if (jsonElement.isJsonObject()) {
            } else {
                System.out.println("El JSON response no es ni un array ni un objeto.");
            }
        } catch (IOException error) {
            System.err.println("Error al obtener o procesar los datos: " + error.getMessage());
        }

    }

    public static Boolean execute() {

        Boolean result = false;

        String url = Emulator.getConfig().getValue("camera.url");
        int terceraBarra = url.indexOf("/", url.indexOf("/", url.indexOf("/") + 1) + 1);
        String linkCameraHotel = "";
        if (terceraBarra != -1) {
            linkCameraHotel = url.substring(0, url.indexOf("/", url.indexOf("/", url.indexOf("/") + 1) + 1));
        } else {
            int segundaBarra = url.indexOf("/", url.indexOf("/") + 1);
            if (segundaBarra != -1) {
                linkCameraHotel = url.substring(0, segundaBarra);
            }
        }

        if (listaPermitida.size() == 0) {
            WebsocketsManager.utilities().LOGGER("\r\n");
            WebsocketsManager.utilities().LOGGER_ERROR("[WEBSOCKETS BY SRTHRONE] | TU HOTEL: "
                    + linkCameraHotel + " NO TIENE PERMITIDO EL USO DEL PLUGIN.");
            WebsocketsManager.utilities().LOGGER_ERROR(
                    "[WEBSOCKETS BY SRTHRONE] | El plugin WebSocketsBySrThrone-1.0 fue desactivado debido a que es ilegal");
            WebsocketsManager.utilities().LOGGER_ERROR(
                    "[WEBSOCKETS BY SRTHRONE] | Muchas gracias por permitir la prueba, debera eliminar el plugin de la carpeta de plugins para continuar...");
            WebsocketsManager.utilities().LOGGER_ERROR(
                    "[WEBSOCKETS BY SRTHRONE] | Contacta con el creador en caso de interesarle (Discord: SrThrone#5868)");
            WebsocketsManager.utilities().LOGGER("\r\n");
            new disablePlugin().execute();
        }

        if (listaPermitida.get(linkCameraHotel) != null) {
            return result = true;
        }

        return result;
    }

    public static void comprobateLoad() {
        if (execute() == false) {
            Emulator.prepareShutdown();
            Emulator.isShuttingDown = true;
            Emulator.isReady = false;
        } else {
            WebsocketsManager.utilities().LOGGER("[WEBSOCKETS BY SRTHRONE] | CARGADO EXITOSAMENTE!");
            WebsocketsManager.utilities().LOGGER("---------------------------------------------------");
        }
    }

    public static void comprobateShutdown() {
        if (execute() == false) {
            Emulator.prepareShutdown();
            Emulator.isShuttingDown = true;
            Emulator.isReady = false;
        }
    }
}
