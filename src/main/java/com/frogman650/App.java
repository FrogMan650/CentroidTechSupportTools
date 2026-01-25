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
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {
    public static String[] directories = {"cncm", "cnct", "cncr", "cncp", "cncl"};
    public static String[] machines = {"mill", "lathe", "router", "plasma", "laser"};
    public static File executableDirectory;
    public static File logFile;
    public static ArrayList<Directory> directoryArray = new ArrayList<>();
    public static TableView directoryTableView = new TableView<Directory>();
    public static Button deactivateButton;
    public static Button activateButton;
    public static Button deactivateAllButton;
    public static TableColumn activeColumn;
    public static TableColumn machineTypeColumn;
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws Exception {
        URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        executableDirectory = Paths.get(uri).getParent().toFile();
        logFile = new File(executableDirectory, "logs.txt");
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

        //Directory Manager
        Button refreshButton = new Button("Refresh");
        refreshButton.setId("tableViewButton");
        refreshButton.setOnAction(event -> {
            refreshTableView();
        });
        activateButton = new Button("Activate");
        activateButton.setId("tableViewButton");
        activateButton.setDisable(true);
        activateButton.setOnAction(event -> {
            Directory selectedDirectory = (Directory) directoryTableView.getSelectionModel().getSelectedItem();
            activateDirectory(selectedDirectory);
            refreshTableView();
        });
        deactivateButton = new Button("Deactivate");
        deactivateButton.setId("tableViewButton");
        deactivateButton.setDisable(true);
        deactivateButton.setOnAction(event -> {
            Directory selectedDirectory = (Directory) directoryTableView.getSelectionModel().getSelectedItem();
            deactivateDirectory(selectedDirectory);
            refreshTableView();
        });
        deactivateAllButton = new Button("Deactivate all");
        deactivateAllButton.setId("tableViewButton");
        deactivateAllButton.setDisable(true);
        deactivateAllButton.setOnAction(event -> {
            for (Directory dir : directoryArray) {
                if (dir.getActive().equals("Yes")) {
                    deactivateDirectory(dir);
                }
            }
            refreshTableView();
        });
        HBox buttonHBox = new HBox(refreshButton, activateButton, deactivateButton, deactivateAllButton);
        activeColumn = new TableColumn<Directory, String>("Active");
        activeColumn.setCellValueFactory(new PropertyValueFactory<Directory, String>("active"));
        activeColumn.setPrefWidth(100);
        activeColumn.setSortType(TableColumn.SortType.DESCENDING);
        machineTypeColumn = new TableColumn<Directory, String>("Machine");
        machineTypeColumn.setCellValueFactory(new PropertyValueFactory<Directory, String>("machineType"));
        machineTypeColumn.setPrefWidth(120);
        TableColumn versionColumn = new TableColumn<Directory, String>("Version");
        versionColumn.setCellValueFactory(new PropertyValueFactory<Directory, String>("version"));
        versionColumn.setPrefWidth(80);
        TableColumn boardColumn = new TableColumn<Directory, String>("Board");
        boardColumn.setCellValueFactory(new PropertyValueFactory<Directory, String>("board"));
        TableColumn pathColumn = new TableColumn<Directory, String>("Path");
        pathColumn.setCellValueFactory(new PropertyValueFactory<Directory, String>("path"));
        TableColumn dateColumn = new TableColumn<Directory, String>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<Directory, String>("date"));
        TableColumn timeColumn = new TableColumn<Directory, String>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<Directory, String>("time"));
        directoryTableView.setId("tableView");

        directoryTableView.getColumns().addAll(activeColumn, machineTypeColumn, versionColumn, boardColumn, dateColumn, timeColumn, pathColumn);
        directoryTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Directory>() {
            @Override
            public void changed(ObservableValue<? extends Directory> observable, 
                Directory oldValue, Directory newValue) {
                    if (newValue != null) {
                        if (newValue.getActive().equals("Yes")) {
                            deactivateButton.setDisable(false);
                            activateButton.setDisable(true);
                        } else {
                            deactivateButton.setDisable(true);
                            activateButton.setDisable(false);
                        }
                    }
                }
            
        });
        refreshTableView();
        directoryManagerAnchor.getChildren().addAll(buttonHBox, directoryTableView);
        AnchorPane.setTopAnchor(directoryTableView, 50.0);
        AnchorPane.setBottomAnchor(directoryTableView, 0.0);
        AnchorPane.setRightAnchor(directoryTableView, 0.0);
        AnchorPane.setLeftAnchor(directoryTableView, 0.0);

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

    //method to disable the activate, deactivate, and deactivate all buttons
    public static void disableButtons() {
        activateButton.setDisable(true);
        deactivateButton.setDisable(true);
        for (Directory dir : directoryArray) {
            if (dir.getActive().equals("Yes")) {
                deactivateAllButton.setDisable(false);
                break;
            } else {
                deactivateAllButton.setDisable(true);
            }
        }
    }

    //method to activate a directory
    public static void activateDirectory(Directory directory) {
        try {
            String machineType = directory.getMachineType();
            String oldPath = directory.getPath();
            String newPath = "";
            String rawVersion = directory.getVersion();
            String[] rawVersionSplit = rawVersion.split("\\.");
            String rawVersionSplitCombined = rawVersionSplit[0] + rawVersionSplit[1];
            int versionCombined = Integer.parseInt(rawVersionSplitCombined);
            if (machineType.equals("lathe")) {
                newPath = "cnct";
            } else if (versionCombined < 539) {
                newPath = "cncm";
            } else if (machineType.equals("mill")) {
                newPath = "cncm";
            } else if (machineType.equals("router")) {
                newPath = "cncr";
            } else if (machineType.equals("plasma")) {
                newPath = "cncp";
            } else if (machineType.equals("laser")) {
                newPath = "cncl";
            }
            for (Directory dir : directoryArray) {
                if ((dir.getPath().equals(newPath) || dir.getMachineType().equals(machineType)) && dir.getActive().equals("Yes")) {
                    deactivateDirectory(dir);
                }
            }
            Path sourcePath = Paths.get("C:/" + oldPath);
            Path destinationPath = Paths.get("C:/" + newPath);
            Files.move(sourcePath, destinationPath);
            writeToLogFile(getDate() + " " + getTime() + ": " + "Directory activated successfully");
            writeToLogFile(getDate() + " " + getTime() + ": " + oldPath + " -> " + newPath);
        } catch (Exception e) {
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error activating directory: " + directory.getPath());
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
        }
    }

    //method for deactivating a directory
    public static void deactivateDirectory(Directory directory) {
        try {
            String machineType = directory.getMachineType();
            String machine = "";
            String newPath = "";
            String oldPath = directory.getPath();
            String rawVersion = directory.getVersion();
            String[] rawVersionSplit = rawVersion.split("\\.");
            String rawVersionSplitCombined = rawVersionSplit[0] + rawVersionSplit[1];
            int versionCombined = Integer.parseInt(rawVersionSplitCombined);
            if (machineType.equals("lathe")) {
                newPath = "cnct";
            } else if (versionCombined < 539) {
                newPath = "cncm";
            } else if (machineType.equals("mill")) {
                newPath = "cncm";
            } else if (machineType.equals("router")) {
                newPath = "cncr";
            } else if (machineType.equals("plasma")) {
                newPath = "cncp";
            } else if (machineType.equals("laser")) {
                newPath = "cncl";
            }
            if (newPath.equals("cncm") && !machineType.equals("mill")) {
                machine = machineType + "_";
            }
            Path sourcePath = Paths.get("C:/" + oldPath);
            Path destinationPath = Paths.get("C:/" + newPath + "_" + rawVersion + "_" + directory.getBoard() + "_" + machine + getDate() + "_" + getTime());
            Files.move(sourcePath, destinationPath);
            writeToLogFile(getDate() + " " + getTime() + ": " + "Directory deactivated successfully");
            writeToLogFile(getDate() + " " + getTime() + ": " + oldPath + " -> " + newPath + "_" + rawVersion + "_" + directory.getBoard() + "_" + machine + getDate() + "_" + getTime());
        } catch (Exception e) {
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error deactivating directory: " + directory.getPath());
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
        }
    }

    //method to refresh the tableview
    public static void refreshTableView() {
        directoryArray.clear();
        directoryTableView.getItems().clear();
        getDirectoryInfo();
        updateTableView();
        directoryTableView.getSortOrder().addAll(activeColumn, machineTypeColumn);
        disableButtons();
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
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.contains("cncm") || fileName.contains("cnct") || fileName.contains("cncr") || 
                fileName.contains("cncp") || fileName.contains("cncl")) {
                    String active = "";
                    if (fileName.equals("cncm") || fileName.equals("cnct") || fileName.equals("cncr") || 
                    fileName.equals("cncp") || fileName.equals("cncl")) {
                        active = "Yes";
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
                    directoryArray.add(new Directory(active, machineType, version, board, fileName, finalDateString, time));
                }
            }
        } catch (Exception e) {
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error getting directory info");
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
        }
    }

    //method to write a line to the log file
    public static void writeToLogFile(String line) {
        try {
            Files.writeString(logFile.toPath(), line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error writing to log file");
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
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
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error getting machine type");
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
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
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error setting board type");
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
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
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error getting document from: " + filePath);
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
        }
        return document;
    }

    //method to get CNC12 version from cnc*/cnc*.prm.xml
    public static String getVersion(String directory) {
        NodeList softwareVersionNodeList;
        String softwareVersion;
        String[] softwareVersionSplit = null;
        try {
            softwareVersionNodeList = getDocument("C:/" + directory + "/" + getFileDirectory(directory) + ".prm.xml").getDocumentElement().getElementsByTagName("SoftwareVersion");
            softwareVersion = softwareVersionNodeList.item(0).getTextContent();
            softwareVersionSplit = softwareVersion.split(" ");
        } catch (Exception e) {
            writeToLogFile(getDate() + " " + getTime() + ": " + "Error setting raw version");
            writeToLogFile(getDate() + " " + getTime() + ": " + e.toString());
        }
        if (softwareVersionSplit[0].equals("ACORN")) {
            return softwareVersionSplit[3];
        } else {
            return softwareVersionSplit[2];
        }
    }
}
