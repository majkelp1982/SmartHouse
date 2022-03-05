package pl.pomazanka.SmartHouse.backend.dataStruct.Substructures;

public class VentZones {
  private Zone salon = new Zone("salon");
  private Zone pralnia = new Zone("pralnia");
  private Zone lazDol = new Zone("lazDol");
  private Zone rodzice = new Zone("rodzice");
  private Zone natalia = new Zone("natalia");
  private Zone karolina = new Zone("karolina");
  private Zone lazGora = new Zone("lazGora");

  public VentZones() {}

  public Zone getSalon() {
    return salon;
  }

  public Zone getPralnia() {
    return pralnia;
  }

  public Zone getLazDol() {
    return lazDol;
  }

  public Zone getRodzice() {
    return rodzice;
  }

  public Zone getNatalia() {
    return natalia;
  }

  public Zone getKarolina() {
    return karolina;
  }

  public Zone getLazGora() {
    return lazGora;
  }
}
