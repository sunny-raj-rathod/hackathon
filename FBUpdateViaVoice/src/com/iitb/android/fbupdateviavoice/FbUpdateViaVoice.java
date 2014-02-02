package com.iitb.android.fbupdateviavoice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class FbUpdateViaVoice extends Activity {

	// Your Facebook APP ID
	private static String APP_ID = "256360104525244"; // Replace with your App ID

	// Instance of Facebook Class
	private Facebook facebook = new Facebook(APP_ID);
	private AsyncFacebookRunner mAsyncRunner;
	String FILENAME = "AndroidSSO_data";
	private SharedPreferences mPrefs;
	
	Button btnPostToWall;
	
	Button btnRedirect;
	//Button btnFbLogin;

	protected static final int RESULT_SPEECH = 1;

	private ImageButton btnSpeak;
	private EditText txtText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loginToFacebook();

		txtText = (EditText) findViewById(R.id.txtText);

		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
		
		btnPostToWall = (Button) findViewById(R.id.btn_fb_post_to_wall);
		
		btnRedirect = (Button) findViewById(R.id.btn_redirect_to_fb);
		
		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

				try {
					startActivityForResult(intent, RESULT_SPEECH);
					txtText.setText("");
				} catch (ActivityNotFoundException a) {
					Toast t = Toast.makeText(getApplicationContext(),
							"Ops! Your device doesn't support Speech to Text",
							Toast.LENGTH_SHORT);
					t.show();
				}
			}
		});

		/**
		 * Posting to Facebook Wall
		 * */
		btnPostToWall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				postToWall();
			}
		});
		
		btnRedirect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PackageManager pm = getPackageManager();
				Intent intent = pm.getLaunchIntentForPackage("com.facebook.katana");
				startActivity(intent);
			}
		});
	}
	
	
	/**
	 * Function to login into facebook
	 * */
	public void loginToFacebook() {

		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null) {
			facebook.setAccessToken(access_token);
			
			Log.d("FB Sessions", "" + facebook.isSessionValid());
		}

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		if (!facebook.isSessionValid()) {
			facebook.authorize(this,
					new String[] { "email", "publish_stream" },
					new DialogListener() {

						@Override
						public void onCancel() {
							// Function to handle cancel event
						}

						@Override
						public void onComplete(Bundle values) {
							// Function to handle complete event
							// Edit Preferences and update facebook acess_token
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putString("access_token",
									facebook.getAccessToken());
							editor.putLong("access_expires",
									facebook.getAccessExpires());
							editor.commit();
						}

						@Override
						public void onError(DialogError error) {
							// Function to handle error

						}

						@Override
						public void onFacebookError(FacebookError fberror) {
							// Function to handle Facebook errors

						}

					});
		}
	}

	
	public void postToWall() {
		
        facebook.authorize(this, new String[]{ "publish_checkins,publish_actions,publish_stream"},new DialogListener() {                     
            @Override                     
            public void onComplete(Bundle values) {   

                Bundle params = new Bundle();              
                params.putString(Facebook.TOKEN, facebook.getAccessToken());              
                params.putString("message", txtText.getText().toString());
                params.putString("description", "topic share");

                AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
                
                mAsyncRunner.request("me/feed", params, "POST", new mRequestListener(),null);
                //Toast.makeText(getApplicationContext(), "Message Posted on Facebook.", Toast.LENGTH_SHORT).show();
            }                      
            @Override                     
            public void onFacebookError(FacebookError error) {}                      
            @Override                     
            public void onError(DialogError e) {}                      
            @Override                     
            public void onCancel() {}                 
        });
	}
	
	public class mRequestListener implements RequestListener{

	    @Override
	    public void onMalformedURLException(MalformedURLException e, Object state) {
	        Log.d("BTS", "******************* FACEBOOK::onMalformedURLException *******************");
	        e.printStackTrace();
	        finish();
	    }

	    @Override
	    public void onIOException(IOException e, Object state) {
	        Log.d("BTS", "******************* FACEBOOK::onIOException *******************");
	        e.printStackTrace();
	        finish();
	    }

	    @Override
	    public void onFileNotFoundException(FileNotFoundException e, Object state) {
	        Log.d("BTS", "******************* FACEBOOK::onFileNotFoundException *******************");
	        e.printStackTrace();
	        Toast.makeText(getApplicationContext(), "Image not found", Toast.LENGTH_LONG).show();
	        finish();
	    }

	    @Override
	    public void onFacebookError(FacebookError e, Object state) {
	        Log.d("BTS", "******************* FACEBOOK::onFacebookError *******************");
	        e.printStackTrace();
	        Toast.makeText(getApplicationContext(), "Facebook error", Toast.LENGTH_LONG).show();
	        finish();
	    }

	    String msg="";

	    @Override
	    public void onComplete(String response, Object state) {
	        Log.d("BTS", "******************* FACEBOOK::onComplete *******************");
	        Log.d("Profile", response);
			String json = response;
			try {
				// Facebook Profile JSON data
				JSONObject respObj = new JSONObject(json);
				
				// getting name of the user
				final String postId = respObj.getString("id");
				
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						//Toast.makeText(getApplicationContext(), "Post id: " + postId, Toast.LENGTH_LONG).show();
						//Toast.makeText(getApplicationContext(), "User id: " + userid, Toast.LENGTH_LONG).show();
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://post/" + postId));
				        startActivity(intent);
					}

				});

				
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        // TODO Auto-generated method stub
	        Log.d("Facebook-Example",
	                "News feed: " + response.toString());
	    }

	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println(requestCode);
		facebook.authorizeCallback(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SPEECH: {
			if (resultCode == RESULT_OK && null != data) {

				ArrayList<String> text = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				txtText.setText(text.get(0));
			}
			break;
		}

		}
	}
}
