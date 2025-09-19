package ca.gc.tbs.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

public class BadWordsTest {

    @BeforeEach
    void reset() throws Exception {
        Field wordsField = BadWords.class.getDeclaredField("words");
        wordsField.setAccessible(true);
        Set<String> words = (Set<String>) wordsField.get(null);
        words.clear();

        Field allowedWordsField = BadWords.class.getDeclaredField("allowedWords");
        allowedWordsField.setAccessible(true);
        Set<String> allowedWords = (Set<String>) allowedWordsField.get(null);
        allowedWords.clear();
    }

    @Test
    void testCensor_simpleSwear() throws Exception {
        Field wordsField = BadWords.class.getDeclaredField("words");
        wordsField.setAccessible(true);
        Set<String> words = (Set<String>) wordsField.get(null);
        words.clear();
        words.add("damn");

        String input = "This is a damn test";
        String output = BadWords.censor(input);

        Assertions.assertNotEquals(input, output, "Should censor the bad word");
    }

    @Test
    void testCensor_allowedWord() throws Exception {
        Field allowedWordsField = BadWords.class.getDeclaredField("allowedWords");
        allowedWordsField.setAccessible(true);
        Set<String> allowedWords = (Set<String>) allowedWordsField.get(null);
        allowedWords.add("testword");

        String input = "testword";
        String output = BadWords.censor(input);
        Assertions.assertEquals(input, output);
    }

    @Test
    void testAllowedWordsDefault() throws Exception {
        Field allowedWordsField = BadWords.class.getDeclaredField("allowedWords");
        allowedWordsField.setAccessible(true);
        Set<String> allowedWords = (Set<String>) allowedWordsField.get(null);
        allowedWords.clear();

        BadWords.loadConfigs();
        Assertions.assertTrue(BadWords.getAllowedWords().contains("carm"));
    }
}
