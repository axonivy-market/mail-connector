package com.axonivy.connector.mail.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.mail.Constants;
import com.axonivy.connector.mail.businessData.Attachment;
import com.axonivy.connector.mail.businessData.Mail;
import com.axonivy.connector.mail.enums.BpmErrorCode;
import com.axonivy.connector.mail.enums.DirectionCode;
import com.axonivy.connector.mail.enums.MailStatus;
import com.axonivy.connector.mail.enums.ResponseAction;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.scripting.objects.Binary;
import ch.ivyteam.ivy.scripting.objects.DateTime;
import ch.ivyteam.ivy.workflow.ICase;
import ch.ivyteam.ivy.workflow.ITask;

public class MailService {

	/**
	 * Create a {@link BPMError} for email with exception message
	 * 
	 * @param errorMessage
	 * @return
	 */
	static public BpmError createSendMailError(String errorMessage) {
		return BpmErrorService.get().buildBpmError(BpmErrorCode.ERROR_MAIL_NOT_SENT, errorMessage);
	}

	/**
	 * Update {@link Mail} which given mailId to status {@link #OUTBOX}. Uses
	 * {@link #updateMailStatus(String, String)}
	 *
	 * @param mailId
	 */
	static public Mail updateMailToOutBox(String mailId) {
		return updateMailStatus(mailId, MailStatus.OUTBOX);
	}

	/**
	 * Update {@link Mail} which given mailId to status {@link #SENT}
	 * 
	 * @param mailId
	 */
	static public Mail updateMailToSent(String mailId) {
		return updateMailStatus(mailId, MailStatus.SENT);
	}

	/**
	 * Update {@link Mail} which given mailId to status {@link #SMTP_FAILED}
	 * 
	 * @param mailId
	 */
	static public Mail updateMailToSMTPFailed(String mailId) {
		return updateMailStatus(mailId, MailStatus.SMTP_FAILED);
	}

	/**
	 * Updates email with given database id to given status
	 *
	 * @param mailId
	 * @param statusCode
	 */
	static private Mail updateMailStatus(String mailId, MailStatus statusCode) {
		if (StringUtils.isBlank(mailId)) {
			return null;
		}
		final Mail mail = Ivy.repo().find(mailId, Mail.class);
		if (MailStatus.SENT.equals(statusCode)) {
			mail.setSentDate(new DateTime());
		}

		if (mail == null) {
			Ivy.log().warn("Cannot find mail has id:{0}", mailId);
			return null;
		}
		mail.setStatus(statusCode);

		mail.setId(Ivy.repo().save(mail).getId());
		return mail;
	}

	/**
	 * Set up new mail for resend
	 *
	 * @param mail
	 * @return
	 */
	public Mail setUpResendMail(Mail mail) {
		final Mail newMail = new Mail();
		newMail.setDirection(DirectionCode.OUT);
		try {
			copyOriginalMail(mail, newMail, ResponseAction.RESEND);

			final String resendTemplate = Ivy.cms().co("/Template/resendSection");
			updateBody(newMail, resendTemplate);

		} catch (final IllegalAccessException e) {
			Ivy.log().error("An error occurred when copy mail: " + e.getMessage());
		} catch (final InvocationTargetException e1) {
			Ivy.log().error("An error occurred when copy mail: " + e1.getMessage());
		}
		return newMail;
	}

	/**
	 * Set up new mail for Forward
	 *
	 * @param mail
	 * @return
	 */
	public Mail setUpForwardMail(Mail mail) {
		final Mail newMail = new Mail();
		newMail.setDirection(DirectionCode.OUT);
		try {
			copyOriginalMail(mail, newMail, ResponseAction.FORWARD);

			final String prefix = Ivy.cms().co("/NewMail/FW");
			updateBody(newMail, null);
			updateSubject(newMail, prefix);
			newMail.setRecipient(null);

		} catch (final IllegalAccessException e) {
			Ivy.log().error("An error occurred when copy mail: " + e.getMessage());
		} catch (final InvocationTargetException e1) {
			Ivy.log().error("An error occurred when copy mail: " + e1.getMessage());
		}
		return newMail;
	}

