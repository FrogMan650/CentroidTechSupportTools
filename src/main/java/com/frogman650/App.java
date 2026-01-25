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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {
    public static String[] directories = {"cncm", "cnct", "cncr", "cncp", "cncl"};
    public static String[] machines = {"mill", "lathe", "router", "plasma", "laser"};
    public static File executableDirectory;
    public static File logFile;
    public static ArrayList<Directory> directoryArray = new ArrayList<>();
    public static TableView<Directory> directoryTableView;
    public static TableColumn<Directory, String> activeColumn;
    public static TableColumn<Directory, String> machineTypeColumn;
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
        Tab installTweakerTab = new Tab("Install tweaker", installTweakerAnchor);
        Tab directoryManagerTab = new Tab("Directory manager", directoryManagerAnchor);
        installTweakerTab.setClosable(false);
        directoryManagerTab.setClosable(false);
        TabPane root = new TabPane(directoryManagerTab, installTweakerTab);
        Scene scene = new Scene(root, Color.BLACK);
        Image icon = new Image(App.class.getResourceAsStream("LK_logo_square.png"));

        /*=============================================
                    Directory Manager start
        ==============================================*/
        //columns
        activeColumn = new TableColumn<>("Active");
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setPrefWidth(100);
        activeColumn.setSortType(TableColumn.SortType.DESCENDING);
        machineTypeColumn = new TableColumn<>("Machine");
        machineTypeColumn.setCellValueFactory(new PropertyValueFactory<>("machineType"));
        machineTypeColumn.setPrefWidth(120);
        TableColumn<Directory, String> versionColumn = new TableColumn<>("Version");
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        versionColumn.setPrefWidth(80);
        TableColumn<Directory, String> boardColumn = new TableColumn<>("Board");
        boardColumn.setCellValueFactory(new PropertyValueFactory<>("board"));
        TableColumn<Directory, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        TableColumn<Directory, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Directory, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        directoryTableView = new TableView<>();
        directoryTableView.setId("tableView");
        directoryTableView.getColumns().addAll(activeColumn, machineTypeColumn, versionColumn, boardColumn, dateColumn, timeColumn, notesColumn);
        //anchors
        AnchorPane.setTopAnchor(directoryTableView, 0.0);
        AnchorPane.setBottomAnchor(directoryTableView, 0.0);
        AnchorPane.setRightAnchor(directoryTableView, 0.0);
        AnchorPane.setLeftAnchor(directoryTableView, 0.0);
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
                writeToLogFile("Openning directory...");
            } catch (Exception e) {
                writeToLogFile("Error openning directory: " + "C:/" + selectedDirectory.getPath(), e.toString());
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
                writeToLogFile("Openning machine notes...");
            } catch (Exception e) {
                writeToLogFile("Error openning notes: " + selectedDirectory.getPath(), e.toString());
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
                    writeToLogFile("Launching " + selectedDirectory.getBasePath() + ".exe...");
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
            openNotesMenuItem, openExeMenuItem, deactivateAllMenuItem);
        directoryTableView.setContextMenu(directoryManagerContextMenu);
        //listening to changes in tableView selections
        directoryTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Directory>() {
            @Override
            public void changed(ObservableValue<? extends Directory> observable, 
                Directory oldValue, Directory newValue) {
                    selectedDirectory = newValue;
                    if (newValue != null) {
                        openNotesMenuItem.setDisable(false);
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
                    }
                }
            
        });
        /*==========================================
                    Directory Manager end
        ==========================================*/

        
        //initial setup
        refreshTableView();
        directoryManagerAnchor.getChildren().addAll(directoryTableView);

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
            writeToLogFile("Directory activated successfully", oldPath + " -> " + newPath);
        } catch (Exception e) {
            writeToLogFile("Error activating directory: " + directory.getPath(), e.toString());
        }
    }

    //method for deactivating a directory
    public static void deactivateDirectory(Directory directory) {
        try {
            String machineType = directory.getMachineType();
            String machine = "";
            String newPath = directory.getBasePath();
            String oldPath = directory.getPath();
            String rawVersion = directory.getVersion();
            if (newPath.equals("cncm") && !machineType.equals("mill")) {
                machine = machineType + "_";
            }
            Path sourcePath = Paths.get("C:/" + oldPath);
            Path destinationPath = Paths.get("C:/" + newPath + "_" + rawVersion + "_" + 
            directory.getBoard() + "_" + machine + getDate() + "_" + getTime());
            Files.move(sourcePath, destinationPath);
            writeToLogFile("Directory deactivated successfully", oldPath + " -> " + destinationPath);
        } catch (Exception e) {
            writeToLogFile("Error deactivating directory: " + directory.getPath(), e.toString());
        }
    }

    //method to refresh the tableview
    public static void refreshTableView() {
        directoryArray.clear();
        directoryTableView.getItems().clear();
        getDirectoryInfo();
        updateTableView();
        directoryTableView.getSortOrder().addAll(activeColumn, machineTypeColumn);
    }

    //method to update the tableview with directory info
    public static void updateTableView() {
        for (Directory directory : directoryArray) {
            directoryTableView.getItems().add(directory);
        }
    }

    //method for getting all directory info from C:
    public static void getDirectoryInfo() {
        try {
            File[] files = new File("C:/").listFiles();
            deactivateAllMenuItem.setDisable(true);
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.contains("cncm") || fileName.contains("cnct") || fileName.contains("cncr") || 
                fileName.contains("cncp") || fileName.contains("cncl")) {
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
                    directoryArray.add(new Directory(active, machineType, version, board, fileName, finalDateString, time, notes, basePath));
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
            String lineCombo = getDate() + " " + getTime() + ": " + line;
            String lineCombo2 = getDate() + " " + getTime() + ": " + line2;
            Files.writeString(logFile.toPath(), lineCombo + System.lineSeparator(), StandardOpenOption.APPEND);
            Files.writeString(logFile.toPath(), lineCombo2 + System.lineSeparator(), StandardOpenOption.APPEND);
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
        NodeList boardVersionNodeList;
        String boardVersion = null;
        String oldFilePath = "C:/" + directory + "/mpu_info.xml";
        try {
            boardVersionNodeList = getDocument(oldFilePath).getDocumentElement().getElementsByTagName("PLCDeviceID");
            boardVersion = boardVersionNodeList.item(0).getTextContent().split("_")[2];
        } catch (Exception e) {
            writeToLogFile("Error setting board type", e.toString());
        }
        return boardVersion;
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
        } catch (Exception e) {
            writeToLogFile("Error setting raw version", e.toString());
        }
        if (softwareVersionSplit[0].equals("ACORN")) {
            return softwareVersionSplit[3];
        } else {
            return softwareVersionSplit[2];
        }
    }
}
