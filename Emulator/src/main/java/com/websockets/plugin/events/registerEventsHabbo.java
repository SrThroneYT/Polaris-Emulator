package com.websockets.plugin.events;

import com.eu.habbo.Emulator;
import com.websockets.plugin.events.habboEvents.OnLogin;
import com.websockets.plugin.events.habboEvents.OnLogout;
import com.websockets.plugin.events.habboEvents.onUserEnterRoom;
import com.websockets.plugin.events.habboEvents.onUserExitRoom;
import com.websockets.plugin.events.taskEvents.*;
import com.websockets.plugin.WebsocketsManager;

public class registerEventsHabbo {

        public registerEventsHabbo() {
        }

        public void Init() {

                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new OnLogin());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new OnLogout());

                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new onUserEnterRoom());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new onUserExitRoom());

                // TASK EVENTS
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepOnTaskTileEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserSavedMottoEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepNavigatorCreateRoom());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserPurchasePicture());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserRespectedEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepFurniturePlacedEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserSavedLookEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepFurnitureToggleEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserRequestFriendshipEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepTradeConfirmEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserCatalogFurnitureBoughtEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserTalkEvent());
                Emulator.getPluginManager().registerEvents(WebsocketsManager.INSTANCE,
                                new StepUserExecuteCommandEvent());

        }

}
