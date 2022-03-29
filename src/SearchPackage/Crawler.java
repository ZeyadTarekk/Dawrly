package SearchPackage;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/*
Important note:

need to save the current state of the crawl
[
  the list of links
  the visited list
  the current number of visited pages
]

before doing anything we need to check the current state
  if never stopped --> start from the beginning
  if stopped before --> load the lists then continue
*/

public class Crawler {
    //will be edited later to 5000
    private static final int MAX_PAGES_TO_SEARCH = 1;
    private URL url;
    private Connection connection;
    private Document htmlDocument;
    //used to save the special word of any page visited before
  /*
    the special word will contain :
    1-
    2-
    3-
    etc
  */
    private Set<String> pagesVisited = new HashSet<String>();

    //links of pages that will be visited next
    private List<String> pagesToVisit = new LinkedList<String>();



    //methods
    public void crawl() {
        pagesToVisit.add("https://www.w3schools.com/");
        pagesToVisit.add("https://www.w3schools.com/videos/index.php");
        pagesToVisit.add("https://www.w3schools.com/videos/index.php?fds=4154&gh=");

        pagesToVisit.add("https://www.tabnine.com/code/java/methods/java.net.URI/normalize");

        //get the first link of the array
        String pageUrl = pagesToVisit.remove(2);

        try {
            url = new URL(pageUrl);
            System.out.println(url.getFile());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //connect to the page
        try {

            connection = Jsoup.connect(pageUrl);
            htmlDocument = connection.get();



        } catch (IOException e) {
            e.printStackTrace();
        }


        //collect the special word or normalize the url

        //check if that word used before in the pagesVisited

        //if not in the set --> get the html document and download it

        //get the links from the document and add them to the pagesToVisit


//    while (this.pagesVisited.size() < MAX_PAGES_TO_SEARCH) {}


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
        final String path = "src\\SearchPackage\\downloads\\";
        final String name = htmlDocument.title().trim().replaceAll(" ", "");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + name + ".html"));
            writer.write(htmlDocument.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Testing() {

        crawl();

    }

    public static void main(String[] arg) {

        Crawler c = new Crawler();

        c.Testing();


    }
}
