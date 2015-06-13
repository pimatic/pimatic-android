package org.pimatic.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class AlertDialogFragment extends DialogFragment {

    public static AlertDialogFragment newInstance(int title) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");

        return new AlertDialog.Builder(getActivity())
                //.setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title)
//                .setPositiveButton(R.string.alert_dialog_ok,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                ((FragmentAlertDialog) getActivity()).doPositiveClick();
//                            }
//                        }
//                )
//                .setNegativeButton(R.string.alert_dialog_cancel,
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                ((FragmentAlertDialog)getActivity()).doNegativeClick();
//                            }
//                        }
//                )
                .create();
    }
}