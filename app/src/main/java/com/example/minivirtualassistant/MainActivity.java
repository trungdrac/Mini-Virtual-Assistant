package com.example.minivirtualassistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

public class MainActivity extends Activity {

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    //Hằng khớp onActivityResult
    private final int REQ_CODE_SPEECH_INPUT = 1;
    Intent intent = new Intent();
    //text lấy từ speech
    private String resultText = "";
    //mảng chứa kết quả tách chuỗi resultText
    private String[] splitText;
    //vị trí của 1 số key trong splitText
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    //Hiển thị hộp thoại thu giọng nói của google
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_LONG).show();
        }
    }

    //thực hiện action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    resultText = result.get(0).toLowerCase();
                    SplitText splText = new SplitText();
                    splitText = splText.split(resultText);
                }
                break;
            }
        }

        //so khớp key
        if (splitText[0].equals("mở") || splitText[0].equals("bật") || splitText[0].equals("vào")) {
            if (splitText.length > 3 && splitText[1].equals("ứng") && splitText[2].equals("dụng")) {
                openApp();
            } else {
                openWeb();
            }
        } else if (splitText.length > 3 && splitText[0].equals("gọi") && splitText[1].equals("điện") && (splitText[2].equals("cho") || splitText[2].equals("đến") || splitText[2].equals("tới"))) {
            call();
        } else if (splitText[0].equals("gửi")) {
            for (int i = splitText.length - 2; i > 0; i--) {
                if (splitText[i].equals("đến") || splitText[i].equals("cho") || splitText[i].equals("tới")) {
                    index = i;
                    sendTo();
                    break;
                }
            }
        }
    }


    //Mở ứng dụng
    private void openApp(){
        Boolean check = true;
        String appName = "";
        for ( int i=3; i<splitText.length; i++ ){
            if (!splitText[i].equals("google")){
                appName += splitText[i];
            }
        }

        //Lấy list những ứng dụng đã cài đặt
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.contains(appName)) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
                    intent = getPackageManager().getLaunchIntentForPackage(packageInfo.packageName);
                }
                if (intent != null){
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
                    }
                }
                check = false;
                break;
            }
        }
        if (check){
            Toast.makeText(MainActivity.this, "Ứng dụng chưa được cài đặt", Toast.LENGTH_LONG).show();
        }
    }

    //Mở trang web
    private void openWeb(){
        for ( int i=0; i<splitText.length; i++ ){
            if (splitText[i].matches("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)")){
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://" + splitText[i]));
                if (intent != null){
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            }
        }
    }

    GetPhoneNumberFromContact getPhoneNumber = new GetPhoneNumberFromContact();

    //Gọi điện
    private void call(){
        String phoneNumber = "";
        String contact = "";
        intent.setAction(Intent.ACTION_CALL);
        if (splitText.length > 3 && splitText[3].matches("\\d*")) {
            phoneNumber = splitText[3];
        }
        else{
            for( int i=3; i<splitText.length; i++){
                contact += splitText[i] + " ";
            }
            contact = WordUtils.capitalizeFully(contact.trim());
            phoneNumber = getPhoneNumber.getPhoneNumber(contact, MainActivity.this);
        }
        if (!phoneNumber.equals("unsaved")) {
            intent.setData(Uri.parse("tel:" + phoneNumber));
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (intent != null) {
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            Toast.makeText(MainActivity.this, "Bạn chưa lưu số của: " + contact, Toast.LENGTH_LONG).show();
        }
    }

    //Nhắn tin
    private void sendTo(){
        String smsBody = "";
        intent.setAction(Intent.ACTION_SENDTO);
        for(int j=1; j<index; j++ ){
            smsBody += splitText[j] + " ";
            smsBody = StringUtils.capitalize(smsBody);
        }
        String phoneNumber = "";
        String contact = "";
        if (splitText.length > index+1 && splitText[index+1].matches("\\d*")) {
            phoneNumber = splitText[index+1];
        }
        else{
            for(int k=index+1; k<splitText.length; k++){
                contact += splitText[k] + " ";
            }
            contact = WordUtils.capitalizeFully(contact.trim());
            phoneNumber = getPhoneNumber.getPhoneNumber(contact, MainActivity.this);
        }
        if (!phoneNumber.equals("unsaved")) {
            intent.putExtra("sms_body", smsBody);
            intent.setData(Uri.parse("sms:"+ phoneNumber));
            try{
                startActivity(intent);
            }
            catch(ActivityNotFoundException e){
                Toast.makeText(MainActivity.this,"Có lỗi xảy ra",Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(MainActivity.this, "Bạn chưa lưu số của: " + contact, Toast.LENGTH_LONG).show();
        }
    }
}





