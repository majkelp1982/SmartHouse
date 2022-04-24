package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Diagnostic;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@PageTitle("Smart House | Diagnostyka")
@Route(value = "Diagnostyka", layout = MainLayout.class)
public class DiagnosticView extends View {
  private static final int SECTIONS = 2;

  // Objects
  private final Header header;
  private final Section[] section = new Section[SECTIONS];
  private final Grid<Diagnostic.ModuleDiagInfo> moduleGrid = new Grid<>();
  private final List<Diagnostic.ModuleDiagInfo> modulesTable = new ArrayList<>();
  @Autowired Diagnostic diagnostic;
  // Update thread
  Thread thread;
  Grid<Diagnostic.ModuleFault> faultGrid = new Grid<>();
  List<Diagnostic.ModuleFault> globalFaultList = new ArrayList<>();
  private List<Diagnostic.ModuleDiagInfo> moduleList = new ArrayList<>();
  private Button globalResetButton;
  private Button groupButton;

  public DiagnosticView(final Diagnostic diagnostic) {
    this.diagnostic = diagnostic;

    this.diagnostic.refreshGlobalFaultList();

    // Header
    header = new Header(diagnostic, "support.svg");
    header.setDiagnoseUpdate(diagnostic.getDiagnosticLastUpdate());

    section[0] = new Section();
    section[1] = new Section();

    // Create sections info/buttons/number fields
    createInfoSection0();
    createInfoSection1();

    // Section 0
    section[0].createTile("place.svg", "IP");
    section[0].getTileDetailsContainer(0).add(moduleGrid);
    section[0].getTileDetailsContainer(0).setWidth("1200px");
    section[0].getTileDetailsContainer(0).setHeight("300px");
    section[0].getTileDetailsContainer(0).setHeight((moduleList.size() * 50 + 120) + "px");

    // Section 1
    section[1].createTile("support.svg", "Błędy");
    section[1]
        .getTileDetailsContainer(0)
        .add(
            new HorizontalLayout(globalResetButton.getSource(), groupButton.getSource()),
            faultGrid);
    section[1].getTileDetailsContainer(0).setMinHeight("100px");
    sectionResize();
    if (diagnostic.isGlobalFaultsListGroupByFault()) {
      section[1].getTileDetailsContainer(0).setMinWidth("1600px");
    } else {
      section[1].getTileDetailsContainer(0).setMinWidth("1200px");
    }
    section[1].getTileDetailsContainer(0).setHeight((globalFaultList.size() * 35 + 180) + "px");
    final Notification notification =
        new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
    for (int i = 0; i < SECTIONS; i++) {
      section[i].getSection().setEnabled(isUserLoggedIn());
    }
    addClickListener(
        event -> {
          if (!isUserLoggedIn()) {
            notification.open();
          }
        });

    add(header.getHeader(), section[0].getSection(), section[1].getSection());
  }

