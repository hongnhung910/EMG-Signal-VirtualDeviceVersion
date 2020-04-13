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
import emg.signal.virtualdeviceversion.SavedDataProcessing.ListFolder.FolderAdapter;
import emg.signal.virtualdeviceversion.SavedDataProcessing.ListFolder.FolderItem;

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
import java.util.List;

public class ListFolderActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    ArrayList<String> myList;
    ListView listView;
    List<FolderItem> listFolder;
    FolderAdapter folderAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listfolder);

        listView = findViewById(R.id.listFiles);
        myList = new ArrayList<>();
        listFolder = new ArrayList<>();
        folderAdapter = new FolderAdapter(ListFolderActivity.this, R.layout.item_listfolder, listFolder);
        listView.setAdapter(folderAdapter);

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data");
                    if (dir.exists()) {
                        Log.d("path", dir.toString());
                        File[] list = dir.listFiles();
                        Arrays.sort(list, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                        for (int i = 0; i < list.length; i++) {
                            myList.add(list[i].getName());
                            FolderItem folderItem = new FolderItem(R.drawable.icon_folder, list[i].getName());
                            listFolder.add(folderItem);
                        }
                        folderAdapter.notifyDataSetChanged();
                    }
                } else {
                    requestPermission(); // Code for permission
                }
            } else {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMG_Data");

                if (dir.exists()) {
                    Log.d("path", dir.toString());
                    File list[] = dir.listFiles();
                    for (int i = 0; i < list.length; i++) {
                        myList.add(list[i].getName());
                        FolderItem folderItem = new FolderItem(R.drawable.icon_folder, list[i].getName());
                        listFolder.add(folderItem);
                    }
                    folderAdapter.notifyDataSetChanged();
                }
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListFolderActivity.this, ListFilesActivity.class);
                intent.putExtra("NameFolder",myList.get(position));
                startActivity(intent);
            }
        });
    }

        private boolean checkPermission() {
            int result = ContextCompat.checkSelfPermission(ListFolderActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
        private void requestPermission() {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ListFolderActivity.this,  android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(ListFolderActivity.this,"Write External Storage permission allows us to read  files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(ListFolderActivity.this, new String[]
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
