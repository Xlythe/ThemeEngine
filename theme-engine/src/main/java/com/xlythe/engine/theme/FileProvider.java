package com.xlythe.engine.theme;

import static com.xlythe.engine.theme.Theme.TAG;

import android.content.ContentProvider;
import android.content.ContentProvider.PipeDataWriter;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** A very simple content provider that can serve arbitrary asset files from our .apk. */
public class FileProvider extends ContentProvider implements PipeDataWriter<InputStream> {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(
            @NonNull Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        // Security
        verifyCaller();

        // content providers that support open and openAssetFile should support queries for all
        // android.provider.OpenableColumns.
        int displayNameIndex = -1;
        int sizeIndex = -1;
        // If projection is null, return all columns.
        if (projection == null) {
            projection = new String[] {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        }
        for (int i = 0; i < projection.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(projection[i])) {
                displayNameIndex = i;
            }
            if (OpenableColumns.SIZE.equals(projection[i])) {
                sizeIndex = i;
            }
        }
        MatrixCursor cursor = new MatrixCursor(projection);
        Object[] result = new Object[projection.length];
        for (int i = 0; i < result.length; i++) {
            if (i == displayNameIndex) {
                result[i] = uri.getPath();
            }
            if (i == sizeIndex) {
                result[i] = null; // Size is unknown, so null, if it was known, it would go here.
            }
        }
        cursor.addRow(result);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Don't support inserts.
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Don't support deletes.
        return 0;
    }

    @Override
    public int update(
            @NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Don't support updates.
        return 0;
    }

    @NonNull
    @Override
    public String getType(@NonNull Uri uri) {
        return "*/*";
    }

    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        // Try to open an asset with the given name.
        try {
            InputStream is = getContext().getAssets().open(uri.getPath().substring(1));
            // Start a new thread that pipes the stream data back to the caller.
            return new AssetFileDescriptor(
                    openPipeHelper(uri, getType(uri), null, is, this), 0, AssetFileDescriptor.UNKNOWN_LENGTH);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open " + uri, e);
            throw new FileNotFoundException("Unable to open " + uri);
        }
    }

    @Override
    public void writeDataToPipe(
            @NonNull ParcelFileDescriptor output,
            @NonNull Uri uri,
            @NonNull String mimeType,
            Bundle opts,
            InputStream args) {
        // Transfer data from the asset to the pipe the client is reading.
        byte[] buffer = new byte[8192];
        int n;
        FileOutputStream outputStream = new FileOutputStream(output.getFileDescriptor());
        try {
            while ((n = args.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed transferring", e);
        } finally {
            try {
                args.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close input stream", e);
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close output stream", e);
            }
        }
    }

    /** Only available on API 19+. Verifies that the caller is a supported theme. */
    private void verifyCaller() throws SecurityException {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }

        // We'll query for ourselves to see if we show up as an available theme for this caller.
        String packageOverride = Theme.getPackageOverride();
        try {
            Theme.setPackageOverride(getCallingPackage());
            List<App> apps = Theme.getApps(getContext());
            for (App app : apps) {
                if (app.getPackageName().equals(getContext().getPackageName())) {
                    return;
                }
            }
        } finally {
            // Restore the original override (which may have been null)
            Theme.setPackageOverride(packageOverride);
        }

        // If we got here, then we couldn't verify the caller.
        throw new SecurityException("Caller [" + getCallingPackage() + "] is not a registered theme.");
    }
}