package SearchPackage;

import java.util.*;
import java.io.*;

import org.jsoup.Jsoup;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;

public class Indexer {
    private static List<String> stopWords;
    private static HashMap<String, HashMap<String, Pair<Integer, Integer>>> invertedIndex;

    public static void main(String[] args) {
        invertedIndex = new HashMap<String, HashMap<String, Pair<Integer, Integer>>>();
        List<String> words = new Vector<>();
        try {
            readDummyVector(words);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stemming(words, "doc1");
        System.out.println(invertedIndex);
        stemming(words, "doc2");
        System.out.println(invertedIndex);
        stemming(words, "doc1");
        System.out.println(invertedIndex);

    }

    // Fot testing only
    private static void readDummyVector(List<String> words) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("test.txt"));
        String word;
        while ((word = reader.readLine()) != null) {
            String sp[] = word.split(" ");
            Collections.addAll(words, sp);

        }
    }

    public static String Parsing(String input) {
        String lines = "";
        StringBuilder Str = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));

            while ((lines = reader.readLine()) != null) {
                Str.append(lines);
            }
            reader.close();
            lines = Str.toString();
            Document html = Jsoup.parse(lines);
            lines = html.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static List<String> splitWords(String Lines) {
        List<String> words = new <String>Vector();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(Lines);
        while (match.find()) {
            words.add(match.group());
        }
        return words;
    }


    private void readStopWords() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));
        stopWords = new Vector<String>();
        String word;
        while ((word = reader.readLine()) != null) {
            stopWords.add(word);
        }
    }

    private void removeStopWords(List<String> words) {
        words.removeAll(stopWords);
    }

    private static void stemming(List<String> words, String docName) {
        PorterStemmer stemmer = new PorterStemmer();
        // stem words in the list
        for (int i = 0; i < words.size(); i++) {
            stemmer.setCurrent(words.get(i)); //set string you need to stem
            stemmer.stem();  //stem the word
            words.set(i, stemmer.getCurrent()); //get the stemmed word
        }

        for (String word : words) {
            // if word not exist then allocate a map for it
            if (!invertedIndex.containsKey(word)) {
                HashMap<String, Pair<Integer, Integer>> docsMapOfWord = new HashMap<String, Pair<Integer, Integer>>();
                invertedIndex.put(word, docsMapOfWord);
            }
            HashMap<String, Pair<Integer, Integer>> docsMapOfWord = invertedIndex.get(word);

            // if document not exist then allocate a pair for it
            if (!docsMapOfWord.containsKey(docName)) {
                Pair<Integer, Integer> TF_Size_pair = new Pair<Integer, Integer>(0, words.size());
                docsMapOfWord.put(docName, TF_Size_pair);
            }
            Pair<Integer, Integer> TF_Size_pair = docsMapOfWord.get(docName);
            TF_Size_pair.TF++;
        }
    }
}
