package com.example.voice_calculator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int[] numButtons = {R.id.btnZero, R.id.btnOne, R.id.btnTwo, R.id.btnThree
            , R.id.btnFour,
    R.id.btnFive, R.id.btnSix, R.id.btnSeven, R.id.btnEight, R.id.btnNine};

    private int[] opButtons = {R.id.plus, R.id.minus, R.id.btnMultiply, R.id.btnDivide};

    private TextView txtScreen;
    private boolean lastNumber;
    private boolean stateError;
    private boolean lastDot;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSpeak = findViewById(R.id.speak);
        txtScreen = findViewById(R.id.txtScreen);

        setNumberOnClickListener();

        setOperatorOnClickListener();

    }

    private void setNumberOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Button button = (Button) view;
                if (stateError) {
                    txtScreen.setText(button.getText());
                    stateError = false;
                } else {
                    txtScreen.append(button.getText());
                }
                lastNumber = true;
            }
        };

        for (int id : numButtons) {
            findViewById(id).setOnClickListener(listener);
        }
    }

    private void setOperatorOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (lastNumber && !stateError) {
                    Button button = (Button) view;
                    txtScreen.append(button.getText());
                    lastNumber = false;
                    lastDot = false; //reset the dot flag
                }
            }
        };

        for (int i : opButtons) {
            findViewById(i).setOnClickListener(listener);
        }

        findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastNumber && !stateError && !lastDot) {
                    txtScreen.append(".");
                    lastNumber = false;
                    lastDot = false;
                }
            }
        });

        //clear button
        findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtScreen.setText("");
                lastNumber = false;
                stateError = false;
                lastDot = false;
            }
        });

        //equal button
        findViewById(R.id.equalBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEqual();
            }
        });

        //speak button
        findViewById(R.id.speak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stateError) {
                    txtScreen.setText("try again");
                    stateError = false;
                } else {
                    speechInput();
                }
                lastNumber = true;
            }
        });

    }

    private void onEqual() {
        if (lastNumber && !stateError) {
            String txt = txtScreen.getText().toString();

            //create an expression
            try {
                Expression expression = null;
                try {
                    expression = new ExpressionBuilder(txt).build();
                    double result = expression.evaluate();
                    txtScreen.setText(Double.toString(result));
                } catch (Exception e) {
                    txtScreen.setText("Error");
                    stateError = true;
                    lastNumber = false;
                }
            } catch (ArithmeticException ex) {
                txtScreen.setText("Error");
                stateError = true;
                lastNumber = false;
            }
        }
    }

    private void speechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT
                , getString(R.string.speech_prompt));

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a){
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported)
                    , Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:{
                if (resultCode == RESULT_OK && data != null) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String change = result.get(0);
                    change =  change.replace("x", "*");
                    change =  change.replace("X", "*");
                    change =  change.replace("add", "+");
                    change =  change.replace("plus", "+");
                    change =  change.replace("sub", "-");
                    change =  change.replace("minus", "-");
                    change =  change.replace("to", "2");
                    change =  change.replace("multiply", "*");
                    change =  change.replace("divide", "/");
                    change =  change.replace("equal", "=");
                    change =  change.replace("equals", "=");

                    if (change.contains("=")) {
                        change = change.replace("=", "");
                        txtScreen.setText(change);
                        onEqual();
                    } else {
                        txtScreen.setText(change);
                    }

                }
                break;
            }
        }
    }

}