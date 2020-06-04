package glebi.javafx.popupwindow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.swing.text.LabelView;
import java.util.concurrent.TimeUnit;

// по задумке этот класс будет компилироваться и запускаться в виде дочернего процесса (всплывающее окно)
public class PopUpWindow extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("pop_up_window.fxml"));
        StackPane layout = new StackPane();
        layout.getChildren().add(new Label("TEST"));
        Stage newWindow = new Stage();
        newWindow.setTitle("New Window");
        newWindow.setScene(new Scene(layout, 600, 400));
        newWindow.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
