package fr.univtln.lhd.controllers;

import fr.univtln.lhd.exceptions.IdException;
import fr.univtln.lhd.facade.Schedule;
import fr.univtln.lhd.model.entities.slots.Classroom;
import fr.univtln.lhd.model.entities.slots.Group;
import fr.univtln.lhd.model.entities.slots.Slot;
import fr.univtln.lhd.model.entities.slots.Subject;
import fr.univtln.lhd.model.entities.users.Professor;
import fr.univtln.lhd.view.authentification.Auth;
import fr.univtln.lhd.view.edt.EdtGrid;
import fr.univtln.lhd.view.slots.SlotUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.threeten.extra.Interval;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Slot Info Panel
 */
@Slf4j
public class SlotInfoController {
    public VBox box;
    private EdtLhdController controller;

    @FXML public BorderPane slotInfoPanel;
    @FXML private Label sipTitle;
    @FXML private Button sipDeleteBtn;
    @FXML private ComboBox<Subject> sipSName;
    @FXML private ComboBox<Professor> sipSPName;
    @FXML private ComboBox<Group> sipSgName;
    @FXML private ComboBox<Slot.SlotType> sipSType;
    @FXML private ComboBox<Classroom> sipSClassName;
    @FXML private DatePicker sipSDate;
    @FXML private ComboBox<String> sipSHourStart;
    @FXML private ComboBox<String> sipSHourEnd;
    @FXML private ProgressIndicator sipSProgress;
    @FXML private TextField sipSMaxHour;
    @FXML private Button sipOkBtn;
    @FXML private Button sipCancelBtn;

    private Auth currentAuth;

    private enum SlotManagementType {AJOUTER, MODIFIER, LIRE}
    private SlotManagementType managementType;

    private SlotUI slotUI;

    /**
     * Base method call from Parent Controller (aka EdtLhdController)
     * @param controller EdtLhdController parent
     */
    public void setParentController(EdtLhdController controller){
        this.controller = controller;
        this.currentAuth = controller.getCurrentAuth();

        slotInfoPanel.getStyleClass().add("slotInfoPanel");

        setupSlotInfoPanel();
    }

    public VBox getBox() {
        return box;
    }

    /**
     * Getter for SlotUi contained inside this Panel
     * @return SlotUi Entity or null
     */
    public SlotUI getSlotUI() { return slotUI; }

    /**
     * Show Slot Info Panel
     */
    public void showSlotInfoPanel() { slotInfoPanel.setVisible(true); }

    /**
     * Hide Slot Info Panel
     */
    public void hideSlotInfoPanel() {
        slotInfoPanel.setVisible(false); }

    /**
     * Toggle Show/Hide Slot Info Panel
     */
    public void toggleSlotInfoPanel(){
        if (slotInfoPanel.isVisible())
            hideSlotInfoPanel();
        else
            showSlotInfoPanel();
    }

    /**
     * Show Slot Info Panel for Adding a Slot
     */
    public void showAddPanel(){
        managementType = SlotManagementType.AJOUTER;
        controller.setLastSlotClicked(null); //resets last clicked slot
        //showSlotInfoPanel();
        toggleSlotInfoPanel();
        clearPanelInfo();

        sipDeleteBtn.setVisible(false);
        sipTitle.setVisible(true);
        sipTitle.setText( managementType.name() );
    }

    /**
     * Show Slot Info Panel given a SlotUi (generally called when clicking on a slot on the planning)
     * @param slotUI SlotUi Entity from the planning
     */
    public void showSlotInfoPanel(SlotUI slotUI){
        this.slotUI = slotUI;
        showSlotInfoPanel();
        sipDeleteBtn.setVisible(currentAuth.isAdmin());
        populateSlotInfoPanel();
    }

