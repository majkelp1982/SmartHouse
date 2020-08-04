package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.pomazanka.SmartHouse.backend.dataStruct.Diagnostic;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module;

import java.text.SimpleDateFormat;
import java.util.Date;

public class View extends VerticalLayout {

    // static variables
    //***************************************
    public static final String COLOR_WARNING = "yellow";
    public static final String COLOR_ALARM = "red";
    public static final String COLOR_OK = "green";
    public static final String COLOR_ON = "green";
    public static final String COLOR_NORMAL = "white";
    public static final String COLOR_OFF = "grey";
    public static final String COLOR_NV = "orange";

    //Last telegram updates info
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public class Header {
        private HorizontalLayout header;
        private Label lastUpdateLabel;
        private Label diagnoseUpdateLabel;

        //Header for Modules
        public Header(Module module, String imageSrc) {
            header = new HorizontalLayout();

            //About module type
            Image image = new Image(imageSrc, imageSrc);
            image.setHeight("80px");
            Label moduleTyp = new Label(module.getModuleName());
            moduleTyp.getStyle().set("font-size", "30px");

            lastUpdateLabel = new Label();
            diagnoseUpdateLabel = new Label();
            VerticalLayout info = new VerticalLayout();
            info.setAlignItems(FlexComponent.Alignment.CENTER);
            info.add(lastUpdateLabel, diagnoseUpdateLabel);
            info.setWidth("800px");
            info.setSizeFull();

            header.addClassName("module");
            header.setMinWidth("800px");
            header.setSizeFull();
            header.setHeight("80px");
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.add(image, moduleTyp, info);
        }

        //Header for diagnostic view
        public Header(Diagnostic diagnostic, String imageSrc) {
            header = new HorizontalLayout();

            //About module type
            Image image = new Image(imageSrc, imageSrc);
            image.setHeight("80px");
            Label moduleTyp = new Label(diagnostic.getModuleName());
            moduleTyp.getStyle().set("font-size", "30px");

            diagnoseUpdateLabel = new Label();
            VerticalLayout info = new VerticalLayout();
            info.setAlignItems(FlexComponent.Alignment.CENTER);
            info.add(diagnoseUpdateLabel);
            info.setWidth("800px");
            info.setSizeFull();

            header.addClassName("module");
            header.setMinWidth("800px");
            header.setSizeFull();
            header.setHeight("80px");
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.add(image, moduleTyp, info);
        }

        public HorizontalLayout getHeader() {
            return header;
        }

        public void setLastUpdate(Date lastUpdate) {
            lastUpdateLabel.setText("Update : " + simpleDateFormat.format(lastUpdate));
            setComponentColor(lastUpdateLabel, true, true, lastUpdate.getTime(), getCurrentDate().getTime(), 60000, 120000);
        }

        public void setDiagnoseUpdate(Date diagnoseUpdate) {
            diagnoseUpdateLabel.setText("Diagnose : " + simpleDateFormat.format(diagnoseUpdate));
            setComponentColor(diagnoseUpdateLabel, true, true, diagnoseUpdate.getTime(), getCurrentDate().getTime(), 60000, 120000);
        }
    }

    public class Section {
        private HorizontalLayout section;
        private Tile[] tile = new Tile[10];

        public Section() {
            section = new HorizontalLayout();
        }

        public void createTile(String imageSrc, String tittle) {
            int tileNo = 0;
            while (tile[tileNo] != null) {
                tileNo++;
            }
            tile[tileNo] = new Tile(imageSrc, tittle);

            section.add(tile[tileNo].getTile());
        }

        public VerticalLayout getTileDetailsContainer(int tileNo) {
            return tile[tileNo].getTileDetailsContainer();
        }

        public HorizontalLayout getSection() {
            return section;
        }

    }

    private class Tile {
        HorizontalLayout tile;
        VerticalLayout detailsContainer;

        public Tile(String imageSrc, String tittle) {
            tile = new HorizontalLayout();
            tile.setAlignItems(FlexComponent.Alignment.CENTER);
            tile.addClassName("module");

            Image image = new Image(imageSrc, imageSrc);
            image.setHeight("50px");
            Label tittleLabel = new Label(tittle);
            detailsContainer = new VerticalLayout();
            tile.add(tittleLabel, image, detailsContainer);
        }

        private HorizontalLayout getTile() {
            return tile;
        }

        public VerticalLayout getTileDetailsContainer() {
            return detailsContainer;
        }

    }

