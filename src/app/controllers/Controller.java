package app.controllers;

import app.Main;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;

public class Controller {

    @FXML AnchorPane mainPane;
    @FXML Button likesBtn;
    @FXML Button retweetsBtn;
    @FXML Button repliesBtn;
    @FXML Button timeBtn;
    @FXML Button lengthBtn;
    @FXML TextField usersField;
    @FXML DatePicker datePicker;
    @FXML TextField lengthField;
    @FXML Button generateBtn;
    @FXML Button cancelBtn;
    @FXML Label progressLabel;
    @FXML Label outputLabel;
    @FXML Label infoText;
    @FXML Button uploadBtn;
    @FXML Button changeDatasetBtn;
    @FXML Button downloadBtn;

    private static String DATASET_PATH;
    private static final String OUTPUT_PATH = "/bigdata/out";
    private static final String OUTPUT_FILE = "part-r-00000";
    private static final String MOST_LIKES_JAR_PATH = "jars/mp-likes.jar";
    private static final String MOST_RETWEETS_JAR_PATH = "jars/mp-retweets.jar";
    private static final String MOST_REPLIES_JAR_PATH = "jars/mp-replies.jar";
    private static final String TIME_JAR_PATH = "jars/mp-time.jar";
    private static final String LENGTH_JAR_PATH = "jars/mp-length.jar";

    private static int argsUsers;
    private static String argsDate;
    private static int argsLength;

    private String option;

