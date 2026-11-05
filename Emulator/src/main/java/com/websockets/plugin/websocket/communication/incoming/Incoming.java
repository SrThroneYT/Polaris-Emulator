package com.websockets.plugin.websocket.communication.incoming;

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException; // Importar SQLException
import java.util.Arrays; // Utilidad de Arrays

import com.websockets.plugin.utilities.utils.GSON; // Importación correcta de GSON

public class Incoming {

    public Incoming(String incoming, Channel client) { // Se remueven las excepciones checked

        IncomingData incomingData = null;

        // 1. Deserializar el mensaje
        try {
            incomingData = GSON.getGson().fromJson(incoming, IncomingData.class);
        } catch (Exception e) {
            // Manejo de error de formato JSON
            System.err.println("Error al deserializar JSON: " + incoming);
            e.printStackTrace();
            return; // Detener el procesamiento si el JSON es inválido
        }

        // Verificación básica del mensaje
        if (incomingData == null || incomingData.header == null || incomingData.data == null
                || incomingData.data.length == 0) {
            System.err.println("Advertencia: Mensaje entrante incompleto o inválido. Header: " + incomingData.header);
            return;
        }

        try {
            // 2. Buscar la clase y obtener el constructor
            Class<? extends IncomingMessage> aClass = IncomingManager.getInstance().getMessages()
                    .get(incomingData.header);

            if (aClass == null) {
                System.err.println("Advertencia: Header no registrado: " + incomingData.header);
                return;
            }

            // Firma del constructor: (String header, THashMap data, Channel client)
            aClass.getDeclaredConstructor(String.class, THashMap.class, Channel.class)
                    // 3. Instanciar y ejecutar el mensaje (el constructor lo ejecuta)
                    .newInstance(incomingData.header, incomingData.data[0], client);

        } catch (NoSuchMethodException e) {
            System.err.println("Error: Constructor no encontrado para la clase: " + incomingData.header);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // La excepción que lanza .execute() está envuelta aquí.
            Throwable actualException = e.getCause();

            if (actualException instanceof SQLException) {
                System.err.println(
                        "ERROR SQL al ejecutar mensaje " + incomingData.header + ": " + actualException.getMessage());
                // Aquí podrías cerrar la conexión o notificar al cliente sobre el error.
            } else {
                System.err.println("ERROR inesperado al ejecutar mensaje " + incomingData.header + ": "
                        + actualException.getMessage());
            }
            actualException.printStackTrace();
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("Error de acceso o instanciación para la clase: " + incomingData.header);
            e.printStackTrace();
        }
        // Ya no necesitamos capturar SQLException aquí, ya que está capturada en
        // InvocationTargetException.
    }

    // El objeto IncomingData (NO requiere cambios, es la estructura de tu petición
    // JS)
    private static class IncomingData {
        public String header;

        // Nota: Esto asume que el JSON siempre envía un array [ { ...datos... } ]
        public THashMap<String, Object>[] data;
    }
}