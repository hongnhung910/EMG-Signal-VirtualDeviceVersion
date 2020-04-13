/*Show data files saved in external storage*/

package emg.signal.virtualdeviceversion.SavedDataProcessing;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Toast;
import emg.signal.virtualdeviceversion.R;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ListFilesActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    ArrayList<String> myList;
    ListView listView;
    ArrayList<String> ArrayData = new ArrayList<>();
    String nameFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_datafile);
        Intent getNameFolder = getIntent();
        nameFolder = getNameFolder.getStringExtra("NameFolder");
        listView = findViewById(R.id.list_dataFile);
        myList = new ArrayList<>();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data/"+nameFolder);
                    if (dir.exists()) {
                        Log.d("path", dir.toString());
                        File[] list = dir.listFiles();
                        Arrays.sort(list, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                        for (int i = 0; i < list.length; i++) {
                            myList.add(list[i].getName());
                        }
                        ArrayAdapter arrayAdapter = new ArrayAdapter(ListFilesActivity.this, android.R.layout.simple_list_item_1, myList);
                        listView.setAdapter(arrayAdapter);
                    }
                } else {
                    requestPermission(); // Code for permission
                }
            } else {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data/"+nameFolder);

                if (dir.exists()) {
                    Log.d("path", dir.toString());
                    File list[] = dir.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        myList.add(list[i].getName());
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(ListFilesActivity.this, android.R.layout.simple_list_item_1, myList);
                    listView.setAdapter(arrayAdapter);
                }
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/EMG_Data/"+nameFolder+"/"+ myList.get(position));
                ArrayData = ReadFile(file);


                double[] timedata = new double[ArrayData.size()];
                int[] domainLabels = new int[ArrayData.size()];

                for (int i=4;i<ArrayData.size();i++) {
                    timedata[i] = (Double.valueOf(ArrayData.get(i)));
                    domainLabels[i] = i;
                }
                Log.i("CHECKING LONG", "onItemClick: " + timedata.length);
                Intent intent = new Intent(ListFilesActivity.this,Loadgraph.class);
                intent.putExtra("Namefile",myList.get(position)+"");
                intent.putExtra("TimeData",timedata);
                intent.putExtra("Length",ArrayData.size());
                intent.putExtra("DomainLabels",domainLabels);
                startActivity(intent);
            }
        });


    }

    private ArrayList<String> ReadFile(File file) {

                String line = null;
                ArrayList<String> lines = new ArrayList<>();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

                    while ((line = bufferedReader.readLine()) != null) {
                        lines.add(line);
                    }

                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return lines;
            }


    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(ListFilesActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ListFilesActivity.this,  android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(ListFilesActivity.this,"Write External Storage permission allows us to read  files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(ListFilesActivity.this, new String[]
                    {android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

}

