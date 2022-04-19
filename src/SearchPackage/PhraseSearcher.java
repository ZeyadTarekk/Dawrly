package SearchPackage;

import org.jsoup.Jsoup;

import java.io.*;
import java.util.*;

public class PhraseSearcher {

    // TODO: Provide an interface to receive the list of query strings
    // TODO: Open each file and search for each query
    // TODO: Provide an interface which returns list of file names
    private static String[] fileNamesList;
    private static String folderRootPath;

    public static void main(String[] args) {
        List<String> phrases = new ArrayList<>();
        phrases.add("Mangaa");
        HashSet<String> resultNames = new HashSet<>();
        HashMap<String, List<String>> relatedParagraphs = new HashMap<>();
        searchForPhrases(phrases, resultNames, relatedParagraphs);
        System.out.println(resultNames);
        System.out.println(relatedParagraphs);
    }

    private static void searchForPhrases(List<String> phrases, HashSet<String> resultNames, HashMap<String, List<String>> relatedParagraphs) {

        // TODO: Get list of files
        readFileList();

        // TODO: search for this strings in the files
        try {
            getFiles(phrases, resultNames, relatedParagraphs);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void readFileList() {
        // creates a file object
        File file = new File("downloads");
        folderRootPath = "downloads//";
        // returns an array of all files
        fileNamesList = file.list();
    }

    private static void getFiles(List<String> phrases, HashSet<String> resultNames, HashMap<String, List<String>> relatedParagraphs) throws IOException {
        // TODO: [OPTIONAL] store the whole paragraph containing this strings

        for (String file : fileNamesList) {

            // Read file and convert it to string
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(folderRootPath + file));
            String lines = "";
            StringBuilder Str = new StringBuilder();
            while ((lines = reader.readLine()) != null) {
                Str.append(lines);
            }
            reader.close();
            lines = Str.toString();
            org.jsoup.nodes.Document html = Jsoup.parse(lines);
            lines = html.text();
            for (String phrase : phrases) {
                int index = lines.indexOf(phrase);
                if (index != -1) {
                    resultNames.add(file);
                    List<String> paragraphs = new ArrayList<>();
                    if (!relatedParagraphs.containsKey(file)) {
                        relatedParagraphs.put(file, paragraphs);
                    }
                    paragraphs.add(lines.substring((index - 20 > 0 ? index - 20 : index), Math.min(index + 50, lines.length() - 1)));
                }
            }
        }
    }

}
