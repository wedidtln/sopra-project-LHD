package fr.univtln.lhd.view.edt;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.threeten.extra.Interval;

public class EdtGrid extends Grid {

    public enum Days { LUNDI, MARDI, MERCREDI, JEUDI, VENDREDI }
    private final List<String> hours;

    private EdtGrid(int rowNumber, int columnNumber){
        super( Grid.builder(rowNumber, columnNumber) );

        hours = new ArrayList<>();
        for (int i = 8; i < 18; i++) {
            if (i < 10)
                hours.add("0" + i + ":00");
            else
                hours.add(i + ":00");
        }

        for (int i = 0; i < Days.values().length; i++){
            FlowPane f = new FlowPane(new Label( Days.values()[i].name() ));
            f.getStyleClass().add("day");
            add(f, 0, i+1);
        }

        FlowPane empty = new FlowPane();
        empty.getStyleClass().add("hour");
        add(empty, 0, 0);

        for (int i = 0; i < hours.size(); i++){
            FlowPane f = new FlowPane( new Label( hours.get(i) ) );
            f.getStyleClass().add("hour");
            add(f, i+1, 0);
        }

        this.modifyColumnConstraints(0, 65);
    }

    public static EdtGrid getInstance(){ return new EdtGrid(11, 6); }

    private String convertIntToHourLabel(int hour){
        String hourStart = "";
        if (hour < 10) hourStart = "0" + hour + ":00";
        else hourStart = hour + ":00";
        return hourStart;
    }

    public void add(Node node, String day, String hourStart, String hourEnd){
        int rowIndex = hours.indexOf(hourStart);
        if (rowIndex == -1) rowIndex = 0;

        int rowIndexEnd = hours.indexOf(hourEnd);
        if (rowIndexEnd == -1) rowIndexEnd = hours.size()-1;

        int columnIndex = Days.valueOf(day).ordinal()+1;
        super.add(node, rowIndex+1, columnIndex, (rowIndexEnd+1) - (rowIndex+1), 1);
    }

    public void add(Node node, Days day, String hourStart, String hourEnd){
        add(node, day.name(), hourStart, hourEnd);
    }

    public void add(Node node, Interval interval){
        LocalDateTime localDateTimeStart = LocalDateTime.ofInstant(interval.getStart(), ZoneId.systemDefault());
        LocalTime localTimeEnd = LocalTime.ofInstant(interval.getEnd(), ZoneId.systemDefault());

        Days day = Days.values()[ localDateTimeStart.getDayOfWeek().ordinal()-1 ];

        String hourStart = convertIntToHourLabel( localDateTimeStart.getHour() );
        String hourEnd = convertIntToHourLabel( localTimeEnd.getHour() );

        add(node, day, hourStart, hourEnd);
    }
    /*
    public void add(SlotUI slotUI){
        //get slot ui info, then call add (node, day, hourstart, hourend)
        //wip
    }
     */
}
