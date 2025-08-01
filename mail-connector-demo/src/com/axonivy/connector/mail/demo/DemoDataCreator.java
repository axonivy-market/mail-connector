package com.axonivy.connector.mail.demo;

import java.util.ArrayList;
import java.util.List;

import com.axonivy.connector.mail.demo.model.CaseModel;

public class DemoDataCreator {

	public static List<CaseModel> createDemoData() {
		List<CaseModel> caseList = new ArrayList<>();
		caseList.add(createDemoCase("CASE_01", "Case 1", "Test Case 1"));
		caseList.add(createDemoCase("CASE_02", "Case 2", "Test Case 2"));
		caseList.add(createDemoCase("CASE_03", "Case 3", "Test Case 3"));
		caseList.add(createDemoCase("CASE_04", "Case 4", "Test Case 4"));
		caseList.add(createDemoCase("CASE_05", "Case 5", "Test Case 5"));
		return caseList;
	}

	private static CaseModel createDemoCase(String caseId, String name, String desc) {
		CaseModel caseModel = new CaseModel();
		caseModel.setId(caseId);
		caseModel.setName(name);
		caseModel.setDescription(desc);
		return caseModel;
	}
}
