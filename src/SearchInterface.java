import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SearchInterface extends HttpServlet {
    private MongoDB database;
    private Search s = new Search();
    private HashMap<String, Pair3<Double, String, String, String>> finalResults;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String query = req.getParameter("query");

        //send the query to other class and get the result here
        //add the query to database
        database = new MongoDB();
        database.ConnectWithQuery();
        database.addQuery(query);

        //get the suggestions from the database
        List<String> Suggestions = database.getSuggestions();

        resp.setContentType("text/html");
        StringBuilder page = new StringBuilder();

        //build the main structure of the page
        page.append("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"css/all.min.css\">\n" +
                "    <link rel=\"stylesheet\" href=\"css/main.css\">\n" +
                "    <title>" + query +  " - Dawrha Search</title>\n" +
                "  </head>\n" +
                "<body>\n" +
                "  <div class=\"suggestions\" style=\"display: none;\">");

        //add the suggestions
        for (int i = 0; i < Suggestions.size(); i++) {
            page.append("<span>" + Suggestions.get(i) + "</span>");
        }

        //add the search bar
        page.append("</div>\n" +
                "  <div class=\"header-search\">\n" +
                "    <h2 style=\"letter-spacing: 4px;\">Dawrha</h2>" +
                "    <form action=\"SearchInterface\" method=\"GET\" class=\"container w-75\" autocomplete=\"off\">\n" +
                "      <div class=\"autocomplete\">\n" +
                "        <input value=\"" + query + "\" name=\"query\" id=\"myInput\" autocomplete=\"off\" type=\"text\" placeholder=\"Search\">\n" +
                "        <button type=\"submit\"><i class=\"fa-solid fa-magnifying-glass\"></i></button>\n" +
                "      </div>\n" +
                "    </form>\n" +
                "  </div>");


        //pages
        finalResults = s.searchQuery(query);
        Pair3<Double, String, String, String> Result = null;


        page.append("<div class=\"search-results container mt-5\">");

        for (String link : finalResults.keySet()) {
            Result = finalResults.get(link);

            if (!Result.getTitle().equals("-1")) {
                page.append("    <div class=\"card hidden mb-3\">\n" +
                        "      <div class=\"card-body\">\n" +
                        "        <a class=\"card-title page-title\" href=\"" + link + "\" target=\"_blank\">" + Result.getTitle() + "</a>\n" +
                        "        <a href=\"" + link  + "\" class=\"card-link link\" target=\"_blank\">" + link +"</a>\n" +
                        "        <p class=\"card-text\">");

                String[] para = Result.getParagraph().split(" ");
                query.toLowerCase();
                List<String> queryList = Arrays.asList(query.split(" "));

                for (int i = 0; i < para.length; i++) {
                    if (queryList.contains(para[i].toLowerCase())) {
                        page.append(" <strong>" + para[i] + "</strong>");
                    } else page.append(" " + para[i]);
                }
                page.append("</p>\n" +
                        "      </div>\n" +
                        "    </div>");
            }
        }
        page.append("  </div>");


        //pages number
        page.append("<div class=\"list container m-auto mt-5 mb-5\">\n" +
                "    <div class=\"numbers\">\n" +
                "\n" +
                "      <span class=\"r-char me-3 previous-btn hidden\">\n" +
                "        <b style=\"text-align: center;\"><</b>\n" +
                "        <a>Previous</a>\n" +
                "      </span>\n" +
                "      <span class=\"dawarha-numbers\">Daw</span>\n" +
                "\n" +
                "      <span class=\"pages-numbers\">\n" +
                "        <span class=\"r-char\">\n" +
                "          r\n" +
                "          <a class=\"selected\">1</a>\n" +
                "        </span>\n" +
                "      </span>\n" +
                "\n" +
                "      <span class=\"dawarha-numbers\">ha</span>\n" +
                "      <span class=\"r-char ms-3 next-btn\">\n" +
                "        <b style=\"text-align: center;\">></b>\n" +
                "        <a>Next</a>\n" +
                "      </span>\n" +
                "    </div>\n" +
                "\n" +
                "  </div>\n" +
                "\n" +
                "  <script src=\"js/popper.min.js\"></script>\n" +
                "  <script src=\"js/bootstrap.min.js\"></script>\n" +
                "  <script src=\"js/main.js\"></script>\n" +
                "</body>\n" +
                "</html>");

        resp.getWriter().println(page);
    }

}
