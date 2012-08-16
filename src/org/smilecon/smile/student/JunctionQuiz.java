package org.smilecon.smile.student;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.w3c.dom.Text;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class JunctionQuiz extends Activity {
    
	/** Called when the activity is first created. */
	//ImageView smile_logo;
	private Button cancelb;
	private Button okb;
	private EditText unameet;
	private EditText pwordet;
	private EditText uri;
	private TextView version_text;
	//ImageView stanford_logo;
			
	static String susername;
	static String susername_op;
	static String suri;
	static String server_uri="192.168.2.4"; // hard-coding
	
	boolean index_start = true; // if true, activity starts
	int ACTIVITY_OK;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //smile_logo    = (ImageView)findViewById(R.id.ismile_logo);
    	//stanford_logo = (ImageView)findViewById(R.id.istanford_logo);
            
        SetLogin();
    }
	
	private boolean check_username() {
		
		boolean return_value = true;
		
		susername = unameet.getText().toString();
		pwordet = (EditText)findViewById(R.id.passwordtext02);
		uri     = (EditText)findViewById(R.id.uri);
		
		if(susername.equals("")) return_value = false;
		else return_value = true;
		
		return return_value;
		
	}
    
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
    private void SetLogin() {
    	
    	cancelb = (Button) findViewById(R.id.cancelb);
		okb     = (Button) findViewById(R.id.okb);
		
		unameet = (EditText)findViewById(R.id.usernametext02);
		pwordet = (EditText)findViewById(R.id.passwordtext02);
		uri     = (EditText)findViewById(R.id.uri);
		
		// Version name
		version_text = (TextView)findViewById(R.id.version);
		version_text.setText(R.string.version);
				
		susername    = unameet.getText().toString();
		susername_op = pwordet.getText().toString();
        suri         = uri.getText().toString();
        
        if ((susername==null) || (susername.equals(""))) {
        	String IP = this.get_IP(); // my IP
        	if (IP != null){
        		unameet.setText("default" + IP.substring(IP.lastIndexOf(".")));
        		susername    = unameet.getText().toString();
        	}
        }
        
         okb.setOnClickListener(new View.OnClickListener() {
    		        	
			public void onClick(View v) { 
				
				susername    = unameet.getText().toString();
				susername_op = pwordet.getText().toString();
		        suri         = uri.getText().toString();				
	
				index_start = check_username();
				
				if(index_start == false) {
				
					Builder adb = new AlertDialog.Builder(JunctionQuiz.this);
					adb.setTitle("Warning!");
					adb.setMessage("Insert username.");
					adb.setPositiveButton("OK", null);
					adb.show();
					
				} else {
					
					Intent courselist = new Intent(getBaseContext(), CourseList.class);
					//Next create the bundle and initialize it
					Bundle bundle = new Bundle();
				
					//Add the parameters to bundle as
					bundle.putString("USERNAME",susername);
					bundle.putString("URI", suri);
					bundle.putString("NAMEOP", susername_op);
									
					//Add this bundle to the intent
					courselist.putExtras(bundle);
				
					try {
						startActivity(courselist);
						//startActivityForResult(courselist, ACTIVITY_OK);
					
					} catch (Exception e) { 
						System.out.println(" Error in starting program");
					}
				} 
    		}
    	});
		
		cancelb.setOnClickListener(new View.OnClickListener() {
    		
			public void onClick(View v) { 
				//clear all information
				
				if ((unameet == null) || (pwordet == null)) { 
					// do nothing
				} else { 
                                 
					// Clear entries in price and units text boxes 
					unameet.setText("");
                    unameet.setHint("Username");
                    pwordet.setText(""); 
                    pwordet.setHint("Name(optional)");
                    uri.setText("");
                    uri.setText(server_uri);
                }
			}
          }); 
   			
    }
       
    public void onPause() {	super.onPause(); }
    public void onStop() { super.onStop(); }
          
}