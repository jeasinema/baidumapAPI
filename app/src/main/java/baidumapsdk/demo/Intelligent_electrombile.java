package baidumapsdk.demo;

//android:value="R3hQfrnr2eAnNh6CUshkRO4m"  is for my digital signature
//android:value="sC5t7ch51iwjoqLpsS7UgOOD"  is for adb digital signature

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public class Intelligent_electrombile extends Activity {

    private EditText username;
    private EditText password;
    private Button login;
    private CheckBox cb1;
    private CheckBox cb2;
    private TextView loginLockedTV;
    private TextView attemptsLeftTV;
    private TextView numberOfRemainingLoginAttemptsTV;
    //private SharedPreference sharedPreference;
    int numberOfRemainingLoginAttempts = 3;
    Map<String,String> map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //sharedPreference = new SharedPreference();
        setContentView(R.layout.activity_intelligent_electrombile);
        setupVariables();
//        map = LoginService.getSavedUserInfo(this);
//        if(map!=null){
//            username.setText(map.get(username));
//            password.setText(map.get(password));
//        }
        loadSavedPreferences();
    }

    private void loadSavedPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Intelligent_electrombile.this);
        //读取是否自动登录，若是，自动登录，若否，设置自动登录为false；若原本就没有这一项，则不自动登录且设置cb2为true
        boolean cb1Value = sharedPreferences.getBoolean("savePassword",false);
        //String name = sharedPreferences.getString("storedName", "");
        boolean cb2Value = sharedPreferences.getBoolean("autoLogin",false);
        cb1.setChecked(cb1Value);
        cb2.setChecked(cb2Value);
        String name = sharedPreferences.getString("NAME","");
        String passwd = sharedPreferences.getString("PASSWD","");
        username.setText(name);
        password.setText(passwd);
        if(!sharedPreferences.getBoolean("logout",false)){
            if(cb2.isChecked()){
                login();
            }
        }
    }

    public void checkAndSave(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(cb1.isChecked()){
//                boolean result = LoginService.saveUserInfo(this,username.getText().toString(),password.getText().toString());
//                if(result)
//                    Toast.makeText(this, "Save Successfully", Toast.LENGTH_SHORT).show();
//                else
//                    Toast.makeText(this,"Save Unsuccessfully",Toast.LENGTH_SHORT).show();
            editor.putString("NAME",username.getText().toString());
            editor.putString("PASSWD",password.getText().toString());
            editor.putBoolean("savePassword",cb1.isChecked());
        }else{
            editor.putString("NAME", "");
            editor.putString("PASSWD","");
            editor.putBoolean("savePassword",cb1.isChecked());
        }
        if(cb2.isChecked()){
            editor.putBoolean("autoLogin",cb2.isChecked());
        }else{
            editor.putBoolean("autoLogin",cb2.isChecked());
        }
        editor.putBoolean("logout",false);
        editor.commit();
    }

    public void login(){
        if (username.getText().toString().equals("gzy") &&//发送用户名到服务器
                password.getText().toString().equals("guzhaoyuan")) {
            checkAndSave();
            Intent intent =new Intent();
            intent.setClass(Intelligent_electrombile.this,info.class);
            startActivity(intent);
            Intelligent_electrombile.this.finish();
        } else {
            Toast.makeText(getApplicationContext(), "username and password do not match",
                    Toast.LENGTH_SHORT).show();
            numberOfRemainingLoginAttempts--;
            attemptsLeftTV.setVisibility(View.VISIBLE);
            numberOfRemainingLoginAttemptsTV.setVisibility(View.VISIBLE);
            numberOfRemainingLoginAttemptsTV.setText(Integer.toString(numberOfRemainingLoginAttempts));

            if (numberOfRemainingLoginAttempts == 0) {
                login.setEnabled(false);
                loginLockedTV.setVisibility(View.VISIBLE);
                loginLockedTV.setBackgroundColor(Color.RED);
                loginLockedTV.setText("LOGIN LOCKED!!!");
            }
        }
    }

    public void authenticateLogin(View view) {
        login();
    }

    private void setupVariables() {
        username = (EditText) findViewById(R.id.usernameET);
        password = (EditText) findViewById(R.id.passwordET);
        login = (Button) findViewById(R.id.loginBtn);
        loginLockedTV = (TextView) findViewById(R.id.loginLockedTV);
        attemptsLeftTV = (TextView) findViewById(R.id.attemptsLeftTV);
        numberOfRemainingLoginAttemptsTV = (TextView) findViewById(R.id.numberOfRemainingLoginAttemptsTV);
        numberOfRemainingLoginAttemptsTV.setText(Integer.toString(numberOfRemainingLoginAttempts));
        cb1 = (CheckBox)findViewById(R.id.checkbox1);
        cb2 = (CheckBox)findViewById(R.id.checkbox2);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
}
