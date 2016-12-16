package org.digitalcampus.oppia.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.CourseIndexAdapter;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Callable;

public class CourseIndexListFragment extends Fragment implements ParseCourseXMLTask.OnParseXmlListener{

    private ExpandableListView _listView;
    private CourseIndexAdapter _adapter;
    private ArrayList<String> _sectionsTitles;
    private HashMap<String, ArrayList<String>> _activitiesTitles;

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_course_index_list, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        vv.setLayoutParams(lp);

        _listView = (ExpandableListView) vv.findViewById(R.id.list_view);

        _sectionsTitles = new ArrayList<>();
        _activitiesTitles = new HashMap<>();

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return vv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity parentActivity = getActivity();

        if(parentActivity instanceof CourseActivity){
            Course course = ((CourseActivity) parentActivity).getCourse();

            ParseCourseXMLTask task =  new ParseCourseXMLTask(this.getActivity(), true);
            task.setListener(this);
            task.execute(course);

        }

    }

    @Override
    public void onParseComplete(CourseXMLReader parsed) {
        initList(parsed.getSections());
    }

    @Override
    public void onParseError()  { showErrorMessage(); }

    private void initList(ArrayList<Section> sections){
        String currentLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        for (Section section : sections){
            _sectionsTitles.add(section.getTitle(currentLang));

            ArrayList<String> titles = new ArrayList<>();
            for(org.digitalcampus.oppia.model.Activity activity : section.getActivities()){
                titles.add(activity.getTitle(currentLang));
            }

            _activitiesTitles.put(section.getTitle(currentLang), titles);
        }


        _adapter = new CourseIndexAdapter(getActivity(), _sectionsTitles, _activitiesTitles);
        _listView.setAdapter(_adapter);
    }

    private void showErrorMessage(){
        UIUtils.showAlert(getActivity(), R.string.error, R.string.error_reading_xml, new Callable<Boolean>() {
            public Boolean call() throws Exception {
                getActivity().finish();
                return true;
            }
        });
    }
}
