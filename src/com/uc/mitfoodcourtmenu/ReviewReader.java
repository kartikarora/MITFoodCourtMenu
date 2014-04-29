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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewReader extends Activity {
	public static final String URL = "http://apps.byethost7.com/reviews.php"; // script
																				// url
	Button postReview; // button to post review
	TextView reviewText; //text view to display the reviews

	public boolean isConnected() { // this method checks if the device is
									// connected to the internet
		NetworkInfo localNetworkInfo = ((ConnectivityManager) getApplicationContext()
				.getSystemService("connectivity")).getActiveNetworkInfo();
		return (localNetworkInfo != null)
				&& (localNetworkInfo.isConnectedOrConnecting());
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.layout_review);
		
		reviewText = ((TextView) findViewById(R.id.reviews));
		
		if (isConnected()) { // if device is connected, reviews will be displayed
			Reviewer reviewer = new Reviewer(); //new instance of reviewer to set text view to the reviews
			reviewer.execute(new String[] { URL });
			try {
				reviewer.get(4000, TimeUnit.MILLISECONDS); //sets timeout function for fetching data
				return;
			} catch (Exception e) {
				e.printStackTrace();
				reviewer.cancel(true);
				Toast.makeText(getApplicationContext(),
						"Unable To Connect To Server", Toast.LENGTH_SHORT)
						.show();
				ReviewReader.this.finish(); //kills the activity
			}
		} else { //if not, activity will be closed
			Toast.makeText(getApplicationContext(),
					"No Active Internet Connection", Toast.LENGTH_SHORT).show();
			ReviewReader.this.finish();
		}
	}

	public void subRev(View paramView) {
		Toast.makeText(getApplicationContext(), "Coming Soon",
				Toast.LENGTH_SHORT).show();
	}

	private class Reviewer extends AsyncTask<String, Void, String> {

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
				reviewText.setText("ERROR");
			}
		else
			reviewText.setText(output);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}