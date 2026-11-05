package com.websockets.plugin.habbo.BarraTop.MejoresLook;

import com.websockets.plugin.WebsocketsManager;

import gnu.trove.map.hash.THashMap;

public class CapituloLook {

    public int capitulo;
    public THashMap<Integer, CapituloLookPosition> positions = new THashMap<>();

    public CapituloLook(int capitulo) {
        this.capitulo = capitulo;
        this.registerPremios(capitulo);
        this.loadPremios();
    }

    public void loadPremios() {
        // Cargar premios desde la configuración (3 posiciones por capítulo)
        for (int i = 1; i <= 3; i++) {
            CapituloLookPosition position = new CapituloLookPosition(this.capitulo, i);
            this.positions.put(i, position);
        }
    }

    public void registerPremios(int capitulo) {
        WebsocketsManager.getConfig().register("capitulo_" + capitulo + ".top_look_1.premios_json_array",
                "[{\"nombre\":\"CF_1_coin_bronze\",\"item_exist\":0,\"cantidad\":100,\"point_type\":5,\"title\":\"Creditos\",\"descripcion\":\"Creditos oficiales\"},{\"nombre\":\"CF_1_coin_silver\",\"item_exist\":0,\"cantidad\":50,\"point_type\":-1,\"title\":\"Creditos\",\"descripcion\":\"Creditos oficiales\"}]");
        WebsocketsManager.getConfig().register("capitulo_" + capitulo + ".top_look_2.premios_json_array",
                "[{\"nombre\":\"CF_1_coin_bronze\",\"item_exist\":0,\"cantidad\":60,\"point_type\":5,\"title\":\"Creditos\",\"descripcion\":\"Creditos oficiales\"}]");
        WebsocketsManager.getConfig().register("capitulo_" + capitulo + ".top_look_3.premios_json_array",
                "[{\"nombre\":\"CF_1_coin_bronze\",\"item_exist\":0,\"cantidad\":30,\"point_type\":5,\"title\":\"Creditos\",\"descripcion\":\"Creditos oficiales\"}]");
    }

}
