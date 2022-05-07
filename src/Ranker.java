import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.io.IOException;
import java.util.*;
/*
HOW TO USE?
1- generateRelevance()
    Parameters: takes HashMap<String, HashMap<String, Pair<Integer, Integer, Double>>>
                returned from the database and converted and  List<String> pages
    returns HashMap of <String, Pair3<Double, String, String, String> sorted in a descending way with respect to
            scores of each page and fills the pages list with the golden Websites
            <String, Pair3<Double, String, String, String>
            page            Score Paragraph Title  Word
2- getPhraseSearching()
    Parameters: takes HashMap<String, HashMap<String, Pair<Integer, Integer, Double>>>
                returned from the database and converted
    return list of Strings of the websites containing all the words in the query
 */

public class Ranker {
    private Integer pagesNumber;
    private HashMap<String, Double> wordsNormalizedIDFS;
    private HashMap<String, HashMap<String, Pair2<Double, Double>>> wordsNormalizedTFSScores;
    //              page              word         TF     Score

    //    private HashMap<String, Double> pagesFinalScore;
    private HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore;
    //                    page        Score    Paragraph  title
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> resultProcessed;

    //    public Ranker(){
//
//    }
//    HashMap<String, HashMap<String, Pair<Integer, Integer,Double>>>
//             Words           page         tf         size   score
    private void getPagesNumber() {
        // creates a file object
        File file = new File("C:\\CMP\\CMP22\\Advanced Programming\\Project\\Search-Engine\\downloads");
        // returns an array of all files
        String[] fileNamesList = file.list();
        if (fileNamesList != null)
            pagesNumber = fileNamesList.length;
        else
            pagesNumber = 0;

    }

