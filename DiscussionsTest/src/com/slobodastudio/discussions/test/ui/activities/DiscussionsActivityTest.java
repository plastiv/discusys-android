package com.slobodastudio.discussions.test.ui.activities;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.ui.activities.DiscussionsActivity;
import com.slobodastudio.discussions.ui.activities.PersonsActivity;
import com.slobodastudio.discussions.ui.activities.TopicsActivity;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.jayway.android.robotium.solo.Solo;

public class DiscussionsActivityTest extends ActivityInstrumentationTestCase2<PersonsActivity> {

	private static final Class<TopicsActivity> nextActivityClass = TopicsActivity.class;
	private static final Class<DiscussionsActivity> testedActivityClass = DiscussionsActivity.class;
	private static final String testedActivityName = DiscussionsActivity.class.getSimpleName();
	private Solo solo;

	public DiscussionsActivityTest() {

		super(PersonsActivity.class);
		Uri uri = Persons.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		setActivityIntent(intent);
	}

	@Override
	public void tearDown() throws Exception {

		solo.finishOpenedActivities();
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
		setPortraitOrientation();
		solo.clickOnImage(0);
		solo.waitForActivity(PersonsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failed go home", PersonsActivity.class);
	}

	public void testOpenNextList() {

		setLandscapeOrientation();
		assertNextListIsOpen();
		solo.goBack();
		solo.waitForActivity(testedActivityName);
		setPortraitOrientation();
		assertNextListIsOpen();
	}

	public void testShowList() {

		setLandscapeOrientation();
		assertListViewHasItems();
		setPortraitOrientation();
		assertListViewHasItems();
	}

	@Override
	protected void setUp() throws Exception {

		solo = new Solo(getInstrumentation(), getActivity());
		solo.clickInList(2);
		solo.waitForActivity(testedActivityName);
		solo.assertCurrentActivity("Failure to start activity", testedActivityClass);
	}

	private void assertListViewHasItems() {

		assertTrue("Cant find list", solo.getCurrentListViews().size() > 0);
		ListView list = solo.getCurrentListViews().get(0);
		assertTrue("Failed to open list with items", list.getCount() > 0);
	}

	private void assertNextListIsOpen() {

		// in current activity
		solo.clickInList(1);
		solo.waitForActivity(nextActivityClass.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", nextActivityClass);
		assertListViewHasItems();
	}

	private void setLandscapeOrientation() {

		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.waitForActivity(testedActivityName);
	}

	private void setPortraitOrientation() {

		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.waitForActivity(testedActivityName);
	}
}