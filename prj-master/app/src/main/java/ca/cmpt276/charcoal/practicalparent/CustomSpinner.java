package ca.cmpt276.charcoal.practicalparent;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Creates preset custom timer for alarm
 * Spinner extension that calls onItemSelected even when the selection is the same as its previous value
 * COPIED FROM: https://stackoverflow.com/questions/5335306/how-can-i-get-an-event-in-android-spinner-when-the-current-selected-item-is-sele/11323043#11323043
 */
public class CustomSpinner extends androidx.appcompat.widget.AppCompatSpinner {
    public CustomSpinner(Context context) {
        super(context);
    }

    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override public void
    setSelection(int position, boolean animate) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position, animate);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }

    @Override public void
    setSelection(int position) {
        boolean sameSelected = position == getSelectedItemPosition();
        super.setSelection(position);
        if (sameSelected) {
            // Spinner does not call the OnItemSelectedListener if the same item is selected, so do it manually now
            getOnItemSelectedListener().onItemSelected(this, getSelectedView(), position, getSelectedItemId());
        }
    }
}