    public class Info {
        private HorizontalLayout info;
        private Label nameLabel;
        private Label valueLabel;
        private String unit;
        private boolean colorEnabled;
        private boolean exceedAlarm;
        private Number expectedValue;
        private Number warningLimit;
        private Number alarmLimit;

        public Info(String name, String unit, boolean colorEnabled, boolean exceedAlarm, Number isValue, Number expectedValue, Number warningLimit, Number alarmLimit) {
            this.unit = unit;
            this.colorEnabled = colorEnabled;
            this.exceedAlarm = exceedAlarm;
            this.expectedValue = expectedValue;
            this.warningLimit = warningLimit;
            this.alarmLimit = alarmLimit;

            info = new HorizontalLayout();
            nameLabel = new Label("" + name);
            nameLabel.getStyle().set("color", COLOR_NORMAL);
            valueLabel = new Label();
            setValue(isValue);
            info.add(nameLabel, valueLabel);
        }

        public Info(String name, String value) {
            info = new HorizontalLayout();
            nameLabel = new Label("" + name);
            this.colorEnabled = false;
            nameLabel.getStyle().set("color", COLOR_NORMAL);
            valueLabel = new Label();
            nameLabel.getStyle().set("color", COLOR_NORMAL);
            setValue(value);
            info.add(nameLabel, valueLabel);
        }

        public Info(String name, boolean colorEnabled, boolean status) {
            info = new HorizontalLayout();
            this.colorEnabled = colorEnabled;
            nameLabel = new Label(name);
            setValue(status);
            info.add(nameLabel);
        }

        public void setValue(Number isValue) {
            valueLabel.setText(" " + isValue + "[" + unit + "]");
            setComponentColor(valueLabel, colorEnabled, exceedAlarm, isValue, expectedValue, warningLimit, alarmLimit);
        }

        public void setValue(String isValue) {
            valueLabel.setText(" " + isValue);
            setComponentColor(nameLabel, colorEnabled, true);
        }

        public void setValue(boolean status) {
            setComponentColor(nameLabel, colorEnabled, status);
        }


        public HorizontalLayout getSource() {
            return info;
        }
        public Label getNameLabel() {
            return nameLabel;
        }
    }

    public class Button {
        com.vaadin.flow.component.button.Button button;

        public Button(String name, boolean colorEnabled, boolean status) {
            button = new com.vaadin.flow.component.button.Button();
            button.setText(name);
            setButtonColor(status, status);
        }

        public void setButtonColor(boolean isStatus, boolean expectedStatus) {
            setActualColor(button, isStatus);
            if (isStatus == expectedStatus) {
                setActualColor(button, isStatus);
            }
            else {
                setPendingColor(button);
            }
        }

        public com.vaadin.flow.component.button.Button getSource() {
            return button;
        }
    }

    public class NumberField {
        com.vaadin.flow.component.textfield.NumberField numberField;

        public NumberField(String name, double initValue, double min, double max, double step) {
            numberField = new com.vaadin.flow.component.textfield.NumberField(name);
            numberField.setHasControls(true);
            numberField.setValue(initValue);
            numberField.setStep(0.5d);
            numberField.setMin(min);
            numberField.setMax(max);
        }

        public void setNumberField(double isValue, double expectedValue) {
            if (isValue == expectedValue) {
                setActualColor(numberField, true);
                numberField.setValue(isValue);
            }
            else {
                setPendingColor(numberField);
                numberField.setValue(expectedValue);
            }
        }

        public com.vaadin.flow.component.textfield.NumberField getSource() {
            return numberField;
        }
    }

    public Date getCurrentDate() {
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
                if ((!component.getStyle().get("color").equals(COLOR_OK)) && (isValue.doubleValue() > expectedValue.doubleValue()))
                    component.getStyle().set("color", COLOR_OK);
            }
        } else component.getStyle().set("color", COLOR_NORMAL);
    }

    private void setComponentColor(HtmlContainer component, boolean colorEnabled, boolean status) {
        if (colorEnabled) {
            if (status) component.getStyle().set("color", COLOR_ON);
            else component.getStyle().set("color", COLOR_OFF);
        } else component.getStyle().set("color", COLOR_NORMAL);
    }

    private void setActualColor(HasStyle hasStyle, boolean status) {
        if (status) hasStyle.getStyle().set("color", COLOR_ON);
        else hasStyle.getStyle().set("color", COLOR_OFF);
    }

    public void setPendingColor(HasStyle hasStyle) {
        hasStyle.getStyle().set("color", COLOR_NV);
    }

    public static boolean isUserLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken) //
                && authentication.isAuthenticated();
    }
}
