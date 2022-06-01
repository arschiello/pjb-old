package com.mincom.ellipse.scripts.conversion

import com.mincom.ellipse.edoi.ejb.msf667.MSF667Key
import com.mincom.ellipse.edoi.ejb.msf667.MSF667Rec
import com.mincom.ellipse.edoi.ejb.msf66e.MSF66EKey
import com.mincom.ellipse.edoi.ejb.msf66e.MSF66ERec
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.eql.Constraint
import com.mincom.eql.Query
import com.mincom.eql.impl.QueryImpl
import java.text.DecimalFormat

/**
 * <H2>MSG66Y - Create Project Estimate Year Records</H2>
 * <p>This groovy script will create Project Estimate summary records at Year level by summing quantities held at Period level,
 * on MSF667 and MSF66E, for the relevant years.</p>
 * <p>Periods are stored on both files in reversed format, for example 798395 = 999999 - 201604<br>
 * Year records contain a reversed period ending with 99, for example 798399 = 999999 - 201600</p>
 * <p>It is possible to create Year records manually in Ellipse. This groovy script will not replace or alter existing records,
 * it will only create them when they are missing.</p>
 * <p>There is one request parameter for this script:</p>
 * <ul><li>Update Mode (Y/N) - Mandatory, defaults to 'N'.</li>
 */

public class ParamsMsg66y {
	String UPDATE_MODE
}

public class EstimateData {
	String projectNo
	String budgetCode
	String revsdPeriod
	String categoryCode
	String expRevInd
	String summaryCode
	BigDecimal yearDirEstCost
	BigDecimal yearTotEstCost
	BigDecimal yearDirUnlFpEst
	BigDecimal yearTotUnlFpEst
}

WX_TODAYS_DATE = new Date().format('yyyyMMdd');
WX_TIME = new Date().format('hhmmss');
REPORT_NAME = "MSG66Y";

loggedOnDistrict = "";

msf667ReadCount = 0;
msf667CreateCount = 0;
msf66eReadCount = 0;
msf66eCreateCount = 0;

public void mainProcess() {

	ReportA = report.open('MSG66YA');
	ReportB = report.open('MSG66YB');

	ParamsMsg66y batchParams = params.fill(new ParamsMsg66y());

	loggedOnDistrict = request.district;

	log.info("loggedOnDistrict: ${loggedOnDistrict}");

	boolean updateMode = (batchParams.UPDATE_MODE == "Y");
	String mode = (updateMode) ? "--- UPDATE MODE ---" : "--- REPORT MODE ---";

	// Process MSF667, Report A

	reportAColumnHeadings(mode);

	EstimateData estimateData667 = new EstimateData();

	initialise(estimateData667);

	Query query1 = getEstimates667();

	edoi.search(query1, restart.each({MSF667Rec msf667Rec ->

		msf667ReadCount++;

		if (current667SameAsPrev(msf667Rec, estimateData667)) {
			estimateData667.yearDirEstCost = estimateData667.yearDirEstCost + msf667Rec.getDirEstCost();
			estimateData667.yearTotEstCost = estimateData667.yearTotEstCost + msf667Rec.getTotEstCost();
			estimateData667.yearDirUnlFpEst = estimateData667.yearDirUnlFpEst + msf667Rec.getDirUnlFpEst();
			estimateData667.yearTotUnlFpEst = estimateData667.yearTotUnlFpEst + msf667Rec.getTotUnlFpEst();
		} else {
			createMsf667(estimateData667, updateMode);
			estimateData667.yearDirEstCost = msf667Rec.getDirEstCost();
			estimateData667.yearTotEstCost = msf667Rec.getTotEstCost();
			estimateData667.yearDirUnlFpEst = msf667Rec.getDirUnlFpEst();
			estimateData667.yearTotUnlFpEst = msf667Rec.getTotUnlFpEst();
		}

		estimateData667.projectNo = msf667Rec.getPrimaryKey().getProjectNo();
		estimateData667.budgetCode = msf667Rec.getPrimaryKey().getBudgetCode();
		estimateData667.revsdPeriod = msf667Rec.getPrimaryKey().getRevsdPeriod().substring(0,4)+"99";
		estimateData667.categoryCode = msf667Rec.getPrimaryKey().getCategoryCode();
		estimateData667.expRevInd = msf667Rec.getPrimaryKey().getExpRevInd();
	}));

	if (msf667ReadCount > 0) {
		createMsf667(estimateData667, updateMode);
	}

	reportASummary(updateMode);

	// Process MSF66E, Report B

	reportBColumnHeadings(mode);

	EstimateData estimateData66e = new EstimateData();

	initialise(estimateData66e);

	Query query2 = getEstimates66e();

	edoi.search(query2, restart.each({MSF66ERec msf66eRec ->

		msf66eReadCount++;

		if (current66eSameAsPrev(msf66eRec, estimateData66e)) {
			estimateData66e.yearDirEstCost = estimateData66e.yearDirEstCost + msf66eRec.getDirEstCost();
			estimateData66e.yearTotEstCost = estimateData66e.yearTotEstCost + msf66eRec.getTotEstCost();
			estimateData66e.yearDirUnlFpEst = estimateData66e.yearDirUnlFpEst + msf66eRec.getDirUnlFpEst();
			estimateData66e.yearTotUnlFpEst = estimateData66e.yearTotUnlFpEst + msf66eRec.getTotUnlFpEst();
		} else {
			createMsf66e(estimateData66e, updateMode);
			estimateData66e.yearDirEstCost = msf66eRec.getDirEstCost();
			estimateData66e.yearTotEstCost = msf66eRec.getTotEstCost();
			estimateData66e.yearDirUnlFpEst = msf66eRec.getDirUnlFpEst();
			estimateData66e.yearTotUnlFpEst = msf66eRec.getTotUnlFpEst();
		}

		estimateData66e.projectNo = msf66eRec.getPrimaryKey().getProjectNo();
		estimateData66e.budgetCode = msf66eRec.getPrimaryKey().getBudgetCode();
		estimateData66e.revsdPeriod = msf66eRec.getPrimaryKey().getRevsdPeriod().substring(0,4)+"99";
		estimateData66e.summaryCode = msf66eRec.getPrimaryKey().getSummaryCode();
	}));

	if (msf66eReadCount > 0) {
		createMsf66e(estimateData66e, updateMode);
	}

	reportBSummary(updateMode);
}

