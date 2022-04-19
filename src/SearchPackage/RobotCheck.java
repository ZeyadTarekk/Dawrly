package SearchPackage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.List;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RobotCheck {

    HashMap<String, ArrayList<String>> allDisallowedLinks;
    ArrayList<String> disallowed;

    public RobotCheck() {
        allDisallowedLinks = new HashMap<>();
    }

    private void getPageDisallows(String newURL) {
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
            if (allDisallowedLinks.get(url.toString()) != null) {
                System.out.println(url.toString() + " Found");
                return;
            }
            System.out.println(url.toString() + " New");
            disallowed = new ArrayList<>();
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


            String[] arrOfWords = robotCommands.toString().split("\n");
            for (String arrOfWord : arrOfWords) {
                String word = arrOfWord.trim();
                if (word.startsWith("User-agent:")) {
                    int indexOfChar = word.indexOf(':') + 2;
                    userAgentStatus = word.charAt(indexOfChar) == '*';
                } else if (word.startsWith("Disallow:") && userAgentStatus) {
                    String disallowedDirectories = word.substring(10).trim();
                    String disallowedUrl = protocol + "://" + serverName + disallowedDirectories;
                    disallowed.add(disallowedUrl);
                }
            }

            allDisallowedLinks.put(url.toString(), disallowed);
        }
    }

    private void generateDisallowedLinks(List<String> pagesToVisit) {
        for (String s : pagesToVisit) {
            getPageDisallows(s);
        }
    }

    private void testing(List<String> pagesToVisit) {
        generateDisallowedLinks(pagesToVisit);
        printDisallowedURLs();
//        writeDisallowedURLsToFile("testing");
    }

    private void printDisallowedURLs() {
        for (String key : allDisallowedLinks.keySet()) {
            System.out.print(key);
            System.out.println(allDisallowedLinks.get(key));
        }
    }

    private ArrayList<String> getDissallowedList(String url) {
        URL targetURL = null;
        String protocol, serverName;
        try {
            targetURL = new URL(url);
            protocol = targetURL.getProtocol();
            serverName = targetURL.getHost();
            targetURL = new URL(protocol + "://" + serverName + "/robots.txt");
        } catch (IOException e) {
            System.err.println("Error happened while trying to open '" + url + "': " + e.getMessage());
        }
        if (targetURL != null)
            return allDisallowedLinks.get(targetURL.toString());
        return null;
    }

    private Boolean isAllowed(String url) {
        ArrayList<String> disallowed = getDissallowedList(url);
        String pattern;
        for (String i : disallowed) {
            pattern = i;
            if (pattern.contains("*")) {
                pattern = pattern.replace("*", "[a-zA-Z]+");
                Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(url);
                if (m.matches())
                    return false;
            } else {
                if (pattern.equals(url))
                    return false;
            }
        }
        return true;
    }

    public synchronized Boolean robotAllowed(String url) {
        getPageDisallows(url);
        return isAllowed(url);
    }

//    public void writeDisallowedURLsToFile(String fileName) {
//        try {
//            FileWriter myObject = new FileWriter(fileName + ".txt");
//            for (String disallowedLink : DisallowedLinks) {
//                myObject.write(disallowedLink);
//                myObject.write("\n");
//            }
//            myObject.close();
//        } catch (IOException e) {
//            System.out.println("An error occurred! File can't be created.");
//        }
//
//    }

    public static void main(String[] args) {
        RobotCheck obj = new RobotCheck();
        List<String> links = new ArrayList<String>();
        links.add("https://www.google.com/");
        links.add("https://www.google.com/");
        links.add("https://github.com/");
        links.add("https://www.geeksforgeeks.org/");
        links.add("https://www.programiz.com/");
        links.add("https://www.javatpoint.com/");

        obj.testing(links);
        System.out.println(obj.getDissallowedList("https://www.google.com/"));
    }

}