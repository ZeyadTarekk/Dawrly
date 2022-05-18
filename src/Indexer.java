
import java.util.*;
import java.io.*;

// mongo libraries
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.tartarus.snowball.ext.PorterStemmer;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import org.bson.conversions.Bson;
import org.json.simple.JSONObject;

import org.jsoup.Jsoup;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class Indexer extends ProcessString implements Runnable {
    private final int threadNumber = 9;
    private static String[] fileNamesList;
    private static String folderRootPath;
    private static HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> invertedIndex;
    // HashMap<fileName,All words in the file after processing>
    // This map helps in phrase searching
    private static HashMap<String, List<String>> processedFiles;
    private static HashMap<String, Double> tagsOfHtml;
    private static HashMap<String, HashMap<String, Double>> scoreOfWords;
    private static HashMap<String, HashMap<String, List<Integer>>> indicesOfWord;
    // TODO: Synchronization of Threads to avoid Concurrency Exception

    public void startIndexing() throws InterruptedException {
        invertedIndex = new HashMap<>();
        indicesOfWord = new HashMap<>();
        scoreOfWords = new HashMap<>();
        List<JSONObject> invertedIndexJSON;

        // read stop words and fill score of tags
        try {
            readStopWords();
            fillScoresOfTags();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // creates a file object
        File file = new File("downloads");
        folderRootPath = "downloads//";
        // returns an array of all files
        fileNamesList = file.list();

        Thread[] threads = new Thread[threadNumber];
        for (int i = 0; i < threadNumber; i++) {
            threads[i] = new Thread(new Indexer());
            threads[i].setPriority(i + 1);
        }
        for (int i = 0; i < threadNumber; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threadNumber; i++) {
            threads[i].join();
        }

        // 8- converted the inverted index into json format
        invertedIndexJSON = convertInvertedIndexToJSON(invertedIndex);
        // 9- Upload to database
        System.out.println("Start uploading to database");
        uploadToDB(invertedIndexJSON);
        System.out.println("Indexer has finished");
    }

    // 30
    // 0*6 => 1*6 0
    // 1*6 => 2*6
    // 2*6 => 3*6
    @Override
    public void run() {
        int start = (Thread.currentThread().getPriority() - 1) * (int) Math.ceil(fileNamesList.length / (double)threadNumber);
        int end = (Thread.currentThread().getPriority()) * (int) Math.ceil(fileNamesList.length / (double)threadNumber);
        // iterate over files
        for (int i = start; i < Math.min(end, fileNamesList.length); i++) {
            String fileName = fileNamesList[i];
            String oldFileName = new String(fileName);
            // TODO: modify file name
            fileName = fileName.replace("`{}", "*");
            fileName = fileName.replace("}", "://");
            fileName = fileName.replace("{", "/");
            fileName = fileName.replace("`", "?");
            fileName = fileName.replace(".html", "");

            // 1- parse html
            StringBuilder noHTMLDoc = new StringBuilder("");
            StringBuilder originalDoc = new StringBuilder("");
            try {
                org.jsoup.nodes.Document html = parsingHTML(oldFileName, folderRootPath, noHTMLDoc, originalDoc);

                filterTags(html, fileName);
                createBodyFiles(html, fileNamesList[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 2- split words
            List<String> words = splitWords(noHTMLDoc.toString());
            // 3-get indices of each word
            getIndexOfWord(words, originalDoc, fileName); // TODO: Synchronized threads
            // 4-convert to lowercase
            convertToLower(words);
            // 5- remove stop words
            removeStopWords(words);
            // 6- stemming
            List<String> stemmedWords = stemming(words);
            // 7- fill other tags with score
            filOtherTags(stemmedWords, fileName);
            // 8- build processed words
            // buildProcessedFiles(fileName, stemmedWords);
            // 9- build inverted index
            buildInvertedIndex(stemmedWords, fileName, invertedIndex);
            System.out.printf("#%d Thread #%d processed file: %s\n", i, Thread.currentThread().getPriority(), fileName);
        }
    }

    private static org.jsoup.nodes.Document parsingHTML(String input, String path, StringBuilder HTML, StringBuilder Str) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path + input));
        String lines = "";
        while ((lines = reader.readLine()) != null) {
            Str.append(lines);
        }
        reader.close();
        org.jsoup.nodes.Document html = Jsoup.parse(Str.toString());
        Str.setLength(0);
        Str.append(html.body().text());
        HTML.append(html.title() + " " + html.body().text());
        return html;
    }

    private static synchronized void buildInvertedIndex(List<String> stemmedWords, String docName, HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> invertedIndex) {
        for (int i = 0; i < stemmedWords.size(); i++) {
            String word = stemmedWords.get(i);
            // if word not exist then allocate a map for it
            if (!invertedIndex.containsKey(word)) {
                HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>> docsMapOfWord = new HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>();
                invertedIndex.put(word, docsMapOfWord);
            }
            HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>> docsMapOfWord = invertedIndex.get(word);

            // if document not exist then allocate a pair for it
            if (!docsMapOfWord.containsKey(docName)) {
                if (scoreOfWords.get(docName).get(word) == null)
                    System.out.println("Error==> " + word);
                Pair<Integer, Integer, Double, Integer, Integer> TF_Size_pair = new Pair<Integer, Integer, Double, Integer, Integer>(0, stemmedWords.size(), scoreOfWords.get(docName).get(word));
                docsMapOfWord.put(docName, TF_Size_pair);
                TF_Size_pair.index = new ArrayList<>();
                TF_Size_pair.actualIndices = indicesOfWord.get(docName).get(word);
            }
            Pair<Integer, Integer, Double, Integer, Integer> TF_Size_pair = docsMapOfWord.get(docName);
            TF_Size_pair.TF++;
            TF_Size_pair.index.add(i);
        }
    }

    // TODO: insert the file and its processed words
    private static synchronized void buildProcessedFiles(String FileName, final List<String> stemmedWords) {
        processedFiles.put(FileName, stemmedWords);
    }

    private static List<JSONObject> convertInvertedIndexToJSON(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> invertedIndexP) {
        /*
        *
        {
            {
                word: word1
                docs:
                    [
                        {
                            docName:doc1
                            tf:10,
                            size:10
                        }
                    ]
            }
        }
        *
        * */
        List<JSONObject> listOfWordJSONS = new Vector<>();
        for (String word : invertedIndexP.keySet()) {
            JSONObject wordJSON = new JSONObject();
            wordJSON.put("word", word);
            List<JSONObject> documents = new Vector<>();
            for (String doc : invertedIndexP.get(word).keySet()) {
                JSONObject documentJSON = new JSONObject();
                documentJSON.put("document", doc);
                documentJSON.put("tf", invertedIndexP.get(word).get(doc).TF);
                documentJSON.put("size", invertedIndexP.get(word).get(doc).size);
                documentJSON.put("score", invertedIndexP.get(word).get(doc).score);
                documentJSON.put("index", invertedIndexP.get(word).get(doc).index);
                documentJSON.put("actualIndices", invertedIndexP.get(word).get(doc).actualIndices);
                documents.add(documentJSON);
            }
            wordJSON.put("documents", documents);
            listOfWordJSONS.add(wordJSON);
        }
        return listOfWordJSONS;
    }

    private static void uploadToDB(List<JSONObject> invertedIndexJSONParameter) {
//        MongoClient client = MongoClients.create("mongodb+srv://mongo:Bq43gQp#mBQ-6%40S@cluster0.emwvc.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        com.mongodb.MongoClient client = new com.mongodb.MongoClient();
        MongoDatabase database = client.getDatabase("SearchEngine");
        MongoCollection<Document> toys = database.getCollection("invertedIndex");
        // check if Doc is found or Not
        // if not exists ==> insert new Doc
        //else check if word is existed by filtration ,then replace it
        for (int i = 0; i < invertedIndexJSONParameter.size(); i++) {
            Document doc = new Document(invertedIndexJSONParameter.get(i));
            Document found = (Document) toys.find(new Document("word", invertedIndexJSONParameter.get(i).get("word"))).first();
            if (found != null) {
                Bson query = eq("word", invertedIndexJSONParameter.get(i).get("word")); //filtration
                toys.replaceOne(query, doc);
            } else {
                toys.insertOne(doc);
            }
        }

    }

    private static synchronized void fillScoresOfTags() {
        // score of each tag
        //    title = 1
        //    h1 = 0.7
        //    h2 = 0.6
        //    h3 = 0.5
        //    h4 = 0.4
        //    h5 = 0.3
        //    h6 = 0.2
        //    else = 0.1
        tagsOfHtml = new HashMap<String, Double>();
        tagsOfHtml.put("title", 0.9);
        Double j = 0.6;
        for (int i = 1; i <= 6; i++) {
            tagsOfHtml.put("h" + i, j);
            j -= 0.1;
        }
    }

    private static synchronized void filterTags(org.jsoup.nodes.Document html, String fileName) throws IOException {
        PorterStemmer stemmer = new PorterStemmer();
        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher;
        HashMap<String, Double> tempScore = new HashMap<>();
        //filtration most important tags
        for (String line : tagsOfHtml.keySet()) {
            String taggedString = html.select(line).text();
            if (html != null && !taggedString.isEmpty()) {
                matcher = pattern.matcher(taggedString.toLowerCase());
                while (matcher.find()) {
                    stemmer.setCurrent(matcher.group());
                    stemmer.stem();
                    taggedString = stemmer.getCurrent();
                    if (!tempScore.containsKey(taggedString))
                        tempScore.put(taggedString, tagsOfHtml.get(line));
                    else
                        tempScore.put(taggedString, tempScore.get(taggedString) + tagsOfHtml.get(line));
                }
            }
        }
        scoreOfWords.put(fileName, tempScore);
    }

    private static synchronized void filOtherTags(List<String> stemmedWords, String fileName) {
        HashMap<String, Double> tempScore = new HashMap<>();
        for (String word : stemmedWords) {
            if (tempScore.containsKey(word)) {
                tempScore.put(word, 0.1 + tempScore.get(word));
            } else
                tempScore.put(word, 0.1);
        }
        tempScore.keySet().remove(""); //remove empty string
        scoreOfWords.put(fileName, tempScore);
    }

    // get indices  of each word in each Document
    private static synchronized void getIndexOfWord(List<String> splitWord, StringBuilder originalDoc, String fileName) {
        // TODO: matching actual string not substring in document
        Integer lengthOfDoc = originalDoc.length();
        PorterStemmer stemmer = new PorterStemmer();
        HashMap<String, List<Integer>> tempIndex = new HashMap<>();
        HashSet<Integer> list = new HashSet<>();

        for (String word : splitWord) {
            int startFrom = 0;
            while (true) {
                int index = originalDoc.indexOf(word, startFrom);// get the occurrence of index of each word
                char startChar = '.';
                char endChar = '.';

                if (index - 1 >= 0)
                    startChar = originalDoc.charAt(index - 1);
                if ((index + word.length()) < lengthOfDoc) {
                    endChar = originalDoc.charAt(word.length() + index);
                }

                boolean beforeWord = Character.toString(startChar).matches(".*[a-zA-Z]+.*");
                boolean afterWord = Character.toString(endChar).matches(".*[a-zA-Z]+.*");

                if (index >= 0) {
                    if (!beforeWord && !afterWord)
                        list.add(index);
                    startFrom = index + word.length();
                } else
                    break;
            }

            String lowerWord = word.toLowerCase();
            stemmer.setCurrent(lowerWord);
            stemmer.stem();

            if (list.isEmpty())
                list.add(-2); //indices out of  body

            tempIndex.put(stemmer.getCurrent(), new ArrayList<>(list));
            list.clear();
        }
        tempIndex.keySet().remove("");
        indicesOfWord.put(fileName, tempIndex);
    }

    private static void createBodyFiles(org.jsoup.nodes.Document html, String fileName) {
        try {
            FileWriter myWriter = new FileWriter("bodyFiles//" + fileName);
            myWriter.write(html.title());
            myWriter.write("\n");
            myWriter.write(html.body().text());
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}