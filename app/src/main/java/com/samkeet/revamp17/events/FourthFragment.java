package com.samkeet.revamp17.events;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.samkeet.revamp17.Constants;
import com.samkeet.revamp17.R;
import com.samkeet.revamp17.myboom.AllMoveDownAnimatorAdapter;
import com.samkeet.revamp17.myboom.CardStackView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import dmax.dialog.SpotsDialog;

/**
 * Created by Frost on 11-03-2017.
 */

public class FourthFragment extends Fragment {

    private CardStackView mStackView;
    private EventsAdapter mEventsAdapter;
    private SpotsDialog pd;
    private Context progressDialogContext;
    private JSONArray sportsevents;
    private boolean errordata = true;


    private static Integer[] TEST_DATAS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.four_frag, container, false);

        progressDialogContext = getActivity();

        mStackView = (CardStackView) v.findViewById(R.id.stackview_main);

        if (!Constants.Events.isSportsAvalible) {
            GetEvents getEvents = new GetEvents();
            getEvents.execute();
        } else {
            loadData();
        }
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void loadData() {

        if (Constants.Events.isSportsAvalible) {
            int count = Constants.Events.sportsEvents.length();
            TEST_DATAS = new Integer[count];
            TEST_DATAS = Constants.Methods.getColors(count);

            mEventsAdapter = new EventsAdapter(getActivity().getApplicationContext(), Constants.Events.sportsEvents, getActivity(), false);
            mStackView.setAdapter(mEventsAdapter);
            mEventsAdapter.updateData(Arrays.asList(TEST_DATAS));
            mStackView.setAnimatorAdapter(new AllMoveDownAnimatorAdapter(mStackView));
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Error in loading data", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetEvents extends AsyncTask<Void, Void, Integer> {

        protected void onPreExecute() {
            pd = new SpotsDialog(progressDialogContext, R.style.CustomPD);
            pd.setTitle("Loading...");
            pd.setCancelable(false);
            pd.show();
        }

        protected Integer doInBackground(Void... params) {
            try {
                URL url = new URL(Constants.URLs.BASE + Constants.URLs.GET_EVENTS);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                Uri.Builder _data = new Uri.Builder().appendQueryParameter("Etype", "sports");
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
                writer.write(_data.build().getEncodedQuery());
                writer.flush();
                writer.close();

                InputStreamReader in = new InputStreamReader(connection.getInputStream());

                StringBuilder jsonResults = new StringBuilder();
                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                connection.disconnect();

                sportsevents = new JSONArray(jsonResults.toString());

                errordata = false;

                return 1;

            } catch (JSONException ex) {
                ex.printStackTrace();
                errordata = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 1;
        }

        protected void onPostExecute(Integer result) {
            if (pd != null) {
                pd.dismiss();
            }

            if (errordata) {
                Toast.makeText(getActivity().getApplicationContext(), "Error in loading data", Toast.LENGTH_SHORT).show();
            } else {
                Constants.Events.sportsEvents = sportsevents;
                Constants.Events.isSportsAvalible = true;
                loadData();
            }

        }
    }
}
