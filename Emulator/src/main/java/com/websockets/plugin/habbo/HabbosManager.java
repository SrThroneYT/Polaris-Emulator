package com.websockets.plugin.habbo;

import java.sql.SQLException;

import com.eu.habbo.habbohotel.users.Habbo;

import gnu.trove.map.hash.THashMap;

public class HabbosManager {

  private final THashMap<Integer, Habbos> Habbos = new THashMap<>();
  private final THashMap<Integer, Habbos> HabbosOfflines = new THashMap<>();

  public HabbosManager() {
  }

  public void update() {
    Habbos.clear();
    HabbosOfflines.clear();
  }

  public void setHabbos(Habbo habbo) throws SQLException {
    if (this.getHabbos(habbo) == null) {
      Habbos.put(habbo.getHabboInfo().getId(), new Habbos(habbo.getHabboInfo().getId()));
    }
  }

  public Habbos getHabbos(Habbo habbo) {
    return this.getHabbos().get(habbo.getHabboInfo().getId());
  }

  public Habbos getHabboRpOffline(Integer habbo_id) {
    if (this.getHabbosOfflines().get(habbo_id) == null) {
      Habbos newHabboRP = new Habbos(habbo_id);
      HabbosOfflines.put(habbo_id, newHabboRP);
      return newHabboRP;
    } else {
      return this.getHabbosOfflines().get(habbo_id);
    }
  }

  public THashMap<Integer, Habbos> getHabbosOfflines() {
    return HabbosOfflines;
  }

  public THashMap<Integer, Habbos> getHabbos() {
    return Habbos;
  }

}
