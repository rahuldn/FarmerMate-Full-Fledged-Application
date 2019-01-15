package com.example.rahul.farmermatever20;




import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * An activity to illustrate how to edit contents of a Drive file.
 */
public class AppendContentsActivity extends BaseDemoActivity {
    private static final String TAG = "AppendContentsActivity";

    @Override
    protected void onDriveClientReady() {
        pickTextFile()
                .addOnSuccessListener(this,
                        driveId -> appendContents(driveId.asDriveFile()))
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "No file selected", e);
                    showMessage(getString(R.string.file_not_selected));
                    finish();
                });
    }

    private void appendContents(DriveFile file) {
        // [START drive_android_open_for_append]
        Task<DriveContents> openTask =
                getDriveResourceClient().openFile(file, DriveFile.MODE_READ_WRITE);
        // [END drive_android_open_for_append]
        // [START drive_android_append_contents]
        openTask.continueWithTask(task -> {
            DriveContents driveContents = task.getResult();
            ParcelFileDescriptor pfd = driveContents.getParcelFileDescriptor();
            long bytesToSkip = pfd.getStatSize();
            try (InputStream in = new FileInputStream(pfd.getFileDescriptor())) {
                // Skip to end of file
                while (bytesToSkip > 0) {
                    long skipped = in.skip(bytesToSkip);
                    bytesToSkip -= skipped;
                }
            }
            try (OutputStream out = new FileOutputStream(pfd.getFileDescriptor())) {
                out.write("Hello world".getBytes());
            }
            // [START drive_android_commit_contents_with_metadata]
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setStarred(true)
                    .setLastViewedByMeDate(new Date())
                    .build();
            Task<Void> commitTask =
                    getDriveResourceClient().commitContents(driveContents, changeSet);
            // [END drive_android_commit_contents_with_metadata]
            return commitTask;
        })
                .addOnSuccessListener(this,
                        aVoid -> {
                            showMessage(getString(R.string.content_updated));
                            finish();
                        })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Unable to update contents", e);
                    showMessage(getString(R.string.content_update_failed));
                    finish();
                });
        // [END drive_android_append_contents]
    }
}
