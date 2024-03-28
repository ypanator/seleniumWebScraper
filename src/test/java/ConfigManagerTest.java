import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Properties;

public class ConfigManagerTest {
    static final String PATH = "src/test/resources/config.properties";
    static Properties cfg;

    @BeforeAll
    static void init() throws IOException {
        new File(PATH).createNewFile();
        cfg = new Properties();
        cfg.setProperty("date",     "");
        cfg.setProperty("timeframe","100");
        cfg.setProperty("password", "abcde12345");
        cfg.setProperty("arg",      "--headless");
        cfg.setProperty("login",    "abcde12345");
        try (FileOutputStream fos = new FileOutputStream(PATH)) { cfg.store(fos, null); }
    }
    
    @Nested
    @DisplayName("Reading test suite.")
    static class ReadTest {

        @AfterEach
        static void cleanUp() throws IOException {
            cfg.setProperty("timeframe", "100");
            try (FileOutputStream fos = new FileOutputStream(PATH)) { cfg.store(fos, null); }
        }

        @Test
        @DisplayName("Testing reading correct config file.")
        void correctConfigTest() throws IOException {
            ConfigManager cfgm = new ConfigManager(PATH);
            assertEquals(cfgm.date,     "");
            assertEquals(cfgm.timeframe,100);
            assertEquals(cfgm.password, "abcde12345");
            assertEquals(cfgm.arg,      "--headless");
            assertEquals(cfgm.login,    "abcde12345");
        }
    
        @Test
        @DisplayName("Testing reading invalid timeframe.")
        void invalidTimeframeTest() throws IOException {
            try (FileOutputStream fos = new FileOutputStream(PATH)) {
                cfg.setProperty("timeframe", "not a number!");
                cfg.store(fos, null);
                assertThrows(NumberFormatException.class, () -> new ConfigManager(PATH));
            }
        }
    
        @Test
        @DisplayName("Testing reading not present key.")
        void keyNotPresentTest() throws IOException {
            try (FileOutputStream fos = new FileOutputStream(PATH)) {
                cfg.remove("timeframe");
                cfg.store(fos, null);
                assertThrows(IllegalArgumentException.class, () -> new ConfigManager(PATH));
            }
        }

        @Test
        @DisplayName("Testing invalid config path.")
        void incorrectPathTest() {
            assertThrows(FileNotFoundException.class, () -> new ConfigManager(""));
        }
    }

    @Nested
    @DisplayName("Writing test suite.")
    class WriteTest {

        @Test
        @DisplayName("Test saving correct date.")
        void correctDateSaveTest() throws IOException {
            final String DATE = LocalDate.now().toString();
            ConfigManager.saveDate(DATE, PATH);

            Properties cfg = new Properties();
            try (FileInputStream fis = new FileInputStream(PATH)) { cfg.load(fis); }
            assertEquals(cfg.getProperty("date"), DATE);
        }

        @Test
        @DisplayName("Test incorrect config path.")
        void incorrectPathTest() {
            assertThrows(FileNotFoundException.class, () -> ConfigManager.saveDate("", ""));
        }
        
        @Test
        @DisplayName("Test formating correct date.")
        void correctDateFormatTest() {
            assertEquals(ConfigManager.formatToLocalDate("2 kwiecień 2024"), LocalDate.of(2024, 4, 2));
        }

        @Test
        @DisplayName("Test formatting incorrect date.")
        void incorrectDateFormatTest() {
            assertThrows(DateTimeParseException.class, () -> ConfigManager.formatToLocalDate("abcdef"));
        }

        @Test
        @DisplayName("Test formatting empty date.")
        void emptyDateFormatTest() {
            assertThrows(DateTimeParseException.class, () -> ConfigManager.formatToLocalDate(""));
        }
    }

    @AfterAll
    static void tearDownFile() {
        new File(PATH).delete();
    }
}