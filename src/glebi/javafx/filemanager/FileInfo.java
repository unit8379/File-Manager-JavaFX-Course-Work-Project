package glebi.javafx.filemanager;

// java NIO библиотке для продвинутых операций ввода-вывода
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo {
    // перечисление реализует типы файла. при создании объекта перечисления сохраняет его имя-идентификатор
    public enum FileType {
        FILE("F"), DIRECTORY("D");

        private String name;

        public String getName() {
            return name;
        }

        FileType(String name) {
            this.name = name;
        }
    }

    private String fileName;
    private FileType type;
    private long size;
    private LocalDateTime lastModified;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public FileInfo(Path path) {
        try {
            // возвращает директорию файла
            this.fileName = path.getFileName().toString();
            // возвращает размер файла в байтах и может выбросить IOException
            this.size = Files.size(path);
            // разделение типов "файл" и "директория". изначально для системы все файлы - это полная директория до файла
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            // получение даты последнего изменения. Полученный объект из метода getLastModifiedTime преобразуем в Инстант, а потом стандартыным
            // LocalDateTime получаем время, которое там было записано. Второй атрибут это смещение по часовому поясу.
            this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(4));
        } catch (IOException e) {
            throw new RuntimeException("Unable to create file info from path.");
        }
    }
}
