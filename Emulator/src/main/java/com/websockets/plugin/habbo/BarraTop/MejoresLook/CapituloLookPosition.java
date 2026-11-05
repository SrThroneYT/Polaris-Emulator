package com.websockets.plugin.habbo.BarraTop.MejoresLook;

import com.eu.habbo.Emulator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.websockets.plugin.WebsocketsManager;

import gnu.trove.map.hash.THashMap;

public class CapituloLookPosition {

    public int position;
    public int capitulo;

    public THashMap<Integer, PremiosCapitulos> premios = new THashMap<>();

    public CapituloLookPosition(int capitulo, int position) {
        this.capitulo = capitulo;
        this.position = position;
        this.loadPremios();
    }

    public void loadPremios() {
        JsonParser parser = new JsonParser();
        JsonArray gsonArr = parser
                .parse(WebsocketsManager.getConfig()
                        .getValue("capitulo_" + capitulo + ".top_look_" + position + ".premios_json_array"))
                .getAsJsonArray();

        int index = 0; // Guardar el índice actual para usarlo dentro del bucle
        for (JsonElement obj : gsonArr) {
            index++; // Incrementar el índice para cada premio
            // Object of array
            JsonObject gsonObj = obj.getAsJsonObject();
            // Primitives elements of object
            String nombre = gsonObj.get("nombre").getAsString();
            int item_exist = gsonObj.get("item_exist").getAsInt();
            int cantidad = gsonObj.get("cantidad").getAsInt();
            int point_type = gsonObj.get("point_type").getAsInt();
            String title = gsonObj.get("title").getAsString();
            String descripcion = gsonObj.get("descripcion").getAsString();

            PremiosCapitulos premio = new PremiosCapitulos(index, nombre, item_exist, cantidad, point_type, title,
                    descripcion);

            if (item_exist == 0) {
                int currencyType = point_type; // Obtener el tipo de moneda del premio
                String currencyName = Emulator.getTexts().getValue("seasonal.name." + currencyType);
                premio.setTitle(currencyName);
            }

            // Insertar
            this.premios.put(index, premio);
        }

    }

}
