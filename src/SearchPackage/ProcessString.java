package SearchPackage;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessString {
    protected static List<String> stopWords;

//    public static void main(String[] args) {
//
//    }

    protected static List<String> splitWords(String Lines) {
        List<String> words = new <String>Vector();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher match = pattern.matcher(Lines);
        while (match.find()) {
            words.add(match.group());
        }
        return words;
    }

    protected static void readStopWords() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));
        stopWords = new Vector<String>();
        String word;
        while ((word = reader.readLine()) != null) {
            stopWords.add(word);
        }
    }

    protected static void convertToLower(List<String> temp) {
        for (int i = 0; i < temp.size(); i++) {
            temp.set(i, temp.get(i).toLowerCase());
        }
    }

    protected static void removeStopWords(List<String> words) {
        words.removeAll(stopWords);
    }

    protected static List<String> stemming(List<String> words) {
        PorterStemmer stemmer = new PorterStemmer();
        // stem words in the list
        for (int i = 0; i < words.size(); i++) {
            stemmer.setCurrent(words.get(i)); //set string you need to stem
            stemmer.stem();  //stem the word
            words.set(i, stemmer.getCurrent()); //get the stemmed word
        }
        return words;
    }

}
