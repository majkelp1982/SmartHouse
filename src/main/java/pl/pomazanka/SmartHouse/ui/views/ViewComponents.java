// !!!!!!!!!!!!!!!!!!!! TO BE DELETED WHEN CHANGE TO VIEW !!!!!!!!!!!!!!!!!!!1

package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewComponents extends VerticalLayout {

    // static variables
    //***************************************
    public static final String COLOR_WARNING = "yellow";
    public static final String COLOR_ALARM = "red";
    public static final String COLOR_OK = "green";
    public static final String COLOR_ON = "green";
    public static final String COLOR_NORMAL = "white";
    public static final String COLOR_OFF ="grey";
    public static final String COLOR_NV = "orange";

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
        setComponentColor(lastUpdateLabel,true,true,module.getFrameLastUpdate().getTime(),getCurrentDate().getTime(),60000, 120000);
        Label diagnoseUpdateLabel = new Label("Diagnose : "+simpleDateFormat.format(module.getDiagnosticLastUpdate()));
        setComponentColor(diagnoseUpdateLabel,true, true, module.getFrameLastUpdate().getTime(),getCurrentDate().getTime(),60000, 120000);
        VerticalLayout info = new VerticalLayout();
        info.setAlignItems(FlexComponent.Alignment.CENTER);
        info.add(lastUpdateLabel,diagnoseUpdateLabel);
        info.setWidth("800px");
        info.setSizeFull();

        // Header summary
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("module");
        header.setMinWidth("800px");
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

    public VerticalLayout createDetailsContainer () {
        VerticalLayout tileDetails = new VerticalLayout();
 //       tileDetails.setAlignItems(Alignment.CENTER);
        tileDetails.setAlignItems(Alignment.START);
        return  tileDetails;
    }

    public HorizontalLayout addInfo (String name, String unit, boolean colorEnabled, boolean exceedAlarm, Number isValue, Number expectedValue, Number warningLimit, Number alarmLimit) {
        HorizontalLayout item = new HorizontalLayout();
        Label nameLabel = new Label(""+name);
        nameLabel.getStyle().set("color",COLOR_NORMAL);
        Label valueLabel = new Label(" "+isValue+"["+unit+"]");
        setComponentColor(valueLabel,colorEnabled,exceedAlarm,isValue,expectedValue,warningLimit,alarmLimit);

        item.add(nameLabel,valueLabel);
        return item;
    }

    public HorizontalLayout addInfo (String name, String value) {
        HorizontalLayout item = new HorizontalLayout();
        Label nameLabel = new Label(""+name);
        nameLabel.getStyle().set("color",COLOR_NORMAL);
        Label valueLabel = new Label(" "+value);
        nameLabel.getStyle().set("color",COLOR_NORMAL);

        item.add(nameLabel,valueLabel);
        return item;
    }

    public HorizontalLayout addInfo (String name, boolean colorEnabled, boolean status) {
        HorizontalLayout item = new HorizontalLayout();
        Label nameLabel = new Label(""+name);
        setComponentColor(nameLabel, colorEnabled, status);

        item.add(nameLabel);
        return item;
    }

    public Button addButton(String name, boolean colorEnabled, boolean status) {

        Button button = new Button(name);
        setActualColor(button,status);
        return button;
    }

    public NumberField addNumberField(String name, double initValue, double min, double max, double step) {
        NumberField numberField = new NumberField(name);
        numberField.setHasControls(true);
        numberField.setValue(initValue);
        numberField.setStep(0.5d);
        numberField.setMin(min);
        numberField.setMax(max);
        return numberField;
    }

    private Date getCurrentDate() {
        return new Date();
    }

    private void setComponentColor(HtmlContainer component, boolean colorEnabled, boolean exceedAlarm, Number isValue, Number expectedValue, Number warningLimit, Number alarmLimit) {
        if (colorEnabled) {
            if (Math.abs(isValue.doubleValue() - expectedValue.doubleValue()) > alarmLimit.doubleValue())
                component.getStyle().set("color", COLOR_ALARM);
            else if (Math.abs(isValue.doubleValue() - expectedValue.doubleValue()) > warningLimit.doubleValue())
                component.getStyle().set("color", COLOR_WARNING);
            else
                component.getStyle().set("color", COLOR_OK);
            if (!exceedAlarm) {
                if ((!component.getStyle().get("color").equals(COLOR_OK)) && (isValue.doubleValue()>expectedValue.doubleValue()))
                    component.getStyle().set("color",COLOR_OK);
            }
        }
        else component.getStyle().set("color", COLOR_NORMAL);
    }

    private void setComponentColor(HtmlContainer component, boolean colorEnabled, boolean status) {
        if (colorEnabled) {
            if (status) component.getStyle().set("color", COLOR_ON);
            else component.getStyle().set("color",COLOR_OFF);
        }
        else component.getStyle().set("color", COLOR_NORMAL);
    }

    public void setPendingColor(HasStyle hasStyle) {
        hasStyle.getStyle().set("color",COLOR_NV);
    }

    public void setActualColor(HasStyle hasStyle, boolean status) {
        if (status) hasStyle.getStyle().set("color",COLOR_ON);
        else hasStyle.getStyle().set("color",COLOR_OFF);
    }

    public static boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken) //
                && authentication.isAuthenticated();
    }
}
