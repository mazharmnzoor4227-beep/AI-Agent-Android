package com.example.aiagent;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;

import org.json.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private LinearLayout chatList;
    private EditText messageInput;
    private Button sendButton;
    private ScrollView scrollView;

    private String apiKey = "";
    private String previousResponseId = null;

    private final String model = "gpt-5.6-luna";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showKeyDialog();
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp((int) radius));
        return drawable;
    }

    private TextView createText(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        view.setTypeface(
                Typeface.DEFAULT,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );

        return view;
    }

    private void showKeyDialog() {

        EditText keyInput = new EditText(this);

        keyInput.setHint("sk-...");
        keyInput.setSingleLine(true);

        keyInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        keyInput.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        LinearLayout wrapper = new LinearLayout(this);

        wrapper.setPadding(
                dp(22),
                dp(8),
                dp(22),
                0
        );

        wrapper.addView(
                keyInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Connect OpenAI API")
                        .setMessage(
                                "अपनी OpenAI API key यहाँ paste करो।"
                        )
                        .setView(wrapper)
                        .setCancelable(false)
                        .setPositiveButton("Connect", null)
                        .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String key =
                        keyInput
                                .getText()
                                .toString()
                                .trim();

                if (key.length() < 10) {

                    keyInput.setError(
                            "Valid API key डालो"
                    );

                    return;
                }

                apiKey = key;

                dialog.dismiss();

                buildUI();
            });
        });

        dialog.show();
    }

    private void buildUI() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.rgb(247, 247, 248)
        );

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.VERTICAL
        );

        header.setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(14)
        );

        header.setBackgroundColor(
                Color.WHITE
        );

        TextView title =
                createText(
                        "AI Agent",
                        23,
                        Color.rgb(18, 18, 20),
                        true
                );

        TextView subtitle =
                createText(
                        "Connected • " + model,
                        12,
                        Color.rgb(95, 95, 105),
                        false
                );

        header.addView(title);
        header.addView(subtitle);

        scrollView =
                new ScrollView(this);

        scrollView.setFillViewport(true);

        chatList =
                new LinearLayout(this);

        chatList.setOrientation(
                LinearLayout.VERTICAL
        );

        chatList.setPadding(
                dp(14),
                dp(18),
                dp(14),
                dp(18)
        );

        scrollView.addView(chatList);

        LinearLayout composer =
                new LinearLayout(this);

        composer.setOrientation(
                LinearLayout.HORIZONTAL
        );

        composer.setGravity(
                Gravity.BOTTOM
        );

        composer.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(12)
        );

        composer.setBackgroundColor(
                Color.WHITE
        );

        messageInput =
                new EditText(this);

        messageInput.setHint(
                "Message AI..."
        );

        messageInput.setTextSize(16);

        messageInput.setMinHeight(
                dp(52)
        );

        messageInput.setMaxLines(5);

        messageInput.setPadding(
                dp(15),
                dp(10),
                dp(15),
                dp(10)
        );

        messageInput.setBackground(
                rounded(
                        Color.rgb(242, 242, 244),
                        18
                )
        );

        sendButton =
                new Button(this);

        sendButton.setText("Send");

        sendButton.setTextColor(
                Color.WHITE
        );

        sendButton.setTextSize(14);

        sendButton.setAllCaps(false);

        sendButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        sendButton.setBackground(
                rounded(
                        Color.rgb(20, 20, 22),
                        18
                )
        );

        sendButton.setOnClickListener(
                v -> sendMessage()
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        inputParams.setMarginEnd(
                dp(8)
        );

        composer.addView(
                messageInput,
                inputParams
        );

        composer.addView(
                sendButton,
                new LinearLayout.LayoutParams(
                        dp(82),
                        dp(52)
                )
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        root.addView(
                composer,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);

        addBubble(
                "AI Agent connected. Message भेजकर API test करो.",
                false
        );
    }

    private void addBubble(
            String value,
            boolean isUser
    ) {

        TextView bubble =
                createText(
                        value,
                        16,
                        isUser
                                ? Color.WHITE
                                : Color.rgb(
                                        25,
                                        25,
                                        28
                                ),
                        false
                );

        bubble.setPadding(
                dp(14),
                dp(11),
                dp(14),
                dp(11)
        );

        bubble.setBackground(
                rounded(
                        isUser
                                ? Color.rgb(
                                        24,
                                        24,
                                        27
                                )
                                : Color.WHITE,
                        18
                )
        );

        bubble.setMaxWidth(
                dp(315)
        );

        LinearLayout row =
                new LinearLayout(this);

        row.setGravity(
                isUser
                        ? Gravity.END
                        : Gravity.START
        );

        row.setPadding(
                0,
                dp(5),
                0,
                dp(5)
        );

        row.addView(bubble);

        chatList.addView(
                row,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        scrollView.post(
                () ->
                        scrollView.fullScroll(
                                View.FOCUS_DOWN
                        )
        );
    }

    private void sendMessage() {

        String message =
                messageInput
                        .getText()
                        .toString()
                        .trim();

        if (message.isEmpty()) {
            return;
        }

        messageInput.setText("");

        addBubble(
                message,
                true
        );

        sendButton.setEnabled(false);

        sendButton.setText("...");

        new Thread(() -> {

            try {

                String reply =
                        callOpenAI(message);

                runOnUiThread(() -> {

                    addBubble(
                            reply,
                            false
                    );

                    sendButton.setEnabled(
                            true
                    );

                    sendButton.setText(
                            "Send"
                    );
                });

            } catch (Exception e) {

                String error =
                        e.getMessage();

                runOnUiThread(() -> {

                    addBubble(
                            "API Error: "
                                    + (
                                    error == null
                                            ? "Unknown error"
                                            : error
                            ),
                            false
                    );

                    sendButton.setEnabled(
                            true
                    );

                    sendButton.setText(
                            "Send"
                    );
                });
            }

        }).start();
    }

    private String callOpenAI(
            String message
    ) throws Exception {

        URL url =
                new URL(
                        "https://api.openai.com/v1/responses"
                );

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod(
                "POST"
        );

        connection.setConnectTimeout(
                20000
        );

        connection.setReadTimeout(
                60000
        );

        connection.setDoOutput(true);

        connection.setRequestProperty(
                "Authorization",
                "Bearer " + apiKey
        );

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        JSONObject body =
                new JSONObject();

        body.put(
                "model",
                model
        );

        body.put(
                "input",
                message
        );

        body.put(
                "instructions",
                "You are a helpful Android AI assistant. Reply in the same language as the user."
        );

        body.put(
                "store",
                true
        );

        if (
                previousResponseId != null
        ) {

            body.put(
                    "previous_response_id",
                    previousResponseId
            );
        }

        byte[] bytes =
                body
                        .toString()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        try (
                OutputStream output =
                        connection
                                .getOutputStream()
        ) {

            output.write(bytes);
        }

        int status =
                connection
                        .getResponseCode();

        InputStream stream =
                status >= 200 &&
                        status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

        String raw =
                readAll(stream);

        JSONObject json =
                new JSONObject(raw);

        if (
                status < 200 ||
                        status >= 300
        ) {

            JSONObject error =
                    json.optJSONObject(
                            "error"
                    );

            String detail =
                    error != null
                            ? error.optString(
                            "message",
                            raw
                    )
                            : raw;

            throw new IOException(
                    "HTTP "
                            + status
                            + ": "
                            + detail
            );
        }

        previousResponseId =
                json.optString(
                        "id",
                        previousResponseId
                );

        String outputText =
                extractOutputText(
                        json
                );

        if (
                outputText.isEmpty()
        ) {

            throw new IOException(
                    "API ने text response नहीं दिया."
            );
        }

        return outputText;
    }

    private String extractOutputText(
            JSONObject json
    ) {

        String direct =
                json.optString(
                        "output_text",
                        ""
                );

        if (
                !direct.isEmpty()
        ) {

            return direct;
        }

        StringBuilder result =
                new StringBuilder();

        JSONArray output =
                json.optJSONArray(
                        "output"
                );

        if (
                output == null
        ) {

            return "";
        }

        for (
                int i = 0;
                i < output.length();
                i++
        ) {

            JSONObject item =
                    output.optJSONObject(i);

            if (
                    item == null
            ) {

                continue;
            }

            JSONArray content =
                    item.optJSONArray(
                            "content"
                    );

            if (
                    content == null
            ) {

                continue;
            }

            for (
                    int j = 0;
                    j < content.length();
                    j++
            ) {

                JSONObject part =
                        content.optJSONObject(j);

                if (
                        part == null
                ) {

                    continue;
                }

                if (
                        "output_text".equals(
                                part.optString(
                                        "type"
                                )
                        )
                ) {

                    if (
                            result.length() > 0
                    ) {

                        result.append("\n");
                    }

                    result.append(
                            part.optString(
                                    "text",
                                    ""
                            )
                    );
                }
            }
        }

        return result
                .toString()
                .trim();
    }

    private String readAll(
            InputStream input
    ) throws Exception {

        if (
                input == null
        ) {

            return "";
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                input,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while (
                (line = reader.readLine())
                        != null
        ) {

            result.append(line);
        }

        return result.toString();
    }
                  }
