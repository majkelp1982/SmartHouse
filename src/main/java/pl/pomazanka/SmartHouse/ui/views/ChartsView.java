package pl.pomazanka.SmartHouse.ui.views;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Charts;
import pl.pomazanka.SmartHouse.backend.dataStruct.Diagnostic;
import pl.pomazanka.SmartHouse.ui.MainLayout;
import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@PageTitle("Smart House | Wykresy")
@Route(value = "Wykresy", layout = MainLayout.class)
public class ChartsView extends View {
    //Objects
    private Dialog chartDialog = new Dialog();
    private Header header;
    private Button buttonManage;
    private Section[] section = new Section[2];
    private ApexCharts apexChart = new ApexCharts();
    private ArrayList<Charts.VariableList> variableList;
    @Autowired
    Charts charts;
    @Autowired
    Diagnostic diagnostic;

    public ChartsView () {
        header = new Header("Wykresy","graph.svg");
        buttonManage = new Button("Zarządzaj", false, false);
        header.getHeader().add(buttonManage.getSource());

        section[0] = new Section();
        section[0].createTile("graph.svg","Wykresy");

        section[0].getTileDetailsContainer(0).setMinHeight("1000px");
        section[0].getTileDetailsContainer(0).setMinWidth("1000px");

        //Create sections info/buttons/number fields
        createInfoSection0();
        section[0].getTileDetailsContainer(0).add(apexChart);
        add(header.getHeader(),section[0].getSection());
    }

    @PostConstruct
    public void post() throws Exception {
        buttonManage.getSource().addClickListener(event -> {
            createDialog();
            chartDialog.open();
        });
        variableList = charts.refreshVariables();
        refreshCharts();
    }

    private void createInfoSection0() {
        apexChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.line).
                                build())
                .withXaxis(XAxisBuilder.get()
                        .withType(XAxisType.datetime)
                        .build())
                .withYaxis(YAxisBuilder.get()
                        .withTooltip(TooltipBuilder.get()
                                .withEnabled(true)
                                .build())
                        .build())

                .build();
        apexChart.setWidth("2000px");
        apexChart.setHeight("1000px");
    }

    private void createDialog() {
        MultiSelectListBox<String> listBox = new MultiSelectListBox<>();
        ArrayList<String> tempList = new ArrayList<>();
        variableList.forEach(item -> {
            tempList.add(item.getVariableName());
        });
        listBox.setItems(tempList);
        variableList.forEach(item -> {
            if (item.isEnabled())
                listBox.select(item.getVariableName());
        });

        listBox.addSelectionListener(event -> {
            for (Charts.VariableList variable : variableList)
                variable.setEnabled(listBox.isSelected(variable.getVariableName()));

            charts.saveVariablesList(variableList);
        });
        chartDialog.removeAll();
        chartDialog.add(listBox);
        chartDialog.setWidth(listBox.getWidth());
        chartDialog.setHeight("500px");
    }

    private void refreshCharts() throws Exception {
        //FIXME. Auto-refresh list after managing list selection changed.
        //FIXME. In case array on managing list activated. Variable name will not working. The same for inner document (module_comfort)
        int chartCount= 0;
        for (Charts.VariableList variable : variableList)
            if (variable.isEnabled()) chartCount++;
        Series<Coordinate>[] series = new Series[chartCount];

        chartCount = 0;
        for (Charts.VariableList variable : variableList) {
            if (!variable.isEnabled()) continue;

            String variableStr = variable.getVariableName();
            int collectionEndIndex = variableStr.indexOf(".");
            String collectionName = variableStr.substring(0,collectionEndIndex);
            String variableName = variableStr.substring(collectionEndIndex+1);

            Coordinate[] list =charts.getSerie(collectionName, variableName, LocalDateTime.now(), LocalDateTime.now());
            series[chartCount] = new Series<Coordinate>(variableName, list);


            chartCount++;
        }

        apexChart.setSeries(series);
    }
}
