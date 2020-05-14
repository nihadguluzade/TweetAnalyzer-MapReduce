package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        setStage(primaryStage);
        primaryStage.setTitle("Tweet Analyzer");
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/app/resources/hadoop-logo.png")));
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        Main.stage = stage;
    }
}
