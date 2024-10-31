package com.hamsterbase.burrowui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsItem extends LinearLayout {

    private static final int HORIZONTAL_MARGIN_DP = 24;
    private static final int VERTICAL_PADDING_DP = 16;
    private static final int TITLE_TEXT_SIZE_SP = 18;
    private static final int DESC_TEXT_SIZE_SP = 14;
    private static final int TITLE_DESC_MARGIN_DP = 4;

    private TextView titleTextView;
    private TextView descTextView;
    private ImageView iconImageView;

    public SettingsItem(Context context) {
        super(context);
        init(context, null);
    }

    public SettingsItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        setClickable(true);
        setFocusable(true);

        int horizontalMargin = dpToPx(context, HORIZONTAL_MARGIN_DP);
        int verticalPadding = dpToPx(context, VERTICAL_PADDING_DP);

        setPadding(horizontalMargin, verticalPadding, horizontalMargin, verticalPadding);

        // Left section
        LinearLayout leftSection = new LinearLayout(context);
        leftSection.setOrientation(VERTICAL);
        LayoutParams leftParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        leftSection.setLayoutParams(leftParams);

        titleTextView = new TextView(context);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TITLE_TEXT_SIZE_SP);
        leftSection.addView(titleTextView);

        descTextView = new TextView(context);
        descTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, DESC_TEXT_SIZE_SP);
        descTextView.setTextColor(Color.parseColor("#757575"));
        LayoutParams descParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = dpToPx(context, TITLE_DESC_MARGIN_DP);
        descTextView.setLayoutParams(descParams);
        leftSection.addView(descTextView);

        addView(leftSection);

        // Right section
        LinearLayout rightSection = new LinearLayout(context);
        rightSection.setOrientation(LinearLayout.VERTICAL);
        rightSection.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(dpToPx(context, 100), LinearLayout.LayoutParams.MATCH_PARENT);
        rightSection.setLayoutParams(rightParams);

        iconImageView = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        iconImageView.setLayoutParams(iconParams);
        rightSection.addView(iconImageView);
        addView(rightSection);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setDescription(String description) {
        descTextView.setText(description);
    }


    public void setIcon(int resId) {
        iconImageView.setImageResource(resId);
    }


    public void setIcon(Drawable drawable) {
        iconImageView.setImageDrawable(drawable);
    }

    public void setOnSectionClickListener(OnClickListener listener) {
        setOnClickListener(listener);
    }

    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
