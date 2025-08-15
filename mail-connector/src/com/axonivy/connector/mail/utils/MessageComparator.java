package com.axonivy.connector.mail.utils;

import java.util.Comparator;

import javax.mail.Message;

import ch.ivyteam.ivy.environment.Ivy;

/**
 * MessageComparator
 *
 * @author ny.huynh
 *
 */
public class MessageComparator implements Comparator<Message> {
	@Override
	public int compare(Message o1, Message o2) {

		try {
			return o1.getSentDate().compareTo(o2.getSentDate());
		} catch (final Exception e) {
			Ivy.log().warn(e);
		}
		return 0;
	}
}
