package pl.pomazanka.SmartHouse.backend.dataStruct.Substructures;

public class VentZones {
  private final Zone salon = new Zone("salon");
  private final Zone pralnia = new Zone("pralnia");
  private final Zone lazDol = new Zone("lazDol");
  private final Zone rodzice = new Zone("rodzice");
  private final Zone natalia = new Zone("natalia");
  private final Zone karolina = new Zone("karolina");
  private final Zone lazGora = new Zone("lazGora");

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
