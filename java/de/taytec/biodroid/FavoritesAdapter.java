package de.taytec.biodroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FavoritesAdapter extends ArrayAdapter<Date> implements SpinnerAdapter {
    private static final String STANDARD_DATE_FORMAT = "yyyy-mm-dd";
    public static final int MAX_FAVORITES = 5;
    private DateFormat _dateFormat;

    /**
     * @param context
     * @param dateFormat
     */
    public FavoritesAdapter(Context context, DateFormat dateFormat) {
        super(context, android.R.layout.select_dialog_item);
        _dateFormat = dateFormat;
    }

    /**
     * @param context
     * @param dateFormat
     * @param textViewResourceId defaults to android.R.layout.select_dialog_item
     */
    public FavoritesAdapter(Context context, DateFormat dateFormat,
                            int textViewResourceId) {
        super(context, textViewResourceId);
        _dateFormat = dateFormat;
    }

    /**
     * @param stringArray
     * @return
     */
    public FavoritesAdapter restoreData(String[] stringArray) {
        SimpleDateFormat df = new SimpleDateFormat(STANDARD_DATE_FORMAT);
        clear();
        for (int i = stringArray.length - 1; i >= 0; i--) {
            try {
                BioDate item = new BioDate(df.parse(stringArray[i]));
                add(item);
            } catch (ParseException e) {
                Log.e("BioDroid", "Restore favorites", e);
            }
        }
        notifyDataSetChanged();
        return this;
    }

    /**
     * @see android.widget.ArrayAdapter#add(java.lang.Object)
     */
    @Override
    public void add(Date item) {
        BioDate ins = (item instanceof BioDate) ? (BioDate) item : new BioDate(item);
        if (getCount() == 0) {
            super.add(ins);
        } else if (ins != getItem(0)) {
            remove(ins);
            if (getCount() >= MAX_FAVORITES) {
                remove(getItem(getCount() - 1));
            }
            super.insert(ins, 0);
        }
    }

    /**
     * @param df
     * @return
     */
    public String[] toStringArray(DateFormat df) {
        String[] fd = new String[getCount()];
        for (int i = getCount() - 1; i >= 0; i--) {
            fd[i] = df.format(getItem(i));
        }
        return fd;

    }

    /**
     * uses STANDARD_DATE_FORMAT
     *
     * @return
     */
    public String[] toStringArray() {
        SimpleDateFormat df = new SimpleDateFormat(STANDARD_DATE_FORMAT);
        return toStringArray(df);
    }

    /**
     * persistent save the activity state
     *
     * @param ed
     * @param prefix
     */
    public void storeToPreferences(Editor ed, String prefix) {
        ed.putInt(prefix + ".length", getCount());
        for (int i = getCount() - 1; i >= 0; i--) {
            ed.putLong(prefix + "." + Integer.toString(i), getItem(i).getTime());
        }
    }

    /**
     * restore activity state from preferences
     *
     * @param preferences
     * @param prefix
     */
    public void restoreFromPreferences(SharedPreferences preferences, String prefix) {
        int hl = preferences.getInt(prefix + ".length", 0);
        for (int i = hl - 1; i >= 0; i--) {
            long millis = preferences.getLong(prefix + "." + Integer.toString(i), -1);
            if (-1 != millis) {
                add(new Date(millis));
            }
        }
    }

    class BioDate extends Date {

        /**
         *
         */
        private static final long serialVersionUID = -5992586306149410193L;

        public BioDate(Date date) {
            super(date.getTime());
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return _dateFormat.format(this);
        }

    }
}
