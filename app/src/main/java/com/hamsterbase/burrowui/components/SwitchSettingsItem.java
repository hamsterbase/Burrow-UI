package com.hamsterbase.burrowui.components;

import android.content.Context;
import android.util.AttributeSet;

import com.hamsterbase.burrowui.R;

public class SwitchSettingsItem extends SettingsItem {

    private boolean switchStatus;
    private OnSwitchChangeListener switchChangeListener;

    public interface OnSwitchChangeListener {
        void onSwitchChanged(boolean isChecked);
    }

    public SwitchSettingsItem(Context context, String title, String desc, boolean switchStatus, OnSwitchChangeListener saveState) {
        super(context);
        init(context, title, desc, switchStatus, saveState);
    }

    public SwitchSettingsItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, "", "", false, null);
    }

    private void init(Context context, String title, String desc, boolean switchStatus, OnSwitchChangeListener saveState) {
        setTitle(title);
        setDescription(desc);
        this.switchStatus = switchStatus;
        this.switchChangeListener = saveState;

        updateSwitchIcon();

        setOnSectionClickListener(v -> {
            toggleSwitch();
            if (switchChangeListener != null) {
                switchChangeListener.onSwitchChanged(this.switchStatus);
            }
        });
    }

    private void updateSwitchIcon() {
        setIcon(switchStatus ? R.drawable.ic_switch_on : R.drawable.ic_switch_off);
    }


    private void toggleSwitch() {
        switchStatus = !switchStatus;
        updateSwitchIcon();
    }
}
