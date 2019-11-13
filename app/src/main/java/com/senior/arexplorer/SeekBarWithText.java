package com.senior.arexplorer;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarWithText extends LinearLayout {
    TextView text;
    SeekBar seekBar;

    public SeekBarWithText(Context context) {
        super(context);
        text = new TextView(context);
        seekBar = new SeekBar(context);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        text.setLayoutParams(params);
        text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        seekBar.setLayoutParams(params);

        addView(text);
        addView(seekBar);

        setOrientation(LinearLayout.VERTICAL);
        setLayoutParams(params);

    }

    public SeekBarWithText setSeekbarMinMaxStep(int min, int max, int step){
        seekBar.setMax(max - min);
        seekBar.setKeyProgressIncrement(step);
        return this;
    }

    public SeekBarWithText setSeekbarText(String textIn){
        text.setText(textIn);
        return this;
    }

    public SeekBarWithText setSeekbarProgress(int progress){
        seekBar.setProgress(progress);
        return this;
    }

    public SeekBarWithText setSeekbarListener(SeekBar.OnSeekBarChangeListener listener){
        seekBar.setOnSeekBarChangeListener(listener);
        return this;
    }

}