	/**
	 * Set up new mail for reply
	 *
	 * @param mail
	 * @return
	 */
	public Mail setUpReplyMail(Mail mail) {
		final Mail newMail = new Mail();
		newMail.setDirection(DirectionCode.OUT);
		try {
			copyOriginalMail(mail, newMail, ResponseAction.REPLY);

			final String prefix = Ivy.cms().co("/NewMail/RE");
			updateBody(newMail, null);
			updateSubject(newMail, prefix);

			newMail.setRecipient(mail.getSender());
		} catch (final IllegalAccessException e) {
			Ivy.log().error("An error occurred when copy mail: " + e.getMessage());
		} catch (final InvocationTargetException e1) {
			Ivy.log().error("An error occurred when copy mail: " + e1.getMessage());
		}
		return newMail;
	}

	/**
	 * Copy original mail to new mail
	 *
	 * @param mail
	 * @param newMail
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void copyOriginalMail(Mail mail, final Mail newMail, ResponseAction actionType)
			throws IllegalAccessException, InvocationTargetException {
		BeanUtils.copyProperties(newMail, mail);
		newMail.setResponseTo(mail);
		newMail.setResponseAction(actionType);
		newMail.setId(null);

		if (ResponseAction.isReplyResendForward(actionType)) {
			newMail.setSentDate(null);
		}
	}

	public ch.ivyteam.ivy.scripting.objects.List<Attachment> copyMailAttachment(String mailId)
			throws IllegalAccessException, InvocationTargetException {
		final List<Attachment> originalAttachments = findMailAttachments(mailId);
		ch.ivyteam.ivy.scripting.objects.List<Attachment> cloneAttachments = new ch.ivyteam.ivy.scripting.objects.List<>();
		for (Attachment attachment : originalAttachments) {
			Attachment cloneAttachment = new Attachment();
			BeanUtils.copyProperties(cloneAttachment, attachment);
			cloneAttachment.setId(null);
			cloneAttachment.setMailId(null);
			cloneAttachments.add(cloneAttachment);
		}
		return cloneAttachments;
	}

	/**
	 * Update body of mail with specify template
	 *
	 * @param mail
	 * @param template
	 * @return
	 */
	public Mail updateBody(Mail mail, String template) {
		final StringBuilder templateSb = new StringBuilder();
		if (StringUtils.isNotEmpty(template)) {
			templateSb.append(template);
			templateSb.append(Constants.BREAK_LINE);
			templateSb.append(Constants.HORIZONTAL_RULE);
			templateSb.append(Constants.BREAK_LINE);
		} else {
			setUpMetaInformation(mail, templateSb);
		}

		final StringBuilder bodySb = new StringBuilder(mail.getBody() != null ? mail.getBody() : "");
		bodySb.insert(0, templateSb.toString());

		mail.setBody(bodySb.toString());

		return mail;
	}

	/**
	 * Update subject of mail with prefix
	 *
	 * @param mail
	 * @param template
	 * @return
	 */
	public Mail updateSubject(Mail mail, String prefix) {
		mail.setSubject((new StringBuilder()).append(prefix).append(StringUtils.SPACE)
				.append(StringUtils.isBlank(mail.getSubject()) ? StringUtils.EMPTY : mail.getSubject()).toString());
		return mail;
	}

	/**
	 * Set up meta information of the original mail
	 *
	 * @param mail
	 * @param templateSb
	 */
	private void setUpMetaInformation(Mail mail, StringBuilder templateSb) {
		final String metaInforFrom = Ivy.cms().co("/NewMail/MetaInformation/From");
		final String metaInforSent = Ivy.cms().co("/NewMail/MetaInformation/Sent");
		final String metaInforTo = Ivy.cms().co("/NewMail/MetaInformation/To");
		final String metaInforCC = Ivy.cms().co("/NewMail/MetaInformation/CC");
		final String metaInforBCC = Ivy.cms().co("/NewMail/MetaInformation/BCC");
		final String metaInforSubject = Ivy.cms().co("/NewMail/MetaInformation/Subject");

		templateSb.append(Constants.BREAK_LINE);
		templateSb.append(Constants.HORIZONTAL_RULE);
		templateSb.append(metaInforFrom + StringUtils.SPACE + mail.getSender());
		templateSb.append(Constants.BREAK_LINE);
		templateSb.append(metaInforSent + StringUtils.SPACE + mail.getResponseTo().getSentDate());
		templateSb.append(Constants.BREAK_LINE);
		templateSb.append(metaInforTo + StringUtils.SPACE + mail.getRecipient());
		templateSb.append(Constants.BREAK_LINE);
		if (StringUtils.isNotEmpty(mail.getRecipientCC())) {
			templateSb.append(metaInforCC + StringUtils.SPACE + mail.getRecipientCC());
			templateSb.append(Constants.BREAK_LINE);
		}
		if (StringUtils.isNotEmpty(mail.getRecipientBCC())) {
			templateSb.append(metaInforBCC + StringUtils.SPACE + mail.getRecipientBCC());
			templateSb.append(Constants.BREAK_LINE);
		}
		templateSb.append(metaInforSubject).append(
				StringUtils.isBlank(mail.getSubject()) ? StringUtils.EMPTY : StringUtils.SPACE + mail.getSubject());
		templateSb.append(Constants.BREAK_LINE);
	}

