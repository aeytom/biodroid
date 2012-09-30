package de.taytec.biodroid;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TextView;

public class BioDroidActivity extends TabActivity  implements BioView.BioHolder {
    
    private static final int MENU_TODAY = 1;
    protected final int DIALOG_DATE_TODAY = 1;
    protected final int DIALOG_FAVORITE = 2;
    protected final int DIALOG_ABOUT = 3;
    protected final int DIALOG_DATE_BIRTH = 4;
    private static final int DIALOG_THEORY = 5;
    private static final int PICK_BIRTHDAY_SUBACTIVITY = 0;

    protected TextView tv_birth;
    protected TextView tv_today;

    protected Calendar calBirth = Calendar.getInstance();
    protected Calendar calToday = Calendar.getInstance();
    private BioView bioview;
    private String[] desc_phy;
    private String[] desc_emo;
    private String[] desc_int;
    private int oldPhaseP = -1;
    private int oldPhaseE = -1;
    private int oldPhaseI = -1;
    private Favorites favorites;
    private boolean firstStart = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	
	CharSequence tab_phy = getResources().getText(R.string.tab_phy);
	CharSequence tab_emo = getResources().getText(R.string.tab_emo);
	CharSequence tab_int = getResources().getText(R.string.tab_int);

	desc_phy = getResources().getStringArray(R.array.desc_phy);
	desc_emo = getResources().getStringArray(R.array.desc_emo);
	desc_int = getResources().getStringArray(R.array.desc_int);

	favorites = new Favorites(this, new SimpleDateFormat(getResources().getString(R.string.format_date)));

	restoreActivityPreferences();
	
	tv_birth = (TextView) findViewById(R.id.text_birth);
	tv_today = (TextView) findViewById(R.id.text_today);

	TabHost mTabHost = getTabHost();
	mTabHost.addTab(mTabHost.newTabSpec("tab_test1")
		.setIndicator(tab_phy, getResources().getDrawable(R.drawable.sin_phy))
		.setContent(R.id.scroll_phy));
	mTabHost.addTab(mTabHost.newTabSpec("tab_test2")
		.setIndicator(tab_emo, getResources().getDrawable(R.drawable.sin_emo))
		.setContent(R.id.scroll_emo));
	mTabHost.addTab(mTabHost.newTabSpec("tab_test3")
		.setIndicator(tab_int, getResources().getDrawable(R.drawable.sin_int))
		.setContent(R.id.scroll_int));
	mTabHost.setCurrentTab(0);
	
	bioview = (BioView)findViewById(R.id.surface);
	bioview.setBioHolder(this);

	updateDisplay();
	
