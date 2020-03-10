package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module;

import java.text.SimpleDateFormat;

public class ViewComponents {

    public ViewComponents() {
    }

    public VerticalLayout createHeader(Module module) {
        VerticalLayout header = new VerticalLayout();
        header.addClassName("module-header");
        header.setSizeFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H1 moduleTyp = new H1(module.getModuleName());

        HorizontalLayout info = new HorizontalLayout();
        info.setAlignItems(FlexComponent.Alignment.CENTER);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Label lastUpdateLabel = new Label("Update : "+simpleDateFormat.format(module.getFrameLastUpdate()));
        lastUpdateLabel.setClassName("ok");
        Label diagnoseUpdateLabel = new Label("Diagnose : "+simpleDateFormat.format(module.getDiagnosticLastUpdate()));
        diagnoseUpdateLabel.setClassName("bad");

        info.add(lastUpdateLabel,diagnoseUpdateLabel);

        header.add(moduleTyp,info);
        return header;
    }
}
