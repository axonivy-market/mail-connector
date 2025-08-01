package com.axonivy.connector.mail.enums;

import java.util.Arrays;

import ch.ivyteam.ivy.environment.Ivy;

public enum BpmErrorCode {
	ERROR_MAIL_NOT_SENT("com:axonivy:connector:mail:mailNotSent","/Errors/mailNotSent"),
	DOC_VIEWER_FAILED_TO_LOAD_FILE("com:axonivy:connector:mail:documentViewer:failedToLoadFile","/Errors/failedToLoadFile"),
	DOC_VIEWER_FAILED_TO_CACHE_FILE("com:axonivy:connector:mail:documentViewer:failedToCacheFile","/Errors/failedToCacheFile"),
	DOC_VIEWER_FAILED_TO_CONVERT_TO_PDF("com:axonivy:connector:mail:documentViewer:failedToConvertToPdf","/Errors/failedToConvertToPdf"),
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
