package org.digitalcampus.oppia.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.CourseIntallViewAdapter;
import org.digitalcampus.oppia.adapter.DownloadCourseListAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.service.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;


public class DownloadFragment extends AppFragment implements APIRequestListener, CourseInstallerListener {

    private SharedPreferences prefs;
    private ProgressDialog progressDialog;
    private JSONObject json;
    private DownloadCourseListAdapter dla;
    private String url;
    private ArrayList<CourseIntallViewAdapter> courses;
    private boolean showUpdatesOnly = false;

    private InstallerBroadcastReceiver receiver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle != null) {
            Tag t = (Tag) bundle.getSerializable(Tag.TAG);
            if (t!=null) this.url = MobileLearning.SERVER_TAG_PATH + String.valueOf(t.getId()) + File.separator;
        } else {
            this.url = MobileLearning.SERVER_COURSES_PATH;
            this.showUpdatesOnly = true;
        }


        courses = new ArrayList<>();
        dla = new DownloadCourseListAdapter(getActivity(), courses);
        dla.setOnClickListener(new CourseListListener());
        ListView listView = (ListView) getView().findViewById(R.id.tag_list);
        listView.setAdapter(dla);

        try {
            this.json = new JSONObject(savedInstanceState.getString("json"));
            ArrayList<CourseIntallViewAdapter> savedCourses = (ArrayList<CourseIntallViewAdapter>) savedInstanceState.getSerializable("courses");
            if (savedCourses!=null) this.courses.addAll(savedCourses);
        } catch (Exception e) {
            // error in the json so just get the list again
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(json == null){
            //The JSON download task has not started or been completed yet
            getCourseList();
        } else if ((courses != null) && courses.size()>0) {
            //We already have loaded JSON and courses (coming from orientationchange)
            dla.notifyDataSetChanged();
        }
        else{
            //The JSON is downloaded but course list is not
            refreshCourseList();
        }
        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseIntallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        getActivity().registerReceiver(receiver, broadcastFilter);

    }

    @Override
    public void onPause(){
        //Kill any open dialogs
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (json != null){
            //Only save the instance if the request has been proccessed already
            savedInstanceState.putString("json", json.toString());
            savedInstanceState.putSerializable("courses", courses);
        }
    }

    public void getCourseList(Tag selectedTag){
        this.url = MobileLearning.SERVER_TAG_PATH + String.valueOf(selectedTag.getId()) + File.separator;
        showUpdatesOnly = false;
        getCourseList();
    }

    private void getCourseList() {
        // show progress dialog
        progressDialog = new ProgressDialog(getActivity(), R.style.Oppia_AlertDialogStyle);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        APIUserRequestTask task = new APIUserRequestTask(getActivity());
        Payload p = new Payload(url);
        task.setAPIRequestListener(this);
        task.execute(p);
    }

    public void refreshCourseList() {
        // process the response and display on screen in listview
        // Create an array of courses, that will be put to our ListActivity
        try {
            String storage = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
            courses.clear();
            courses.addAll(CourseIntallViewAdapter.parseCoursesJSON(getActivity(), json, storage, showUpdatesOnly));

            dla.notifyDataSetChanged();
            getView().findViewById(R.id.empty_state).setVisibility((courses.size()==0) ? View.VISIBLE : View.GONE);

        } catch (Exception e) {
            Mint.logException(e);
            e.printStackTrace();
            UIUtils.showAlert(getActivity(), R.string.loading, R.string.error_processing_response);
        }

    }

    public void apiRequestComplete(Payload response) {
        progressDialog.dismiss();

        Callable<Boolean> finishActivity = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                getActivity().finish();
                return true;
            }
        };

        if(response.isResult()){
            try {
                json = new JSONObject(response.getResultResponse());
                refreshCourseList();
            } catch (JSONException e) {
                Mint.logException(e);
                e.printStackTrace();
                UIUtils.showAlert(getActivity(), R.string.loading, R.string.error_connection, finishActivity);
            }
        } else {
            String errorMsg = response.getResultResponse();
            UIUtils.showAlert(getActivity(), R.string.error, errorMsg, finishActivity);
        }
    }

    //@Override
    public void onDownloadProgress(String fileUrl, int progress) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            course.setDownloading(true);
            course.setInstalling(false);
            course.setProgress(progress);
            dla.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallProgress(String fileUrl, int progress) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            course.setDownloading(false);
            course.setInstalling(true);
            course.setProgress(progress);
            dla.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallFailed(String fileUrl, String message) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            resetCourseProgress(course, false, false);
        }
    }

    //@Override
    public void onInstallComplete(String fileUrl) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            Toast.makeText(getActivity(), getActivity().getString(R.string.install_course_complete, course.getShortname()), Toast.LENGTH_LONG).show();
            course.setInstalled(true);
            resetCourseProgress(course, false, false);
        }
    }

    private CourseIntallViewAdapter findCourse(String fileUrl){
        if ( courses.size()>0){
            for (CourseIntallViewAdapter course : courses){
                if (course.getDownloadUrl().equals(fileUrl)){
                    return course;
                }
            }
        }
        return null;
    }

    protected void resetCourseProgress(CourseIntallViewAdapter courseSelected,
                                       boolean downloading, boolean installing ){

        courseSelected.setDownloading(downloading);
        courseSelected.setInstalling(installing);
        courseSelected.setProgress(0);
        dla.notifyDataSetChanged();
    }

    private class CourseListListener implements ListInnerBtnOnClickListener {
        //@Override
        public void onClick(int position) {
            Log.d("course-download", "Clicked " + position);
            CourseIntallViewAdapter courseSelected = courses.get(position);

            //When installing, don't do anything on click
            if (courseSelected.isInstalling()) return;

            if (!courseSelected.isDownloading()){
                if(!courseSelected.isInstalled() || courseSelected.isToUpdate()){
                    Intent mServiceIntent = new Intent(getActivity(), CourseIntallerService.class);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_DOWNLOAD);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_URL, courseSelected.getDownloadUrl());
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_VERSIONID, courseSelected.getVersionId());
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
                    getActivity().startService(mServiceIntent);

                    resetCourseProgress(courseSelected, true, false);
                }
                else if(courseSelected.isToUpdateSchedule()){
                    Intent mServiceIntent = new Intent(getActivity(), CourseIntallerService.class);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_UPDATE);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_SCHEDULEURL, courseSelected.getScheduleURI());
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
                    getActivity().startService(mServiceIntent);

                    resetCourseProgress(courseSelected, false, true);
                }
            }
            else{
                //If it's already downloading, send an intent to cancel the task
                Intent mServiceIntent = new Intent(getActivity(), CourseIntallerService.class);
                mServiceIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_CANCEL);
                mServiceIntent.putExtra(CourseIntallerService.SERVICE_URL, courseSelected.getDownloadUrl());
                getActivity().startService(mServiceIntent);

                resetCourseProgress(courseSelected, false, false);
            }

        }
    }
}
