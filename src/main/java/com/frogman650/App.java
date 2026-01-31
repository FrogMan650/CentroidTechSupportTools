package com.frogman650;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    public static String[] directories = {"cncm", "cnct", "cncr", "cncp", "cncl"};
    public static String[] machines = {"mill", "lathe", "router", "plasma", "laser"};
    public static ArrayList<ToggleButton> toggleButtonArray = new ArrayList<>();
    public static File executableDirectory;
    public static File logFile;
    public static ArrayList<Directory> directoryArray = new ArrayList<>();
    public static TableView<Directory> directoryTableView;
    public static TableColumn<Directory, String> activeColumn;
    public static TableColumn<Directory, String> machineTypeColumn;
    public static TableColumn<Directory, String> versionColumn;
    public static ContextMenu directoryManagerContextMenu;
    public static Directory selectedDirectory;
    public static HostServices hostService;
    public static MenuItem deactivateAllMenuItem;
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //define constants
        URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        executableDirectory = Paths.get(uri).getParent().toFile();
        logFile = new File(executableDirectory, "logs.txt");
        hostService = getHostServices();

        //define everything we need for the base GUI
        AnchorPane installTweakerAnchor = new AnchorPane();
        AnchorPane directoryManagerAnchor = new AnchorPane();
        AnchorPane helpAnchor = new AnchorPane();
        Tab installTweakerTab = new Tab("Install tweaker", installTweakerAnchor);
        Tab directoryManagerTab = new Tab("Directory manager", directoryManagerAnchor);
        Tab helpTab = new Tab("Help", helpAnchor);
        installTweakerTab.setClosable(false);
        directoryManagerTab.setClosable(false);
        helpTab.setClosable(false);
        TabPane root = new TabPane(directoryManagerTab, installTweakerTab, helpTab);
        Scene scene = new Scene(root, Color.BLACK);
        Image icon = new Image(App.class.getResourceAsStream("LK_logo_square.png"));

        /*=============================================
                        Help start
        ==============================================*/
        Label helpDirectoryManagerHeaderLabel = new Label("Directory Manager");
        helpDirectoryManagerHeaderLabel.setId("helpHeaderLabel");
        Label helpDirectoryManagerbodyLabel = new Label("'Activate' and 'Deactivate' CNC12 installations.\nOn startup or Refresh " +
            "the C: drive will be checked for any directories with names containing: cncm, cnct, cncr, cncp, or cncl. Information " +
            "from these directories will then be pulled into the spreadsheet style interface. Here you can see if the directory is " +
            "Active, the machine type, software version, date and time last modified, the path, and the first line from machine_notes.txt. " +
            "By default the list is sorted by Active, Machine, and then Version but can be sorted differently by clicking the column headers.\n" +
            "The table can be filtered by Board and Machine type using the buttons along the left side. Clicking the Filter button will " +
            "turn on or off all filters at once.\n" +
            "Left or right click a row in the table to select that row.\n" +
            "Double left click a row to Activate the directory and start the executable.\n" +
            "Right click for a context menu to select from several different functions for interacting with the selected installation:\n" +
            "Refresh: Will force a refresh of the installation list.\n" +
            "Activate: Will 'Activate' the selected installation meaning that the directory will be renamed properly to " +
            "cncm, cnct, etc. This, if necessary, will also deactivate other directories.\n" +
            "Deactivate: Will 'Deactivate' the selected installation renaming the directory. Example: " +
            "cncm_5.40.04_acorn_01-26-26_10.40.23\n" +
            "Open Directory: Opens a file explorer to the path of the selected installation.\n" +
            "Open Notes: Opens the machine_notes.txt of the selected installation. If machine_notes.txt does not exist " +
            "it is created.\n" +
            "Open Message Log: Opens the msg_log.txt of the selected installation.\n" +
            "Start Executable: Starts CNC12 from the selected installation. Note that this will launch the executable from the " +
            "installation whether it's active or not.\n" +
            "Deactivate All: Will deactivate all active installations."
        );
        helpDirectoryManagerbodyLabel.setId("helpBodyLabel");
        VBox helpDirectoryManagerVBox = new VBox(helpDirectoryManagerHeaderLabel, helpDirectoryManagerbodyLabel);
        helpDirectoryManagerVBox.setId("helpVBox");
        Label helpInstallTweakerHeaderLabel = new Label("Install Tweaker");
        helpInstallTweakerHeaderLabel.setId("helpHeaderLabel");
        Label helpInstallTweakerbodyLabel = new Label("Work in progress. Will be for applying various changes to active " +
            "installations such as loopback parameters, disable config password, etc."
        );
        helpInstallTweakerbodyLabel.setId("helpBodyLabel");
        VBox helpInstallTweakerVBox = new VBox(helpInstallTweakerHeaderLabel, helpInstallTweakerbodyLabel);
        helpInstallTweakerVBox.setId("helpVBox");
        VBox helpTabMainVBox = new VBox(helpDirectoryManagerVBox, helpInstallTweakerVBox);
        AnchorPane scrollPaneAnchor = new AnchorPane(helpTabMainVBox);
        ScrollPane helpScrollPane = new ScrollPane(scrollPaneAnchor);
        helpAnchor.getChildren().add(helpScrollPane);
        AnchorPane.setTopAnchor(helpScrollPane, 0.0);
        AnchorPane.setBottomAnchor(helpScrollPane, 0.0);
        AnchorPane.setRightAnchor(helpScrollPane, 0.0);
        AnchorPane.setLeftAnchor(helpScrollPane, 0.0);
        /*=============================================
                        Help end
        ==============================================*/

        /*=============================================
                    Directory Manager start
        ==============================================*/
        //columns
        activeColumn = new TableColumn<>("Active");
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setPrefWidth(90);
        activeColumn.setSortType(TableColumn.SortType.DESCENDING);
        machineTypeColumn = new TableColumn<>("Machine");
        machineTypeColumn.setCellValueFactory(new PropertyValueFactory<>("machineType"));
        machineTypeColumn.setPrefWidth(107);
        versionColumn = new TableColumn<>("Version");
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        versionColumn.setPrefWidth(107);
        TableColumn<Directory, String> boardColumn = new TableColumn<>("Board");
        boardColumn.setCellValueFactory(new PropertyValueFactory<>("board"));
        TableColumn<Directory, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        TableColumn<Directory, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Directory, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Directory, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        directoryTableView = new TableView<>();
        directoryTableView.setId("tableView");
        directoryTableView.getColumns().addAll(activeColumn, machineTypeColumn, versionColumn, 
            boardColumn, dateColumn, timeColumn, pathColumn, notesColumn);
        directoryTableView.setRowFactory(tv -> {
            TableRow<Directory> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    if (!selectedDirectory.getActive().equals("Yes")) {
                        activateDirectory(selectedDirectory);
                    }
                    try {
                        File executable = new File("C:/" + selectedDirectory.getBasePath(), selectedDirectory.getBasePath() + ".exe");
                        File executablePath = new File("C:/" + selectedDirectory.getBasePath());
                        if (executable.exists()) {
                            Runtime.getRuntime().exec(executable.toString(), null, executablePath);
                            writeToLogFile("Launching " + selectedDirectory.getBasePath() + ".exe...");
                        }
                    } catch (Exception e) {
                        writeToLogFile("Error starting executable: " + selectedDirectory.getBasePath() + ".exe", e.toString());
                    }
                    refreshTableView();
                }
            });
            return row;
        });
        //filters
        ToggleButton filterToggleButton1 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton1);
        Tooltip filterToggleButton1ToolTip = new Tooltip("Acorn");
        filterToggleButton1ToolTip.setId("toolTip");
        filterToggleButton1.setOnMouseMoved(event -> {
            filterToggleButton1ToolTip.show(filterToggleButton1, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton1.setOnMouseExited(event -> {
            filterToggleButton1ToolTip.hide();
        });
        ToggleButton filterToggleButton2 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton2);
        Tooltip filterToggleButton2ToolTip = new Tooltip("AcornSix");
        filterToggleButton2ToolTip.setId("toolTip");
        filterToggleButton2.setOnMouseMoved(event -> {
            filterToggleButton2ToolTip.show(filterToggleButton2, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton2.setOnMouseExited(event -> {
            filterToggleButton2ToolTip.hide();
        });
        ToggleButton filterToggleButton3 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton3);
        Tooltip filterToggleButton3ToolTip = new Tooltip("Hickory");
        filterToggleButton3ToolTip.setId("toolTip");
        filterToggleButton3.setOnMouseMoved(event -> {
            filterToggleButton3ToolTip.show(filterToggleButton3, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton3.setOnMouseExited(event -> {
            filterToggleButton3ToolTip.hide();
        });
        ToggleButton filterToggleButton4 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton4);
        Tooltip filterToggleButton4ToolTip = new Tooltip("Oak");
        filterToggleButton4ToolTip.setId("toolTip");
        filterToggleButton4.setOnMouseMoved(event -> {
            filterToggleButton4ToolTip.show(filterToggleButton4, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton4.setOnMouseExited(event -> {
            filterToggleButton4ToolTip.hide();
        });
        ToggleButton filterToggleButton5 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton5);
        Tooltip filterToggleButton5ToolTip = new Tooltip("Allin1DC");
        filterToggleButton5ToolTip.setId("toolTip");
        filterToggleButton5.setOnMouseMoved(event -> {
            filterToggleButton5ToolTip.show(filterToggleButton5, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton5.setOnMouseExited(event -> {
            filterToggleButton5ToolTip.hide();
        });
        ToggleButton filterToggleButton6 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton6);
        Tooltip filterToggleButton6ToolTip = new Tooltip("Laser");
        filterToggleButton6ToolTip.setId("toolTip");
        filterToggleButton6.setOnMouseMoved(event -> {
            filterToggleButton6ToolTip.show(filterToggleButton6, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton6.setOnMouseExited(event -> {
            filterToggleButton6ToolTip.hide();
        });
        ToggleButton filterToggleButton7 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton7);
        Tooltip filterToggleButton7ToolTip = new Tooltip("Lathe");
        filterToggleButton7ToolTip.setId("toolTip");
        filterToggleButton7.setOnMouseMoved(event -> {
            filterToggleButton7ToolTip.show(filterToggleButton7, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton7.setOnMouseExited(event -> {
            filterToggleButton7ToolTip.hide();
        });
        ToggleButton filterToggleButton8 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton8);
        Tooltip filterToggleButton8ToolTip = new Tooltip("Mill");
        filterToggleButton8ToolTip.setId("toolTip");
        filterToggleButton8.setOnMouseMoved(event -> {
            filterToggleButton8ToolTip.show(filterToggleButton8, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton8.setOnMouseExited(event -> {
            filterToggleButton8ToolTip.hide();
        });
        ToggleButton filterToggleButton9 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton9);
        Tooltip filterToggleButton9ToolTip = new Tooltip("Plasma");
        filterToggleButton9ToolTip.setId("toolTip");
        filterToggleButton9.setOnMouseMoved(event -> {
            filterToggleButton9ToolTip.show(filterToggleButton9, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton9.setOnMouseExited(event -> {
            filterToggleButton9ToolTip.hide();
        });
        ToggleButton filterToggleButton10 = new ToggleButton();
        toggleButtonArray.add(filterToggleButton10);
        Tooltip filterToggleButton10ToolTip = new Tooltip("Router");
        filterToggleButton10ToolTip.setId("toolTip");
        filterToggleButton10.setOnMouseMoved(event -> {
            filterToggleButton10ToolTip.show(filterToggleButton10, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterToggleButton10.setOnMouseExited(event -> {
            filterToggleButton10ToolTip.hide();
        });
        Button filterButton = new Button();
        filterButton.setId("filterButton");
        Tooltip filterButtonToolTip = new Tooltip("Toggle all filters");
        filterButtonToolTip.setId("toolTip");
        filterButton.setOnMouseMoved(event -> {
            filterButtonToolTip.show(filterButton, event.getScreenX() + 10, event.getScreenY() + 20);
        });
        filterButton.setOnMouseExited(event -> {
            filterButtonToolTip.hide();
        });
        filterButton.setOnAction(event -> {
            int allSelected = 0;
            for (ToggleButton toggleButton : toggleButtonArray) {
                if (toggleButton.isSelected()) {
                    for (ToggleButton toggleButtons : toggleButtonArray) {
                    toggleButtons.setSelected(false);
                    toggleButtons.setStyle("-fx-border-color: green;");
                    }
                } else {
                    allSelected += 1;
                }
            }
            if (allSelected == 10) {
                for (ToggleButton toggleButtons : toggleButtonArray) {
                    toggleButtons.setSelected(true);
                    toggleButtons.setStyle("-fx-border-color: red;");
                }
            }
            updateTableView();
        });
        for (int i = 0; i < toggleButtonArray.size(); i++) {
            int j = i;
            toggleButtonArray.get(j).setId("directoryToggleButton" + (j + 1));
            toggleButtonArray.get(j).setOnAction(event -> {
                if (toggleButtonArray.get(j).isSelected()) {
                    toggleButtonArray.get(j).setStyle("-fx-border-color: red;");
                } else {
                    toggleButtonArray.get(j).setStyle("-fx-border-color: green;");
                }
                updateTableView();
            });
        }
        VBox directoryFilterVBox = new VBox(filterButton, filterToggleButton1, filterToggleButton2, filterToggleButton3, filterToggleButton4,
            filterToggleButton5, filterToggleButton6, filterToggleButton7, filterToggleButton8, filterToggleButton9, filterToggleButton10);
        directoryFilterVBox.setId("filterVBox");
        directoryFilterVBox.setSpacing(5);
        //anchors
        AnchorPane.setTopAnchor(directoryTableView, 0.0);
        AnchorPane.setBottomAnchor(directoryTableView, 0.0);
        AnchorPane.setRightAnchor(directoryTableView, 0.0);
        AnchorPane.setLeftAnchor(directoryTableView, 50.0);
        AnchorPane.setTopAnchor(directoryFilterVBox, 0.0);
        AnchorPane.setBottomAnchor(directoryFilterVBox, 0.0);
        AnchorPane.setLeftAnchor(directoryFilterVBox, 0.0);
        //context menu
        MenuItem activateMenuItem = new MenuItem("Activate");
        activateMenuItem.setDisable(true);
        activateMenuItem.setOnAction(event -> {
            try {
                activateDirectory(selectedDirectory);
                refreshTableView();
            } catch (Exception e) {
                writeToLogFile("Error activating directory: " + selectedDirectory.getPath(), e.toString());
            }
        });
        MenuItem deactivateMenuItem = new MenuItem("Deactivate");
        deactivateMenuItem.setDisable(true);
        deactivateMenuItem.setOnAction(event -> {
            try {
                deactivateDirectory(selectedDirectory);
                refreshTableView();
            } catch (Exception e) {
                writeToLogFile("Error deactivating directory: " + selectedDirectory.getPath(), e.toString());
            }
        });
        MenuItem openDirectoryMenuItem = new MenuItem("Open Directory");
        openDirectoryMenuItem.setDisable(true);
        openDirectoryMenuItem.setOnAction(event -> {
            try {
                hostService.showDocument("C:/" + selectedDirectory.getPath());
                writeToLogFile("Opening directory: " + selectedDirectory.getPath());
            } catch (Exception e) {
                writeToLogFile("Error opening directory: " + "C:/" + selectedDirectory.getPath(), e.toString());
            }
        });
        MenuItem openNotesMenuItem = new MenuItem("Open Notes");
        openNotesMenuItem.setDisable(true);
        openNotesMenuItem.setOnAction(event -> {
            try {
                File machineNotes = new File("C:/" + selectedDirectory.getPath(), "machine_notes.txt");
                if (machineNotes.exists()) {
                    hostService.showDocument(machineNotes.toString());
                } else {
                    Files.write(machineNotes.toPath(), "".getBytes());
                    hostService.showDocument(machineNotes.toString());
                }
                writeToLogFile("Opening machine notes from " + selectedDirectory.getPath());
            } catch (Exception e) {
                writeToLogFile("Error opening notes: " + selectedDirectory.getPath(), e.toString());
            }
        });
        MenuItem openMsgLogMenuItem = new MenuItem("Open Message Log");
        openMsgLogMenuItem.setDisable(true);
        openMsgLogMenuItem.setOnAction(event -> {
            try {
                File msgLogFile = new File("C:/" + selectedDirectory.getPath(), "msg_log.txt");
                if (msgLogFile.exists()) {
                    hostService.showDocument(msgLogFile.toString());
                } else {
                    Files.write(msgLogFile.toPath(), "".getBytes());
                    hostService.showDocument(msgLogFile.toString());
                }
                writeToLogFile("Opening message log from " + selectedDirectory.getPath());
            } catch (Exception e) {
                writeToLogFile("Error opening message log: " + selectedDirectory.getPath(), e.toString());
            }
        });
        MenuItem openExeMenuItem = new MenuItem("Start Executable");
        openExeMenuItem.setDisable(true);
        openExeMenuItem.setOnAction(event -> {
            try {
                File executable = new File("C:/" + selectedDirectory.getPath(), selectedDirectory.getBasePath() + ".exe");
                File executablePath = new File("C:/" + selectedDirectory.getPath());
                if (executable.exists()) {
                    Runtime.getRuntime().exec(executable.toString(), null, executablePath);
                    writeToLogFile("Launching " + selectedDirectory.getBasePath() + ".exe from " + selectedDirectory.getPath());
                }
            } catch (Exception e) {
                writeToLogFile("Error starting executable: " + selectedDirectory.getPath() + ".exe", e.toString());
            }
        });
        deactivateAllMenuItem = new MenuItem("Deactivate All");
        deactivateAllMenuItem.setDisable(true);
        deactivateAllMenuItem.setOnAction(event -> {
            try {
                for (Directory dir : directoryArray) {
                    if (dir.getActive().equals("Yes")) {
                        deactivateDirectory(dir);
                    }
                }
                refreshTableView();
            } catch (Exception e) {
                writeToLogFile("Error starting executable: " + selectedDirectory.getPath() + ".exe", e.toString());
            }
        });
        MenuItem refreshMenuItem = new MenuItem("Refresh");
        refreshMenuItem.setOnAction(event -> {
            try {
                refreshTableView();
            } catch (Exception e) {
                writeToLogFile("Error refreshing the table", e.toString());
            }
        });
        directoryManagerContextMenu = new ContextMenu(refreshMenuItem, activateMenuItem, deactivateMenuItem, openDirectoryMenuItem, 
            openNotesMenuItem, openMsgLogMenuItem, openExeMenuItem, deactivateAllMenuItem);
        directoryTableView.setContextMenu(directoryManagerContextMenu);
        //listening to changes in tableView selections
        directoryTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Directory>() {
            @Override
            public void changed(ObservableValue<? extends Directory> observable, 
                Directory oldValue, Directory newValue) {
                    selectedDirectory = newValue;
                    if (newValue != null) {
                        openNotesMenuItem.setDisable(false);
                        openMsgLogMenuItem.setDisable(false);
                        openDirectoryMenuItem.setDisable(false);
                        openExeMenuItem.setDisable(false);
                        if (newValue.getActive().equals("Yes")) {
                            deactivateMenuItem.setDisable(false);
                            activateMenuItem.setDisable(true);
                        } else {
                            deactivateMenuItem.setDisable(true);
                            activateMenuItem.setDisable(false);
                        }
                    } else {
                        openExeMenuItem.setDisable(true);
                        openNotesMenuItem.setDisable(true);
                        openDirectoryMenuItem.setDisable(true);
                        deactivateMenuItem.setDisable(true);
                        activateMenuItem.setDisable(true);
                        openMsgLogMenuItem.setDisable(true);
                    }
                }
        });
        /*==========================================
                    Directory Manager end
        ==========================================*/

        
        //initial setup
        refreshTableView();
        directoryManagerAnchor.getChildren().addAll(directoryFilterVBox, directoryTableView);

        //set basic stage settings
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setTitle("Centroid Tech Support Tools");
        stage.getIcons().add(icon);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.setResizable(true);

        //set the scene and show the stage
        stage.setScene(scene);
        stage.show();

        //set width of various components based on the scene
        helpTabMainVBox.setMaxWidth(scene.getWidth() - 2);
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                helpTabMainVBox.setMaxWidth((Double) newValue - 2);
            }
            
        });

    }

    //method to activate a directory
    public static void activateDirectory(Directory directory) {
        try {
            String machineType = directory.getMachineType();
            String oldPath = directory.getPath();
            String newPath = directory.getBasePath();
            for (Directory dir : directoryArray) {
                if ((dir.getPath().equals(newPath) || dir.getMachineType().equals(machineType)) && 
                dir.getActive().equals("Yes")) {
                    deactivateDirectory(dir);
                }
            }
            Path sourcePath = Paths.get("C:/" + oldPath);
            Path destinationPath = Paths.get("C:/" + newPath);
            Files.move(sourcePath, destinationPath);
            writeToLogFile("Directory activated", sourcePath + " -> " + destinationPath);
        } catch (Exception e) {
            writeToLogFile("Error activating directory: " + directory.getPath(), e.toString());
        }
    }

    //method for deactivating a directory
    public static void deactivateDirectory(Directory directory) {
        try {
            String machineType = directory.getMachineType().toLowerCase();
            String machine = "";
            String newPath = directory.getBasePath();
            String oldPath = directory.getPath();
            String rawVersion = directory.getVersion();
            if (newPath.equals("cncm") && !machineType.equals("mill")) {
                machine = machineType + "_";
            }
            Path sourcePath = Paths.get("C:/" + oldPath);
            Path destinationPath = Paths.get("C:/" + newPath + "_" + rawVersion + "_" + 
            directory.getBoard().toLowerCase() + "_" + machine + getDate() + "_" + getTime());
            Files.move(sourcePath, destinationPath);
            writeToLogFile("Directory deactivated", sourcePath + " -> " + destinationPath);
        } catch (Exception e) {
            writeToLogFile("Error deactivating directory: " + directory.getPath(), e.toString());
        }
    }

    //method to refresh the tableview
    public static void refreshTableView() {
        directoryArray.clear();
        getDirectoryInfo();
        updateTableView();
    }

    //method to update the tableview with directory info
    public static void updateTableView() {
        directoryTableView.getItems().clear();
        for (Directory directory : directoryArray) {
            if (directory.getBoard().equals("Acorn") && toggleButtonArray.get(0).isSelected()) {
                continue;
            } else if (directory.getBoard().equals("Acornsix") && toggleButtonArray.get(1).isSelected()) {
                continue;
            } else if (directory.getBoard().equals("Hickory") && toggleButtonArray.get(2).isSelected()) {
                continue;
            } else if (directory.getBoard().equals("Oak") && toggleButtonArray.get(3).isSelected()) {
                continue;
            } else if (directory.getBoard().equals("Allin1DC") && toggleButtonArray.get(4).isSelected()) {
                continue;
            } else if (directory.getMachineType().equals("Laser") && toggleButtonArray.get(5).isSelected()) {
                continue;
            } else if (directory.getMachineType().equals("Lathe") && toggleButtonArray.get(6).isSelected()) {
                continue;
            } else if (directory.getMachineType().equals("Mill") && toggleButtonArray.get(7).isSelected()) {
                continue;
            } else if (directory.getMachineType().equals("Plasma") && toggleButtonArray.get(8).isSelected()) {
                continue;
            } else if (directory.getMachineType().equals("Router") && toggleButtonArray.get(9).isSelected()) {
                continue;
            } else {
                directoryTableView.getItems().add(directory);
            }
        }
        directoryTableView.getSortOrder().addAll(activeColumn, machineTypeColumn, versionColumn);
    }

    //method for getting all directory info from C:
    public static void getDirectoryInfo() {
        try {
            File[] files = new File("C:/").listFiles();
            deactivateAllMenuItem.setDisable(true);
            for (File file : files) {
                try {
                    String fileName = file.getName();
                    if ((fileName.contains("cncm") || fileName.contains("cnct") || fileName.contains("cncr") || 
                    fileName.contains("cncp") || fileName.contains("cncl")) && !fileName.contains("ERROR")) {
                        String active = "";
                        if (fileName.equals("cncm") || fileName.equals("cnct") || fileName.equals("cncr") || 
                        fileName.equals("cncp") || fileName.equals("cncl")) {
                            active = "Yes";
                            deactivateAllMenuItem.setDisable(false);
                        }
                        String machineType = getMachineType(fileName);
                        String version = getVersion(fileName);
                        String board = getBoardType(fileName);
                        String rawDate = new Date(new File(file, "system").lastModified()).toString();
                        String[] rawDateSplit = rawDate.split(" ");
                        String newDateString = rawDateSplit[0] + " " + rawDateSplit[1] + " " + rawDateSplit[2] + " " + rawDateSplit[5];
                        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy", Locale.ENGLISH);
                        LocalDate localDate = LocalDate.parse(newDateString, inputFormatter);
                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String finalDateString = localDate.format(outputFormatter);
                        String time = rawDateSplit[3];
                        String notes = getMachineNotes(fileName);
                        String basePath = "";
                        String[] rawVersionSplit = version.split("\\.");
                        String rawVersionSplitCombined = rawVersionSplit[0] + rawVersionSplit[1];
                        int versionCombined = Integer.parseInt(rawVersionSplitCombined);
                        if (machineType.equals("lathe")) {
                            basePath = "cnct";
                        } else if (versionCombined < 539) {
                            basePath = "cncm";
                        } else if (machineType.equals("mill")) {
                            basePath = "cncm";
                        } else if (machineType.equals("router")) {
                            basePath = "cncr";
                        } else if (machineType.equals("plasma")) {
                            basePath = "cncp";
                        } else if (machineType.equals("laser")) {
                            basePath = "cncl";
                        }
                        directoryArray.add(new Directory(active, capitalizeString(machineType), version, capitalizeString(board), fileName, finalDateString, time, notes, basePath));
                    } 
                } catch (Exception e) {
                    if (!file.getName().contains("_ERROR_")) {
                        String startPath = "";
                        if (file.getName().contains("cncm")) {
                            startPath = "cncm";
                        } else if (file.getName().contains("cnct")) {
                            startPath = "cnct";
                        } else if (file.getName().contains("cncr")) {
                            startPath = "cncr";
                        } else if (file.getName().contains("cncp")) {
                            startPath = "cncp";
                        } else if (file.getName().contains("cncl")) {
                            startPath = "cncl";
                        }
                        Path sourcePath = Paths.get("C:/" + file.getName());
                        Path destinationPath = Paths.get("C:/" + startPath + "_ERROR_" + getDate() + "_" + getTime());
                        Files.move(sourcePath, destinationPath);
                        writeToLogFile("Error getting directory info; renaming directory to: " + destinationPath, e.toString());
                    }
                    continue;
                }
            }
        } catch (Exception e) {
            writeToLogFile("Error getting directory info", e.toString());
        }
    }

    //method to get machine notes
    public static String getMachineNotes(String filePath) {
        try {
            File machineNotes = new File("C:/" + filePath, "machine_notes.txt");
            if (machineNotes.exists()) {
                List<String> lines = Files.readAllLines(machineNotes.toPath());
                if (lines.size() > 0) {
                    return lines.get(0);
                }
            }
        } catch (Exception e) {
            writeToLogFile("Error getting machine notes", e.toString());
        }
        return "";
    }

    //method to write a line to the log file
    public static void writeToLogFile(String line) {
        try {
            String lineCombo = getDate() + " " + getTime() + ": " + line;
            Files.writeString(logFile.toPath(), lineCombo + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            writeToLogFile("Error writing to log file", e.toString());
        }
    }

    //method to write 2 lines to the log file
    public static void writeToLogFile(String line, String line2) {
        try {
            String lineCombo = getDate() + " " + getTime() + ": " + line + " | " + line2;
            // String lineCombo2 = getDate() + " " + getTime() + ": " + line2;
            Files.writeString(logFile.toPath(), lineCombo + System.lineSeparator(), StandardOpenOption.APPEND);
            // Files.writeString(logFile.toPath(), lineCombo2 + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            writeToLogFile("Error writing to log file", e.toString());
        }
    }

    //method to return the current date in day-month-year format
    public static String getDate() {
        String dateNow = String.valueOf(LocalDate.now());
        String[] dateSplit = dateNow.split("-");
        return dateSplit[1] + "-" + dateSplit[2] + "-" + dateSplit[0].split("0")[1];
    }

    //method to return the current time in hour.minute format
    public static String getTime() {
        String timeNow = String.valueOf(LocalTime.now());
        String[] timeSplit = timeNow.split(":");
        return timeSplit[0] + "." + timeSplit[1] + "." + Math.round(Double.parseDouble(timeSplit[2]));
    }

    //method to figure out the type of machine based on which .exe is in the directory
    public static String getMachineType(String directory) {
        try {
            for (int i = 0; i < directories.length; i++) {
                File directoryExe = new File("C:/" + directory + "/" + directories[i] +".exe");
                if (directoryExe.exists()) {
                    return machines[i];
                }
            }  
        } catch (Exception e) {
            writeToLogFile("Error getting machine type", e.toString());
        }
        return null;
    }

    //method to capitalize a word
    public static String capitalizeString(String str) {
        try {
            char firstChar = Character.toUpperCase(str.charAt(0));
            String restOfString = str.substring(1);
            return firstChar + restOfString;
        } catch (Exception e) {
            writeToLogFile("Error capitalizing string", e.toString());
        }
        return null;
    }

    //method to get the directory used for files such as cncm.prm.xml, cnctcfg.xml, etc.
    public static String getFileDirectory(String directory) {
        if (directory.contains("cnct")) {
            return "cnct";
        } else {
            return "cncm";
        }
    }

    //method to get the board type from cnc*/mpu_info.xml
    public static String getBoardType(String directory) {
        try {
            File filePath = new File("C:/" + directory + "/mpu_info.txt");
            Scanner scanner = new Scanner(filePath);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.toLowerCase().contains("plc device")) {
                    String board = line.split(" ")[2];
                    scanner.close();
                    if (board.toLowerCase().contains("allinone")) {
                        return "allin1DC";
                    }
                    return board.toLowerCase();
                }
            }
            scanner.close();
        } catch (Exception e) {
            writeToLogFile("Error getting board type", e.toString());
        }
        return null;
    }

    //method to get a document based on provided path
    public static Document getDocument(String filePath) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document document = null;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(filePath));
        } catch (Exception e) {
            writeToLogFile("Error getting document from: " + filePath, e.toString());
        }
        return document;
    }

    //method to get CNC12 version from cnc*/cnc*.prm.xml
    public static String getVersion(String directory) {
        NodeList softwareVersionNodeList;
        String softwareVersion;
        String[] softwareVersionSplit = null;
        try {
            softwareVersionNodeList = getDocument("C:/" + directory + "/" + getFileDirectory(directory) + 
            ".prm.xml").getDocumentElement().getElementsByTagName("SoftwareVersion");
            softwareVersion = softwareVersionNodeList.item(0).getTextContent();
            softwareVersionSplit = softwareVersion.split(" ");
        if (softwareVersionSplit[0].equals("ACORN")) {
            return softwareVersionSplit[3];
        } else {
            return softwareVersionSplit[2];
        }
        } catch (Exception e) {
            writeToLogFile("Error setting raw version", e.toString());
        }
        return null;
    }
}
