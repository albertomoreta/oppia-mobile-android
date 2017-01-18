package org.digitalcampus.oppia.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.adapter.TagListAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.TagClickListener;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;


public class TagSelectFragment extends AppFragment implements APIRequestListener {

    public static final String TAG = TagSelectFragment.class.getSimpleName();

    private ProgressDialog pDialog;
    private JSONObject json;
    private TagListAdapter tla;
    private ArrayList<Tag> tags;

    private TagClickListener _listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tag_select, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tags = new ArrayList<>();
        tla = new TagListAdapter(getActivity(), tags);


        ListView listView = (ListView) getView().findViewById(R.id.tag_list);
        listView.setAdapter(tla);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(_listener != null){
                    Tag selectedTag = tags.get(position);
                    _listener.onTagClick(selectedTag);
                }

            }
        });

        try {
            Serializable savedTags = savedInstanceState.getSerializable("tags");
            if (savedTags != null){
                ArrayList<Tag> savedTagsList = (ArrayList<Tag>) savedTags;
                this.tags.addAll(savedTagsList);
            }

            this.json = new JSONObject(savedInstanceState.getString("json"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onResume(){
        super.onResume();
        // Get tags list
        if(this.json == null){
            this.getTagList();
        } else if ((tags != null) && tags.size()>0) {
            //We already have loaded JSON and tags (coming from orientationchange)
            tla.notifyDataSetChanged();
        }
        else{
            //The JSON is downloaded but tag list is not
            refreshTagList();
        }
    }

    @Override
    public void onPause(){
        // kill any open dialogs
        if (pDialog != null){
            pDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (json != null){
            //Only save the instance if the request has been proccessed already
            savedInstanceState.putString("json", json.toString());
            savedInstanceState.putSerializable("tags", tags);
        }
    }

    private void getTagList() {
        // show progress dialog
        pDialog = new ProgressDialog(getActivity(), R.style.Oppia_AlertDialogStyle);
        pDialog.setTitle(R.string.loading);
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setCancelable(true);
        pDialog.show();

        APIUserRequestTask task = new APIUserRequestTask(getActivity());
        Payload p = new Payload(MobileLearning.SERVER_TAG_PATH);
        task.setAPIRequestListener(this);
        task.execute(p);
    }

    public void refreshTagList() {
        tags.clear();
        try {
            for (int i = 0; i < (json.getJSONArray("tags").length()); i++) {
                JSONObject json_obj = (JSONObject) json.getJSONArray("tags").get(i);
                Tag t = new Tag();
                t.setName(json_obj.getString("name"));
                t.setId(json_obj.getInt("id"));
                t.setCount(json_obj.getInt("count"));
                // Description
                if (json_obj.has("description") && !json_obj.isNull("description")){
                    t.setDescription(json_obj.getString("description"));
                }
                // icon
                if (json_obj.has("icon") && !json_obj.isNull("icon")){
                    t.setIcon(json_obj.getString("icon"));
                }
                // highlight
                if (json_obj.has("highlight") && !json_obj.isNull("highlight")){
                    t.setHighlight(json_obj.getBoolean("highlight"));
                }
                // order priority
                if (json_obj.has("order_priority") && !json_obj.isNull("order_priority")){
                    t.setOrderPriority(json_obj.getInt("order_priority"));
                }
                tags.add(t);
            }
            tla.notifyDataSetChanged();
            getView().findViewById(R.id.empty_state).setVisibility((tags.size()==0) ? View.VISIBLE : View.GONE);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void apiRequestComplete(Payload response) {
        // close dialog and process results
        pDialog.dismiss();

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
                refreshTagList();
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

    public void setTagClickListener(TagClickListener listener) {
        _listener=listener;
    }
}
