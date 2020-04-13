package emg.signal.virtualdeviceversion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.util.ArrayList;

import emg.signal.virtualdeviceversion.Database.Add_Sensor_Activity;
import emg.signal.virtualdeviceversion.Database.Add_User_Activity;
import emg.signal.virtualdeviceversion.Database.DBManager;
import emg.signal.virtualdeviceversion.SavedDataProcessing.ExternalStorageUtil;
import emg.signal.virtualdeviceversion.SavedDataProcessing.ListFolderActivity;
import emg.signal.virtualdeviceversion.SavedDataProcessing.SaveData;

public class MainActivity extends AppCompatActivity {
    private Button btnConnectDisconnect, btnSaveData, btnReset;
    boolean isRunning = false;
    boolean isSaving  = false;
    private LineGraphSeries<DataPoint> series_maternal;
    ArrayList<Double> data1Save = new ArrayList();
    private SaveData saveData = new SaveData();

    private TextView timerValue;
    private Handler customHandler = new Handler();
    private long startTime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private ArrayList<String> listUser, listSensor;
    private double lastX1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HandleMenu();
        CreateSaveFolder();
        initGraphMaternal();

        btnSaveData = findViewById(R.id.btn_saveData);
        btnReset = findViewById(R.id.btn_reset);
        timerValue = findViewById(R.id.timerValue);
        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(data1Save.size() == 0)
                { Toast.makeText(MainActivity.this, "No EMG signal data available yet", Toast.LENGTH_SHORT).show();}
                else*/ {
                    if (btnSaveData.getText().equals("Save")) {
                        data1Save = new ArrayList<>();
                        for (int i=0; i<100; i++){
                            data1Save.add(i + 0.1);
                        }
                        btnSaveData.setText("Saving");
                        isSaving = true;
                        startTime = SystemClock.uptimeMillis();
                        customHandler.postDelayed(updateTimerThread, 0);
                    }
                    else {
                        timeSwapBuff = 0;
                        customHandler.removeCallbacks(updateTimerThread);
                        btnSaveData.setText("Save");
                        showdialog();
                    }
                }
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void HandleMenu(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.menu_home:
                        break;
                    case R.id.menu_saved_data:
                        Intent intent = new Intent(MainActivity.this, ListFolderActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_add_user:
                        Intent intent2 = new Intent(MainActivity.this, Add_User_Activity.class);
                        startActivity(intent2);
                        break;
                    case R.id.menu_add_sensor:
                        Intent intent3 = new Intent(MainActivity.this, Add_Sensor_Activity.class);
                        startActivity(intent3);
                        break;
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    private void initGraphMaternal(){
        // we get graph view instance
        GraphView graph =  findViewById(R.id.realtime_chart);
        graph.setTitleColor(Color.BLUE);
        graph.setTitle("Real time Signal");
        series_maternal = new LineGraphSeries();
        series_maternal.setColor(Color.RED);
        series_maternal.setThickness(2);
        graph.addSeries(series_maternal);

        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(3500);
        viewport.setMinX(0);
        viewport.setMaxX(10000);
        viewport.setScrollable(true);
        viewport.setScalable(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(10);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(true);


        graph.getGridLabelRenderer().setLabelsSpace(5);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX);
                }
            }
        });
    }
    private void CreateSaveFolder() {
        try {
            if (ExternalStorageUtil.isExternalStorageMounted()) {
                // Check whether this app has write external storage permission or not.
                int writeExternalStoragePermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                // If do not grant write external storage permission.
                if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    // Request user to grant write external storage permission.
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                else {
                    File sdCard = Environment.getExternalStorageDirectory();
                    if (sdCard.exists()) {
                        File publicDcimDirPath = new File(sdCard.getAbsolutePath() + "/EMG_Data");

                        if (!publicDcimDirPath.exists()) {
                            publicDcimDirPath.mkdirs();
                            Log.i("making", "Creating Directory: " + publicDcimDirPath);
                        }

                    }
                }
            }
        }
        catch (Exception ex)
        {
            Log.e("EXTERNAL_STORAGE", ex.getMessage(), ex);
        }
    }
    public void showdialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_info_saved);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        final String addUser = "Add user info before saving data";
        final String addSensor = "Add sensor info before saving data";
        DBManager dbManager = new DBManager(MainActivity.this);
        listUser = new ArrayList<>();
        listUser.add("Select testee");
        listSensor = new ArrayList<>();
        listSensor.add("Select sensor");
        ArrayList<String> getNameUser = new ArrayList<>();
        ArrayList<String> getTypeSensor = new ArrayList<>();
        getNameUser = dbManager.getAllUsersName();
        getTypeSensor = dbManager.getAllSensorType();
        if (getNameUser.isEmpty()) {
            listUser.add(addUser);
        } else {
            for (int i = 0; i < dbManager.NumberOfUsers(); i++) {
                listUser.add(getNameUser.get(i));
            } }
        if (getTypeSensor.isEmpty()) {
            listSensor.add(addSensor);
        } else {
            for (int j = 0; j < dbManager.NumberOfSensors(); j++) {
                listSensor.add(getTypeSensor.get(j));
            }
        }


        //Spinner setup for selecting testee
        final Spinner spinner = dialog.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.custom_spinner,
                listUser
        ) {
            @Override
            public boolean isEnabled(int position){
                if(position == 0) { return false; }
                else { return true; }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else { tv.setTextColor(Color.BLACK); }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedUser = spinner.getItemAtPosition(position).toString();
                if (selectedUser.equals(addUser)) {
                    Intent intentAddUser = new Intent(MainActivity.this, Add_User_Activity.class);
                    startActivity(intentAddUser);
                    dialog.dismiss();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //Spinner setup for selecting sensor
        final Spinner spinner2 = dialog.findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter2  = new ArrayAdapter<String>(
                this,
                R.layout.custom_spinner,
                listSensor
        ) {
            @Override
            public boolean isEnabled(int position){
                if(position == 0) { return false; }
                else { return true; }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else { tv.setTextColor(Color.BLACK); }
                return view;
            }
        };
        adapter2.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedSensor = spinner2.getItemAtPosition(position).toString();
                if (selectedSensor.equals(addSensor)) {
                    Intent intentAddSensor = new Intent(MainActivity.this, Add_User_Activity.class);
                    startActivity(intentAddSensor);
                    dialog.dismiss();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Button btnSave = dialog.findViewById(R.id.Dialog_btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUser = spinner.getSelectedItem().toString().trim();
                String selectedSensor = spinner2.getSelectedItem().toString().trim();
                saveData.save(data1Save, selectedUser);
                Toast.makeText(MainActivity.this, "Data saved successfully",Toast.LENGTH_SHORT).show();
                resetData();
                dialog.dismiss();
            }
        });
    }
    private void resetData(){
        isRunning = false;
        isSaving = false;
        data1Save.clear();
        lastX1=0;
        series_maternal.resetData(new DataPoint[] {
                new DataPoint(lastX1, 0)
        });
        initGraphMaternal();
        timerValue.setText("00 sec");
    }
    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            secs = secs % 60;
            timerValue.setText(String.format("%02d", secs) +" sec");
            customHandler.postDelayed(this, 0);
        }

    };
}
