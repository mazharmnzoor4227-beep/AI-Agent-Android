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
    private String endpoint = "";
    private String model = "";
    private String provider = "";

    private final JSONArray history = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSetupDialog();
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(
                Typeface.DEFAULT,
                bold ? Typeface.BOLD : Typeface.NORMAL
        );
        return t;
    }

    private void showSetupDialog() {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(10), dp(20), 0);

        Spinner spinner = new Spinner(this);

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

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        providers
                );

        spinner.setAdapter(adapter);

        EditText endpointInput = new EditText(this);
        endpointInput.setHint("API URL");

        EditText modelInput = new EditText(this);
        modelInput.setHint("Model name");

        EditText keyInput = new EditText(this);
        keyInput.setHint("API key");

        keyInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        box.addView(spinner);
        box.addView(endpointInput);
        box.addView(modelInput);
        box.addView(keyInput);

        spinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        String p = providers[position];

                        if (p.equals("OpenAI")) {

                            endpointInput.setText(
                                    "https://api.openai.com/v1/chat/completions"
                            );

                            modelInput.setText("");

                        } else if (p.equals("OpenRouter")) {

                            endpointInput.setText(
                                    "https://openrouter.ai/api/v1/chat/completions"
                            );

                            modelInput.setText("");

                        } else if (p.equals("Groq")) {

                            endpointInput.setText(
                                    "https://api.groq.com/openai/v1/chat/completions"
                            );

                            modelInput.setText("");

                        } else if (p.equals("DeepSeek")) {

                            endpointInput.setText(
                                    "https://api.deepseek.com/chat/completions"
                            );

                            modelInput.setText("");

                        } else if (p.equals("Together AI")) {

                            endpointInput.setText(
                                    "https://api.together.xyz/v1/chat/completions"
                            );

                            modelInput.setText("");

                        } else if (p.equals("xAI")) {

                            endpointInput.setText(
                                    "https://api.x.ai/v1/chat/completions"
                            );

                            modelInput.setText("");

                        } else if (p.equals("Gemini")) {

                            endpointInput.setText(
                                    "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent"
                            );

                            modelInput.setText(
                                    "gemini-2.5-flash"
                            );

                        } else if (p.equals("Claude")) {

                            endpointInput.setText(
                                    "https://api.anthropic.com/v1/messages"
                            );

                            modelInput.setText("");

                        } else {

                            endpointInput.setText("");
                            modelInput.setText("");
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {}
                }
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Universal AI Agent")
                        .setMessage(
                                "Provider चुनो, Model Name और API Key डालो।"
                        )
                        .setView(box)
                        .setCancelable(false)
                        .setPositiveButton(
                                "Connect",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                provider =
                        spinner
                                .getSelectedItem()
                                .toString();

                endpoint =
                        endpointInput
                                .getText()
                                .toString()
                                .trim();

                model =
                        modelInput
                                .getText()
                                .toString()
                                .trim();

                apiKey =
                        keyInput
                                .getText()
                                .toString()
                                .trim();

                if (endpoint.isEmpty()) {

                    endpointInput.setError(
                            "API URL डालो"
                    );

                    return;
                }

                if (model.isEmpty()) {

                    modelInput.setError(
                            "Model name डालो"
                    );

                    return;
                }

                if (apiKey.length() < 5) {

                    keyInput.setError(
                            "API key डालो"
                    );

                    return;
                }

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
                Color.rgb(
                        247,
                        247,
                        248
                )
        );

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.VERTICAL
        );

        header.setPadding(
                dp(18),
                dp(16),
                dp(18),
                dp(14)
        );

        header.setBackgroundColor(
                Color.WHITE
        );

        TextView title =
                text(
                        "Universal AI Agent",
                        22,
                        Color.rgb(
                                20,
                                20,
                                22
                        ),
                        true
                );

        TextView subtitle =
                text(
                        provider + " • " + model,
                        12,
                        Color.GRAY,
                        false
                );

        header.addView(title);
        header.addView(subtitle);

        scrollView =
                new ScrollView(this);

        chatList =
                new LinearLayout(this);

        chatList.setOrientation(
                LinearLayout.VERTICAL
        );

        chatList.setPadding(
                dp(14),
                dp(16),
                dp(14),
                dp(16)
        );

        scrollView.addView(
                chatList
        );

        LinearLayout bottom =
                new LinearLayout(this);

        bottom.setOrientation(
                LinearLayout.HORIZONTAL
        );

        bottom.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(12)
        );

        bottom.setBackgroundColor(
                Color.WHITE
        );

        messageInput =
                new EditText(this);

        messageInput.setHint(
                "Message AI..."
        );

        messageInput.setTextSize(16);

        messageInput.setMaxLines(5);

        messageInput.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        messageInput.setBackground(
                rounded(
                        Color.rgb(
                                242,
                                242,
                                244
                        ),
                        18
                )
        );

        sendButton =
                new Button(this);

        sendButton.setText(
                "Send"
        );

        sendButton.setTextColor(
                Color.WHITE
        );

        sendButton.setAllCaps(
                false
        );

        sendButton.setBackground(
                rounded(
                        Color.rgb(
                                20,
                                20,
                                22
                        ),
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

        bottom.addView(
                messageInput,
                inputParams
        );

        bottom.addView(
                sendButton,
                new LinearLayout.LayoutParams(
                        dp(82),
                        dp(52)
                )
        );

        root.addView(header);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        root.addView(bottom);

        setContentView(root);

        addBubble(
                "Connected. अब message भेजो.",
                false
        );
    }

    private void addBubble(
            String value,
            boolean user
    ) {

        TextView bubble =
                text(
                        value,
                        16,
                        user
                                ? Color.WHITE
                                : Color.BLACK,
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
                        user
                                ? Color.rgb(
                                        24,
                                        24,
                                        27
                                )
                                : Color.WHITE,
                        18
                )
        );

        LinearLayout row =
                new LinearLayout(this);

        row.setGravity(
                user
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

        chatList.addView(row);

        scrollView.post(
                () ->
                        scrollView.fullScroll(
                                View.FOCUS_DOWN
                        )
        );
    }

    private void sendMessage() {

        String msg =
                messageInput
                        .getText()
                        .toString()
                        .trim();

        if (msg.isEmpty()) {
            return;
        }

        messageInput.setText("");

        addBubble(
                msg,
                true
        );

        try {

            JSONObject historyItem =
                    new JSONObject();

            historyItem.put(
                    "role",
                    "user"
            );

            historyItem.put(
                    "content",
                    msg
            );

            history.put(
                    historyItem
            );

        } catch (Exception ignored) {}

        sendButton.setEnabled(
                false
        );

        sendButton.setText(
                "..."
        );

        new Thread(() -> {

            try {

                String reply =
                        callAPI();

                JSONObject aiItem =
                        new JSONObject();

                aiItem.put(
                        "role",
                        "assistant"
                );

                aiItem.put(
                        "content",
                        reply
                );

                history.put(
                        aiItem
                );

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

                runOnUiThread(() -> {

                    addBubble(
                            "API Error: "
                                    + e.getMessage(),
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

    private String callAPI()
            throws Exception {

        if (
                provider.equals(
                        "Gemini"
                )
        ) {

            return callGemini();

        } else if (
                provider.equals(
                        "Claude"
                )
        ) {

            return callClaude();

        } else {

            return callOpenAICompatible();
        }
    }

    private String callOpenAICompatible()
            throws Exception {

        JSONObject body =
                new JSONObject();

        body.put(
                "model",
                model
        );

        body.put(
                "messages",
                history
        );

        JSONObject response =
                request(
                        endpoint,
                        body,
                        "Authorization",
                        "Bearer " + apiKey
                );

        JSONArray choices =
                response.optJSONArray(
                        "choices"
                );

        if (
                choices == null ||
                choices.length() == 0
        ) {

            throw new IOException(
                    response.toString()
            );
        }

        JSONObject message =
                choices
                        .getJSONObject(0)
                        .getJSONObject(
                                "message"
                        );

        return message.optString(
                "content"
        );
    }

    private String callGemini()
            throws Exception {

        String url =
                endpoint.replace(
                        "{model}",
                        model
                );

        JSONArray contents =
                new JSONArray();

        for (
                int i = 0;
                i < history.length();
                i++
        ) {

            JSONObject item =
                    history.getJSONObject(i);

            JSONObject content =
                    new JSONObject();

            content.put(
                    "role",
                    item.optString(
                            "role"
                    ).equals(
                            "assistant"
                    )
                            ? "model"
                            : "user"
            );

            JSONArray parts =
                    new JSONArray();

            JSONObject part =
                    new JSONObject();

            part.put(
                    "text",
                    item.optString(
                            "content"
                    )
            );

            parts.put(part);

            content.put(
                    "parts",
                    parts
            );

            contents.put(
                    content
            );
        }

        JSONObject body =
                new JSONObject();

        body.put(
                "contents",
                contents
        );

        JSONObject response =
                request(
                        url,
                        body,
                        "x-goog-api-key",
                        apiKey
                );

        JSONArray candidates =
                response.getJSONArray(
                        "candidates"
                );

        JSONObject content =
                candidates
                        .getJSONObject(0)
                        .getJSONObject(
                                "content"
                        );

        JSONArray parts =
       
