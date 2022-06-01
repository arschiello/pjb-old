/**
 * @EMS 2020
 *
 * 30-May-2022 - a9ra5213 - Ricky Afriano - PJB UBJOM Merger
 * 10-Okt-2020 - a9ra5213 - Ricky Afriano - PJB AR Implementation
 *               Initial Coding - Validate Document Reference, prevent duplication.
 **/
package PjbUbJom

import com.mincom.ellipse.ejra.mso.GenericMsoRecord;
import com.mincom.ellipse.ejra.mso.MsoErrorMessage;
import com.mincom.ellipse.hook.hooks.MSOHook;
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException;

import groovy.sql.Sql;

import com.mincom.eql.impl.QueryImpl;

import javax.naming.InitialContext;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mincom.ellipse.ejra.mso.MsoField;

import java.util.Calendar;

import com.mincom.ellipse.edoi.ejb.msf001.MSF001_DC0031Key
import com.mincom.ellipse.edoi.ejb.msf001.MSF001_DC0031Rec
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec

public class MSM906A extends MSOHook{
	String hookVersion = "1";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM906A logging.version: ${hookVersion}");

		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM906A logging.version: ${hookVersion}");
		String jnlNo = screen.getField("JOURNAL_NO1I").getValue();
		String docRef1 = screen.getField("DOCUMENT_REF1I1").getValue();
		String docRef2 = screen.getField("DOCUMENT_REF1I2").getValue();
		String docRef3 = screen.getField("DOCUMENT_REF1I3").getValue();
		String docRef4 = screen.getField("DOCUMENT_REF1I4").getValue();
		String dst1 = screen.getField("DSTRCT_CODE1I1").getValue();
		String dst2 = screen.getField("DSTRCT_CODE1I2").getValue();
		String dst3 = screen.getField("DSTRCT_CODE1I3").getValue();
		String dst4 = screen.getField("DSTRCT_CODE1I4").getValue();
		def QRY1;

		log.info ("screen.getNextAction()  : " + screen.getNextAction().toString());
		if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			/*if (!docRef1.trim().equals("")) {
				if (!jnlNo.trim().equals("")) {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst1.trim()+docRef1.trim()+"') and REF_NO = '001' and SEQ_NUM = '001' and trim(ref_code) <> '"+jnlNo+"'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef1.trim()+ " FOR DISTRICT "+dst1.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I1")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}else {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst1.trim()+docRef1.trim()+"') and REF_NO = '001' and SEQ_NUM = '001'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef1.trim()+ " FOR DISTRICT "+dst1.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I1")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}
			}

			if (!docRef2.trim().equals("")) {
				if (!jnlNo.trim().equals("")) {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst2.trim()+docRef2.trim()+"') and REF_NO = '001' and SEQ_NUM = '001' and trim(ref_code) <> '"+jnlNo+"'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef2.trim()+ " FOR DISTRICT "+dst2.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I2")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}else {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst2.trim()+docRef2.trim()+"') and REF_NO = '001' and SEQ_NUM = '001'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef2.trim()+ " FOR DISTRICT "+dst2.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I2")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}
			}

			if (!docRef3.trim().equals("")) {
				if (!jnlNo.trim().equals("")) {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst3.trim()+docRef3.trim()+"') and REF_NO = '001' and SEQ_NUM = '001' and trim(ref_code) <> '"+jnlNo+"'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef3.trim()+ " FOR DISTRICT "+dst3.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I3")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}else {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst3.trim()+docRef3.trim()+"') and REF_NO = '001' and SEQ_NUM = '001'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef3.trim()+ " FOR DISTRICT "+dst3.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I3")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}
			}

			if (!docRef4.trim().equals("")) {
				if (!jnlNo.trim().equals("")) {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst4.trim()+docRef4.trim()+"') and REF_NO = '001' and SEQ_NUM = '001' and trim(ref_code) <> '"+jnlNo+"'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef4.trim()+ " FOR DISTRICT "+dst4.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I4")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}else {
					QRY1 = sql.firstRow("select * from ELLIPSECUSTOM.EMF071 where ENTITY_TYPE = 'RJN' and trim(entity_value) = trim('"+dst4.trim()+docRef4.trim()+"') and REF_NO = '001' and SEQ_NUM = '001'");
					if(!QRY1.equals(null)) {
						screen.setErrorMessage(new MsoErrorMessage("", "9999", "DOCUMENT REF "+docRef4.trim()+ " FOR DISTRICT "+dst4.trim()+" ALREADY EXIST IN OTHER JOURNAL : " + QRY1.REF_CODE.trim(), MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
						MsoField DOCUMENT_NO = new MsoField()
						DOCUMENT_NO.setName("DOCUMENT_REF1I4")
						screen.setCurrentCursorField(DOCUMENT_NO)
						return screen
					}
				}
			}
			*/


