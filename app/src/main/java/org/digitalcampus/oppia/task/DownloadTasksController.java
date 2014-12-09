package org.digitalcampus.oppia.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.DownloadCompleteListener;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.UpdateScheduleListener;
import org.digitalcampus.oppia.model.DownloadProgress;

import java.io.Serializable;

public class DownloadTasksController implements Parcelable, InstallCourseListener, UpdateScheduleListener {

    private Context ctx;
    private DownloadCompleteListener onDownloadCompleteListener;
    private SharedPreferences prefs;

    private DownloadProgress currentProgress;
    private boolean taskInProgress;
    private ProgressDialog progressDialog;

    public DownloadTasksController(Context ctx, SharedPreferences prefs){
        this.prefs = prefs;
        this.ctx = ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setTitle(R.string.install);
        progressDialog.setMessage(ctx.getString(R.string.download_starting));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setCancelable(true);

        if (isTaskInProgress()){
            updateDialogMessage();
            showDialog();
        }
    }

    public boolean isTaskInProgress() {
        return taskInProgress;
    }

    public void setOnDownloadCompleteListener(DownloadCompleteListener onDownloadCompleteListener) {
        this.onDownloadCompleteListener = onDownloadCompleteListener;
        if (progressDialog != null) progressDialog.dismiss();
    }

    public void showDialog(){
        if (!progressDialog.isShowing()){
            progressDialog.show();
        }
    }

    //Method for updating the values in the dialog with the current progress state
    private void updateDialogMessage(){
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(currentProgress.getProgress());
        progressDialog.setMessage(currentProgress.getMessage());
        showDialog();
    }

    @Override
    public void downloadComplete(Payload p) {
        Log.d("download-task", "downloadComplete");
        if (p.isResult()){
            //If it finished correctly, start the task to install the course
            progressDialog.setMessage(ctx.getString(R.string.download_complete));
            progressDialog.setIndeterminate(true);

            InstallDownloadedCoursesTask installTask = new InstallDownloadedCoursesTask(ctx);
            installTask.setInstallerListener(this);
            installTask.execute(p);

        } else {
            progressDialog.setTitle(ctx.getString(R.string.error_download_failure));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(true);
            taskInProgress = false;
        }

    }

    @Override
    public void installComplete(Payload p) {
        Log.d("download-task", "installComplete");
        if(p.isResult()){
            SharedPreferences.Editor e = prefs.edit();
            e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
            e.commit();
            progressDialog.setTitle(ctx.getString(R.string.install_complete));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(100);

            if (onDownloadCompleteListener != null) {
                onDownloadCompleteListener.onComplete();
            }
            progressDialog.dismiss();

        } else {
            progressDialog.setTitle(ctx.getString(R.string.error_install_failure));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(100);
        }
        taskInProgress = false;
    }

    @Override
    public void updateComplete(Payload p) {
        Log.d("download-task", "updateComplete");

        currentProgress.setMessage(p.getResultResponse());
        currentProgress.setProgress(100);
        updateDialogMessage();

        if(p.isResult()){
            progressDialog.setTitle(ctx.getString(R.string.update_complete));

            //Notify the activity
            if (onDownloadCompleteListener != null) {
                onDownloadCompleteListener.onComplete();
            }

            SharedPreferences.Editor e = prefs.edit();
            e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
            e.commit();
            progressDialog.dismiss();

        } else {
            progressDialog.setTitle(ctx.getString(R.string.error_update_failure));
        }
        this.taskInProgress = false;

    }

    @Override
    public void downloadProgressUpdate(DownloadProgress dp) {
        Log.d("download-task", "downloadProgressUpdate " + dp.getProgress());
        currentProgress = dp;
        updateDialogMessage();
    }

    @Override
    public void installProgressUpdate(DownloadProgress dp) {
        Log.d("download-task", "installProgressUpdate " + dp.getProgress());
        currentProgress = dp;
        updateDialogMessage();
    }

    @Override
    public void updateProgressUpdate(DownloadProgress dp) {
        Log.d("download-task", "updateProgressUpdate");
        currentProgress = dp;
        updateDialogMessage();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeValue(currentProgress);
        parcel.writeByte((byte) (taskInProgress ? 0x01 : 0x00));
        //parcel.writeValue(progressDialog);
    }

    public DownloadTasksController(Parcel in) {
        currentProgress = (DownloadProgress) in.readValue(DownloadProgress.class.getClassLoader());
        taskInProgress = in.readByte() != 0x00;
        //progressDialog = (ProgressDialog) in.readValue(ProgressDialog.class.getClassLoader());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public DownloadTasksController createFromParcel(Parcel in) {
            return new DownloadTasksController(in);
        }

        public DownloadTasksController[] newArray(int size) {
            return new DownloadTasksController[size];
        }
    };
}
