package pl.pomazanka.SmartHouse.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Element;
import pl.pomazanka.SmartHouse.backend.dataStruct.Module;

import java.text.SimpleDateFormat;
import java.util.Date;

public class View extends VerticalLayout{

    // static variables
    //***************************************
    public static final String COLOR_WARNING = "yellow";
    public static final String COLOR_ALARM = "red";
    public static final String COLOR_OK = "green";
    public static final String COLOR_ON = "green";
    public static final String COLOR_NORMAL = "white";
    public static final String COLOR_OFF ="grey";
    public static final String COLOR_NV = "orange";

    //Last telegram updates info
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public class Header {
        private HorizontalLayout header;
        private Label lastUpdateLabel;
        private Label diagnoseUpdateLabel;

        public  Header(Module module, String imageSrc) {
            header = new HorizontalLayout();

            //About module type
            Image image = new Image(imageSrc, imageSrc);
            image.setHeight("80px");
            Label moduleTyp = new Label(module.getModuleName());
            moduleTyp.getStyle().set("font-size","30px");

            lastUpdateLabel = new Label();
            diagnoseUpdateLabel = new Label();
            VerticalLayout info = new VerticalLayout();
            info.setAlignItems(FlexComponent.Alignment.CENTER);
            info.add(lastUpdateLabel,diagnoseUpdateLabel);
            info.setWidth("800px");
            info.setSizeFull();

            header.addClassName("module");
            header.setMinWidth("800px");
            header.setSizeFull();
            header.setHeight("80px");
            header.setAlignItems(FlexComponent.Alignment.CENTER);
            header.add(image,moduleTyp,info);
        }

        public HorizontalLayout getHeader() {
            return header;
        }

        public void setLastUpdate(Date lastUpdate) {
            lastUpdateLabel.setText("Update : "+simpleDateFormat.format(lastUpdate));
            setComponentColor(lastUpdateLabel,true,true,lastUpdate.getTime(),getCurrentDate().getTime(),60000, 120000);
        }

        public void setDiagnoseUpdate(Date diagnoseUpdate) {
            diagnoseUpdateLabel.setText("Diagnose : "+simpleDateFormat.format(diagnoseUpdate));
            setComponentColor(diagnoseUpdateLabel,true, true, diagnoseUpdate.getTime(),getCurrentDate().getTime(),60000, 120000);
        }
    }

    public class Section {
        private HorizontalLayout section;
        private HorizontalLayout[] tile = new HorizontalLayout[10];

        public Section() {
            section = new HorizontalLayout();
        }

        public void createTile(String imageSrc, String tittle) {
            int tileNo = 0;
            while (tile[tileNo] != null) {
                tileNo++;
            }
            tile[tileNo] = new HorizontalLayout();
            tile[tileNo].setAlignItems(FlexComponent.Alignment.CENTER);
            tile[tileNo].addClassName("module");

            Image image = new Image(imageSrc, imageSrc);
            image.setHeight("50px");

            Label tittleLabel = new Label(tittle);

            VerticalLayout detailsContainer = new VerticalLayout();

            tile[tileNo].add(tittleLabel, image, detailsContainer);

            section.add(tile[tileNo]);
        }

        public Component getTileDetailsContainer(int tileNo) {
            return tile[tileNo].getElemen
        }

        public HorizontalLayout getSection() {
            return section;
        }

    }

    public class Info {
        private HorizontalLayout info;
        private Label nameLabel;
        private Label valueLabel;
        private String unit;
        private boolean colorEnabled;
        private boolean exceedAlarm;
        private Number isValue;
        private Number expectedValue;
        private Number warningLimit;
        private Number alarmLimit;

        public  Info (String name, String unit, boolean colorEnabled, boolean exceedAlarm, Number isValue, Number expectedValue, Number warningLimit, Number alarmLimit) {
            this.unit = unit;
            this.colorEnabled = colorEnabled;
            this.exceedAlarm = exceedAlarm;
            this.isValue = isValue;
            this.expectedValue = expectedValue;
            this.warningLimit = warningLimit;
            this.alarmLimit = alarmLimit;

            info = new HorizontalLayout();
            nameLabel = new Label(""+name);
            nameLabel.getStyle().set("color",COLOR_NORMAL);
            valueLabel = new Label();
            info.add(nameLabel,valueLabel);
        }

        public void setValue(Number isValue) {
            this.isValue = isValue;
            valueLabel = new Label(" "+isValue+"["+unit+"]");
            setComponentColor(valueLabel,colorEnabled,exceedAlarm,isValue,expectedValue,warningLimit,alarmLimit);
        }

        public HorizontalLayout getInfo() {
            return info;
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

}
