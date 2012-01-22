package de.taytec.biodroid;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ContactPicker extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
	setContentView(R.layout.contactpicker);

	final Cursor c = BirthdayCursorAdapter.getAllBirthdays(getContentResolver());
	if (c == null) {
	    setResult(Activity.RESULT_CANCELED);
	    finish();
	}

	ListView lv = (ListView) findViewById(R.id.contactListView);
	lv.setAdapter(new BirthdayCursorAdapter(getApplicationContext(), c));

	// Return the selected contact when it is chosen from the list.
	lv.setOnItemClickListener(new OnItemClickListener() {

	    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		// Move the cursor to the selected item
		c.moveToPosition(pos);
		// Extract the row id.
		int rowId = c.getInt(c.getColumnIndexOrThrow("_id"));
		// Construct the result URI.
		Uri outURI = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, rowId);
		Intent outData = new Intent();
		outData.setData(outURI);
		setResult(Activity.RESULT_OK, outData);
		finish();
	    }

	});
    }

}