package com.slobodastudio.discussions.test.ui.activities;

import com.slobodastudio.discussions.ui.activities.PointsActivity;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.RenamingDelegatingContext;

public class PointsMockActivityTest extends ActivityUnitTestCase<PointsActivity> {

	private static final String PREFIX = "readonly.";
	private RenamingDelegatingContext mMockContext;

	public PointsMockActivityTest() {

		super(PointsActivity.class);
	}

	public void testSampleTextDisplayed() {

		setActivityContext(mMockContext);
		startActivity(new Intent(), null, null);
		final PointsActivity activity = getActivity();
		assertNotNull(activity);
		// String text = activity.getText();
		// assertEquals("This is *MOCK* data", text);
	}

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		mMockContext = new RenamingDelegatingContext(getInstrumentation().getTargetContext(), PREFIX);
		mMockContext.makeExistingFilesAndDbsAccessible();
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}
}
