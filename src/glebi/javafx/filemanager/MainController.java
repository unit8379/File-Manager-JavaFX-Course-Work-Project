package glebi.javafx.filemanager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class MainController{
    @FXML
    VBox leftPanel, rightPanel;

    public void btnCopyAction(ActionEvent actionEvent) {
        // с помощью свойства в fxml файле панели получаем доступ к PanelController обоих таблиц
        PanelController leftPanelController = (PanelController)leftPanel.getProperties().get("controller");
        PanelController rightPanelController = (PanelController)rightPanel.getProperties().get("controller");

        // выход из метода, если не был выбран файл
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

        // выход из метода, если не был выбран файл
        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // в зависимости от того в какой панели был выбран файл, определятся откуда в какую панель будет перемещаться файл
        PanelController sourcePanelController = null, destinationPanelController = null;
        if (leftPanelController.getSelectedFileName() != null) {
            // выход из метода, если выбранный файл из директории System
            Path systemPath = Paths.get("./../").toAbsolutePath().normalize(); // корень директории System
            if (Paths.get(leftPanelController.pathField.getText()).resolve(leftPanelController.getSelectedFileName()).startsWith(systemPath)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Переместить файл System невозможно.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            // запись в переменные панелей источника и получателя
            sourcePanelController = leftPanelController;
            destinationPanelController = rightPanelController;
        }

        if (rightPanelController.getSelectedFileName() != null) {
            // выход из метода, если выбранный файл из директории System
            Path systemPath = Paths.get("./../").toAbsolutePath().normalize(); // корень директории System
            if (Paths.get(rightPanelController.pathField.getText()).resolve(rightPanelController.getSelectedFileName()).startsWith(systemPath)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Переместить файл System невозможно.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            // запись в переменные панелей источника и получателя
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

        // выход из метода, если не был выбран файл
        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ни один файл не был выбран.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        // в зависимости от того в какой панели был выбран файл, определятся с какой панелью взаимодействуем
        PanelController panelController = null;
        if (leftPanelController.getSelectedFileName() != null) {
            // выход из метода, если выбранный файл из директории System
            Path systemPath = Paths.get("./../").toAbsolutePath().normalize(); // корень директории System
            if (Paths.get(leftPanelController.pathField.getText()).resolve(leftPanelController.getSelectedFileName()).startsWith(systemPath)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Удалить файл System невозможно.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            panelController = leftPanelController;
        }

        if (rightPanelController.getSelectedFileName() != null) {
            // выход из метода, если выбранный файл из директории System
            Path systemPath = Paths.get("./../").toAbsolutePath().normalize(); // корень директории System
            if (Paths.get(rightPanelController.pathField.getText()).resolve(rightPanelController.getSelectedFileName()).startsWith(systemPath)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Удалить файл System невозможно.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
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
        // здесь нужно сделать логику и отображение файла в память
        ProcessInfo thisProcessInfo = JProcesses.getProcess((int)ProcessHandle.current().pid()); // Инфа о процессе ФМ
        String thisPID = thisProcessInfo.getPid(); // pid ФМ в формате строки
        String thisStartTime = thisProcessInfo.getStartTime(); // start time файлового менедежера

        File file = new File("./src/glebi/javafx/popupwindow/mapped_file.txt"); // creating file to write
        file.delete(); // delete it, because we gonna make random access file on directory of this file

        try (RandomAccessFile randomAccessFile  = new RandomAccessFile(file, "rw")) {
            FileChannel fileChannel = randomAccessFile.getChannel(); // getting files channel in read/write mode
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 32768); // 32kb буфер отображённого на память файла
            buffer.put((thisPID + "\n").getBytes());
            buffer.put(thisStartTime.getBytes());
        }

        Process process = Runtime.getRuntime()
                .exec("javac --module-path /home/glebi/Development/javafx-sdk-14.0.1/lib --add-modules=javafx.controls,javafx.fxml -cp src:/home/glebi/Development/jProcesses_1.6.5/jProcesses-1.6.5.jar src//glebi//javafx//popupwindow//PopUpWindow.java");
        process.waitFor();
        process = Runtime.getRuntime()
                .exec("java --module-path /home/glebi/Development/javafx-sdk-14.0.1/lib --add-modules=javafx.controls,javafx.fxml -cp src:/home/glebi/Development/jProcesses_1.6.5/jProcesses-1.6.5.jar glebi.javafx.popupwindow.PopUpWindow");
    }

    public void itemProcessesLogging(ActionEvent actionEvent) throws Exception {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setHeaderText("Введите имя файла, в который будет\nсохранён протокол процессов.\n" +
                "Файл будет хранится в папке My Documents.");
        textInputDialog.setContentText("Имя файла:");
        Optional<String> dialogOutputString = textInputDialog.showAndWait();
        // Вычленяем из dialogOutputString подстроку с вводом пользователя, потому что изначальная форма: "Optional[output]"
        String fileName = dialogOutputString.toString().substring(9, dialogOutputString.toString().length() - 1);

        Path processesProtocol = Paths.get("./../../My Documents/" + fileName).toAbsolutePath().normalize();
        if (!processesProtocol.toFile().exists()) {
            Files.createFile(processesProtocol);
            // List получает информацию обо всех процессах. Используется сторонняя библиотека JProcesses
            List<ProcessInfo> processesList = JProcesses.getProcessList();
            ProcessInfo thisProcessInfo = JProcesses.getProcess((int)ProcessHandle.current().pid()); // Инфа о процессе ФМ
            // Открывается поток для записи в файл с помощью FileWriter
            FileWriter fileWriter = new FileWriter(processesProtocol.toFile());
            for (final ProcessInfo processInfo : processesList) {
                if (LocalTime.parse(processInfo.getStartTime()).isAfter(LocalTime.parse(thisProcessInfo.getStartTime()))) {
                    fileWriter.write("Process Name: " + processInfo.getName() + "\n");
                    fileWriter.write("Start Time: " + LocalTime.parse(processInfo.getStartTime()) + "\n");
                    fileWriter.write("------------------" + "\n");
                }
            }
            // Поток закрывается
            fileWriter.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Такой файл уже существует.", ButtonType.OK);
            alert.showAndWait();
        }
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

    public void itemAboutProgram(ActionEvent actionEvent) {
        String aboutMessage = "Операционная система: Ubuntu 18 (ядро Linux).\nЯзык программирования: Java 14.0.1 + JavaFX 14.0.1.\nФИО разработчика:" +
                " Ежов Глеб Владимирович.\nГруппа: РПИС-82.";
        Alert alert = new Alert(Alert.AlertType.NONE, aboutMessage, ButtonType.CLOSE);
        alert.showAndWait();
    }
}
