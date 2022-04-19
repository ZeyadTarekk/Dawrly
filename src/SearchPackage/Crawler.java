package SearchPackage;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

public class Crawler {
    //will be edited later to 5000
    private static final int MAX_PAGES_TO_SEARCH = 3;
    private int NofVisitedPages;
    private URL url;
    private Connection connection;
    private Document htmlDocument;
    //used to save the special word of any page visited before
    private Set<String> pagesVisited = new HashSet<String>();
    //links of pages that will be visited next
    private List<String> pagesToVisit = new LinkedList<String>();
    private MongoDB database;
    private RobotCheck robotObject = new RobotCheck();

    //methods
    public Crawler() {
        database = new MongoDB();
        database.ConnectToDataBase();
        this.NofVisitedPages = 0;
    }

    public void crawl() {

        if (database.CheckState().equals("Interrupted")) {
            database.GetSavedLinks(pagesToVisit, pagesVisited);
            NofVisitedPages = (int) database.GetNofVisitedPages();
        } else {
            pagesToVisit = GetLinksFromSeedFile();
        }

        database.ChangeState("Interrupted");
        while (NofVisitedPages < MAX_PAGES_TO_SEARCH) {

            //get the first link of the array
            String pageUrl = pagesToVisit.remove(0);

            //check the robot file and remove the forbidden links
            database.UpdatePagesToVisit(pageUrl);
            if (!robotObject.robotAllowed(pageUrl))
                continue;
            //connect to the page
            try {
                connection = Jsoup.connect(pageUrl);
                htmlDocument = connection.get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //collect the special word or normalize the url
            String SpecialWord = CollectSpecialWord();
            if (!pagesVisited.contains(SpecialWord)) {
                pagesVisited.add(SpecialWord);
                database.UpdatePagesVisited(SpecialWord);

                DownloadHTML();
                List<String> Links = getLinks();


                pagesToVisit.addAll(Links);
                database.UpdatePagesToVisit(Links);
            }

            NofVisitedPages++;
            database.UpdateNofVisitedPages(NofVisitedPages);
        }

        //join the threads here
        database.ChangeState("Finished");
    }

    public List<String> GetLinksFromSeedFile() {
        List<String> Links = new ArrayList<>();
        try {
            File myObj = new File("src\\SearchPackage\\Seeds.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Links.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Links;
    }

    public List<String> getLinks() {
        List<String> links = new LinkedList<String>();
        for (Element link : htmlDocument.select("a[href]")) {
            links.add(link.absUrl("href"));
        }
        return links;
    }

    //may need to send the document when we implement the class using threads
    public void DownloadHTML() {
        final String path = "downloads\\";
        String name = htmlDocument.title().trim().replaceAll(" ", "");
        if (name.length() > 10) {
            name = name.substring(0, 10);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + name + ".html"));
            writer.write(htmlDocument.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String CollectSpecialWord() {
        StringBuilder Collector = new StringBuilder();

        //collect the title of the page
        String title = htmlDocument.title().replaceAll(" ", "");
        Collector.append(title);

        //collect the first char of some words in the body
        String body = htmlDocument.body().text();
        String[] bodyWords = body.split(" ");
        for (int i = 0; i < bodyWords.length; i += 10)
            if (!bodyWords[i].isEmpty())
                Collector.append(bodyWords[i].charAt(0));

        return Collector.toString();
    }

    public void Testing() {

        crawl();

    }

    public static void main(String[] arg) {

        Crawler c = new Crawler();

        c.Testing();

    }
}
