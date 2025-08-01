package com.axonivy.connector.mail.enums;

import ch.ivyteam.ivy.environment.Ivy;

public enum MailStatus {
	OUTBOX("/Mail/Status/outbox"), 
	SENT("/Mail/Status/sent"), 
	SMTP_FAILED("/Mail/Status/failed"),
	;

	private String cmsPath;

	private MailStatus(String cmsPath) {
		this.cmsPath = cmsPath;
	}

	public String getCms() {
		return Ivy.cms().co(cmsPath);
	}

	public String getCmsPath() {
		return cmsPath;
	}

	public void setCmsPath(String cmsPath) {
		this.cmsPath = cmsPath;
	}

}
