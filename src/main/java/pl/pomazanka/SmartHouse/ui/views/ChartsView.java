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
import pl.pomazanka.SmartHouse.ui.MainLayout;

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

    private void createInfoSection0() {
        //FIXME only temporary series for test
        Series<Coordinate>[] series = new Series[2];

        series[0] = new Series<Coordinate>("Pierwszy",
                new Coordinate<>(getISOString(1537788600000L),3.11),
                new Coordinate<>(getISOString(1538778600000L),10.81),
                new Coordinate<>(getISOString(1538788600000L),11.11),
                new Coordinate<>(getISOString(1538888600000L),9.11),
                new Coordinate<>(getISOString(1539788600000L),4.14)
        );

        series[1] = new Series<Coordinate>("drugi",
                new Coordinate<>(getISOString(1537788800000L),5.11),
                new Coordinate<>(getISOString(1538778800000L),7.81),
                new Coordinate<>(getISOString(1538788900000L),15.11),
                new Coordinate<>(getISOString(1538888700000L),4.11),
                new Coordinate<>(getISOString(1539788900000L),13.14)
        );

        apexChart = ApexChartsBuilder.get()
                .withChart(ChartBuilder.get()
                        .withType(Type.line).
                        build())
                .withSeries(series)
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
