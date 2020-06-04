package glebi.javafx.filemanager;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController{
    @FXML
    VBox leftPanel, rightPanel;

    public void btnCopyAction(ActionEvent actionEvent) {
        // с помощью свойства в fxml файле панели получаем доступ к PanelController обоих таблиц
        PanelController leftPanelController = (PanelController)leftPanel.getProperties().get("controller");
        PanelController rightPanelController = (PanelController)rightPanel.getProperties().get("controller");

        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // в зависимости от того в какой панели был выбран файл, определятся откуда в какую панель будет копироваться файл
        PanelController sourcePanelController = null, destinationPanelController = null;
        if (leftPanelController.getSelectedFileName() != null) {
            sourcePanelController = leftPanelController;
            destinationPanelController = rightPanelController;
        }

        if (rightPanelController.getSelectedFileName() != null) {
            sourcePanelController = rightPanelController;
            destinationPanelController = leftPanelController;
        }

        // создаём пути откуда куда копировать. путь источника формируется из текущего пути + имени выбранного файла
        // путь назначения формируется из текущего пути панели + имя файла который копируют.
        Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());
        Path destinationPath = Paths.get(destinationPanelController.getCurrentPath()).resolve(sourcePath.getFileName().toString());

        try {
            Files.copy(sourcePath, destinationPath);
            destinationPanelController.updateList(Paths.get(destinationPanelController.getCurrentPath()));  // обновление панели куда скопировали
        } catch (IOException e) {  // если такой файл уже существует
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось скопировать указанный файл.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnMoveAction(ActionEvent actionEvent) {
        // с помощью свойства в fxml файле панели получаем доступ к PanelController обоих таблиц
        PanelController leftPanelController = (PanelController)leftPanel.getProperties().get("controller");
        PanelController rightPanelController = (PanelController)rightPanel.getProperties().get("controller");

        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // в зависимости от того в какой панели был выбран файл, определятся откуда в какую панель будет перемещаться файл
        PanelController sourcePanelController = null, destinationPanelController = null;
        if (leftPanelController.getSelectedFileName() != null) {
            sourcePanelController = leftPanelController;
            destinationPanelController = rightPanelController;
        }

        if (rightPanelController.getSelectedFileName() != null) {
            sourcePanelController = rightPanelController;
            destinationPanelController = leftPanelController;
        }

        // создаём пути откуда куда перемещать. путь источника формируется из текущего пути + имени выбранного файла
        // путь назначения формируется из текущего пути панели + имя файла который перемещают.
        Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());
        Path destinationPath = Paths.get(destinationPanelController.getCurrentPath()).resolve(sourcePath.getFileName().toString());

        try {
            Files.move(sourcePath, destinationPath);
            destinationPanelController.updateList(Paths.get(destinationPanelController.getCurrentPath()));  // обновление панели куда переместили
            sourcePanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));  // обновление панели откуда переместили
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось переместить указанный файл.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnDeleteAction(ActionEvent actionEvent) {
        // с помощью свойства в fxml файле панели получаем доступ к PanelController обоих таблиц
        PanelController leftPanelController = (PanelController)leftPanel.getProperties().get("controller");
        PanelController rightPanelController = (PanelController)rightPanel.getProperties().get("controller");

        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // в зависимости от того в какой панели был выбран файл, определятся с какой панелью взаимодействуем
        PanelController panelController = null;
        if (leftPanelController.getSelectedFileName() != null) {
            panelController = leftPanelController;
        }

        if (rightPanelController.getSelectedFileName() != null) {
            panelController = rightPanelController;
        }

        // создаём путь удаляемого файла. путь формируется из текущего пути + имени выбранного файла
        Path path = Paths.get(panelController.getCurrentPath(), panelController.getSelectedFileName());

        try {
            Files.delete(path);
            panelController.updateList(Paths.get(panelController.getCurrentPath()));  // обновление панели после удаления
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось удалить указанный файл.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void itemNewProcessAction(ActionEvent actionEvent) throws Exception {
        /*
        Parent root = FXMLLoader.load(getClass().getResource("pop_up_window.fxml"));
        Stage newWindow = new Stage();
        newWindow.setTitle("New Window");
        newWindow.setScene(new Scene(root, 600, 400));
        newWindow.show();

         */
        Process process = Runtime.getRuntime()
                .exec("javac --module-path /home/glebi/Development/javafx-sdk-14.0.1/lib --add-modules=javafx.controls,javafx.fxml -cp src src//glebi//javafx//popupwindow//PopUpWindow.java");
        process = Runtime.getRuntime()
                .exec("java --module-path /home/glebi/Development/javafx-sdk-14.0.1/lib --add-modules=javafx.controls,javafx.fxml -cp src glebi.javafx.popupwindow.PopUpWindow");
    }

    public void itemOpenCalculatorAction(ActionEvent actionEvent) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("gnome-calculator");
        Process process = builder.start();
    }

    public void itemOpenCalendarAction(ActionEvent actionEvent) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("gnome-calendar");
        Process process = builder.start();
    }

    public void itemOpenSystemMonitorAction(ActionEvent actionEvent) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("gnome-system-monitor");
        Process process = builder.start();
    }
}
