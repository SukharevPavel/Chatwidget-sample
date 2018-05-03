package ru.mmt.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ru.mmt.chatwidget.ChatWidgetInstance;
import ru.mmt.chatwidget.ErrorCode;
import ru.mmt.chatwidget.OnChatLoadListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final Pattern pattern = Pattern.compile("[А-я]");

    private EditText editText;
    private Button button;
    private EditText sessionText;
    private EditText loginText;
    private EditText passwordText;
    private Button loginButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginText = (EditText) findViewById(R.id.main_login_text);
        passwordText = (EditText) findViewById(R.id.main_password_text);
        loginButton = (Button) findViewById(R.id.main_login);
        editText = (EditText) findViewById(R.id.main_edit_text);
        sessionText = findViewById(R.id.main_session_text);
        button = (Button) findViewById(R.id.main_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "clicked");
                ChatWidgetInstance.getInstance(getApplicationContext())
                        .start(sessionText.getText().toString(), editText.getText().toString(), new OnChatLoadListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(ErrorCode errorCode) {
                                Toast.makeText(getApplicationContext(), errorCode.toString(), Toast.LENGTH_SHORT).show();
                                switch (errorCode) {
                                    case USER_NOT_LOGGED_IN:
                                        ChatWidgetInstance.getInstance(getApplicationContext()).stop();
                                }
                            }
                        });
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask().execute(loginText.getText().toString(), passwordText.getText().toString());
            }
        });
    }

    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String login = params[0];
            String password = params[1];
            String base = "https://onlinedoctor.ru";
            if (BuildConfig.DEBUG) {
                base = "https://dev.onlinedoctor.ru";
            }
            String urlWithParams = base + "/mobile/login/?email=" + login + "&password=" + password;
            try {
                URL url = new URL(urlWithParams);
                URLConnection conn = url.openConnection();
                conn.connect();
                Map<String, List<String>> headers = conn.getHeaderFields();
                InputStream stream = conn.getInputStream();
                List<String> values = headers.get("Set-Cookie");

                String cookieValue = null;
                for (Iterator<String> iter = values.iterator(); iter.hasNext(); ) {
                    String v = iter.next();
                    String[] array = v.split(";");
                    for (String arrayElement : array) {
                        if (arrayElement.startsWith("PHPSESSID=")) {
                            cookieValue = arrayElement.replace("PHPSESSID=", "");
                        }
                    }
                }
                return cookieValue;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            sessionText.setText(s);
            Log.i(MainActivity.class.getSimpleName(), "cookie = " + s);
            Toast.makeText(MainActivity.this, "Got new cookie", Toast.LENGTH_SHORT).show();
        }
    }


}
