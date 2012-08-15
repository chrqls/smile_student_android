package org.smilecon.smile.student;

import java.net.URI;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class JunctionStudent extends JunctionActor {
	
	String APP_TAG = "junction_student";
	CourseList main;
	XMPPSwitchboardConfig config;
	boolean is_connected = false;
	String curr_name;
	String IP;
	final static String ROLE = "student";

	public JunctionStudent(CourseList _main, String name, String[] roles)
	{  // not used

		super(roles);
		main = _main;
		curr_name = name;
		IP =  roles[1];
		Log.d(APP_TAG, "IP = " + IP);
		
	}
	public JunctionStudent(CourseList _main, String name, String _IP)
	{
		super("student");
		main = _main;
		curr_name = name;
		if (_IP == null)
			IP = "000.000.000." + name;
		else 
			IP = _IP;
	}

	
	public boolean create_connection(String ip_addr)
	{
		System.out.println("connecting2:" + ip_addr);
 
   		URI JX_URI = null; 
   		
  		try {
  			JX_URI = new URI("junction://"+ip_addr+"/junction_quiz_ver_0001");
  		} catch (Exception e) {
    		System.out.println(e);
    		return false;
  		}
  		
   		try {
  			config = new XMPPSwitchboardConfig(ip_addr);
  	
  			AndroidJunctionMaker jm1 = AndroidJunctionMaker.getInstance(config);
  			jm1.newJunction(JX_URI, this);

  		} catch (Exception e) {
  			System.out.println("Error:" + e);
  			return false;
  		}
  		
  		Log.i(APP_TAG, "connected:"+ip_addr);
  		is_connected = true;
  		return true;
 	}
	
	synchronized public void send_initial_message() 	// send to teacher after joining the quiz
	{
		JSONObject reply = new JSONObject();
		try {
			Log.i(APP_TAG, "ID = " + getActorID());				
			reply.put("TYPE", "HAIL");
			reply.put("NAME", curr_name);
			//reply.put("IP", IP);
			System.out.println(reply);
			//sendMessageToRole("teacher", reply);
			this.sendMessageToSession(reply);
		
		} catch (Exception e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
	
	public void post_question_to_teacher(
			String q, String o1, String o2, String o3, String o4, String a)
	{
		JSONObject reply = new JSONObject();
		try {
			reply.put("TYPE", "QUESTION");
			reply.put("NAME", curr_name);	
			reply.put("Q", q);
			reply.put("O1", o1);			
			reply.put("O2", o2);			
			reply.put("O3", o3);	
			reply.put("O4", o4);
			reply.put("A", a);			
			
			sendMessageToRole("teacher", reply);
			
			Log.i(APP_TAG,"Sending Qustion"+q +" Righ Answer =  " + a);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(APP_TAG,"Exception:question sending error");
		}
	}
	
	public void post_question_to_teacher_picture(
			String q, String o1, String o2, String o3, String o4, String a, byte[] jpeg)
	{
		JSONObject reply = new JSONObject();
		try {
			reply.put("TYPE", "QUESTION_PIC");
			reply.put("NAME", curr_name);	
			reply.put("Q", q);
			reply.put("O1", o1);			
			reply.put("O2", o2);			
			reply.put("O3", o3);	
			reply.put("O4", o4);
			reply.put("A", a);			
			
			// change binary into string
		
			String encoded_jpg = Base64.encodeBytes( jpeg );
			reply.put("PIC", encoded_jpg);
			sendMessageToRole("teacher", reply);
			
			Log.i(APP_TAG,"Sending Qustion"+q +" Righ Answer =  " + a);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.i(APP_TAG,"Exception");
		}
	}	
	
	public Vector<Integer> jsonarraytovector (JSONArray _arrayname) {
		
		Vector<Integer> vectorarray = null;
		vectorarray = new Vector<Integer> ();
		
		for(int i= 0 ; i < _arrayname.length(); i++) {
			try {
				if(_arrayname.get(i) == null) vectorarray.set(i,0);
				else vectorarray.set(i,(Integer) _arrayname.get(i));
			} catch (JSONException e) {
				Log.i(APP_TAG, "Transition error: JSONArray to Vector");
				e.printStackTrace();
			}
		}
		
		return vectorarray;
	}
	
	public JSONArray vectortojsonarray (Vector<Integer> _arrayname) {
		
		JSONArray jsonarray = new JSONArray();
				
		for(int i= 0 ; i < _arrayname.size(); i++) {
			try {
				if(_arrayname.get(i) == null) jsonarray.put(i,0);
				else jsonarray.put(i,_arrayname.get(i));
												
			} catch (JSONException e) {
				Log.i(APP_TAG, "Transition error: Vector to JSONArray");
				e.printStackTrace();
			}
		}
		
		return jsonarray;
	}
	
	public void submit_answer_to_teacher(Vector<Integer> _answer, Vector<Integer> _rating) {
		
		JSONObject reply = new JSONObject();
		JSONArray submit_answer = new JSONArray();
		JSONArray submit_rating = new JSONArray();
					
		submit_answer = vectortojsonarray(_answer);
		submit_rating = vectortojsonarray(_rating);
		
		
		try {
			reply.put("TYPE", "ANSWER");
			reply.put("NAME", curr_name);	
			reply.put("MYANSWER", submit_answer);
			reply.put("MYRATING", submit_rating);
			
			sendMessageToRole("teacher", reply);
			Log.i(APP_TAG,"Submitting my answer and rating");
			
		} catch (JSONException e) {
			e.printStackTrace();
			Log.i(APP_TAG, "submit error");
		}
					
	}
	
	// when an application is finished, it should be called.
	public void clean_up() {
		if (is_connected) {
			getJunction().disconnect();
		}
		
		is_connected = false;
	}
	
	
	synchronized public void onMessageReceived(MessageHeader arg0, JSONObject arg1) {
		// TODO Auto-generated method stub
		
		//Log.d(APP_TAG, "received Something:" + arg0 + ":"+arg1);
		
		try {
			String type = arg1.getString("TYPE");

			if (type.equals("QUERY"))
			{
				String state = arg1.getString("STATE");		// INIT, MAKE, SOLVE, SHOW
				Integer seq = arg1.getInt("SEQ");
				
				// send message to teacher
				JSONObject reply = new JSONObject();
				reply.put("TYPE", "REPLY");
				reply.put("NAME", curr_name); 
				reply.put("SEQ", seq);
				sendMessageToRole("teacher", reply);
				Log.d(APP_TAG, "REPLY"+ seq);

				// query message gives the current state. If I'm out of phase, change state
				
				int curr_stat = main.getCurrentState();
				if ((state.equals("MAKE")    && (curr_stat != CourseList.MAKE_QUESTION) 
				    		                 && (curr_stat != CourseList.WAIT_SOLVE_QUESTION)) ||
				    (state.equals("SOLVE")   && (curr_stat != CourseList.SOLVE_QUESTION) 
   		                 					 && (curr_stat != CourseList.WAIT_SEE_RESULT)) ||
   		            (state.equals("SHOW")    && (curr_stat != CourseList.SEE_RESULT) 
   	   		              					 && (curr_stat != CourseList.FINISH)))	
				{
					// I must have lost synch. So send initial message again, to get 're-initialized'
					send_initial_message();
				}
				
			} else if (type.equals("START_MAKE")) {
				
				System.out.println("START_MAKE");
				Log.i(APP_TAG, "START_MAKE RECEIVED");
				
				if (main.getCurrentState()== CourseList.INIT_WAIT)
					main.setNewStateFromTeacher(CourseList.MAKE_QUESTION);

			} else if (type.equals("RESEND_MAKE")) {
				if (main.getCurrentState()!= CourseList.INIT_WAIT) {
					return;  // I'm not the new guy 
				}
				
				Log.d(APP_TAG, "NAME = " + arg1.getString("NAME"));
				
				if (main.getMyName().equals(arg1.getString("NAME"))) {
				//Okay I'm the new guy
				// check if I've already made the question
					if (arg1.getString("MADE").equals("Y"))
						main.setNewStateFromTeacher(CourseList.WAIT_SOLVE_QUESTION);
					else
						main.setNewStateFromTeacher(CourseList.MAKE_QUESTION); // now make Q
				}
			    
			} else if (type.equals("START_SOLVE")) {
				
				int time_limit    = arg1.getInt("TIME_LIMIT");
				int num_questions = arg1.getInt("NUMQ");
				JSONArray ranswer = new JSONArray();
				ranswer = arg1.getJSONArray("RANSWER");
				System.out.println("START_SOLVE");
				
				main.setTimer(time_limit);
				main.setNumOfScene(num_questions);
				main.setRightAnswers(ranswer, num_questions);
				main.setNewStateFromTeacher(CourseList.SOLVE_QUESTION);
				
			} else if (type.equals("RESEND_SOLVE")) {

				if (main.getCurrentState()!= CourseList.INIT_WAIT) {
					return;  // I'm not the new guy 
				}
				if (main.getMyName().equals(arg1.getString("NAME"))) {
					//Okay I'm the new guy
					// check if I've already made the question
						
					int time_limit    = arg1.getInt("TIME_LIMIT");
					int num_questions = arg1.getInt("NUMQ");
					JSONArray ranswer = arg1.getJSONArray("RANSWER");
					System.out.println("START_SOLVE");
				
					main.setTimer(time_limit);
					main.setNumOfScene(num_questions);
					
					main.setRightAnswers(ranswer, num_questions);

					if (arg1.getString("SOLVED").equals("Y")) {
						main.setNewStateFromTeacher(CourseList.WAIT_SEE_RESULT);
						JSONArray saved_answers = arg1.getJSONArray("YOUR_ANSWERS");
					
						main.initialize_AnswerRatingArray_withDummyValues();
						main.restoreSavedAnswers(saved_answers, num_questions);
					
					}
					else {		
						main.setNewStateFromTeacher(CourseList.SOLVE_QUESTION); // now solve questions
					}
				}
				
			} else if (type.equals("START_SHOW")) {
				
				JSONArray winscore  = new JSONArray();
				JSONArray winrating = new JSONArray();
												
				winscore  = arg1.getJSONArray("WINSCORE");
				winrating = arg1.getJSONArray("WINRATING");
				
				int high_score = arg1.getInt("HIGHSCORE");
				float high_rating = (float) arg1.getDouble("HIGHRATING");
								
				System.out.println("high_score: "+ high_score);
				System.out.println("high_rating: "+ high_rating);
				
				System.out.println("START_SHOW_RESULT");
				
				main.setWinScore(winscore, high_score);
				System.out.println("set win score");
				main.setWinRating(winrating, high_rating);
				main.setNewStateFromTeacher(CourseList.SEE_RESULT);
			} else if (type.equals("RESEND_SHOW"))	{
				if (main.getCurrentState()!= CourseList.INIT_WAIT) {
					return;  // I'm not the new guy 
				}
				if (main.getMyName().equals(arg1.getString("NAME"))) {
					//Okay I'm the new guy
					
					// set my answers/correct answers
					int num_questions = arg1.getInt("NUMQ");
					JSONArray ranswer = arg1.getJSONArray("RANSWER");
					System.out.println("START_SOLVE");
				
					main.setNumOfScene(num_questions);
					main.setRightAnswers(ranswer, num_questions);
					JSONArray saved_answers = arg1.getJSONArray("YOUR_ANSWERS");
					
					main.initialize_AnswerRatingArray_withDummyValues();
					main.restoreSavedAnswers(saved_answers, num_questions);					


					// set total status
					JSONArray winscore  = new JSONArray();
					JSONArray winrating = new JSONArray();
													
					winscore  = arg1.getJSONArray("WINSCORE");
					winrating = arg1.getJSONArray("WINRATING");
					
					int high_score = arg1.getInt("HIGHSCORE");
					float high_rating = (float) arg1.getDouble("HIGHRATING");
									
					System.out.println("high_score: "+ high_score);
					System.out.println("high_rating: "+ high_rating);
					
					System.out.println("START_SHOW_RESULT");
					
					main.setWinScore(winscore, high_score);
					System.out.println("set win score");
					main.setWinRating(winrating, high_rating);
					main.setNewStateFromTeacher(CourseList.SEE_RESULT);
					
				}		
			}
															
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in MSG "+ e);
		}	
				
	}

}
