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
  File Name: intro.java
  Modified Time: 08.03.2012
======================================================================================*/


package org.smilec.smile.student;

import java.util.List;

import org.smilec.smile.student.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;

public class intro extends Activity {
    
	protected boolean _active = true;
    protected int _splashTime = 2000;
    protected Uri server_data;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        server_data = getIntent().getData();
        //String scheme = server_data.getScheme(); // "http"
        //String host = server_data.getHost(); // "twitter.com"
        
        // thread for displaying the SplashScreen
        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                	
                    finish();
                    Intent smile = new Intent(getBaseContext(), smile.class);
                    String first = ""; 
                    String server_ip = ""; 
                    Bundle bundle = new Bundle();  
                    
                    if(server_data != null)
                    {
                       List<String> params = server_data.getPathSegments();
                    
                       if(params != null && params.size()==2)
                       {
                          first = params.get(0); // "smile"
                          server_ip = params.get(1); // "192.168.2.4"
                       }
                                           					    
                    }
                    
                      //Add the Server IP to the bundle as
					bundle.putString("URI", server_ip);
					smile.putExtras(bundle);
                    startActivity(smile);                    
                }
            }
        };
        splashTread.start();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _active = false;
        }
        return true;
    }
}