package org.smilecon.smile.student;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

@TargetApi(5)
public class AddPictureDialog extends Dialog implements OnItemClickListener  {

	private Gallery mGallery;
	private Cursor mCursor;
	private Activity _act;
	private boolean bIsNullImage = true;

	static Uri uri;

	public AddPictureDialog(Context context) {
		super(context);
		bIsNullImage = true;
	}

	public void setActivity(Activity a) {
		_act = a;
	}

	public boolean isSelectedImg() {return !bIsNullImage;} // return true if img is not null

	public void onStart() {

		super.onStart();

		//In exit without selecting the image
		Button ok = (Button) findViewById(R.id.okBPic);
		ok.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				bIsNullImage = true;
				dismiss();
			}
		});

		// add pictures from the gallery
		mGallery = (Gallery)findViewById(R.id.picturesTaken);
		displayGallery();

	}

	private void displayGallery() {

		/* The problem here is that it's querying for the thumbnails, rather than the actual images.
		 * The camera app on some devices do not create thumbnails by default, and so this query will
		 * fail to return images that do not already have thumbnails calculated.*/
	    // uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
		// Where images are stored
		// displaySdCard();

		/*String selection = MediaStore.Images.Thumbnails.KIND + "="  + // Select only mini's
		MediaStore.Images.Thumbnails.MINI_KIND;

		String[] projection = {
				MediaStore.Images.ImageColumns._ID,  // The columns we want
				MediaStore.Images.Thumbnails.IMAGE_ID,
				MediaStore.Images.Thumbnails.KIND
		};

		mCursor = _act.managedQuery(uri, projection, selection, null, null);*/
		// added on 7/31/2012 read the original images
		String[] projection = { MediaStore.Images.ImageColumns._ID, MediaStore.Images.Media._ID,
				 MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				 MediaStore.Images.Media.DATA};
	    //String selection = "";
		String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[] {  //  select camera image only
            "Camera"
        };

	    mCursor = _act.managedQuery( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                                 projection, selection, selectionArgs, null );

		if (mCursor != null) {
			mCursor.moveToFirst();
			ImageAdapter adapter = new ImageAdapter(mCursor, _act);
			mGallery.setAdapter(adapter);
			mGallery.setOnItemClickListener(this);

		} else {
			bIsNullImage = true;
		}

			//showToast(this, "Gallery is empty.");
	}


	// After selecting the image
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long rowId) {

		try {

			mCursor.moveToPosition(position);
			//long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
			//create the Uri for the Image
			//uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"");

			// added on 7/31/2012
			long id = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media._ID) );
			uri = Uri.withAppendedPath( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"" );

			bIsNullImage = false;
			Toast.makeText(_act, "SELECTED", Toast.LENGTH_SHORT).show();

		} catch (CursorIndexOutOfBoundsException e) {
			//Log.i(TAG, "CursorIndexOutOfBoundsException " + e.getStackTrace());
		}

		dismiss();
	}

	public Uri readURI() {
		return uri;
	}

	public String readURLPath() {

		try {
			int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
			return mCursor.getString(column_index);
		} catch(IllegalArgumentException e) {
			Toast.makeText(_act, "ARGUMENT WRONG", Toast.LENGTH_SHORT).show();
			return "";
		}
	}

	public Bitmap readThunmbBitmap() {

		try {
			//long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
			int kind = MediaStore.Images.Thumbnails.MICRO_KIND;
			// added on 7/31/2012
			long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

			return MediaStore.Images.Thumbnails.getThumbnail(_act.getContentResolver(), id, kind, null);

		} catch(IllegalArgumentException e) {
			Toast.makeText(_act, "ARGUMENT WRONG", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

}
