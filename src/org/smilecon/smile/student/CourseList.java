package org.smilecon.smile.student;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Vector;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import android.widget.RatingBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebChromeClient;

public class CourseList extends Activity implements OnDismissListener {
	private Random mRandom = new Random();
	String TAG = "MySMILE";
	String server_dir = "/JunctionServerExecution/current/";

	WebView webviewQ;
	WebView webviewSR;
	WebView webviewSRD;
	WebView curwebview;
	WebView webviewPR;
	ImageView imageview;
	private RadioGroup rgb02;
	private RadioGroup rgb03;
	private RadioGroup rgb04; // for making question

	Vector<Integer> answer_arr;
	Vector<Integer> rating_arr; // per each question by individual
	Vector<Integer> right_answer;
	Vector<Integer> my_score;
	Vector<String> score_winner_name;
	Vector<String> rating_winner_name;
	Vector<String> final_avg_ratings; // per each question by all users
	Vector<String> r_answer_percents; // per each question by all users

	static int high_score;
	static float high_rating;

	private String question_arr[] = { "", "", "", "", "", "" };
	int LAST_SCENE_NUM = 0;
	private String category_arr[] = { "Make Your Question", "Solve Questions", "See Results" };
	private String curcategory;

	static int selarridx = 0; // 0:solve question 1: see results
	static int previewidx = 0; // 0:no preview 1:preview
	int issolvequestion = 0; // 0: solve question 1: see results

	static int chkimg = 0; // 0:no image 1: image selected
	static int choice02 = -2;
	static int choice03 = -2;
	static int scene_number;
	static int curusertype = 1;
	static String cururi;
	static String curusername;
	static String curusername_op;
	// boolean connection_created = false;

	String myRightan;

	private boolean isImageSelected = false;

	AddPictureDialog add_pic_d = null;
	piechart draw_piechart;

	TextView notemkcontent;
	TextView notesolcontent;
	TextView noteseecontent;

	String stored_file_url;
	// JunctionStudent student;
	HttpMsgForStudent student;

	// variables regarding on quick_action
	ActionItem image_first;
	ActionItem image_second;
	ActionItem image_third;

	QuickAction image_qa;

	Boolean TakenImage = false;
	Uri imageUri;
	Uri ThumUri;
	Intent camera_intent;
	static Cursor mcursor_for_camera;
	int IMAGECAPTURE_OK = 0;
	String media_path;

	Activity activity;

	// ---------------------------------------------------------------------------------------------------------
	// communication channel from JunctionStudent -> main application
	// JunctionStudent -> main.setNewState -> [MessageHandler] -> handleMessage
	// -> main.change_todo_view_state
	// ---------------------------------------------------------------------------------------------------------
	public void setNewStateFromTeacher(int todo_number) { // called by junction
															// object
		messageHandler.sendMessage(Message.obtain(messageHandler, todo_number));
		// below Handler will be called soon enough with 'todo_number' as
		// msg.what
	}

	public void setTranferStatus(boolean is_send, int time) { // called by
																// junction
																// object
		// 0: finished
		// 1~20: how many time has passed
		if (is_send)
			messageHandler.sendMessage(Message.obtain(messageHandler,
					HTTP_SEND_STATUS, time, 0));
		else
			messageHandler.sendMessage(Message.obtain(messageHandler,
					HTTP_PING_STATUS, time, 0));
		// below Handler will be called soon enough with 'todo_number' as
		// msg.what
	}

	void setHttpStatus(int kind, int time) {
		try {
			TextView http = (TextView) findViewById(R.id.HTTPText);
			if (time == 0)
				http.setText("");
			else {
				StringBuffer sb = new StringBuffer("");
				if (kind == HTTP_PING_STATUS)
					sb.append("ConnectingSever:");
				else
					sb.append("Sending:");

				for (int i = 0; i < time; i++)
					sb.append('.');
				http.setText(sb.toString());
				// Log.d(TAG,"Hey:"+sb.toString());
			}
		} catch (Exception e) {
		}

	}

	private Handler messageHandler = new Handler() {
		public void handleMessage(Message msg) {
			if ((msg.what == HTTP_PING_STATUS)
					|| (msg.what == HTTP_SEND_STATUS)) {
				int time = msg.arg1;
				setHttpStatus(msg.what, time);
			} else {
				change_todo_view_state(msg.what);
			}
		};
	};

	public int getCurrentState() {
		return last_state_received;
	}

	public String getMyName() {
		return this.curusername;
	}

	// States of junction_quiz
	public final static int HTTP_PING_STATUS = -10;
	public final static int HTTP_SEND_STATUS = -20;
	public final static int CONNECT_FAIL = -2;
	public final static int BEFORE_CONNECT = -1;
	public final static int INIT_WAIT = 0;
	public final static int MAKE_QUESTION = 1;
	public final static int WAIT_SOLVE_QUESTION = 2;
	public final static int SOLVE_QUESTION = 3;
	public final static int WAIT_SEE_RESULT = 4;
	public final static int SEE_RESULT = 5;
	public final static int FINISH = 6;

	int last_state_received = BEFORE_CONNECT; // MAKE_QUESTION,
	static String MY_IP = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().requestFeature(Window.FEATURE_PROGRESS);

		// First Extract the bundle from intent
		Bundle bundle = getIntent().getExtras();
		// Next extract the values using the key as
		curusertype = 1; // fixed (student)

		curusername = bundle.getString("USERNAME");
		cururi = bundle.getString("URI");
		curusername_op = bundle.getString("NAMEOP");

