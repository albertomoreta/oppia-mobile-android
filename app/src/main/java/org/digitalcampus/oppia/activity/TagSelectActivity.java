/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.TagListAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.fragments.DownloadFragment;
import org.digitalcampus.oppia.fragments.TagSelectFragment;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.TagClickListener;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class TagSelectActivity extends AppActivity implements TagClickListener {

	public static final String TAG = TagSelectActivity.class.getSimpleName();

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_select_activity);

		TagSelectFragment tagSelectFragment = (TagSelectFragment) getSupportFragmentManager()
				.findFragmentById(R.id.tag_select_fragment);

		tagSelectFragment.setTagClickListener(this);

	}

	@Override
	public void onTagClick(Tag selectedTag) {
		DownloadFragment downloadFragment = (DownloadFragment) getSupportFragmentManager()
				.findFragmentById(R.id.download_fragment);

		if(downloadFragment != null) {
			downloadFragment.getCourseList(selectedTag);
		} else {
			Intent i = new Intent(this, DownloadActivity.class);
			Bundle tb = new Bundle();
			tb.putSerializable(Tag.TAG, selectedTag);
			i.putExtras(tb);
			startActivity(i);
		}
	}
}
