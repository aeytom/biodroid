/**
 * 
 */
package de.taytec.biodroid;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * @author tay
 * 
 */
public class BirthdayCursorAdapter extends CursorAdapter {

    public BirthdayCursorAdapter(Context context, Cursor c) {
	super(context, c);
    }

    /**
     * get all Contacts with birthday
     * 
     * @param contentResolver
     * @return Cursor or null
     */
    static Cursor getAllBirthdays(ContentResolver contentResolver) {
	String[] cols = new String[] {
		ContactsContract.Contacts._ID,
		ContactsContract.Contacts.LOOKUP_KEY,
		ContactsContract.Data.CONTACT_ID,
		ContactsContract.Data.DISPLAY_NAME,
		ContactsContract.Data.DATA1
	};
	String where = ContactsContract.CommonDataKinds.Event.START_DATE
		+ " IS NOT NULL AND "
		+ ContactsContract.CommonDataKinds.Event.TYPE + "=? AND "
		+ ContactsContract.Data.MIMETYPE + "=? "
		+ ") GROUP BY ("+ContactsContract.Data.CONTACT_ID;
	String[] args = new String[] {
		Integer.toString(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY),
		ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE 
	};
	Cursor c = contentResolver.query(ContactsContract.Data.CONTENT_URI, cols,
		where, args,
		ContactsContract.CommonDataKinds.Event.DISPLAY_NAME);
	return c.moveToFirst() ? c : null;
    }

    /**
     * get single contact with birthday
     * 
     * @param contentResolver
     * @param id
     * @return Cursor or null
     */
    static Cursor getContactById(ContentResolver contentResolver, int id) {
	String where = ContactsContract.CommonDataKinds.Event._ID + "=? AND "
		+ ContactsContract.CommonDataKinds.Event.TYPE + "=? AND "
		+ ContactsContract.Data.MIMETYPE + "=?";
	String[] args = new String[] {
		Integer.toString(id),
		Integer.toString(ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY),
		ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE };
	 Cursor c = contentResolver.query(ContactsContract.Data.CONTENT_URI, null,
		where, args, null);
	 return c.moveToFirst() ? c : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.CursorAdapter#newView(android.content.Context,
     * android.database.Cursor, android.view.ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View view = inflater
		.inflate(R.layout.name_date_listitem, parent, false);
	bindView(view, context, cursor);
	return view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.CursorAdapter#bindView(android.view.View,
     * android.content.Context, android.database.Cursor)
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
	TextView nameView = (TextView) view.findViewById(R.id.itemNameView);
	TextView dateView = (TextView) view.findViewById(R.id.itemDateView);

	int colNoDisplayName = cursor
		.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME);
	nameView.setText(cursor.getString(colNoDisplayName));

	Date bd = getBirthdayFromCursor(cursor);
	if (bd != null) {
	    DateFormat dfOut = new SimpleDateFormat("EEE, d. MMM yyyy");
	    dateView.setText(dfOut.format(bd));
	} else {
	    int colNoBirthday = cursor
		    .getColumnIndexOrThrow(ContactsContract.Data.DATA1);
	    dateView.setText(cursor.getString(colNoBirthday));
	}
    }

    /**
     * Get the birthday as Date instance from cursor
     * 
     * @param cursor
     * @return Birthday or null on error
     */
    public static Date getBirthdayFromCursor(Cursor cursor) {
	int colNoBirthday = cursor
		.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE);
//	Log.d("BioDroid", cursor.getString(colNoBirthday));
//	for (int c=0; c<cursor.getColumnCount(); c++) {
//		Log.d("BioDroid", "    "+cursor.getColumnName(c)+" := "+cursor.getString(c));	    
//	}
	Date bd = null;
	try {
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	    bd = df.parse(cursor.getString(colNoBirthday).replace(".000Z",
		    "+00:00").replace(".000", ""));
	} catch (ParseException e) {
	    try {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		bd = df.parse(cursor.getString(colNoBirthday).replace(".000Z",
			"+00:00").replace(".000", ""));
	    } catch (ParseException ee) {
		Log.e("BioDroid", cursor.getString(colNoBirthday), ee);
	    }
	}
	return bd;
    }
}