private void initialise(EstimateData ed) {
	ed.yearDirEstCost = 0.00;
	ed.yearTotEstCost = 0.00;
	ed.yearDirUnlFpEst = 0.00;
	ed.yearTotUnlFpEst = 0.00;
}

private Query getEstimates667() {

	Query query = new QueryImpl(MSF667Rec.class);

	query.and(MSF667Key.dstrctCode.equalTo(loggedOnDistrict));
	query.and(MSF667Key.revsdPeriod.notEqualTo("000000"));
	query.and(MSF667Key.revsdPeriod.notEqualTo(" "));
	query.andNot(MSF667Key.revsdPeriod.like("%99"));

	Constraint[] c = [MSF667Key.projectNo, MSF667Key.budgetCode, MSF667Key.categoryCode, MSF667Key.expRevInd, MSF667Key.revsdPeriod];

	query.nonIndexSortAscending(c);

	return query;
}

private Query getEstimates66e() {

	Query query = new QueryImpl(MSF66ERec.class);

	query.and(MSF66EKey.dstrctCode.equalTo(loggedOnDistrict));
	query.and(MSF66EKey.revsdPeriod.notEqualTo("000000"));
	query.and(MSF66EKey.revsdPeriod.notEqualTo(" "));
	query.andNot(MSF66EKey.revsdPeriod.like("%99"));
	query.orderByAscending(MSF66ERec.aix1);

	return query;
}

private boolean current667SameAsPrev(MSF667Rec msf667Rec, EstimateData ed){

	if (ed.projectNo == null)	{
		return true
	}
	if (ed.projectNo.equals(msf667Rec.getPrimaryKey().getProjectNo()) &&
	ed.budgetCode.equals(msf667Rec.getPrimaryKey().getBudgetCode()) &&
	ed.revsdPeriod.substring(0,4).equals(msf667Rec.getPrimaryKey().getRevsdPeriod().substring(0,4)) &&
	ed.categoryCode.equals(msf667Rec.getPrimaryKey().getCategoryCode()) &&
	ed.expRevInd.equals(msf667Rec.getPrimaryKey().getExpRevInd())) {
		return true
	}
	return false
}

