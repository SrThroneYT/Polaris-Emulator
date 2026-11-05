package com.websockets.plugin.habbo.BarraTop.CatalogWindow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogItem;
import com.eu.habbo.habbohotel.catalog.CatalogPage;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.websocket.Habbo.HabboClient;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;

public class CatalogWindowManager {

    private static final Comparator<JsonElement> ORDER_NUM_COMPARATOR = (a, b) -> {
        int orderA = a.getAsJsonObject().get("order_num").getAsInt();
        int orderB = b.getAsJsonObject().get("order_num").getAsInt();
        return Integer.compare(orderA, orderB);
    };
    private static final int ROOT_PARENT_ID = -1;
    private int pageId = 0;
    private int itemId = 0;
    private String mode = "base";

    public CatalogWindowManager() {

    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void IncomingListActions(Map<String, Object> data, HabboClient wsclient) {
        try {
            if (wsclient != null) {
                String name = (String) data.get("name");
                if (name.equalsIgnoreCase("showCataloguesWindow")) {

                    Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(wsclient.getHabbo());

                    habbos.getHabboClient()
                            .send(new MessageManager("MessageManager", true,
                                    Outcoming.showCataloguesWindow, this.getArrayButtons()));

                    WebsocketsManager.utilities().LOGGER("LLEGO ACA");
                }
            }
        } catch (Exception e) {
        }
    }

    public JsonArray getArrayButtons() {
        JsonArray array = new JsonArray();
        JsonObject jsonObject = new JsonObject();

        // Agregamos el identificador del mensaje (el "case 20" de tu switch)
        jsonObject.addProperty("message", 20);

        // Enviamos los IDs que capturará el componente
        jsonObject.addProperty("page_id", pageId);
        jsonObject.addProperty("item_id", itemId);
        // Opcional: puedes enviar el modo si quieres que abra 'items', 'pages' o 'base'
        jsonObject.addProperty("mode", mode);

        array.add(jsonObject);
        return array;
    }

    private int getCatalogItemOrder(int catalog_item) {
        int integer = 0;
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection
                        .prepareStatement(
                                "SELECT * FROM `catalog_items` WHERE id= ? LIMIT 1")) {
            statement.setInt(1, catalog_item);
            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    integer = set.getInt("order_number");
                }
            }
            return integer;
        } catch (SQLException e) {
            WebsocketsManager.utilities().LOGGER("Caught SQL exception " + e);
            return integer;
        }
    }

    // --- Función Completa ---
    public JsonArray initializeCatalogPages() {
        String rutaCatalogue = WebsocketsManager.getConfig().getValue("global_url.hotel.catalogue");
        String url_furnis = WebsocketsManager.getConfig().getValue("global_url.hotel.furnis");

        Map<Integer, CatalogPage> catalogPages = Emulator.getGameEnvironment().getCatalogManager().catalogPages;

        Map<Integer, JsonObject> pageJsonMap = new HashMap<>();
        Map<Integer, JsonArray> childrenMap = new HashMap<>();
        List<JsonElement> topLevelList = new ArrayList<>();

        // =======================================================================
        // PRIMERA PASADA: Crear JsonObjects y agrupar hijos
        // =======================================================================
        for (CatalogPage page : catalogPages.values()) {
            JsonObject pageObject = new JsonObject();
            int pageId = page.getId();
            int parentId = page.getParentId();

            // Mapeo de atributos de la página
            pageObject.addProperty("page_id", pageId);
            pageObject.addProperty("parent_id", parentId);
            pageObject.addProperty("caption", page.getCaption());
            pageObject.addProperty("caption_save", page.getPageName());
            pageObject.addProperty("icon_color", page.getIconColor());
            pageObject.addProperty("icon_image", rutaCatalogue + "icon_" + page.getIconImage() + ".png");
            pageObject.addProperty("page_icon", page.getIconImage());
            pageObject.addProperty("order_num", page.getOrderNum());
            pageObject.addProperty("visible", page.isVisible());
            pageObject.addProperty("enabled", page.isEnabled());
            pageObject.addProperty("club_only", page.isClubOnly());
            pageObject.addProperty("page_layout", page.getLayout());
            pageObject.addProperty("page_headline", page.getHeaderImage());
            pageObject.addProperty("page_teaser", page.getTeaserImage());
            pageObject.addProperty("page_special", page.getSpecialImage());
            pageObject.addProperty("page_text1", page.getTextOne());
            pageObject.addProperty("page_text2", page.getTextTwo());
            pageObject.addProperty("page_text_details", page.getTextDetails());
            pageObject.addProperty("page_text_teaser", page.getTextTeaser());

            pageObject.addProperty("items_count", page.getCatalogItems().size());
            pageObject.add("children", new JsonArray()); // Inicializar vacío por defecto

            pageJsonMap.put(pageId, pageObject);

            // Agrupar en el mapa de hijos usando el parentId
            if (!childrenMap.containsKey(parentId)) {
                childrenMap.put(parentId, new JsonArray());
            }
            childrenMap.get(parentId).add(pageObject);
        }

        // =======================================================================
        // SEGUNDA PASADA: Adjuntar hijos a padres y ordenar
        // =======================================================================
        for (JsonObject pageObject : pageJsonMap.values()) {
            int pageId = pageObject.get("page_id").getAsInt();
            int parentId = pageObject.get("parent_id").getAsInt();

            JsonArray unsortedChildren = childrenMap.get(pageId);

            if (unsortedChildren != null && unsortedChildren.size() > 0) {
                List<JsonElement> childrenList = new ArrayList<>();
                for (JsonElement child : unsortedChildren) {
                    childrenList.add(child);
                }

                // Ordenar páginas hijas por order_num
                childrenList.sort(ORDER_NUM_COMPARATOR);

                JsonArray sortedChildrenArray = new JsonArray();
                for (JsonElement child : childrenList) {
                    sortedChildrenArray.add(child);
                }
                pageObject.add("children", sortedChildrenArray);
            }

            // Si es una página raíz, añadirla a la lista principal
            if (parentId == ROOT_PARENT_ID) {
                topLevelList.add(pageObject);
            }
        }

        // Ordenar páginas de nivel superior
        topLevelList.sort(ORDER_NUM_COMPARATOR);

        JsonArray finalResult = new JsonArray();
        for (JsonElement topPage : topLevelList) {
            finalResult.add(topPage);
        }

        return finalResult;
    }

    public JsonObject getPageData(int pageId) {
        CatalogPage page = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(pageId);
        JsonObject response = new JsonObject();

        if (page == null) {
            response.addProperty("error", "Page not found");
            return response;
        }

        String url_furnis = WebsocketsManager.getConfig().getValue("global_url.hotel.furnis") + "icons/";

        // --- SECCIÓN DE ORDENAMIENTO ---
        // 1. Convertimos la colección de ítems a una lista
        List<CatalogItem> itemsList = new ArrayList<>(page.getCatalogItems().values());

        // Reemplaza el Comparator por Collections.sort o List.sort con naturalOrder:
        itemsList.sort(Comparator.naturalOrder());
        // -------------------------------

        JsonArray itemsArray = new JsonArray();

        // 3. Iteramos sobre la lista ya ordenada
        for (CatalogItem catalogItem : itemsList) {
            JsonObject itemObj = new JsonObject();

            itemObj.addProperty("id", catalogItem.getId());
            itemObj.addProperty("name", catalogItem.getName());
            itemObj.addProperty("offer_id", catalogItem.getOfferId());
            itemObj.addProperty("page_id", catalogItem.getPageId());
            itemObj.addProperty("cost_credits", catalogItem.getCredits());
            itemObj.addProperty("cost_points", catalogItem.getPoints());
            itemObj.addProperty("points_type", catalogItem.getPointsType());
            itemObj.addProperty("amount", catalogItem.getAmount());

            try {
                java.lang.reflect.Field field = CatalogItem.class.getDeclaredField("orderNumber");
                field.setAccessible(true);
                int orderNumber = (int) field.get(catalogItem);

                itemObj.addProperty("order", orderNumber);
            } catch (Exception e) {
                itemObj.addProperty("order", 0);
            }

            itemObj.addProperty("limited_sells", catalogItem.getLimitedSells());
            itemObj.addProperty("limited_stack", catalogItem.getLimitedStack());

            itemObj.addProperty("have_offer", catalogItem.isHaveOffer() ? 1 : 0);
            itemObj.addProperty("club_only", catalogItem.isClubOnly() ? 1 : 0);
            itemObj.addProperty("is_limited", catalogItem.isLimited() ? 1 : 0);
            itemObj.addProperty("extradata", catalogItem.getExtradata());

            if (!catalogItem.getBaseItems().isEmpty()) {
                Item baseItem = catalogItem.getBaseItems().iterator().next();

                if (baseItem != null) {
                    itemObj.addProperty("item_name", baseItem.getName());
                    itemObj.addProperty("type", baseItem.getType().name());

                    String formatNameItem = baseItem.getName().replace("*", "_");
                    itemObj.addProperty("icon", url_furnis + formatNameItem + "_icon.png");
                }
            }

            itemsArray.add(itemObj);
        }

        response.add("items", itemsArray);
        return response;
    }

    public JsonObject getItemBaseData(int pageId) {
        CatalogPage page = Emulator.getGameEnvironment().getCatalogManager().getCatalogPage(pageId);
        JsonObject response = new JsonObject();

        if (page == null) {
            response.addProperty("error", "Page not found");
            return response;
        }

        // 1. Obtenemos y ordenamos la lista igual que en getPageData
        List<CatalogItem> itemsList = new ArrayList<>(page.getCatalogItems().values());

        // Reemplaza el Comparator por Collections.sort o List.sort con naturalOrder:
        itemsList.sort(Comparator.naturalOrder());

        JsonArray basesArray = new JsonArray();

        String url_furnis = WebsocketsManager.getConfig().getValue("global_url.hotel.furnis") + "icons/";

        // 2. Iteramos para extraer la info técnica de Item (BaseItem)
        for (CatalogItem catalogItem : itemsList) {
            if (!catalogItem.getBaseItems().isEmpty()) {
                Item baseItem = catalogItem.getBaseItems().iterator().next();

                if (baseItem != null) {
                    JsonObject baseObj = new JsonObject();

                    baseObj.addProperty("id", baseItem.getId()); // ID real del furni_base
                    baseObj.addProperty("item_id", baseItem.getId()); // ID real del furni_base
                    baseObj.addProperty("sprite_id", baseItem.getSpriteId());
                    baseObj.addProperty("item_name", baseItem.getName());
                    baseObj.addProperty("public_name", baseItem.getFullName());
                    baseObj.addProperty("width", baseItem.getWidth());
                    baseObj.addProperty("length", baseItem.getLength());
                    baseObj.addProperty("stack_height", baseItem.getHeight());
                    baseObj.addProperty("allow_stack", baseItem.allowStack() ? 1 : 0);
                    baseObj.addProperty("allow_sit", baseItem.allowSit() ? 1 : 0);
                    baseObj.addProperty("allow_lay", baseItem.allowLay() ? 1 : 0);
                    baseObj.addProperty("allow_walk", baseItem.allowWalk() ? 1 : 0);
                    baseObj.addProperty("allow_gift", baseItem.allowGift() ? 1 : 0);
                    baseObj.addProperty("allow_trade", baseItem.allowTrade() ? 1 : 0);
                    baseObj.addProperty("allow_recycle", baseItem.allowRecyle() ? 1 : 0);
                    baseObj.addProperty("allow_marketplace", baseItem.allowMarketplace() ? 1 : 0);
                    baseObj.addProperty("allow_inventory_stack", baseItem.allowInventoryStack() ? 1 : 0);
                    baseObj.addProperty("interaction_type", baseItem.getInteractionType().getName());
                    baseObj.addProperty("interaction_modes_count", baseItem.getStateCount());
                    baseObj.addProperty("vending_ids",
                            baseItem.getVendingItems() != null ? baseItem.getVendingItems().toString() : "");
                    // Convertimos el array a un String separado por ";"
                    String multiheightString = "";
                    if (baseItem.getMultiHeights() != null) {
                        // Si es un array de Double, lo unimos directamente
                        multiheightString = String.join(";", Arrays.stream(baseItem.getMultiHeights())
                                .mapToObj(String::valueOf)
                                .toArray(String[]::new));
                    }
                    baseObj.addProperty("multiheight", multiheightString);
                    baseObj.addProperty("customparams", baseItem.getCustomParams());
                    baseObj.addProperty("effect_id_male", baseItem.getEffectM());
                    baseObj.addProperty("effect_id_female", baseItem.getEffectF());
                    baseObj.addProperty("type", baseItem.getType().name());

                    String formatNameItem = baseItem.getName().replace("*", "_");
                    baseObj.addProperty("icon", url_furnis + formatNameItem + "_icon.png");

                    basesArray.add(baseObj);
                }
            }
        }

        response.add("base_items", basesArray);
        return response;
    }

    public void localizarItemEvent(Habbo habbo, HabboItem item) {
        Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

        if (habbos.getCacheInt("localiceItemActiveCatalogue") == 1) {

            WebsocketsManager.utilities().LOGGER(
                    "Intentando localizar item en catalogo: " + item.getId() + " - " + item.getBaseItem().getName());

            int catalogPageId = 0;
            int itemCatalogId = 0;
            try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                    PreparedStatement statement = connection
                            .prepareStatement("SELECT page_id, id FROM catalog_items WHERE item_ids = ? LIMIT 1")) {
                statement.setInt(1, item.getBaseItem().getId());
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        catalogPageId = set.getInt("page_id");
                        itemCatalogId = set.getInt("id");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            this.setMode("base");
            if (mode.equals("base")) {
                this.setItemId(item.getBaseItem().getId());
            } else {
                this.setItemId(item.getId());
            }
            this.setPageId(catalogPageId);

            try {

                habbos.getHabboClient()
                        .send(new MessageManager("MessageManager", false,
                                Outcoming.showCataloguesWindow, null));

                habbos.getHabboClient()
                        .send(new MessageManager("MessageManager", true,
                                Outcoming.showCataloguesWindow, this.getArrayButtons()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}