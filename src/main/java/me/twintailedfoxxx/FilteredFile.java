package me.twintailedfoxxx;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Отфильтрованный файл
 * @param <T> тип данных, который будет храниться в файле
 */
public class FilteredFile<T> {
    /**
     * Закрепленный за фильтром файл
     */
    private final File file;

    /**
     * Модификатор, отвечающий за перезаписывание существующих данных в отфильтрованном файле
     */
    private boolean rewrite;

    /**
     * Список последних записанных объектов в файл
     */
    private final List<T> lastWrittenItems;

    private final Logger logger = Main.getLoggerInstance();

    /**
     * Конструктор отфильтрованного файла
     * @param file файл, куда будут записываться отфильтрованные данные
     * @param rewrite модификатор перезаписи существующих данных в файле
     */
    public FilteredFile(File file, boolean rewrite) {
        this.file = file;
        this.rewrite = rewrite;
        this.lastWrittenItems = new ArrayList<>();
    }

    /**
     * Запись отфильтрованных данных в файл
     * @param items отфильтрованные данные
     */
    @SafeVarargs
    public final void write(T... items) {
        if(!lastWrittenItems.isEmpty()) {
            lastWrittenItems.clear();
        }

        if(!file.exists()) {
            try {
                if(!file.getParentFile().exists()) {
                    if(file.getParentFile().mkdirs()) {
                        logger.info("Created directory " + file.getParentFile().getAbsolutePath());
                    }
                }

                if(file.createNewFile()) {
                    logger.info("Created file " + file.getAbsolutePath());
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