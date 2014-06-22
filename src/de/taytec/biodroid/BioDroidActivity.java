package de.taytec.biodroid;



import android.app.*;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;
import android.content.SharedPreferences.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import de.taytec.biodroid.*;

import java.text.*;
import java.util.*;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.ActionBar.*;

public class BioDroidActivity extends FragmentActivity implements BioView.BioHolder, TabListener
{
    protected final int DIALOG_FAVORITE = 2;
    protected final int DIALOG_ABOUT = 3;
    private static final int DIALOG_THEORY = 5;
 
    protected TextView tv_birth;
    protected TextView tv_today;

    protected Calendar calBirth = Calendar.getInstance();
    protected Calendar calToday = Calendar.getInstance();

    private BioView bioview;

    private int oldPhaseP = -1;
    private int oldPhaseE = -1;
    private int oldPhaseI = -1;
    private Favorites favorites;
    
    private boolean firstStart = true;
	private ViewPager mViewPager;
	private TabAdapter mTabAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		favorites = new Favorites(this, 
			new SimpleDateFormat(getResources().getString(R.string.format_date)));

		restoreActivityPreferences();

		tv_birth = (TextView) findViewById(R.id.editBirthday);
		tv_today = (TextView) findViewById(R.id.editToday);
        
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mTabAdapter =
			new TabAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.descPager);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager
		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});
        
        for (int i = 0; i < mTabAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mTabAdapter.getPageTitle(i))
					.setIcon(mTabAdapter.getIcon(i))
					.setTabListener(this));
		}
		
		bioview = (BioView)findViewById(R.id.surface);
		bioview.setBioHolder(this);

		updateDisplay();

		if (firstStart)
		{
			showDialog(DIALOG_ABOUT);
		}
    }

    /**
     * persistent save the activity state
     */
    protected void storeActivityPreferences()
	{
		//	Log.d(getPackageName(), "storeActivityPreferences()");
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Editor ed = preferences.edit();
		ed.putLong("birthday", calBirth.getTime().getTime());
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			ed.putInt("version", pInfo.versionCode);
		}
		catch (NameNotFoundException e)
		{
			Log.e(getPackageName(), "storeActivityPreferences() : PackageManager.GET_META_DATA", e);
		}
		favorites.storeToPreferences(ed, "history");
		ed.commit();
    }

    /**
     * restore activity state from preferences
     */
    protected void restoreActivityPreferences()
	{
		//	Log.d(getPackageName(), "restoreActivityPreferences()");
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			firstStart = ! (pInfo.versionCode == preferences.getInt("version", 0));
		}
		catch (NameNotFoundException e)
		{
			Log.e(getPackageName(), "restoreActivityPreferences() : PackageManager.GET_META_DATA", e);
		}	
		favorites.restoreFromPreferences(preferences, "history");
		//
		// restore birthday
		//
		long bdTime = preferences.getLong("birthday", -1);
		if (-1 != bdTime)
		{
			calBirth.setTime(new Date(bdTime));
			favorites.add(calBirth.getTime());
		}
		else
		{
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
    protected void onStop()
	{
		//	Log.d(getPackageName(), "onStop()");
		super.onStop();
		storeActivityPreferences();
    }

    /* (non-Javadoc)
     * @see android.app.ActivityGroup#onResume()
     */
    @Override
    protected void onResume()
	{
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
    protected void onRestoreInstanceState(Bundle state)
	{
		//	Log.d(getPackageName(), "onRestoreInstanceState()");
		super.onRestoreInstanceState(state);
		restoreActivityPreferences();
		if (state.containsKey("today"))
		{
			calToday.setTimeInMillis(state.getLong("today"));
		}
    }

    /* (non-Javadoc)
     * @see android.app.TabActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
	{
		//	Log.d(getPackageName(), "onSaveInstanceState()");
		super.onSaveInstanceState(outState);
		storeActivityPreferences();
		outState.putLong("today", calToday.getTimeInMillis());
    }


    /**
     * Invoked during init to give the Activity a chance to set up its Menu.
     * 
     * @param menu
     *            the Menu to which entries may be added
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


    /**
     * 
     * @return
     */
    protected AlertDialog createFavoriteDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setTitle(getResources().getString(R.string.favorite_dialog_title))
			.setCancelable(true)
			.setAdapter(favorites, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item)
				{
					Date choosed = favorites.getItem(item);
					calBirth.setTime(choosed);
					favorites.add(choosed);
					updateDisplay();
				}
			});
		AlertDialog alert = builder.create();
		return alert;
    }


    /**
     * 
     * @return
     */
    protected AlertDialog createAboutDialog()
	{	
		String versionName = "";
		try
		{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			versionName = pInfo.versionName;
		}
		catch (NameNotFoundException e)
		{
			Log.e(getPackageName(), "createAboutDialog() : PackageManager.GET_META_DATA", e);
		}	
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setMessage(android.text.Html.fromHtml(getResources().getString(R.string.About).replace("VERSION", versionName)))
			.setCancelable(false)
			.setPositiveButton(R.string.Button_Ok,
			new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
			    }
			});
		AlertDialog alert = builder.create();
		return alert;
    }

    /**
     * 
     */
    protected void updateDisplay()
	{
		bioview.invalidate();

		SimpleDateFormat df = new SimpleDateFormat(
			getResources().getString(R.string.format_date));
		tv_birth.setText(df.format(calBirth.getTime()));
		tv_today.setText(df.format(calToday.getTime()));

		int phaseP = bioview.getPhase(BioView.PHYSICAL);
		if (oldPhaseP != phaseP)
		{
//			((TextView) findViewById(R.id.tab_phy)).setText(desc_phy[phaseP]);
			oldPhaseP  = phaseP;
		}
		int phaseE = bioview.getPhase(BioView.EMOTIONAL);
		if (oldPhaseE != phaseE)
		{
//			((TextView) findViewById(R.id.tab_emo)).setText(desc_emo[phaseE]);
			oldPhaseE = phaseE;
		}
		int phaseI = bioview.getPhase(BioView.INTELECTUAL);
		if (oldPhaseI != phaseE)
		{
//			((TextView) findViewById(R.id.tab_int)).setText(desc_int[phaseI]);
			oldPhaseI = phaseI;
		}
    }

    /**
     * @param id
     *            wanted dialog id
     */
    @Override
    protected Dialog onCreateDialog(int id)
	{
		switch (id)
		{
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
    protected Calendar checkCalendar(Calendar cal)
	{
		if (cal.get(Calendar.YEAR) < cal.getActualMinimum(Calendar.YEAR))
		{
			cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		}
		if (cal.get(Calendar.YEAR) > cal.getActualMaximum(Calendar.YEAR))
		{
			cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		}
		if (cal.get(Calendar.MONTH) < cal.getActualMinimum(Calendar.MONTH))
		{
			cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		}
		if (cal.get(Calendar.MONTH) > cal.getActualMaximum(Calendar.MONTH))
		{
			cal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		}
		if (cal.get(Calendar.DAY_OF_MONTH) < cal.getActualMinimum(Calendar.DAY_OF_MONTH))
		{
			cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		}
		if (cal.get(Calendar.DAY_OF_MONTH) > cal.getActualMaximum(Calendar.DAY_OF_MONTH))
		{
			cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		}

		return cal;
    }

    /**
     * 
     * @return
     */
    private Dialog createTheoryDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setMessage(android.text.Html.fromHtml(getResources().getString(R.string.theory)))
			.setCancelable(false)
			.setPositiveButton(R.string.Button_Ok,
			new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
			    }
			});
		AlertDialog alert = builder.create();
		return alert;
    }



    /**
     * Invoked when the user selects an item from the Menu.
     * 
     * @param item
     *            the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
//			case R.id.menuBirthday:
//				DialogFragment dpBirthFragment = new DatePickerFragment(calBirth, true);
//				dpBirthFragment.show(getSupportFragmentManager(), "datePickerBirthday");
//				return true;
//			case R.id.menuForDay:
//				DialogFragment dpTodayFragment = new DatePickerFragment(calToday, false);
//				dpTodayFragment.show(getSupportFragmentManager(), "datePickerToday");
//				return true;
			case R.id.menuHistory:
				showDialog(DIALOG_FAVORITE);
				return true;
			case R.id.menuAbout:
				showDialog(DIALOG_ABOUT);
				return true;
			case R.id.menuTheory:
				showDialog(DIALOG_THEORY);
				return true;
		}
		return false;
    }


    @Override
    public Calendar getEndCalendar()
	{
		return calToday;
    }

    @Override
    public Calendar getStartCalendar()
	{
		return calBirth;
    }

    @Override
    public void bioHolderChanged()
	{
		updateDisplay();
    }

    protected class TabListener implements ActionBar.TabListener
    {

		@Override
		public void onTabSelected(ActionBar.Tab p1, android.app.FragmentTransaction p2)
		{
			
		}

		@Override
		public void onTabUnselected(ActionBar.Tab p1, android.app.FragmentTransaction p2)
		{
			// TODO: Implement this method
		}

		@Override
		public void onTabReselected(ActionBar.Tab p1, android.app.FragmentTransaction p2)
		{
			// TODO: Implement this method
		}


	
    }
//
//    /**
//     * 
//     * @author tay
//     *
//     */
//	protected class BirthdayPickerFragment extends DialogFragment
//	implements DatePickerDialog.OnDateSetListener
//	{
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState)
//		{
//			return new DatePickerDialog(getActivity(), this, 
//										calBirth.get(Calendar.YEAR), 
//										calBirth.get(Calendar.MONTH), 
//										calBirth.get(Calendar.DAY_OF_MONTH));
//		}
//
//		public void onDateSet(DatePicker view, int year, int month, int day)
//		{
//			calBirth.set(year, month, day);
//			favorites.add(calBirth.getTime());
//			storeActivityPreferences();				
//			updateDisplay();
//		}
//	}
//
//    /**
//     * 
//     * @author tay
//     *
//     */
//	protected class DatePickerFragment extends DialogFragment
//	implements DatePickerDialog.OnDateSetListener
//	{
//		private Calendar calendar;
//		private boolean isBirthday;
//
//
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState)
//		{
//			return new DatePickerDialog(getActivity(), this, 
//										calendar.get(Calendar.YEAR), 
//										calendar.get(Calendar.MONTH), 
//										calendar.get(Calendar.DAY_OF_MONTH));
//		}
//
//		public void onDateSet(DatePicker view, int year, int month, int day)
//		{
//			calendar.set(year, month, day);
//			if (isBirthday)
//			{
//				favorites.add(calBirth.getTime());
//				storeActivityPreferences();				
//			}
//			updateDisplay();
//		}
//	}
	
	/**
	 * 
	 * @author tay
	 * 
	 */
	public class TabAdapter extends FragmentPagerAdapter {
		private TabFragement[] mTabs;

		public TabAdapter(FragmentManager fm) {
			super(fm);

			mTabs = new TabFragement[3];
			int pos = 0;

			TabFragement tf = new TabFragement();
			tf.setPosition(pos);
			tf.setTitle(getResources().getText(R.string.tab_phy));
			tf.setDescription(getResources().getStringArray(R.array.desc_phy));
			tf.setIcon(getResources().getDrawable(R.drawable.sin_phy));
			mTabs[pos++] = tf;

			tf = new TabFragement();
			tf.setPosition(pos);
			tf.setTitle(getResources().getText(R.string.tab_emo));
			tf.setDescription(getResources().getStringArray(R.array.desc_emo));
			tf.setIcon(getResources().getDrawable(R.drawable.sin_emo));
			mTabs[pos++] = tf;

			tf = new TabFragement();
			tf.setPosition(pos);
			tf.setTitle(getResources().getText(R.string.tab_int));
			tf.setDescription(getResources().getStringArray(R.array.desc_int));
			tf.setIcon(getResources().getDrawable(R.drawable.sin_int));
			mTabs[pos++] = tf;
		}

		/*
		 * @see android.support.v4.view.PagerAdapter#getCount()
		 */
		@Override
		public int getCount() {
			return mTabs.length;
		}

		/*
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {
			return mTabs[position];
		}

		/*
		 * @see android.support.v4.view.PagerAdapter#getPageTitle(int)
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			return mTabs[position].getTitle();
		}

		/**
		 * 
		 * @param position
		 * @return
		 */
		public Drawable getIcon(int position) {
			return mTabs[position].getIcon();
		}
	}

	/*
	 * @see
	 * android.app.ActionBar.TabListener#onTabReselected(android.app.ActionBar
	 * .Tab, android.app.FragmentTransaction)
	 */
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	/*
	 * @see
	 * android.app.ActionBar.TabListener#onTabSelected(android.app.ActionBar
	 * .Tab, android.app.FragmentTransaction)
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	/*
	 * @see
	 * android.app.ActionBar.TabListener#onTabUnselected(android.app.ActionBar
	 * .Tab, android.app.FragmentTransaction)
	 */
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
}
