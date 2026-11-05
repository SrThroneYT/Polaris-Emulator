package com.websockets.plugin.habbo.Tareas.Habbo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.websockets.plugin.WebsocketsManager;
import com.websockets.plugin.habbo.Habbos;
import com.websockets.plugin.habbo.Tareas.Level.Level;
import com.websockets.plugin.habbo.Tareas.Level.Reward;
import com.websockets.plugin.habbo.Tareas.Tarea;
import com.websockets.plugin.habbo.Tareas.TareaLevel;
import com.websockets.plugin.habbo.Tareas.TareaType;
import com.websockets.plugin.websocket.communication.Outcoming;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManager;
import com.websockets.plugin.websocket.communication.outgoing.messages.MessageManagerDoubleObjet;
import com.websockets.plugin.websocket.communication.outgoing.messages.outworks.PlaySound;

import gnu.trove.map.hash.THashMap;

public class HabboTareas {

    Habbo habbo;
    Habbos habbos;
    THashMap<String, ProgresoTarea> progresos = new THashMap<String, ProgresoTarea>();

    private final THashMap<Tarea, Integer> achievementProgress;

    private THashMap<Integer, HabboItem> items_distintos = new THashMap<Integer, HabboItem>();

    int notifications;


    public HabboTareas(Habbo habbo) {

        this.achievementProgress = new THashMap<>(0);

        this.habbo = habbo;

        try {
            this.habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
            this.update();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.loadAchievementProgressHabbo();

    }

    public void update() throws SQLException {
        this.progresos.clear();
    }

    public int getAchievementProgress(Tarea achievement) {
        if (achievement == null) {
            return 0;
        }
        synchronized (this.achievementProgress) {
            Integer progress = this.achievementProgress.get(achievement);
            return progress != null ? progress : -1;
        }
    }

    public void setProgress(Tarea achievement, int progress) {
        synchronized (this.achievementProgress) {
            this.achievementProgress.put(achievement, progress);
        }
    }

    /**
     * Atomic read-add-write so concurrent progress sources don't lose updates.
     * Returns the new total.
     */
    public int incrementProgress(Tarea achievement, int amount) {
        synchronized (this.achievementProgress) {
            Integer current = this.achievementProgress.get(achievement);
            int next = (current != null ? current : 0) + amount;
            this.achievementProgress.put(achievement, next);
            return next;
        }
    }

    public int getNotification() {
        return notifications;
    }

    public void setNotification(int number) {
        notifications = number;
    }


    public void progressAchievement(Habbo habbo, Tarea achievement) {
        progressAchievement(habbo, achievement, 1);
    }

    public void progressAchievement(Habbo habbo, Tarea achievement, int amount) {
        if (achievement == null || habbo == null || !habbo.isOnline()) {
            return;
        }

        // Tareas basicas
        if(achievement.type_id == 1){

        }

        int currentProgress = this.getAchievementProgress(achievement);

        if (currentProgress == -1) {
            currentProgress = 0; // Inicializa en 0, no en 1.
            createUserEntry(habbo, achievement);
            this.setProgress(achievement, currentProgress);
        }

        this.setNotification(this.getNotification() + 1);
        this.showButtonTareas();

        TareaLevel oldLevel = achievement.getLevelForProgress(currentProgress);

        if (oldLevel != null && (oldLevel.level == achievement.levels.size() && currentProgress >= oldLevel.progress)) {
            return; // Ya alcanzó el nivel máximo, no hacemos nada.
        }

        // Guardamos el número de nivel actual de forma segura para evitar el
        // NullPointerException
        int currentLevelNum = 0;

        if (oldLevel != null) {
            currentLevelNum = oldLevel.level;
        }

        int newProgress = this.incrementProgress(achievement, amount);

        TareaLevel newLevel = achievement.getNextLevel(currentLevelNum);
        if (newLevel != null) {
            if (newProgress >= newLevel.progress) {

                habbos.showMessage("/websocketsBySrThrone/tareas/quest.png",
                        WebsocketsManager.getTexts().getValue("normal_pase_tarea_quest_completed"));

                try {
                    habbos.getHabboClient()
                            .send(new PlaySound("PlaySound", true, "success.mp3", habbos.getHabbo()));
                } catch (Exception e) {
                }

                habbos.getLevelHabbo().insertXp(newLevel.getXp());

                this.showSuccessTarea(achievement, newLevel);

                if (newLevel.rewardAmount > 0) {
                    habbo.givePoints(newLevel.rewardType, newLevel.rewardAmount);
                }
            }

            if (habbos != null && habbos.getTareas() != null) {
                if (habbos.getCacheString("windowTareasActive") != "none") {
                    if (habbos.getCacheInt("windowTareasActive") == 1) {

                        if(achievement.name.equalsIgnoreCase("MinutesOnline")){
                            return;
                        }
                        
                        this.ocultTareasWindow();
                        this.showTareasWindow();
                    }
                }
            }
        }
    }

    // public boolean insertProgress(String name) {
    // try {
    // // Selecciona la tarea activa actual (Nivel 1 si recién empieza)
    // Tarea tareaComprobate =
    // WebsocketsManager.getTareasManager().getTareaActivaParaUsuario(name, this);
    // if (tareaComprobate != null) {

    // ProgresoTarea progresoTarea = progresos.get(tareaComprobate.getName());

    // // Si aún no tiene progreso para este nivel
    // if (progresoTarea == null) {

    // Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

    // this.setNotification(this.getNotification() + 1);
    // this.tareaCreateProgress(name);
    // this.showButtonTareas();

    // // Cuando se necesite solo 1 punto para completar la tarea
    // if (tareaComprobate.getUsageAccount() == 1) {

    // habbos.showMessage("/websocketsBySrThrone/tareas/quest.png",
    // WebsocketsManager.getTexts().getValue("normal_pase_tarea_quest_completed"));

    // habbos.getHabboClient()
    // .send(new PlaySound("PlaySound", true, "success.mp3", habbos.getHabbo()));

    // habbos.getLevelHabbo().insertXp(tareaComprobate.getXp());

    // if (tareaComprobate.getCash() >= 1) {
    // habbos.givePoints(tareaComprobate.getCash());
    // }

    // this.showSuccessTarea(tareaComprobate.getName());

    // // Actualizamos el estado a completado en BD
    // this.tareaUpdateProgress(name, 1);
    // this.update();

    // }

    // if (habbos.getCacheString("windowTareasActive") != "none") {
    // if (habbos.getCacheInt("windowTareasActive") == 1) {
    // this.ocultTareasWindow();
    // this.showTareasWindow();
    // }
    // }

    // return true;

    // } else {

    // Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
    // int cantidad = progresoTarea.getProgress() + 1;

    // if (cantidad > tareaComprobate.getUsageAccount()) {
    // return false;
    // }

    // this.tareaUpdateProgress(name, cantidad);

    // if (cantidad >= tareaComprobate.getUsageAccount()) {

    // habbos.showMessage("/websocketsBySrThrone/tareas/quest.png",
    // WebsocketsManager.getTexts().getValue("normal_pase_tarea_quest_completed"));

    // habbos.getHabboClient()
    // .send(new PlaySound("PlaySound", true, "success.mp3", habbos.getHabbo()));

    // habbos.getLevelHabbo().insertXp(tareaComprobate.getXp());

    // if (tareaComprobate.getCash() >= 1) {
    // habbos.givePoints(tareaComprobate.getCash());
    // }

    // this.showSuccessTarea(tareaComprobate.getName());

    // // --- BUSCAR Y HABILITAR SIGUIENTE TAREA DEL MISMO NOMBRE ---
    // java.util.List<Tarea> tareasMismoNombre =
    // WebsocketsManager.getTareasManager()
    // .getTareasByName(name);
    // if (tareasMismoNombre != null && !tareasMismoNombre.isEmpty()) {
    // Tarea siguienteTarea = tareasMismoNombre.stream()
    // .filter(t -> t.getId() > tareaComprobate.getId())
    // .sorted(java.util.Comparator.comparingInt(Tarea::getId))
    // .findFirst()
    // .orElse(null);

    // if (siguienteTarea != null && this.progresos.get(siguienteTarea.getName()) ==
    // null) {
    // this.tareaCreateProgress(siguienteTarea.getName());
    // }
    // }

    // }

    // if (!tareaComprobate.getName().equalsIgnoreCase("JugarRP")) {
    // this.setNotification(this.getNotification() + 1);
    // this.showButtonTareas();
    // }

    // if (habbos.getCacheString("windowTareasActive") != "none") {
    // if (habbos.getCacheInt("windowTareasActive") == 1) {
    // this.ocultTareasWindow();
    // this.showTareasWindow();
    // }
    // }

    // this.update();

    // return true;

    // }
    // } else {
    // WebsocketsManager.utilities().LOGGER_ERROR("La tarea " + name + " no se
    // encuentra");

    // return false;
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // return false;
    // }
    // }

    /**
     * Carga el progreso de las tareas del usuario desde la base de datos
     * y lo almacena en la memoria (achievementProgress y progresos).
     */
    public void loadAchievementProgressHabbo() {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT tarea_name, progress FROM websocketsth_pase_progresos WHERE user_id = ?")) {

            // Asignamos el ID del usuario a la consulta
            statement.setInt(1, this.habbo.getHabboInfo().getId());

            try (ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    String tareaName = set.getString("tarea_name");
                    int progress = set.getInt("progress");

                    // Obtenemos la tarea desde el TareasManager
                    Tarea tarea = WebsocketsManager.getTareasManager().getTarea(tareaName);

                    if (tarea != null) {
                        // Lo guardamos en el HashMap de progreso
                        this.setProgress(tarea, progress);

                        // Si estás utilizando la clase ProgresoTarea para enviarlo al cliente en
                        // getObjetTareas:
                        // this.progresos.put(tareaName, new ProgresoTarea(tareaName, progress));
                    }
                }
            }
        } catch (SQLException e) {
            WebsocketsManager.LOGGER.error("Caught SQL exception while loading achievement progress", e);
        }
    }

    public void showSuccessTarea(Tarea tarea, TareaLevel newLevel) {
        try {
            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

            JsonArray buttonInfo = new JsonArray();
            JsonObject jsonObject;

            // Verificamos que ambos objetos existan
            if (tarea != null && newLevel != null) {
                jsonObject = new JsonObject();
                jsonObject.addProperty("title", newLevel.title);
                jsonObject.addProperty("descripcion", newLevel.descripcion);

                // Agregamos los datos de la meta superada al JSON
                jsonObject.addProperty("level", newLevel.level);
                jsonObject.addProperty("rewardAmount", newLevel.rewardAmount);
                jsonObject.addProperty("rewardType", newLevel.rewardType);
                jsonObject.addProperty("xp", newLevel.xp); // O newLevel.getXp() si la experiencia varía por nivel

                String imagenFinal = newLevel.getImagen();

                // Si NO empieza con http:// ni https://, le concatenamos la ruta base del hotel
                if (imagenFinal != null && !imagenFinal.startsWith("http://") && !imagenFinal.startsWith("https://")) {
                    imagenFinal = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications")
                            + "websocketsBySrThrone/" + imagenFinal;
                }

                jsonObject.addProperty("img", imagenFinal);
                jsonObject.addProperty("eventOpen", "viewTareasWindow");

                buttonInfo.add(jsonObject);
            }

            habbos.getHabboClient()
                    .send(new MessageManager("MessageManager", true, Outcoming.SuccessTarea, buttonInfo));
        } catch (Exception e) {
            WebsocketsManager.LOGGER.error("Error en showSuccessTarea: ", e);
        }
    }

    public void showButtonTareas() {
        String rutaImagesNotifications = WebsocketsManager.getConfig()
                .getValue("global_url.hotel.notifications") + "/websocketsBySrThrone";
        try {

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

            JsonArray buttonInfo = new JsonArray();
            JsonObject jsonObject;
            jsonObject = new JsonObject();
            jsonObject.addProperty("title", WebsocketsManager.getTexts().getValue("normal_pase_title_window"));
            jsonObject.addProperty("img", rutaImagesNotifications + "/tareas/quest.png");
            jsonObject.addProperty("event", "viewTareasWindow");

            jsonObject.addProperty("notification", notifications);
            buttonInfo.add(jsonObject);
            habbos.getHabboClient()
                    .send(new MessageManager("MessageManager", true, Outcoming.TareasButton, buttonInfo));
        } catch (Exception e) {
        }
    }

    public void showTareasWindow() {
        try {

            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);

            String rutaImages = WebsocketsManager.getConfig()
                    .getValue("global_url_images");

            if (habbos.getTareas().getNotification() >= 1) {
                habbos.getTareas().setNotification(0);
                habbos.getTareas().showButtonTareas();
            }

            JsonArray habboInfo = new JsonArray();
            JsonObject jsonObject;
            jsonObject = new JsonObject();
            String look = WebsocketsManager.getConfig().getValue("global.look.ruta")
                    + "&headonly=1&size=a&gesture=sml&head_direction=2&action=std";
            jsonObject.addProperty("look", look.replace("%LOOK%", habbo.getHabboInfo().getLook()));
            jsonObject.addProperty("username", habbo.getHabboInfo().getUsername());
            jsonObject.addProperty("user_id", habbo.getHabboInfo().getId());

            jsonObject.addProperty("img_rutas",
                    rutaImages);

            if (habbos.getVip() == true) {
                jsonObject.addProperty("vip", 1);
            } else {
                jsonObject.addProperty("vip", 0);
            }

            jsonObject.addProperty("title_window",
                    WebsocketsManager.getTexts().getValue("normal_pase_title_window"));
            jsonObject.addProperty("title_part_1",
                    WebsocketsManager.getTexts().getValue("normal_pase_title_part_1"));
            jsonObject.addProperty("title_part_2",
                    WebsocketsManager.getTexts().getValue("normal_pase_title_part_2"));
            jsonObject.addProperty("title_part_3",
                    WebsocketsManager.getTexts().getValue("normal_pase_title_part_3"));
            jsonObject.addProperty("title_part_4",
                    WebsocketsManager.getTexts().getValue("normal_pase_title_part_4"));
            jsonObject.addProperty("title_part_5",
                    WebsocketsManager.getTexts().getValue("normal_pase_title_part_5"));

            // ENVIAMOS EL TIEMPO DINÁMICO RESTANTE, CAPÍTULO Y TEMPORADA ACTUAL EN EL JSON
            jsonObject.addProperty("time_reset", WebsocketsManager.getTareasManager().getTimeFilter());
            jsonObject.addProperty("capitulo", WebsocketsManager.getTareasManager().capituloActual);
            jsonObject.addProperty("temporada", WebsocketsManager.getTareasManager().temporadaActual);

            int ultimosPermisos = Emulator.getGameEnvironment().getPermissionsManager().getAllRanks().size() - 2;
            if (habbo.getHabboInfo().getRank().getId() >= ultimosPermisos) {
                jsonObject.addProperty("allow_make_task", 1);
            } else {
                jsonObject.addProperty("allow_make_task", 0);
            }

            Level level = WebsocketsManager.getLevelManager().getLevel(habbos.getLevelHabbo().getLevel());
            Level proximo_level = WebsocketsManager.getLevelManager()
                    .getLevel(habbos.getLevelHabbo().getLevel() + 1);

            jsonObject.addProperty("xp", habbos.getLevelHabbo().getXP());

            jsonObject.addProperty("xp_required", proximo_level.getXP());

            jsonObject.addProperty("level", habbos.getLevelHabbo().getLevel());
            habboInfo.add(jsonObject);

            habbos.getHabboClient().send(new MessageManagerDoubleObjet("MessageManager", true,
                    Outcoming.TareasWindow, habboInfo, this.getObjetTareas(habbos.getHabbo())));

        } catch (Exception e) {

        }
    }

    public void ocultTareasWindow() {
        try {
            Habbos habbos = WebsocketsManager.getHabbosManager().getHabbos(habbo);
            habbos.getHabboClient().send(
                    new MessageManagerDoubleObjet("MessageManager", false, Outcoming.TareasWindow, null, null));
        } catch (Exception e) {
        }
    }

    public JsonObject getObjetTareas(Habbo habbo) {

        JsonObject Objeto = new JsonObject();
        JsonArray types = new JsonArray();

        habbos.getLevelHabbo().loadPremios();
        habbos.getLevelHabbo().loadPremiosVIP();

        TreeMap<Integer, TareaType> sortedMapType = new TreeMap<>(
                WebsocketsManager.getTareasManager().getTypesTareas());
        for (TareaType typeTareas : sortedMapType.values()) {

            JsonArray arrayType = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            JsonArray info = new JsonArray();
            JsonObject jsonObjectInfo = new JsonObject();
            jsonObjectInfo.addProperty("id", typeTareas.getId());
            jsonObjectInfo.addProperty("title", typeTareas.getTitle());
            jsonObjectInfo.addProperty("descripcion", typeTareas.getDescripcion());

            String imagenFinal = typeTareas.getImagen();

            // Si NO empieza con http:// ni https://, le concatenamos la ruta base del hotel
            if (imagenFinal != null && !imagenFinal.startsWith("http://") && !imagenFinal.startsWith("https://")) {
                imagenFinal = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications")
                        + "websocketsBySrThrone/" + imagenFinal;
            }

            jsonObjectInfo.addProperty("src_imagen",
                    WebsocketsManager.getConfig().getValue("global_url.hotel.notifications"));
            jsonObjectInfo.addProperty("imagen", imagenFinal);

            info.add(jsonObjectInfo);
            jsonObject.add("info", info);

            JsonArray tareas = new JsonArray();

            // 1. Obtener los valores y convertirlos a lista
            List<Tarea> listaTareas = new ArrayList<>(WebsocketsManager.getTareasManager().getTareas().values());
            // 2. Ordenar usando la propiedad directa 'orden'
            listaTareas.sort(Comparator.comparingInt(t -> t.orden));
            // 3. Recorrer la lista ordenada para añadirlas al JsonArray
            for (Tarea tarea : listaTareas) {

                // Asegúrate de que tu clase Tarea tenga un método para verificar su tipo, ej:
                // tarea.getTypeId() == typeTareas.getId()
                if (tarea.type_id != typeTareas.getId()) {
                    continue;
                }

                // Opcional: Si las tareas pertenecen a un tipo específico (typeTareas),
                // deberías filtrarlas aquí para que no se envíen todas en todos los types.
                // Ejemplo: if (tarea.getTypeId() != typeTareas.getId()) continue;

                // 1. CREAMOS EL OBJETO DE LA TAREA ACTUAL
                JsonObject tareaObj = new JsonObject();
                tareaObj.addProperty("name", tarea.name);
                // Si tu clase 'Tarea' tiene id, título, etc., puedes agregarlos aquí:
                // tareaObj.addProperty("id", tarea.id);

                // --- MODIFICACIÓN EN EL BUCLE DE NIVELES ---

                // 1. Obtenemos el progreso actual del usuario
                int currentProgress = this.getAchievementProgress(tarea);
                currentProgress = (currentProgress == -1) ? 0 : currentProgress;

                // 2. Determinamos qué nivel es el "activo" para mostrar
                // Si el progreso es mayor o igual al necesario para el nivel actual,
                // intentamos buscar el siguiente.
                TareaLevel nivelActivo = tarea.getLevelForProgress(currentProgress);

                // Si el progreso ya superó el nivel que devuelve getLevelForProgress,
                // buscamos el primer nivel que no haya sido completado (donde el progreso sea
                // menor al requerido)
                if (nivelActivo != null && currentProgress >= nivelActivo.progress) {
                    TareaLevel siguiente = tarea.getNextLevel(nivelActivo.level);
                    if (siguiente != null) {
                        nivelActivo = siguiente;
                    }
                }

                // --- AQUÍ EMPIEZA LO NUEVO DE LOS NIVELES ---
                JsonArray nivelesTarea = new JsonArray();

                // 1. Convertimos los valores a una lista para poder ordenarlos
                java.util.List<TareaLevel> listaNiveles = new java.util.ArrayList<>(tarea.levels.values());

                // 2. Ordenamos la lista de menor a mayor según la propiedad 'level'
                listaNiveles.sort((a, b) -> Integer.compare(a.level, b.level));

                // 3. Iteramos sobre la lista ya ordenada
                for (TareaLevel tareaLevel : listaNiveles) {

                    JsonObject levelObj = new JsonObject();
                    levelObj.addProperty("level", tareaLevel.level);
                    levelObj.addProperty("reward_amount", tareaLevel.rewardAmount);
                    levelObj.addProperty("reward_type", tareaLevel.rewardType);

                    // Se obtienen las propiedades mediante los getters
                    levelObj.addProperty("name", tareaLevel.getName());
                    levelObj.addProperty("title", tareaLevel.getTitle());
                    levelObj.addProperty("descripcion", tareaLevel.getDescripcion());
                    levelObj.addProperty("xp", tareaLevel.getXp());
                    levelObj.addProperty("orden", tareaLevel.getOrden());
                    levelObj.addProperty("required", tareaLevel.progress);
                    levelObj.addProperty("usage_account", tareaLevel.progress);

                    // Lógica de la imagen
                    String imagenFinalJson2 = tareaLevel.getImagen();
                    if (imagenFinalJson2 != null && !imagenFinalJson2.startsWith("http://")
                            && !imagenFinalJson2.startsWith("https://")) {
                        imagenFinalJson2 = WebsocketsManager.getConfig().getValue("global_url.hotel.notifications")
                                + "websocketsBySrThrone/" + imagenFinalJson2;
                    }
                    levelObj.addProperty("imagen", imagenFinalJson2);

                    // Propiedades extra
                    levelObj.addProperty("distintos_items", tareaLevel.getDistintosItems() ? 1 : 0);
                    levelObj.addProperty("distintos_rooms", tareaLevel.getDistintosRooms() ? 1 : 0);
                    levelObj.addProperty("button_visit_room", tareaLevel.getButtonVisitRoom());
                    levelObj.addProperty("room_id", tareaLevel.getRoomId());

                    levelObj.addProperty("progress", currentProgress);

                    // NUEVA LÓGICA DE NIVEL ACTIVO:
                    // Marcamos como activo (1) el nivel que calculamos arriba como 'nivelActivo'
                    levelObj.addProperty("current_level_active",
                            (nivelActivo != null && nivelActivo.level == tareaLevel.level) ? 1 : 0);

                    nivelesTarea.add(levelObj);
                }

                // 2. LO PEGAMOS AL JSON DE LA TAREA ACTUAL
                tareaObj.add("niveles", nivelesTarea);

                // 3. AGREGAMOS LA TAREA COMPLETA AL ARREGLO GLOBAL DE TAREAS
                tareas.add(tareaObj);
                // --- FIN DE LO NUEVO -d--
            }

            jsonObject.add("tareas", tareas);

            arrayType.add(jsonObject);
            types.add(arrayType);
        }
        Objeto.add("types", types);

        TopNiveles topNiveles = new TopNiveles();
        topNiveles.load();
        JsonArray niveles = new JsonArray();
        String look = WebsocketsManager.getConfig().getValue("global.look.ruta")
                + "&headonly=1&size=a&gesture=sml&head_direction=2&action=std";
        for (TopUser topUser : topNiveles.getTopNiveles().values()) {
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("look", look.replace("%LOOK%", topUser.getLook()));
            jsonObject2.addProperty("username", topUser.getUsername());
            jsonObject2.addProperty("user_id", topUser.getId());
            jsonObject2.addProperty("level", topUser.getLevel());
            jsonObject2.addProperty("xp", topUser.getXp());
            jsonObject2.addProperty("event", "OpenMenuLevelOtherTop");
            niveles.add(jsonObject2);
        }
        Objeto.add("niveles", niveles);

        JsonArray levels = new JsonArray();
        TreeMap<Integer, Level> sortedMapLevels = new TreeMap<>(
                WebsocketsManager.getLevelManager().getLevels());
        for (Level level : sortedMapLevels.values()) {

            JsonArray arrayType = new JsonArray();
            JsonObject jsonObject = new JsonObject();

            JsonArray info = new JsonArray();
            JsonObject jsonObjectInfo = new JsonObject();
            jsonObjectInfo.addProperty("id", level.getLevel());
            jsonObjectInfo.addProperty("xp", level.getXP());

            if (habbos.getLevelHabbo().comprobateNextLevel(level) == true) {
                jsonObjectInfo.addProperty("button_obtener", 1);
                jsonObjectInfo.addProperty("event_obtener", "OpenObtenerPremiosLevel");
                jsonObjectInfo.addProperty("imagen_fondo_cofre", WebsocketsManager.getConfig()
                        .getValue("global_url_images").replace("/frank", "") + "/media/content.png?v3");
            } else {
                jsonObjectInfo.addProperty("button_obtener", 0);
                jsonObjectInfo.addProperty("event_obtener", "none");

                if (habbos.getLevelHabbo().getPremiosWon().get(level.getLevel()) != null) {
                    jsonObjectInfo.addProperty("imagen_fondo_cofre",
                            WebsocketsManager.getConfig().getValue("global_url_images")
                                    .replace("/frank", "") + "/media/content_abierto.png?v3");
                } else {
                    jsonObjectInfo.addProperty("imagen_fondo_cofre", WebsocketsManager.getConfig()
                            .getValue("global_url_images").replace("/frank", "") + "/media/content.png?v3");
                }
            }

            if (habbos.getLevelHabbo().comprobateNextLevelVip(level) == true) {
                jsonObjectInfo.addProperty("button_obtener_VIP", 1);
                jsonObjectInfo.addProperty("event_obtener_VIP", "OpenObtenerPremiosLevelVIP");
                jsonObjectInfo.addProperty("imagen_fondo_cofre_VIP",
                        WebsocketsManager.getConfig().getValue("global_url_images").replace("/frank",
                                "") + "/media/content_VIP_abierto.png?v3");
            } else {
                jsonObjectInfo.addProperty("button_obtener_VIP", 0);
                jsonObjectInfo.addProperty("event_obtener_VIP", "none");
                if (habbos.getLevelHabbo().getPremiosWonVIP().get(level.getLevel()) != null) {
                    jsonObjectInfo.addProperty("imagen_fondo_cofre_VIP",
                            WebsocketsManager.getConfig().getValue("global_url_images")
                                    .replace("/frank", "") + "/media/content_VIP_abierto.png?v3");
                } else {
                    jsonObjectInfo.addProperty("imagen_fondo_cofre_VIP", WebsocketsManager.getConfig()
                            .getValue("global_url_images").replace("/frank", "") + "/media/content_VIP.png?v3");
                }
            }

            if (habbos.getLevelHabbo().getPremiosWon().get(level.getLevel()) == null) {
                jsonObjectInfo.addProperty("obtenido", 0);
                jsonObjectInfo.addProperty("img_caja",
                        WebsocketsManager.getConfig().getValue("global_url_images")
                                .replace("frank/", "") + "media/caja_cerrada3.png");
            } else {
                jsonObjectInfo.addProperty("obtenido", 1);
                jsonObjectInfo.addProperty("img_caja",
                        WebsocketsManager.getConfig().getValue("global_url_images")
                                .replace("frank/", "") + "media/caja_abierta2.png");
            }

            if (habbos.getLevelHabbo().getPremiosWonVIP().get(level.getLevel()) == null) {
                jsonObjectInfo.addProperty("obtenidoVIP", 0);
                jsonObjectInfo.addProperty("img_cajaVIP",
                        WebsocketsManager.getConfig().getValue("global_url_images")
                                .replace("frank/", "") + "media/caja_cerrada3.png");
            } else {
                jsonObjectInfo.addProperty("obtenidoVIP", 1);
                jsonObjectInfo.addProperty("img_cajaVIP",
                        WebsocketsManager.getConfig().getValue("global_url_images")
                                .replace("frank/", "") + "media/caja_abierta2.png");
            }

            jsonObjectInfo.addProperty("img_vip",
                    WebsocketsManager.getConfig().getValue("global_url_images")
                            .replace("frank/", "") + "media/vip_icon.png");

            info.add(jsonObjectInfo);
            jsonObject.add("info", info);

            JsonArray Rewards = new JsonArray();
            for (Reward reward : level.getRewards().values()) {
                JsonObject jsonObject2 = new JsonObject();

                // Configs
                jsonObject2.addProperty("name", reward.getName());
                jsonObject2.addProperty("item_exist", reward.getExist());

                jsonObject2.addProperty("title", reward.getTitle());
                jsonObject2.addProperty("descripcion", reward.getDescripcion());
                jsonObject2.addProperty("cantidad", reward.getCantidad());
                if (reward.getExist() == 1) {
                    jsonObject2.addProperty("imagen",
                            WebsocketsManager.getConfig().getValue("global_url.hotel.furnis")
                                    + "icons/" + reward.getName() + "_icon.png");
                } else {
                    jsonObject2.addProperty("imagen",
                            WebsocketsManager.getConfig().getValue("global_url_images")
                                    .replace("frank/", "")
                                    + "items/" + reward.getName() + ".png");
                }

                Rewards.add(jsonObject2);
            }
            jsonObject.add("rewards", Rewards);

            JsonArray RewardsVIP = new JsonArray();
            for (Reward reward : level.getRewardsVip().values()) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("name", reward.getName());
                jsonObject2.addProperty("title", reward.getTitle());
                jsonObject2.addProperty("descripcion", reward.getDescripcion());
                jsonObject2.addProperty("cantidad", reward.getCantidad());
                if (reward.getExist() == 1) {
                    jsonObject2.addProperty("imagen",
                            WebsocketsManager.getConfig().getValue("global_url.hotel.furnis")
                                    + "icons/" + reward.getName() + "_icon.png");
                } else {
                    jsonObject2.addProperty("imagen",
                            WebsocketsManager.getConfig().getValue("global_url_images")
                                    .replace("frank/", "")
                                    + "items/" + reward.getName() + ".png");
                }
                RewardsVIP.add(jsonObject2);
            }
            jsonObject.add("rewardsVIP", RewardsVIP);

            arrayType.add(jsonObject);

            levels.add(arrayType);
        }

        Objeto.add("levels", levels);

        return Objeto;
    }

    public THashMap<String, ProgresoTarea> getProgresos() {
        return this.progresos;
    }

    public THashMap<Integer, HabboItem> getItemsDistintosCollect() {
        return this.items_distintos;
    }

    public void setItemsDistintosCollect(HabboItem habboItem) {
        this.items_distintos.put(habboItem.getId(), habboItem);
    }

    public static void createUserEntry(Habbo habbo, Tarea achievement) {
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO websocketsth_pase_progresos (user_id, tarea_name, progress) VALUES (?, ?, ?)")) {
            statement.setInt(1, habbo.getHabboInfo().getId());
            statement.setString(2, achievement.name);
            statement.setInt(3, 1);
            statement.execute();
        } catch (SQLException e) {
            WebsocketsManager.LOGGER.error("Caught SQL exception", e);
        }
    }

    public THashMap<Tarea, Integer> getAchievementProgress() {
        return this.achievementProgress;
    }
}