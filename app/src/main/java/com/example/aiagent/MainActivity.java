package com.example.aiagent;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private LinearLayout chatList;
    private ScrollView scrollView;
    private EditText input;
    private Button send;

    private String provider, apiKey, endpoint, model;
    private final JSONArray history = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setup();
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density + .5f);
    }

    private GradientDrawable bg(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        return d;
    }

    private void setup() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(10), dp(20), 0);

        String[] providers = {
                "OpenAI",
                "OpenRouter",
                "Groq",
                "DeepSeek",
                "Together AI",
                "xAI",
                "Gemini",
                "Claude",
                "Custom OpenAI-compatible"
        };

        Spinner sp = new Spinner(this);
        sp.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                providers
        ));

        EditText url = new EditText(this);
        url.setHint("API URL");

        EditText modelInput = new EditText(this);
        modelInput.setHint("Model name");

        EditText key = new EditText(this);
        key.setHint("API key");
        key.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        box.addView(sp);
        box.addView(url);
        box.addView(modelInput);
        box.addView(key);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String s = providers[pos];

                if (s.equals("OpenAI"))
                    url.setText("https://api.openai.com/v1/chat/completions");
                else if (s.equals("OpenRouter"))
                    url.setText("https://openrouter.ai/api/v1/chat/completions");
                else if (s.equals("Groq"))
                    url.setText("https://api.groq.com/openai/v1/chat/completions");
                else if (s.equals("DeepSeek"))
                    url.setText("https://api.deepseek.com/chat/completions");
                else if (s.equals("Together AI"))
                    url.setText("https://api.together.xyz/v1/chat/completions");
                else if (s.equals("xAI"))
                    url.setText("https://api.x.ai/v1/chat/completions");
                else if (s.equals("Gemini")) {
                    url.setText("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent");
                    modelInput.setText("gemini-2.5-flash");
                } else if (s.equals("Claude"))
                    url.setText("https://api.anthropic.com/v1/messages");
                else
                    url.setText("");
            }

            public void onNothingSelected(AdapterView<?> p) {}
        });

        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Universal AI Agent")
                .setMessage("Provider, model और API key डालो")
                .setView(box)
                .setCancelable(false)
                .setPositiveButton("Connect", null)
                .create();

        d.setOnShowListener(x ->
                d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    provider = sp.getSelectedItem().toString();
                    endpoint = url.getText().toString().trim();
                    model = modelInput.getText().toString().trim();
                    apiKey = key.getText().toString().trim();

                    if (endpoint.isEmpty()) { url.setError("API URL डालो"); return; }
                    if (model.isEmpty()) { modelInput.setError("Model डालो"); return; }
                    if (apiKey.length() < 5) { key.setError("API key डालो"); return; }

                    d.dismiss();
                    ui();
                })
        );

        d.show();
    }

    private void ui() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247,247,248));

        TextView title = new TextView(this);
        title.setText("Universal AI Agent\n" + provider + " • " + model);
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setPadding(dp(18), dp(16), dp(18), dp(14));
        title.setBackgroundColor(Color.WHITE);

        scrollView = new ScrollView(this);
        chatList = new LinearLayout(this);
        chatList.setOrientation(LinearLayout.VERTICAL);
        chatList.setPadding(dp(14), dp(14), dp(14), dp(14));
        scrollView.addView(chatList);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setPadding(dp(10), dp(10), dp(10), dp(10));
        bottom.setBackgroundColor(Color.WHITE);

        input = new EditText(this);
        input.setHint("Message AI...");
        input.setMaxLines(4);
        input.setBackground(bg(Color.rgb(242,242,244)));

        send = new Button(this);
        send.setText("Send");
        send.setTextColor(Color.WHITE);
        send.setBackground(bg(Color.BLACK));
        send.setOnClickListener(v -> sendMessage());

        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        ip.setMarginEnd(dp(8));

        bottom.addView(input, ip);
        bottom.addView(send, new LinearLayout.LayoutParams(dp(82), dp(52)));

        root.addView(title);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1
        ));
        root.addView(bottom);

        setContentView(root);
        bubble("Connected. अब message भेजो.", false);
    }

    private void bubble(String text, boolean user) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setTextSize(16);
        b.setTextColor(user ? Color.WHITE : Color.BLACK);
        b.setPadding(dp(14), dp(11), dp(14), dp(11));
        b.setBackground(bg(user ? Color.BLACK : Color.WHITE));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(user ? Gravity.END : Gravity.START);
        row.setPadding(0, dp(5), 0, dp(5));
        row.addView(b);

        chatList.addView(row);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void sendMessage() {
        String msg = input.getText().toString().trim();
        if (msg.isEmpty()) return;

        input.setText("");
        bubble(msg, true);

        try {
            JSONObject m = new JSONObject();
            m.put("role", "user");
            m.put("content", msg);
            history.put(m);
        } catch (Exception ignored) {}

        send.setEnabled(false);
        send.setText("...");

        new Thread(() -> {
            try {
                String reply = callApi();

                JSONObject m = new JSONObject();
                m.put("role", "assistant");
                m.put("content", reply);
                history.put(m);

                runOnUiThread(() -> {
                    bubble(reply, false);
                    send.setEnabled(true);
                    send.setText("Send");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    bubble("API Error: " + e.getMessage(), false);
                    send.setEnabled(true);
                    send.setText("Send");
                });
            }
        }).start();
    }

    private String callApi() throws Exception {
        if (provider.equals("Gemini")) return gemini();
        if (provider.equals("Claude")) return claude();
        return openAICompatible();
    }

    private String openAICompatible() throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", history);

        JSONObject r = request(
                endpoint,
                body,
                "Authorization",
                "Bearer " + apiKey,
                null,
                null
        );

        JSONArray choices = r.optJSONArray("choices");
        if (choices == null || choices.length() == 0)
            throw new IOException(r.toString());

        return choices.getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "");
    }

    private String gemini() throws Exception {
        JSONArray contents = new JSONArray();

        for (int i = 0; i < history.length(); i++) {
            JSONObject h = history.getJSONObject(i);

            JSONObject c = new JSONObject();
            c.put(
                    "role",
                    h.optString("role").equals("assistant") ? "model" : "user"
            );

            JSONArray parts = new JSONArray();
            JSONObject p = new JSONObject();
            p.put("text", h.optString("content"));
            parts.put(p);

            c.put("parts", parts);
            contents.put(c);
        }

        JSONObject body = new JSONObject();
        body.put("contents", contents);

        JSONObject r = request(
                endpoint.replace("{model}", model),
                body,
                "x-goog-api-key",
                apiKey,
                null,
                null
        );

        JSONArray candidates = r.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0)
            throw new IOException(r.toString());

        return candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .optString("text", "");
    }

    private String claude() throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", 1024);
        body.put("messages", history);

        JSONObject r = request(
                endpoint,
                body,
                "x-api-key",
                apiKey,
                "anthropic-version",
                "2023-06-01"
        );

        JSONArray content = r.optJSONArray("content");
        if (content == null || content.length() == 0)
            throw new IOException(r.toString());

        return content.getJSONObject(0).optString("text", "");
    }

    private JSONObject request(
            String urlText,
            JSONObject body,
            String h1,
            String v1,
            String h2,
            String v2
    ) throws Exception {

        HttpURLConnection c =
                (HttpURLConnection) new URL(urlText).openConnection();

        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(20000);
        c.setReadTimeout(60000);
        c.setRequestProperty("Content-Type", "application/json");

        if (h1 != null) c.setRequestProperty(h1, v1);
        if (h2 != null) c.setRequestProperty(h2, v2);

        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int status = c.getResponseCode();

        InputStream stream =
                status >= 200 && status < 300
                        ? c.getInputStream()
                        : c.getErrorStream();

        String raw = readAll(stream);

        if (status < 200 || status >= 300)
            throw new IOException("HTTP " + status + ": " + raw);

        return new JSONObject(raw);
    }

    private String readAll(InputStream in) throws Exception {
        if (in == null) return "";

        BufferedReader br = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8)
        );

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null)
            sb.append(line);

        return sb.toString();
    }
        }
