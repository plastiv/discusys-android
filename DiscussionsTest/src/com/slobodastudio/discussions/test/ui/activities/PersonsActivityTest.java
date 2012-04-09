package com.slobodastudio.discussions.test.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.ui.activities.DiscussionsActivity;
import com.slobodastudio.discussions.ui.activities.PersonsActivity;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.actionbarsherlock.internal.widget.IcsProgressBar;
import com.jayway.android.robotium.solo.Solo;

public class PersonsActivityTest extends ActivityInstrumentationTestCase2<PersonsActivity> {

	private static final String activityName = PersonsActivity.class.getSimpleName();
	private static final Class<DiscussionsActivity> nextActivityClass = DiscussionsActivity.class;
	private Solo solo;

	public PersonsActivityTest() {

		super(PersonsActivity.class);
		Uri uri = Persons.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		setActivityIntent(intent);
	}

	@Override
	public void tearDown() throws Exception {

		solo.finishOpenedActivities();
	}

	public void testOpenNextList() {

		setLandscapeOrientation();
		assertNextListIsOpen();
		solo.goBack();
		solo.waitForActivity(activityName);
		setPortraitOrientation();
		assertNextListIsOpen();
	}

	public void testRefresh() {

		setLandscapeOrientation();
		assertProgressBarIsRollingAndFinished();
		setPortraitOrientation();
		assertProgressBarIsRollingAndFinished();
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
	}

	private void assertListViewHasItems() {

		assertTrue("Cant find list", solo.getCurrentListViews().size() > 0);
		ListView list = solo.getCurrentListViews().get(0);
		assertTrue("Failed to open list with items", list.getCount() > 0);
	}

	private void assertNextListIsOpen() {

		// in current activity
		solo.clickInList(2);
		solo.waitForActivity(nextActivityClass.getSimpleName());
		solo.assertCurrentActivity("Failure to start activity", nextActivityClass);
		assertListViewHasItems();
	}

	private void assertProgressBarIsRollingAndFinished() {

		assertListViewHasItems();
		solo.clickOnView(solo.getView(R.id.menu_refresh));
		solo.waitForText("Refreshing all data...");
		solo.waitForView(IcsProgressBar.class);
		assertNotNull(solo.getView(IcsProgressBar.class, 0));
		assertListViewHasItems();
		int triesNum = 15;
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
		assertListViewHasItems();
	}

	private void setLandscapeOrientation() {

		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.waitForActivity(activityName);
	}

	private void setPortraitOrientation() {

		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.waitForActivity(activityName);
	}
}