private boolean current66eSameAsPrev(MSF66ERec msf66eRec, EstimateData ed){

	if (ed.projectNo == null)	{
		return true
	}
	if (ed.projectNo.equals(msf66eRec.getPrimaryKey().getProjectNo()) &&
	ed.budgetCode.equals(msf66eRec.getPrimaryKey().getBudgetCode()) &&
	ed.revsdPeriod.substring(0,4).equals(msf66eRec.getPrimaryKey().getRevsdPeriod().substring(0,4)) &&
	ed.summaryCode.equals(msf66eRec.getPrimaryKey().getSummaryCode())) {
		return true
	}
	return false
}

private void createMsf667(EstimateData ed, boolean updateMode) {

	MSF667Key key = new MSF667Key(loggedOnDistrict, ed.projectNo,
			ed.budgetCode, ed.revsdPeriod, ed.categoryCode, ed.expRevInd);

	if (edoi.exists(key)) {
		String message = "Year record already exists";
		reportADetail(ed, message);
	} else {
		msf667CreateCount++;
		reportADetail(ed);
		if (updateMode) {
			try {
				msf667Rec = new MSF667Rec(key);
				msf667Rec.setDirEstCost(ed.yearDirEstCost);
				msf667Rec.setTotEstCost(ed.yearTotEstCost);
				msf667Rec.setDirUnlFpEst(ed.yearDirUnlFpEst);
				msf667Rec.setTotUnlFpEst(ed.yearTotUnlFpEst);
				edoi.create(msf667Rec);
			} catch (Exception e) {
				listErrors(e);
			}
		}
	}
}

private void createMsf66e(EstimateData ed, boolean updateMode) {

	MSF66EKey key = new MSF66EKey(loggedOnDistrict, ed.projectNo,
			ed.budgetCode, ed.revsdPeriod,
			ed.summaryCode);

	if (edoi.exists(key)) {
		String message = "Year record already exists";
		reportBDetail(ed, message);
	} else {
		msf66eCreateCount++;
		reportBDetail(ed);
		if (updateMode) {
			try {
				msf66eRec = new MSF66ERec(key);
				msf66eRec.setDirEstCost(ed.yearDirEstCost);
				msf66eRec.setTotEstCost(ed.yearTotEstCost);
				msf66eRec.setDirUnlFpEst(ed.yearDirUnlFpEst);
				msf66eRec.setTotUnlFpEst(ed.yearTotUnlFpEst);
				msf66eRec.setCreationDate(WX_TODAYS_DATE);
				msf66eRec.setCreationTime(WX_TIME);
				msf66eRec.setCreationUser(REPORT_NAME);
				msf66eRec.setLastModDate(WX_TODAYS_DATE);
				msf66eRec.setLastModTime(WX_TIME);
				msf66eRec.setLastModUser(REPORT_NAME);
				edoi.create(msf66eRec);
			} catch (Exception e) {
				listErrors(e);
			}
		}
	}
}

private void reportAColumnHeadings(String mode) {

	List <String> headingsA = new ArrayList <String>();

	String reportHeading = "Create Project Estimate Year Records on MSF667";

	headingsA.add(reportHeading.center(160))
	headingsA.add(mode.center(160))
	headingsA.add(String.format("%160s"," "));
	headingsA.add("District  Project   Budget   Revsd    Category   Expense                          Direct                   Total      Direct Unallocated       Total Unallocated");
	headingsA.add("Code      Number    Code     Period   Code       Rev Ind  Year             Estimate Cost           Estimate Cost             FP Estimate             FP Estimate");
	headingsA.add(String.format("%160s"," ").replace(" ", "-"));
	headingsA.each{ ReportA.write(it) };
}

private void reportBColumnHeadings(String mode) {

	List <String> headingsB = new ArrayList <String>();

	String reportHeading = "Create Project Estimate Year Records on MSF66E";

	headingsB.add(reportHeading.center(165))
	headingsB.add(mode.center(165))
	headingsB.add(String.format("%165s"," "));
	headingsB.add("District  Project   Budget   Revsd    Summary                                          Direct                   Total      Direct Unallocated       Total Unallocated");
	headingsB.add("Code      Number    Code     Period   Code                     Year             Estimate Cost           Estimate Cost             FP Estimate             FP Estimate");
	headingsB.add(String.format("%165s"," ").replace(" ", "-"));
	headingsB.each{ ReportB.write(it) };
}

