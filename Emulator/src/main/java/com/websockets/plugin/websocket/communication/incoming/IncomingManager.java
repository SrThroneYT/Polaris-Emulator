package com.websockets.plugin.websocket.communication.incoming;

import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.EventLocaliceItems;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.EventOpenCatalogo;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.EventUpdateCatalogo;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.EventUpdateItems;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.GetPagesListDataRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.SearchCatalogPageRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.UpdateCatalogItemRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.UpdateCatalogItemsOrderRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.UpdateCatalogPageRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.catalogEditor.UpdateItemBaseRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.eventos.EventosGoToRoom;
import com.websockets.plugin.websocket.communication.incoming.fetch.looks.EventPostularLook;
import com.websockets.plugin.websocket.communication.incoming.fetch.looks.EventVotarLook;
import com.websockets.plugin.websocket.communication.incoming.fetch.looks.FetchMejoresLooks;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.EntregarTareaRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.PaseCreateNewTaskLevelRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.PaseCreateNewTaskRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.PaseDeleteTaskLevelRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.PaseDeleteTaskRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.PaseEditRewardRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.PaseEditTaskRequest;
import com.websockets.plugin.websocket.communication.incoming.fetch.paseBatalla.PaseVisitRoom;
import com.websockets.plugin.websocket.communication.incoming.fetch.youtube.GetRequestYoutubeDataResponse;
import com.websockets.plugin.websocket.communication.incoming.fetch.youtube.InsertYoutubeVideo;
import com.websockets.plugin.websocket.communication.incoming.fetch.youtube.SetVideoActive;
import com.websockets.plugin.websocket.communication.incoming.fetch.youtube.SetVideoClose;
import com.websockets.plugin.websocket.communication.incoming.fetch.youtube.SetVideoPause;
import com.websockets.plugin.websocket.communication.incoming.fetch.youtube.SetVideoPlay;
import com.websockets.plugin.websocket.communication.incoming.fetch.youtube.SetVideoTime;
import com.websockets.plugin.websocket.communication.incoming.messages.*;
// import com.websockets.plugin.websocket.Habbo.HabboClient; // Esta clase no es necesaria aquí

import gnu.trove.map.hash.THashMap;
import io.netty.channel.Channel;

