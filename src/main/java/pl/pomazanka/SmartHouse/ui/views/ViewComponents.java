package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewComponents {

    // static variables
    //***************************************
    private static final String COLOR_WARNING = "yellow";
    private static final String COLOR_ALARM = "red";
    private static final String COLOR_OK = "green";
    private static final String COLOR_ON = "green";
    private static final String COLOR_NORMAL = "white";
    private static final String COLOR_OFF ="grey";

    public ViewComponents() {
    }

    // Header creator
    public HorizontalLayout createHeader(Module module, String imageSrc) {
        //About module type
        Image image = new Image(imageSrc, imageSrc);
        image.setHeight("80px");
        Label moduleTyp = new Label(module.getModuleName());
        moduleTyp.getStyle().set("font-size","30px");

        //Last telegram updates info
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Label lastUpdateLabel = new Label("Update : "+simpleDateFormat.format(module.getFrameLastUpdate()));
        setComponentColor(lastUpdateLabel,module.getFrameLastUpdate().getTime(),getCurrentDate().getTime(),60000, 120000);
        Label diagnoseUpdateLabel = new Label("Diagnose : "+simpleDateFormat.format(module.getDiagnosticLastUpdate()));
        setComponentColor(diagnoseUpdateLabel,module.getFrameLastUpdate().getTime(),getCurrentDate().getTime(),60000, 120000);
        VerticalLayout info = new VerticalLayout();
        info.setAlignItems(FlexComponent.Alignment.CENTER);
        info.add(lastUpdateLabel,diagnoseUpdateLabel);
        info.setSizeFull();

        // Header summary
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("module");
        header.setSizeFull();
        header.setHeight("80px");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.add(image,moduleTyp,info);
        return header;
    }

    // Icon creator
    public HorizontalLayout createTile(String imageSrc, String tittle) {
        HorizontalLayout tile = new HorizontalLayout();
        tile.setAlignItems(FlexComponent.Alignment.CENTER);
        tile.addClassName("module");

        Image image = new Image(imageSrc, imageSrc);
        image.setHeight("50px");

        Label tittleLabel = new Label(tittle);

        tile.add(tittleLabel, image);
        return tile;
    }

    private Date getCurrentDate() {
        return new Date();
    }

    private void setComponentColor(HtmlContainer component, long isValue, long expectedValue, long warningLimit, long alarmLimit) {
        if (Math.abs(isValue-expectedValue)>alarmLimit)
            component.getStyle().set("color", COLOR_ALARM);
        else
            if (Math.abs(isValue-expectedValue)>warningLimit)
            component.getStyle().set("color", COLOR_WARNING);
        else
            component.getStyle().set("color", COLOR_OK);
    }
}
