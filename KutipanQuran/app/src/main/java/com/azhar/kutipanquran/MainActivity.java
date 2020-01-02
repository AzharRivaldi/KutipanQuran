package com.azhar.kutipanquran;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new AssetDatabaseOpenHelper(MainActivity.this, "quran.db").openDatabase();
        final Spinner surat = findViewById(R.id.surat);
        final Spinner ayat = findViewById(R.id.ayat);
        final TextView arab = findViewById(R.id.arab);
        final TextView terjemah = findViewById(R.id.terjemah);
        final LinearLayout target = findViewById(R.id.target);
        arab.setTypeface(Typeface.createFromAsset(getAssets(), "me_quran.ttf"));
        surat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                int pos = p1.getSelectedItemPosition();
                Cursor cur = db.rawQuery("SELECT ayat FROM quran WHERE surat=" + (pos + 1), null);
                List<String> list = new ArrayList<String>();
                cur.moveToFirst();
                for (int i = 0; i < cur.getCount(); i++) {
                    cur.moveToPosition(i);
                    list.add(cur.getString(cur.getColumnIndex("ayat")));
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_spinner_item, list);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ayat.setAdapter(dataAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {

            }
        });
        ayat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
                int pos = p1.getSelectedItemPosition();
                Cursor cur = db.rawQuery("SELECT text, terjemah FROM quran WHERE surat=" + (surat.getSelectedItemPosition() + 1) + " AND ayat=" + (pos + 1), null);
                cur.moveToFirst();
                String txt = cur.getString(cur.getColumnIndex("text"));
                if (pos == 0 & surat.getSelectedItemPosition() != 0) txt = txt.substring(38);
                arab.setText(txt);
                terjemah.setText("\"" + cur.getString(cur.getColumnIndex("terjemah")) + "\"");

            }

            @Override
            public void onNothingSelected(AdapterView<?> p1) {

            }
        });
        findViewById(R.id.savebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View p1) {
                new Simpan().execute(target, ayat, surat);
            }
        });

        //Permission Android MM+
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            //granted
            //not granted
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Save Image
    public static Bitmap getBitmapFromView(View view) {

        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    private class Simpan extends AsyncTask<Object, Void, Boolean> {
        File f;

        @SuppressLint("WrongThread")
        @Override
        protected Boolean doInBackground(Object[] p1) {
            LinearLayout target = (LinearLayout) p1[0];
            Spinner ayat = (Spinner) p1[1];
            Spinner surat = (Spinner) p1[2];
            //target.setDrawingCacheEnabled(true);

            f = new File(Environment.getExternalStorageDirectory()
                    + "/quran-" + surat.getSelectedItemPosition() + "_" + ayat.getSelectedItemPosition() + ".png");
            if (f.exists()) f.delete();
            try {
                FileOutputStream ostream = new FileOutputStream(f);
                getBitmapFromView(target).compress(Bitmap.CompressFormat.PNG, 10, ostream);
                target.setDrawingCacheEnabled(false);
                return true;
            } catch (FileNotFoundException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(MainActivity.this, "Tersimpan di " + f.getPath(), Toast.LENGTH_LONG).show();
            } else Toast.makeText(MainActivity.this, "Gagal disimpan", Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
        }
    }
}
