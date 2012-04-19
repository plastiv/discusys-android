package com.slobodastudio.discussions.test.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.ui.activities.DiscussionsActivity;
import com.slobodastudio.discussions.ui.activities.PersonsActivity;
import com.slobodastudio.discussions.ui.activities.PointDetailsActivity;
import com.slobodastudio.discussions.ui.activities.PointsActivity;
import com.slobodastudio.discussions.ui.activities.TopicsActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.internal.widget.IcsProgressBar;
import com.jayway.android.robotium.solo.Solo;

public class PointsActivityTest extends ActivityInstrumentationTestCase2<PersonsActivity> {

	private static final Class<PointsActivity> testedActivityClass = PointsActivity.class;
	private static final String testedActivityName = PointsActivity.class.getSimpleName();
	private Solo solo;

	public PointsActivityTest() {

		super(PersonsActivity.class);
		Uri uri = Persons.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		setActivityIntent(intent);
	}

	@Override
	public void tearDown() throws Exception {

		solo.finishOpenedActivities();
	}

	public void testEditPointView() {

		setLandscapeOrientation();
		solo.clickInList(0);
		assertEditDetailsAreShown();
		assertEditTextsAreNotEmpty();
		solo.goBack();
		solo.waitForActivity(testedActivityName);
		setPortraitOrientation();
		solo.clickInList(0);
		assertEditDetailsAreShown();
		assertEditTextsAreNotEmpty();
	}

