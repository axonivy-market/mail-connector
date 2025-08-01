package com.axonivy.connector.mail.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ivyteam.ivy.scripting.objects.DateTime;

public class DateUtil {
	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss");

	public static String format(DateTime dateTime) {
		if (dateTime == null) {
			return "";
		}
		Date javaDate = dateTime.toJavaDate();
		return FORMATTER.format(javaDate);
	}
}
