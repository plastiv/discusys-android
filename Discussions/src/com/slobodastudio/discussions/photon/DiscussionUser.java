package com.slobodastudio.discussions.photon;

public class DiscussionUser {

	private int actorNr;
	private final int id;
	private final String name;

	private DiscussionUser(final String name, final int id) {

		this.name = name;
		this.id = id;
	}

	public static DiscussionUser newInstance(final String name, final int id) {

		return new DiscussionUser(name, id);
	}

	public int getActorNr() {

		return actorNr;
	}

	public int getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public void setActorNr(final int actorNr) {

		this.actorNr = actorNr;
	}
}
