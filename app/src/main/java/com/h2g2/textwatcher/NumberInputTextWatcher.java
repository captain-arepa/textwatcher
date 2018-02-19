package com.h2g2.textwatcher;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by C. Serrano (cserrano@teravisiontech.com)
 * Teravision Technologies
 * Date: 2018/02/15
 */
public class NumberInputTextWatcher implements TextWatcher {

    private Locale DEFAULT_LOCALE;
    private DecimalFormat NUMBER_FORMAT;
    private int FRACTION_DIGITS = 2;
    private char DECIMAL_SEPARATOR;
    private char GROUPING_SEPARATOR;
    private EditText et;
    private String regex;
    private String current = "";

    private int startChanged;
    private int beforeChanged;
    private int countChanged;
    private boolean busy = false;
    private int DECIMAL_CHAR_INDEX;
    private int place;
    private StringBuilder num;
    private String v_text, v_formatted;
    private int count;
    private char c;
    private BigDecimal v_value;
    private NumberFormat formatter;

    public NumberInputTextWatcher() {

    }

    public NumberInputTextWatcher(EditText price, char decimal, char grouping, Locale locale) {
        this.et = price;
        this.DECIMAL_SEPARATOR = decimal;
        this.GROUPING_SEPARATOR = grouping;
        this.DEFAULT_LOCALE = locale;
        this.NUMBER_FORMAT = new DecimalFormat("0" + DECIMAL_SEPARATOR + "00");
        this.regex = "[" + this.DECIMAL_SEPARATOR + this.GROUPING_SEPARATOR + "]";
        this.formatter = NumberFormat.getNumberInstance(DEFAULT_LOCALE);
        this.regex = "[" + this.DECIMAL_SEPARATOR + this.GROUPING_SEPARATOR + "]";
        formatter.setMaximumFractionDigits(2); //Don't know if this is correct, but welp!
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        startChanged = start;
        beforeChanged = before;
        countChanged = count;
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Do nothing here
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().equals(current)) {
            et.removeTextChangedListener(this);

            //Delete all decimal and grouping characters
            v_text = s.toString().replaceAll(regex, "");
            v_value = new BigDecimal(0);

            //Set value with BigDecimal instead of Double (for precision)
            if (v_text != null && v_text.length() > 0) {
                v_value = new BigDecimal(v_text)
                        .setScale(2, BigDecimal.ROUND_FLOOR)
                        .divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            }

            //Set formatted value and make string builder
            v_formatted = NUMBER_FORMAT.format(v_value);
            num = new StringBuilder(v_formatted);

            //Set ending zero-value
            setEndingZeroValue();
            setGroupingSeparators();


            //assign values
            v_formatted = num.toString();
            current = v_formatted;

            //set text and selection
            et.setText(v_formatted);

            // set cursor to the end after text is formatted
            et.setSelection(v_formatted.length()); //???
            et.setSelection(startChanged + countChanged); //???

            et.addTextChangedListener(this);
        }
    }

    private void setGroupingSeparators() {
        //Piedra Solutions, Inc. (just some Frankencode, adapted from a Kotlin excerpt
        //Source: https://stackoverflow.com/a/45993013
        place = 0;

        DECIMAL_CHAR_INDEX = num.indexOf(String.valueOf(DECIMAL_SEPARATOR));
        if (DECIMAL_CHAR_INDEX == -1) {
            count = num.length() - 1;
        } else {
            count = DECIMAL_CHAR_INDEX - 1;
        }

        while (count >= 0) {
            c = num.charAt(count);
            if (c == GROUPING_SEPARATOR) {
                num.delete(count, count + 1);
            } else {
                if (place % 3 == 0 && place != 0) {
                    // insert a comma to the left of every 3rd digit (counting from right to
                    // left) unless it's the leftmost digit
                    num.insert(count + 1, String.valueOf(GROUPING_SEPARATOR));
                }
                place++;
            }
            count--;
        }
    }

    private void setEndingZeroValue() {
        if (num.toString().equals("0")) {
            while (num.length() < 3)
                num.insert(0, '0');
            num.insert(num.length() - 2, DECIMAL_SEPARATOR);
        }
    }
}
