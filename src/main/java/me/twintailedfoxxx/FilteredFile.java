package me.twintailedfoxxx;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Файл, содержащий в себе данные определенного типа.
 * @param <T> тип данных, который будет храниться в файле
 */
@SuppressWarnings("ignored")
public class FilteredFile<T> {
    /**
     * Закрепленный за фильтром файл
     */
    private final File file;

    /**
     * Модификатор, отвечающий за перезаписывание существующих данных в файле
     */
    private boolean rewrite;

    /**
     * Список последних записанных объектов в файл
     */
    private final List<T> lastWrittenItems;

    private final Logger logger = Main.getLoggerInstance();

    /**
     * Конструктор файла с данными определенного типа <code>T</code>
     * @param file файл, куда будут записываться данные
     * @param rewrite модификатор перезаписи существующих данных в файле
     */
    public FilteredFile(File file, boolean rewrite) {
        this.file = file;
        this.rewrite = rewrite;
        this.lastWrittenItems = new ArrayList<>();
    }

    /**
     * Запись данных в файл
     * @param items данные
     */
    @SafeVarargs
    public final void write(T... items) {
        if(!lastWrittenItems.isEmpty()) {
            lastWrittenItems.clear();
        }

        if(!file.exists()) {
            try {
                if(!file.getParentFile().exists()) {
                    if(!file.getParentFile().mkdirs()) {
                        logger.warn("Directory " + file.getParentFile().getAbsolutePath() + " was not created.");
                    }
                }

                if(!file.createNewFile()) {
                    logger.warn("File " + file.getAbsolutePath() + " was not created.");
                }
            } catch (IOException e) {
                logger.severe("Failed to create file " + file.getAbsolutePath() + ": " + e.getMessage());
            }
        }

        String[] existingContent = read();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            if(!rewrite && existingContent != null) {
                for(String line : existingContent) {
                    writer.write(line + "\n");
                }
            }

            for(T t : items) {
                writer.write(t + "\n");
            }

            lastWrittenItems.addAll(List.of(items));
        } catch (IOException e) {
            logger.severe("Failed to write content into file " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    /**
     * Чтение данных с файла
     * @return массив строк &ndash; прочитанные данные
     */
    public String[] read() {
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().toArray(String[]::new);
        } catch (IOException e) {
            logger.severe("Failed to read content from file " + file.getAbsolutePath() + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Установка модификатора перезаписи существующих данных в файле
     * @param rewrite модификатор перезаписи
     */
    public void setRewrite(boolean rewrite) {
        this.rewrite = rewrite;
    }

    /**
     * Объект файла
     * @return закрепленный файл за фильтром
     */
    public File getFile() {
        return file;
    }

    /**
     * Последние записанные данные в файл
     * @return список последних записанных данных в файл
     */
    public List<T> getLastWrittenItems() {
        return lastWrittenItems;
    }
}