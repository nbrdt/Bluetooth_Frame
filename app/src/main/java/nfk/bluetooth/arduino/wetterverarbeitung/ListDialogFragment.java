package nfk.bluetooth.arduino.wetterverarbeitung;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;

/**
 * @author KI
 * @version 1.0
 **/
@Deprecated
public class ListDialogFragment extends AppCompatDialogFragment {
    private static final String INSTANCESTATE_ITEMS = "Items";
    private static final String INSTANCESTATE_TITLE = "Title";
    private static final String DEFAULT_TITLE = "List Dialog";
    public static final String LOG_TAG = "ListDialogFragment";

    private DialogInterface.OnClickListener onItemPickListener;

    public ListDialogFragment() {
        ;
    }

    public static ListDialogFragment getInstance(@NonNull CharSequence[] items, @NonNull DialogInterface.OnClickListener onClickListener, @Nullable String title) {
        ListDialogFragment fragment = new ListDialogFragment();
        Bundle args = new Bundle();
        args.putCharSequenceArray(INSTANCESTATE_ITEMS, items);
        args.putString(INSTANCESTATE_TITLE, title);
        fragment.setArguments(args);
        fragment.setOnItemPickListener(onClickListener);
        return fragment;
    }

    public static ListDialogFragment getInstance(@NonNull CharSequence[] items, @NonNull DialogInterface.OnClickListener onClickListener) {
        return getInstance(items, onClickListener, null);
    }

    private void setOnItemPickListener(DialogInterface.OnClickListener listener) {
        onItemPickListener = listener;
    }


    @Override
    public
    @NonNull
    Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            String title = args.getString(INSTANCESTATE_TITLE);
            if ((title != null) && (!title.equalsIgnoreCase(""))) {
                builder.setTitle(title);
            } else {
                Log.w(LOG_TAG, "Had to use Default title, because of an invalid title");
                builder.setTitle(DEFAULT_TITLE);
            }
            builder.setItems(args.getCharSequenceArray(INSTANCESTATE_ITEMS), onItemPickListener);
            return builder.create();
        } else {
            Log.w(LOG_TAG, "Had to use default Dialog, because Arguments could not be read");
            return super.onCreateDialog(savedInstanceState);
        }
    }
}
