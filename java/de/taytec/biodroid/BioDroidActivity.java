        package de.taytec.biodroid;


        import android.app.ActionBar;
        import android.app.ActionBar.Tab;
        import android.app.ActionBar.TabListener;
        import android.app.AlertDialog;
        import android.app.DatePickerDialog;
        import android.app.Dialog;
        import android.app.FragmentTransaction;
        import android.content.DialogInterface;
        import android.content.SharedPreferences;
        import android.content.SharedPreferences.Editor;
        import android.content.pm.PackageInfo;
        import android.content.pm.PackageManager;
        import android.content.pm.PackageManager.NameNotFoundException;
        import android.graphics.drawable.Drawable;
        import android.os.Bundle;
        import android.support.v4.app.DialogFragment;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentActivity;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentPagerAdapter;
        import android.support.v4.view.ViewPager;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemSelectedListener;
        import android.widget.Button;
        import android.widget.DatePicker;
        import android.widget.Spinner;

        import java.text.SimpleDateFormat;
        import java.util.Calendar;
        import java.util.Date;

        public class BioDroidActivity extends FragmentActivity implements BioView.BioCalendarProvider, TabListener, OnItemSelectedListener, View.OnLongClickListener
{

    protected final int DIALOG_FAVORITE = 2;
    protected final int DIALOG_ABOUT = 3;
    private static final int DIALOG_THEORY = 5;
 
    protected Spinner tv_birth;
    protected Button tv_today;

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

		tv_birth = (Spinner) findViewById(R.id.editBirthday);
		tv_today = (Button) findViewById(R.id.editToday);

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);

        mTabAdapter =
                new TabAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.descPager);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mViewPager.setCurrentItem(position);
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
        
        Spinner spinner = (Spinner) findViewById(R.id.editBirthday);
        favorites.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setOnItemSelectedListener(this);
		spinner.setOnLongClickListener(this);
        spinner.setAdapter(favorites);

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.actionbar_buttons, menu);
        return super.onCreateOptionsMenu(menu);
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
		tv_today.setText(df.format(calToday.getTime()));
        favorites.add(calBirth.getTime());

		int phaseP = bioview.getPhase(BioView.PHYSICAL);
		if (oldPhaseP != phaseP)
		{
			mTabAdapter.showDescription(0, phaseP);
			oldPhaseP  = phaseP;
		}
		int phaseE = bioview.getPhase(BioView.EMOTIONAL);
		if (oldPhaseE != phaseE)
		{
			mTabAdapter.showDescription(1, phaseE);
			oldPhaseE = phaseE;
		}
		int phaseI = bioview.getPhase(BioView.INTELECTUAL);
		if (oldPhaseI != phaseI)
		{
			mTabAdapter.showDescription(2, phaseI);
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
            case R.id.menuBirthday:
                showBirthdayPickerDialog(null);
                return true;
            case R.id.menuForDay:
                showDatePickerDialog(null);
                return true;
            case R.id.menuHistory:
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
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
    {
        Favorites.BioDate choosed = (Favorites.BioDate)parent.getItemAtPosition(position);
        calBirth.setTime(choosed);
        favorites.add(choosed);
        updateDisplay();    
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        showBirthdayPickerDialog(null);
    }


	@Override
	public boolean onLongClick(View p1)
	{
		showBirthdayPickerDialog(null);
		return false;
	}
	
    
    public void showBirthdayPickerDialog(View v) {
        DialogFragment newFragment = new BirthdayPickerFragment();
        newFragment.show(getSupportFragmentManager(), "birthdayPicker");
    }

    
	private class BirthdayPickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			return new DatePickerDialog(getActivity(), this, 
										calBirth.get(Calendar.YEAR), 
										calBirth.get(Calendar.MONTH), 
										calBirth.get(Calendar.DAY_OF_MONTH));
		}

		public void onDateSet(DatePicker view, int year, int month, int day)
		{
			calBirth.set(year, month, day);
			favorites.add(calBirth.getTime());
			storeActivityPreferences();				
			updateDisplay();
		}
	}


    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    
	private class DatePickerFragment extends DialogFragment
	implements DatePickerDialog.OnDateSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState)
		{
			return new DatePickerDialog(getActivity(), this, 
										calToday.get(Calendar.YEAR), 
										calToday.get(Calendar.MONTH), 
										calToday.get(Calendar.DAY_OF_MONTH));
		}

		public void onDateSet(DatePicker view, int year, int month, int day)
		{
			calToday.set(year, month, day);
			updateDisplay();
		}
	}
	
	/**
	 * 
	 * @author tay
	 * 
	 */
	public class TabAdapter extends FragmentPagerAdapter {
		private TabFragment[] mTabs;

		public TabAdapter(FragmentManager fm) {
			super(fm);

			mTabs = new TabFragment[3];
			int pos = 0;

			TabFragment tf = new TabFragment();
			tf.setPosition(pos);
			tf.setTitle(getResources().getText(R.string.tab_phy));
			tf.setDescription(getResources().getStringArray(R.array.desc_phy));
			tf.setIcon(getResources().getDrawable(R.drawable.sin_phy));
			mTabs[pos++] = tf;

			tf = new TabFragment();
			tf.setPosition(pos);
			tf.setTitle(getResources().getText(R.string.tab_emo));
			tf.setDescription(getResources().getStringArray(R.array.desc_emo));
			tf.setIcon(getResources().getDrawable(R.drawable.sin_emo));
			mTabs[pos++] = tf;

			tf = new TabFragment();
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

        /**
         *
         * @param position
         * @param phase
         */
		public void showDescription(int position, int phase) {
			mTabs[position].showDescription(phase);
		}
		
	}

    /*
     * Interface TabListener
     */

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(getPackageName(), "(BioDroidActivity:TabListener) : onTabSelected()");
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(getPackageName(), "(BioDroidActivity:TabListener) : onTabUnselected()");
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(getPackageName(), "(BioDroidActivity:TabListener) : onTabReselected()");
    }

    /*
     * Interface BioView.BioCalendarProvider
     */

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
    public void onScroll()
    {
        updateDisplay();
    }
}
