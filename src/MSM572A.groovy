/**
 * @EMS 2014
 *
 * Revision History
 * 10-Okt-2020 - a9ra5213 - Ricky Afriano - PJB AR Implementation
 * .........................................Auto Generate Receipt Number.
 * 01-Mar-2014.............a9aa6024 Initial Non User Exit- MSM688A
 * ........................
 * */

import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.eql.impl.*
import com.mincom.eql.*
import java.util.Date;

import javax.naming.InitialContext

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.System

import com.mincom.ellipse.edoi.ejb.msf000.MSF000_PCYYMMKey
import com.mincom.ellipse.edoi.ejb.msf000.MSF000_PCYYMMRec
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import groovy.sql.Sql;
import com.mincom.eql.impl.QueryImpl;
import javax.naming.InitialContext;

public class MSM572A extends MSOHook {

	String hookVersion = "10-OKt-20 00:30"
	String dstrctCode , startDate , endDate , subAsset  ,retireTy,dispDate ,depTy,depTy2 ,dispAmtS, receiptDate , actDispTx
	String addDeprBk , addDeprTx , deprMeth ,CurrentPer ,acctProf
	BigDecimal actDispVal , dispAmt
	//PJB-AR-START
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	String bankBranch = "";
	String bankPrefix = "";
	String currYear = "";
	String currMonth = "";
	Integer seqInvNo = 1;
	String district2 = "";
	String dummyArNo = "";
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)
	//PJB-AR-END
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){

		// onDisplay is only to be used for setting up fields.

		log.info("Hooks onDisplay logging.version: ${hookVersion}")
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date nowdate = new Date();
		String dateNow = dateFormat.format(nowdate)
		log.info("now"+ dateNow )
		log.info("now" + nowdate)

		receiptDate = screen.getField("RECEIPT_DATE1I").getValue()

		log.info("receipt Date is ${receiptDate}")

		//screen.getField("RECEIPT_DATE1I").setValue("")

		// Override screen field value which will be displayed to the user

		//protect field (read-only)

		//return the screen which will be displayed (includes the updated field values)

		//PJB-AR-START
		screen.setFieldValue("RECEIPT_FLAG1I", "Y")
		screen.getField("RECEIPT_NUM1I").setIsProtected(true);
		if (screen.getField("RECEIPT_NUM1I").getValue().trim().equals("")) {
			screen.setFieldValue("REF1I", "DUMMY");
		}else {
			screen.setFieldValue("REF1I", screen.getField("RECEIPT_NUM1I").getValue().trim());
		}

		//PJB-AR-END
		return null
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){

		log.info("Hooks onPreSubmit logging.version: ${hookVersion}")

		dstrctCode = tools.commarea.District
		def mss002lnk = tools.eroi.execute('MSS002', {mss002lnk ->
			mss002lnk.dstrctCode = dstrctCode
			mss002lnk.module = '3560'
			mss002lnk.optionX = '2'
		})
		CurrentPer =  mss002lnk.glCodeDstrct
		//CurrentPer = '1501'
		//CurrentPer = tools.commarea.3680CP
		log.info("CurrentPer2 is ${CurrentPer}")
		receiptDate = screen.getField("RECEIPT_DATE1I").getValue()

		log.info("receipt Date is ${receiptDate}")
		if (receiptDate != "") {
			Constraint key000_1 = MSF000_PCYYMMKey.dstrctCode.equalTo(dstrctCode)
			Constraint key000_2 = MSF000_PCYYMMKey.controlRecType.equalTo("PC")
			Constraint key000_3 = MSF000_PCYYMMKey.controlRecNo.equalTo(CurrentPer)

			QueryImpl query000 = new QueryImpl(MSF000_PCYYMMRec.class).and(key000_1).and(key000_2).and(key000_3)

			MSF000_PCYYMMRec msf000rec_1 = tools.edoi.firstRow(query000)

			if (msf000rec_1 != null){
				MSF000_PCYYMMRec msf000recb = new MSF000_PCYYMMRec()
				msf000recb = msf000rec_1
				endDate = msf000recb.getThisMeDate()
				startDate =  msf000recb.getPcStartDate()
				log.info("startDate : ${startDate}" )
				log.info("endDate : ${endDate}" )
				if ( receiptDate < startDate ||  receiptDate > endDate ) {
					screen.setErrorMessage(new MsoErrorMessage("test 1", "0131", "RECEIPT DATE MUST BE IN CURRENT PERIOD,  Current Period = ${CurrentPer} ", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

					//need to return screen if there is any error as we do not want to run the business logic when there is any error
					return screen
				}
			}
		}

		//PJB-AR-START
		dummyArNo = screen.getField("RECEIPT_NUM1I").getValue();
		log.info("dummyArNo : " + dummyArNo)

		bankBranch = "";
		bankPrefix = "";
		currYear = "";
		currMonth = "";
		seqInvNo = 1;
		String bankAcctNo = "";


		screen.setFieldValue("RECEIPT_FLAG1I", "Y")
		bankBranch = screen.getField("BRANCH_CODE1I").getValue();
		bankAcctNo = screen.getField("BANK_ACCT_NO1I").getValue();
		def QRY0 = sql.firstRow("select * from msf000_bk where trim(BRANCH_CODE) = trim('"+bankBranch+"') and DSTRCT_CODE = ' ' and trim(BANK_ACCT_NO) = trim('"+bankAcctNo+"')");
		if(!QRY0.equals(null)) {
			district2 = QRY0.OWNED_BY.trim();
		}
		QRY0 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 a where ENTITY_TYPE = 'PBK' " +
				"and a.REF_NO = '001' and a.SEQ_NUM = '001' and trim(entity_value) = trim('"+district2+bankBranch+"')");
		if(QRY0.equals(null)) {
			screen.setErrorMessage(new MsoErrorMessage("", "9999", "BANK PREFIX DOES NOT EXIST!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
			MsoField DOCUMENT_NO = new MsoField()
			DOCUMENT_NO.setName("BRANCH_CODE1I")
			screen.setCurrentCursorField(DOCUMENT_NO)
			return screen
		}else {
			bankPrefix = QRY0.REF_CODE.trim()
		}

		String HO = "";
		def QRY1 = sql.firstRow("select trim(NVL(max(ENTITY_VALUE),'NA')) ENTITY_VALUE from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'HOD'");
		if (QRY1.ENTITY_VALUE.equals("NA")) {
			screen.setErrorMessage(new MsoErrorMessage("", "9999", "HEAD OFFICE SETTING NOT DEFINED!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
			return screen
		}else {
			HO = QRY1.ENTITY_VALUE;
		}

		QRY0 = sql.firstRow("select DSTRCT_CODE,CURR_ACCT_MN,CURR_ACCT_YR,CURR_ACCT_MN||'/'||CURR_ACCT_YR CURR_PERIOD,'20'||CURR_ACCT_YR||CURR_ACCT_MN FOR_PER " +
				"from msf000_cp where trim(DSTRCT_CODE) = trim('"+district2+"') and CONTROL_REC_NO = '0009'");
		if(!QRY0.equals(null)) {
			currYear = QRY0.CURR_ACCT_YR;
			currMonth = QRY0.CURR_ACCT_MN;
		}
		String monthPrefix;
		if (currMonth.equals("01")) {
			monthPrefix = "M";
		}else if (currMonth.equals("02")) {
			monthPrefix = "N";
		}else if (currMonth.equals("03")) {
			monthPrefix = "O";
		}else if (currMonth.equals("04")) {
			monthPrefix = "P";
		}else if (currMonth.equals("05")) {
			monthPrefix = "Q";
		}else if (currMonth.equals("06")) {
			monthPrefix = "R";
		}else if (currMonth.equals("07")) {
			monthPrefix = "S";
		}else if (currMonth.equals("08")) {
			monthPrefix = "T";
		}else if (currMonth.equals("09")) {
			monthPrefix = "U";
		}else if (currMonth.equals("10")) {
			monthPrefix = "V";
		}else if (currMonth.equals("11")) {
			monthPrefix = "W";
		}else if (currMonth.equals("12")) {
			monthPrefix = "X";
		}

		QRY0 = sql.firstRow("select TRIM(a.REF_CODE) SEQUENCE from ELLIPSECUSTOM.EMF071 a where a.ENTITY_TYPE = 'SRC' " +
				"and a.REF_NO = '001' and a.SEQ_NUM = '001' and trim(a.entity_value) = trim('"+district2+bankBranch+currMonth+currYear+"')");
		if(!QRY0.equals(null)) {
			if(!QRY0.SEQUENCE.equals("") && !QRY0.SEQUENCE.equals("0") && !QRY0.SEQUENCE.equals(null)) {
				seqInvNo = Integer.parseInt(QRY0.SEQUENCE) + 1 ;
			}
		}

		String dumSeqInvNo = "";
		dumSeqInvNo = String.format("%03d", seqInvNo);

		String arInvNo = monthPrefix + currYear + dumSeqInvNo + bankPrefix
		log.info("arInvNo : " + arInvNo );
		if(arInvNo.length() != 8) {
			screen.setErrorMessage(new MsoErrorMessage("", "9999", "INVALID LENGTH AR INV NO !", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
			return screen
		}

		QRY0 = sql.firstRow("select * from ELLIPSECUSTOM.EMF570 where RECEIPT_NO = '"+arInvNo+"' and DSTRCT_CODE = '"+district2+"' ");
		if (!QRY0.equals(null)) {
			screen.setErrorMessage(new MsoErrorMessage("", "9999","SEQUENCE AR RECEIPT NUMBER " + arInvNo + " ALREADY EXIST!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
			return screen
		}

		QRY0 = sql.firstRow("select * from MSF570 where trim(RECEIPT_NUM) = trim('"+arInvNo+"') and DSTRCT_CODE = '"+district2+"' ");
		if (!QRY0.equals(null)) {
			screen.setErrorMessage(new MsoErrorMessage("", "9999","SEQUENCE AR RECEIPT NUMBER " + arInvNo + " ALREADY EXIST!", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
			return screen
		}
		screen.getField("RECEIPT_NUM1I").setIsProtected(false);
		screen.setFieldValue("RECEIPT_NUM1I", arInvNo);
		screen.setFieldValue("REF1I", arInvNo);
		screen.getField("RECEIPT_NUM1I").setIsProtected(true);
		//PJB-AR-END

	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM572A logging.version: ${hookVersion}");
		//PJB-AR-START
		String FKEYS1I = input.getField("FKEYS1I").getValue();
		String ERRMESS1I = input.getField("ERRMESS1I").getValue();
		if (((input.getNextAction() == 1) || (input.getNextAction() == 0))) {
			if (FKEYS1I.trim().contains("XMIT-Confirm") && ERRMESS1I.trim().contains("<SUBMIT> TO CONFIRM THE UPDATE")) {
				log.info("dummyArNo2 : " + dummyArNo.trim().substring(3,6));
				if(isNumeric(dummyArNo.trim().substring(3,6))) {
					String EMP_ID = GetUserEmpID(tools.commarea.UserId.trim(), tools.commarea.District.trim())
					String QueryDelete = ("delete ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'SRC' and trim(entity_value) = '"+district2+bankBranch+currMonth+currYear+"' and seq_num = '001' and ref_no = '001'");
					sql.execute(QueryDelete);
					GetNowDateTime();
					log.info("district2 : " + district2);
					log.info("bankBranch : " + bankBranch);
					log.info("currMonth : " + currMonth);
					log.info("currYear : " + currYear);
					log.info("currYear : " + currYear);
					String QueryInsert = ("Insert into ELLIPSECUSTOM.EMF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SRC','"+district2+bankBranch+currMonth+currYear+"','001','001','"+strCrDT+"','"+strCrTM+"','"+EMP_ID+"',?,'            ')");
					sql.execute(QueryInsert,Integer.parseInt(dummyArNo.trim().substring(3,6)).toString().trim());
					log.info ("EMF071 SEQ UPDATE : " + QueryInsert);
				}
			}
		}
		//PJB-AR-END
		return result
	}
	public boolean isNumeric(String str) {
		try {
			str = str.replace(",", "")
			Integer.parseInt(str);
			//Float.parseFloat(str);
			return true;
		}
		catch (NumberFormatException e) {
			// s is not numeric
			return false;
		}
	}
	public def GetNowDateTime() {
		Date InPer = new Date();

		Calendar cal = Calendar.getInstance();
		cal.setTime(InPer);
		int iyear = cal.get(Calendar.YEAR);
		int imonth = cal.get(Calendar.MONTH);
		int iday = cal.get(Calendar.DAY_OF_MONTH);
		int iHH = cal.get(Calendar.HOUR_OF_DAY);
		int iMM = cal.get(Calendar.MINUTE);
		int iSS = cal.get(Calendar.SECOND);

		if (iday.toString().trim().length() < 2){
			StrDT = "0" + iday.toString().trim()
		}else{
			StrDT = iday.toString().trim()
		}

		"(imonth + 1) untuk membuat bulan sesuai"
		if ((imonth + 1).toString().trim().length() < 2){
			StrMT = "0" + (imonth + 1).toString().trim()
		}else{
			StrMT = (imonth + 1).toString().trim()
		}

		if (iyear.toString().trim().length() < 3){
			StrYR = "20" + iyear.toString().trim()
		}else{
			StrYR = iyear.toString().trim()
		}

		strCrDT = StrYR + StrMT + StrDT

		if (iHH.toString().trim().length() < 2){
			StrHH = "0" + iHH.toString().trim()
		}else{
			StrHH = iHH.toString().trim()
		}

		if (iMM.toString().trim().length() < 2){
			StrMM = "0" + iMM.toString().trim()
		}else{
			StrMM = iMM.toString().trim()
		}

		if (iSS.toString().trim().length() < 2){
			StrSS = "0" + iSS.toString().trim()
		}else{
			StrSS = iSS.toString().trim()
		}

		strCrTM = StrHH + StrMM + StrSS
	}

	public String GetUserEmpID(String user, String district) {
		String EMP_ID = "";
		def QRY1;
		QRY1 = sql.firstRow("select * From msf020 where ENTRY_TYPE = 'S' and trim(ENTITY) = trim('"+user+"') and DSTRCT_CODE = '"+district+"'");
		if(!QRY1.equals(null)) {
			EMP_ID = QRY1.EMPLOYEE_ID;
		}
		return EMP_ID;
	}

}






