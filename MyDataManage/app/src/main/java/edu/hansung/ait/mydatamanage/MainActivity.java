package edu.hansung.ait.mydatamanage;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final static String TAG = "FILETEST";
    private String state;
    private TextView result;
    private int mode = 0;

    private EditText input;
    private Button save, load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = (EditText)findViewById(R.id.editText);
        save = (Button)findViewById(R.id.save);
        load = (Button)findViewById(R.id.load);

        save.setOnClickListener(this);
        load.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);   // findItem의 첫번째 파라미터는 메뉴아이템 리소스 아이디
        menu.findItem(R.id.internal_storage).setChecked(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.internal_storage:
                item.setChecked(true);
                mode = 0;
                return super.onOptionsItemSelected(item);
            case R.id.external_storage:
                item.setChecked(true);
                mode = 1;
                return super.onOptionsItemSelected(item);
            case R.id.sqlite:
                item.setChecked(true);
                mode = 2;
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    void requestPermission() {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            result.setText("외부메모리 읽기 쓰기 모두 가능");
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            result.setText("외부메모리 읽기만 가능");
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
//        if(mode == 0) {
            switch (view.getId()) {
                case R.id.save:
                    String data = input.getText().toString();

                    try {
                        FileOutputStream fos = openFileOutput("myfile.txt", Context.MODE_APPEND);   // 저장모드
                        PrintWriter out = new PrintWriter(fos);
                        out.println(data);
                        out.close();
                        result.setText("file saved");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.load:
                    try {   // 파일에서 읽은 데이터를 저장하기 위해서 만든 변수
                        StringBuffer loadData = new StringBuffer();
                        FileInputStream fis = openFileInput("myfile.txt");  // 파일명
                        BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));
                        String str = buffer.readLine(); // 파일에서 한줄을 읽어옴
                        while (str != null) {
                            loadData.append(str + "\n");
                            str = buffer.readLine();
                        }
                        result.setText(loadData);
                        buffer.close();
                    } catch (FileNotFoundException e) {
                        result.setText("File Not Found");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
//        }

        if(mode == 1) {
            switch (view.getId()) {
                case R.id.save:
                    if(!isExternalStorageWritable())
                        return;     // 외부메모리를 사용하지 못하면 끝냄
                    requestPermission();

                    String data1 = input.getText().toString();
                    Log.i(TAG, getLocalClassName() + ":file save start");

                    try {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                      File path = Environment.getExternalStorageDirectory();
                        File f = new File(path, "external.txt");    // 경로, 파일명
                        FileWriter write = new FileWriter(f, true);

                        PrintWriter out = new PrintWriter(write);
                        out.println(data1);
                        out.close();

                        result.setText("저장완료");
                        Log.i(TAG, getLocalClassName() + ":file saved");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.load:
                    if (!isExternalStorageReadable())
                        return;     // 외부메모리를 사용하지 못하면 끝냄
                    requestPermission();

                    try {
                        StringBuffer data2 = new StringBuffer();
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                      File path = Environment.getExternalStorageDirectory();
                        File f = new File(path, "external.txt");

                        BufferedReader buffer = new BufferedReader(new FileReader(f));
                        String str = buffer.readLine();
                        while (str != null) {
                            data2.append(str + "\n");
                            str = buffer.readLine();
                        }
                        result.setText(data2);
                        buffer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        else {
            switch (view.getId()) {
                case R.id.save:
                    if (!isExternalStorageWritable())
                        return;     // 외부메모리를 사용하지 못하면 끝냄
                    requestPermission();

                    Log.i(TAG, getLocalClassName() + ":file save start");
                    try {
                        File f = new File(getExternalFilesDir(null), "demofile.txt");   // 경로, 파일명
                        InputStream is = getResources().openRawResource(R.raw.ballons);
                        OutputStream os = new FileOutputStream(f);
                        byte[] data = new byte[is.available()];
                        is.read(data);
                        os.write(data);
                        is.close();
                        os.close();
                        result.setText("저장완료");
                        Log.i(TAG, getLocalClassName() + ":file saved");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}