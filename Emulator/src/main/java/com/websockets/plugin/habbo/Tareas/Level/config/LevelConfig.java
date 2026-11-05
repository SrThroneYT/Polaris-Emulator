package com.websockets.plugin.habbo.Tareas.Level.config;

import com.websockets.plugin.WebsocketsManager;

public class LevelConfig {

  public LevelConfig() {

  }

  public void register() {

    // Global config
    WebsocketsManager.getConfig().register("level_config.amount_levels", "10");

    // XP Required
    WebsocketsManager.getConfig().register("level_2.xp_required", "60");
    WebsocketsManager.getConfig().register("level_3.xp_required", "150");
    WebsocketsManager.getConfig().register("level_4.xp_required", "220");
    WebsocketsManager.getConfig().register("level_5.xp_required", "300");
    WebsocketsManager.getConfig().register("level_6.xp_required", "350");
    WebsocketsManager.getConfig().register("level_7.xp_required", "400");
    WebsocketsManager.getConfig().register("level_8.xp_required", "450");
    WebsocketsManager.getConfig().register("level_9.xp_required", "500");
    WebsocketsManager.getConfig().register("level_10.xp_required", "600");

    String reward2 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_2.premios_json_array", reward2);

    String reward3 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_3.premios_json_array", reward3);

    String reward4 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_4.premios_json_array", reward4);

    String reward5 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_5.premios_json_array", reward5);

    String reward6 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_6.premios_json_array", reward6);

    String reward7 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_7.premios_json_array", reward7);

    String reward8 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_8.premios_json_array", reward8);

    String reward9 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_9.premios_json_array", reward9);

    String reward10 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_10.premios_json_array", reward10);

    // ------------------------
    // LISTADO DE REWARDS VIPS
    // ------------------------

    String rewardVIP2 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_2_vip.premios_json_array", rewardVIP2);

    String rewardVIP3 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_3_vip.premios_json_array", rewardVIP3);

    String rewardVIP4 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_4_vip.premios_json_array", rewardVIP4);

    String rewardVIP5 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_5_vip.premios_json_array", rewardVIP5);

    String rewardVIP6 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_6_vip.premios_json_array", rewardVIP6);

    String rewardVIP7 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_7_vip.premios_json_array", rewardVIP7);

    String rewardVIP8 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_8_vip.premios_json_array", rewardVIP8);

    String rewardVIP9 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_9_vip.premios_json_array", rewardVIP9);

    String rewardVIP10 = "[{nombre:CF_1_coin_bronze,item_exist:1,cantidad:500,title:\"Creditos\",descripcion:\"Creditos oficiales\"}]";
    WebsocketsManager.getConfig().register("level_10_vip.premios_json_array", rewardVIP10);

  }

}
