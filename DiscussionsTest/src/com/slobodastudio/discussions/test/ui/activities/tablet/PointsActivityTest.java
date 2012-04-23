package com.slobodastudio.discussions.test.ui.activities.tablet;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.ui.activities.DiscussionsActivity;
import com.slobodastudio.discussions.ui.activities.PersonsActivity;
import com.slobodastudio.discussions.ui.activities.PointsActivity;
import com.slobodastudio.discussions.ui.activities.TopicsActivity;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

public class PointsActivityTest extends ActivityInstrumentationTestCase2<PersonsActivity> {

	private static final Class<PointsActivity> testedActivityClass = PointsActivity.class;
	private static final String testedActivityName = PointsActivity.class.getSimpleName();
	private final static String testPointDescription = "Description" + System.currentTimeMillis();
	private final static String testPointName = "Name" + System.currentTimeMillis();
	private final static String testUpdatePointDescription = "Description update"
			+ System.currentTimeMillis();
	private final static String testUpdatePointName = "Name update" + System.currentTimeMillis();
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

	public void testActionHome() {

		setPortraitOrientation();
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
		setLandscapeOrientation();
		solo.clickOnImage(0);
		solo.waitForActivity(PersonsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failed go home", PersonsActivity.class);
	}

	public void testActionNewUpdateDelete() {

		setPortraitOrientation();
		assertActionNewUpdateDeleteWorks();
		setLandscapeOrientation();
		assertActionNewUpdateDeleteWorks();
	}

	public void testActionRefresh() {

		setPortraitOrientation();
		assertProgressBarIsRollingAndFinished();
		setLandscapeOrientation();
		assertProgressBarIsRollingAndFinished();
	}

	public void testViewEditPoint() {

		setPortraitOrientation();
		solo.clickInList(0);
		assertPointDetailsAreShown();
		assertPointDetailsAreEnabled(true);
		assertPointDetailsEditsAreEmpty(false);
		assertActionBarEditShown();
		setLandscapeOrientation();
		solo.clickInList(0);
		assertPointDetailsAreShown();
		assertPointDetailsAreEnabled(true);
		assertPointDetailsEditsAreEmpty(false);
		assertActionBarEditShown();
	}

	public void testViewMain() {

		setPortraitOrientation();
		assertTestedActivityIsProperlyShown();
		setLandscapeOrientation();
		assertTestedActivityIsProperlyShown();
	}

	public void testViewNewPoint() {

		setPortraitOrientation();
		solo.clickOnView(solo.getView(R.id.menu_new));
		assertPointDetailsAreShown();
		assertPointDetailsAreEnabled(true);
		assertPointDetailsEditsAreEmpty(true);
		assertActionBarNewPointShown();
		setLandscapeOrientation();
		solo.clickOnView(solo.getView(R.id.menu_new));
		assertPointDetailsAreShown();
		assertPointDetailsAreEnabled(true);
		assertPointDetailsEditsAreEmpty(true);
		assertActionBarNewPointShown();
	}

	public void testViewOtherUserPoint() {

		setPortraitOrientation();
		solo.clickInList(0, 1);
		assertPointDetailsAreShown();
		assertPointDetailsAreEnabled(false);
		assertPointDetailsEditsAreEmpty(false);
		setLandscapeOrientation();
		solo.clickInList(0, 1);
		assertPointDetailsAreShown();
		assertPointDetailsAreEnabled(false);
		assertPointDetailsEditsAreEmpty(false);
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

	private void assertActionBarEditShown() {

		// Action bar title
		TextView tvActionBarTitle = solo.getText(getActivity().getString(R.string.action_mode_title_points),
				true);
		assertNotNull(tvActionBarTitle);
		// ActionBar button save
		Button saveButton = solo.getButton(getActivity().getString(R.string.menu_action_save), true);
		assertNotNull(saveButton);
		// ActionBar button delete
		Button deleteButton = solo.getButton(getActivity().getString(R.string.menu_action_delete), true);
		assertNotNull(deleteButton);
		// ActionBar button cancel
		Button cancelButton = solo.getButton(getActivity().getString(R.string.menu_action_cancel), true);
		assertNotNull(cancelButton);
	}

	private void assertActionBarNewPointShown() {

		// Action bar title
		TextView tvActionBarTitle = solo.getText(getActivity().getString(R.string.action_mode_title_points),
				true);
		assertNotNull(tvActionBarTitle);
		// ActionBar button save
		Button saveButton = solo.getButton(getActivity().getString(R.string.menu_action_save), true);
		assertNotNull(saveButton);
		// ActionBar button cancel
		Button cancelButton = solo.getButton(getActivity().getString(R.string.menu_action_cancel), true);
		assertNotNull(cancelButton);
	}

	private void assertActionNewUpdateDeleteWorks() {

		int initialCount = solo.getCurrentListViews().get(0).getCount();
		solo.clickOnView(solo.getView(R.id.menu_new));
		solo.waitForActivity(testedActivityName);
		solo.typeText(0, testPointName);
		solo.typeText(1, testPointDescription);
		solo.clickOnView(solo.getView(R.id.menu_save));
		solo.waitForText(testPointName);
		int actualCount = solo.getCurrentListViews().get(0).getCount();
		assertEquals(initialCount + 1, actualCount);
		// / Test if action new actually added new point
		solo.clickInList(0);
		solo.waitForActivity(testedActivityName);
		String actualPointName = solo.getCurrentEditTexts().get(0).getText().toString();
		assertEquals(testPointName, actualPointName);
		String actualPointDescription = solo.getCurrentEditTexts().get(1).getText().toString();
		assertEquals(testPointDescription, actualPointDescription);
		// update point
		solo.clearEditText(0);
		solo.clearEditText(1);
		solo.typeText(0, testUpdatePointName);
		solo.typeText(1, testUpdatePointDescription);
		solo.clickOnView(solo.getView(R.id.menu_save));
		solo.waitForText(testUpdatePointName);
		int actualUpdatedCount = solo.getCurrentListViews().get(0).getCount();
		assertEquals(initialCount + 1, actualUpdatedCount);
		// tesis if action update actually updated point
		solo.clickInList(0);
		solo.waitForActivity(testedActivityName);
		String actualUpdatedPointName = solo.getCurrentEditTexts().get(0).getText().toString();
		assertEquals(testUpdatePointName, actualUpdatedPointName);
		String actualUpdatedPointDescription = solo.getCurrentEditTexts().get(1).getText().toString();
		assertEquals(testUpdatePointDescription, actualUpdatedPointDescription);
		// delete point
		solo.clickOnView(solo.getView(R.id.menu_delete));
		solo.waitForView(ListView.class);
		int actualDeleteCount = solo.getCurrentListViews().get(0).getCount();
		assertEquals(initialCount, actualDeleteCount);
	}

	private void assertPointDetailsAreEnabled(final boolean expectedEnabled) {

		assertEquals("Details edit text", expectedEnabled, solo.getCurrentEditTexts().get(0).isEnabled());
		assertEquals("Details edit text", expectedEnabled, solo.getCurrentEditTexts().get(1).isEnabled());
		assertEquals("Details checked box", expectedEnabled, solo.getCurrentCheckBoxes().get(0).isEnabled());
		assertEquals("Details spinner", expectedEnabled, solo.getCurrentSpinners().get(0).isEnabled());
	}

	private void assertPointDetailsAreShown() {

		// Point title
		TextView tvPointName = solo.getText(getActivity().getString(R.string.text_point_name), true);
		assertNotNull(tvPointName);
		// Description title
		TextView tvPointDesctiption = solo.getText(getActivity().getString(R.string.text_point_description),
				true);
		assertNotNull(tvPointDesctiption);
		// Comments title
		TextView tvPointComment = solo
				.getText(getActivity().getString(R.string.text_point_description), true);
		assertNotNull(tvPointComment);
		// Agreement code title
		TextView tvPointAgreementCode = solo.getText(getActivity().getString(
				R.string.text_point_shoose_a_side_code), true);
		assertNotNull(tvPointAgreementCode);
		// Shared to public title
		TextView tvPointSharedToPublic = solo.getText(getActivity().getString(
				R.string.text_point_shared_to_public), true);
		assertNotNull(tvPointSharedToPublic);
		assertTrue("Details list", solo.getCurrentListViews().size() == 3);
		assertTrue("Details edit text", solo.getCurrentEditTexts().size() == 2);
		assertTrue("Details checked box", solo.getCurrentCheckBoxes().size() == 1);
		assertTrue("Details spinner", solo.getCurrentSpinners().size() == 1);
	}

	private void assertPointDetailsEditsAreEmpty(final boolean expectedEmpty) {

		assertEquals("Details edit text", expectedEmpty, TextUtils.isEmpty(solo.getCurrentEditTexts().get(0)
				.getText().toString()));
		assertEquals("Details edit text", expectedEmpty, TextUtils.isEmpty(solo.getCurrentEditTexts().get(1)
				.getText().toString()));
	}

	private void assertProgressBarIsRollingAndFinished() {

		solo.clickOnView(solo.getView(R.id.menu_refresh));
		solo.waitForView(ProgressBar.class);
		// for (View v : solo.getViews()) {
		// Log.d("test", v.toString());
		// }
		// assertNotNull(solo.getView(IcsProgressBar.class, 0));
		int triesNum = 30;
		int i = 0;
		ProgressBar progressBar = (ProgressBar) solo.getView(ProgressBar.class, 0);
		while ((progressBar != null)) {
			if (i > triesNum) {
				fail("refresh doent work");
			}
			try {
				progressBar = (ProgressBar) solo.getView(ProgressBar.class, 0);
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
		// User points list title
		TextView tvUserList = solo.getText(getActivity().getString(R.string.text_current_user_points), true);
		assertNotNull(tvUserList);
		// Other users points list title
		TextView tvOtherUserList = solo.getText(getActivity().getString(R.string.text_other_users_points),
				true);
		assertNotNull(tvOtherUserList);
		assertTrue("Points list", solo.getCurrentListViews().size() == 2);
		ListView usersList = solo.getCurrentListViews().get(0);
		assertTrue("List was empty", usersList.getCount() > 0);
		ListView otherUsersList = solo.getCurrentListViews().get(1);
		assertTrue("List was empty", otherUsersList.getCount() > 0);
		// ActionBar button
		Button newButton = solo.getButton(getActivity().getString(R.string.menu_action_new), true);
		assertNotNull(newButton);
		// ActionBar button
		Button refreshButton = solo.getButton(getActivity().getString(R.string.menu_action_refresh), true);
		assertNotNull(refreshButton);
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