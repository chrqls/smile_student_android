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

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

public class timer {

	String APP_TAG = "TIMER";
	TextView TimeDisplay; //textview to display the countdown
	long minuteUnit;
	long secondUnit;
	
	private CourseList _act;
	
	public timer(CourseList CL) {
		_act = CL;
	}
	
	/** Called when the activity is first created. */
	public void onStart() {
	
		//TimeDisplay = new TextView(this);
		
		new CountDownTimer(5*60*1000, 1000) {
			public void onTick(long millisUntilFinished) {
				TimeDisplay.setText(formatTime(millisUntilFinished));
			}     
			
			public void onFinish() {
			TimeDisplay.setText("Time out!");
			}  
	
	}.start(); 
	}
	
	// formating function
	public String formatTime(long millis) {
		  
		String output = "00:00:00";
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;

		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 60;

		String secondsD = String.valueOf(seconds);
		String minutesD = String.valueOf(minutes);
		String hoursD = String.valueOf(hours); 

		if (seconds < 10)
		    secondsD = "0" + seconds;
		if (minutes < 10)
		    minutesD = "0" + minutes;
		if (hours < 10)
		    hoursD = "0" + hours;

		output = hoursD + " : " + minutesD + " : " + secondsD;
		return output;
		}
}

