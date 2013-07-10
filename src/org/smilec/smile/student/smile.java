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

/*===================================================================================
  Developed by Sunmi Seol
  File Name: smile.java
  Modified Time: 08.03.2012
======================================================================================*/


package org.smilec.smile.student;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;

//import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.view.ViewGroup;
import android.view.Gravity;

import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.smilec.smile.student.R;

public class smile extends Activity {
    
	/** Called when the activity is first created. */
	Button   cancelb;
	Button   okb;
	Button   shareb;
	Button   shareb2;
	EditText unameet;
	EditText uri;
	TextView version_text;
    Spinner spin;
	
	static Typeface face=null;
	static String susername;
	static String suri;
	static String server_uri; 
	static String[] language_list;
	static String chosen_language = "English"; // default
	boolean index_start = true; // if true, activity starts
	
	int ACTIVITY_OK;
	
	//ArrayAdapter<String> langlist;
	ArrayAdapter<CharSequence> langlist;
		
	Resources 		res;
	Locale 			cur_locale;
	Configuration 	cur_config;
	String last_default = "";
	
	// false: no system out, true: system out 
	boolean show_systemout = true;
	
	FindServerTask findServerTask = null;
	
	// Start point
	@Override
	public void onCreate(Bundle savedInstanceState) {		
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);
        set_init_locale(); 
        initialize_basic_features();
        set_adaptor();        
        select_language(1);    
        readSettings(); 
    }
	
	void set_init_locale(){ //English
		
		cur_locale = new Locale("");
		Locale.setDefault(cur_locale);
		cur_config = new Configuration();
		cur_config.locale = cur_locale;
		getBaseContext().getResources().updateConfiguration(cur_config, getBaseContext().getResources().getDisplayMetrics());
	}
	
	void set_cur_locale (String curr_lang) {
		
		last_default = getString(R.string.default_user_name);
		face=null;
		
		if (curr_lang.equals(language_list[0])) {   // Arabic			
			cur_locale = new Locale("ar");			
		} else if (curr_lang.equals(language_list[1])) {  // English			
			cur_locale = new Locale("");	
		} else if (curr_lang.equals(language_list[2])) {  // Gujarati (India); added on 3//4/2013
			cur_locale = new Locale("gu");	
			face=Typeface.createFromAsset(getAssets(), "fonts/shruti.ttf");		
		} else if (curr_lang.equals(language_list[3])) {  // Hindi (India)			
			cur_locale = new Locale("hi");	
			face=Typeface.createFromAsset(getAssets(), "fonts/mangal.ttf");		
		} else if (curr_lang.equals(language_list[4])) {  // Malayalam (India)			
			cur_locale = new Locale("ma");	
			face=Typeface.createFromAsset(getAssets(), "fonts/kartika.ttf");		
		} else if (curr_lang.equals(language_list[5])) {  // Portuguese 		
			cur_locale = new Locale("pt");						
		} else if (curr_lang.equals(language_list[6])) {   // Spanish			
			cur_locale = new Locale("sp");					
		} else if (curr_lang.equals(language_list[7])) {   // Swahili			
			cur_locale = new Locale("sw");		
		} else if (curr_lang.equals(language_list[8])) {  // Hindi-Tamil			
			cur_locale = new Locale("ta");	
		    face=Typeface.createFromAsset(getAssets(), "fonts/latha.ttf");
		} else if (curr_lang.equals(language_list[9])) {   // Thai			
			cur_locale = new Locale("th");			       
		} else if (curr_lang.equals(language_list[10])) {   // Urdu			
			cur_locale = new Locale("ur");			       
		}
				
		if(face != null)
			overrideFonts(this, findViewById(android.R.id.content));
		
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
	    if ((getResources().getConfiguration().screenLayout &      
				Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {     // 480x640 dp units
			shareb2 = null;
		}
		else 
		   shareb2  = (Button) findViewById(R.id.shareb2);
		
	
		unameet = (EditText)findViewById(R.id.usernametext02);
		uri     = (EditText)findViewById(R.id.uri);
		
		/*Bundle bundle = getIntent().getExtras();    
		String server_uri  = bundle.getString("URI");
		if (server_uri != null && server_uri.length() > 0)
		{
			uri.setText(server_uri);
			uri.setEnabled(false);
		}*/
		
		version_text = (TextView)findViewById(R.id.version);
									
	}
	
	public void set_adaptor () {
		
		// making adaptor
		//langlist = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		//langlist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		langlist = ArrayAdapter.createFromResource(
	            this, R.array.language_list, R.layout.spinner_layout);
		langlist.setDropDownViewResource(R.layout.spinner_layout);
	
		language_list = res.getStringArray(R.array.language_list);

		/*for(int i = 0 ; i < language_list.length ; i++) {
			langlist.add(language_list[i]);
		}*/
		
	}
	
	public void select_language(int i) {
		
		// Define the spinner
	    spin = (Spinner) findViewById(R.id.select_language);
		spin.setAdapter(langlist);
		spin.setSelection(i);  // default language English; added on 8/20/2012
		
		// Choose a Language
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {
        	
			public void onItemSelected(AdapterView<?> parent, View v,int position, long id) {					
				
				chosen_language = (String) spin.getSelectedItem();
				
				if (chosen_language.equals(language_list[4])||  // Malayalam (India)		
					chosen_language.equals(language_list[10])) {  // Urdu
					if(shareb2 != null)
					{
						shareb.setVisibility(View.INVISIBLE); 
						shareb2.setVisibility(View.VISIBLE);	
					}
				}
				else
				{
					if(shareb2 != null)
					{
						shareb.setVisibility(View.VISIBLE); 
						shareb2.setVisibility(View.INVISIBLE);
					}
				}
							    	   
				set_cur_locale(chosen_language);
        		
				//set the button
				cancelb.setText(R.string.login_reset);
				okb.setText(R.string.login);
				shareb.setText(R.string.login_share);
				if(shareb2 != null)
					shareb2.setText(R.string.login_share);
				version_text.setText(R.string.version);
				   			
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
		
	    if ((susername == null) || (susername.equals("")) || susername.startsWith(last_default) ) {
        	
	    	String IP = this.get_IP(); // my IP
        	
	    	if (IP != null){
        		unameet.setText(getString(R.string.default_user_name) + IP.substring(IP.lastIndexOf(".")));
        		susername    = unameet.getText().toString();
        	
        	} else { // no network connected
        		
        		//Toast.makeText(smile.this,R.string.no_network, Toast.LENGTH_LONG).show();       
        		showToast(getString(R.string.no_network));
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
					writeSettings();
					//uploadErrorLog(suri);
					
					if(findServerTask != null)
						findServerTask.stopFinding();
						
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
				/*Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				String shareBody = getString(R.string.default_share_scheme) + "://" + getString(R.string.default_share_host) + 
				                   getString(R.string.default_share_pathPrefix) + uri.getText().toString();
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share SMILE");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, "Share via"));*/
				
				if(findServerTask != null)
					findServerTask.stopFinding();
				
				findServerTask = new FindServerTask();
				findServerTask.execute(getString(R.string.login_share_msg));
			}
          }); 
        
        if(shareb2 != null)
        {
	        shareb2.setOnClickListener(new View.OnClickListener() {
	    		
				public void onClick(View v) { 
								
					if(findServerTask != null)
						findServerTask.stopFinding();
					
					findServerTask = new FindServerTask();
					findServerTask.execute(getString(R.string.login_share_msg));
				}
	          }); 
        }
    }
        
    public int getResponseCode(String urlString) throws MalformedURLException, IOException {
    	URL u = new URL ( urlString );
    	HttpURLConnection huc =  ( HttpURLConnection )  u.openConnection (); 
    	huc.setRequestMethod ("HEAD"); // OR huc.setRequestMethod ("GET");   
    	huc.connect () ; 
        return huc.getResponseCode();
    }
    
    private boolean check_username() {
		
		boolean return_value = true;
		
		susername = unameet.getText().toString();
		uri     = (EditText)findViewById(R.id.uri);
		
		if(susername.equals("")) return_value = false;
		else return_value = true;
		
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
    
    private class FindServerTask extends AsyncTask<String, String, String> {
    	MulticastSocket socket = null;
    	
        protected String doInBackground(String... msg) {
        	String received="";            
    	    InetAddress group;
    	    DatagramPacket packet; 
    	    socket = null;
    	    
    	    publishProgress(msg[0]);
    	    
	        try {
	        		// Get the Multicast Lock
	            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	            if(wifi != null)
	            {
	            	MulticastLock mcLock = wifi.createMulticastLock("myLock");
	                mcLock.acquire();
	            	            
	                byte[] buf = new byte[256];
	                socket = new MulticastSocket(4445);
	                group = InetAddress.getByName("224.0.0.251");
	                socket.joinGroup(group);
    	        
	                packet = new DatagramPacket(buf, buf.length);
	                socket.receive(packet);                
	                received = new String(packet.getData(), 0, packet.getLength());

	                socket.leaveGroup(group);  
	                socket.close();
                
                    // Release the lock
                    // Release the Lock to save battery power
                    if(mcLock.isHeld())
                    {
                        mcLock.release();
                    }
	            }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return received;
        }

        protected void onProgressUpdate(String... msg) {
        	//Toast.makeText(smile.this, msg[0], Toast.LENGTH_LONG).show();
        	showToast(msg[0]);
        }

        protected void onPostExecute(String result) {
        	if (result != null && result.length() > 0)
    		{
        		uri.setText(result);
        		uri.setEnabled(false);
    		}
        }
        
        protected void stopFinding(){ 
  	       if (socket != null)
  	    	   socket.close();
  	    }
    }
    
    public void showToast(String msg)
    {
    	LayoutInflater inflater = getLayoutInflater();
    	View layout = inflater.inflate(R.layout.toast_layout,
    	                               (ViewGroup) findViewById(R.id.toast_layout_root));

    	
    	TextView text = (TextView) layout.findViewById(R.id.text);
    	if(face != null)
    	   text.setTypeface(face);
    	text.setText(msg);
     
    	Toast toast = new Toast(getApplicationContext());
    	toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 150);
    	toast.setDuration(Toast.LENGTH_LONG);
    	toast.setView(layout);
    	toast.show();   	
    }
    
    private void writeSettings()
    {
        try {
    	  // open myfilename.txt for writing
    	  OutputStreamWriter out = new OutputStreamWriter(openFileOutput("smileSettings.txt",0));
    	  // write the contents on mySettings to the file
    	  out.write(susername);  		// username
    	  out.write("\n");  
    	  out.write(suri);      	    // host ip
    	  out.write("\n");  
    	  out.write(chosen_language);   // language
    	  // close the file
    	  out.close();  
 
    	} catch (java.io.IOException e) {
    	  //do something if an IOException occurs.
    	}
    }
    
    private void readSettings()
    {    	   	
    	try {
    	    // open the file for reading
    	    InputStream instream = openFileInput("smileSettings.txt");
    	 
    	    // if file the available for reading
    	    if (instream != null) {
    	      // prepare the file for reading
    	      InputStreamReader inputreader = new InputStreamReader(instream);
    	      BufferedReader buffreader = new BufferedReader(inputreader);
    	                 
    	      String line;
    	      int i=0;
    	      // read every line of the file into the line-variable, on line at the time
    	      
    	      while (( line = buffreader.readLine()) != null) {
    	        // do something with the settings from the file
    	    	  line = line.trim();
    	    	  
    	    	  if(i==0)
    	    	  {
    	    		  //Toast.makeText(smile.this, line, Toast.LENGTH_LONG).show();  
    	    		  unameet.setText(line);
    	    	  }
    	    	  else if(i==1)
    	    	  {   	    		  
    	    		  //Toast.makeText(smile.this, line, Toast.LENGTH_LONG).show();  
    	    		  uri.setText(line);
    	    	  } 
    	    	  else if (i==2)
    	    	  {    	    		  
    	    		  //Toast.makeText(smile.this, line, Toast.LENGTH_LONG).show();  
    	    		  Spinner spin = (Spinner) findViewById(R.id.select_language);
    	    		  
    	    		  for(int j=0; j<language_list.length; j++)
    	    		     if(language_list[j].equals(line))
    	    		     {
    	    		    	 spin.setSelection(j);  		    	
    	    		     }   	    		  
    	    	  }
    	    	  
    	    	  i++;
    	      }
    	 
    	    }
    	     
    	    // close the file again       
    	    instream.close();
    	} catch (java.io.FileNotFoundException e) {
    	    // do something if the myfilename.txt does not exits
    	} catch (java.io.IOException e) {
	  	  //do something if an IOException occurs.
	  	}      	    
    }
    
    public static void overrideFonts(final Context context, final View v) { 
    	//int currentapiVersion = android.os.Build.VERSION.SDK_INT;
    	//int ICE_CREAM_SANDWICH=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    	//System.out.println("currentapiVersion: " + currentapiVersion + ", ICE_CREAM_SANDWICH: "+ ICE_CREAM_SANDWICH);
    	if(face != null)
    	{
	        try {
	            if (v instanceof ViewGroup) {
	                ViewGroup vg = (ViewGroup) v;
	                for (int i = 0; i < vg.getChildCount(); i++) {
	                    View child = vg.getChildAt(i);
	                    overrideFonts(context, child);
	                }
	            } else if (v instanceof TextView) {
	                ((TextView)v).setTypeface(face);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            // ignore
	        }
    	}
    }
}