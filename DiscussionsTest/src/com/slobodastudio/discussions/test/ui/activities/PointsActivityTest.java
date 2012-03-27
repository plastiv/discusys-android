package com.slobodastudio.discussions.test.ui.activities;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;
import com.slobodastudio.discussions.ui.IntentExtrasKey;
import com.slobodastudio.discussions.ui.activities.PointsActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.jayway.android.robotium.solo.Solo;

public class PointsActivityTest extends ActivityInstrumentationTestCase2 {

	private Solo solo;

	public PointsActivityTest() {

		super(PointsActivity.class);
		Uri uri = Topics.buildPointUri(2);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(IntentExtrasKey.PERSON_ID, 2);
		intent.putExtra(IntentExtrasKey.TOPIC_ID, 1);
		intent.putExtra(IntentExtrasKey.DISCUSSION_ID, 1);
		intent.putExtra(IntentExtrasKey.PERSON_NAME, "Muhammed");
		setActivityIntent(intent);
	}

	@Override
	public void tearDown() throws Exception {

		solo.finishOpenedActivities();
	}

	public void testNewAction() {

		solo.waitForActivity(PointsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start points activity", PointsActivity.class);
		// press add new action menu
		solo.clickOnImageButton(0);
		solo.waitForText(solo.getString(R.string.text_point_name));
		EditText editName = solo.getEditText(0);
		assertTrue("Edit text enabled", editName.isEnabled());
		String actualText = editName.getText().toString();
		assertEquals("Edit text empty", "", actualText);
		CheckBox shared = solo.getCurrentCheckBoxes().get(0);
		assertTrue("Shared check box is enabled", shared.isEnabled());
		assertTrue("By default set checked", shared.isChecked());
		assertTrue("Shared to public checked", solo.isCheckBoxChecked(0));
		Spinner sideCode = solo.getCurrentSpinners().get(0);
		assertTrue("Side code is enabled", sideCode.isEnabled());
		assertEquals("3 side code available", 3, sideCode.getCount());
		assertEquals("By default neutral side code", Points.SideCode.NEUTRAL, sideCode.getSelectedItemId());
		assertTrue("Spinner set to neutral", solo.isSpinnerTextSelected("Neutral"));
		EditText editDescription = solo.getEditText(1);
		assertTrue("Edit description enabled", editDescription.isEnabled());
		String actualDescription = editDescription.getText().toString();
		assertEquals("Edit description empty", "", actualDescription);
	}

	public void testOpenEditableDetails() {

		solo.waitForActivity(PointsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start points activity", PointsActivity.class);
		solo.clickInList(1, 0);
		solo.waitForText(solo.getString(R.string.text_point_name));
		EditText editName = solo.getEditText(0);
		assertTrue("Edit text enabled", editName.isEnabled());
		String actualText = editName.getText().toString();
		assertEquals(
				"Edit text text",
				"Like any other difficult situation, abortion creates stress. Yet the American Psychological Association found that stress was greatest prior to an abortion, and that there was no evidence of post-abortion syndrome.",
				actualText);
		CheckBox shared = solo.getCurrentCheckBoxes().get(0);
		assertTrue("Shared check box is enabled", shared.isEnabled());
		Spinner sideCode = solo.getCurrentSpinners().get(0);
		assertTrue("Side code is enabled", sideCode.isEnabled());
		EditText editDescription = solo.getEditText(1);
		assertTrue("Edit description enabled", editDescription.isEnabled());
	}

	public void testOpenEmptyList() {

		Uri uri = Topics.buildPointUri(2);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(IntentExtrasKey.PERSON_ID, 2);
		intent.putExtra(IntentExtrasKey.TOPIC_ID, 2);
		intent.putExtra(IntentExtrasKey.DISCUSSION_ID, 1);
		intent.putExtra(IntentExtrasKey.PERSON_NAME, "Muhammed");
		solo.getCurrentActivity().startActivity(intent);
		solo.waitForActivity(PointsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start points activity", PointsActivity.class);
		assertTrue("List should be empty", solo.searchText(solo.getString(R.string.fragment_empty_points),
				true));
	}

	public void testOpenLists() {

		solo.waitForActivity(PointsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start points activity", PointsActivity.class);
		ListView userList = solo.getCurrentListViews().get(0);
		solo.waitForView(userList);
		assertTrue("User list shown", userList.getVisibility() == View.VISIBLE);
		assertEquals("Points count", 20, userList.getCount());
		{
			Cursor cursor = (Cursor) userList.getItemAtPosition(0);
			// assert id
			int idIndex = cursor.getColumnIndexOrThrow(Points.Columns.ID);
			int actualId = cursor.getInt(idIndex);
			assertEquals("Value id", 20, actualId);
			// assert name
			int nameIndex = cursor.getColumnIndexOrThrow(Points.Columns.NAME);
			String actualName = cursor.getString(nameIndex);
			assertEquals(
					"Value name",
					"Like any other difficult situation, abortion creates stress. Yet the American Psychological Association found that stress was greatest prior to an abortion, and that there was no evidence of post-abortion syndrome.",
					actualName);
		}
		ListView othersList = solo.getCurrentListViews().get(1);
		assertTrue("other list shown", othersList.getVisibility() == View.VISIBLE);
		assertEquals("Points count", 1, othersList.getCount());
		{
			Cursor cursor = (Cursor) othersList.getItemAtPosition(0);
			// assert id
			int idIndex = cursor.getColumnIndexOrThrow(Points.Columns.ID);
			int actualId = cursor.getInt(idIndex);
			assertEquals("Value id", 21, actualId);
			// assert name
			int nameIndex = cursor.getColumnIndexOrThrow(Points.Columns.NAME);
			String actualName = cursor.getString(nameIndex);
			assertEquals("Value name", "Its my cool point from other user.", actualName);
		}
		//
		View detailsFrame = solo.getCurrentActivity().findViewById(R.id.frame_layout_details);
		boolean dualPane = (detailsFrame != null) && (detailsFrame.getVisibility() == View.VISIBLE);
		if (dualPane) {
			solo.searchText(solo.getString(R.string.fragment_select_point));
		}
	}

	public void testOpenReadOnlyDetails() {

		solo.waitForActivity(PointsActivity.class.getSimpleName());
		solo.assertCurrentActivity("Failure to start points activity", PointsActivity.class);
		solo.clickInList(1, 1);
		solo.waitForText(solo.getString(R.string.text_point_name));
		EditText editName = solo.getEditText(0);
		assertFalse("Edit text enabled", editName.isEnabled());
		String actualText = editName.getText().toString();
		assertEquals("Edit text text", "Its my cool point from other user.", actualText);
		CheckBox shared = solo.getCurrentCheckBoxes().get(0);
		assertFalse("Shared check box is enabled", shared.isEnabled());
		Spinner sideCode = solo.getCurrentSpinners().get(0);
		assertFalse("Side code is enabled", sideCode.isEnabled());
		EditText editDescription = solo.getEditText(1);
		assertFalse("Edit description enabled", editDescription.isEnabled());
	}

	@Override
	protected void setUp() throws Exception {

		solo = new Solo(getInstrumentation(), getActivity());
	}
}