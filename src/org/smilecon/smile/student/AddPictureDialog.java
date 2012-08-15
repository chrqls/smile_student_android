package org.smilecon.smile.student;

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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
	
	public boolean isSelectedImg() {return !bIsNullImage; } // return true if img is not null
	
	public void onStart() {
		
		super.onStart();
		Button ok = (Button) findViewById(R.id.okBPic);
		ok.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {	
				bIsNullImage = true;
				dismiss();
			}
		});
		
	
		// add pictures to the gallery
		mGallery = (Gallery)findViewById(R.id.picturesTaken);
		displayGallery();
	}
	
	private void displayGallery() {
		Uri uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI; // Where images are stored
		//displaySdCard();
		String[] projection = {
				MediaStore.Images.ImageColumns._ID,  // The columns we want
				MediaStore.Images.Thumbnails.IMAGE_ID,  
				MediaStore.Images.Thumbnails.KIND 
		};
		String selection = MediaStore.Images.Thumbnails.KIND + "="  + // Select only mini's
		MediaStore.Images.Thumbnails.MINI_KIND;
		mCursor = _act.managedQuery(uri, projection, selection, null, null);	
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
		//Log.i(TAG,"onImageClick position= " + position +  " rowId= " 
		//		+ rowId + " nCursor=" + mCursor.getCount());
		try {
			mCursor.moveToPosition(position);
			long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
			//create the Uri for the Image 
			uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id+"");

			bIsNullImage = false;
			//Toast.makeText(_act, "SELECTED", Toast.LENGTH_SHORT).show();
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
			long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
			int kind = MediaStore.Images.Thumbnails.MICRO_KIND;
			return MediaStore.Images.Thumbnails.getThumbnail(_act.getContentResolver(), id, kind, null);
		} catch(IllegalArgumentException e) {
			Toast.makeText(_act, "ARGUMENT WRONG", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

}