	public void testGoHome() {

		setLandscapeOrientation();
		solo.clickOnImage(0);
		solo.waitForActivity(PersonsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failed go home", PersonsActivity.class);
		// discussions
		solo.clickInList(2);
		solo.waitForActivity(DiscussionsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", DiscussionsActivity.class);
		// topics
		solo.clickInList(1);
		solo.waitForActivity(TopicsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", TopicsActivity.class);
		// points
		solo.clickInList(1);
		solo.waitForActivity(testedActivityClass.getSimpleName());
		setPortraitOrientation();
		solo.clickOnImage(0);
		solo.waitForActivity(PersonsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failed go home", PersonsActivity.class);
	}

	public void testMainView() {

		setLandscapeOrientation();
		assertTestedActivityIsProperlyShown();
		setPortraitOrientation();
		assertTestedActivityIsProperlyShown();
	}

	public void testNewPointView() {

		setLandscapeOrientation();
		solo.clickOnView(solo.getView(R.id.menu_new));
		assertNewDetailsAreShown();
		assertEditTextsAreEmpty();
		solo.goBack();
		solo.waitForActivity(testedActivityName);
		setPortraitOrientation();
		solo.clickOnView(solo.getView(R.id.menu_new));
		assertNewDetailsAreShown();
		assertEditTextsAreEmpty();
	}

	public void testOtherUserPointView() {

		setLandscapeOrientation();
		solo.clickInList(0, 1);
		assertViewDetailsAreShown();
		assertEditTextsAreNotEmpty();
		solo.goBack();
		solo.waitForActivity(testedActivityName);
		setPortraitOrientation();
		solo.clickInList(0, 1);
		assertViewDetailsAreShown();
		assertEditTextsAreNotEmpty();
	}

	public void testRefreshButton() {

		setLandscapeOrientation();
		assertProgressBarIsRollingAndFinished();
		setPortraitOrientation();
		assertProgressBarIsRollingAndFinished();
	}

	@Override
	protected void setUp() throws Exception {

		solo = new Solo(getInstrumentation(), getActivity());
		// discussions
		solo.clickInList(2);
		solo.waitForActivity(DiscussionsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", DiscussionsActivity.class.getSimpleName());
		// topics
		solo.clickInList(1);
		solo.waitForActivity(TopicsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", TopicsActivity.class.getSimpleName());
		// points
		solo.clickInList(1);
		solo.waitForActivity(testedActivityClass.getSimpleName());
		assertTestedActivityIsProperlyShown();
	}

	private void assertEditDetailsAreShown() {

		solo.waitForActivity(PointDetailsActivity.class.getSimpleName());
		assertTrue("Details list", solo.getCurrentListViews().size() == 1);
		assertTrue("Details edit text", solo.getCurrentEditTexts().size() == 2);
		assertTrue("Details edit text", solo.getCurrentEditTexts().get(0).isEnabled());
		assertTrue("Details edit text", solo.getCurrentEditTexts().get(1).isEnabled());
		assertTrue("Details checked box", solo.getCurrentCheckBoxes().size() == 1);
		assertTrue("Details checked box", solo.getCurrentCheckBoxes().get(0).isEnabled());
		assertTrue("Details spinner", solo.getCurrentSpinners().size() == 1);
		assertTrue("Details spinner", solo.getCurrentSpinners().get(0).isEnabled());
		if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// should be 2 action bar buttons on screen + check box
			if ((getActivity().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_NORMAL) {
				// should be 3 action bar buttons on screen + check box
				Log.d("test", "screenLayout: " + getActivity().getResources().getConfiguration().screenLayout);
				assertTrue("Details buttons, actual: " + solo.getCurrentButtons().size(), solo
						.getCurrentButtons().size() == 4);
			} else {
				assertTrue("Details buttons", solo.getCurrentButtons().size() == 3);
			}
		} else {
			// should be 3 action bar buttons on screen + check box
			assertTrue("Details buttons", solo.getCurrentButtons().size() == 4);
		}
	}

	private void assertEditTextsAreEmpty() {

		EditText pointName = solo.getCurrentEditTexts().get(0);
		assertTrue("Edit text was not empty", TextUtils.isEmpty(pointName.getText()));
		EditText pointDescr = solo.getCurrentEditTexts().get(1);
		assertTrue("Edit text was not empty", TextUtils.isEmpty(pointDescr.getText()));
	}

	private void assertEditTextsAreNotEmpty() {

		EditText pointName = solo.getCurrentEditTexts().get(0);
		assertFalse("Edit text was not empty", TextUtils.isEmpty(pointName.getText()));
		EditText pointDescr = solo.getCurrentEditTexts().get(1);
		assertFalse("Edit text was not empty", TextUtils.isEmpty(pointDescr.getText()));
	}

	private void assertNewDetailsAreShown() {

		solo.waitForActivity(PointDetailsActivity.class.getSimpleName());
		assertTrue("Details list", solo.getCurrentListViews().size() == 1);
		assertTrue("Details edit text", solo.getCurrentEditTexts().size() == 2);
		assertTrue("Details edit text", solo.getCurrentEditTexts().get(0).isEnabled());
		assertTrue("Details edit text", solo.getCurrentEditTexts().get(1).isEnabled());
		assertTrue("Details checked box", solo.getCurrentCheckBoxes().size() == 1);
		assertTrue("Details checked box", solo.getCurrentCheckBoxes().get(0).isEnabled());
		assertTrue("Details spinner", solo.getCurrentSpinners().size() == 1);
		assertTrue("Details spinner", solo.getCurrentSpinners().get(0).isEnabled());
		// should be 2 action bar buttons on screen + check box
		assertTrue("Details buttons", solo.getCurrentButtons().size() == 3);
	}

	private void assertProgressBarIsRollingAndFinished() {

		solo.clickOnView(solo.getView(R.id.menu_refresh));
		solo.waitForView(IcsProgressBar.class);
		assertNotNull(solo.getView(IcsProgressBar.class, 0));
		int triesNum = 30;
		int i = 0;
		IcsProgressBar progressBar = (IcsProgressBar) solo.getView(IcsProgressBar.class, 0);
		while ((progressBar != null)) {
			if (i > triesNum) {
				fail("refresh doent work");
			}
			try {
				progressBar = (IcsProgressBar) solo.getView(IcsProgressBar.class, 0);
			} catch (Throwable t) {
				progressBar = null;
			}
			solo.sleep(1000);
			i++;
		}
		assertTestedActivityIsProperlyShown();
	}

	private void assertTestedActivityIsProperlyShown() {

		solo.assertCurrentActivity("Failure to start activity", testedActivityClass);
		assertTrue("Points list", solo.getCurrentListViews().size() == 2);
		ListView usersList = solo.getCurrentListViews().get(0);
		assertTrue("List was empty", usersList.getCount() > 0);
		ListView otherUsersList = solo.getCurrentListViews().get(1);
		assertTrue("List was empty", otherUsersList.getCount() > 0);
		assertTrue("Points list", solo.getCurrentButtons().size() == 2);
	}

	private void assertViewDetailsAreShown() {

		solo.waitForActivity(PointDetailsActivity.class.getSimpleName());
		assertTrue("Details list", solo.getCurrentListViews().size() == 1);
		assertTrue("Details edit text", solo.getCurrentEditTexts().size() == 2);
		assertFalse("Details edit text", solo.getCurrentEditTexts().get(0).isEnabled());
		assertFalse("Details edit text", solo.getCurrentEditTexts().get(1).isEnabled());
		assertTrue("Details checked box", solo.getCurrentCheckBoxes().size() == 1);
		assertFalse("Details checked box", solo.getCurrentCheckBoxes().get(0).isEnabled());
		assertTrue("Details spinner", solo.getCurrentSpinners().size() == 1);
		assertFalse("Details spinner", solo.getCurrentSpinners().get(0).isEnabled());
		assertTrue("Details buttons", solo.getCurrentButtons().size() == 2);
	}

	private void setLandscapeOrientation() {

		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.waitForActivity(testedActivityName);
		assertTestedActivityIsProperlyShown();
	}

	private void setPortraitOrientation() {

		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.waitForActivity(testedActivityName);
		assertTestedActivityIsProperlyShown();
	}
}