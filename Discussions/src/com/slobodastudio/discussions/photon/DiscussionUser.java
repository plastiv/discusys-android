package com.slobodastudio.discussions.photon;

public class DiscussionUser {

	private int actorNumber = -10;
	private int userId = -10;
	private String userName = null;

	public int getActorNumber() {

		return actorNumber;
	}

	public int getUserId() {

		return userId;
	}

	public String getUserName() {

		return userName;
	}

	public void setActorNumber(final int actorNumber) {

		this.actorNumber = actorNumber;
	}

	public void setUserId(final int userId) {

		this.userId = userId;
	}

	public void setUserName(final String userName) {

		this.userName = userName;
	}
}
