package me.twintailedfoxxx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    private static FilteredFile<Long> integersFile;
    private static FilteredFile<Float> floatsFile;
    private static FilteredFile<String> stringsFile;
    private static Logger logger;

    public static void main(String[] args) {
        logger = new Logger(System.out);

        String path = System.getProperty("user.dir");
        String os = System.getProperty("os.name");
        String prefix = "";
        boolean rewrite = true;
        boolean fullStatistics = false;
        List<String> filesToRead = new ArrayList<>();

        if(args.length == 0) {
            outputUsage();
            return;
        }

        for(int i = 0; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "-o" -> {
                    try {
                        if(args[i + 1] == null || args[i + 1].matches("-[opafs?]+$")) {
                            logger.warn("You did not specify the path. Usage: -o [full path]");
                            outputUsage();
                            return;
                        }

                        if((os.contains("Windows") && !args[i + 1]
                                .matches("^[a-zA-Z]:\\\\(((?![<>:\"/\\\\|?*]).)+((?<![ .])\\\\)?)*$")
                           ) || (os.contains("Linux") || os.contains("Mac OS") || os.contains("Unix")) && !args[i + 1]
                                .matches("^(/[^/ ]*)+/?$")) {
                            logger.warn("Invalid path!");
                            outputUsage();
                            return;
                        }

                        path = args[i + 1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.warn("You did not specify the path. Usage: -o [full path]");
                        outputUsage();
                        return;
                    }
                }
                case "-p" -> {
                    try {
                        if(args[i + 1] == null || args[i + 1].matches("-[opafs?]+$")) {
                            logger.warn("You did not specify the prefix. Usage: -p [file prefix]");
                            outputUsage();
                            return;
                        }

                        if(!args[i + 1].matches("^[\\wа-яА-Я\\-._]+$")) {
                            logger.warn("Invalid prefix. Use cyrillic or latin letters. Allowed characters: -, _");
                            outputUsage();
                            return;
                        }

                        prefix = args[i + 1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.warn("You did not specify the file prefix. Usage: -p [prefix]");
                        outputUsage();
                        return;
                    }
                }
                case "-a" -> rewrite = false;
                case "-f" -> fullStatistics = true;
                case "-s" -> fullStatistics = false;

                default -> {
                    if(args[i].contains(".txt")) {
                        filesToRead.add(args[i]);
                    }
                }
            }
        }

        if(filesToRead.isEmpty()) {
            logger.warn("You did not specify what .txt files to read.");
            outputUsage();
            return;
        }

        integersFile = new FilteredFile<>(new File(path + File.separator + prefix +
                "integers.txt"), rewrite);
        floatsFile = new FilteredFile<>(new File(path + File.separator + prefix +
                "floats.txt"), rewrite);
        stringsFile = new FilteredFile<>(new File(path + File.separator + prefix +
                "strings.txt"), rewrite);

        filterFiles(filesToRead.toArray(String[]::new));
        outputStatistics(fullStatistics);
    }

    private static void filterFiles(String... files) {
        List<Long> integers = new ArrayList<>();
        List<Float> floats = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        for(String path : files) {
            File file = new File(path);
            if(!file.exists()) {
                logger.warn("File " + path + " does not exist!");
                continue;
            }

            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while((line = reader.readLine()) != null) {
                    try {
                        if(line.matches("[+-]?[0-9]*\\.[0-9]+") ||
                                line.matches("[+-]?\\d*\\.\\d+([eE][+-]?\\d+)?")) {
                            float parsedFloat = Float.parseFloat(line);
                            floats.add(parsedFloat);
                        } else {
                            long parsedLong = Long.parseLong(line);
                            integers.add(parsedLong);
                        }
                    } catch (NumberFormatException e) {
                        strings.add(line);
                    }
                }
            } catch (IOException e) {
                logger.severe("Failed to read file " + path + ": " + e.getMessage());
            }
        }

        integersFile.write(integers.toArray(Long[]::new));
        floatsFile.write(floats.toArray(Float[]::new));
        stringsFile.write(strings.toArray(String[]::new));
    }

    private static void outputStatistics(boolean full) {
        logger.info("Integers written: " + integersFile.getLastWrittenItems().size());
        logger.info("Floats written: " + floatsFile.getLastWrittenItems().size());
        logger.info("Strings written: " + stringsFile.getLastWrittenItems().size());
        System.out.println();

        if(full) {
            long intMax = integersFile.getLastWrittenItems().stream()
                    .max(Comparator.comparingLong(Long::longValue))
                    .orElse(0L);
            long intMin = integersFile.getLastWrittenItems().stream()
                    .min(Comparator.comparingLong(Long::longValue))
                    .orElse(0L);
            long intSum = integersFile.getLastWrittenItems().stream()
                    .reduce(Long::sum)
                    .orElse(0L);
            long intMid = intSum / integersFile.getLastWrittenItems().size();

            float floatMax = floatsFile.getLastWrittenItems().stream()
                    .max(Comparator.comparingDouble(Float::doubleValue))
                    .orElse(0.0F);
            float floatMin = floatsFile.getLastWrittenItems().stream()
                    .min(Comparator.comparingDouble(Float::doubleValue))
                    .orElse(0.0F);
            float floatSum = floatsFile.getLastWrittenItems().stream()
                    .reduce(Float::sum)
                    .orElse(0.0F);
            float floatMid = floatSum / floatsFile.getLastWrittenItems().size();

            int strMax = stringsFile.getLastWrittenItems().stream()
                    .max(Comparator.comparingInt(String::length))
                    .orElse("").length();
            int strMin = stringsFile.getLastWrittenItems().stream()
                    .min(Comparator.comparingInt(String::length))
                    .orElse("").length();

            logger.info("Min integer: " + intMin);
            logger.info("Max integer: " + intMax);
            logger.info("Mid integer: " + intMid);
            System.out.println();
            logger.info("Min float: " + floatMin);
            logger.info("Max float: " + floatMax);
            logger.info("Mid float: " + floatMid);
            System.out.println();
            logger.info("Longest string length: " + strMax);
            logger.info("Shortest string length: " + strMin);
        }
    }

    private static void outputUsage() {
        System.out.println("""
                Usage:
                {} - optional
                [] - required
                \tutil.jar {-o [full path]} {-p [file prefix]} {-a} {-f} {-s} [paths to .txt files]
                Parameters:
                \t-o: Sets a path where the filtered files will be stored. (default=current directory)
                \t-p: Sets a prefix in the filtered files' file names. (default=no prefix)
                \t-a: Using this parameter the file contents of filtered files will not be rewritten
                \t-s: Outputs a short statistic after file filtration. (default)
                \t-f: Outputs a full statistic after file filtration.
                """);
    }

    public static Logger getLoggerInstance() {
        return logger;
    }
}