    /**
     * Setting up All Combo Boxes on slot info panel
     */
    private void setupAllComboBoxes(){
        for (Subject subject : Schedule.getAllSubjects()){
            //sipSName.getItems().removeAll(subject);
            sipSName.getItems().add(subject);
        }
        sipSPName.getItems().add(null);
        for (Professor professor : Schedule.getAllProfessors()){
            //sipSPName.getItems().removeAll(professor);
            sipSPName.getItems().add(professor);
        }

        for (Group group : Schedule.getAllGroups()){
            //sipSgName.getItems().removeAll(group);
            sipSgName.getItems().add(group);
        }

        for (Slot.SlotType type : Slot.SlotType.values()){
            //sipSType.getItems().removeAll(type);
            sipSType.getItems().add(type);
        }

        for (Classroom classroom : Schedule.getAllClassrooms()){
            //sipSClassName.getItems().removeAll(classroom);
            sipSClassName.getItems().add(classroom);
        }

        for (int i = 8; i < 18; i++){
            String hourString = EdtGrid.convertIntToHourLabel(i);
            //sipSHourStart.getItems().removeAll(hourString);
            sipSHourStart.getItems().add(hourString);
            //sipSHourEnd.getItems().removeAll(hourString);
            sipSHourEnd.getItems().add(hourString);
        }
    }

    /**
     * Setting up base information on Slot Info Panel
     * populate SlotType Combo Box
     * Show/Hide some fields depending on which types of user is logged in
     */
    private void setupSlotInfoPanel(){
        setupAllComboBoxes();

        boolean isAdmin = currentAuth.isAdmin();

        if(!isAdmin){
            managementType = SlotManagementType.LIRE;
            sipOkBtn.setText("Ok");
            sipCancelBtn.setText("Fermer");
        }else{
            sipOkBtn.setText("Sauvegarder");
            sipCancelBtn.setText("Annuler");
        }

        sipTitle.setVisible(isAdmin);
        sipDeleteBtn.setVisible(isAdmin);
        sipSName.setDisable(!isAdmin);
        sipSPName.setDisable(!isAdmin);
        sipSType.setDisable(!isAdmin);
        sipSClassName.setDisable(!isAdmin);
        sipSgName.setDisable(!isAdmin);
        sipSDate.setDisable(!isAdmin);
        sipSProgress.setDisable(!isAdmin);
        sipSMaxHour.setDisable(true);
        sipSHourStart.setDisable(!isAdmin);
        sipSHourEnd.setDisable(!isAdmin);
    }

    /**
     * Clears every field to default value
     */
    private void clearPanelInfo(){
        sipSName.setValue(null);
        sipSPName.setValue(null);
        sipSType.setValue(Slot.SlotType.CM);
        sipSClassName.setValue(null);
        sipSgName.setValue(null);

        sipSDate.setValue( LocalDate.now() );
        sipSProgress.setProgress(0);
        sipSMaxHour.setText("");
        sipSHourStart.setValue("");
        sipSHourEnd.setValue("");
    }

    public void clearAllEntry(){
        sipSName.getItems().removeAll(Schedule.getAllSubjects());
        sipSClassName.getItems().removeAll(Schedule.getAllClassrooms());
        sipSPName.getItems().removeAll(Schedule.getAllProfessors());
        sipSPName.getItems().remove(null);
        sipSgName.getItems().removeAll(Schedule.getAllGroups());
        sipSType.getItems().removeAll(Slot.SlotType.values());
        for (int i = 8; i < 18; i++){
            String hourString = EdtGrid.convertIntToHourLabel(i);
            sipSHourStart.getItems().removeAll(hourString);
            sipSHourEnd.getItems().removeAll(hourString);
        }
    }

    /**
     * Populate Slot Info Panel from current slotUi
     */
    private void populateSlotInfoPanel(){
        Slot slot = slotUI.getSlot();

        slotInfoPanel.setStyle("-fx-border-color: " + slotUI.getAssociatedColor());

        if (currentAuth.isAdmin()){
            managementType = SlotManagementType.MODIFIER;
            sipTitle.setText( managementType.name() );
        }

        sipSName.setValue( slot.getSubject() );

        if (!slot.getProfessors().isEmpty())
            sipSPName.setValue( slot.getProfessors().get(0) );
        else
            sipSPName.setValue(null);

        sipSType.setValue( slot.getType() );

        sipSClassName.setValue( slot.getClassroom() );

        if (!slot.getGroup().isEmpty())
            sipSgName.setValue( slot.getGroup().get(0) );
        else
            sipSgName.setValue(null);

        sipSDate.setValue( slot.getTimeRange().getStart().atZone(ZoneId.systemDefault()).toLocalDate() );
        final double percent=controller.getSlotFinishedPercent( slot.getGroup().get(0), slot);
        sipSProgress.setProgress( percent );
        final double hourCountMax=slot.getSubject().getHourCountMax();
        sipSMaxHour.setText(  (int) (percent * hourCountMax) + " / " + (int) hourCountMax + "h");

        String[] hour = slot.getDisplayTimeInterval().split(" - ");

        sipSHourStart.setValue(hour[0].substring(0, 3) + "00");
        sipSHourEnd.setValue(hour[1].substring(0, 3) + "00");
    }

