package com.axonivy.connector.mail.enums;

import java.util.Arrays;

import ch.ivyteam.ivy.environment.Ivy;

public enum BpmErrorCode {
	ERROR_MAIL_NOT_SENT("com:axonivy:connector:mail:mailNotSent","/Errors/mailNotSent"),
	DOC_VIEWER_FAILED_TO_LOAD_FILE("com:axonivy:connector:mail:documentViewer:failedToLoadFile","/Errors/failedToLoadFile"),
	DOC_VIEWER_FAILED_TO_CACHE_FILE("com:axonivy:connector:mail:documentViewer:failedToCacheFile","/Errors/failedToCacheFile"),
	DOC_VIEWER_FAILED_TO_CONVERT_TO_PDF("com:axonivy:connector:mail:documentViewer:failedToConvertToPdf","/Errors/failedToConvertToPdf"),
	RECEIVE_MAIL_PROCESSING_MESSAGE("com:axonivy:connector:mail:retrieveMail:processingMessage","/RetrieveMail/processingMessage"),
	RECEIVE_MAIL_CONTENT_ERROR("com:axonivy:connector:mail:retrieveMail:contentError","/RetrieveMail/contentError"),
	RECEIVE_MAIL_READING_MAIL_MESSAGE_ERROR("com:axonivy:connector:mail:retrieveMail:readingMailMessageError","/RetrieveMail/readingMailMessageError"),
	RECEIVE_MAIL_INCOMPLETE_MAIL_ERROR("com:axonivy:connector:mail:retrieveMail:incompleteMailError","/RetrieveMail/incompleteMailError"),
	RECEIVE_MAIL_CONNECTOR_ERROR("com:axonivy:connector:mail:retrieveMail:connectorError","/RetrieveMail/connectorError"),
	IVY_ROLE_NOT_FOUND("com:axonivy:connector:mail:utils:ivyRoleNotFound", "/RetrieveMail/ivyRoleNotFound"),
	;

	private final String code;
	private final String cmsPath;

	private BpmErrorCode(String code, String cmsPath) {
		this.cmsPath = cmsPath;
		this.code = code;
	}

	/**
	 * Return the message entry of the instance.
	 *
	 * @return
	 */
	public String getCmsMessage(Object... params) {
		return Ivy.cms().co(cmsPath, Arrays.asList(params));
	}

	public String getCmsPath() {
		return cmsPath;
	}

	public String getCode() {
		return code;
	}
}
