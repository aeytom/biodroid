package de.taytec.biodroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class TheoryFragment extends DialogFragment {

    public static final java.lang.String TAG = "TheoryFragment";

    public static TheoryFragment newInstance() {
        TheoryFragment fragment = new TheoryFragment();
        return fragment;
    }

    public TheoryFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setMessage(android.text.Html.fromHtml(getResources().getString(R.string.theory)))
                .setCancelable(false)
                .setPositiveButton(R.string.button_ok, null);
        return builder.create();
    }
}
