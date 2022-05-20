import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
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

    //    private HashMap<String, Double> pagesFinalScore;
    private HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore = new LinkedHashMap<>();
    //                    page    Score    Paragraph  title word
    private HashMap<String, Double> pagesScores;
    private HashMap<String, Integer> numberOfWordsOnEachPage;

    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer, Double>>> resultProcessed;


//    HashMap<String, HashMap<String, Pair<Integer, Integer,Double>>>
//             Words           page         tf         size   score

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


    private void generateFinalScoresNew() {
        pagesScores = new HashMap<>();
        numberOfWordsOnEachPage = new HashMap<>();
        Pair<Integer, Integer, Double, Integer, Integer, Double> dummyPair;
        for (String word : resultProcessed.keySet()) {
            for (String page : resultProcessed.get(word).keySet()) {
                dummyPair = resultProcessed.get(word).get(page);
                if (pagesScores.get(page) == null) {
                    pagesScores.put(page, dummyPair.TF_IDF);
                    numberOfWordsOnEachPage.put(page, 1);
                } else {
                    pagesScores.put(page, pagesScores.get(page) + dummyPair.TF_IDF);
                    numberOfWordsOnEachPage.put(page, numberOfWordsOnEachPage.get(page) + 1);
                }
            }
        }

    }

    private void getParagraphs(HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore, String query) {
        String wordToSearch = null;
        String wholeDocument;
        String pageName;
        String titlePage;
        boolean phraseSearchFlag;
        boolean notFoundPhrase = false;
        if (query.contains("\"")) {
            query = query.replaceAll("\"", "");
            phraseSearchFlag = true;
        } else
            phraseSearchFlag = false;
        List<String> wordsInQueryStemmed = new ArrayList<>();
        List<String> wordsInQuery = splitQuery(query.toLowerCase(), wordsInQueryStemmed);
        for (String page : pagesScores.keySet()) {
            int index = -1;
            try {
                pageName = page;
                pageName = pageName.replace("*", "`{}");
                pageName = pageName.replace("://", "}");
                pageName = pageName.replace("/", "{");
                pageName = pageName.replace("?", "`");
                pageName = pageName + ".html";
//                    System.out.println(pageName);
                File file = new File("bodyFiles//" + pageName);
                BufferedReader br = new BufferedReader(new FileReader(file));
                titlePage = br.readLine();
                wholeDocument = br.readLine().toLowerCase();
            } catch (IOException e) {
                Pair3<Double, String, String, String> dummyPair = new Pair3<>();
                dummyPair.setTitle("-1");
                pagesFinalScore.put(page, dummyPair);
                e.printStackTrace();
                continue;
            }
            if (phraseSearchFlag)
                if (wholeDocument.contains(query.toLowerCase())) {
                    index = wholeDocument.indexOf(query.toLowerCase());
                    wordToSearch = query;
                    notFoundPhrase = false;
                } else
                    notFoundPhrase = true;

            if (!phraseSearchFlag || notFoundPhrase)
                for (String word : wordsInQuery)
                    if (wholeDocument.contains(word.toLowerCase())) {
                        wordToSearch = word;
                        index = wholeDocument.indexOf(wordToSearch);
                    }
            Pair3<Double, String, String, String> dummyPair = new Pair3<>();
            if (index != -1) {


                int endIndex = index + 100;
                int startIndex = index - 100;
                int newEndIndex;
                if (startIndex > 0) {
                    while (wholeDocument.charAt(startIndex) != ' ' && startIndex != 0)
                        startIndex--;
                    if (wholeDocument.charAt(startIndex) == ' ')
                        startIndex++;
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

                dummyPair.setParagraph(paragraph);
                dummyPair.setWord(wordToSearch);
                dummyPair.setTitle(titlePage);
            } else {
                pagesFinalScore.get(page).setTitle("-1");
            }
            pagesFinalScore.put(page, dummyPair);


        }


    }

    private void getPagePopularity() {
        MongoDB dataBase = new MongoDB();
        dataBase.ConnectWithPagePopularity();
        HashMap<String, Integer> popularityScores = dataBase.getPagePopularity();
        int scorePopularity;
        for (String page : pagesScores.keySet()) {
            if (popularityScores.get(page) == null)
                scorePopularity = 1;
            else
                scorePopularity = popularityScores.get(page);
            pagesScores.put(page, pagesScores.get(page) * scorePopularity);
        }
    }

    private void fetchParagraphs(HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore, String query) {

        //clear the query from ""
        if (query.contains("\"")) {
            query = query.replaceAll("\"", "");
        }
        query = query.toLowerCase();

        for (String page : pagesScores.keySet()) {
            String titlePage, wholeDocument;
            try {
                String pageName = page;
                pageName = pageName.replace("*", "`{}");
                pageName = pageName.replace("://", "}");
                pageName = pageName.replace("/", "{");
                pageName = pageName.replace("?", "`");
                pageName = pageName + ".html";
                File file = new File("bodyFiles\\" + pageName);
                BufferedReader br = new BufferedReader(new FileReader(file));
                String lineRead;
                lineRead = br.readLine();
                if (lineRead != null)
                    titlePage = lineRead;
                else
                    titlePage = "-1";
                lineRead = br.readLine();
                if (lineRead != null)
                    wholeDocument = lineRead.toLowerCase();
                else
                    continue;
            } catch (IOException e) {
                Pair3<Double, String, String, String> dummyPair = new Pair3<>();
                dummyPair.setTitle("-1");
                dummyPair.setScore(pagesScores.get(page));
                pagesFinalScore.put(page, dummyPair);
                e.printStackTrace();
                continue;
            }

            String queryList[] = query.split(" ");
            int bestCount = -1, currentCount = 0, index = 0, finalIndex = 0;
            int docLength = wholeDocument.length();
            String subPara, bestPara = null;

            while (true) {
                finalIndex = index + 500;
                currentCount = 0;

//                System.out.println("Start = " + index + " End = " + finalIndex + " doc length = " + docLength);
                if (index < docLength) {

                    //check if the finalIndex bigger than the document length
                    if (finalIndex >= docLength) {
                        finalIndex = docLength - 1;

                        while (wholeDocument.charAt(index) != ' ' && index < docLength && finalIndex > index + 350)
                            index++;

                        //get the sub-paragraph and check the number of query words in it
                        subPara = wholeDocument.substring(index, finalIndex);
                        for (int i = 0; i < queryList.length; i++) {
                            if (subPara.contains(queryList[i]))
                                currentCount++;
                        }
                        //save the start index of the best paragraph
                        if (currentCount > bestCount) {
                            bestCount = currentCount;
                            bestPara = subPara;
                        }
                        break;

                    } else {

                        while (wholeDocument.charAt(index) != ' ' && index < docLength && finalIndex > index + 350)
                            index++;

                        //get the sub-paragraph and check the number of query words in it
                        subPara = wholeDocument.substring(index, finalIndex);
                        for (int i = 0; i < queryList.length; i++) {
                            if (subPara.contains(queryList[i]))
                                currentCount++;
                        }
                        //save the start index of the best paragraph
                        if (currentCount > bestCount) {
                            bestCount = currentCount;
                            bestPara = subPara;
                        }
                        if (currentCount == queryList.length)
                            break;
                        index = finalIndex + 1;
                    }
                } else break;

            }
            Pair3<Double, String, String, String> dummyPair = new Pair3<>();
            dummyPair.setWord(query);
            dummyPair.setScore(pagesScores.get(page));
            if (bestCount > 0) {
                bestPara = bestPara + "....";
                bestPara = bestPara.replaceAll("<", "").replaceAll(">", "");
                titlePage = titlePage.replaceAll("<", "").replaceAll(">", "");
                dummyPair.setTitle(titlePage);
                dummyPair.setParagraph(bestPara);
            } else {
                dummyPair.setTitle("-1");
                dummyPair.setScore(pagesScores.get(page));
            }
            pagesFinalScore.put(page, dummyPair);


        }
    }

    private HashMap<String, Double> sortHashMap() {
        List<Map.Entry<String, Double>> list
                = new ArrayList<Map.Entry<String, Double>>(
                pagesScores.entrySet());


        list.sort(new Comparator<Map.Entry<String, Double>>() {
            public int compare(
                    Map.Entry<String, Double> object1,
                    Map.Entry<String, Double> object2) {
                return (object2.getValue())
                        .compareTo(object1.getValue());
            }
        });

        HashMap<String, Double> result
                = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> me : list) {
            result.put(me.getKey(), me.getValue());
        }
        return result;
    }

    public HashMap<String, Pair3<Double, String, String, String>> generateRelevance(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer, Double>>> result, List<String> pages, String query) {


        this.resultProcessed = result;

        generateFinalScoresNew();

        getPagePopularity();
        pagesScores = sortHashMap();
        fetchParagraphs(pagesFinalScore, query);

        for (String page : pagesScores.keySet()) {
            if (numberOfWordsOnEachPage.get(page) >= resultProcessed.size())
                pages.add(page);
        }
//        getParagraphs(pagesFinalScore, query);
        return pagesFinalScore;
    }


    public static void main(String[] args) {

        Ranker rank = new Ranker();
        HashMap<String, Pair3<Double, String, String, String>> finalResult;
        Indexer indexer = new Indexer();

        List<String> phraseSearch = new ArrayList<>();
        QueryProcessor qp = new QueryProcessor();
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer, Double>>> result = qp.processQuery("normalize", phraseSearch);
        System.out.println("============================================");
        System.out.println(result);
        System.out.println("============================================");
        finalResult = rank.generateRelevance(result, phraseSearch, "normalize");
        System.out.println(finalResult);
    }


}
