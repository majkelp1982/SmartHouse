package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module_SolarPanels;
import pl.pomazanka.SmartHouse.ui.MainLayout;

@PageTitle("Smart House | Solar")
@Route(value = "Solar", layout = MainLayout.class)
public class SolarView extends View {

  @Autowired Module_SolarPanels module_solarPanels;

  // Update thread
  Thread thread;

  // Objects
  Header header;
  Section[] section = new Section[1];
  Info[][][] info = new Info[1][2][4];
  Button tempButton;

  public SolarView(Module_SolarPanels module_solarPanels) {
    this.module_solarPanels = module_solarPanels;

    // Header
    header = new Header(module_solarPanels, "sewage.svg");
    header.setLastUpdate(module_solarPanels.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_solarPanels.getDiagnosticLastUpdate());

    // Sections
    section[0] = new Section();

    // Create tile for sections
    // Section 0
    section[0].createTile("solar.svg", "Solar");

    // Create sections info/buttons/number fields
    createInfoSection0();

    section[0].getTileDetailsContainer(0).add(tempButton.getSource());

    Notification notification =
        new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
    section[0]
        .getSection()
        .addClickListener(
            event -> {
              if (!isUserLoggedIn()) {
                notification.open();
              }
            });
    section[0].getTileDetailsContainer(0).setEnabled(isUserLoggedIn());
    add(header.getHeader(), section[0].getSection());
  }

  private void createInfoSection0() {
    // Status
    tempButton = new Button("test", false, false);
    tempButton
        .getSource()
        .addClickListener(
            buttonClickEvent -> {
              module_solarPanels.saveWebPageData();
            });
  }

  @Override
  void update() {
    // Header
    header.setLastUpdate(module_solarPanels.getFrameLastUpdate());
    header.setDiagnoseUpdate(module_solarPanels.getDiagnosticLastUpdate());
  }
}
