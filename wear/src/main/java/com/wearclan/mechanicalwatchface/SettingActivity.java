package com.wearclan.mechanicalwatchface;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class SettingActivity extends Activity {


    String mSectorChecked;
    String mSpeed;

    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);



        CheckBox sectorCheckbox = (CheckBox) findViewById(R.id.showSector);


        RadioButton button1 = (RadioButton) findViewById(R.id.in1);
        RadioButton button2 = (RadioButton) findViewById(R.id.in2);
        RadioButton button3 = (RadioButton) findViewById(R.id.in3);
        RadioButton button4 = (RadioButton) findViewById(R.id.in4);


        mSectorChecked="1";
        mSpeed = "1";



        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sp.getString("last_sector_checked","").equals("")){
            mSectorChecked = sp.getString("last_sector_checked","");

        }

        if(!sp.getString("last_speed","").equals("")){
            mSpeed = sp.getString("last_speed","");

        }





        if(mSectorChecked.equals("2")){
            sectorCheckbox.setChecked(false);
        }else{
            sectorCheckbox.setChecked(true);
        }


        if(mSpeed.equals("1")){
            button1.setChecked(true);
        }else if(mSpeed.equals("2")){
            button2.setChecked(true);
        }else if(mSpeed.equals("3")){
            button3.setChecked(true);
        }else{
            button4.setChecked(true);
        }





        editor = PreferenceManager.getDefaultSharedPreferences(SettingActivity.this).edit();


        RadioGroup group = (RadioGroup) findViewById(R.id.gearGroup);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.in1) {
                    mSpeed = "1";
                } else if(checkedId == R.id.in2){
                    mSpeed = "2";
                }else if(checkedId == R.id.in3){
                    mSpeed = "3";
                }else if(checkedId == R.id.in4){
                    mSpeed = "4";
                }

                editor.putString("last_speed", mSpeed);
                editor.putString("gear_speed", mSpeed);
                editor.commit();
                sendIntent();

            }
        });



        sectorCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked== true) {
                    mSectorChecked = "1";
                } else {
                    mSectorChecked = "2";
                }

                editor.putString("last_sector_checked", mSectorChecked);
                editor.putString("sector_checked", mSectorChecked);
                editor.commit();

                sendIntent();

            }
        });




    }


    public void sendIntent(){
        Intent intent = new Intent("android.intent.action.SETTING_CHANGED");
        intent.putExtra("gear_speed", mSpeed);
        intent.putExtra("sector_checked",mSectorChecked);
        sendBroadcast(intent);
    }



}
