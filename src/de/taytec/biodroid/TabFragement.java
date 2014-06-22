package de.taytec.biodroid;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class TabFragement extends Fragment {

	private View mView;
	private int position;
	private String[] description;
	private CharSequence title;
	private Drawable icon;
	private int Phase;


	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mView != null && mView.getParent() instanceof ViewGroup) {
			Log.d(getClass().getSimpleName(), "onCreateView() - recycled");
			((ViewGroup) mView.getParent()).removeView(mView);
		} else {
			Log.d(getClass().getSimpleName(), "onCreateView() - new");
			mView = inflater.inflate(R.layout.tab_fragment, container, false);
			((TextView)mView.findViewById(R.id.textTitleDescription)).setText(title);
			((ImageView)mView.findViewById(R.id.iconDescription)).setImageDrawable(icon);
			showDescription(Phase);
		}
		return mView;
	}

	public CharSequence getTitle() {
		return title;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setDescription(String[] description) {
		this.description = description;
	}

	public void showDescription(int phase)
	{
		this.Phase = phase;
		if (mView != null) {
			TextView text = (TextView) mView.findViewById(R.id.descriptionText);
			text.setText(description[phase]);			
		}
	}
	
	public void setTitle(CharSequence text) {
		this.title = text;
		if (mView != null) {
			((TextView)mView.findViewById(R.id.textTitleDescription)).setText(title);			
		}
	}

	public void setIcon(Drawable drawable) {
		this.icon = drawable;
		if (mView != null) {
			((ImageView)mView.findViewById(R.id.iconDescription)).setImageDrawable(drawable);
		}
	}

	public Drawable getIcon() {
		return icon;
	}

}