    private Interval getDateTimeInstant(){
        String startDateTime = sipSDate.getValue().toString() + "T" + sipSHourStart.getValue() + ":00.00Z";
        String endDateTime = sipSDate.getValue().toString() + "T" + sipSHourEnd.getValue() + ":00.00Z";

        Instant start = Instant.parse(startDateTime).atZone(ZoneId.systemDefault()).minusHours(1).toInstant();
        Instant end = Instant.parse(endDateTime).atZone(ZoneId.systemDefault()).minusHours(1).toInstant();
        if (start.isBefore(end))
            return Interval.of( start, end);
        return Interval.of(end, start);
    }

    private Slot getSlotFromPanel(){
        List<Group> groupList = new ArrayList<>();
        if (sipSgName.getValue() != null)
            groupList.add(sipSgName.getValue());

        List<Professor> professorList = new ArrayList<>();
        if (sipSPName.getValue() != null)
            professorList.add(sipSPName.getValue());

        return Slot.getInstance(
                sipSType.getValue(),
                sipSClassName.getValue(),
                sipSName.getValue(),
                groupList,
                professorList,
                getDateTimeInstant()
        );
    }

    /**
     * Add new slot
     * Gets all information from each field
     * Calls a method on parent Controller to create a new slot
     */
    private void addNewSlot(){
        //wip
        //need to get all panel entry, maybe call method on edt lhd controller which calls schedule to convert entry to right type
        //exemple, classroom name entry -> string, needs to be Classroom Entity from database
        //need to add method to create subject or classroom in schedule
        if (Schedule.addToSchedule( getSlotFromPanel() ))
            log.error("ADD ERROR");
        hideSlotInfoPanel();
    }

    /**
     * Modify a slot
     * Gets all information from each field
     * Calls a method on parent Controller to modify old Slot with new modified information
     */
    private void modifySlot(){
        Slot oldSlot = getSlotUI().getSlot();

        if(Schedule.updateInSchedule(oldSlot, getSlotFromPanel()))
            log.error("MODIFY ERROR");

        hideSlotInfoPanel();
    }

    /**
     * Delete a slot
     * Gets all information from each field
     * Calls a method on parent Controller to delete the selected slot
     */
    private void deleteSlot(){
        Slot slotToDelete = getSlotFromPanel();
        try {
            slotToDelete.setId( getSlotUI().getSlot().getId() );
        } catch (IdException e){
            log.error(e.getMessage());
        }

        if(Schedule.deleteInSchedule( slotToDelete ))
            log.error("DELETE ERROR");

        hideSlotInfoPanel();
    }

    //region BUTTON EVENT HANDLER

    /**
     * Callback Method when clicking on the ok button
     * do different action based on managementType
     * - ADD => add new slot
     * - MODIFY => modify a slot
     * - default / READ => hide slot info panel
     * @param actionEvent ActionEvent
     */
    @FXML public void sipOkBtnOnClick(ActionEvent actionEvent) {
        switch (managementType){
            case AJOUTER -> addNewSlot();
            case MODIFIER -> modifySlot();
            default -> hideSlotInfoPanel();
        }
    }

    /**
     * Callback method when clicking on the cancel button
     * Hides the slot info panel
     * @param actionEvent ActionEvent
     */
    @FXML public void sipCancelBtnOnClick(ActionEvent actionEvent) { hideSlotInfoPanel(); }

    /**
     * Callback method when clicking on the delete button
     * Delete currently selected slot
     * @param actionEvent ActionEvent
     */
    @FXML public void sipDeleteBtnOnClick(ActionEvent actionEvent) { deleteSlot(); }
    //endregion
}