		curusername = curusername + curusername_op;
		// Log.d(TAG, "Username: "+ curusername);

		draw_piechart = new piechart(this); // to draw the piechart
		draw_piechart.onStart(100,0);

		answer_arr = new Vector<Integer>();
		rating_arr = new Vector<Integer>();
		my_score = new Vector<Integer>();
		right_answer = new Vector<Integer>();

		score_winner_name = new Vector<String>();
		rating_winner_name = new Vector<String>();
		final_avg_ratings = new Vector<String>();
		r_answer_percents = new Vector<String>();

		// connection_created = false;
		_act = this;

		chkimg = 0; // no image
		image_qa = null;

		student = null; // student communicator is created by make_connection()

		// Adding quick action menu
		image_quickaction();

		MY_IP = get_IP();

		if ((MY_IP == null) || (MY_IP.length() == 0) || (MY_IP=="")) {
			MY_IP = generateFakeIP();
		}
		Toast.makeText(this, "Using IP: " + MY_IP, Toast.LENGTH_SHORT).show();
		getStoredFileURL();
		create_connection();
		show_todo_view();

		// student.send_initial_message();

	}

	private String generateFakeIP() {
		String addr = "127.0.0.";
	 	addr = addr + String.valueOf(mRandom.nextInt(255) + 1);
		return addr;
	}
	
	private void create_connection() {

		Log.d(TAG, "Creating connection");
		student = null;

		Toast.makeText(this, "Creating Connection", Toast.LENGTH_SHORT).show();

		student = new HttpMsgForStudent(this, curusername, MY_IP, this);
		if (!student.beginConnection(cururi)) {
			Toast.makeText(this, "Connection Issue connecting with client IP = " + MY_IP + " to server IP = " + cururi, Toast.LENGTH_SHORT).show();
			// We should return to the login
		}

		// create connection with another thread
		/*
		 * new Thread(new Runnable() { public void run() {
		 * 
		 * int err_count = 0; while (student == null) {
		 * Log.d(TAG,"Creating Student");
		 * 
		 * student = new JunctionStudent((CourseList)_act, curusername, MY_IP);
		 * err_count++;
		 * 
		 * // finish application if (err_count > 2) {
		 * Log.d(TAG,"Too many Error in making connection"); inform.setText(
		 * "Too many Error in making connection, please check your network.");
		 * break; } } if (student == null) { // Connection Fail
		 * setNewStateFromTeacher(CONNECT_FAIL); return; }
		 * 
		 * boolean succ = false; err_count = 0; while (!succ) {
		 * Log.d(TAG,"Creating Connection"); succ =
		 * student.create_connection(cururi);
		 * 
		 * if (err_count++ > 2) {
		 * Log.d(TAG,"Too many Error in making connection");
		 * //inform.setText
		 * ("Too many Error in making connection, please check your network");
		 * break; } } if (!succ) setNewStateFromTeacher(CONNECT_FAIL); else
		 * setNewStateFromTeacher(INIT_WAIT);
		 * 
		 * 
		 * } }).start();
		 */
	}

	private String get_IP() {
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface ni = e.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					String ipa = ips.nextElement().toString();

					if (ipa.startsWith("/"))
						ipa = ipa.substring(1);

					if (ipa.indexOf(':') >= 0) { // IPv6. Ignore
						continue;
					}

					if (ipa.equals("127.0.0.1")) {
						continue; // loopback MY_IP. Not meaningful for out
								  // purpose
					}

					// mp.addIPList(ipa);
					// System.out.println(ipa+",");
					Log.d(TAG, ipa);
					return ipa;
				}
			}

		} catch (SocketException e) {
			Log.e(TAG, "get_IP() can't get an IP address.  ");
			e.printStackTrace();
		}

		return null;
	}

	private void getStoredFileURL() {
		// save URL for stored file
		File data_dir = getBaseContext().getFilesDir();

		try {
			URL url = data_dir.toURL();
			stored_file_url = url.toString();
		} catch (Exception e) {
			Log.d(TAG, "URL ERROR");
		}
	}

	TextView inform;
	Button exitTodoView;
	Button makeQ;
	Button solveQ;
	Button seeR;
	Boolean enabled_m = false;
	Boolean enabled_s = false;
	Boolean enabled_r = false;
	int status_t = 0;

	private void show_todo_view() {

		setTitle(curusername + "@SMILE Student");
		setContentView(R.layout.category);

		inform = (TextView) findViewById(R.id.ProgressText);

		makeQ = (Button) findViewById(R.id.MKQbutton);
		solveQ = (Button) findViewById(R.id.SOLQbutton);
		seeR = (Button) findViewById(R.id.SEERbutton);

		exitTodoView = (Button) findViewById(R.id.exitbutton);

		makeQ.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				change_todo_view_state(WAIT_SOLVE_QUESTION);
				student.can_rest_now();
				MakeQuestion();

			}
		});
		solveQ.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				change_todo_view_state(WAIT_SEE_RESULT);
				student.can_rest_now();
				SolveQuestion();
				selarridx = 0;
			}
		});

		seeR.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				student.can_rest_now();
				seeResults();
				selarridx = 1;
			}
		});

		exitTodoView.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				final Builder adb = new AlertDialog.Builder(CourseList.this);
				adb.setTitle("Warnning!!");
				adb.setMessage("Do you want to exit the program?");
				adb.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								CourseList.this.finish();
							}
						});

				adb.setNegativeButton("Cancel", null);
				adb.show();

				// CourseList.this.finish();
			}
		});

		change_todo_view_state(this.last_state_received); // init_state
	}

	public void onDestroy() {
		if (student != null)
			student.clean_up();
		super.onDestroy();
	}

	private void change_todo_view_state(int state) {

		if (state == CONNECT_FAIL) {
			Toast.makeText(this, "Connection Failed. Please Restart",
					Toast.LENGTH_LONG);
			this.finish(); // finish current activity. Connection lost
			return;
		}

		if (state < last_state_received) { // repeated messag
			Log.d(TAG, "repeated todo status:" + state + ", curr:"
					+ last_state_received);
			return; // cannot go back state.
		}

		Log.d(TAG, "New todo status " + state);
		last_state_received = state;

		switch (state) {
		case BEFORE_CONNECT: // wait
			makeQ.setEnabled(false);
			solveQ.setEnabled(false);
			seeR.setEnabled(false);
			inform.setText("Trying to connect to the server.");
			break;
		case INIT_WAIT: // wait
			// if (!connection_created) {
			// Toast.makeText(this, "Connected to server",
			// Toast.LENGTH_SHORT).show();
			// student.send_initial_message(); // send first message
			// connection_created = true;
			// }
			makeQ.setEnabled(false);
			solveQ.setEnabled(false);
			seeR.setEnabled(false);
			inform.setText("Waiting for others to join the Quiz.");
			break;
		case MAKE_QUESTION: // press button for making
			makeQ.setEnabled(true);
			solveQ.setEnabled(false);
			seeR.setEnabled(false);
			inform.setText("Press the button to start making your question.");
			break;
		case WAIT_SOLVE_QUESTION: // wait solving
			makeQ.setEnabled(false);
			solveQ.setEnabled(false);
			seeR.setEnabled(false);
			inform.setText("Waiting for others to finish their question making.");
			break;
		case SOLVE_QUESTION: // press button for solving
			// clear cache of webview
			makeQ.setEnabled(false);
			solveQ.setEnabled(true);
			seeR.setEnabled(false);
			inform.setText("Press the button to start solving the questions.");
			break;
		case WAIT_SEE_RESULT: // wait seeing results
			makeQ.setEnabled(false);
			solveQ.setEnabled(false);
			seeR.setEnabled(false);
			inform.setText("Waiting for others to finish solving the questions...");
			break;
		case SEE_RESULT: // see results
			makeQ.setEnabled(false);
			solveQ.setEnabled(false);
			seeR.setEnabled(true);
			inform.setText("Press the button to see your results and ratings.");
			break;
		case FINISH: // activity is over but it is possible to see results
			makeQ.setEnabled(false);
			solveQ.setEnabled(false);
			seeR.setEnabled(true);
			inform.setText("Quiz activity is over. Press Exit. "
					+ "(You need to exit first and connect again, if you you want to do another round of Junction-Quiz.)");
		}

	}

	public void setTimer(int _time) {
		Log.d(TAG, "time_limit:" + _time);
		// call timer function
	}

	public void setNumOfScene(int _numscene) {
		LAST_SCENE_NUM = _numscene;
	}

	public void setRightAnswers(JSONArray _array, int _num) {

		// Log.d(TAG, "setRightAnswers");
		Integer temp = 0;

		right_answer = new Vector<Integer>();
		for (int i = 0; i < _array.length(); i++) {
			right_answer.add(i, -1);
		}

		for (int i = 0; i < _num; i++) {
			try {
				temp = _array.getInt(i);
				right_answer.set(i, temp);
			} catch (JSONException e) {
				Log.d(TAG, "Correct answers error: " + e);
			}
		}
	}

	public void restoreSavedAnswers(JSONArray _array, int _num) {
		Integer temp = 0;

		for (int i = 0; i < _num; i++) {
			try {
				temp = _array.getInt(i);
				answer_arr.set(i, temp);
				Log.d(TAG, "Saved Answer:" + temp);
			} catch (JSONException e) {
				Log.d(TAG, "Correct answers error: " + e);
			}
		}
	}

	public void setWinScore(JSONArray _array, int h_score) {

		for (int i = 0; i < _array.length(); i++) {
			try {

				String name = _array.getString(i);
				score_winner_name.add(i, name);

			} catch (JSONException e) {
				Log.d(TAG, "Score Winner Error: " + e);
			}
		}

		high_score = h_score;
	}

	public void setWinRating(JSONArray _array, float h_rating) {

		for (int i = 0; i < _array.length(); i++) {
			try {

				String name = _array.getString(i);
				rating_winner_name.add(i, name);

			} catch (JSONException e) {
				Log.d(TAG, "Rating Winner Error: " + e);
			}

		}
		high_rating = h_rating;
	}

	// It is added in 6/22
	public void setAvgRating(JSONArray _array) {

		for (int i = 0; i < _array.length(); i++) {
			try {

				final_avg_ratings.add(i, _array.getString(i));

			} catch (JSONException e) {
				Log.d(TAG, "Avrage Rating Error: " + e);
			}

		}

	}

	public void setRAPercents(JSONArray _array) {

		for (int i = 0; i < _array.length(); i++) {
			try {

				r_answer_percents.add(i, _array.getString(i));

			} catch (JSONException e) {
				Log.d(TAG, "Percent of right answer Error: " + e);
			}

		}

	}

	Activity _act;
	static String imgURL;
	Bitmap bmImg = null;
	Bitmap _bmImg;
	String myHTMLfile;
	String _content;
	String _op1;
	String _op2;
	String _op3;
	String _op4;
	String _rightan;

	// 1. Make Questions (Revision: 04222011)
	private void MakeQuestion() {

		curcategory = category_arr[0];

		setTitle(curcategory);
		setContentView(R.layout.mkquestion);

		imageview = (ImageView) findViewById(R.id.galleryimg01);

		final EditText myContent = (EditText) findViewById(R.id.mkqContent);
		final EditText myOp1 = (EditText) findViewById(R.id.op1);
		final EditText myOp2 = (EditText) findViewById(R.id.op2);
		final EditText myOp3 = (EditText) findViewById(R.id.op3);
		final EditText myOp4 = (EditText) findViewById(R.id.op4);
		rgb04 = (RadioGroup) findViewById(R.id.rgroup04); // for inserting right
															// answer

		// retain the previous contents for preview
		if (previewidx == 1) {
			myContent.setText(question_arr[0]);
			myOp1.setText(question_arr[1]);
			myOp2.setText(question_arr[2]);
			myOp3.setText(question_arr[3]);
			myOp4.setText(question_arr[4]);
			rgb04.check(Integer.parseInt(question_arr[5]) + R.id.rightan01 - 1);
			imageview.setImageBitmap(_bmImg);
		}

		// Add Image
		ImageButton addimg = (ImageButton) findViewById(R.id.camera01);
		addimg.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				image_qa = new QuickAction(v);

				image_qa.addActionItem(image_first);
				image_qa.addActionItem(image_second);
				image_qa.addActionItem(image_third);

				image_qa.show();
			}

		});

		// save a question made by student (ok = 1, cancel = 2,
		// post+makeQuestion = 3)
		Button post1 = (Button) findViewById(R.id.post01);

		post1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				final Builder adb = new AlertDialog.Builder(CourseList.this);
				adb.setTitle(curcategory);
				adb.setMessage("Do you want to post this question");
				adb.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {

								int checked_rid = rgb04
										.getCheckedRadioButtonId();
								int my_an = checked_rid - R.id.rightan01 + 1;

								if (my_an < 0) {
									Toast.makeText(CourseList.this,
											"ERROR: No Right Answer!!",
											Toast.LENGTH_SHORT).show();
								} else {

									myRightan = Integer.toString(my_an);

									if (bmImg != null) {
										ByteArrayOutputStream jpg = new ByteArrayOutputStream(
												64 * 1024);
										boolean error = bmImg.compress(
												Bitmap.CompressFormat.JPEG,
												100, jpg);

										if (!error)
											Log.d(TAG, "ERROR JPGE");

										// post with picture
										student.post_question_to_teacher_picture(
												myContent.getText().toString(),
												myOp1.getText().toString(),
												myOp2.getText().toString(),
												myOp3.getText().toString(),
												myOp4.getText().toString(),
												myRightan, jpg.toByteArray());

									} else { // post it without picture
										student.post_question_to_teacher(
												myContent.getText().toString(),
												myOp1.getText().toString(),
												myOp2.getText().toString(),
												myOp3.getText().toString(),
												myOp4.getText().toString(),
												myRightan);
									}

									Log.d(TAG, "Posting:"
											+ myContent.getText().toString());
									// after posting question, return the main
									// screen
									if (previewidx == 1) {
										previewidx = 0;
										_bmImg = null;
									}

									show_todo_view();
									selarridx = 0;

								}
							}
						});

				// Cancel to post the question
				adb.setNegativeButton("Cancel", null);

				// Post and make the question
				adb.setNeutralButton("Post+MoreQ",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								int checked_rid = rgb04
										.getCheckedRadioButtonId();
								int my_an = checked_rid - R.id.rightan01 + 1;

								if (my_an < 0) {
									Toast.makeText(CourseList.this,
											"ERROR: No Right Answer!!",
											Toast.LENGTH_SHORT).show();
								} else {

									myRightan = Integer.toString(my_an);

									if (bmImg != null) {
										ByteArrayOutputStream jpg = new ByteArrayOutputStream(
												64 * 1024);
										boolean error = bmImg.compress(
												Bitmap.CompressFormat.JPEG,
												100, jpg);

										if (!error)
											Log.d(TAG, "ERROR JPGE");

										// post with picture
										student.post_question_to_teacher_picture(
												myContent.getText().toString(),
												myOp1.getText().toString(),
												myOp2.getText().toString(),
												myOp3.getText().toString(),
												myOp4.getText().toString(),
												myRightan, jpg.toByteArray());

									} else { // post it without picture
										student.post_question_to_teacher(
												myContent.getText().toString(),
												myOp1.getText().toString(),
												myOp2.getText().toString(),
												myOp3.getText().toString(),
												myOp4.getText().toString(),
												myRightan);
									}

									Log.d(TAG, "Posting:"
											+ myContent.getText().toString());

									if (previewidx == 1) {
										previewidx = 0;
										_bmImg = null;
										chkimg = 0;
										bmImg = null;
									}

									chkimg = 0;
									selarridx = 0;
									bmImg = null;
									MakeQuestion();
								}
							}
						});

				adb.show();

			}
		});

		// See the question beforehand
		Button preview1 = (Button) findViewById(R.id.preview01);
		preview1.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				previewidx = 1;

				int checked_rid = rgb04.getCheckedRadioButtonId();
				int my_an = checked_rid - R.id.rightan01 + 1;

				myRightan = Integer.toString(my_an);

				String _content = myContent.getText().toString();
				String _op1 = myOp1.getText().toString();
				String _op2 = myOp2.getText().toString();
				String _op3 = myOp3.getText().toString();
				String _op4 = myOp4.getText().toString();
				String _rightan = myRightan;

				question_arr[0] = _content;
				question_arr[1] = _op1;
				question_arr[2] = _op2;
				question_arr[3] = _op3;
				question_arr[4] = _op4;
				question_arr[5] = _rightan;

				preview(_content, _op1, _op2, _op3, _op4);

			}
		});

	}

	public void addimage() {

		chkimg = 1;
		add_pic_d = new AddPictureDialog(CourseList.this);
		add_pic_d.setActivity(_act);

		Window window = add_pic_d.getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		add_pic_d.setTitle("Select a picture for the question");
		add_pic_d.setContentView(R.layout.addpicdialog);
		add_pic_d.show();
		add_pic_d.setOnDismissListener((CourseList) _act);

	}

	public void image_quickaction() {

		// Adding quick action menu
		image_first = new ActionItem();

		image_first.setTitle("Add Image");
		image_first.setIcon(getResources().getDrawable(R.drawable.plus));
		image_first.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Toast.makeText(ebookmaker.this, "Add" ,
				// Toast.LENGTH_SHORT).show();
				addimage();
				if (image_qa != null)
					image_qa.dismiss();

			}
		});

		image_second = new ActionItem();

		image_second.setTitle("Take a Picture");
		image_second.setIcon(getResources()
				.getDrawable(R.drawable.take_picture));
		image_second.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Toast.makeText(ebookmaker.this, "Take a Picture",
				// Toast.LENGTH_SHORT).show();
				// call for taking picture
				takepicture();
				if (image_qa != null)
					image_qa.dismiss();

			}
		});

		image_third = new ActionItem();

		image_third.setTitle("Remove Image");
		image_third.setIcon(getResources().getDrawable(R.drawable.minus));
		image_third.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Toast.makeText(ebookmaker.this, "Remove",
				// Toast.LENGTH_SHORT).show();

				removeimage_dialog();
				if (image_qa != null)
					image_qa.dismiss();
			}
		});

	}

	private void removeimage_dialog() {

		chkimg = 0;
		activity = this;
		Builder adb = new AlertDialog.Builder(activity);
		adb.setTitle("JunctionQuiz: Making a question");
		adb.setMessage("Do you want to delete image?");
		adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {

				bmImg = null;
				imageview.setImageBitmap(bmImg);
				_bmImg = bmImg;
			}

		});

		adb.setNegativeButton("Cancel", null);
		adb.show();

	}

	private void takepicture() {

		chkimg = 1;
		TakenImage = false;

		// -----------------------------------------------------------------
		// Start Built-in Camera Activity
		// -----------------------------------------------------------------
		// define the file-name to save photo taken by Camera activity
		String img_filename = "new-photo-name.jpg";
		// create parameters for Intent with filename
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, img_filename);
		values.put(MediaStore.Images.Media.DESCRIPTION,
				"Image capture by camera");
		// imageUri is the current activity attribute, define and save it for
		// later usage (also in onSaveInstanceState)
		imageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		// imageUri =
		// getContentResolver().insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
		// values);

		// create new Intent
		camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		camera_intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

		startActivityForResult(camera_intent, IMAGECAPTURE_OK);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == IMAGECAPTURE_OK) {
			if (resultCode == RESULT_OK) {
				// ----------------------------------------
				// Get Result from Camera
				// ----------------------------------------
				TakenImage = true;

				setImageFromCamera();

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Picture was not taken",
						Toast.LENGTH_SHORT);
			} else {
				Toast.makeText(this, "Picture was not taken",
						Toast.LENGTH_SHORT);
			}
		}

	}

	private void setImageFromCamera() {

		if (TakenImage) {
			bmImg = getBitmapFromCameraResult();
		}

		// If there was a problem with getBitMap..., bmImg became null again
		try {

			// open file as input stream
			InputStream is = getContentResolver().openInputStream(imageUri);

			// save this output to local file
			OutputStream os = getBaseContext().openFileOutput("test.jpg",
					MODE_PRIVATE);
			byte[] BUF = new byte[1024];

			try {
				while (is.read(BUF) != -1) {
					os.write(BUF);
				}
				;

			} catch (IOException e) {
				Log.d(TAG, "ERROr COPYING DATA");
			}

		} catch (FileNotFoundException e) {
			Log.d(TAG, imageUri.toString());
		}

		imageview.setImageBitmap(bmImg);
		_bmImg = bmImg;

	}

	private Bitmap getBitmapFromCameraResult() {

		long id = -1;
		_act = this;
		// 1. Get Original id using m_cursor
		try {
			String[] proj = { MediaStore.Images.Media._ID };
			mcursor_for_camera = _act.managedQuery(imageUri, proj, // Which
																	// columns
																	// to return
					null, // WHERE clause; which rows to return (all rows)
					null, // WHERE clause selection arguments (none)
					null);// Order-by clause (ascending by name)

			int id_ColumnIndex = mcursor_for_camera
					.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

			if (mcursor_for_camera.moveToFirst()) {
				id = mcursor_for_camera.getLong(id_ColumnIndex);

			} else {
				return null;
			}

		} finally {
			if (mcursor_for_camera != null) {
				mcursor_for_camera.close();
			}
		}

		// 2. get Bitmap
		Bitmap b = null;
		try {
			Bitmap c = MediaStore.Images.Media.getBitmap(_act
					.getContentResolver(), Uri.withAppendedPath(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id + ""));

			Log.d(TAG, "Height" + c.getHeight() + " width= " + c.getWidth());

			// resize bitmap: code copied from
			// thinkandroid.wordpress.com/2009/12/25/resizing-a-bitmap/

			int target_width = 800;
			int target_height = 600;
			int w = c.getWidth();
			int h = c.getHeight();
			if ((w > target_width) || (h > target_height)) {
				// float scale_w = target_width /(float) w;
				// float scale_h = target_height / (float) h;
				// Matrix matrix = new Matrix();
				// matrix.postScale(scale_w, scale_h);
				// b = Bitmap.createBitmap(c, 0, 0, target_width, target_height,
				// matrix, false);
				b = Bitmap.createScaledBitmap(c, target_width, target_height,
						false);
			} else {
				b = c;
			}
		} catch (FileNotFoundException e) {
			Log.d(TAG, "ERROR" + e);
		} catch (IOException e) {
			Log.d(TAG, "ERROR" + e);
		}

		return b;

	}

	String image;
	String my_html;

	public void preview(String _content, String _op1, String _op2, String _op3,
			String _op4) {

		setTitle(curcategory);
		setContentView(R.layout.preview);

		Button backpreview = (Button) findViewById(R.id.previewBack);
		backpreview.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				MakeQuestion();
			}
		});

		curwebview = (WebView) findViewById(R.id.webviewPreview);
		curwebview.clearCache(true);

		String header = "<html> <head> previewing your question </head>";
		String body1 = " <body>";
		String question = "<P>" + _content + "</P>";

		if (chkimg == 1) {
			image = "<center><img class=\"main\" src=\"test.jpg\" width=250 height=200/></center>";
		}
		String choices = "<P>(1)" + _op1 + "<br>" + "(2)" + _op2 + "<br>"
				+ "(3)" + _op3 + "<br>" + "(4)" + _op4 + "<br>" + "</P>";
		String end = "</body>" + "</html>";

		if (chkimg == 1) {
			my_html = header + body1 + question + image + choices + end;
		} else {
			my_html = header + body1 + question + choices + end;
		}

		boolean from_asset = false;
		readHTMLfromString(my_html, from_asset);
	}

	// Called when add_pic_d is dismissed
	public void onDismiss(DialogInterface dialog) {

		isImageSelected = add_pic_d.isSelectedImg();

		if (isImageSelected) {
			// Quality of image should be improved.
			bmImg = add_pic_d.readThunmbBitmap();

			// save this file to asset
			try {

				// open file as input stream
				InputStream is = getContentResolver().openInputStream(
						add_pic_d.readURI());

				// save this output to local file
				OutputStream os = getBaseContext().openFileOutput("test.jpg",
						MODE_PRIVATE);
				byte[] BUF = new byte[1024];
				try {
					while (is.read(BUF) != -1) {
						os.write(BUF);
					}
					;

				} catch (IOException e) {
					Log.d(TAG, "ERROr COPYING DATA");
				}

			} catch (FileNotFoundException e) {
				Log.d(TAG, add_pic_d.readURI().toString());
			}
		} else {
			// bmImg = null;
		}

		imageview.setImageBitmap(bmImg);
		_bmImg = bmImg;
	}

	// initialization of arrays
	void initialize_AnswerRatingArray_withDummyValues() {
		answer_arr.clear();
		rating_arr.clear();
		my_score.clear();

		Log.d(TAG, "LAST_SCENE " + LAST_SCENE_NUM);
		for (int i = 0; i < LAST_SCENE_NUM; i++) {
			answer_arr.add(i, -1);
			rating_arr.add(i, -1);
			my_score.add(i, -1);
		}

	}

	private int saveCurrentAnswers() {

		int tot_answer = 0;
		int checked_id = rgb02.getCheckedRadioButtonId();

		if (checked_id > 0) {
			answer_arr.set((scene_number - 1), (checked_id - R.id.op01 + 1));
			tot_answer++;
		} else {
			answer_arr.set((scene_number - 1), -1);
		}

		checked_id = rgb03.getCheckedRadioButtonId();

		if (checked_id > 0) {
			rating_arr.set((scene_number - 1), (checked_id - R.id.rt01 + 1));
			tot_answer++;
		} else {
			rating_arr.set((scene_number - 1), -1);
		}

		return tot_answer;

	}

	private void checkCurrentAnswers() {

		// display answer
		if (answer_arr.get((scene_number - 1)) == -1) // not selected yet
			rgb02.check(-1);
		else
			rgb02.check(answer_arr.get((scene_number - 1)) + R.id.op01 - 1);

		if (rating_arr.get((scene_number - 1)) == -1)
			rgb03.check(-1);
		else
			rgb03.check(rating_arr.get((scene_number - 1)) + R.id.rt01 - 1);

	}

	String webpage;
	Boolean ansewr_chk = false;

	// 2. solve Questions w/solving questions screen
	private void SolveQuestion() {

		issolvequestion = 0;
		// 1st screen
		curwebview = webviewQ;
		curcategory = category_arr[1];
		scene_number = 1;

		setTitle(curcategory + "   1/" + LAST_SCENE_NUM);
		setContentView(R.layout.question);

		curwebview = (WebView) findViewById(R.id.webviewQ);
		rgb02 = (RadioGroup) findViewById(R.id.rgroup02);
		rgb03 = (RadioGroup) findViewById(R.id.rgroup03);
		curwebview.clearCache(true);

		getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
				Window.PROGRESS_VISIBILITY_ON);

		curwebview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Make the bar disappear after URL is loaded, and changes
				// string to Loading...
				CourseList.this.setTitle("Loading...");
				CourseList.this.setProgress(progress * 100); // Make the bar
																// disappear
																// after URL is
																// loaded
				// Restore the app name after finish loading
				if (progress == 100)
					CourseList.this.setTitle(curcategory + "   " + scene_number
							+ " /" + LAST_SCENE_NUM);
			}
		});

		initialize_AnswerRatingArray_withDummyValues();

		showScene();
		checkCurrentAnswers();

		// reset button (delete previous answer)
		Button resetB = (Button) findViewById(R.id.resetQ);
		resetB.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				rgb02.clearCheck();
				rgb03.clearCheck();
			}
		});

		// previous button
		Button prevB = (Button) findViewById(R.id.prevQ);
		prevB.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (scene_number > 1) {

					int temp = saveCurrentAnswers();
					showPreviousScene();
					checkCurrentAnswers();
				}
			}
		});

		// next button
		Button nextB = (Button) findViewById(R.id.nextQ);
		nextB.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (scene_number != LAST_SCENE_NUM) {

					int temp1 = saveCurrentAnswers();
					if (temp1 == 2) {
						showNextScene();
						checkCurrentAnswers();
					} else {
						Toast.makeText(CourseList.this,
								"Insert answer & rating", Toast.LENGTH_SHORT)
								.show();
					}

				} else {

					int temp1 = saveCurrentAnswers(); // save the last answer

					if (temp1 == 2) {
						Builder adb = new AlertDialog.Builder(CourseList.this);
						adb.setTitle(curcategory);
						adb.setMessage("Do you want to submit your answers?");
						adb.setPositiveButton("Submit",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface arg0,
											int arg1) {

										student.submit_answer_to_teacher(
												answer_arr, rating_arr);

										// Log.d(TAG,
										// "answer_arr[0]="+answer_arr.get(0));
										// Log.d(TAG,
										// "answer_arr[1]="+answer_arr.get(1));

										show_todo_view();
										selarridx = 0;

									}
								});

						adb.setNegativeButton("Cancel", null);
						adb.show();
					} else {
						Toast.makeText(CourseList.this,
								"Insert answer & rating", Toast.LENGTH_SHORT)
								.show();
					}
				}
			}
		});

	}

	private void showScene() {

		if (issolvequestion == 0) { // related to solving questions
			webpage = "http://" + cururi + server_dir + (scene_number - 1)
					+ ".html";

		} else { // related to seeing results
			
			webpage = "http://" + cururi + server_dir + (scene_number - 1)
					+ "_result" + ".html";
			my_answer_view.setText(answer_arr.get(scene_number - 1).toString());

			// 5-rating star
			Float f = new Float(final_avg_ratings.get(scene_number - 1));
			ratingbar.setRating(f);
			ratingbar.setEnabled(false);

			// percent graph	
			right_val = Integer.parseInt(r_answer_percents.get(scene_number - 1).trim());
			wrong_val = 100 - right_val;
			draw_piechart.redraw(right_val, wrong_val);		
			
		}

		curwebview.clearView();
		curwebview.loadUrl(webpage);
	}

	private void showNextScene() {

		if (scene_number != LAST_SCENE_NUM) {
			scene_number++;
			setTitle(curcategory + "   " + scene_number + "/" + LAST_SCENE_NUM);
			showScene();
		}
	}

	private void showPreviousScene() {
		if (scene_number > 1) {
			scene_number--;
			setTitle(curcategory + "   " + scene_number + "/" + LAST_SCENE_NUM);
			showScene();
		}
	}

	// read questions
	private void readHTMLfromResouceFile(String fileName) {
		InputStream is;
		try {
			is = getAssets().open(fileName);
		} catch (IOException e) {
			return;
		}

		InputStreamReader isr = new InputStreamReader(is);
		StringBuffer builder = new StringBuffer();
		char buffer[] = new char[1024];

		try {
			int chars;

			while ((chars = isr.read(buffer)) >= 0) {
				builder.append(buffer, 0, chars);
			}
		} catch (IOException e) {
			return;
		}

		String htmlstring = builder.toString();
		curwebview.loadDataWithBaseURL("file:///android_asset/", htmlstring,
				"text/html", "utf-8", "");
	}

	private void readHTMLfromString(String htmlStr, boolean from_asset) {
		String url;
		if (from_asset)
			url = "file:///andorid_asset";
		else
			url = stored_file_url;

		curwebview.loadDataWithBaseURL(stored_file_url, htmlStr, "text/html",
				"utf-8", "");
	}

	// 3. See Results
	private void seeResults() {

		issolvequestion = 1; // see results
		curcategory = category_arr[2];
		setTitle(curcategory);
		setContentView(R.layout.seeresults);
		curwebview = (WebView) findViewById(R.id.webviewSeeR);
		curwebview.clearCache(false);

		scorequestion(answer_arr, right_answer); // score my answers

		// 1. show main result
		String received_html = createresulthtml(my_score, curusername);
		curwebview.loadData(received_html, "text/html", "UTF-8");

		Button quitSR = (Button) findViewById(R.id.SeeRQuit);
		quitSR.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				show_todo_view();
				selarridx = 0;
				// change_todo_view_state(6);
			}
		});

		// who is winner in score and rating
		Button winnerSR = (Button) findViewById(R.id.WinnerResult);
		winnerSR.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				show_winner();
			}
		});

		// show the result of each question
		Button detailSR = (Button) findViewById(R.id.DetailResult);
		detailSR.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				draw_piechart.setIsAdded(false);
				show_detail(my_score);
			}
		});

	}

	private void show_winner() {

		setContentView(R.layout.winner);
		curwebview = (WebView) findViewById(R.id.webviewWinner);
		String received_html = createwinnerhtml();
		curwebview.loadData(received_html, "text/html", "UTF-8");

		Button returnSW = (Button) findViewById(R.id.returnresult01);
		returnSW.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				seeResults();
			}
		});
	}

	TextView my_answer_view;
	RatingBar ratingbar;
	int right_val;
	int wrong_val;
	
	private void show_detail(Vector<Integer> _myscore) {

		curcategory = category_arr[2];
		scene_number = 1;
		setContentView(R.layout.detailresult);

		setTitle(curcategory + "     1/" + LAST_SCENE_NUM);
		curwebview = (WebView) findViewById(R.id.webviewdetailresult);
		curwebview.clearCache(false);

		my_answer_view = (TextView) findViewById(R.id.textresult02);
		
		ratingbar = (RatingBar) findViewById(R.id.ratingbar);
				
		showScene();

		Button returnMR = (Button) findViewById(R.id.returnresult02);
		returnMR.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				seeResults();
			}
		});

		Button prevDR = (Button) findViewById(R.id.prevResult);
		prevDR.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (scene_number > 1) {
					showPreviousScene();
				}
			}
		});

		Button nextDR = (Button) findViewById(R.id.nextResult);
		nextDR.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (scene_number != LAST_SCENE_NUM) {
					showNextScene();
				} else {

					Builder adb = new AlertDialog.Builder(CourseList.this);
					adb.setTitle(curcategory);
					adb.setMessage("This is the last question.");
					adb.setPositiveButton("OK", null);
					adb.show();
				}
			}
		});

	}

	private void scorequestion(Vector<Integer> _rightanswer,
			Vector<Integer> _myanswer) {

		for (int i = 0; i < _rightanswer.size(); i++) {
			if (_myanswer.get(i) == _rightanswer.get(i))
				my_score.set(i, 1); // right
			else
				my_score.set(i, 0); // wrong
		}
	}

	private int countrightquestion(Vector<Integer> arr) {
		int revalue = 0;

		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(i) == 1)
				revalue++;
		}

		return revalue;
	}

	private String createwinnerhtml() {
		String return_html = "";

		String header1 = "<html><head></head><body><P></P><font face=\"helvetica\">";
		String header2 = "<center><Strong>Who's the Winner?</Strong></center><br>";
		String body1 = "<P></P>*** Quiz Score ***<br>";
		String body2 = "Highest Score: " + high_score + "<br>";
		String body3 = "Name:<br>";
		String mid_html = "";
		for (int i = 0; i < score_winner_name.size(); i++) {
			String name = score_winner_name.get(i);
			mid_html = mid_html + name + "<br>";

		}

		// NumberFormat formatter = new DecimalFormat("#0.00");

		String body4 = "<P></P>";
		String body5 = "*** Question Rating Score ***<br>";
		Formatter f = new Formatter();
		String avg = f.format(new String("%4.2f"), high_rating).toString();
		String body6 = "Highest Average Rating: " + avg + "<br>";
		String body7 = "Name:<br>";
		String next_html = "";
		for (int i = 0; i < rating_winner_name.size(); i++) {
			String r_name = rating_winner_name.get(i);
			next_html = next_html + r_name + "<br>";

		}
		String end = "</font></body></html>";

		return_html = header1 + header2 + body1 + body2 + body3 + mid_html
				+ body4 + body5 + body6 + body7 + next_html + end;

		return return_html;
	}

	String result_html;

	private String createresulthtml(Vector<Integer> _myscore, String username) {

		int num_right = countrightquestion(_myscore);
		int total_question = LAST_SCENE_NUM;

		String header1 = "<html><head></head><body><P></P><font face=\"helvetica\">";
		String header2 = "<center>" + username + "'s result<br>";
		String header3 = "Total Score: " + num_right + "/" + total_question + "<br />&#x2717;=Incorrect, &#x2713;=Correct";
		String body1 = "<P><table border=\"1\">";
		String body2 = "<tr><td><div align=\"center\"> Question Number </div></td>"
				+ "<td><div align=\"center\"> Correct or Wrong </div></td></tr>";

		String fore_html = header1 + header2 + header3 + body1 + body2;
		String mid_html = "";
		for (int i = 0; i < _myscore.size(); i++) {
			String mid;
			int number = 0;
			number = i + 1;
			int score = _myscore.get(i);
			if (score == 1) { // right
				mid = "<tr><td><div align=\"center\">" + "(" + number + ")"
						+ "</div></td>"
						+ "<td><div align=\"center\">&#x2713;</div></td></tr>";
			} else { // wrong
				mid = "<tr><td><div align=\"center\">" + "(" + number + ")"
						+ "</div></td>"
						+ "<td><div align=\"center\">&#x2717;</div></td></tr>";
			}

			mid_html = mid_html + mid;
		}
		String end = "</table></p></center></font></body></html>";
		String last_html = end;
		result_html = fore_html + mid_html + last_html;

		return result_html;
	}

	public void onStop() {

		// CourseList.this.finish();
		super.onStop();
	}

	public void onPause() {

		// CourseList.this.finish();
		super.onPause();
	}

} // end of activity