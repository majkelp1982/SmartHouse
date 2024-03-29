package pl.pomazanka.SmartHouse.ui.views;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.tooltip.builder.XBuilder;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Charts;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;

@PageTitle("Smart House | Wykresy")
@Route(value = "Wykresy", layout = MainLayout.class)
public class ChartsView extends View {
  private static final int SECTIONS = 1;
  // Objects
  private final Dialog chartDialog = new Dialog();
  private final Header header;
  private final Button buttonManage;
  private final Section[] section = new Section[SECTIONS];
  @Autowired Charts charts;
  private ApexCharts apexChart = new ApexCharts();
  private ArrayList<Charts.VariableList> variableList;

  public ChartsView() {
    header = new Header("Wykresy", "graph.svg");
    buttonManage = new Button("Zarządzaj", false, false);
    header.getHeader().add(buttonManage.getSource());

    section[0] = new Section();
    section[0].createTile("graph.svg", "Wykresy");

    section[0].getTileDetailsContainer(0).setMinHeight("1000px");
    section[0].getTileDetailsContainer(0).setMinWidth("1000px");

    // Create sections info/buttons/number fields
    createInfoSection0();
    final Notification notification =
        new Notification("Brak możliwości zmian ustawień. Zaloguj się.", 5000);
    section[0].getTileDetailsContainer(0).add(apexChart);
    for (int i = 0; i < SECTIONS; i++) {
      section[i].getSection().setEnabled(isUserLoggedIn());
    }
    addClickListener(
        event -> {
          if (!isUserLoggedIn()) {
            notification.open();
          }
        });
    add(header.getHeader(), section[0].getSection());
  }

  @PostConstruct
  public void post() throws Exception {
    buttonManage
        .getSource()
        .addClickListener(
            event -> {
              createDialog();
              chartDialog.open();
            });
    variableList = charts.refreshVariables();
    refreshCharts();
  }

  private void createInfoSection0() {
    final String[] colors = new String[10];
    colors[0] = "#ffff00";
    colors[1] = "#247ba0";
    colors[2] = "#00ffff";
    colors[3] = "#ff0000";
    colors[4] = "#00ff00";
    colors[5] = "#0000ff";
    colors[6] = "#FFBD07";
    colors[7] = "#2EDDFF";
    colors[8] = "#E821C7";
    colors[9] = "#733846";

    apexChart =
        ApexChartsBuilder.get()
            .withColors(colors)
            .withChart(ChartBuilder.get().withType(Type.line).build())
            .withStroke(StrokeBuilder.get().withWidth(1.0).build())
            .withXaxis(
                XAxisBuilder.get()
                    .withType(XAxisType.datetime)
                    .withTooltip(TooltipBuilder.get().withEnabled(false).build())
                    .build())
            .withYaxis(
                YAxisBuilder.get()
                    .withTooltip(TooltipBuilder.get().withEnabled(true).build())
                    .build())
            .withTooltip(
                TooltipBuilder.get()
                    .withEnabled(true)
                    .withX(XBuilder.get().withFormat("dd/MM HH:mm:ss").withShow(true).build())
                    .withShared(false)
                    .withFillSeriesColor(false)
                    .withFollowCursor(true)
                    .build())
            .build();
    apexChart.setWidth("2000px");
    apexChart.setHeight("1000px");
  }

  private void createDialog() {
    final MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
    final ArrayList<String> tempList = new ArrayList<>();
    variableList.forEach(
        item -> {
          tempList.add(item.getVariableName());
        });
    listBox.setItems(tempList);
    variableList.forEach(
        item -> {
          if (item.isEnabled()) {
            listBox.select(item.getVariableName());
          }
        });

    listBox.addSelectionListener(
        event -> {
          for (final Charts.VariableList variable : variableList) {
            variable.setEnabled(listBox.isSelected(variable.getVariableName()));
          }
          charts.saveVariablesList(variableList);
          try {
            refreshCharts();
          } catch (final Exception e) {
            e.printStackTrace();
          }
        });
    chartDialog.removeAll();
    chartDialog.add(listBox);
    chartDialog.setWidth(listBox.getWidth());
    chartDialog.setHeight("500px");
  }

  private void refreshCharts() throws Exception {
    // Get number of series
    int chartCount = 0;
    for (final Charts.VariableList variable : variableList) {
      if (variable.isEnabled()) {
        chartCount++;
      }
    }
    final Series<Coordinate>[] series = new Series[chartCount];

    // Get series according to the list
    chartCount = 0;
    for (final Charts.VariableList variable : variableList) {
      if (!variable.isEnabled()) {
        continue;
      }

      final String variableStr = variable.getVariableName();
      final int collectionEndIndex = variableStr.indexOf(".");
      final String collectionName = variableStr.substring(0, collectionEndIndex);
      final String variableName = variableStr.substring(collectionEndIndex + 1);

      try {
        final Coordinate[] list =
            charts.getSerie(
                collectionName,
                variableName,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now());
        series[chartCount] = new Series<>(variableName, list);
        chartCount++;
      } catch (final Exception e) {
        e.printStackTrace();
        final Notification notification =
            new Notification(
                "Błąd przy dodawaniu [" + variableName + "] z kolekcji [" + collectionName + "]",
                10000);
        notification.open();
      }
    }
    apexChart.updateSeries(series);
  }

  @Override
  void update() {}
}
