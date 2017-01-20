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
import android.widget.TextView;

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

public class CourseIndexListFragment extends Fragment implements ParseCourseXMLTask.OnParseXmlListener, ExpandableListView.OnChildClickListener{

    private TextView _courseTitle;
    private ExpandableListView _listView;
    private CourseIndexAdapter _adapter;
    private ArrayList<Section> _sections;
    private ArrayList<String> _sectionsTitles;
    private HashMap<String, ArrayList<String>> _activitiesTitles;

    private SharedPreferences _prefs;
    private Course _currentCourse;
    private Section _currentSection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_course_index_list, null);
        View header = inflater.inflate(R.layout.index_list_header, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        vv.setLayoutParams(lp);

        _prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        _courseTitle = (TextView) header.findViewById(R.id.course_title);

        _listView = (ExpandableListView) vv.findViewById(R.id.list_view);
        _listView.addHeaderView(header);
        _listView.setOnChildClickListener(this);

        _sectionsTitles = new ArrayList<>();
        _activitiesTitles = new HashMap<>();

        return vv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity parentActivity = getActivity();

        if(parentActivity instanceof CourseActivity){
            _currentCourse = ((CourseActivity) parentActivity).getCourse();
            _currentSection = ((CourseActivity) parentActivity).getSection();

            ParseCourseXMLTask task =  new ParseCourseXMLTask(this.getActivity(), true);
            task.setListener(this);
            task.execute(_currentCourse);

        }

    }

    @Override
    public void onParseComplete(CourseXMLReader parsed) {
        _sections = parsed.getSections();
        initList();
    }

    @Override
    public void onParseError()  { showErrorMessage(); }

    private void initList(){
        String currentLang = _prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());

        _courseTitle.setText(_currentCourse.getTitle(currentLang));

        for (Section section : _sections){
            _sectionsTitles.add(section.getTitle(currentLang));

            ArrayList<String> titles = new ArrayList<>();
            for(org.digitalcampus.oppia.model.Activity activity : section.getActivities()){
                titles.add(activity.getTitle(currentLang));
            }

            _activitiesTitles.put(section.getTitle(currentLang), titles);
        }


        _adapter = new CourseIndexAdapter(getActivity(), _sectionsTitles, _activitiesTitles);
        _adapter.setCurrentSection(_currentSection.getTitle(currentLang));
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

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int headerPos, int childPos, long id) {
        Section selectedSection = _sections.get(headerPos);
        ((CourseActivity) getActivity()).loadActivities(selectedSection, childPos);
        changeCurrentSection(selectedSection);
        expandableListView.setItemChecked(childPos, true);
        _adapter.notifyDataSetChanged();
        return true;
    }

    private void changeCurrentSection(Section section){
        String currentLang = _prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        _currentSection = section;
        _adapter.setCurrentSection(section.getTitle(currentLang));
    }
}
