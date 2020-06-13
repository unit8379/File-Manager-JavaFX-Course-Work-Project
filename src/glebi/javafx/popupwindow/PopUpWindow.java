package glebi.javafx.popupwindow;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jutils.jprocesses.JProcesses;
import org.jutils.jprocesses.model.ProcessInfo;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// по задумке этот класс будет компилироваться и запускаться в виде дочернего процесса (всплывающее окно)
public class PopUpWindow extends Application {
    // List получает информацию обо всех процессах вначале работы окна
    List<ProcessInfo> processesListBeginning = JProcesses.getProcessList();
    private String textForLabel = "";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane pane = new Pane(); // панель компоновки элементов

        // label для надписей "PID процесса" и "Start Time процесса"
        Label labelPreInfo = new Label("PID процесса:\nStart Time процесса:");
        labelPreInfo.setStyle("-fx-font-size: 20px;");
        labelPreInfo.setTranslateX(20);
        labelPreInfo.setTranslateY(20);

        // label для полученной из отображённого файла инфы
        Label labelInfo = new Label("");
        labelInfo.setStyle("-fx-font-size: 20px;");
        labelInfo.setTranslateX(260);
        labelInfo.setTranslateY(20);
        // чтение отображённого файла с информацией и передача прочитанного в labelInfo
        File mappedFile = new File("./src/glebi/javafx/popupwindow/mapped_file.txt");
        try (RandomAccessFile file = new RandomAccessFile(mappedFile, "r")) {
            FileChannel fileChannel = file.getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            for (int i = 0; i < buffer.limit(); i++) {
                textForLabel += (char) buffer.get();
            }
            labelInfo.setText(textForLabel);
        }

        // кнопка для сохранения информации в лог-файл
        Button buttonLogFile = new Button("Сохранить информацию в лог-файл");
        buttonLogFile.setTranslateX(30);
        buttonLogFile.setTranslateY(80);
        buttonLogFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter("./../../My Documents/processInfo.log");
                    fileWriter.write("PID процесса и его время старта:\n");
                    fileWriter.write(textForLabel);
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Лог-файл processInfo.log с информацией\n" +
                        "о процессе сохранён в папке My Documents.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // label для функции сохранения списка процессов
        Label labelEndedProcesses = new Label("Сохранение в текстовый файл списка процессов,\nзавершивших своё выполнение\n" +
                "в период работы процесса этого окна.");
        labelEndedProcesses.setStyle("-fx-font-size: 20px;");
        labelEndedProcesses.setTranslateX(20);
        labelEndedProcesses.setTranslateY(120);

        // кнопка для сохранения списка завершённых в период работы данного окна процессов
        Button buttonEndedProcesses = new Button("Сохранить список процессов");
        buttonEndedProcesses.setTranslateX(30);
        buttonEndedProcesses.setTranslateY(210);
        buttonEndedProcesses.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Заполнение ArrayList информацией о процессах, которые завершили своё выполнение в период работы всплыв. окна.
                List<ProcessInfo> processesListButtonEvent = JProcesses.getProcessList(); // список процессов, в момент нажатия кнопки
                ArrayList<ProcessInfo> listEndedProcesses = new ArrayList<>(); // список завершённых процессов
                for (ProcessInfo processBegin : processesListBeginning) {
                    boolean isProcessStillActive = false; // флаг, того что какой-либо процесс после нажатия на кнопку ещё работает
                    for (ProcessInfo processNow : processesListButtonEvent) {
                        if (processBegin.getName().compareTo(processNow.getName()) == 0) {
                            isProcessStillActive = true;
                            break;
                        }
                    }
                    // если процесс в новом списке не был найден, то он попадает в коллекцию завершённых процессов
                    if (!isProcessStillActive) {
                        listEndedProcesses.add(processBegin);
                    }
                }

                TextInputDialog textInputDialog = new TextInputDialog();
                textInputDialog.setHeaderText("Введите имя файла, в который будет\nсохранён список процессов.\n" +
                        "Файл будет хранится в папке My Documents.");
                textInputDialog.setContentText("Имя файла:");
                Optional<String> dialogOutputString = textInputDialog.showAndWait();
                // Вычленяем из dialogOutputString подстроку с вводом пользователя, потому что изначальная форма: "Optional[output]"
                String fileName = dialogOutputString.toString().substring(9, dialogOutputString.toString().length() - 1);

                Path processesProtocol = Paths.get("./../../My Documents/" + fileName).toAbsolutePath().normalize();
                // Открывается поток в try с ресурсами для записи в файл с помощью FileWriter
                try (FileWriter fileWriter = new FileWriter(processesProtocol.toFile())) {
                    for (final ProcessInfo processInfo : listEndedProcesses) {
                        fileWriter.write("Process Name: " + processInfo.getName() + "\n");
                        fileWriter.write("Start Time: " + LocalTime.parse(processInfo.getStartTime()) + "\n");
                        fileWriter.write("------------------" + "\n");
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // добавляет элементы на панель
        pane.getChildren().add(labelPreInfo);
        pane.getChildren().add(labelInfo);
        pane.getChildren().add(buttonLogFile);
        pane.getChildren().add(labelEndedProcesses);
        pane.getChildren().add(buttonEndedProcesses);

        primaryStage.setTitle("New Process. Pop Up Window");
        primaryStage.setScene(new Scene(pane, 600, 400));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
