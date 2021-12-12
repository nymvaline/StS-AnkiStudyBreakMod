package ankiStudyBreak.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ankiStudyBreak.AnkiStudyBreak;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class AnkiConnectRequest {
    AnkiConnectRequest() {

    }

    public static int getCardsStudied() {
        String jsonResponse;
        try {
            URL url = new URL(AnkiStudyBreak.ankiConnectUrlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            String jsonInputString = "{\"action\": \"getNumCardsReviewedToday\", \"version\": 6}";
            OutputStream os = connection.getOutputStream();
            byte[] input = jsonInputString.getBytes("utf-8");
            AnkiStudyBreak.logger.info("Sending " + jsonInputString + " to " +url.toString());
            os.write(input, 0, input.length);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            jsonResponse = response.toString();
        } catch (MalformedURLException | ProtocolException e) {
            jsonResponse = "{result: -1000}";
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            jsonResponse = "{result: -1000}";
            e.printStackTrace();
        } catch (IOException e) {
            jsonResponse = "{result: -1000}";
            e.printStackTrace();
        }
        JsonObject jsonobj = new JsonParser().parse(jsonResponse).getAsJsonObject();
        return jsonobj.get("result").getAsInt();
    }
}
