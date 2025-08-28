package com.frogman650;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {
    public static String[] directories = {"cncm", "cnct", "cncr", "cncp", "cncl"};
    public static String[] machines = {"mill", "lathe", "router", "plasma", "laser"};
    public static String exceptionText = "";
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    //method used to change the text next to the buttons
    public static String renamedDirectory(String directory) {
        if (directoryExists(directory)) {
            String machine;
            if (directory.equals("cncm") && !getMachineType(directory).equals("mill")) {
                machine = getMachineType(directory) + "_";
            } else {
                machine = "";
            }
            if (!exceptionText.equals("")) {
                return exceptionText;
            }
        return directory + "_" + getVersion(directory) + "_" + getBoardType(directory) + "_" + machine + "_" + getDate() + "_" + getTime();
        } else {
            exceptionText = directory + " does not exist or was already renamed";
            return null;
        }
    }

    //method called to actually rename the directory requested
    public static void renameDirectory(String directory) {
        if (directoryExists(directory)) {
            String machine;
            if (directory.equals("cncm") && !getMachineType(directory).equals("mill")) {
                machine = getMachineType(directory) + "_";
            } else {
                machine = "";
            }
            Path sourcePath = Paths.get("C:/" + directory);
            Path destinationPath = Paths.get("C:/" + directory + "_" + getVersion(directory) + "_" + getBoardType(directory) + "_" + machine + getDate() + "_" + getTime());
            try {
                Files.move(sourcePath, destinationPath);
            } catch (Exception e) {
                exceptionText = "exception thrown while renaming " + directory;
            }
        }
    }

    //method to check if a directory exists
    public static Boolean directoryExists(String directory) {
        File file = new File("C:/" + directory);
        return file.exists();
    }

    //method to rename a directory only if said directory exists
    public static void renameIfExists(String directory) {
        if (directoryExists(directory)) {
            renameDirectory(directory);
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
        return timeSplit[0] + "." + timeSplit[1];
    }

    //method to figure out the type of machine based on which .exe is in the directory
    public static String getMachineType(String directory) {
        for (int i = 0; i < directories.length; i++) {
            File directoryExe = new File("C:/" + directory + "/" + directories[i] +".exe");
            if (directoryExe.exists()) {
                return machines[i];
            }
        }
        return null;
    }

    //method to get the directory used for files such as cncm.prm.xml, cnctcfg.xml, etc.
    public static String getFileDirectory(String directory) {
        if (directory.equals("cnct")) {
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
            exceptionText = "Exception thrown while setting board type";
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
            exceptionText = "Exception thrown while getting document from: " + filePath;
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
            exceptionText = "Exception thrown while setting raw version";
        }
        if (softwareVersionSplit[0].equals("ACORN")) {
            return softwareVersionSplit[3];
        } else {
            return softwareVersionSplit[2];
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        //define everything we need for the GUI
        Group root = new Group();
        Scene scene = new Scene(root, Color.BLACK);
        Image icon = new Image(App.class.getResourceAsStream("LK_logo_square.png"));
        Button cncmButton = new Button("cncm");
        Text cncmText = new Text();
        Button cnctButton = new Button("cnct");
        Text cnctText = new Text();
        Button cncrButton = new Button("cncr");
        Text cncrText = new Text();
        Button cncpButton = new Button("cncp");
        Text cncpText = new Text();
        Button cnclButton = new Button("cncl");
        Text cnclText = new Text();
        Button allButton = new Button("All");
        Text allText = new Text();
        Button[] buttons = {allButton, cncmButton, cnctButton, cncrButton, cncpButton, cnclButton};
        Text[] texts = {allText, cncmText, cnctText, cncrText, cncpText, cnclText};

        //set basic stage settings
        stage.setTitle("Directory Renamer");
        stage.getIcons().add(icon);
        stage.setWidth(750);
        stage.setHeight(225);
        stage.setResizable(false);
        stage.setX(50);
        stage.setY(50);
        stage.setFullScreen(false);

        //set button and text settings and add them to the root group
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setPrefSize(50, 25);
            buttons[i].setLayoutX(5);
            buttons[i].setLayoutY(5 + (i*30));

            texts[i].setX(60);
            texts[i].setY(25 + (i*30));
            texts[i].setFill(Color.WHITE);
            texts[i].setFont(Font.font("Courier New", FontWeight.BOLD, 20));

            root.getChildren().add(buttons[i]);
            root.getChildren().add(texts[i]);
        }

        //set actions for buttons
        for (int i = 0; i < directories.length; i++) {
            int j = i;
            buttons[i + 1].setOnAction(event -> {
                exceptionText = "";
                texts[j + 1].setText("DONE: " + renamedDirectory(directories[j]));
                if (!exceptionText.equals("")) {
                    texts[j + 1].setText(exceptionText);
                } else {
                    renameDirectory(directories[j]);
                }
        });}

        //set action for set all button
        allButton.setOnAction(event -> {
            for (int i = 0; i < directories.length; i++) {
                exceptionText = "";
                texts[i + 1].setText("DONE: " + renamedDirectory(directories[i]));
                if (!exceptionText.equals("")) {
                    texts[i + 1].setText(exceptionText);
                } else {
                    renameDirectory(directories[i]);
                }
            }
            allText.setText("DONE");
        });

        //set the scene and show the stage
        stage.setScene(scene);
        stage.show();
    }
}
