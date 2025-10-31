import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ParaphraserClient {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String url = getenv("PARA_URL",
        "https://api.apyhub.com/sharpapi/api/v1/content/paraphrase");

    public ParaphraserClient(String apiKey) {
        if (apiKey != null && !apiKey.isBlank()) {
            System.setProperty("PARA_API_KEY", apiKey);
        }
    }

    public String tryParaphrase(String text) {
        if (text == null || text.isBlank()) return text;
        try {
            HttpRequest req = buildRequest(buildPayload(text));
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            int sc = res.statusCode();
            if (sc == 200 || sc == 202) {
                return extractText(res.body());
            }
        } catch (Exception ignored) {
        }
        return text;
    }

    private HttpRequest buildRequest(String payload) {
        String mode = getenv("PARA_AUTH_MODE", "basic").toLowerCase();
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json");

        if ("basic".equals(mode)) {
            String u = getenv("PARA_USER", "");
            String s = getenv("PARA_SECRET", "");
            String basic = Base64.getEncoder()
                .encodeToString((u + ":" + s).getBytes(StandardCharsets.UTF_8));
            b.header("Authorization", "Basic " + basic);
        } else {
            String key = getenv("PARA_API_KEY", "");
            b.header("apy-token", key);
        }

        return b.POST(HttpRequest.BodyPublishers.ofString(payload)).build();
    }

    private String buildPayload(String text) {
        String lang = getenv("PARA_LANG", "English");
        String tone = getenv("PARA_TONE", "neutral");
        String ctx  = getenv("PARA_CONTEXT", "");
        String max  = getenv("PARA_MAXLEN", "");
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"content\":").append(json(text));
        if (!isBlank(lang)) sb.append(",\"language\":").append(json(lang));
        if (!isBlank(tone)) sb.append(",\"voice_tone\":").append(json(tone));
        if (!isBlank(max))  sb.append(",\"max_length\":").append(max);
        if (!isBlank(ctx))  sb.append(",\"context\":").append(json(ctx));
        sb.append("}");
        return sb.toString();
    }

    private static String extractText(String body) {
        int i = body.indexOf("\"data\"");
        if (i >= 0) {
            int colon = body.indexOf(':', i);
            if (colon > 0) {
                int q1 = body.indexOf('"', colon + 1);
                int q2 = body.indexOf('"', q1 + 1);
                if (q1 > 0 && q2 > q1) return body.substring(q1 + 1, q2);
            }
        }
        return body;
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String json(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    public static void main(String[] args) {
        ParaphraserClient p = new ParaphraserClient(System.getenv("PARA_API_KEY"));
        String out = p.tryParaphrase("Your book is due today. Please return it to avoid fines.");
        System.out.println(out);
    }
}