private void reportADetail(EstimateData ed) {
	ReportA.write(loggedOnDistrict.padRight(10) +
			ed.projectNo.padRight(10) +
			ed.budgetCode.padRight(9) +
			ed.revsdPeriod.padRight(9) +
			ed.categoryCode.padRight(11) +
			ed.expRevInd.padRight(9) +
			String.valueOf(999999 - Integer.parseInt(ed.revsdPeriod)).substring(0,4).padRight(6) +
			displayNumber(ed.yearDirEstCost).padLeft(24) +
			displayNumber(ed.yearTotEstCost).padLeft(24) +
			displayNumber(ed.yearDirUnlFpEst).padLeft(24) +
			displayNumber(ed.yearTotUnlFpEst).padLeft(24));
}

private void reportADetail(EstimateData ed, String message) {
	ReportA.write(loggedOnDistrict.padRight(10) +
			ed.projectNo.padRight(10) +
			ed.budgetCode.padRight(9) +
			ed.revsdPeriod.padRight(9) +
			ed.categoryCode.padRight(11) +
			ed.expRevInd.padRight(9) +
			String.valueOf(999999 - Integer.parseInt(ed.revsdPeriod)).substring(0,4).padRight(6) +
			message);
}

private void reportBDetail(EstimateData ed) {
	ReportB.write(loggedOnDistrict.padRight(10) +
			ed.projectNo.padRight(10) +
			ed.budgetCode.padRight(9) +
			ed.revsdPeriod.padRight(9) +
			ed.summaryCode.padRight(25) +
			String.valueOf(999999 - Integer.parseInt(ed.revsdPeriod)).substring(0,4).padRight(6) +
			displayNumber(ed.yearDirEstCost).padLeft(24) +
			displayNumber(ed.yearTotEstCost).padLeft(24) +
			displayNumber(ed.yearDirUnlFpEst).padLeft(24) +
			displayNumber(ed.yearTotUnlFpEst).padLeft(24));
}

private void reportBDetail(EstimateData ed, String message) {
	ReportB.write(loggedOnDistrict.padRight(10) +
			ed.projectNo.padRight(10) +
			ed.budgetCode.padRight(9) +
			ed.revsdPeriod.padRight(9) +
			ed.summaryCode.padRight(25) +
			String.valueOf(999999 - Integer.parseInt(ed.revsdPeriod)).substring(0,4).padRight(6) +
			message);
}

private void reportASummary(boolean updateMode) {
	ReportA.write(String.format("%160s"," ").replace(" ", "-"));
	ReportA.write("");
	ReportA.write("MSF667 Period Records Read        : ${msf667ReadCount}");

	if (updateMode) {
		ReportA.write("MSF667 Year Records Created       : ${msf667CreateCount}");
	} else {
		ReportA.write("MSF667 Year Records to be Created : ${msf667CreateCount}");
	}
	ReportA.write("");
	ReportA.write(String.format("%160s"," ").replace(" ", "-"));
	ReportA.close();
}

private void reportBSummary(boolean updateMode) {
	ReportB.write(String.format("%165s"," ").replace(" ", "-"));
	ReportB.write("");
	ReportB.write("MSF66E Period Records Read        : ${msf66eReadCount}");

	if (updateMode) {
		ReportB.write("MSF66E Year Records Created       : ${msf66eCreateCount}");
	} else {
		ReportB.write("MSF66E Year Records to be Created : ${msf66eCreateCount}");
	}
	ReportB.write("");
	ReportB.write(String.format("%165s"," ").replace(" ", "-"));
	ReportB.close();
}

private String displayNumber(BigDecimal n) {
	DecimalFormat df = new DecimalFormat("#0.00");
	return df.format(n).toString();
}

private void listErrors (EnterpriseServiceOperationException e) {
	List <ErrorMessageDTO> listError = e.getErrorMessages()
	listError.each{ErrorMessageDTO errorDTO ->
		log.info ("Error Code: " + errorDTO.getCode())
		log.info ("Error Message: " + errorDTO.getMessage())
		log.info ("Error Fields: " + errorDTO.getFieldName())
	}
}

mainProcess();