package de.taytec.biodroid;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TabFragment extends Fragment {

    private static final String STATE_KEY_POSITION = "position";
    private static final String STATE_KEY_ICON_ID = "icon_id";
    private static final String STATE_KEY_TITLE_ID = "title_id";
    private static final String STATE_KEY_DESCRIPTION = "description";

    private TextView mDescriptionView;

    public static TabFragment getInstance(int position, int iconId, int titleId, String description) {
        Bundle arguments = new Bundle();
        arguments.putInt(STATE_KEY_POSITION, position);
        arguments.putInt(STATE_KEY_ICON_ID, iconId);
        arguments.putInt(STATE_KEY_TITLE_ID, titleId);
        arguments.putString(STATE_KEY_DESCRIPTION, description);

        TabFragment tf = new TabFragment();
        tf.setArguments(arguments);
        return tf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int position = getArguments().getInt(STATE_KEY_POSITION);
        Log.d(getClass().getSimpleName(), "onCreateView() " + position + " – " + this.toString());

        View view = getView();
        if (view == null) {
            Log.d(getClass().getSimpleName(), "onCreateView() - new");
            view = inflater.inflate(R.layout.tab_fragment, container, false);

            Drawable icon = view.getResources().getDrawable(getArguments().getInt(STATE_KEY_ICON_ID));
            ((ImageView) view.findViewById(R.id.iconDescription)).setImageDrawable(icon);

            String title = view.getResources().getString(getArguments().getInt(STATE_KEY_TITLE_ID));
            ((TextView) view.findViewById(R.id.textTitleDescription)).setText(title);
        } else if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }

        mDescriptionView = (TextView) view.findViewById(R.id.descriptionText);
        showDescription(getArguments().getString(STATE_KEY_DESCRIPTION, ""));

        return view;
    }

    public void showDescription(String description) {
        int position = getArguments().getInt(STATE_KEY_POSITION);
        Log.d(getClass().getSimpleName(), "showDescription() " + position + " – " + toString());

        getArguments().putString(STATE_KEY_DESCRIPTION, description);
        if (mDescriptionView != null) {
            Log.d(getClass().getSimpleName(), "showDescription() changed " + position);
            mDescriptionView.setText(description);
        } else {
            Log.d(getClass().getSimpleName(), "    null == mDescriptionView " + position);
        }
    }

}
