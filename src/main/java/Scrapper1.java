import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Scrapper1 {
    private final String title;
    private static final File dir = new File("queries");
    private static final String[] categories = new String[]{"ft", "tv", "ep"};

    public Scrapper1(String title, int category) {
        this.title = title;
        try {

            if (!dir.isDirectory() && !dir.mkdirs()){
                throw new IOException("Failed to create directory");
            }
            String ttype = category == 4? "": "&ttype=" + categories[category-1];
            String type = category == 4? "": (" (" + categories[category-1] + ")");
            final File file = new File(dir, title + type + ".txt");
            final FileWriter writeToFile = new FileWriter( file);
            final Document document = Jsoup.connect("https://www.imdb.com/find?s=tt"
                    + ttype + "&q=" + title + "&ref_=nv_sr_sm").get();
            //^from array of categories by category (-1 to index)
            int i = 0;
            for (Element row : document.select("table.findList tr")) {
                final String title2 = row.select(".result_text a").text();
                final String title3 = row.select(".result_text").text();

                //if the movie not in development and the title match without spaces
                if (title3.toLowerCase().indexOf("in development") == -1 &&
                        title2.toLowerCase().indexOf(title.toLowerCase()) != -1) {

                    final String url = "https://www.imdb.com/"+ row.select(".result_text a")
                            .attr("href");

                    final Document document1 = Jsoup.connect(url).get();

                    writeToFile.write(title2+"|");

                    //find the scripts (in javascript) to extract data about the movie
                    Elements scripts = document1.select("script[type=application/ld+json]");

                    writeToFile.write(genres(scripts.first().data()) + "|");

                    String MPAA_rating = MPAA_rating(scripts.first().data());
                    if (!MPAA_rating.equals("Not Rated") && !MPAA_rating.equals("Unrated"))
                        writeToFile.write(MPAA_rating);
                    writeToFile.write("|");

                    writeToFile.write(duration(scripts.first().data()) + "|");

                    writeToFile.write(directors(scripts.first().data()) + "|");

                    writeToFile.write(stars(scripts.first().data()));

                    writeToFile.write("\n");

                }
            }
            writeToFile.close();
        }
        catch (IOException e){
            System.out.println(e.getMessage());
        }
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
        String stars = "";
        i = starsList.indexOf("\"name\":\"");
        //extract stars one by one
        while(i != -1) {
            j = starsList.indexOf('\"', i+"\"name\":\"".length()+1);
            stars += starsList.substring(i+"\"name\":".length()+1,j);
            i = starsList.indexOf("\"name\":\"", i+1);
            if (i != -1)
                stars += ",";
        }
        return stars;
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
        String directors = "";
        i = directorsList.indexOf("\"name\":\"");
        while(i != -1) {
            j = directorsList.indexOf('\"', i+"\"name\":\"".length()+1);
            directors += directorsList.substring(i+"\"name\":".length()+1,j);
            i = directorsList.indexOf("\"name\":\"", i+1);
            if (i != -1)
                directors += ",";
        }
        return directors;
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
}