	/*
	 * Create confirm message when resends mail
	 */
	public String setUpMessageConfirm(Mail mail, String actionType) {
		String result = StringUtils.EMPTY;
		if (actionType.equals(ResponseAction.RESEND.toString())) {
			final String message = Ivy.cms().co("/Labels/resendConfirmMessage",
					Arrays.asList(mail.getSubject(), mail.getRecipient(), mail.getRecipientCC()));
			if (StringUtils.isEmpty(mail.getRecipientCC())) {
				result = StringUtils.replace(message, Constants.SEMICOLON, StringUtils.EMPTY);
				return result;
			} else {
				return message;
			}
		}
		return result;
	}

	/**
	 * Get SendingMail task list which have errorMessage in custom field
	 * 
	 * @param ivyCase
	 * @return
	 */
	static public List<ITask> getSendMailTasks(ICase ivyCase) {
		return ivyCase.tasks().all().stream()
				.filter(task -> task.getName().equals(Ivy.cms().co("/Tasks/SendingMail/name"))
						|| task.getName().equals(Ivy.cms().co("/Tasks/RetrySendingMail/name")))
				.toList();
	}

	public static void waitForMailCount(long oldCount, String caseId, long timeoutMillis) {
		long startTime = System.currentTimeMillis();
		long currentCount;

		do {
			currentCount = countMail(caseId);
			if (currentCount > oldCount) {
				return;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				Ivy.log().warn("Thread was interrupted while waiting for mail count {0}", e.getMessage());
			}

		} while (System.currentTimeMillis() - startTime < timeoutMillis);
	}

	private static long countMail(String caseId) {
		return Ivy.repo().search(Mail.class).textField("caseId").containsAllWordPatterns(caseId).execute().count();
	}

	/**
	 * create list of {@link ch.ivyteam.ivy.scripting.objects.File} from list
	 * attchments {@link Attachment}
	 * 
	 * @param attachments
	 * @return
	 */
	static public List<ch.ivyteam.ivy.scripting.objects.File> prepareAttachments(String mailId) {
		final List<ch.ivyteam.ivy.scripting.objects.File> files = new ArrayList<ch.ivyteam.ivy.scripting.objects.File>();
		List<Attachment> attachments = findMailAttachments(mailId);
		if (CollectionUtils.isNotEmpty(attachments)) {
			for (final Attachment attachment : attachments) {
				try {
					final ch.ivyteam.ivy.scripting.objects.File file = new ch.ivyteam.ivy.scripting.objects.File(
							attachment.getName(), true);
					final Binary binary = new Binary(attachment.getContent());
					file.writeBinary(binary);
					files.add(file);
				} catch (final IOException e) {
					Ivy.log().error("prepareAttachments Error " + e.getMessage());
				}
			}
		}
		return files;
	}

	public static List<Attachment> findMailAttachments(String mailId) {
		return Ivy.repo().search(Attachment.class).textField("mailId").containsAllWordPatterns(mailId).execute()
				.getAll();
	}

	public static long countMailAttachments(String mailId) {
		return Ivy.repo().search(Attachment.class).textField("mailId").containsAllWordPatterns(mailId).execute()
				.count();
	}

	public static Mail saveMail(Mail mail, List<Attachment> attachments) {
		mail.setCreatedDate(new DateTime());
		String mailId = Ivy.repo().save(mail).getId();
		mail.setId(mailId);
		if (CollectionUtils.isNotEmpty(attachments)) {
			for (Attachment attachment : attachments) {
				attachment.setMailId(mailId);
				Ivy.repo().save(attachment);
			}
		}
		return mail;
	}

}
