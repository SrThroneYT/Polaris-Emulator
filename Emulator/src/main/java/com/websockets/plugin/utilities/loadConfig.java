package com.websockets.plugin.utilities;

import com.websockets.plugin.WebsocketsManager;

public class loadConfig {

        public static void ILoadConfig() throws Exception {

                WebsocketsManager.getConfig().register("global.look.ruta",
                                "https://imager.nabbo.es/?figure=%LOOK%");
                WebsocketsManager.getConfig().register("global_url.hotel.notifications",
                                "http://localhost/swf/c_images/notifications/");
                WebsocketsManager.getConfig().register("global_url.hotel.catalogue",
                                "http://localhost/swf/c_images/catalogue/");
                WebsocketsManager.getConfig().register("global_url.hotel.furnis",
                                "http://localhost/swf/dcr/hof_furni/");
                WebsocketsManager.getConfig().register("global_url_images",
                                "http://localhost/websocketsBySrThrone/");
                WebsocketsManager.getConfig().register("global_url_nitro",
                                "http://localhost/nitro-assets/");

                // PARA EL TOP
                WebsocketsManager.getConfig().register("top_currency_type",
                                "5");

                WebsocketsManager.getConfig().register("ws_settings_rank_min_personalice_catalog",
                                "7");

        }
}