  private void createInfoSection0() {
    moduleList = diagnostic.getModules();
    moduleGrid
        .addColumn(Diagnostic.ModuleDiagInfo::getModuleType)
        .setHeader("Typ")
        .setWidth("10px");
    moduleGrid
        .addColumn(Diagnostic.ModuleDiagInfo::getModuleName)
        .setHeader("Nazwa modułu")
        .setWidth("50px");
    moduleGrid
        .addColumn(
            new ComponentRenderer<>(
                moduleDiagInfo -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Button button = new Button(moduleDiagInfo.getIP(), false, false);
                  button
                      .getSource()
                      .addClickListener(
                          buttonClickEvent -> {
                            UI.getCurrent()
                                .getPage()
                                .executeJs(
                                    "window.open('http://"
                                        + moduleDiagInfo.getIP()
                                        + "/diagnose', '_blank');");
                          });
                  layout.add(button.getSource());
                  return layout;
                }))
        .setHeader("Adres IP");
    moduleGrid
        .addColumn(
            new ComponentRenderer<>(
                moduleDiagInfo -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final int signal = moduleDiagInfo.getSignal();
                  final Label label = new Label("" + signal);
                  if (signal <= -80) {
                    label.getStyle().set("color", View.COLOR_ALARM);
                  } else {
                    label.getStyle().set("color", View.COLOR_OK);
                  }
                  layout.add(label);
                  return layout;
                }))
        .setHeader("Sygnał[db]")
        .setWidth("15px");
    moduleGrid
        .addColumn(Diagnostic.ModuleDiagInfo::getFirmwareVersion)
        .setHeader("Firmware")
        .setWidth("150px");
    moduleGrid
        .addColumn(
            new ComponentRenderer<>(
                moduleDiagInfo -> {
                  final HorizontalLayout layout = new HorizontalLayout();
                  final Long duraiton = moduleDiagInfo.getDiagLastUpdate();
                  final Label label = new Label(duraiton.toString());
                  if (duraiton > 120) {
                    label.getStyle().set("color", View.COLOR_ALARM);
                  } else {
                    label.getStyle().set("color", View.COLOR_OK);
                  }
                  layout.add(label);
                  return layout;
                }))
        .setHeader("Last Update[s]");

    modulesTable.clear();
    moduleList.stream()
        .filter(modMain -> !Objects.equals(modMain.getModuleType(), 1))
        .forEach(item -> modulesTable.add(item));
    moduleGrid.setItems(modulesTable);
  }

  private void createInfoSection1() {
    globalFaultList = diagnostic.getGlobalFaultsList();
    faultGrid.addColumn(Diagnostic.ModuleFault::getModuleType).setHeader("Typ");
    faultGrid.addColumn(Diagnostic.ModuleFault::getModuleName).setHeader("Nazwa modułu");
    faultGrid.addColumn(Diagnostic.ModuleFault::getIndex).setHeader("Index");
    faultGrid.addColumn(Diagnostic.ModuleFault::getDescription).setHeader("Opis");
    faultGrid.addColumn(Diagnostic.ModuleFault::getIncomingToString).setHeader("Początek");
    faultGrid.addColumn(Diagnostic.ModuleFault::getOutgoingToString).setHeader("Koniec");
    faultGrid
        .addColumn(Diagnostic.ModuleFault::getActiveTime)
        .setHeader("czas trwania[s]")
        .setKey("czas");
    if (diagnostic.isGlobalFaultsListGroupByFault()) {
      faultGrid
          .addColumn(Diagnostic.ModuleFault::getNumberOfErrors)
          .setHeader("liczba wystąpień")
          .setKey("liczba")
          .setSortable(true);
      faultGrid.getColumnByKey("czas").setHeader("łączny czas trwania[s]");
    }

    faultGrid.setItems(globalFaultList);
    faultGrid
        .getColumns()
        .forEach(
            column -> {
              column.setResizable(true);
              column.setSortable(true);
              column.setAutoWidth(true);
            });

    globalResetButton = new Button("Potwierdź", false, false);
    globalResetButton.getSource().addClickListener(event -> diagnostic.resetGlobalList());

    groupButton = new Button("Grupuj", true, diagnostic.isGlobalFaultsListGroupByFault());
    groupButton
        .getSource()
        .addClickListener(
            event -> {
              diagnostic.globalFaultsListGroupByFault();
              if (diagnostic.isGlobalFaultsListGroupByFault()) {
                faultGrid
                    .addColumn(Diagnostic.ModuleFault::getNumberOfErrors)
                    .setHeader("liczba wystąpień")
                    .setKey("liczba")
                    .setSortable(true);
                faultGrid.getColumnByKey("czas").setHeader("łączny czas trwania[s]");
              } else {
                faultGrid.removeColumnByKey("liczba");
                faultGrid.getColumnByKey("czas").setHeader("czas trwania[s]");
              }
              update();
            });
  }

  @Override
  void update() {
    header.setDiagnoseUpdate(diagnostic.getDiagnosticLastUpdate());
    groupButton.setButtonColor(
        diagnostic.isGlobalFaultsListGroupByFault(), diagnostic.isGlobalFaultsListGroupByFault());
    diagnostic.refreshGlobalFaultList();
    faultGrid.setItems(diagnostic.getGlobalFaultsList());
    moduleGrid.setItems(modulesTable);
    sectionResize();
  }

  private void sectionResize() {
    if (diagnostic.isGlobalFaultsListGroupByFault()) {
      section[1].getTileDetailsContainer(0).setMinWidth("1600px");
    } else {
      section[1].getTileDetailsContainer(0).setMinWidth("1200px");
    }
    section[1].getTileDetailsContainer(0).setHeight((globalFaultList.size() * 35 + 180) + "px");
  }
}
