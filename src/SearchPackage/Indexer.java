package SearchPackage;

import java.util.*;
import java.io.*;

import org.jsoup.Jsoup;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jsoup.nodes.Document;

public class Indexer {
    List<String> stopWords;

    public static void main(String[] args) {
//        String s=Parsing("../input.txt");
//        Vector<String> ll;
//        ll= splitWords(s);
//        for(int i=0;i<ll.size();i++){
//            System.out.println(ll.get(i));
//        }
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

    public static Vector<String> splitWords(String Lines) {
        Vector<String> words = new <String>Vector();
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
}
