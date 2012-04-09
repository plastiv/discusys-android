package com.slobodastudio.discussions.data;

import com.slobodastudio.discussions.data.model.Description;
import com.slobodastudio.discussions.data.model.Discussion;
import com.slobodastudio.discussions.data.model.Person;
import com.slobodastudio.discussions.data.model.PersonTopic;
import com.slobodastudio.discussions.data.model.Point;
import com.slobodastudio.discussions.data.model.Topic;
import com.slobodastudio.discussions.data.model.Value;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;

public class ProviderTestData {

	private static final int JUST_FOR_NEW_URI = Integer.MIN_VALUE;

	public static void deleteData(final Context context) {

		ContentResolver cr = context.getContentResolver();
		cr.delete(Discussions.CONTENT_URI, null, null);
		cr.delete(Persons.CONTENT_URI, null, null);
		cr.delete(Topics.CONTENT_URI, null, null);
		cr.delete(Points.CONTENT_URI, null, null);
	}

	public static void generateData(final Context context) {

		ContentResolver cr = context.getContentResolver();
		generateDiscussions(cr);
		generatePersons(cr);
		generateTopics(cr);
		generatePoints(cr);
		generateDescriptions(cr);
	}

	public static void generateDescriptions(final ContentResolver cr) {

		Description description = new Description(1, "my description first", null, 1);
		cr.insert(Descriptions.CONTENT_URI, description.toContentValues());
		description = new Description(2, "my description second", null, 2);
		cr.insert(Descriptions.CONTENT_URI, description.toContentValues());
		description = new Description(3, "my description third", null, 3);
		cr.insert(Descriptions.CONTENT_URI, description.toContentValues());
		description = new Description(4, "my description fourth", null, 4);
		cr.insert(Descriptions.CONTENT_URI, description.toContentValues());
		description = new Description(5, "my description fifht", null, 5);
		cr.insert(Descriptions.CONTENT_URI, description.toContentValues());
	}

	public static void generateDiscussions(final ContentResolver cr) {

		Discussion discussion = new Discussion(1, true, "Abortion");
		cr.insert(Discussions.CONTENT_URI, discussion.toContentValues());
		discussion = new Discussion(2, true, "News");
		cr.insert(Discussions.CONTENT_URI, discussion.toContentValues());
		discussion = new Discussion(3, true, "Nuclear energy");
		cr.insert(Discussions.CONTENT_URI, discussion.toContentValues());
		discussion = new Discussion(4, true, "2012 end of the world");
		cr.insert(Discussions.CONTENT_URI, discussion.toContentValues());
		discussion = new Discussion(5, true, "How stupic people can be?");
		cr.insert(Discussions.CONTENT_URI, discussion.toContentValues());
	}

