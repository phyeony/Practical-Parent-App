package ca.cmpt276.charcoal.practicalparent;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * A fragment launched in the TimeOutActivity to allow the user to change the speed of the timer
 */
// Reference - Bottom sheet fragment tutorial:
//   https://codinginflow.com/tutorials/android/modal-bottom-sheet
public class TimeOutBottomSheetFragment extends BottomSheetDialogFragment {
    private BottomSheetListener listener;
    private TextView timeSpeedPercentageText;
    private int timeScaleIndex;
    private double[] timeScaleOptions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        timeScaleIndex = getArguments().getInt(TimeOutActivity.TIME_SCALE_INDEX_TAG);
        timeScaleOptions = getArguments().getDoubleArray(TimeOutActivity.TIME_SCALE_OPTIONS_TAG);
        int timeScalePercentage = (int)(timeScaleOptions[timeScaleIndex] * 100);

        View view = inflater.inflate(R.layout.fragment_bottom_sheet_time_out, container, false);

        timeSpeedPercentageText = view.findViewById(R.id.text_time_rate);
        timeSpeedPercentageText.setText(getString(R.string.msg_timer_speed_percentage, timeScalePercentage));

        setUpButtons(view);

        return view;
    }

    private void setUpButtons(View view) {
        ImageButton plusButton = view.findViewById(R.id.button_speed_up_time);
        plusButton.setOnClickListener(v -> {
            if (timeScaleIndex+1 < timeScaleOptions.length) {
                timeScaleIndex++;
                int timeScalePercentage = (int)(timeScaleOptions[timeScaleIndex] * 100);
                timeSpeedPercentageText.setText(getString(R.string.msg_timer_speed_percentage, timeScalePercentage));
                listener.onFragmentButtonClick(timeScaleIndex);
            }
        });

        ImageButton minusButton = view.findViewById(R.id.button_slow_down_time);
        minusButton.setOnClickListener(v -> {
            if (timeScaleIndex-1 >= 0) {
                timeScaleIndex--;
                int timeScalePercentage = (int)(timeScaleOptions[timeScaleIndex] * 100);
                timeSpeedPercentageText.setText(getString(R.string.msg_timer_speed_percentage, timeScalePercentage));
                listener.onFragmentButtonClick(timeScaleIndex);
            }
        });
    }

    public interface BottomSheetListener {
        void onFragmentButtonClick(int newTimeScaleIndex);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }
}