import java.sql.SQLException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class IncomingManager {
    private static IncomingManager instance = null;
    private THashMap<String, Class<? extends IncomingMessage>> messages = new THashMap<>();

    public IncomingManager() {
        this.registerMessages();
    }

    public static IncomingManager getInstance() {
        if (instance == null)
            return instance = new IncomingManager();
        else
            return instance;
    }

    public void registerMessages() {
        // Asegúrate de registrar aquí TODOS los headers que esperas recibir del cliente
        this.messages.put("SSOTicketEvent", SSOTicketEvent.class);
        this.messages.put("PingEvent", PingEvent.class);
        this.messages.put("MessagesManager", MessagesManager.class);
        this.messages.put("RequestActionEvent", RequestActionEvent.class);

        // En IncomingManager.java, método registerMessages()
        this.messages.put(GetLevelDataRequest.REQUEST_HEADER, GetLevelDataRequest.class);

        this.messages.put("crearEvento", CrearEventoEvent.class);
        this.messages.put("editarEvento", EditarEventoEvent.class);

        // Nuevos catalog editor
        this.messages.put("GetPagesListDataRequest", GetPagesListDataRequest.class);
        this.messages.put("SearchCatalogPageRequest", SearchCatalogPageRequest.class);
        this.messages.put("UpdateCatalogItemRequest", UpdateCatalogItemRequest.class);
        this.messages.put("UpdateCatalogItemsOrderRequest", UpdateCatalogItemsOrderRequest.class);
        this.messages.put("UpdateCatalogPageRequest", UpdateCatalogPageRequest.class);
        this.messages.put("UpdateItemBaseRequest", UpdateItemBaseRequest.class);
        //
        this.messages.put("EventUpdateCatalogo", EventUpdateCatalogo.class);
        this.messages.put("EventOpenCatalogo", EventOpenCatalogo.class);
        this.messages.put("EventUpdateItems", EventUpdateItems.class);
        this.messages.put("EventLocaliceItems", EventLocaliceItems.class);

        // Youtube
        this.messages.put("GetRequestYoutubeDataResponse", GetRequestYoutubeDataResponse.class);
        this.messages.put("InsertYoutubeVideo", InsertYoutubeVideo.class);
        this.messages.put("SetVideoActive", SetVideoActive.class);
        this.messages.put("SetVideoPause", SetVideoPause.class);
        this.messages.put("SetVideoPlay", SetVideoPlay.class);
        this.messages.put("SetVideoClose", SetVideoClose.class);
        this.messages.put("SetVideoTime", SetVideoTime.class);

        // EVENTOS
        this.messages.put("EventosGoToRoom", EventosGoToRoom.class);

        // LOOKS
        this.messages.put("FetchMejoresLooks", FetchMejoresLooks.class);
        this.messages.put("EventPostularLook", EventPostularLook.class);
        this.messages.put("EventVotarLook", EventVotarLook.class);

        // Pase de batallas
        this.messages.put("PaseCreateNewTaskRequest", PaseCreateNewTaskRequest.class);
        this.messages.put("PaseEditTaskRequest", PaseEditTaskRequest.class);
        this.messages.put("PaseDeleteTaskRequest", PaseDeleteTaskRequest.class);
        this.messages.put("PaseVisitRoom", PaseVisitRoom.class);
        this.messages.put("PaseEditRewardRequest", PaseEditRewardRequest.class);
        this.messages.put("EntregarTareaRequest", EntregarTareaRequest.class);
        this.messages.put("PaseCreateNewTaskLevelRequest", PaseCreateNewTaskLevelRequest.class);
        this.messages.put("PaseDeleteTaskLevelRequest", PaseDeleteTaskLevelRequest.class);

    }

    public THashMap<String, Class<? extends IncomingMessage>> getMessages() {
        return this.messages;
    }

    // ----------------------------------------------------------------------
    // ⭐️ MÉTODO PARA RECIBIR Y PROCESAR LA PETICIÓN ⭐️
    // ----------------------------------------------------------------------

    /**
     * Busca la clase de mensaje por su header y crea una nueva instancia para
     * procesarla.
     * 
     * @param header El nombre del evento que viene en la petición JSON.
     * @param data   El mapa de datos (cuerpo) recibido del cliente.
     * @param client El canal Netty del cliente.
     * @throws SQLException Si la ejecución del mensaje provoca una excepción de
     *                      SQL.
     */
    public void handleMessage(String header, THashMap<String, Object> data, Channel client) throws SQLException {

        Class<? extends IncomingMessage> messageClass = this.messages.get(header);

        if (messageClass == null) {
            System.err.println("Advertencia: No se encontró la clase de mensaje registrada para el header: " + header);
            return;
        }

        try {
            // 1. Obtener el constructor que coincide con la firma de IncomingMessage:
            // (String header, THashMap<String, Object> data, Channel client)
            Constructor<? extends IncomingMessage> constructor = messageClass.getConstructor(
                    String.class,
                    THashMap.class,
                    Channel.class);

            // 2. Crear una nueva instancia de la clase, pasando los parámetros requeridos.
            // Esto ejecuta el constructor que, a su vez, llama a .execute().
            constructor.newInstance(header, data, client);

        } catch (NoSuchMethodException e) {
            System.err.println("Error de Reflexión: La clase '" + messageClass.getSimpleName()
                    + "' no tiene el constructor: (String, THashMap, Channel).");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // Si el constructor lanza una excepción (como SQLException desde .execute()),
            // la relanzamos.
            if (e.getTargetException() instanceof SQLException) {
                throw (SQLException) e.getTargetException();
            }
            System.err.println("Error de Ejecución al instanciar mensaje: " + e.getCause().getMessage());
            e.printStackTrace();
        } catch (InstantiationException | IllegalAccessException e) {
            System.err.println("Error al crear la instancia del mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}