import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;

/*
TODO: create an abstract class called ProcessString
      Make QueryProcessor & Indexer extends it
      ProcessString should contains all methods of string processing
*/
public class QueryProcessor extends ProcessString {
    // NOTE: Take care of quotes -> search as it is

    public static void main(String[] args) {
        List<String> phraseSearch = new ArrayList<>();
        // HOW TO USE QueryProcessor
        QueryProcessor qp = new QueryProcessor();
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> result = qp.processQuery("Mangaa Ingredients", phraseSearch);
        System.out.println(result);
        System.out.println(phraseSearch);
    }
    // TODO: Determine the output data structure of processQuery method

    // TODO: Provide an interface to receive the query string
    // TODO: Provide an interface to pass the words to the RANKER
    HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> processQuery(String query, List<String> phraseSearch) {
        // TODO: Read stop words
        try {
            readStopWords();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in reading stop words");
        }
        // TODO: Extract Double Quotes from stemmedWords passing to phraseSearch
        extractDoubleQuotes(query, phraseSearch);

        // TODO: Split string into words
        List<String> words = splitWords(query);

        // TODO: Convert to lowercase
        convertToLower(words);

        // TODO: Remove stop words
        removeStopWords(words);

        // TODO: Stem words
        List<String> stemmedWords = stemming(words);

        // TODO: Get documents containing words from database
        List<Document> words_documents = getDocsFromDB(stemmedWords);
        // TODO: [OPTIONAL] convert JSON into HASHMAP
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> words_documents_map = convertJSONintoHashMap(words_documents);

        return words_documents_map;
    }

    // TODO: Implement a function to get data from database
    List<Document> getDocsFromDB(List<String> stemmedWords) {
        List<Document> result = new Vector<>();
//        MongoClient client = MongoClients.create("mongodb+srv://mongo:Bq43gQp#mBQ-6%40S@cluster0.emwvc.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        com.mongodb.MongoClient client = new com.mongodb.MongoClient();
        MongoDatabase database = client.getDatabase("SearchEngine");
        MongoCollection<Document> collection = database.getCollection("invertedIndex");
        for (String word : stemmedWords) {
            Document found = (Document) collection.find(new Document("word", word)).first();
            if (found != null)
                result.add(found);
        }
        return result;
    }

    // TODO: Implement a function that converts a JSON into hash
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> convertJSONintoHashMap(List<Document> words_documents) {
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> convertedHashMap = new HashMap<>();
        for (Document word_doc : words_documents) {
            HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>> documents = new HashMap<>();
            convertedHashMap.put((String) word_doc.get("word"), documents);
            ArrayList<Document> v = (ArrayList<Document>) word_doc.get("documents");
//            System.out.println(v);
            for (Document docJSON : v) {
                Pair<Integer, Integer, Double, Integer, Integer> tf_size = new Pair<>();
                tf_size.TF = (Integer) docJSON.get("tf");
                tf_size.size = (Integer) docJSON.get("size");
                tf_size.score = (Double) docJSON.get("score");
                tf_size.index = new ArrayList<>((ArrayList<Integer>) docJSON.get("index"));
                tf_size.actualIndices = new ArrayList<>((ArrayList<Integer>) docJSON.get("actualIndices"));
                documents.put((String) docJSON.get("document"), tf_size);
            }
        }
        return convertedHashMap;
    }


    private void extractDoubleQuotes(String query, List<String> phraseSearch) {
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(query);
        while (m.find()) {
            String []words = m.group(1).split(" ");
            phraseSearch.addAll(Arrays.asList(words));
        }
        convertToLower(phraseSearch);
        removeStopWords(phraseSearch);
        phraseSearch = new ArrayList<>(stemming(phraseSearch));
    }
}
