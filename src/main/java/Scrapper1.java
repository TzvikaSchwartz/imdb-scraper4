import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Scrapper1 {
    private final File queriesResultsDir;
    private final static ArrayList<String> types = new ArrayList<>(Arrays.asList("movie", "tv", "tv episode"));
    private final static ArrayList<String> searchTypes = new ArrayList<>(Arrays.asList("&ttype=ft", "&ttype=ft", "&ttype=ft"));

    public Scrapper1(String dirName) throws IOException{
        queriesResultsDir = new File(dirName);
        if (!queriesResultsDir.isDirectory() && !queriesResultsDir.mkdirs()) {
            throw new IOException("Failed to create directory");
        }
    }

    public Scrapper1() throws IOException {
        this("queries results");
    }

    public void addQuery(String inputTitle, String category) throws IOException{

        String searchType = "";
        String type = "";
        
        if (types.contains(category.toLowerCase())) {
            type = " (" + category.toLowerCase() + ")";
            searchType = searchTypes.get(types.indexOf(category.toLowerCase()));
        }
        else if (!category.equalsIgnoreCase(""))
            ;//exception

        runImdbQueryAndWriteToFile(inputTitle, type, searchType);

    }

    public void addQuery(String title) throws  IOException{
        addQuery(title, "");
    }

    /**
     *
     * @param script the script (in javascript) to extract the genres of movie
     * @return all the genres
     */
    private String genres(String script) {
        //extract the first index of all data about genres
        int i = script.indexOf("\"genre\":");
        if (i == -1)//no genres for that movie
            return "";
        //extract the last index of all data about genres
        int j = script.indexOf(']',i);
        //remove the " in string
        return script.substring(i+"\"genre\":".length()+1,j).replaceAll("\"","");
    }

    /**
     *
     * @param script the script (in javascript) to extract the stars of movie
     * @return the stars
     */
    private String stars(String script) {
        int i = script.indexOf("\"actor\":");
        if (i == -1)
            return "";
        int j = script.indexOf(']',i);
        String starsList = script.substring(i+"\"actor\":".length()+1,j);
        StringBuilder stars = new StringBuilder();
        i = starsList.indexOf("\"name\":\"");
        //extract stars one by one
        while(i != -1) {
            j = starsList.indexOf('\"', i+"\"name\":\"".length()+1);
            stars.append(starsList, i + "\"name\":".length() + 1, j);
            i = starsList.indexOf("\"name\":\"", i+1);
            if (i != -1)
                stars.append(",");
        }
        return stars.toString();
    }

    /**
     *
     * @param script the script (in javascript) to extract the directors of movie
     * @return the directors
     */
    private String directors(String script) {
        int i = script.indexOf("\"director\":");
        if (i == -1)
            return "";
        int j = script.indexOf(']',i);
        String directorsList = script.substring(i+"\"director\":".length()+1,j);
        StringBuilder directors = new StringBuilder();
        i = directorsList.indexOf("\"name\":\"");
        while(i != -1) {
            j = directorsList.indexOf('\"', i+"\"name\":\"".length()+1);
            directors.append(directorsList, i + "\"name\":".length() + 1, j);
            i = directorsList.indexOf("\"name\":\"", i+1);
            if (i != -1)
                directors.append(",");
        }
        return directors.toString();
    }

    /**
     *
     * @param script the script (in javascript) to extract the MPAA rating of movie
     * @return MPAA rating
     */
    private String MPAA_rating(String script) {
        int i = script.indexOf("\"contentRating\":");
        if (i == -1)
            return "";
        int j = script.indexOf('\"',i+"\"contentRating\":\"".length()+1);
        return script.substring(i+"\"contentRating\":".length()+1,j);
    }

    /**
     *
     * @param script the script (in javascript) to extract the duration of movie
     * @return the duration
     */
    private String duration(String script) {
        int i = script.indexOf("\"duration\":");
        if (i == -1)
            return "";
        int j = script.indexOf('\"',i+"\"duration\":\"".length()+1);

        //replace some chars according to assignment
        return script.substring(i+"\"duration\":".length()+1,j)
                .replace("PT","")
                .replace("H", "h ")
                .replace("M", "min");
    }

    private void writeToFileFromScript(FileWriter fileWriter, String title, String script) throws IOException {
        fileWriter.write(title+"|");

        fileWriter.write(genres(script) + "|");

        String MPAA_rating = MPAA_rating(script);
        if (!MPAA_rating.equals("Not Rated") && !MPAA_rating.equals("Unrated"))
            fileWriter.write(MPAA_rating);
        fileWriter.write("|");

        fileWriter.write(duration(script) + "|");

        fileWriter.write(directors(script) + "|");

        fileWriter.write(stars(script));

        fileWriter.write("\n");
    }

    private void runImdbQueryAndWriteToFile(String inputTitle, String type, String searchType) throws IOException{
        final File fileResult = new File(queriesResultsDir, inputTitle + type + ".txt");

        if (!fileResult.exists()) {
            final FileWriter writeToFile = new FileWriter(fileResult);
            final Document document = Jsoup.connect("https://www.imdb.com/find?s=tt"
                    + searchType + "&q=" + inputTitle + "&ref_=nv_sr_sm").get();

            for (Element row : document.select("table.findList tr")) {
                final String title = row.select(".result_text a").text();
                final String innerTitle = row.select(".result_text").text();

                //if the movie not in development and the title match without spaces
                if (!innerTitle.toLowerCase().contains("in development") &&
                        title.toLowerCase().contains(inputTitle.toLowerCase())) {

                    final String url = "https://www.imdb.com/"+ row.select(".result_text a")
                            .attr("href");

                    //
                    final Document document1 = Jsoup.connect(url).get();

                    //find the scripts (in javascript) to extract data about the movie
                    Elements scripts = document1.select("script[type=application/ld+json]");

                    writeToFileFromScript(writeToFile, title, scripts.first().data());

                }
            }
            writeToFile.close();
        }

    }

}
