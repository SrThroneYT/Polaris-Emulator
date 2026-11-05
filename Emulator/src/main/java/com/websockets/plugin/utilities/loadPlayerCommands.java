package com.websockets.plugin.utilities;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.websockets.plugin.commands.SexoCommandWS;
import com.websockets.plugin.commands.TocarCommandWS;
import com.websockets.plugin.commands.UpdateWSConfig;
import com.websockets.plugin.commands.UpdateWSTexts;

public class loadPlayerCommands {

        public static void ILoadPlayerCommands() {

                // Comandos para actualizar
                CommandHandler.addCommand(new UpdateWSConfig("cmd_update_ws_config",
                                Emulator.getTexts().getValue("commands.keys.cmd_update_ws_config").split(";")));
                CommandHandler.addCommand(new UpdateWSTexts("cmd_update_ws_text",
                                Emulator.getTexts().getValue("commands.keys.cmd_update_ws_text").split(";")));

                // NUEVOS COMANDOS
                CommandHandler.addCommand(new SexoCommandWS("cmd_sexo_ws",
                                Emulator.getTexts().getValue("commands.keys.cmd_sexo_ws").split(";")));
                CommandHandler.addCommand(new TocarCommandWS("cmd_tocar_ws",
                                Emulator.getTexts().getValue("commands.keys.cmd_tocar_ws").split(";")));

        }

}