    private String stemTheWord(String word) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent(); //

    }

    private List<String> splitQuery(String query, List<String> stemmed) {
        List<String> words = Arrays.asList(query.split(" "));
        for (String word : words)
            stemmed.add(stemTheWord(word));
        return words;
    }

    private void generateIDFS() {
        wordsNormalizedIDFS = new HashMap<>();
        double dummyIDF;
        String dummyWord;
        for (Map.Entry<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> Entry : resultProcessed.entrySet()) {
            dummyWord = Entry.getKey();
            dummyIDF = ((double) pagesNumber / Entry.getValue().size());
            wordsNormalizedIDFS.put(dummyWord, dummyIDF);
        }
    }

    private void generateTFSAndScores() {
        wordsNormalizedTFSScores = new HashMap<>();
        HashMap<String, Pair2<Double, Double>> dummyMap;
        Pair<Integer, Integer, Double, Integer, Integer> dummyPair;
        for (String word : resultProcessed.keySet()) {
            for (String page : resultProcessed.get(word).keySet()) {
                dummyPair = resultProcessed.get(word).get(page);
//                if (resultProcessed.get(word).get(page) != null) {
                dummyMap = new HashMap<>();
                if (wordsNormalizedTFSScores.get(page) == null) {
                    wordsNormalizedTFSScores.put(page, dummyMap);
                    wordsNormalizedTFSScores.get(page).put(word, new Pair2<Double, Double>(((double) dummyPair.TF / dummyPair.size), dummyPair.score));

                } else {
                    wordsNormalizedTFSScores.get(page).put(word, new Pair2<Double, Double>(((double) dummyPair.TF / dummyPair.size), dummyPair.score));
                }
//                }
            }
        }
    }

    private void generateFinalScores() {
        pagesFinalScore = new HashMap<>();
        MongoDB dataBase = new MongoDB();
        dataBase.ConnectWithPagePopularity();
        int scorePopularity;
        double dummyScore;
        Pair3<Double, String, String, String> dummyScorePair;
        Pair2<Double, Double> dummyPair;
        for (String page : wordsNormalizedTFSScores.keySet()) {
            dummyScore = 0;
            for (String word : wordsNormalizedTFSScores.get(page).keySet()) {
                dummyPair = wordsNormalizedTFSScores.get(page).get(word);
                dummyScore = dummyScore + dummyPair.TF * dummyPair.score * wordsNormalizedIDFS.get(word);
            }
            scorePopularity = dataBase.getPagePopularity(page);
            pagesFinalScore.put(page, new Pair3<Double, String, String, String>(dummyScore * scorePopularity, "", "", ""));
        }

    }

    private void getParagraphs(HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore, HashMap<String, HashMap<String, Pair2<Double, Double>>> wordsNormalizedTFSScores, String query) {
        String wordToSearch;
        String wholeDocument;
        List<String> wordsInQueryStemmed = new ArrayList<>();
        List<String> wordsInQuery = splitQuery(query.toLowerCase(), wordsInQueryStemmed);
        int indexOfWord;
        List<Integer> actualIndices;
        Pair<Integer, Integer, Double, Integer, Integer> dummyPair;
        for (String page : pagesFinalScore.keySet()) {
            indexOfWord = -1;
            for (int i = 0; i < wordsInQueryStemmed.size(); i++) {
//                System.out.println(wordsInQuery.get(i) + " " + wordsInQueryStemmed.get(i) + " " + resultProcessed.get(wordsInQueryStemmed.get(i)));
                if (resultProcessed.get(wordsInQueryStemmed.get(i)) != null && resultProcessed.get(wordsInQueryStemmed.get(i)).get(page) != null) {
                    indexOfWord = i;
                    break;
                }
            }
            if (indexOfWord != -1) {
                wordToSearch = wordsInQuery.get(indexOfWord);
                dummyPair = resultProcessed.get(wordsInQueryStemmed.get(indexOfWord)).get(page);
                actualIndices = dummyPair.actualIndices;
                Connection connection;
                Document htmlDocument = null;
                try {
                    connection = Jsoup.connect(page);
                    htmlDocument = connection.get();
                } catch (IOException e) {
                    pagesFinalScore.get(page).setTitle("-1");
                    e.printStackTrace();
                    continue;
                }
                if (htmlDocument != null) {
                    pagesFinalScore.get(page).setTitle(htmlDocument.title());
                    wholeDocument = htmlDocument.body().text().toString().toLowerCase();
                    int index = -1;
                    for (Integer actualIndex : actualIndices) {
                        if (wholeDocument.indexOf(" ", actualIndex) == -1 && wholeDocument.substring(actualIndex, wholeDocument.length() - 1).equals(wordToSearch)) {
                            index = actualIndex;
                            continue;
                        }
                        if (wholeDocument.indexOf(" ", actualIndex) == -1)
                            continue;
                        if (wholeDocument.substring(actualIndex, wholeDocument.indexOf(" ", actualIndex)).equals(wordToSearch))
                            index = actualIndex;
                    }
                    if (index != -1) {
                        int endIndex = index + 100;
                        int startIndex = index - 100;
                        int newEndIndex;
                        if (startIndex > 0) {
                            newEndIndex = wholeDocument.indexOf(" ", endIndex);
                        } else if (startIndex == 0) {
                            newEndIndex = wholeDocument.indexOf(" ", endIndex);
                        } else {
                            startIndex = 0;
                            newEndIndex = wholeDocument.indexOf(" ", endIndex);
                        }

                        if (newEndIndex >= wholeDocument.length() || newEndIndex == -1) {
                            newEndIndex = wholeDocument.length() - 1;
                        }

                        String paragraph = wholeDocument.substring(startIndex, newEndIndex) + "...";
                        paragraph = paragraph.replaceAll("<", "");
                        paragraph = paragraph.replaceAll(">", "");
                        pagesFinalScore.get(page).setParagraph(paragraph);
                        pagesFinalScore.get(page).setWord(wordToSearch);
                    } else {
                        pagesFinalScore.get(page).setTitle("-1");
                    }
                }
            }


        }
    }

    private HashMap<String, Pair3<Double, String, String, String>> sortHashMap() {
        // Creating a list from elements of HashMap
        List<Map.Entry<String, Pair3<Double, String, String, String>>> list
                = new LinkedList<Map.Entry<String, Pair3<Double, String, String, String>>>(
                pagesFinalScore.entrySet());

        // Sorting the list using Collections.sort() method
        // using Comparator
        Collections.sort(
                list,
                new Comparator<Map.Entry<String, Pair3<Double, String, String, String>>>() {
                    public int compare(
                            Map.Entry<String, Pair3<Double, String, String, String>> object1,
                            Map.Entry<String, Pair3<Double, String, String, String>> object2) {
                        return (object2.getValue().getScore())
                                .compareTo(object1.getValue().getScore());
                    }
                });

        // putting the  data from sorted list back to hashmap
        HashMap<String, Pair3<Double, String, String, String>> result
                = new LinkedHashMap<String, Pair3<Double, String, String, String>>();
        for (Map.Entry<String, Pair3<Double, String, String, String>> me : list) {
            result.put(me.getKey(), me.getValue());
        }

        // returning the sorted HashMap
        return result;
    }

    public HashMap<String, Pair3<Double, String, String, String>> generateRelevance(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> result, List<String> pages, String query) {

//        System.out.println("Printing data structure from database");
//        System.out.println(result);
        getPagesNumber();
        this.resultProcessed = result;
        generateIDFS();
        generateTFSAndScores();
        generateFinalScores();

        for (String page : wordsNormalizedTFSScores.keySet()) {
            if (wordsNormalizedTFSScores.get(page).size() == resultProcessed.size())
                pages.add(page);
        }

        pagesFinalScore = sortHashMap();
        getParagraphs(pagesFinalScore, wordsNormalizedTFSScores, query);
        return pagesFinalScore;
    }

    public List<String> getPhraseSearching(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> result) {
        List<String> pages = new ArrayList<>();
        getPagesNumber();
        this.resultProcessed = result;
        generateTFSAndScores();

        for (String page : wordsNormalizedTFSScores.keySet()) {
            if (wordsNormalizedTFSScores.get(page).size() == resultProcessed.size())
                pages.add(page);
        }

        return pages;
    }
}
