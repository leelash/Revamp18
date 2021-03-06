package com.samkeet.revamp17;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.samkeet.revamp17.admin.AdminMainActivity;
import com.samkeet.revamp17.coordinator.CoMainActivity;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import dmax.dialog.SpotsDialog;

public class BLoginActivity extends AppCompatActivity {

    public AppCompatButton mLoginButton, mSignupButton, backstage;
    public TextInputLayout tMobileno, tPassword;
    public String mobileno, password;
    public EditText mMobileno, mPassword;
    private SpotsDialog pd;
    private Context progressDialogContext;

    public boolean authenticationError = true;
    public String errorMessage = "Data Corrupted";

    public String token, type, ec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backstage_login);

        progressDialogContext = this;

        mMobileno = (EditText) findViewById(R.id.input_mobileno);
        mPassword = (EditText) findViewById(R.id.input_password);

        tMobileno = (TextInputLayout) findViewById(R.id.text_mobileno);
        tPassword = (TextInputLayout) findViewById(R.id.text_password);

        mLoginButton = (AppCompatButton) findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (valid()) {
                    if (Constants.Methods.networkState(getApplicationContext(), (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE))) {
                        Login login = new Login();
                        login.execute();
                    }
                }
            }
        });

    }

    public boolean valid() {

        mobileno = mMobileno.getText().toString().trim();
        password = mPassword.getText().toString().trim();

        if (mobileno.isEmpty() || mobileno.length() != 10) {
            tMobileno.setError("Enter 10 digits Mobile No");
            requestFocus(mMobileno);
            return false;
        } else {
            tMobileno.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            tPassword.setError("Enter valid Password");
            requestFocus(mPassword);
            return false;
        } else {
            tPassword.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void forward() {
        if (type.equals("COORDINATOR")) {
            Intent intent = new Intent(getApplicationContext(), CoMainActivity.class);
            startActivity(intent);
            finish();
        } else if (type.equals("ADMIN")) {
            Intent intent = new Intent(getApplicationContext(), AdminMainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "USER_TYPE mismatch", Toast.LENGTH_SHORT).show();
            Constants.SharedPreferenceData.clearData();
        }
    }

    private class Login extends AsyncTask<Void, Void, Integer> {

        protected void onPreExecute() {
            pd = new SpotsDialog(progressDialogContext, R.style.CustomPD);
            pd.setTitle("Loading...");
            pd.setCancelable(false);
            pd.show();
        }

        protected Integer doInBackground(Void... params) {
            try {

                URL url = new URL(Constants.URLs.BASE + Constants.URLs.BACKSTAGE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                Uri.Builder _data = new Uri.Builder().appendQueryParameter("Mobile", mobileno)
                        .appendQueryParameter("Password", password);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(_data.build().getEncodedQuery());
                writer.flush();
                writer.close();

                InputStreamReader in = new InputStreamReader(connection.getInputStream());
                StringBuilder jsonResults = new StringBuilder();
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                connection.disconnect();

                authenticationError = jsonResults.toString().contains("Authentication Error");

                if (authenticationError) {
                    errorMessage = jsonResults.toString();
                } else {
                    JSONObject jsonObj = new JSONObject(jsonResults.toString());
                    String status = jsonObj.getString("status");
                    if (status.equals("success")) {
                        token = jsonObj.getString("token");
                        type = jsonObj.getString("type");
                        ec = jsonObj.getString("ec");
                        authenticationError = false;
                    } else {
                        authenticationError = true;
                        errorMessage = status;
                    }
                }

                return 1;
            } catch (FileNotFoundException | ConnectException | UnknownHostException ex) {
                authenticationError = true;
                errorMessage = "Connection to server terminated.\n Please check internet connection.";
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }

        protected void onPostExecute(Integer result) {
            if (pd != null) {
                pd.dismiss();
            }
            if (authenticationError) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                Constants.SharedPreferenceData.setIsLoggedIn(token);
                Constants.SharedPreferenceData.setTOKEN(token);
                Constants.SharedPreferenceData.setMobileNo(mobileno);
                Constants.SharedPreferenceData.setTYPE(type);
                Constants.SharedPreferenceData.setEC(ec);

                forward();
            }

        }
    }

}
