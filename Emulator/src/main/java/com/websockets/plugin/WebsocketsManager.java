package com.websockets.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.ItemInteraction;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.messages.outgoing.rooms.RoomRelativeMapComposer;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadItemsManagerEvent;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import com.websockets.plugin.bots.BotsManager;
import com.websockets.plugin.events.registerEventsHabbo;
import com.websockets.plugin.events.clases.HabboVisitor;
import com.websockets.plugin.habbo.Tareas.TareasManager;
import com.websockets.plugin.utilities.checkDatabase;
import com.websockets.plugin.utilities.checkDataSettings;
import com.websockets.plugin.utilities.checkDataTexts;
import com.websockets.plugin.utilities.checkDataUsers;
import com.websockets.plugin.utilities.loadConfig;
import com.websockets.plugin.utilities.loadPlayerCommands;
import com.websockets.plugin.utilities.loadTexts;
import com.websockets.plugin.utilities.utils.comprobateUsePlugin;
import com.websockets.plugin.websocket.WebSocketServer;
import com.websockets.plugin.websocket.Habbo.SSOClientManager;
import com.websockets.plugin.websocket.communication.incoming.IncomingManager;
import com.websockets.plugin.youtube.YoutubeRoomManager;
import com.websockets.plugin.handled.handleUserClickWs;
import com.websockets.plugin.handled.handledFurniClick;
import com.websockets.plugin.interactions.wireds.effects.WiredEffectGiveTaskPaseCustomNitroV3;
import com.websockets.plugin.habbo.HabbosManager;
import com.websockets.plugin.habbo.BarraTop.CatalogWindow.CatalogWindowManager;
import com.websockets.plugin.habbo.BarraTop.Eventos.EventosManager;
import com.websockets.plugin.habbo.BarraTop.MejoresLook.MejoresLooksManager;
import com.websockets.plugin.habbo.BarraTop.Recompensas.RecompensasManager;
import com.websockets.plugin.habbo.Tareas.Level.LevelManager;
import com.websockets.plugin.utilities.Utilities;

