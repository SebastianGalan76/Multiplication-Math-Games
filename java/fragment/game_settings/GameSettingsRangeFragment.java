package com.coresaken.multiplication.fragment.game_settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.RangeType;
import com.coresaken.multiplication.service.JsonFileReader;

import java.text.ParseException;


public class GameSettingsRangeFragment extends Fragment {

    private RangeType selectedRange;
    public Settings presetSettings;

    ConstraintLayout cl_range_ab;
    ConstraintLayout cl_range_result;

    EditText v_aMin;
    EditText v_aMax;
    EditText v_bMin;
    EditText v_bMax;
    EditText v_cMin;
    EditText v_cMax;

    ConstraintLayout cl_fragment_container;

    TextView tv_info;

    public static GameSettingsRangeFragment newInstance() {
        return new GameSettingsRangeFragment();
    }

    public void loadPresetSettings(Settings presetSettings){
        this.presetSettings = presetSettings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_settings_range, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl_range_ab = view.findViewById(R.id.cl_range_ab_container);
        cl_range_result = view.findViewById(R.id.cl_range_result_container);

        v_aMin =  view.findViewById(R.id.v_range_ab_a_from);
        v_aMax =  view.findViewById(R.id.v_range_ab_a_to);
        v_bMin =  view.findViewById(R.id.v_range_ab_b_from);
        v_bMax =  view.findViewById(R.id.v_range_ab_b_to);
        v_cMin =  view.findViewById(R.id.v_range_result_from);
        v_cMax =  view.findViewById(R.id.v_range_result_to);

        tv_info = view.findViewById(R.id.tv_range_info);

        cl_fragment_container = view.findViewById(R.id.cl_fragment_container);

        if((presetSettings!=null)){
            if(presetSettings.rangeType!=null || presetSettings.modeType == ModeType.CORRECTION){
                cl_fragment_container.setVisibility(View.GONE);
            }
        }

        selectRange(JsonFileReader.selectedRangeType);

        int[] values = JsonFileReader.rangeValue;
        v_aMin.setText(String.valueOf(values[0]));
        v_aMax.setText(String.valueOf(values[1]));
        v_bMin.setText(String.valueOf(values[2]));
        v_bMax.setText(String.valueOf(values[3]));
        v_cMin.setText(String.valueOf(values[4]));
        v_cMax.setText(String.valueOf(values[5]));
    }

    public void changeType(View view){
        if(view.getId()==R.id.cl_range_ab_container){
            selectRange(RangeType.AB);
        }
        else if(view.getId()==R.id.cl_range_result_container){
            selectRange(RangeType.RESULT);
        }
    }

    public void selectRange(RangeType rangeType){
        if(rangeType == RangeType.AB){
            if(selectedRange != RangeType.AB){
                selectedRange = RangeType.AB;

                cl_range_ab.setEnabled(false);
                cl_range_result.setEnabled(true);

                changeTextColor(cl_range_ab,  getContext().getColor(R.color.white));
                changeTextColor(cl_range_result,  getContext().getColor(R.color.normal_text));

                changeEditText(cl_range_ab,  true);
                changeEditText(cl_range_result,  false);
            }
        }
        else if(rangeType == RangeType.RESULT){
            if(selectedRange != RangeType.RESULT){
                selectedRange = RangeType.RESULT;

                cl_range_result.setEnabled(false);
                cl_range_ab.setEnabled(true);

                changeTextColor(cl_range_result, getContext().getColor(R.color.white));
                changeTextColor(cl_range_ab, getContext().getColor(R.color.normal_text));

                changeEditText(cl_range_result, true);
                changeEditText(cl_range_ab, false);
            }
        }
    }

    private void changeTextColor(View parent, int colorId) {
        try {
            if (parent instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) parent;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    changeTextColor(child, colorId);
                }
            } else if (parent instanceof TextView && !(parent instanceof EditText)) {
                ((TextView) parent).setTextColor(colorId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void changeEditText(View parent, boolean activated) {
        try {
            if (parent instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) parent;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    changeEditText(child, activated);
                }
            } else if (parent instanceof EditText) {
                parent.setActivated(activated);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkRange(ScrollView scrollView){
        if(getValue(v_aMin)>getValue(v_aMax)){
            reverseValue(v_aMin, v_aMax);
        }
        if(getValue(v_bMin)>getValue(v_bMax)){
            reverseValue(v_bMin, v_bMax);
        }
        if(getValue(v_cMin)>getValue(v_cMax)){
            reverseValue(v_cMin, v_cMax);
        }

        if(selectedRange == RangeType.RESULT){
            if(getValue(v_cMax) - getValue(v_cMin) < 10){
                tv_info.setVisibility(View.VISIBLE);
                scrollView.smoothScrollTo(0, tv_info.getScrollY());

                return false;
            }
        }
        else if(selectedRange == RangeType.AB){
            int a = getValue(v_aMax) - getValue(v_aMin);
            int b = getValue(v_bMax) - getValue(v_bMin);

            if(getValue(v_aMax) == 0 && getValue(v_aMin) == 0 ||
                    getValue(v_bMax) == 0 && getValue(v_bMin) == 0){
                tv_info.setVisibility(View.VISIBLE);
                scrollView.smoothScrollTo(0, tv_info.getScrollY());

                return false;
            }

            if(a+b < 4){
                tv_info.setVisibility(View.VISIBLE);
                scrollView.smoothScrollTo(0, tv_info.getScrollY());

                return false;
            }
        }


        return true;
    }
    public void saveData(){
        if(getValue(v_aMin)>getValue(v_aMax)){
            reverseValue(v_aMin, v_aMax);
        }
        if(getValue(v_bMin)>getValue(v_bMax)){
            reverseValue(v_bMin, v_bMax);
        }
        if(getValue(v_cMin)>getValue(v_cMax)){
            reverseValue(v_cMin, v_cMax);
        }

        Settings settings = SettingsController.getInstance().settings;
        if(presetSettings!=null && presetSettings.rangeType!=null){
            settings.rangeType = presetSettings.rangeType;

            settings.aMin = presetSettings.aMin;
            settings.aMax = presetSettings.aMax;
            settings.bMin = presetSettings.bMin;
            settings.bMax = presetSettings.bMax;
            settings.cMin = presetSettings.cMin;
            settings.cMax = presetSettings.cMax;
            return;
        }
        settings.rangeType = selectedRange;

        settings.aMin = getValue(v_aMin);
        settings.aMax = getValue(v_aMax);
        settings.bMin = getValue(v_bMin);
        settings.bMax = getValue(v_bMax);
        settings.cMin = getValue(v_cMin);
        settings.cMax = getValue(v_cMax);

        int[] values = new int[]{settings.aMin, settings.aMax, settings.bMin, settings.bMax,settings.cMin, settings.cMax};

        JsonFileReader.updateRange(getContext(), selectedRange, values);
    }

    private int getValue(EditText editText){
        try{
            int value = Integer.parseInt(editText.getText().toString());

            return value;
        }
        catch (NumberFormatException e){
            editText.setText("0");

            return 0;
        }
    }

    private void reverseValue(EditText editText1, EditText editText2){
        String value = editText1.getText().toString();
        editText1.setText(editText2.getText().toString());
        editText2.setText(value);
    }
}