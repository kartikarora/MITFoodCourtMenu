package com.uc.mitfoodcourtmenu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Button seeMenu, getReview, contactDesk, contactOffice;
	TextView outputText;
	int pos = 1;

	public static final String URL = "http://apps.byethost7.com/index.php";

	public boolean isConnected() { // function to check if phone is connected to
									// internet
		boolean isConnected;
		ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		isConnected = (activeNetwork != null)
				&& (activeNetwork.isConnectedOrConnecting());
		return isConnected;
	}

	void notConnectedErrorMsg() { // this dialog box appears when phone is not
									// connected to Internet
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("ERROR!!");
		alertDialogBuilder
				.setMessage("Could not connect to server!!")
				.setCancelable(false)
				.setPositiveButton("Got it...",
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// put function here
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		start();
	}

	@Override
	public void onBackPressed() {
		if (pos == 2)
			start();
		else {
			moveTaskToBack(true);
			MainActivity.this.finish();
		}
	}

	public void start() { // this function handles the intro layout
		pos = 1;
		setContentView(R.layout.layout_intro_test);

		seeMenu = (Button) findViewById(R.id.conn_butn); // button to display
															// menu
		getReview = (Button) findViewById(R.id.rev_butn); // button to view
															// reviews
		contactDesk = (Button) findViewById(R.id.contact_desk__butn); // button
																		// to
																		// call
																		// food
																		// court
																		// helpdesk
		contactOffice = (Button) findViewById(R.id.contact_office_butn); // button
																			// to
																			// call
																			// food
																			// court
																			// office

		contactOffice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent call = new Intent(Intent.ACTION_CALL, Uri
						.parse("tel: +918202927491"));
				call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(call);
			}
		});

		contactDesk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent call = new Intent(Intent.ACTION_CALL, Uri
						.parse("tel: +918202927494"));
				call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(call);
			}
		});

		seeMenu.setOnClickListener(new OnClickListener() { // this button loads
			// the layout
			// containing the
			// menu
			@Override
			public void onClick(View v) {
				if (isConnected()) {
					setContentView(R.layout.layout_menu);
					pos = 2;
					outputText = (TextView) findViewById(R.id.outputTxt);
					PHPContact task = new PHPContact();
					task.execute(new String[] { URL });
					try {
						task.get(4000, TimeUnit.MILLISECONDS);
					} catch (Exception e) {
						e.printStackTrace();
						task.cancel(true);
						Toast.makeText(getApplicationContext(),
								"Unable To Connect To Server",
								Toast.LENGTH_SHORT).show();
						start();
					}
				}

				else
					Toast.makeText(getApplicationContext(),
							"No Active Internet Connection", Toast.LENGTH_SHORT)
							.show();
			}
		});

		getReview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent review = new Intent(MainActivity.this,
						ReviewReader.class);
				startActivity(review);
			}
		});
	}

	private class PHPContact extends AsyncTask<String, Void, String> { // this
																		// class
																		// contacts
																		// the
																		// online
																		// php
																		// script
		@Override
		protected String doInBackground(String... urls) {
			String output = null;
			for (String url : urls) {
				output = getOutputFromUrl(url);
			}
			return output;
		}

		private String getOutputFromUrl(String url) {
			StringBuffer output = new StringBuffer("");
			try {
				InputStream stream = getHttpConnection(url);
				BufferedReader buffer = new BufferedReader(
						new InputStreamReader(stream));
				String s = "";
				s = buffer.readLine();
				if (s.equalsIgnoreCase("50e7c87cdd40f5c3c1e77121e0a812c2")) {

					while ((s = buffer.readLine()) != null) {
						output.append(s);
						output.append("\n");
					}
				} else {
					s = "AUTH_ERROR";
					output.append(s);
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return output.toString();
		}

		// Makes HttpURLConnection and returns InputStream
		private InputStream getHttpConnection(String urlString)
				throws IOException {
			InputStream stream = null;
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();

			try {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					stream = httpConnection.getInputStream();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return stream;
		}

		@Override
		protected void onPostExecute(String output) {
			if(output.contains("AUTH_ERROR")) {
				Toast.makeText(getApplicationContext(),
						"Authentication Failed", Toast.LENGTH_SHORT)
						.show();
				outputText.setText("ERROR");
			}
		else
			outputText.setText(output);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
