package com.slobodastudio.discussions.data.model;

import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sources;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class Source implements Parcelable {

	public static final Parcelable.Creator<Source> CREATOR = new Parcelable.Creator<Source>() {

		@Override
		public Source createFromParcel(final Parcel in) {

			return new Source(in);
		}

		@Override
		public Source[] newArray(final int size) {

			return new Source[size];
		}
	};
	private int descriptionId;
	private String link;
	private int sourceId;

	public Source() {

		link = "";
		sourceId = Integer.MIN_VALUE;
	}

	private Source(final Parcel in) {

		descriptionId = in.readInt();
		link = in.readString();
		sourceId = in.readInt();
	}

	@Override
	public int describeContents() {

		return 0;
	}

	public int getDescriptionId() {

		return descriptionId;
	}

	public String getLink() {

		return link;
	}

	public int getSourceId() {

		return sourceId;
	}

	public void setDescriptionId(final int descriptionId) {

		this.descriptionId = descriptionId;
	}

	public void setLink(final String link) {

		this.link = link;
	}

	public void setSourceId(final int sourceId) {

		this.sourceId = sourceId;
	}

	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Sources.Columns.ID, sourceId);
		cv.put(Sources.Columns.DESCRIPTION_ID, descriptionId);
		cv.put(Sources.Columns.LINK, link);
		return cv;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {

		out.writeInt(descriptionId);
		out.writeString(link);
		out.writeInt(sourceId);
	}
}