public class WebsocketsManager extends HabboPlugin implements EventListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(Emulator.class);

    // HABBOS MANAGER
    private static HabbosManager habbosManager;

    public static WebsocketsManager INSTANCE = null;
    private static checkDataSettings config;
    private static checkDataTexts text;
    private static checkDataUsers data;
    private static Utilities utils;
    private static LevelManager levelManager;
    private static YoutubeRoomManager youtubeRoomManager;
    private static EventosManager eventosManager;
    private static RecompensasManager recompensasManager;
    private static BotsManager botsManager;

    private static CatalogWindowManager catalogWindowManager;
    private static MejoresLooksManager mejoresLooksManager;
    private static HabboVisitor habboVisitor;

    // Nuevas tareas
    private static TareasManager tareasManager;

    public void onEnable() throws Exception {

        Emulator.getPluginManager().registerEvents(this, this);
        if (Emulator.isReady && !Emulator.isShuttingDown) {
            this.onEmulatorLoadedEvent(null);
        }

        INSTANCE = this;
        new registerEventsHabbo().Init();

        config = new checkDataSettings();
        config.loadFromDatabase();
        text = new checkDataTexts();
        text.loadFromDatabase();
        utils = new Utilities();
    }

    public void onRegisterConfigs() {
        try {
            // Cargar y registrar los permisos
            checkDatabase.ICheckDatabase();
            // Cargar los comandos
            loadPlayerCommands.ILoadPlayerCommands();

            // Cargar y registrar las configuraciones
            loadConfig.ILoadConfig();

            // Cargar y registrar los textos
            loadTexts.ILoadTexts();

        } catch (Exception err) {
        }
    }

    @EventHandler
    public void onEmulatorLoadedEvent(EmulatorLoadedEvent e) throws Exception {

        this.onRegisterConfigs();

        Emulator.getConfig().register("websockets_srthrone.ws.host", "0.0.0.0");
        Emulator.getConfig().register("websockets_srthrone.ws.port", "2083");

        // Inicializar el manager de Habbos
        habbosManager = new HabbosManager();
        habbosManager.update();

        LOGGER.info("---------------------------------------------------");
        LOGGER.info("[WEBSOCKETS BY SRTHRONE] | Procesando...");
        LOGGER.info("---------------------------------------------------");

        // Comprobar permisos del plugin
        comprobateUsePlugin comprobateUse = new comprobateUsePlugin();
        comprobateUse.loadHotelesPermited();
        comprobateUsePlugin.comprobateLoad();

        WebSocketServer webSocketServer = new WebSocketServer();
        webSocketServer.initializePipeline();
        webSocketServer.connect();
        IncomingManager.getInstance().registerMessages();

        SSOClientManager.SSOclients.clear();

        tareasManager = new TareasManager();
        tareasManager.update();

        levelManager = new LevelManager();

        youtubeRoomManager = new YoutubeRoomManager();

        eventosManager = new EventosManager();
        recompensasManager = new RecompensasManager();

        botsManager = new BotsManager();
        botsManager.insertTexts();

        catalogWindowManager = new CatalogWindowManager();

        mejoresLooksManager = new MejoresLooksManager();

        habboVisitor = new HabboVisitor();

        ICallable onClickUser = new handleUserClickWs();
        Emulator.getGameServer().getPacketManager().registerCallable(2138, onClickUser);

        ICallable onClickFurni = new handledFurniClick();
        Emulator.getGameServer().getPacketManager().registerCallable(3288, onClickFurni);

        // Registrar interacciones

        this.updateItemBaseInteraction("wf_act_give_custom_task_pase", "wf_act_give_custom_task_pase");

        Emulator.getGameEnvironment().getItemManager().addItemInteraction(
                new ItemInteraction("wf_act_give_custom_task_pase", WiredEffectGiveTaskPaseCustomNitroV3.class));

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
    }

    @Override
    public void onDisable() throws Exception {
    }

    public static boolean UpdateAllRoomsContentItem(String base_name, String extra_data, double z) {
        Item item = Emulator.getGameEnvironment().getItemManager().getItem(base_name);
        if (item == null) {
            return false;
        }
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement("SELECT * FROM items WHERE item_id= ? AND room_id!= ?")) {
            statement.setInt(1, item.getId());
            statement.setInt(2, 0);
            if (statement.execute()) {
                try (ResultSet set = statement.getResultSet()) {
                    while (set.next()) {

                        Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(set.getInt("room_id"));
                        if (room != null) {

                            if (room.getCategory() != WebsocketsManager.getConfig()
                                    .getInt("global_room.category.roleplay.id")) {
                                continue;
                            }

                            try (Connection conn2 = Emulator.getDatabase().getDataSource().getConnection()) {
                                try (PreparedStatement statement2 = conn2
                                        .prepareStatement("UPDATE items SET extra_data= ?, z= ? WHERE id = ?")) {
                                    statement2.setString(1, extra_data);
                                    statement2.setString(2, z + "");
                                    statement2.setInt(3, set.getInt("id"));
                                    statement2.executeUpdate();
                                }

                            } catch (SQLException exception) {
                                exception.printStackTrace();
                            }
                        }

                    }
                }
            }
        } catch (SQLException e) {
        }

        return false;
    }

    public void updateItemBaseInteraction(String name_item, String interaction_type) {
        Item el_item = Emulator.getGameEnvironment().getItemManager().getItem(name_item);
        if (el_item != null) {

            if (el_item.getInteractionType().getName().equalsIgnoreCase(interaction_type)) {
                return;
            }

            int id_furni = el_item.getId();

            try (Connection conn = Emulator.getDatabase().getDataSource().getConnection()) {
                try (PreparedStatement statement = conn
                        .prepareStatement("UPDATE items_base SET interaction_type= ? WHERE id = ?")) {
                    statement.setString(1, interaction_type);
                    statement.setInt(2, id_furni);
                    statement.executeUpdate();
                    statement.close();
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static TareasManager getTareasManager() {
        return tareasManager;
    }

    public static HabbosManager getHabbosManager() {
        return habbosManager;
    }

    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }

    public static checkDataSettings getConfig() {
        return config;
    }

    public static checkDataTexts getTexts() {
        return text;
    }

    public static checkDataUsers getConfigUser(int user_id) {
        WebsocketsManager.data = new checkDataUsers(user_id);
        return data;
    }

    public static Utilities utilities() {
        return utils;
    }

    public static LevelManager getLevelManager() {
        return levelManager;
    }

    public static YoutubeRoomManager getYoutubeRoomManager() {
        return youtubeRoomManager;
    }

    public static EventosManager getEventosManager() {
        return eventosManager;
    }

    public static RecompensasManager getRecompensasManager() {
        return recompensasManager;
    }

    public static BotsManager getBotsManager() {
        return botsManager;
    }

    public static CatalogWindowManager getCatalogWindowManager() {
        return catalogWindowManager;
    }

    public static MejoresLooksManager getMejoresLooksManager() {
        return mejoresLooksManager;
    }

    public static HabboVisitor getHabboVisitor() {
        return habboVisitor;
    }
}