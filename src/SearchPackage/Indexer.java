package SearchPackage;

import java.util.*;
import java.io.*;

// mongo libraries
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;


import com.mongodb.client.result.UpdateResult;
import org.tartarus.snowball.ext.PorterStemmer;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import org.bson.conversions.Bson;
import org.json.simple.JSONObject;

import org.jsoup.Jsoup;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class Indexer extends ProcessString implements Runnable {
    private static String[] fileNamesList;
    private static String folderRootPath;
    private static HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> invertedIndex;
    private static org.jsoup.nodes.Document htmlPage;

    // HashMap<fileName,All words in the file after processing>
    // This map helps in phrase searching
    private static HashMap<String, List<String>> processedFiles;
    private static HashMap<String, Double> tagsHtml;
    private static HashMap<String, Double> scoreOfWords;

    public void startIndexing() throws InterruptedException {
        invertedIndex = new HashMap<>();
//        processedFiles = new HashMap<>();
        List<JSONObject> invertedIndexJSON;

        // read stop words
        try {
            readStopWords();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in reading stop words");
        }
        // creates a file object
        File file = new File("downloads");
        folderRootPath = "downloads//";
        // returns an array of all files
        fileNamesList = file.list();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(new Indexer());
            threads[i].setPriority(i + 1);
        }
        for (int i = 0; i < 5; i++) {
            System.out.println("here");
            threads[i].start();
        }
        for (int i = 0; i < 5; i++) {
            threads[i].join();
        }
        // 8- converted the inverted index into json format
        invertedIndexJSON = convertInvertedIndexToJSON(invertedIndex);
        // 9- Upload to database
        uploadToDB(invertedIndexJSON);
//        uploadProcessedFiles();

        System.out.println(invertedIndexJSON);
        printTableHtml(invertedIndex);
        System.out.println("Done");
    }

    // 30
    // 0*6 => 1*6 0
    // 1*6 => 2*6
    // 2*6 => 3*6
    @Override
    public void run() {
        int start = (Thread.currentThread().getPriority() - 1) * (int) Math.ceil(fileNamesList.length / 5.0);
        int end = (Thread.currentThread().getPriority()) * (int) Math.ceil(fileNamesList.length / 5.0);
        System.out.println(start + " " + end);
        System.out.println(Thread.currentThread().getPriority());
        // iterate over files
        for (int i = start; i < Math.min(end, fileNamesList.length); i++) {
            String fileName = fileNamesList[i];
            System.out.println("Thread " + Thread.currentThread().getPriority() + " processed file: " + fileName);
            // 1- parse html
            String noHTMLDoc = "";
            try {
                noHTMLDoc = parsingHTML(fileName, folderRootPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 2- split words
            List<String> words = splitWords(noHTMLDoc);
            // 3-convert to lowercase
            convertToLower(words);
            // 4- remove stop words
            removeStopWords(words);
            // 5- stemming
            List<String> stemmedWords = stemming(words);
            // 6- build processed words
//            buildProcessedFiles(fileName, stemmedWords);
            // 7- build inverted index
            buildInvertedIndex(stemmedWords, fileName, invertedIndex);

//            System.out.println(processedFiles);
            System.out.println(stemmedWords);
            System.out.println(invertedIndex);
            System.out.println("\n\n");
        }
    }

    private static void printTableHtml(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> invertedIndex) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("invertedIndex.html"));
            writer.write("<table>");
            writer.write("  <tr>" + "    <th>Word</th>\n" + "    <th>Document tf size</th>\n" + "  </tr>");

            for (String word : invertedIndex.keySet()) {
                writer.write("  <tr>");

                HashMap<String, Pair<Integer, Integer, Double, Integer>> docs = invertedIndex.get(word);
                for (String doc : docs.keySet()) {
                    writer.write("  <td>");
                    writer.write(word);

                    writer.write("  </td>");
                    writer.write("  <td>");

                    Pair<Integer, Integer, Double, Integer> tf_size = docs.get(doc);
                    writer.write("<strong>Doc Name</strong>: " + doc + " | <strong>TF</strong>: " + tf_size.TF + " | <strong>Size</strong>: " + tf_size.size);
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

    private static String parsingHTML(String input, String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path + input));
        String lines = "";
        StringBuilder Str = new StringBuilder();
        while ((lines = reader.readLine()) != null) {
            Str.append(lines);
        }
        reader.close();
        lines = Str.toString();
        org.jsoup.nodes.Document html = Jsoup.parse(lines);
        lines = html.text();
        return lines;
    }

    private static synchronized void buildInvertedIndex(List<String> stemmedWords, String docName, HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> invertedIndex) {
        for (int i = 0; i < stemmedWords.size(); i++) {
            String word = stemmedWords.get(i);
            // if word not exist then allocate a map for it
            if (!invertedIndex.containsKey(word)) {
                HashMap<String, Pair<Integer, Integer, Double, Integer>> docsMapOfWord = new HashMap<String, Pair<Integer, Integer, Double, Integer>>();
                invertedIndex.put(word, docsMapOfWord);
            }
            HashMap<String, Pair<Integer, Integer, Double, Integer>> docsMapOfWord = invertedIndex.get(word);

            // if document not exist then allocate a pair for it
            if (!docsMapOfWord.containsKey(docName)) {
                Pair<Integer, Integer, Double, Integer> TF_Size_pair = new Pair<Integer, Integer, Double, Integer>(0, stemmedWords.size(), 1.5);
                docsMapOfWord.put(docName, TF_Size_pair);
                TF_Size_pair.index = new ArrayList<>();
            }
            Pair<Integer, Integer, Double, Integer> TF_Size_pair = docsMapOfWord.get(docName);
            TF_Size_pair.TF++;
            TF_Size_pair.index.add(i);
        }
    }

    // TODO: insert the file and its processed words
    private static synchronized void buildProcessedFiles(String FileName, final List<String> stemmedWords) {
        processedFiles.put(FileName, stemmedWords);
    }

    private static List<JSONObject> convertInvertedIndexToJSON(HashMap<String, HashMap<String, Pair<Integer, Integer, Double,Integer>>> invertedIndexP) {
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
                documents.add(documentJSON);
            }
            wordJSON.put("documents", documents);
//            System.out.println(wordJSON);
            listOfWordJSONS.add(wordJSON);
        }
        return listOfWordJSONS;
    }

    private static void uploadToDB(List<JSONObject> invertedIndexJSONParameter) {
        MongoClient client = MongoClients.create("mongodb+srv://mongo:Bq43gQp#mBQ-6%40S@cluster0.emwvc.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoDatabase database = client.getDatabase("myFirstDatabase");
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

    private static void uploadProcessedFiles() {
        MongoClient client = MongoClients.create("mongodb+srv://mongo:Bq43gQp#mBQ-6%40S@cluster0.emwvc.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoDatabase database = client.getDatabase("myFirstDatabase");
        MongoCollection<Document> collection = database.getCollection("processesFiles");
        for (String fileName : processedFiles.keySet()) {
            Document found = (Document) collection.find(new Document("fileName", fileName.replace('.', '_'))).first();
            Document doc = new Document("fileName", fileName.replace('.', '_')).append("processedFile", processedFiles.get(fileName));
            if (found != null) {
                Bson query = eq("fileName", fileName.replace('.', '_')); //filtration
                collection.replaceOne(query, doc);
            } else
                collection.insertOne(doc);
        }
    }

    private static void fillScoresOfTags() {
        // score of each tag
        //    title = 1
        //    h1 = 0.7
        //    h2 = 0.6
        //    h3 = 0.5
        //    h4 = 0.4
        //    h5 = 0.3
        //    h6 = 0.2
        //    else = 0.1
        tagsHtml.put("title", 0.9);
        Double j = 0.6;
        for (int i = 1; i <= 6; i++) {
            tagsHtml.put("h" + i, j);
            j -= 0.1;
        }
        tagsHtml.put("else", 0.1);
    }

    private static void filterTags(String file, String path) throws IOException {
        fillScoresOfTags();
        String lines = parsingHTML(file, path);
        Pattern pattern = Pattern.compile("\\w+");
        Matcher matcher = pattern.matcher("");
        //filtration most important tags
        for (String line : tagsHtml.keySet()) {
            if (htmlPage.select(line).html() != "") {
                String temp = htmlPage.select(line).html().toString();
                matcher = pattern.matcher(temp);
                while (matcher.find()) {
                    if (!scoreOfWords.containsKey(matcher.group()))
                        scoreOfWords.put(matcher.group(), tagsHtml.get(line));
                    else
                        scoreOfWords.put(matcher.group(), scoreOfWords.get(matcher.group()) + tagsHtml.get(line));
                }
            }
        }
        //rest of document
        lines = htmlPage.text();
        matcher = pattern.matcher(lines);
        while (matcher.find()) {
            if (!scoreOfWords.containsKey(matcher.group()))
                scoreOfWords.put(matcher.group(), tagsHtml.get("else")); //create new one
            else
                scoreOfWords.put(matcher.group(), scoreOfWords.get(matcher.group()) + tagsHtml.get("else"));//increment previous score

        }
    }
}