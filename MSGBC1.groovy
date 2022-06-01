package com.mincom.ellipse.script.conversion.m3660

import com.mincom.ellipse.edoi.ejb.msf000.MSF000_DC0017Key
import com.mincom.ellipse.edoi.ejb.msf000.MSF000_DC0017Rec
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf662.MSF662Key
import com.mincom.ellipse.edoi.ejb.msf667.MSF667Key
import com.mincom.ellipse.edoi.ejb.msf66e.MSF66EKey
import com.mincom.ellipse.edoi.ejb.msf66e.MSF66ERec
import com.mincom.ellipse.edoi.ejb.msf961.MSF961Key
import com.mincom.ellipse.edoi.ejb.msf961.MSF961Rec
import com.mincom.ellipse.edoi.ejb.msf963.MSF963Key
import com.mincom.ellipse.edoi.ejb.msf963.MSF963Rec
import com.mincom.ellipse.edoi.ejb.msf965.MSF965Key
import com.mincom.ellipse.edoi.ejb.msf96u.MSF96UKey
import com.mincom.ellipse.edoi.ejb.msf96u.MSF96URec
import com.mincom.ellipse.edoi.ejb.msf980.MSF980Key
import com.mincom.eql.Query
import com.mincom.eql.UpdateQuery
import com.mincom.eql.impl.QueryImpl
import com.mincom.eql.impl.UpdateQueryImpl

/**
 * <H2>MSGBC1 - Populate Blank Budget Code</H2>
 * This groovy script updates the following files, setting all blank Budget Code<br>
 * fields to the value passed in as a mandatory parameter:<br>
 * <br>
 * <ul><li>MSF000_DC0017 – District Control Asset No's</li>
 * <li>MSF662 – Project Estimate History</li>
 * <li>MSF667 – Project Estimates</li>
 * <li>MSF66E – Project Estimates by Expense Element by Budget Code</li>
 * <li>MSF961 – GL Budget Balances</li>
 * <li>MSF963 – GL Account Plan Statistics</li>
 * <li>MSF965 – Budget Preparation</li>
 * <li>MSF96U – GL Reporting Template Column</li>
 * <li>MSF980 – Plan Actual Statistics</li></ul>
 * The groovy script can be run in report or update mode.
 */

public class ParamsMsgBC1 {
	String UPDATE_MODE
	String BUDGET_CODE
}

WX_TODAYS_DATE = new Date().format('yyyyMMdd');
WX_TIME = new Date().format('hhmmss');
REPORT_NAME = "MSGBC1";

