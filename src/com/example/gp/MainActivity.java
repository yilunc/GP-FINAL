package com.example.gp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.RouteManager;
import com.mapquest.android.maps.RouteResponse;
import com.mapquest.android.maps.ServiceResponse.Info;


public class MainActivity extends SimpleMap implements LocationListener {

	@Override
	protected int getLayoutId() {
		return R.layout.activity_main;
	}
	
	@Override
	protected void init() {
		super.init();

		// find the objects we need to interact with
		final MapView mapView = (MapView) findViewById(R.id.map);
		final WebView itinerary = (WebView) findViewById(R.id.itinerary);

		final RelativeLayout mapLayout = (RelativeLayout) findViewById(R.id.mapLayout);
		final RelativeLayout itineraryLayout = (RelativeLayout) findViewById(R.id.itineraryLayout);
		final Button createRouteButton = (Button) findViewById(R.id.createRouteButton);
		final Button showItineraryButton = (Button) findViewById(R.id.showItineraryButton);
		final Button showMapButton = (Button) findViewById(R.id.showMapButton);
		final Button clearButton = (Button) findViewById(R.id.clearButton);
		final EditText end = (EditText) findViewById(R.id.endTextView);
		final RouteManager routeManager = new RouteManager(this);
		
		routeManager.setMapView(mapView);
		routeManager.setItineraryView(itinerary);
		routeManager.setDebug(true);
		routeManager.setRouteCallback(new RouteManager.RouteCallback() {
			@Override
			public void onError(RouteResponse routeResponse) {
				Info info = routeResponse.info;
				int statusCode = info.statusCode;

				StringBuilder message = new StringBuilder();
				message.append("Unable to create route.\n").append("Error: ")
						.append(statusCode).append("\n").append("Message: ")
						.append(info.messages);
				Toast.makeText(getApplicationContext(), message.toString(),
						Toast.LENGTH_LONG).show();
				createRouteButton.setEnabled(true);
			}

			@Override
			public void onSuccess(RouteResponse routeResponse) {
				clearButton.setVisibility(View.VISIBLE);
				if (showItineraryButton.getVisibility() == View.GONE
						&& showMapButton.getVisibility() == View.GONE) {
					showItineraryButton.setVisibility(View.VISIBLE);
				}
				createRouteButton.setEnabled(true);
			}
		});

		// attach the show itinerary listener
		showItineraryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapLayout.setVisibility(View.GONE);
				itineraryLayout.setVisibility(View.VISIBLE);
				showItineraryButton.setVisibility(View.GONE);
				showMapButton.setVisibility(View.VISIBLE);
			}
		});

		// attach the show map listener
		showMapButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mapLayout.setVisibility(View.VISIBLE);
				itineraryLayout.setVisibility(View.GONE);
				showMapButton.setVisibility(View.GONE);
				showItineraryButton.setVisibility(View.VISIBLE);
			}
		});

		// attach the clear route listener
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				routeManager.clearRoute();
				clearButton.setVisibility(View.GONE);
				showItineraryButton.setVisibility(View.GONE);
				showMapButton.setVisibility(View.GONE);
				mapLayout.setVisibility(View.VISIBLE);
				itineraryLayout.setVisibility(View.GONE);
				setup = false;
			}
		});

		// create an onclick listener for the instructional text
		createRouteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideSoftKeyboard(view);
				createRouteButton.setEnabled(false);

				LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

				// Create a criteria object to retrieve provider
				Criteria criteria = new Criteria();

				// Get the name of the best provider
				String provider = locationManager.getBestProvider(criteria,
						true);

				// Get Current Location
				Location myLocation = locationManager
						.getLastKnownLocation(provider);
				
				double latitude = myLocation.getLatitude();
				double longitude = myLocation.getLongitude();
				String startAt = latitude + ", " + longitude;

				String endAt = getText(end);
				
				Toast.makeText(getApplicationContext(), "Hello",
						Toast.LENGTH_LONG).show();

			
				setupRoute(startAt, endAt, routeManager);
			}
		});
		
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Create a criteria object to retrieve provider
		Criteria criteria = new Criteria();

		// Get the name of the best provider
		String provider = locationManager.getBestProvider(criteria,
				true);

		// Get Current Location
		Location myLocation = locationManager
				.getLastKnownLocation(provider);

		locationManager.requestLocationUpdates(provider, 400, 1, this);
		
		Toast.makeText(getApplicationContext(), "Yo",
				Toast.LENGTH_LONG).show();
		
		CheckBt();
		
		Connect();
		
		//writeData(1 + "");
		writeData(2 + "");
	}

	private void setupRoute(String start, String end, RouteManager routeManager) {
		String url = "";
		String json = "";
		try {
			url = "https://open.mapquestapi.com/guidance/v1/route?key="
					+ "Fmjtd%7Cluurn96ynh%2Cbg%3Do5-9w8w5f"
					+ "&from="
					+ URLEncoder.encode(start, "UTF-8")
					+ "&to="
					+ URLEncoder.encode(end, "UTF-8")
					+ "&narrativeType=text&fishbone=false&callback=renderBasicInformation";
			
			GetJsonTask jsonTask = new GetJsonTask();
			jsonTask.execute(url);
			
			while (json.length() == 0) {
				json = jsonTask.json;	
			}
			// String url =
			// "https://open.mapquestapi.com/guidance/v1/route?key=Fmjtd%7Cluurn96ynh%2Cbg%3Do5-9w8w5f&from=1152%20fairford%20way,%2095129&to=1077%20Eagle%20Ridge%20Way,%20Milpitas&narrativeType=text&fishbone=false&callback=renderBasicInformation";
		} catch (Exception e) {
			json = "hi";
		}
		
		json = json.substring(23, json.length() - 2);

		String arr = json.split("\"shapePoints\":")[1]
				.split(",\"RouteLinkCount")[0];
		arr = arr.substring(1, arr.length() - 1);

		String[] single_coords = arr.split(",");
		Location[] coords = new Location[single_coords.length / 2];

		for (int i = 0; i < single_coords.length - 1; i += 2) {
			coords[i / 2] = new Location("ok");
			coords[i / 2].setLatitude(Double.parseDouble(single_coords[i]));
			coords[i / 2]
					.setLongitude(Double.parseDouble(single_coords[i + 1]));
			
			locations.add(coords[i / 2].toString());
			// System.out.print("(" + coords[i / 2][0] + "," + coords[i / 2][1]
			// + ")\n");
		}

		String[] man = json.split("\"maneuverType\":");

		ArrayList<Integer> mans = new ArrayList<Integer>();
		ArrayList<Integer> links = new ArrayList<Integer>();

		for (int i = 1; i < man.length; i++) {
			mans.add(Integer.parseInt(man[i].split(",")[0]));
			System.out.println(mans);

			links.add(Integer.parseInt(man[i].split("\\[")[1].split("\\]")[0]) + 1);
			System.out.println(links);
			
		}

		this.coords = coords;
		this.mans = mans;
		this.links = links;
		setup = true;
		
		routeManager.createRoute(start, end);
	}

	private boolean setup = false;
	private Location[] coords;
	private ArrayList<String> locations = new ArrayList<String>();
	private ArrayList<Integer> mans = new ArrayList<Integer>();
	private ArrayList<Integer> links = new ArrayList<Integer>();

	public static final int MAX_DIST = 30;

	@Override
	public void onLocationChanged(Location location) {
		if (!setup) {
			return;
		}
		
		Toast.makeText(getApplicationContext(), location.distanceTo(coords[links.get(0)])+"",
				Toast.LENGTH_SHORT).show();
		
		if(links.size() == 0){
			Context context = getApplicationContext();
			CharSequence text = "We're done!";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			
			setup = false;
		}
		
		else if (location.distanceTo(coords[links.get(0)]) < MAX_DIST) {
			
			if (mans.get(0) == 4) {
				Toast.makeText(getApplicationContext(), "LEFT",
						Toast.LENGTH_LONG).show();
				
				writeData(1 + "");
				
				
			} else if (mans.get(0) == 7) {
				Toast.makeText(getApplicationContext(), "RIGHT",
						Toast.LENGTH_LONG).show();
				
				writeData(2 + "");
			}
			// other cases maybe

			mans.remove(0);
			links.remove(0);
		} else {

		}
	}
	
	private static final String TAG = "Jon";
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private static String address = "30:14:09:22:16:50";
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private InputStream inStream = null;
	Handler handler = new Handler();
	byte delimiter = 10;
	boolean stopWorker = false;
	int readBufferPosition = 0;
	byte[] readBuffer = new byte[1024];
	
	private void CheckBt() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			Toast.makeText(getApplicationContext(), "Bluetooth is not active!",
					Toast.LENGTH_SHORT).show();
		}

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(),
					"Bluetooth doesnt exist", Toast.LENGTH_SHORT)
					.show();
		}
	}
	
	public void Connect() {
		Log.d(TAG, address);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		Log.d(TAG, "Connected " + device);
		mBluetoothAdapter.cancelDiscovery();
		try {
			btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			btSocket.connect();
			Log.d(TAG, "Connexion russie !!");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				Log.d(TAG, "Impossible to connect.");
			}
			Log.d(TAG, "Cration de socket houe.");
		}
	}
	
	private void writeData(String data) {
		try {
			outStream = btSocket.getOutputStream();
		} catch (IOException e) {
			Log.d(TAG, "Bug AVANT l'envoie.", e);
		}

		String message = data;

		byte[] msgBuffer = message.getBytes();

		try {
			outStream.write(msgBuffer);
		} catch (IOException e) {
			Log.d(TAG, "Bug DURANT l'envoie.", e);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Do nothing here
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Do nothing here
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Do nothing here
	}

//	private String readUrl(String urlString) throws Exception {
//		BufferedReader reader = null;
//		try {
//			URL url = new URL(urlString);
//			reader = new BufferedReader(new InputStreamReader(url.openStream()));
//			StringBuffer buffer = new StringBuffer();
//			int read;
//			char[] chars = new char[1024];
//			while ((read = reader.read(chars)) != -1)
//				buffer.append(chars, 0, read);
//
//			return buffer.toString();
//		} finally {
//			if (reader != null)
//				reader.close();
//		}
//
//	}
	private class GetJsonTask extends AsyncTask<String, Void, String> {

		/**
		 * Count the time needed for the data download
		 */
		private int downloadTimer;

		public String json = "";
		
		@Override
		protected String doInBackground(String... url) {
			// Get the data from the URL
			String output = "";
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			try {
				response = httpclient.execute(new HttpGet(url[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					output = out.toString();
				} else {
					// Close the connection
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (Exception e) {
				Log.e("GetJsonTask",
						"Could not get the data. This is the error message: "
								+ e.getMessage());
				return null;
			}
			json = output;
			return output;
		}
	}
}
