package SearchPackage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.util.List;
import java.io.FileWriter;


public class RobotCheck {
    ArrayList<String> DisallowedLinks = new ArrayList<String>();

    private void getPageLinks(String newURL) {
        boolean userAgentStatus = false;
        URL url = null;
        String protocol = null;
        String serverName = null;
        try {
            url = new URL(newURL);
            protocol = url.getProtocol();
            serverName = url.getHost();
            url = new URL(protocol + "://" + serverName + "/robots.txt");
        } catch (IOException e) {
            // print exception messages
            System.err.println("Error happened while trying to open '" + newURL + "': " + e.getMessage());
        }
        StringBuilder robotCommands = new StringBuilder();
        if (url != null) {
            try {
                String readLine;

                DataInputStream theBody = new DataInputStream(url.openStream());
                while ((readLine = theBody.readLine()) != null) {
                    robotCommands.append(readLine).append("\n");
                }
//                System.out.println(Arrays.toString(robotCommands.toString().split("\n")));
            } catch (IOException e) {
                System.err.println("Error happened while trying to read the content of  '" + newURL + "': " + e.getMessage());
            }
        }

        String[] arrOfWords = robotCommands.toString().split("\n");
        for (String arrOfWord : arrOfWords) {
            String word = arrOfWord.trim();
            if (word.startsWith("User-agent:")) {
                int indexOfChar = word.indexOf(':') + 2;
                userAgentStatus = word.charAt(indexOfChar) == '*';
            } else if (word.startsWith("Disallow:") && userAgentStatus) {
                String disallowedDirectories = word.substring(10).trim();
                String disallowedUrl = protocol + "://" + serverName + disallowedDirectories;
                DisallowedLinks.add(disallowedUrl);
            }
        }


    }

    public void generateDisallowedLinks(List<String> pagesToVisit) {
        for (String s : pagesToVisit) {
            getPageLinks(s);
        }
    }

    public void testing(List<String> pagesToVisit) {
        generateDisallowedLinks(pagesToVisit);
        printDisallowedURLs();
        writeDisallowedURLsToFile("testing");
    }

    public void printDisallowedURLs() {
        for (String disallowedLink : DisallowedLinks) {
            System.out.println(disallowedLink);
        }
    }

    public void writeDisallowedURLsToFile(String fileName) {
        try {
            FileWriter myObject = new FileWriter(fileName + ".txt");
            for (String disallowedLink : DisallowedLinks) {
                myObject.write(disallowedLink);
                myObject.write("\n");
            }
            myObject.close();
        } catch (IOException e) {
            System.out.println("An error occurred! File can't be created.");
        }

    }

    public static void main(String[] args) {
        RobotCheck obj = new RobotCheck();
        List<String> links = new ArrayList<String>();
        links.add("https://www.google.com/");
        links.add("https://github.com/");
        links.add("https://www.geeksforgeeks.org/");
        links.add("https://www.programiz.com/");
        links.add("https://www.javatpoint.com/");

        obj.testing(links);
    }

}
