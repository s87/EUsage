package de.s87.eusage;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class CustomCheckBox extends CheckBox {

	public CustomCheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomCheckBox(Context context) {
		super(context);
	}
	
	protected void onTextChanged(CharSequence text, int start, int before,
			int after) {
		if (text.toString().compareTo("") != 0) {
			setChecked(text.toString().compareTo("1") == 0 ? true : false);
			setText("");
		}
	}

}