    public void initialize() {

        startPosition();
        changeDatasetBtn.setVisible(false);
        outputLabel.setVisible(false);
        downloadBtn.setVisible(false);

        uploadBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(Main.getStage());
            System.out.println(file);
            if (file != null) {
                DATASET_PATH = uploadIntoHDFS(file);
                System.out.println("Dataset path: " + DATASET_PATH);
                if (DATASET_PATH != null) {
                    infoText.setText("Select one of the option to generate data");
                    infoText.setStyle("-fx-text-fill: black");
                    uploadBtn.setVisible(false);
                    likesBtn.setVisible(true);
                    retweetsBtn.setVisible(true);
                    repliesBtn.setVisible(true);
                    timeBtn.setVisible(true);
                    lengthBtn.setVisible(true);
                    changeDatasetBtn.setVisible(true);
                } else {
                    infoText.setText("Dataset couldn't be uploaded into HDFS.");
                    infoText.setStyle("-fx-text-fill: crimson");
                }
            } else {
                infoText.setText("Dataset isn't chosen.");
                infoText.setStyle("-fx-text-fill: crimson");
            }
        });

        changeDatasetBtn.setOnAction(event -> {
            uploadBtn.setVisible(true);
            startPosition();
            changeDatasetBtn.setVisible(false);
            downloadBtn.setVisible(false);
        });

        downloadBtn.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File dir = directoryChooser.showDialog(Main.getStage());
            downloadFromHDFS(dir);
        });

        likesBtn.setOnAction(event -> {
            option = "likes"; // key value
            usersField.setDisable(false);
            //likesBtn.setStyle("-fx-border-color: crimson;"); // TODO: change style
            generateBtn.setDisable(false);
            cancelBtn.setDisable(false);
            retweetsBtn.setDisable(true);
            repliesBtn.setDisable(true);
            timeBtn.setDisable(true);
            lengthBtn.setDisable(true);
            changeDatasetBtn.setDisable(true);
            progressLabel.setText("Results will be displayed below");
        });

        retweetsBtn.setOnAction(event -> {
            option = "retweets";
            usersField.setDisable(false);
            generateBtn.setDisable(false);
            cancelBtn.setDisable(false);
            likesBtn.setDisable(true);
            repliesBtn.setDisable(true);
            timeBtn.setDisable(true);
            lengthBtn.setDisable(true);
            changeDatasetBtn.setDisable(true);
            progressLabel.setText("Results will be displayed below");
        });

        repliesBtn.setOnAction(event -> {
            option = "replies";
            usersField.setDisable(false);
            generateBtn.setDisable(false);
            cancelBtn.setDisable(false);
            likesBtn.setDisable(true);
            retweetsBtn.setDisable(true);
            timeBtn.setDisable(true);
            lengthBtn.setDisable(true);
            changeDatasetBtn.setDisable(true);
            progressLabel.setText("Results will be displayed below");
        });

        timeBtn.setOnAction(event -> {
            option = "time";
            datePicker.setDisable(false);
            generateBtn.setDisable(false);
            cancelBtn.setDisable(false);
            likesBtn.setDisable(true);
            retweetsBtn.setDisable(true);
            repliesBtn.setDisable(true);
            lengthBtn.setDisable(true);
            changeDatasetBtn.setDisable(true);
            progressLabel.setText("Results will be displayed below");
        });

        lengthBtn.setOnAction(event -> {
            option = "length";
            lengthField.setDisable(false);
            generateBtn.setDisable(false);
            cancelBtn.setDisable(false);
            likesBtn.setDisable(true);
            retweetsBtn.setDisable(true);
            repliesBtn.setDisable(true);
            timeBtn.setDisable(true);
            changeDatasetBtn.setDisable(true);
            progressLabel.setText("Results will be displayed below");
        });

        cancelBtn.setOnAction(event -> {
            cancelBtn.setDisable(true);
            likesBtn.setDisable(false);
            retweetsBtn.setDisable(false);
            repliesBtn.setDisable(false);
            timeBtn.setDisable(false);
            lengthBtn.setDisable(false);
            usersField.setText("");
            datePicker.setDisable(true);
            lengthField.setText("");
            usersField.setDisable(true);
            lengthField.setDisable(true);
            generateBtn.setDisable(true);
            changeDatasetBtn.setDisable(false);
        });

        generateBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                disableAll();
                progressLabel.setText("Generating data..."); // FIXME: GUI not updating

                // get the value from user field
                if (!usersField.getText().isEmpty()) {
                    argsUsers = Integer.parseInt(usersField.getText());
                } else {
                    argsUsers = 0;
                }

                // get the value from date picker
                if (datePicker.getValue() != null) {
                    LocalDate localDate = datePicker.getValue();
                    argsDate = localDate.toString();
                } else {
                    argsDate = "0";
                }

                // get the value from length field
                if (!lengthField.getText().isEmpty()) {
                    argsLength = Integer.parseInt(lengthField.getText());
                } else {
                    argsLength = -1;
                }

                // Variables from terminal commands
                Runtime rt = Runtime.getRuntime();
                Controller controller = new Controller();

                // detect which button is selected
                switch (option) {
                    case "likes":
                        bashcall(rt, controller, MOST_LIKES_JAR_PATH, Integer.toString(argsUsers));
                        break;
                    case "retweets":
                        bashcall(rt, controller, MOST_RETWEETS_JAR_PATH, Integer.toString(argsUsers));
                        break;
                    case "replies":
                        bashcall(rt, controller, MOST_REPLIES_JAR_PATH, Integer.toString(argsUsers));
                        break;
                    case "time":
                        bashcall(rt, controller, TIME_JAR_PATH, argsDate);
                        break;
                    case "length":
                        bashcall(rt, controller, LENGTH_JAR_PATH, Integer.toString(argsLength));
                        break;
                }
            }

            /**
             * Executes hadoop jobs.
             * @param rt Runtime variable used for executing from bash (shell)
             * @param controller This class
             * @param f JAR file for hadoop, defined in constants at top
             * @param args Additional argument. Example: top 10 users, specific date...
             */
            private void bashcall(Runtime rt, Controller controller, String f, String args) {
                CustomThread errorReported;
                CustomThread outputMessage;
                try {
                    Process proc = rt.exec("hadoop jar " + f + " " + DATASET_PATH + " " + OUTPUT_PATH + " " + args);
                    errorReported = controller.getStreamWrapper(proc.getErrorStream(), "ERROR");
                    outputMessage = controller.getStreamWrapper(proc.getInputStream(), "OUTPUT");
                    errorReported.start();
                    errorReported.join();
                    outputMessage.start();
                    outputMessage.join();

                    if (!outputMessage.isAlive()) {
                        progressLabel.setText("Generating finished.");
                        enableAll();
                        printResults(outputLabel);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /** Enables all labels and buttons after thread dies */
            private void enableAll() {
                likesBtn.setDisable(false);
                retweetsBtn.setDisable(false);
                repliesBtn.setDisable(false);
                timeBtn.setDisable(false);
                lengthBtn.setDisable(false);
                usersField.clear();
                lengthField.clear();
                changeDatasetBtn.setDisable(false);
                downloadBtn.setVisible(true);
            }

            /** Disables all labels and buttons while thread is running */
            private void disableAll() {
                likesBtn.setDisable(true);
                retweetsBtn.setDisable(true);
                repliesBtn.setDisable(true);
                timeBtn.setDisable(true);
                lengthBtn.setDisable(true);
                usersField.setDisable(true);
                lengthField.setDisable(true);
                datePicker.setDisable(true);
                cancelBtn.setDisable(true);
                generateBtn.setDisable(true);
                changeDatasetBtn.setDisable(true);
                downloadBtn.setVisible(false);
            }
        });

        // force all values entered to be numerical
        usersField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                usersField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        lengthField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                lengthField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

    }

    // hide buttons on start
    private void startPosition() {
        likesBtn.setVisible(false);
        retweetsBtn.setVisible(false);
        repliesBtn.setVisible(false);
        timeBtn.setVisible(false);
        lengthBtn.setVisible(false);
    }

    /**
     * Uploads file into HDFS file system through bash command.
     * @param file File to be uploaded
     * @return Path for dataset
     */
    private String uploadIntoHDFS(File file) {
        removeOlderFile(file);
        String command = "hdfs dfs -put " + file.toString() + " /";
        System.out.println(command);
        try {
            // Execute terminal command
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            proc.waitFor();
            System.out.println("Uploaded.");
            return "/" + file.getName();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes older dataset file from HDFS before uploading a new one. Not important to check if exists or not.
     * @param file File to be deleted
     */
    private void removeOlderFile(File file) {
        String command = "hdfs dfs -rm /" + file.getName();
        System.out.println(command);
        try {
            // Execute terminal command
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            proc.waitFor();
            System.out.println("Deleted.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads file from HDFS file system through bash command.
     * @param localDir Directory where file will be downloaded
     */
    private void downloadFromHDFS(File localDir) {
        String command = "hdfs dfs -get " + OUTPUT_PATH + "/" + OUTPUT_FILE + " " + localDir;
        System.out.println(command);
        try {
            // Execute terminal command
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            proc.waitFor();
            System.out.println("Downloaded.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Utility function for running hadoop jobs */
    public CustomThread getStreamWrapper(InputStream is, String type) {
        return new CustomThread(is, type);
    }

    /** Utility class for running hadoop jobs */
    private static class CustomThread extends Thread {
        private InputStream is = null;

        public CustomThread(InputStream is, String type) {
            this.is = is;
        }

        @Override
        public void run() {
            String s = null;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((s = br.readLine()) != null) {
                    System.out.println(s);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Prints results from output file located in HDFS on the GUI.
     * @param outputLabel Name of the node on GUI where results will be printed
     */
    private void printResults(Label outputLabel) {
        String command = "hdfs dfs -cat " + OUTPUT_PATH + "/" + OUTPUT_FILE;
        try {
            // Execute terminal command
            Process proc = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "", output = "";
            while ((line = reader.readLine()) != null) {
                output += (line + "\n");
            }
            proc.waitFor();
            System.out.println(output);
            outputLabel.setVisible(true);
            outputLabel.setText(output);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
