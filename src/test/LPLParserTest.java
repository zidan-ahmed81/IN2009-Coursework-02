package test;

// Maven: org.junit.jupiter:junit-jupiter:5.9.3


import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 500, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
class LPLParserTest {

    @ParameterizedTest
    @MethodSource("testFilePathsA")
    void parseA(String testFilePath) throws IOException {
        assertEquals("ok", test.Utils.parse(testFilePath));
    }

    @ParameterizedTest
    @MethodSource("testFilePathsB")
    void parseB(String testFilePath) throws IOException {
        assertEquals("ok", test.Utils.parse(testFilePath));
    }

    @ParameterizedTest
    @MethodSource("testFilePathsC")
    void parseC(String testFilePath) throws IOException {
        assertEquals("ok", test.Utils.parse(testFilePath));
    }

    private static Stream<String> testFilePathsA() {
        return test.Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/A");
    }

    private static Stream<String> testFilePathsB() {
        return test.Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/B");
    }

    private static Stream<String> testFilePathsC() {
        return test.Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/parser/C");
    }
}