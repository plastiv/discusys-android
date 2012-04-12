package com.slobodastudio.discussions.data;

import com.slobodastudio.discussions.data.odata.OdataWriteClient;

import android.content.Context;
import android.graphics.Color;

public class OdataTestData {

	// FIXME : provide real context
	private static Context nullContext;

	public static void generateDiscussions() {

		OdataWriteClient odata = new OdataWriteClient(nullContext);
		odata.insertDiscussion("Abortion");
		odata.insertDiscussion("News");
		odata.insertDiscussion("Nuclear energy");
		odata.insertDiscussion("2012 end of the world");
		odata.insertDiscussion("How stupic people can be?");
	}

	public static void generatePersons() {

		OdataWriteClient odata = new OdataWriteClient(nullContext);
		odata.insertPerson("Isaac Newton", "android@test.com", Color.BLUE, false);
		odata.insertPerson("Muhammad", "android@test.com", Color.CYAN, false);
		odata.insertPerson("Jesus Christ", "android@test.com", Color.DKGRAY, false);
		odata.insertPerson("Buddha", "android@test.com", Color.GREEN, false);
		odata.insertPerson("Confucius", "android@test.com", Color.RED, false);
		odata.insertPerson("St. Paul", "android@test.com", Color.WHITE, false);
		odata.insertPerson("Cài Lún", "android@test.com", Color.YELLOW, false);
		odata.insertPerson("Johannes Gutenberg", "android@test.com", Color.MAGENTA, false);
		odata.insertPerson("Christopher Columbus", "android@test.com", Color.GRAY, false);
		odata.insertPerson("Albert Einstein", "android@test.com", Color.LTGRAY, false);
		odata.insertPerson("Hillary Rodham Clinton", "android@test.com", Color.BLACK, false);
		odata.insertPerson("Bill Gates", "android@test.com", Color.GREEN, false);
		odata.insertPerson("Larry Page", "android@test.com", Color.RED, false);
		odata.insertPerson("Mark Zuckerberg", "android@test.com", Color.YELLOW, false);
	}

	public static void generatePoints() {

		OdataWriteClient odata = new OdataWriteClient(nullContext);
		final int groupId = 1;
		final int personId = 7;
		final int topicId = 1;
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Since life begins at conception, abortion is akin to murder as it is the act of taking human life. Abortion is in direct defiance of the commonly accepted idea of the sanctity of human life",
				true, 0, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"No civilized society permits one human to intentionally harm or take the life of another human without punishment, and abortion is no different.",
				true, 0, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Adoption is a viable alternative to abortion and accomplishes the same result. And with 1.5 million American families wanting to adopt a child, there is no such thing as an unwanted child.",
				true, 0, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"An abortion can result in medical complications later in life; the risk of ectopic pregnancies doubles, and the chance of a miscarriage and pelvic inflammatory disease also increases.",
				true, 0, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"In the instance of rape and incest, proper medical care can ensure that a woman will not get pregnant. Abortion punishes the unborn child who committed no crime; instead, it is the perpetrator who should be punished.",
				true, 0, topicId);
		odata.insertPoint(0, null, false, groupId, null, personId,
				"Abortion should not be used as another form of contraception.", true, 0, 1);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"For women who demand complete control of their body, control should include preventing the risk of unwanted pregnancy through the responsible use of contraception or, if that is not possible, through abstinence.",
				true, 0, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Many Americans who pay taxes are opposed to abortion, therefore it's morally wrong to use tax dollars to fund abortion.",
				true, 0, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Those who choose abortions are often minors or young women with insufficient life experience to understand fully what they are doing. Many have lifelong regrets afterwards.",
				true, 0, topicId);
		odata.insertPoint(0, null, false, groupId, null, personId,
				"Abortion frequently causes intense psychological pain and stress.", true, 0, 1);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Nearly all abortions take place in the first trimester, when a fetus cannot exist independent of the mother. As it is attached by the placenta and umbilical cord, its health is dependent on her health, and cannot be regarded as a separate entity as it cannot exist outside her womb.",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"The concept of personhood is different from the concept of human life. Human life occurs at conception, but fertilized eggs used for in vitro fertilization are also human lives and those not implanted are routinely thrown away. Is this murder, and if not, then how is abortion murder?",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Adoption is not an alternative to abortion, because it remains the woman's choice whether or not to give her child up for adoption. Statistics show that very few women who give birth choose to give up their babies - less than 3% of white unmarried women and less than 2% of black unmarried women.",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Abortion is a safe medical procedure. The vast majority of women - 88% - who have an abortion do so in their first trimester. Medical abortions have less than 0.5% risk of serious complications and do not affect a woman's health or future ability to become pregnant or give birth.",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"In the case of rape or incest, forcing a woman made pregnant by this violent act would cause further psychological harm to the victim. Often a woman is too afraid to speak up or is unaware she is pregnant, thus the morning after pill is ineffective in these situations.",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Abortion is not used as a form of contraception. Pregnancy can occur even with responsible contraceptive use. Only 8% of women who have abortions do not use any form of birth control, and that is due more to individual carelessness than to the availability of abortion.",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"The ability of a woman to have control of her body is critical to civil rights. Take away her reproductive choice and you step onto a slippery slope. If the government can force a woman to continue a pregnancy, what about forcing a woman to use contraception or undergo sterilization?",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Taxpayer dollars are used to enable poor women to access the same medical services as rich women, and abortion is one of these services. Funding abortion is no different from funding a war in the Mideast. For those who are opposed, the place to express outrage is in the voting booth.",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Teenagers who become mothers have grim prospects for the future. They are much more likely to leave of school; receive inadequate prenatal care; rely on public assistance to raise a child; develop health problems; or end up divorced.",
				true, 1, topicId);
		odata.insertPoint(
				0,
				null,
				false,
				groupId,
				null,
				personId,
				"Like any other difficult situation, abortion creates stress. Yet the American Psychological Association found that stress was greatest prior to an abortion, and that there was no evidence of post-abortion syndrome.",
				true, 1, topicId);
	}

	public static void generateTestDataOnServer() {

		generateDiscussions();
		generatePersons();
		generateTopics();
		generatePoints();
	}

	public static void generateTopics() {

		OdataWriteClient odata = new OdataWriteClient(nullContext);
		odata.insertTopic("Abortion pro and cons", 1, 2);
		odata.insertTopic("Observers slam Russian vote as Putin declares victory", 2, 2);
		odata.insertTopic("Crowley: Super Tuesday looms larger for Gingrich than anyone else", 2, 2);
		odata.insertTopic("Child found in field after tornado dies", 2, 2);
		odata.insertTopic("Grief, resilience after storms rip through states, killing 39", 2, 2);
		odata.insertTopic("Lindsay Lohan parties hearty after 'Today'", 2, 2);
		odata.insertTopic("Obama'a day: Meeting Netanyahu", 2, 2);
		odata.insertTopic("Al Qaeda Blamed for Attack in Yemen", 2, 2);
		odata.insertTopic("Poland mourns train crash victims", 2, 2);
	}
}