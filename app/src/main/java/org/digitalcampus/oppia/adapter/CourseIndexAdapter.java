package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Section;

import java.util.ArrayList;
import java.util.HashMap;

public class CourseIndexAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<String> _sectionsTitles;
    private HashMap<String, ArrayList<String>> _activitiesTitles;
    private String _currentSection;

    public CourseIndexAdapter(Context context, ArrayList<String> sectionsTitles,
                              HashMap<String, ArrayList<String>> activitiesTitles){
        _context = context;
        _sectionsTitles = sectionsTitles;
        _activitiesTitles = activitiesTitles;
    }

    @Override
    public int getGroupCount() {
        return _sectionsTitles.size();
    }

    @Override
    public int getChildrenCount(int headerPos) {
        return _activitiesTitles.get(_sectionsTitles.get(headerPos)).size();
    }

    @Override
    public Object getGroup(int headerPos) {
        return _sectionsTitles.get(headerPos);
    }

    @Override
    public Object getChild(int headerPos, int childPos) {
        return _activitiesTitles.get(_sectionsTitles.get(headerPos)).get(childPos);
    }

    @Override
    public long getGroupId(int headerPos) {
        return headerPos;
    }

    @Override
    public long getChildId(int headerPos, int childPos) {
        return childPos;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int headerPos, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(headerPos);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) _context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_header, null);
        }

        TextView listHeader = (TextView) convertView
                .findViewById(R.id.list_header);
        listHeader.setText(headerTitle);
        listHeader.setTextColor(headerTitle.equals(_currentSection) ? ResourcesCompat.getColor(_context.getResources(), R.color.highlight_light, null)
                                        : ResourcesCompat.getColor(_context.getResources(), R.color.text_light, null));


        return convertView;
    }

    @Override
    public View getChildView(int headerPos, int childPos,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(headerPos, childPos);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) _context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        TextView listItem = (TextView) convertView
                .findViewById(R.id.list_item);

        listItem.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int headerPos, int childPos) {
        return true;
    }

    public void setCurrentSection(String section){
        _currentSection = section;
    }
}