	if (firstStart ) {
	    showDialog(DIALOG_ABOUT);
	}
    }

    /**
     * persistent save the activity state
     */
    protected void storeActivityPreferences() {
//	Log.d(getPackageName(), "storeActivityPreferences()");
	SharedPreferences preferences = getPreferences(MODE_PRIVATE);
	Editor ed = preferences.edit();
	ed.putLong("birthday", calBirth.getTime().getTime());
	try {
	    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
	    ed.putInt("version", pInfo.versionCode);
	} catch (NameNotFoundException e) {
	    Log.e(getPackageName(), "storeActivityPreferences() : PackageManager.GET_META_DATA", e);
	}
	favorites.storeToPreferences(ed, "history");
	ed.commit();
    }

    /**
     * restore activity state from preferences
     */
    protected void restoreActivityPreferences() {
//	Log.d(getPackageName(), "restoreActivityPreferences()");
	SharedPreferences preferences = getPreferences(MODE_PRIVATE);
	try {
	    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
	    firstStart = ! (pInfo.versionCode == preferences.getInt("version",0));
	} catch (NameNotFoundException e) {
	    Log.e(getPackageName(), "restoreActivityPreferences() : PackageManager.GET_META_DATA", e);
	}	
	favorites.restoreFromPreferences(preferences, "history");
	//
	// restore birthday
	//
	long bdTime = preferences.getLong("birthday", -1);
	if (-1 != bdTime) {
	    calBirth.setTime(new Date(bdTime));
	    favorites.add(calBirth.getTime());
	}
	else {
	    calBirth.set(2003, 2, 6);
	    favorites.add(calBirth.getTime());
	}
    }

    /**
     * persistent save the activity state
     * 
     * @see android.app.ActivityGroup#onStop()
     */
    @Override
    protected void onStop() {
//	Log.d(getPackageName(), "onStop()");
	super.onStop();
	storeActivityPreferences();
    }
    
    /* (non-Javadoc)
     * @see android.app.ActivityGroup#onResume()
     */
    @Override
    protected void onResume() {
//	Log.d(getPackageName(), "onResume()");
	super.onResume();
	restoreActivityPreferences();
    }
    
    /**
     * Restore activity state
     * 
     * @see android.app.TabActivity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle state) {
//	Log.d(getPackageName(), "onRestoreInstanceState()");
	super.onRestoreInstanceState(state);
	restoreActivityPreferences();
	if (state.containsKey("today")) {
	    calToday.setTimeInMillis(state.getLong("today"));
	}
    }

    /* (non-Javadoc)
     * @see android.app.TabActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
//	Log.d(getPackageName(), "onSaveInstanceState()");
	super.onSaveInstanceState(outState);
	storeActivityPreferences();
	outState.putLong("today", calToday.getTimeInMillis());
    }
    
    

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	if (PICK_BIRTHDAY_SUBACTIVITY == requestCode && Activity.RESULT_OK == resultCode) {
	    Cursor c = managedQuery(data.getData(), null, null, null, null);
	    if (c.moveToFirst()) {
		Date bd = BirthdayCursorAdapter.getBirthdayFromCursor(c);
		if (bd != null) {
		    calBirth.setTime(bd);
		    favorites.add(bd);
		    updateDisplay();
		    storeActivityPreferences();
		}
	    }
	}
    }

    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu
     *            the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main, menu);
	return true;
    }

    /**
     * 
     * @return
     */
    protected AlertDialog createFavoriteDialog() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder
		.setTitle(getResources().getString(R.string.favorite_dialog_title))
		.setCancelable(true)
		.setAdapter(favorites, new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int item) {
		Date choosed = favorites.getItem(item);
		calBirth.setTime(choosed);
		favorites.add(choosed);
		updateDisplay();
	    }
	});
	AlertDialog alert = builder.create();
	return alert;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onSearchRequested()
     */
    @Override
    public boolean onSearchRequested() {
	Intent intent = new Intent(Intent.ACTION_PICK, Uri
		.parse("content://birthdays"));
	startActivityForResult(intent, PICK_BIRTHDAY_SUBACTIVITY);
	return true;
    }

    /**
     * 
     * @return
     */
    protected AlertDialog createAboutDialog() {	
	String versionName = "";
	try {
	    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
	    versionName = pInfo.versionName;
	} catch (NameNotFoundException e) {
	    Log.e(getPackageName(), "createAboutDialog() : PackageManager.GET_META_DATA", e);
	}	
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder
		.setMessage(android.text.Html.fromHtml(getResources().getString(R.string.About).replace("VERSION", versionName)))
		.setCancelable(false)
		.setPositiveButton(R.string.Button_Ok,
			new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			    }
			});
	AlertDialog alert = builder.create();
	return alert;
    }

    /**
     * 
     */
    protected void updateDisplay() {
	bioview.invalidate();
	
	SimpleDateFormat df = new SimpleDateFormat(
		getResources().getString(R.string.format_date));
	tv_birth.setText(df.format(calBirth.getTime()));
	tv_today.setText(df.format(calToday.getTime()));

	int phaseP = bioview.getPhase(BioView.PHYSICAL);
	if (oldPhaseP != phaseP) {
	    ((TextView) findViewById(R.id.tab_phy)).setText(desc_phy[phaseP]);
	    oldPhaseP  = phaseP;
	}
	int phaseE = bioview.getPhase(BioView.EMOTIONAL);
	if (oldPhaseE != phaseE) {
	    ((TextView) findViewById(R.id.tab_emo)).setText(desc_emo[phaseE]);
	    oldPhaseE = phaseE;
	}
	int phaseI = bioview.getPhase(BioView.INTELECTUAL);
	if (oldPhaseI != phaseE) {
	    ((TextView) findViewById(R.id.tab_int)).setText(desc_int[phaseI]);
	    oldPhaseI = phaseI;
	}
    }

    /**
     * @param id
     *            wanted dialog id
     */
    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case DIALOG_DATE_BIRTH:
	    return new DatePickerDialog(this, dateSetListenerBirth, 
		    calBirth.get(Calendar.YEAR), 
		    calBirth.get(Calendar.MONTH),
		    calBirth.get(Calendar.DAY_OF_MONTH));
	case DIALOG_DATE_TODAY:
