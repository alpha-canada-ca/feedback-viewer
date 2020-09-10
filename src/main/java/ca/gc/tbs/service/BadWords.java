package ca.gc.tbs.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class BadWords {

	static Set<String> words = new HashSet<String>();

	public class LengthComparator implements java.util.Comparator<String> {

		public int compare(String s1, String s2) {
			if (s1.length() > s2.length()) {
				return -1;
			} else if (s1.length() < s2.length()) {
				return 1;
			} else {
				return s1.compareTo(s2);
			}
		}
	}

	public static void loadConfigs() {
		loadGoogleConfigs();
		loadFileConfigs("static/badwords/facebook_badwords_en.txt");
		loadFileConfigs("static/badwords/youtube_badwords_en.txt");
		loadFileConfigs("static/badwords/badwords_fr.txt");
		loadFileConfigs("static/badwords/threats_fr.txt");
		loadFileConfigs("static/badwords/threats_en.txt");
		System.out.println("Loaded " + words.size() + " words to filter out");
	}

	public static void loadFileConfigs(String filePath) {
		try {
			Resource resource = new ClassPathResource(filePath, BadWords.class.getClassLoader());
			String newWords[] = IOUtils.toString(resource.getInputStream(), "UTF-8").split(",");
			for (String word : newWords) {
				words.add(word.trim());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void loadGoogleConfigs() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(
					"https://docs.google.com/spreadsheets/d/1hIEi2YG3ydav1E06Bzf2mQbGZ12kh2fe4ISgLg_UBuM/export?format=csv")
							.openConnection().getInputStream()));
			String line = "";

			while ((line = reader.readLine()) != null) {
				String[] content = null;
				try {
					content = line.split(",");
					if (content.length == 0) {
						continue;
					}
					String word = content[0];
					words.add(word);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String removeLeetSpeak(String input) {
		input = input.replaceAll("1", "i");
		input = input.replaceAll("!", "i");
		input = input.replaceAll("3", "e");
		input = input.replaceAll("4", "a");
		input = input.replaceAll("@", "a");
		input = input.replaceAll("5", "s");
		input = input.replaceAll("7", "t");
		input = input.replaceAll("0", "o");
		input = input.replaceAll("9", "g");
		input = input.replaceAll("\\$", "s");
		return input;
	}

	static String censor(String text) {

		text = removeLeetSpeak(text);
		// Break down sentence by ' ' spaces
		// and store each individual word in
		// a different list
		String[] word_list = text.split("\\s+");

		// A new string to store the result
		String result = "";

		// Iterating through our list
		// of extracted words
		int index = 0;
		for (String i : word_list) {
			String wordToCheckOrig = i;
			String wordToCheck = removeLeetSpeak(wordToCheckOrig);
			wordToCheck = wordToCheck.toLowerCase().replaceAll("[^a-zA-Z]", "");
			if (words.contains(wordToCheck)) {
				// changing the censored word to
				// created asterisks censor
				word_list[index] = createMask(wordToCheckOrig);
			}
			index++;
		}

		// join the words
		for (String i : word_list)
			result += i + ' ';

		return result;
	}

	public static String createMask(String word) {
		String mask = "";
		for (int i = 0; i < word.length(); i++) {
			mask += "#";
		}
		return mask;
	}

}