package glebi.javafx.filemanager;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class PanelController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;

    @FXML
    ComboBox<String> disksBox;

    @FXML
    TextField pathField;

    // Метод, который производит инициализацию таблицы с файлами. Готовит саму таблицу и выполняет предварительные работы.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // создание столбца таблицы с типом файла. данные из класса FileInfo преобразовывваем в String
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        // ширина столбца
        fileTypeColumn.setPrefWidth(24);

        // создание столбца таблицы с именем файла. данные из класса FileInfo преобразовывваем в String
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        // ширина столбца
        fileNameColumn.setPrefWidth(260);

        // столбец с размером файла
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(120);
        // Форматирование ячеек столбца с размером файла. Каждая ячейка будет обрабатываться методом updateItem
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // строка форматируется как число для корректной сортировки в приложении
                        String text = String.format("%,d bytes", item); // формат строки: разделение числа (пробел через три символа).
                        // директории не указывают размер, только тэг [DIR]
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        // столбец с датой последнего изменения
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"); // создаём паттерн для форматирования даты
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(240);



        // добавление столбцов в таблицу в самом GUI
        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
        // сразу отсортировать по столбцу с типами файлов
        filesTable.getSortOrder().add(fileTypeColumn);

        // заполняем КомбоБокс корневыми директориями
        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0); // по умолчанию выбираем первую запись в КомбоБоксе

        // вешаем событие на клики по элементам таблицы для перехода по директориям
        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    // для нового пути, с помощью метода resolve(), добавляем к текущему пути имя выбранного файла
                    Path path = Paths.get(pathField.getText()).resolve(getSelectedFileName());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                    else {
                        // если файл не является директорией, то открыть его с помощью программы по умолчанию
                        try {
                            System.out.println(path.toString().replace(" ", "\\ "));
                            //Process process = Runtime.getRuntime().exec("xdg-open " + path.toString().replace(" ", "\\ "));
                            //process.waitFor();
                            //Desktop.getDesktop().open(path.toFile());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        // указание пути для первого обновления списка файлов
        updateList(Paths.get("./../../"));
    }

    public void updateList(Path path) {
        try {
            /* normalize() приводит путь к обычному виду, без различных переходов типа точки.
                toAbsolutePath() показывает весь путь от самого корня "\"
             */
            pathField.setText(path.toAbsolutePath().normalize().toString()); // заполняем текстовую строку полным путём к файлу
            filesTable.getItems().clear();
            /* собираем информацию о всех файлах по указанному пути, с помощью потока данных (Stream API) все файлы пропускаем
             * через конструктор FileInfo, затем все экземпляры FileInfo собираем в список.
             */
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            // всплывающее окно с предупреждением JavaFX
            Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    /**
     * Событие на нажатие кнопки "Вверх". Переводит текущий каталог на уровень вверх.
     * @param actionEvent событие
     */
    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent(); // путь родителя текущей директории
        Path rootPath = Paths.get("./../../../").toAbsolutePath().normalize(); // корень пространства, чтобы не пускать выше
        if (upperPath.compareTo(rootPath) == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы находитесь в корневой директории.", ButtonType.OK);
            alert.showAndWait();
        } else if (upperPath != null) {
            updateList(upperPath);
        }
    }

    /**
     * Событие на выбор элемента КомбоБокса. Если элемент всего один, то он является выбранным
     * всегда, поэтому на Linux системах, где всего одна корневая директория, этот метод работать не будет.
     * @param actionEvent
     */
    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>)actionEvent.getSource();
        // передаём в качестве пути выбранный элемент КомбоБокса.
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFileName() {
        if (!filesTable.isFocused()) {  // если таблица не в фокусе (не подсвечивается синим)
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }
}
