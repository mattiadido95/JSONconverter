
import java.awt.image.Kernel;
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Converter {
    private static final String USER_AGENT = "Mozilla/5.0";

    public static void Convert() {
        JSONArray arrayBook = new JSONArray();
        try {
            List<String> allLines = Files.readAllLines(Paths.get("../Materiale/amazon-meta.txt"));
            for (String line : allLines) {
                if (line.indexOf("Id:") != -1) {
                    JSONObject book = new JSONObject();
                    System.out.println(line);
                    int index = allLines.indexOf(line);
                    if (line.equals("Id:   100")) {
                        break;
                    }

                    // BAD OBJECT
                    String exitString = allLines.get(index + 2);
                    if (exitString.equals("  discontinued product")) {
                        System.out.println("stringa di errore" + " " + exitString);
                        continue;
                    }

                    // GROUP
                    String group = allLines.get(index + 3);
                    String[] groupSplit = group.split("group: ");
                    if (groupSplit[1].equals("Book")) {
                        // book.put("group", groupSplit[1]);
                    } else {
                        // NON SONO UN LIBRO QUINDI SKIPPO L'OGGETTO
                        System.out.println("trovato un non libro");
                        continue;
                    }

                    // ASIN
                    String asin = allLines.get(index + 1);
                    String[] asinSplit = asin.split("ASIN: ");
                    book.put("ASIN", asinSplit[1]);

                    // ISBN
                    book.put("ISBN", "");

                    // LANGUAGE CODE
                    book.put("language_code", "");

                    // AVAERAGE RATING
                    book.put("average_rating", "");

                    // NUM PAGES
                    book.put("num_pages", "");

                    // PUB YEAR
                    book.put("publication_year","");

                    // PUB MONTH
                    book.put("publication_month","");

                    // PUB DAY
                    book.put("publication_day","");

                    // RATING COUNT
                    book.put("rating_count","");

                    // IMAGE URL
                    book.put("image_url","");

                    // DESCRIPTION
                    book.put("description","");

//                    // AUTHOR
//                    String author = Scrape(asinSplit[1]);
//                    book.put("author", author);

                    // TITLE
                    String title = allLines.get(index + 2);
                    String[] titleSplit = title.split("title: ");
                    book.put("title", titleSplit[1]);

                    // CATEGORIES
                    String categories = allLines.get(index + 6);
                    String[] categoriesSplit = categories.split("categories: ");
                    int cat = Integer.parseInt(categoriesSplit[1]);
//                    book.put("count_categories", cat);
                    JSONArray categoriesList = new JSONArray();
                    if (cat > 0) {
                        //AGGIUNGO TUTTE LE CATEGORIE IN UN ARRAY
                        for (int i = 1; i <= cat; i++) {
                            String catelem = allLines.get(index + 6 + i);
                            if (catelem.contains("Children")) {
                                categoriesList.add("children");
                            }
                            if (catelem.contains("Comics")) {
                                categoriesList.add("comics");
                            }
                            if (catelem.contains("Fantasy")) {
                                categoriesList.add("fantasy");
                            }
                            if (catelem.contains("History")) {
                                categoriesList.add("history");
                            }
                            if (catelem.contains("Mystery")) {
                                categoriesList.add("mystery");
                            }
                            if (catelem.contains("Poetry")) {
                                categoriesList.add("poetry");
                            }
                            if (catelem.contains("Romance")) {
                                categoriesList.add("romance");
                            }
                            if (catelem.contains("Young")) {
                                categoriesList.add("young");
                            }
                            if (catelem.contains("Adult")) {
                                categoriesList.add("adult");
                            }
                            if (catelem.contains("Graphic")) {
                                categoriesList.add("graphic");
                            }
                            if (catelem.contains("Paranormal")) {
                                categoriesList.add("paranormal");
                            }
                            if (catelem.contains("Biography")) {
                                categoriesList.add("biography");
                            }
                            if (catelem.contains("Thriller")) {
                                categoriesList.add("thriller");
                            }
                            if (catelem.contains("Crime")) {
                                categoriesList.add("crime");
                            }
                            if (categoriesList.isEmpty()) {
                                String catExtracted = StringUtils.substringBetween(catelem, "|Books[283155]|Subjects[1000]|", "[");
                                categoriesList.add(catExtracted);
                            }
                        }
                        book.put("genres", categoriesList);
                    }

                    // BOOK REVIEWS
                    String reviews = allLines.get(index + 6 + cat + 1);
                    String[] reviewsSplit = reviews.split("reviews: ");
                    String[] app = reviewsSplit[1].split(": ");
                    String[] countReview = app[1].split("  ");
                    int rev = Integer.parseInt(countReview[0]);
//                    book.put("count_reviews", rev);
                    JSONArray reviewsList = new JSONArray();
                    if (rev > 0) {
                        for (int i = 1; i <= rev; i++) {
                            String reviewExtracted = allLines.get(index + 6 + cat + i + 1);
                            JSONObject review = new JSONObject();

                            String data = StringUtils.substringBetween(reviewExtracted, "", "cutomer").replaceAll(" ", "");
                            String cutomer = StringUtils.substringBetween(reviewExtracted, "cutomer:", "rating").replaceAll(" ", "");
                            String rating = StringUtils.substringBetween(reviewExtracted, "rating:", "votes").replaceAll(" ", "");
                            String votes = StringUtils.substringBetween(reviewExtracted, "votes:", "helpful").replaceAll(" ", "");
//                            String helpfulSplit[] = reviewExtracted.split("helpful: ");

                            review.put("date_added", data);
                            review.put("date_updated", data);
                            review.put("review_id", cutomer + "_" + asin);
                            review.put("rating", rating);
                            review.put("n_votes", votes);
                            review.put("review_text", "");
                            review.put("user_id", "");
                            //review.put("helpful", helpfulSplit[1].replaceAll(" ",""));

                            reviewsList.add(review);
                        }
                        book.put("reviews", reviewsList);
                    } else {
                        book.put("reviews", reviewsList);
                    }

                    // ID
                    String[] parts = line.split(":   ");
                    book.put("book_id", parts[1]);

//                    // SIMILAR BOOK ARRAY AND COUNTER
//                    String similar = allLines.get(index + 5);
//                    String[] similarSplit = similar.split("similar: ");
//                    String[] similarSplitElems = similarSplit[1].split("  ");
//                    int similarcount = Integer.parseInt(similarSplitElems[0]);
//                    book.put("count_similar", similarcount);
//                    JSONArray similarList = new JSONArray();
//                    if (similarcount > 0) {
//                        for (int i = 1; i <= similarcount; i++) {
//                            similarList.add(similarSplitElems[i]);
//                        }
//                    }
//                    book.put("similar", similarList);


                    arrayBook.add(book);
                }
            }
            FileWriter file = new FileWriter("../Materiale/BookAmazon.json");
            file.write(arrayBook.toJSONString());
            file.close();
            System.out.println("elementi inseriti nel JSON: " + arrayBook.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String Scrape(String asin) throws IOException {
        URL obj = new URL("https://www.amazon.in/s?k=" + asin + "&ref=hp_aps_search");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Encoding", "gzip");
        int responseCode = con.getResponseCode();
        // System.out.println("GET Response Code :: " + con.getResponseCode() + "...." + con.getInputStream());
        Reader reader = null;
        if ("gzip".equals(con.getContentEncoding())) {
            reader = new InputStreamReader(new GZIPInputStream(con.getInputStream()));
        } else {
            reader = new InputStreamReader(con.getInputStream());
        }
        StringBuffer response = new StringBuffer();
        while (true) {
            int ch = reader.read();
            if (ch == -1) {
                break;
            }
            response.append((char) ch);
        }

        //System.out.println(response);

        String str = response.toString();
        String result = StringUtils.substringBetween(str, "<span class=\"a-size-base\">by </span>", "</div>");
        System.out.println("risultato primo parsing \n" + result);

        if (StringUtils.substringBetween(result, ">", "</a>") != null) {
            if (StringUtils.substringBetween(result, ">", "</a>").contains("<")) {
                String app[] = StringUtils.substringBetween(result, ">", "</a>").split("<");
                System.out.println(app[0]);
                return app[0];
            } else {
                System.out.println(StringUtils.substringBetween(result, ">", "</a>"));
                return StringUtils.substringBetween(result, ">", "</a>");
            }
        } else if (StringUtils.substringBetween(result, ">", "</span>") != null) {
            if (StringUtils.substringBetween(result, ">", "</span>").contains("<")) {
                String app[] = StringUtils.substringBetween(result, ">", "</span>").split("<");
                System.out.println(app[0]);
                return app[0];
            } else {
                System.out.println(StringUtils.substringBetween(result, ">", "</span>"));
                return StringUtils.substringBetween(result, ">", "</span>");
            }
        }

        return "";
    }

    public static void main(String[] args) throws IOException {
        Convert();
    }


}