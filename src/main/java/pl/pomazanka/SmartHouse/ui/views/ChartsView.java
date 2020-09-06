package pl.pomazanka.SmartHouse.ui.views;

import com.github.appreciated.apexcharts.ApexCharts;
import com.github.appreciated.apexcharts.ApexChartsBuilder;
import com.github.appreciated.apexcharts.config.builder.*;
import com.github.appreciated.apexcharts.config.chart.Type;
import com.github.appreciated.apexcharts.config.xaxis.XAxisType;
import com.github.appreciated.apexcharts.helper.Coordinate;
import com.github.appreciated.apexcharts.helper.Series;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pl.pomazanka.SmartHouse.backend.dataStruct.Charts;
import pl.pomazanka.SmartHouse.ui.MainLayout;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@PageTitle("Smart House | Wykresy")
@Route(value = "Wykresy", layout = MainLayout.class)
public class ChartsView extends View {
    //Objects
    private Header header;
    private Section[] section = new Section[2];
    private ApexCharts apexChart = new ApexCharts();

    @Autowired
    Charts charts;

    public ChartsView () {
        header = new Header("Wykresy","graph.svg");
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
        Series<Coordinate>[] series = new Series[3];

        String collectionName = "module_heating";
        String variableName = "tBufferCWUDown";
        Coordinate[] list =charts.getSerie(collectionName, variableName, LocalDateTime.now(), LocalDateTime.now());
        series[0] = new Series<Coordinate>(variableName, list);

        variableName = "tBufferCWUMid";
        list =charts.getSerie(collectionName, variableName, LocalDateTime.now(), LocalDateTime.now());
        series[1] = new Series<Coordinate>(variableName, list);

        variableName = "tBufferCWUHigh";
        list =charts.getSerie(collectionName, variableName, LocalDateTime.now(), LocalDateTime.now());
        series[2] = new Series<Coordinate>(variableName, list);

        apexChart.setSeries(series);
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

    private String getISOString(long l) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
