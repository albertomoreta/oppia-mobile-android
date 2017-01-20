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


import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.DownloadFragment;
import org.digitalcampus.oppia.fragments.TagSelectFragment;
import org.digitalcampus.oppia.listener.TagClickListener;
import org.digitalcampus.oppia.model.Tag;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class TagSelectActivity extends AppActivity implements TagClickListener {

	public static final String TAG = TagSelectActivity.class.getSimpleName();

	private LinearLayout dualPanel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_select_activity);

		dualPanel = (LinearLayout) findViewById(R.id.dual_panel);

		TagSelectFragment tagSelectFragment = (TagSelectFragment) getSupportFragmentManager()
				.findFragmentById(R.id.tag_select_fragment);

		tagSelectFragment.setTagClickListener(this);

	}

	@Override
	public void onTagClick(Tag selectedTag) {

		DownloadFragment downloadFragment = (DownloadFragment) getSupportFragmentManager()
				.findFragmentById(R.id.download_fragment);

		if(downloadFragment != null) {
			//If tablet, refresh course list
			//Slide animation
			ObjectAnimator animator = ObjectAnimator.ofFloat(
					dualPanel,
					"weightSum",
					dualPanel.getWeightSum(),
					2.2f);

			animator.setDuration(700);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					dualPanel.requestLayout();
				}
			});
			animator.start();

			downloadFragment.getCourseList(selectedTag);
			
		} else {
			//If phone, start DownloadActivity
			Intent i = new Intent(this, DownloadActivity.class);
			Bundle tb = new Bundle();
			tb.putSerializable(Tag.TAG, selectedTag);
			i.putExtras(tb);
			startActivity(i);
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(dualPanel != null) {
			outState.putFloat("weightSum", dualPanel.getWeightSum());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if(dualPanel != null) {
			dualPanel.setWeightSum(savedInstanceState.getFloat("weightSum"));
		}
	}
}
