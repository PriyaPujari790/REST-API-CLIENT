import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class SimpleWeather {
    private static final String API_URL =
        "https://api.weatherbit.io/v2.0/forecast/daily";
    private static final String API_KEY = "YOUR_API_KEY";

    public static void main(String[] args) throws Exception {
        String city = "London";
        String units = "M"; // "M" = Metric°C
        String fullUrl = API_URL + "?city=" + URLEncoder.encode(city, "UTF-8")
                         + "&units=" + units
                         + "&key=" + API_KEY;

        HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();
        if (status != 200) {
            System.err.println("HTTP error: " + status);
            return;
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "UTF-8")
        );
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();

        String json = sb.toString();
        printWeather(json);
    }

    private static void printWeather(String json) {
        // Very simple parsing—expects "data":[{...},...]
        String[] parts = json.split("\"data\":\\[", 2);
        if (parts.length < 2) {
            System.err.println("Unexpected response");
            return;
        }
        String afterData = parts[1];
        String[] entries = afterData.split("\\},\\{");
        System.out.println("Date       | Max°C | Min°C | Precip(mm)");
        System.out.println("-----------+-------+-------+-----------");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < Math.min(entries.length, 5); i++) {
            String e = entries[i];
            String date = extract(e, "\"valid_date\":\"", "\"");
            String max = extract(e, "\"max_temp\":", ",");
            String min = extract(e, "\"min_temp\":", ",");
            String prec = extract(e, "\"precip\":", ",");
            System.out.printf("%-10s | %5s | %5s | %9s%n",
                              date, max, min, prec);
        }
    }

    private static String extract(String src, String open, String close) {
        int i = src.indexOf(open);
        if (i < 0) return "?";
        i += open.length();
        int j = src.indexOf(close, i);
        return (j < 0) ? src.substring(i) : src.substring(i, j);
    }
}
