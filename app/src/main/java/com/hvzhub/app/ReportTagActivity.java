package com.hvzhub.app;


import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import android.widget.EditText;
import android.widget.Toast;


/**
 * A report tag activity.
 */
public class ReportTagActivity extends AppCompatActivity  {

    private EditText mTagCodeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.

        mTagCodeView = (EditText) findViewById(R.id.tagCode);

    }



    public void reportTag(View v) {
        String code = mTagCodeView.getText().toString();
        if (codeValid(code)){
            //TODO: REPORT THE TAG
        } else {
            Toast fail;
            fail = Toast.makeText(this, "Code not valid", Toast.LENGTH_LONG);
            fail.show();
        }

    }

    private boolean codeValid(String tagCode) {
        //TODO: Replace something that makes sense
        return true;
    }



}

