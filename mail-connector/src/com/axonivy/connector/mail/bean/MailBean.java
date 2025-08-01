package com.axonivy.connector.mail.bean;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.file.UploadedFile;

import com.axonivy.connector.mail.Constants;
import com.axonivy.connector.mail.businessData.Attachment;
import com.axonivy.connector.mail.businessData.Mail;
import com.axonivy.connector.mail.enums.MailStatus;
import com.axonivy.connector.mail.enums.ResponseAction;
import com.axonivy.connector.mail.model.MailLazyDataModel;
import com.axonivy.connector.mail.service.MailService;
import com.axonivy.connector.mail.utils.DateUtil;
import com.axonivy.connector.mail.utils.TextUtil;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.scripting.objects.DateTime;

@ManagedBean
@ViewScoped
public class MailBean {
	private Mail mail;
	private Mail selectedMail;
	private MailLazyDataModel mailModel;
	private MailService mailService;
	private String caseId;
	private java.util.List<Attachment> attachments;
	private String allowFileTypes = Ivy.var().get("allowFileTypes");
	private String maxUploadSize = Ivy.var().get("maxUploadSize");

	private static final Map<String, String> MIME_TYPE_ICON_MAP = new HashMap<>();

	static {
		MIME_TYPE_ICON_MAP.put(Constants.MIME_PDF, Constants.ICON_PDF);
		MIME_TYPE_ICON_MAP.put(Constants.MIME_EXCEL_LEGACY, Constants.ICON_EXCEL);
		MIME_TYPE_ICON_MAP.put(Constants.MIME_EXCEL_OPENXML, Constants.ICON_EXCEL);
		MIME_TYPE_ICON_MAP.put(Constants.MIME_WORD_LEGACY, Constants.ICON_WORD);
		MIME_TYPE_ICON_MAP.put(Constants.MIME_WORD_OPENXML, Constants.ICON_WORD);
	}

	@PostConstruct
	public void init() {
		mailService = new MailService();
	}

	public void initMail() {
		mailModel = new MailLazyDataModel(caseId);
		mail = new Mail();
		mail.setCaseId(caseId);
		attachments = new ArrayList<Attachment>();
	}

	public void handleCloseDialog() {
		initMail();
	}

	public void prepareMail(String actionType) throws IllegalAccessException, InvocationTargetException {
		ResponseAction type = ResponseAction.valueOf(actionType);
		mail.setCaseId(caseId);
		switch (type) {
		case RESEND:
			setMail(mailService.setUpResendMail(selectedMail));
			attachments = mailService.copyMailAttachment(selectedMail.getId());
			break;
		case FORWARD:
			setMail(mailService.setUpForwardMail(selectedMail));
			attachments = mailService.copyMailAttachment(selectedMail.getId());
			break;
		case REPLY:
			setMail(mailService.setUpReplyMail(selectedMail));
			break;
		default:
			break;
		}
	}

	/**
	 * Check if mail is Sent
	 *
	 * @return boolean
	 */
	public boolean isSent() {
		return selectedMail.getStatus().equals(MailStatus.SENT);
	}

	public String getConfirmMessage() {
		return mailService.setUpMessageConfirm(selectedMail, ResponseAction.RESEND.toString());
	}

	/**
	 * Get mail body with embedded images
	 */
	public String getMailBodyWithEmbeddedImages() {
		// Detect if it's HTML or plain text
		if (!isHtml(selectedMail.getBody())) {
			// Escape HTML and wrap in <pre> to preserve formatting
			selectedMail.setBody("<pre>" + StringEscapeUtils.escapeHtml4(selectedMail.getBody()) + "</pre>");
		}

		return selectedMail.getBody();
	}

	/**
	 * Checks whether the given text contains HTML tags.
	 * 
	 * @param text the input string to check
	 * @return {@code true} if the input contains HTML tags; {@code false} otherwise
	 */
	private static boolean isHtml(String text) {
		return text != null && text.trim().matches(Constants.HTML_REGEX);
	}

	/**
	 * Gets the maximum size allowed for uploading files in bytes
	 *
	 * @return
	 */
	public Integer getMaxUploadSizeInBytes() {
		return getMaxUploadSizeInMB() * 1024 * 1024;
	}

	/**
	 * Gets the maximum size allowed for uploading files in megabytes.
	 * 
	 * @return
	 */
	public Integer getMaxUploadSizeInMB() {
		if (StringUtils.isBlank(maxUploadSize)) {
			return Integer.valueOf(10);
		}
		return Integer.valueOf(maxUploadSize);
	}

	/**
	 * Gets the allowed file types.
	 *
	 * @return
	 */
	public String getAllowedFileTypes() {
		if (StringUtils.isBlank(allowFileTypes)) {
			return "";
		}
		return java.util.Arrays.stream(allowFileTypes.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.collect(java.util.stream.Collectors.joining("|"));
	}

	/**
	 * Uploads a file and attaches it to the mail
	 *
	 * @param event {@link FileUploadEvent}
	 */
	public void handleFileUpload(FileUploadEvent event) throws IOException {
		final UploadedFile uploadedFile = event.getFile();
		final Attachment attachment = new Attachment();
		attachment.setName(uploadedFile.getFileName());
		attachment.setContent(uploadedFile.getContent());
		attachment.setSize(uploadedFile.getSize());
		attachment.setContentType(uploadedFile.getContentType());
		attachment.setDefaultExtension(
				StringUtils.upperCase(StringUtils.substringAfterLast(uploadedFile.getFileName(), ".")));
		if (CollectionUtils.isEmpty(attachments)) {
			attachments = new java.util.ArrayList<Attachment>();
		}
		attachments.add(attachment);
	}

	public void removeFile(Attachment attachment) {
		attachments.remove(attachment);
	}

	public String formatDate(DateTime dateTime) {
		return DateUtil.format(dateTime);
	}

	public static String getAttachmentIcon(String contentType) {
		return MIME_TYPE_ICON_MAP.getOrDefault(contentType, Constants.ICON_DEFAULT);
	}

	/**
	 * Show a file from attachment on DocumentViewer dialog
	 * 
	 * @param file to be for show
	 */
	public void viewDocument(Attachment file) {
		Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
		DocumentViewerBean documentViewerBean = (DocumentViewerBean) viewMap.get("documentViewerBean");
		documentViewerBean.showFile(file);
	}

	public String textEllipsis(String text, int maxLength) {
		return TextUtil.ellipsis(text, maxLength);
	}

	public void handleSelectMail(SelectEvent<Mail> event) {
		selectedMail = event.getObject();
		if (selectedMail != null) {
			attachments = MailService.findMailAttachments(selectedMail.getId());
		}
	}

	public Mail getMail() {
		return mail;
	}

	public void setMail(Mail mail) {
		this.mail = mail;
	}

	public MailLazyDataModel getMailModel() {
		return mailModel;
	}

	public void setMailModel(MailLazyDataModel mailModel) {
		this.mailModel = mailModel;
	}

	public Mail getSelectedMail() {
		return selectedMail;
	}

	public void setSelectedMail(Mail selectedMail) {
		this.selectedMail = selectedMail;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getAllowFileTypes() {
		return allowFileTypes;
	}

	public void setAllowFileTypes(String allowFileTypes) {
		this.allowFileTypes = allowFileTypes;
	}

	public String getMaxUploadSize() {
		return maxUploadSize;
	}

	public void setMaxUploadSize(String maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}

	public java.util.List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(java.util.List<Attachment> attachments) {
		this.attachments = attachments;
	}

}
