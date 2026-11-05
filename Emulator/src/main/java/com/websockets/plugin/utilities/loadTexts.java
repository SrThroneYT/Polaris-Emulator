package com.websockets.plugin.utilities;

import com.websockets.plugin.WebsocketsManager;

public class loadTexts {

        public static void ILoadTexts() throws Exception {

                WebsocketsManager.getTexts().register("normal_pase_tarea_quest_completed",
                                "Acabas de completar una tarea.");
                WebsocketsManager.getTexts().register("normal_pase_title_window",
                                "PASE DE BATALLA");
                WebsocketsManager.getTexts().register("normal_pase_title_part_1", "MI EXPERIENCIA");
                WebsocketsManager.getTexts().register("normal_pase_title_part_2",
                                "PREMIOS POR SUBIR DE NIVEL");
                WebsocketsManager.getTexts().register("normal_pase_title_part_3",
                                "SELECCIONA EL TIPO DE TAREAS");
                WebsocketsManager.getTexts().register("normal_pase_title_part_4",
                                "TIPO DE TAREAS SELECCIONADAS");
                WebsocketsManager.getTexts().register("normal_pase_title_part_5", "LISTADO DE TAREAS");

                // TITULOS PARA EL TOP
                WebsocketsManager.getTexts().register("top_titles_format", "Mas %TOP%");

        }

}
