package me.ele.trojan.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.List;

import me.ele.trojan.Trojan;
import me.ele.trojan.demo.upload.DemoLeanCloudUploader;
import me.ele.trojan.listener.WaitUploadListener;
import me.ele.trojan.log.Logger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Trojan";

    private static final int REQUEST_WRITE_PERMISSION_CODE = 888;

    private static final String DEMO_USER_A = "520";

    private static final String DEMO_USER_B = "214";

    private String trojanUser = DEMO_USER_A;

    private DemoLeanCloudUploader uploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestWritePermissions();

        uploader = new DemoLeanCloudUploader(this);

        findViewById(R.id.btn_request_permission).setOnClickListener(this);
        findViewById(R.id.btn_write_log).setOnClickListener(this);
        findViewById(R.id.btn_write_big_log).setOnClickListener(this);
        findViewById(R.id.btn_write_encrypt_log).setOnClickListener(this);
        findViewById(R.id.btn_change_user).setOnClickListener(this);
        findViewById(R.id.btn_upload_log_file).setOnClickListener(this);
    }

    private void requestWritePermissions() {
        int hasWritePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION_CODE);
            return;
        } else {
            Logger.e(TAG, "permission granted");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION_CODE:
                Logger.e(TAG, "got writer permission now");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        final int viewId = v.getId();
        if (viewId == R.id.btn_request_permission) {
            requestWritePermissions();
        } else if (viewId == R.id.btn_write_log) {
            Trojan.log(TAG, "Have a nice day from Trojan!");
        } else if (viewId == R.id.btn_write_big_log) {
            for (int i = 0; i < 1000; i++) {
                Trojan.log(TAG, "Have a nice day from Trojan by " + (i + 1) + "!");
            }
        } else if (viewId == R.id.btn_write_encrypt_log) {
            Trojan.log(TAG, "Have a nice day from Trojan!", true);
        } else if (viewId == R.id.btn_change_user) {
            if (trojanUser.equals(DEMO_USER_A)) {
                trojanUser = DEMO_USER_B;
            } else {
                trojanUser = DEMO_USER_A;
            }
            Trojan.refreshUser(trojanUser);
        } else if (viewId == R.id.btn_upload_log_file) {
            Trojan.prepareUploadLogFile(new WaitUploadListener() {
                @Override
                public void onReadyToUpload(final String user, final String device, final List<File> waitUploadFileList) {
                    Logger.e("MainActivity->onReadyToUpload:" + waitUploadFileList);
                    if (waitUploadFileList == null || waitUploadFileList.size() == 0) {
                        return;
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            for (File logFile : waitUploadFileList) {
                                try {
                                    uploader.uploadLogFile(user, device, logFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                }

                @Override
                public void onReadyFail() {
                    Logger.e("MainActivity->onReadyFail");
                }
            });
        }
    }
}
