package com.slobodastudio.discussions.odata;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Discussion;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Person;
import com.slobodastudio.discussions.odata.DiscussionsTableShema.Point;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.odata4j.core.OEntity;

public class DiscussionsDataActivity extends Activity {

	/** Called when the activity is first created. */
	// @Override
	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		testInsertPoint();
		Log.v("Hello", "Success");
	}

	private void testInsertDiscussion() {

		DiscussionsOdata odata = new DiscussionsOdata();
		OEntity discussion = odata.insertDiscussion();
		ODataReportUtil.reportEntity("Inserted discussion: "
				+ discussion.getProperty(Discussion.SUBJECT).getValue(), discussion);
	}

	private void testInsertPerson() {

		DiscussionsOdata odata = new DiscussionsOdata();
		OEntity person = odata.insertPerson();
		ODataReportUtil
				.reportEntity("Inserted person: " + person.getProperty(Person.NAME).getValue(), person);
	}

	private void testInsertPoint() {

		DiscussionsOdata odata = new DiscussionsOdata();
		OEntity point = odata.insertPoint();
		ODataReportUtil.reportEntity("Inserted point: " + point.getProperty(Point.POINT).getValue(), point);
	}

	private void testInsertTopic() {

		DiscussionsOdata odata = new DiscussionsOdata();
		odata.insertTopic();
		// OEntity topic = odata.insertTopic();
		// ODataReportUtil.reportEntity("Inserted topic: " + topic.getProperty(Topic.NAME).getValue(), topic);
	}
}