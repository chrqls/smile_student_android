/**
Copyright 2012-2013 SMILE Consortium, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**/

package org.smilec.smile.student;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
//import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class smile extends Activity {

	/** Called when the activity is first created. */
	Button   cancelb;
	Button   okb;
	Button   shareb;
	EditText unameet;
	EditText uri;

	static String susername;
	static String suri;
	static String server_uri;
	static String[] language_list;
	static String chosen_language = "English"; // default
	boolean index_start = true; // if true, activity starts

	int ACTIVITY_OK;

	ArrayAdapter<String> langlist;

	Resources 		res;
	Locale 			cur_locale;
	Configuration 	cur_config;

	// false: no system out, true: system out
	boolean show_systemout = true;

	// Start point
	@Override
	public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        try {
            setTitle(getTitle() + " v" + getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName);
        } catch(NameNotFoundException nnfe) {
        }
        set_init_locale();
        setContentView(R.layout.main);
        initialize_basic_features();
        set_adaptor();
        select_language();

    }

    // If we are going to hardcode the handling of the localization init
    // We should just hardcode the languages and mappings, get rid of a bunch of this code
    // and lookup from a hashmap
	void set_init_locale(){ //English

		cur_locale = new Locale("");
		Locale.setDefault(cur_locale);
		cur_config = new Configuration();
		cur_config.locale = cur_locale;
		getBaseContext().getResources().updateConfiguration(cur_config, getBaseContext().getResources().getDisplayMetrics());
	}

	void set_cur_locale (String curr_lang) {

		if (curr_lang.equals(language_list[1])) {  // English

			cur_locale = new Locale("");
			/*Locale.setDefault(cur_locale);
			cur_config = new Configuration();
			cur_config.locale = cur_locale;
			getBaseContext().getResources().updateConfiguration(cur_config, getBaseContext().getResources().getDisplayMetrics());*/

		} else if (curr_lang.equals(language_list[3])) {   // Spanish

			cur_locale = new Locale("sp");
			/*Locale.setDefault(cur_locale);
			cur_config = new Configuration();
			cur_config.locale = cur_locale;
			getBaseContext().getResources().updateConfiguration(cur_config, getBaseContext().getResources().getDisplayMetrics());*/

		} else if (curr_lang.equals(language_list[2])) {  // Portuguese; added on 8/20/2012
			cur_locale = new Locale("pt");
		} else if (curr_lang.equals(language_list[0])) {   // Arabic
			cur_locale = new Locale("ar");
		} else if (curr_lang.equals(language_list[4])) {   // Japanese
			cur_locale = new Locale("jp");
		} else {
        }

		Locale.setDefault(cur_locale);
		cur_config = new Configuration();
		cur_config.locale = cur_locale;
		getBaseContext().getResources().updateConfiguration(cur_config, getBaseContext().getResources().getDisplayMetrics());
	}

	public void initialize_basic_features() {

		// getting resources
		res = getResources();

		cancelb = (Button) findViewById(R.id.cancelb);
		okb     = (Button) findViewById(R.id.okb);
		shareb  = (Button) findViewById(R.id.shareb);

		unameet = (EditText)findViewById(R.id.usernametext02);
		uri     = (EditText)findViewById(R.id.uri);

		Bundle bundle = getIntent().getExtras();
		String server_uri  = bundle.getString("URI");
		if (server_uri != null && server_uri.length() > 0)
		{
			uri.setText(server_uri);
			uri.setEnabled(false);
		}

	}

	public void set_adaptor () {

		// making adaptor
		langlist = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		langlist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		language_list = res.getStringArray(R.array.language_list);

		for(int i = 0 ; i < language_list.length ; i++) {
			langlist.add(language_list[i]);
		}

	}

	public void set_language(String r_language) {
		cancelb.setText(R.string.login_reset);
		okb.setText(R.string.login);
	}

	public void select_language() {

		// Define the spinner
		final Spinner spin = (Spinner) findViewById(R.id.select_language);
		spin.setAdapter(langlist);
		spin.setSelection(1);  // default language English; added on 8/20/2012

		// Choose a Language
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View v,int position, long id) {

				chosen_language = (String) spin.getSelectedItem();
				set_cur_locale(chosen_language);

				//set the button
				cancelb.setText(R.string.login_reset);
				okb.setText(R.string.login);

        		Setup_Eng();

        	}

        	public void onNothingSelected(AdapterView<?> parent) {
        		if (show_systemout) System.out.println("Nothing selected.");
            }
        });


	}

	private void Setup_Eng() {

    	susername    = unameet.getText().toString();
		suri         = uri.getText().toString();

	    if ((susername == null) || (susername.equals("")) || susername.startsWith(getString(R.string.default_user_com_name)) ) {

	    	String IP = this.get_IP(); // my IP

	    	if (IP != null){
        		unameet.setText(getString(R.string.default_user_name) + IP.substring(IP.lastIndexOf(".")));
        		susername    = unameet.getText().toString();

        	} else { // no network connected

        		Toast.makeText(smile.this,R.string.no_network, Toast.LENGTH_LONG).show();
        	}
        }

         okb.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				susername    = unameet.getText().toString();
				suri         = uri.getText().toString();

				index_start = check_username();

				if(index_start == false) {

					Builder adb = new AlertDialog.Builder(smile.this);
					adb.setTitle(R.string.warn);
					adb.setMessage(R.string.insert_name);
					adb.setPositiveButton(R.string.OK, null);
					adb.show();

				} else {

					Intent courselist = new Intent(getBaseContext(), CourseList.class);
					//Next create the bundle and initialize it
					Bundle bundle = new Bundle();

					//Add the parameters to bundle as
					bundle.putString("USERNAME",susername);
					bundle.putString("URI", suri);
					bundle.putString("CHOSEN_LANGUAGE", chosen_language);

					//Add this bundle to the intent
					courselist.putExtras(bundle);

					try {
						startActivity(courselist);

					} catch (Exception e) {
						if (show_systemout) System.out.println("Error in starting program");
					}
				}
    		}
    	});

		cancelb.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//clear all information

				if (unameet == null) {
					// do nothing
				} else {

					unameet.setText("");

                }
			}
          });

        shareb.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//share SMILE server IP information
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String shareBody = getString(R.string.default_share_scheme) + "://" + getString(R.string.default_share_host) +
				                   getString(R.string.default_share_pathPrefix) + uri.getText().toString();
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share SMILE");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, "Share via"));
			}
          });
    }

    private boolean check_username() {

		boolean return_value = true;

		susername = unameet.getText().toString();
		uri     = (EditText)findViewById(R.id.uri);

		if(susername.equals("")) return_value = false;
		else return_value = true;

		if (return_value) {
            try {
			    setTitle(susername + "@SMILE Student v" + getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName);
            } catch(NameNotFoundException nnfe) {
            }
		}
		return return_value;

	}

	// receive IP information
	private  String get_IP() {

	  	try {

	  		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {

				NetworkInterface ni = e.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					String ipa =  ips.nextElement().toString();

					if (ipa.startsWith("/"))
						ipa = ipa.substring(1);

					if (ipa.indexOf(':') >= 0) {  // IPv6. Ignore
						continue;
					}
					if (ipa.equals("127.0.0.1")) {
						continue;		// loopback MY_IP. Not meaningful for out purpose
					}
					return ipa;
				}
			}

		} catch (SocketException e) {

			e.printStackTrace();
		}

		return null;

	}

    public void onPause() {	super.onPause(); }
    public void onStop()  { super.onStop();  }

}
