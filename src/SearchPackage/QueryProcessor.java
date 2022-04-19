package SearchPackage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import static com.mongodb.client.model.Filters.eq;

/*
TODO: create an abstract class called ProcessString
      Make QueryProcessor & Indexer extends it
      ProcessString should contains all methods of string processing
*/
public class QueryProcessor extends ProcessString {
    // NOTE: Take care of quotes -> search as it is
    List<String> stopWords;


    public static void main(String[] args) {
        QueryProcessor qp = new QueryProcessor();
        List<Document> result = qp.processQuery("Mangaa Ingredients");
        System.out.println(result);
    }
    // TODO: Determine the output data structure of processQuery method

    // TODO: Provide an interface to receive the query string
    // TODO: Provide an interface to pass the words to the RANKER
    List<Document> processQuery(String query) {

        // TODO: Read stop words
        try {
            readStopWords();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error in reading stop words");
        }

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

        return words_documents;
    }

    // TODO: Implement a function to get data from database
    List<Document> getDocsFromDB(List<String> stemmedWords) {
        List<Document> result = new Vector<>();
        MongoClient client = MongoClients.create("mongodb+srv://mongo:Bq43gQp#mBQ-6%40S@cluster0.emwvc.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoDatabase database = client.getDatabase("myFirstDatabase");
        MongoCollection<Document> collection = database.getCollection("invertedIndex");
        for (String word : stemmedWords) {
            Document found = (Document) collection.find(new Document("word", word)).first();
            result.add(found);
        }
        return result;
    }
}
