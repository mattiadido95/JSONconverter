
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Converter {
    public static void main(String args[]) {
        //Creating a JSONObject object
        JSONObject jsonObject = new JSONObject();
        //Inserting key-value pairs into the json object
        jsonObject.put("ID", "1");
        jsonObject.put("First_Name", "Shikhar");
        jsonObject.put("Last_Name", "Dhawan");
        jsonObject.put("Date_Of_Birth", "1981-12-05");
        jsonObject.put("Place_Of_Birth", "Delhi");
        jsonObject.put("Country", "India");
        JSONObject jsonObject2 = new JSONObject();
        //Inserting key-value pairs into the json object
        jsonObject2.put("ID", "2");
        jsonObject2.put("First_Name", "Shikhar");
        jsonObject2.put("Last_Name", "Dhawan");
        jsonObject2.put("Date_Of_Birth", "1981-12-05");
        jsonObject2.put("Place_Of_Birth", "Delhi");
        jsonObject2.put("Country", "India");
        //put all in array
        JSONArray array = new JSONArray();
        array.add(jsonObject);
        array.add(jsonObject2);
        try {
            FileWriter file = new FileWriter("output.json");
            file.write(array.toJSONString());
            file.close();
            List<String> allLines = Files.readAllLines(Paths.get("input.txt"));
            for (String line : allLines) {
                System.out.println(line);
                String search = "total: ";
                if (line.toLowerCase().indexOf(search.toLowerCase()) != -1) {
                    System.out.println("I found the keyword");
                } else {
                    System.out.println("not found");
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("JSON file created: " + jsonObject);
    }
}