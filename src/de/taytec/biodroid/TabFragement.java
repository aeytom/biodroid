package de.taytec.biodroid;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabFragement extends Fragment {

	private View mView;
	private int position;
	private String[] description;
	private CharSequence title;
	private Drawable icon;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mView != null && mView.getParent() instanceof ViewGroup) {
			Log.d(getClass().getSimpleName(), "onCreateView() - recycled");
			((ViewGroup) mView.getParent()).removeView(mView);
		} else {
			Log.d(getClass().getSimpleName(), "onCreateView() - new");
			mView = inflater.inflate(R.layout.tab_fragment, container, false);
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

	public void setTitle(CharSequence text) {
		this.title = text;
	}

	public void setIcon(Drawable drawable) {
		this.icon = drawable;
	}

	public Drawable getIcon() {
		return icon;
	}

}
