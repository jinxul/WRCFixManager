package com.givekesh.wrcfix.manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends Activity implements View.OnClickListener {

    private String file_name = "WRCFix.apk";
    private Button install, uninstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        install = (Button) findViewById(R.id.install);
        uninstall = (Button)findViewById(R.id.uninstall);
        install.setOnClickListener(this);
        uninstall.setOnClickListener(this);

        ((TextView)findViewById(R.id.title)).setText(String.format(getString(R.string.title), getVersion()));
        checkInstallation();
    }

    private void checkInstallation() {
        if (isInstalled())
            install.setEnabled(false);
        else
            uninstall.setEnabled(false);
    }

    private boolean isInstalled() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.givekesh.wrcfix", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void gainAccess(){
        if (!RootTools.isAccessGiven())
            RootTools.offerSuperUser(this);
    }

    private void Reboot(){
        new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
                .setTitle(getString(R.string.confirm_boot))
                .setMessage(getString(R.string.boot_dialog_msg))
                .setIcon(R.drawable.ic_launcher)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Runtime.getRuntime().exec("su -c reboot");
                            ProgressDialog.show(MainActivity.this, getString(R.string.in_progress), getString(R.string.rebooting)).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).create().show();
    }


    private String getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void pushAPK() {
        String Path = getFilesDir().getParentFile().getPath() + "/files/" + file_name;
        try {
            InputStream in = getAssets().open(file_name);
            new File(Path).createNewFile();
            OutputStream out = new FileOutputStream(Path);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
            RootTools.runShellCommand(RootTools.getShell(true), new CommandCapture(0, "chmod 777 "+Path));
            RootTools.copyFile(Path, "/system/priv-app/" + file_name, true, true);
        } catch(Exception e) {
            e.printStackTrace();
            Log.e("WRCFix", e.getMessage());
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void Install(){
        gainAccess();
        pushAPK();
        Reboot();
    }


    private void Uninstall(){
        gainAccess();
        RootTools.deleteFileOrDirectory("/system/priv-app/" + file_name, true);
        Reboot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.install: Install();
                break;
            case R.id.uninstall: Uninstall();
                break;
        }
    }
}