public void mainProcess() {

	ReportA = report.open('MSGBC1A');
	ReportA.write("");

	ParamsMsgBC1 batchParams = params.fill(new ParamsMsgBC1());

	List<String> errors = new ArrayList<String>();
	List<String> reportLines = new ArrayList<String>();
	boolean updateMode = (batchParams.UPDATE_MODE == "Y") ? true : false;

	validateBudgetCode(batchParams.BUDGET_CODE, errors);

	if (errors.isEmpty()){
		updateMsf000(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf662(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf667(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf66e(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf961(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf963(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf965(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf96u(batchParams.BUDGET_CODE, updateMode, reportLines);
		updateMsf980(batchParams.BUDGET_CODE, updateMode, reportLines);
	} else {
		for(String errorMessage: errors){
			ReportA.write("${errorMessage}");
		}
	}

	for(String reportMessage: reportLines){
		ReportA.write("${reportMessage}");
	}

	ReportA.write("");
	ReportA.close();
}

private void updateMsf000(String budgetCode, boolean updateMode, List<String> reportLines){

	Query query = new QueryImpl(MSF000_DC0017Key.class);
	query.and(MSF000_DC0017Rec.budgetCode.equalTo(" "));
	query.and(MSF000_DC0017Rec.faDepn.equalTo("Y"));

	if (updateMode){
		UpdateQuery updateQuery = new UpdateQueryImpl(query);
		updateQuery.set(MSF000_DC0017Rec.budgetCode, budgetCode);
		int count = edoiFacade.updateAll(updateQuery);
		reportLines.add("MSF000 Records Updated : ${count}");
	} else {
		query.count(MSF000_DC0017Rec.budgetCode);
		Long count = (Long) edoiFacade.search(query).getResults().get(0);
		reportLines.add("MSF000 Records to be Updated : ${count}");
	}
}

private void updateMsf662(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF662Key.class);
		query.and(MSF662Key.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF662Key.budgetCode, budgetCode);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF662 Records Updated : ${count}");
		} else {
			query.count(MSF662Key.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF662 Records to be Updated : ${count}");
		}
	}

private void updateMsf667(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF667Key.class);
		query.and(MSF667Key.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF667Key.budgetCode, budgetCode);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF667 Records Updated : ${count}");
		} else {
			query.count(MSF667Key.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF667 Records to be Updated : ${count}");
		}
	}

private void updateMsf66e(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF66EKey.class);
		query.and(MSF66EKey.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF66EKey.budgetCode, budgetCode);
			updateQuery.set(MSF66ERec.lastModDate, WX_TODAYS_DATE);
			updateQuery.set(MSF66ERec.lastModTime, WX_TIME);
			updateQuery.set(MSF66ERec.lastModUser, REPORT_NAME);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF66E Records Updated : ${count}");
		} else {
			query.count(MSF66EKey.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF66E Records to be Updated : ${count}");
		}
	}

private void updateMsf961(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF961Key.class);
		query.and(MSF961Key.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF961Key.budgetCode, budgetCode);
			updateQuery.set(MSF961Rec.lastModDate, WX_TODAYS_DATE);
			updateQuery.set(MSF961Rec.lastModTime, WX_TIME);
			updateQuery.set(MSF961Rec.lastModUser, REPORT_NAME);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF961 Records Updated : ${count}");
		} else {
			query.count(MSF961Key.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF961 Records to be Updated : ${count}");
		}
	}

private void updateMsf963(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF963Key.class);
		query.and(MSF963Key.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF963Key.budgetCode, budgetCode);
			updateQuery.set(MSF963Rec.lastModDate, WX_TODAYS_DATE);
			updateQuery.set(MSF963Rec.lastModTime, WX_TIME);
			updateQuery.set(MSF963Rec.lastModUser, REPORT_NAME);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF963 Records Updated : ${count}");
		} else {
			query.count(MSF963Key.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF963 Records to be Updated : ${count}");
		}
	}

private void updateMsf965(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF965Key.class);
		query.and(MSF965Key.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF965Key.budgetCode, budgetCode);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF965 Records Updated : ${count}");
		} else {
			query.count(MSF965Key.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF965 Records to be Updated : ${count}");
		}
	}

private void updateMsf96u(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF96UKey.class);
		query.and(MSF96URec.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF96URec.budgetCode, budgetCode);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF96U Records Updated : ${count}");
		} else {
			query.count(MSF96URec.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF96U Records to be Updated : ${count}");
		}
	}

private void updateMsf980(String budgetCode, boolean updateMode, List<String> reportLines){

		Query query = new QueryImpl(MSF980Key.class);
		query.and(MSF980Key.budgetCode.equalTo(" "));

		if (updateMode){
			UpdateQuery updateQuery = new UpdateQueryImpl(query);
			updateQuery.set(MSF980Key.budgetCode, budgetCode);
			int count = edoiFacade.updateAll(updateQuery);
			reportLines.add("MSF980 Records Updated : ${count}");
		} else {
			query.count(MSF980Key.budgetCode);
			Long count = (Long) edoiFacade.search(query).getResults().get(0);
			reportLines.add("MSF980 Records to be Updated : ${count}");
		}
	}

private void validateBudgetCode(String budgetCode, List<String> errors){

	// Ensure the Budget Code exists on the BT table

	BUDGET_CODE_TYPE = "BT";

	MSF010Key key = new MSF010Key(BUDGET_CODE_TYPE, budgetCode);

	if (!edoiFacade.exists(key)) {
		errors.add("Error - Budget Code does not exist");
	}

	// Ensure the Budget Code doesn't exist on MSF667

	Query query1 = new QueryImpl(MSF667Key.class);
	query1.and(MSF667Key.budgetCode.equalTo(budgetCode));
	query1.count(MSF667Key.budgetCode);

	Long count1 = (Long) edoiFacade.search(query1).getResults().get(0);

	if (count1 > 0){
		errors.add("Error - Budget Code already exists on MSF667");
	}

	// Ensure the Budget Code doesn't exist on MSF961

	Query query2 = new QueryImpl(MSF961Key.class);
	query2.and(MSF961Key.budgetCode.equalTo(budgetCode));
	query2.count(MSF961Key.budgetCode);

	Long count2 = (Long) edoiFacade.search(query2).getResults().get(0);

	if (count2 > 0){
		errors.add("Error - Budget Code already exists on MSF961");
	}

	// Ensure the Budget Code doesn't exist on MSF980

	Query query3 = new QueryImpl(MSF980Key.class);
	query3.and(MSF980Key.budgetCode.equalTo(budgetCode));
	query3.count(MSF980Key.budgetCode);

	Long count3 = (Long) edoiFacade.search(query3).getResults().get(0);

	if (count3 > 0){
		errors.add("Error - Budget Code already exists on MSF980");
	}
}

mainProcess();
