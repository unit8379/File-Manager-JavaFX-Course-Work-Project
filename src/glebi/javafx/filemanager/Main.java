package glebi.javafx.filemanager;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Java File Manager");
        primaryStage.setScene(new Scene(root, 1280, 600));
        primaryStage.show();
        // фокус на первый элемент из списка элементов main.fxml, чтобы убрать фокус с pathfield'а
        root.getChildrenUnmodifiable().get(0).requestFocus();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