			String DST1 = screen.getField("DSTRCT_CODE1I1").getValue();
			String DST2 = screen.getField("DSTRCT_CODE1I2").getValue();
			String DST3 = screen.getField("DSTRCT_CODE1I3").getValue();
			String DST4 = screen.getField("DSTRCT_CODE1I4").getValue();
			
			if (DST1.trim().equals("")) {
				DST1 = tools.commarea.District.trim();
			}
			if (DST2.trim().equals("")) {
				DST2 = tools.commarea.District.trim();
			}
			if (DST3.trim().equals("")) {
				DST3 = tools.commarea.District.trim();
			}
			if (DST4.trim().equals("")) {
				DST4 = tools.commarea.District.trim();
			}

			String ACCT1 = screen.getField("ACCOUNT_CODE1I1").getValue();
			String ACCT2 = screen.getField("ACCOUNT_CODE1I2").getValue();
			String ACCT3 = screen.getField("ACCOUNT_CODE1I3").getValue();
			String ACCT4 = screen.getField("ACCOUNT_CODE1I4").getValue();
			String qyrSetting = "";
			def qry;
			def qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST1+"' and ACCOUNT_CODE = '"+ACCT1+"' and ACCOUNT_IND = '3'");
			if (!qry1.equals(null)){
				qyrSetting = "select substr(SUB_SYS_INDS,8,1) JN_USED from msf940 where  DSTRCT_CODE = '"+DST1+"' and GL_CODE = '"+ACCT1+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.JN_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("ACCOUNT_CODE1I1");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !","99999",
								"ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}else {
					MsoField errField = new MsoField();
					errField.setName("ACCOUNT_CODE1I1");
					errField.setCurrentCursor(true);
					screen.setErrorMessage(new MsoErrorMessage("SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !","99999",
							"SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !",
							MsoErrorMessage.ERR_TYPE_ERROR,
							MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
					screen.setCurrentCursorField(errField)
					return screen;
				}
			}
			
			qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST2+"' and ACCOUNT_CODE = '"+ACCT2+"' and ACCOUNT_IND = '3'");
			if (!qry1.equals(null)){
				qyrSetting = "select substr(SUB_SYS_INDS,8,1) JN_USED from msf940 where  DSTRCT_CODE = '"+DST2+"' and GL_CODE = '"+ACCT2+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.JN_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("ACCOUNT_CODE1I2");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !","99999",
								"ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}else {
					MsoField errField = new MsoField();
					errField.setName("ACCOUNT_CODE1I2");
					errField.setCurrentCursor(true);
					screen.setErrorMessage(new MsoErrorMessage("SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !","99999",
							"SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !",
							MsoErrorMessage.ERR_TYPE_ERROR,
							MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
					screen.setCurrentCursorField(errField)
					return screen;
				}
			}
			
			qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST3+"' and ACCOUNT_CODE = '"+ACCT3+"' and ACCOUNT_IND = '3'");
			if (!qry1.equals(null)){
				qyrSetting = "select substr(SUB_SYS_INDS,8,1) JN_USED from msf940 where  DSTRCT_CODE = '"+DST3+"' and GL_CODE = '"+ACCT3+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.JN_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("ACCOUNT_CODE1I3");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !","99999",
								"ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}else {
					MsoField errField = new MsoField();
					errField.setName("ACCOUNT_CODE1I3");
					errField.setCurrentCursor(true);
					screen.setErrorMessage(new MsoErrorMessage("SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !","99999",
							"SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !",
							MsoErrorMessage.ERR_TYPE_ERROR,
							MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
					screen.setCurrentCursorField(errField)
					return screen;
				}
			}
			
			qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST4+"' and ACCOUNT_CODE = '"+ACCT4+"' and ACCOUNT_IND = '3'");
			if (!qry1.equals(null)){
				qyrSetting = "select substr(SUB_SYS_INDS,8,1) JN_USED from msf940 where  DSTRCT_CODE = '"+DST4+"' and GL_CODE = '"+ACCT4+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.JN_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("ACCOUNT_CODE1I4");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !","99999",
								"ACCOUNT CODE NOT ALLOWED FOR THIS TRANSACTION !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}else {
					MsoField errField = new MsoField();
					errField.setName("ACCOUNT_CODE1I4");
					errField.setCurrentCursor(true);
					screen.setErrorMessage(new MsoErrorMessage("SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !","99999",
							"SUB SYSTEM SETTING FOR ACCOUNT CODE NOT FOUND !",
							MsoErrorMessage.ERR_TYPE_ERROR,
							MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
					screen.setCurrentCursorField(errField)
					return screen;
				}
			}
		}

		return null;
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM906A logging.version: ${hookVersion}");

		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