	public static void generatePersons(final ContentResolver cr) {

		Person person = new Person(new byte[] {}, Color.BLACK, "android@test.com", 1, "Isaac Newton", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.BLUE, "android@test.com", 2, "Muhammad", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.CYAN, "android@test.com", 3, "Jesus Christ", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.DKGRAY, "android@test.com", 4, "Buddha", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.GRAY, "android@test.com", 5, "Confucius", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.GREEN, "android@test.com", 6, "St. Paul", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.LTGRAY, "android@test.com", 7, "Cài Lún", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.MAGENTA, "android@test.com", 8, "Johannes Gutenberg", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.RED, "android@test.com", 9, "Christopher Columbus", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.WHITE, "android@test.com", 10, "Albert Einstein", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.YELLOW, "android@test.com", 11, "Hillary Rodham Clinton",
				false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.BLACK, "android@test.com", 12, "Bill Gates", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
		person = new Person(new byte[] {}, Color.BLUE, "android@test.com", 13, "Mark Zuckerberg", false);
		cr.insert(Persons.CONTENT_URI, person.toContentValues());
	}

	public static void generatePoints(final ContentResolver cr) {

		final int groupId = 1;
		final int personId = 2;
		final int topicId = 1;
		Value point = new Point(
				0,
				null,
				false,
				groupId,
				1,
				"Since life begins at conception, abortion is akin to murder as it is the act of taking human life. Abortion is in direct defiance of the commonly accepted idea of the sanctity of human life",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				2,
				"No civilized society permits one human to intentionally harm or take the life of another human without punishment, and abortion is no different.",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				3,
				"Adoption is a viable alternative to abortion and accomplishes the same result. And with 1.5 million American families wanting to adopt a child, there is no such thing as an unwanted child.",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				4,
				"An abortion can result in medical complications later in life; the risk of ectopic pregnancies doubles, and the chance of a miscarriage and pelvic inflammatory disease also increases.",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				5,
				"In the instance of rape and incest, proper medical care can ensure that a woman will not get pregnant. Abortion punishes the unborn child who committed no crime; instead, it is the perpetrator who should be punished.",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(0, null, false, groupId, 6,
				"Abortion should not be used as another form of contraception.", null, personId, true, 0, 1);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				7,
				"For women who demand complete control of their body, control should include preventing the risk of unwanted pregnancy through the responsible use of contraception or, if that is not possible, through abstinence.",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				8,
				"Many Americans who pay taxes are opposed to abortion, therefore it's morally wrong to use tax dollars to fund abortion.",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				9,
				"Those who choose abortions are often minors or young women with insufficient life experience to understand fully what they are doing. Many have lifelong regrets afterwards.",
				null, personId, true, 0, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(0, null, false, groupId, 10,
				"Abortion frequently causes intense psychological pain and stress.", null, personId, true, 0,
				1);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				11,
				"Nearly all abortions take place in the first trimester, when a fetus cannot exist independent of the mother. As it is attached by the placenta and umbilical cord, its health is dependent on her health, and cannot be regarded as a separate entity as it cannot exist outside her womb.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				12,
				"The concept of personhood is different from the concept of human life. Human life occurs at conception, but fertilized eggs used for in vitro fertilization are also human lives and those not implanted are routinely thrown away. Is this murder, and if not, then how is abortion murder?",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				13,
				"Adoption is not an alternative to abortion, because it remains the woman's choice whether or not to give her child up for adoption. Statistics show that very few women who give birth choose to give up their babies - less than 3% of white unmarried women and less than 2% of black unmarried women.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				14,
				"Abortion is a safe medical procedure. The vast majority of women - 88% - who have an abortion do so in their first trimester. Medical abortions have less than 0.5% risk of serious complications and do not affect a woman's health or future ability to become pregnant or give birth.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				15,
				"In the case of rape or incest, forcing a woman made pregnant by this violent act would cause further psychological harm to the victim. Often a woman is too afraid to speak up or is unaware she is pregnant, thus the morning after pill is ineffective in these situations.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				16,
				"Abortion is not used as a form of contraception. Pregnancy can occur even with responsible contraceptive use. Only 8% of women who have abortions do not use any form of birth control, and that is due more to individual carelessness than to the availability of abortion.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				17,
				"The ability of a woman to have control of her body is critical to civil rights. Take away her reproductive choice and you step onto a slippery slope. If the government can force a woman to continue a pregnancy, what about forcing a woman to use contraception or undergo sterilization?",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				18,
				"Taxpayer dollars are used to enable poor women to access the same medical services as rich women, and abortion is one of these services. Funding abortion is no different from funding a war in the Mideast. For those who are opposed, the place to express outrage is in the voting booth.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				19,
				"Teenagers who become mothers have grim prospects for the future. They are much more likely to leave of school; receive inadequate prenatal care; rely on public assistance to raise a child; develop health problems; or end up divorced.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(
				0,
				null,
				false,
				groupId,
				20,
				"Like any other difficult situation, abortion creates stress. Yet the American Psychological Association found that stress was greatest prior to an abortion, and that there was no evidence of post-abortion syndrome.",
				null, personId, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
		point = new Point(0, null, false, groupId, 21, "Its my cool point from other user.", null,
				personId + 1, true, 1, topicId);
		cr.insert(Points.CONTENT_URI, point.toContentValues());
	}

	public static void generateTopics(final ContentResolver cr) {

		Topic topic = new Topic(1, 1, "Abortion pro and cons");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		Value personsTopic = new PersonTopic(2, 1);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 2, "Observers slam Russian vote as Putin declares victory");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 2);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 3, "Crowley: Super Tuesday looms larger for Gingrich than anyone else");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 3);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 4, "Child found in field after tornado dies");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 4);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 5, "Grief, resilience after storms rip through states, killing 39");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 5);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 6, "Lindsay Lohan parties hearty after 'Today'");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 6);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 7, "Obama'a day: Meeting Netanyahu");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 7);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 8, "Al Qaeda Blamed for Attack in Yemen");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 8);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
		//
		topic = new Topic(2, 9, "Poland mourns train crash victims");
		cr.insert(Topics.CONTENT_URI, topic.toContentValues());
		personsTopic = new PersonTopic(2, 9);
		cr.insert(Persons.buildTopicUri(JUST_FOR_NEW_URI), personsTopic.toContentValues());
	}
}