//	    Log.d("oCD", "Y: "+cal_today.get(Calendar.YEAR)+" M: "+cal_today.get(Calendar.MONTH)+" D: "+cal_today.get(Calendar.DAY_OF_MONTH));
	    return new DatePickerDialog(this, dateSetListenerToday, 
		    calToday.get(Calendar.YEAR), 
		    calToday.get(Calendar.MONTH),
		    calToday.get(Calendar.DAY_OF_MONTH));
	case DIALOG_FAVORITE:
	    return this.createFavoriteDialog();
	case DIALOG_ABOUT:
	    return this.createAboutDialog();
	case DIALOG_THEORY:
	    return this.createTheoryDialog();
	}
	return null;
    }

    /**
     * check date values of Calendar to ensure that all fields are correct
     * 
     * @param cal
     * @return
     */
    protected Calendar checkCalendar(Calendar cal) {
	if (cal.get(Calendar.YEAR) < cal.getActualMinimum(Calendar.YEAR)) {
	    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
	}
	if (cal.get(Calendar.YEAR) > cal.getActualMaximum(Calendar.YEAR)) {
	    cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
	}
	if (cal.get(Calendar.MONTH) < cal.getActualMinimum(Calendar.MONTH)) {
	    cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
	}
	if (cal.get(Calendar.MONTH) > cal.getActualMaximum(Calendar.MONTH)) {
	    cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
	}
	if (cal.get(Calendar.DAY_OF_MONTH) < cal.getActualMinimum(Calendar.DAY_OF_MONTH)) {
	    cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
	}
	if (cal.get(Calendar.DAY_OF_MONTH) > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
	    cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
	}
	
	return cal;
    }

    /**
     * 
     * @return
     */
    private Dialog createTheoryDialog() {
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder
		.setMessage(android.text.Html.fromHtml(getResources().getString(R.string.theory)))
		.setCancelable(false)
		.setPositiveButton(R.string.Button_Ok,
			new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			    }
			});
	AlertDialog alert = builder.create();
	return alert;
    }

    /**
     * 
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
	switch (id) {
	case DIALOG_DATE_BIRTH:
	    ((DatePickerDialog) dialog).updateDate(
		    calBirth.get(Calendar.YEAR),
		    calBirth.get(Calendar.MONTH), 
		    calBirth.get(Calendar.DAY_OF_MONTH));
	    break;
	case DIALOG_DATE_TODAY:
	    ((DatePickerDialog) dialog).updateDate(
		    calToday.get(Calendar.YEAR),
		    calToday.get(Calendar.MONTH), 
		    calToday.get(Calendar.DAY_OF_MONTH));
	    break;
	}
    }

    private DatePickerDialog.OnDateSetListener dateSetListenerBirth = new DatePickerDialog.OnDateSetListener() {
	public void onDateSet(DatePicker view, int year, int monthOfYear,
		int dayOfMonth) {
	    calBirth.set(year, monthOfYear, dayOfMonth);
	    favorites.add(calBirth.getTime());
	    updateDisplay();
	    storeActivityPreferences();
	}
    };
    
 
    private DatePickerDialog.OnDateSetListener dateSetListenerToday = new DatePickerDialog.OnDateSetListener() {
	public void onDateSet(DatePicker view, int year, int monthOfYear,
		int dayOfMonth) {
	    calToday.set(year, monthOfYear, dayOfMonth);
	    updateDisplay();
	}
    };

    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item
     *            the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menuBirthday:
	    showDialog(DIALOG_DATE_BIRTH);
	    return true;
	case R.id.menuForDay:
	    showDialog(MENU_TODAY);
	    return true;
	case R.id.menuHistory:
	    showDialog(DIALOG_FAVORITE);
	    return true;
	case R.id.menuAbout:
	    showDialog(DIALOG_ABOUT);
	    return true;
	case R.id.menuTheory:
	    showDialog(DIALOG_THEORY);
	    return true;
	case R.id.menuContacts:
	    Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://birthdays"));
	    startActivityForResult(intent, PICK_BIRTHDAY_SUBACTIVITY);
	    return true;
	}
	return false;
    }


    @Override
    public Calendar getEndCalendar() {
	return calToday;
    }

    @Override
    public Calendar getStartCalendar() {
	return calBirth;
    }

    @Override
    public void bioHolderChanged() {
	updateDisplay();
    }

}