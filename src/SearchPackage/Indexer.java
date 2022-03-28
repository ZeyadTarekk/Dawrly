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
        // read stop words
        try {
            readStopWords();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in reading stop words");
        }
        // creates a file object
        File file = new File("downloads");
        String folderRootPath = "downloads//";
        // returns an array of all files
        String[] fileNamesList = file.list();
        // iterate over files
        for (String fileName : fileNamesList) {
            // 1- parse html
            String noHTMLDoc = parsingHTML(fileName, folderRootPath);
            // 2- split words
            List<String> words = splitWords(noHTMLDoc);
            // 3-convert to lowercase
            convertToLower(words);
            // 4- remove stop words
            removeStopWords(words);
            // 5- stemming
            stemming(words, fileName);
            System.out.println(invertedIndex);
            System.out.println("\n\n");
        }
        printTableHtml();
    }

    private static void printTableHtml() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("invertedIndex.html"));
            writer.write("<table>");
            writer.write("  <tr>" +
                    "    <th>Word</th>\n" +
                    "    <th>Document tf size</th>\n" +
                    "  </tr>");

            for (String word : invertedIndex.keySet()) {
                writer.write("  <tr>");

                HashMap<String, Pair<Integer, Integer>> docs = invertedIndex.get(word);
                for (String doc : docs.keySet()) {
                    writer.write("  <td>");
                    writer.write(word);

                    writer.write("  </td>");
                    writer.write("  <td>");

                    Pair<Integer, Integer> tf_size = docs.get(doc);
                    writer.write("<strong>Doc Name</strong>: "+doc + " | <strong>TF</strong>: " + tf_size.TF + " | <strong>Size</strong>: " + tf_size.size);
                    writer.write("  </td>\n");

                }
                writer.write("  </tr>\n");

            }
            writer.write("</table>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    public static String parsingHTML(String input, String path) {
        String lines = "";
        StringBuilder Str = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path + input));

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


    private static void readStopWords() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));
        stopWords = new Vector<String>();
        String word;
        while ((word = reader.readLine()) != null) {
            stopWords.add(word);
        }
    }

    private static void convertToLower(List<String> temp) {
        for (int i = 0; i < temp.size(); i++) {
            temp.set(i, temp.get(i).toLowerCase());
        }
    }

    private static void removeStopWords(List<String> words) {
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
