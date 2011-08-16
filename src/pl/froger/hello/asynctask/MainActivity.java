package pl.froger.hello.asynctask;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
	private GridView gvFriends;
	private Button btnLoadImages;
	private ProgressBar pbWheel;
	private ProgressBar pbHorizontal;
	
	private ArrayList<Bitmap> images;
	private ImagesAdapter imagesGridAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        gvFriends = (GridView) findViewById(R.id.gvFriends);
        btnLoadImages = (Button) findViewById(R.id.btnLoadImages);
        pbHorizontal = (ProgressBar) findViewById(R.id.pbHorizontal);
        pbWheel = (ProgressBar) findViewById(R.id.pbWheel);
        initButtonOnClick();
        initGrid();
    }

	private void initButtonOnClick() {
		btnLoadImages.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadFriendImagesAsynchronously();
			}
		});
	}

	private void initGrid() {
		images = new ArrayList<Bitmap>();
        imagesGridAdapter = new ImagesAdapter(getApplicationContext(), images);
        gvFriends.setAdapter(imagesGridAdapter);
	}
    
	private class ImageLoaderTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			btnLoadImages.setEnabled(false);
			pbHorizontal.setVisibility(View.VISIBLE);
			pbWheel.setVisibility(View.VISIBLE);
			pbHorizontal.setProgress(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
	    	images.clear();
	    	Cursor c = getAllContacts();
	    	if(c.moveToFirst()) {
	    		do {
	    			Bitmap image = loadContactPhoto(c.getInt(0));
	    			if(image != null) images.add(image);
	    	    	publishProgress(c.getPosition() / c.getColumnCount());
	    		} while (c.moveToNext());
	    	}
	    	c.close();
			return null;
		}
		
		private Cursor getAllContacts() {
			Uri uri = Contacts.CONTENT_URI;
			String[] projection = { Contacts._ID };
			return getContentResolver().query(uri, projection, null, null, null);
		}
		
		private Bitmap loadContactPhoto(long id) {
		    Uri uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
		    InputStream input = Contacts.openContactPhotoInputStream(getContentResolver(), uri);
		    return BitmapFactory.decodeStream(input);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			pbHorizontal.setProgress(values[0]);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			btnLoadImages.setEnabled(true);
			pbHorizontal.setVisibility(View.GONE);
			pbWheel.setVisibility(View.INVISIBLE);
			gvFriends.invalidateViews();
		}
	}
	
	private void loadFriendImagesAsynchronously() {
		new ImageLoaderTask().execute();
	}
}