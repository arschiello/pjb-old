/**
 * @EMS May 2022
 *
 * 20220530 - a9ra5213 - Ricky Afriano - PJB UBJOM Merge
 *            Initial Coding - Hooks to validate Account Code.  
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
import com.mincom.ellipse.ejra.mso.MsoField;

public class MSM265A extends MSOHook{
	String hookVersion = "1";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	String SUPP = "";
	String INV = "";
	String District260 = "";
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM265A logging.version: ${hookVersion}");

		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM265A logging.version: ${hookVersion}");

		log.info ("screen.getNextAction()  : " + screen.getNextAction().toString());
		if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			String DST1 = screen.getField("ACCT_DSTRCT1I1").getValue();
			String DST2 = screen.getField("ACCT_DSTRCT1I2").getValue();
			String DST3 = screen.getField("ACCT_DSTRCT1I3").getValue();

			if (DST1.trim().equals("")) {
				DST1 = tools.commarea.District.trim();
			}
			if (DST2.trim().equals("")) {
				DST2 = tools.commarea.District.trim();
			}
			if (DST3.trim().equals("")) {
				DST3 = tools.commarea.District.trim();
			}

			String ACCT1 = screen.getField("ACCOUNT1I1").getValue();
			String ACCT2 = screen.getField("ACCOUNT1I2").getValue();
			String ACCT3 = screen.getField("ACCOUNT1I3").getValue();

			String VALUE1 = screen.getField("INV_ITEM_VALUE1I1").getValue();
			String VALUE2 = screen.getField("INV_ITEM_VALUE1I2").getValue();
			String VALUE3 = screen.getField("INV_ITEM_VALUE1I3").getValue();

			if (!VALUE1.trim().equals("") && ACCT1.trim().equals("")) {
				MsoField errField = new MsoField();
				errField.setName("ACCOUNT1I1");
				errField.setCurrentCursor(true);
				screen.setErrorMessage(new MsoErrorMessage("ACCOUNT CODE REQUIRED !","99999",
						"ACCOUNT CODE REQUIRED !",
						MsoErrorMessage.ERR_TYPE_ERROR,
						MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
				screen.setCurrentCursorField(errField)
				return screen;
			}else {
				def qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST1+"' and ACCOUNT_CODE = '"+ACCT1+"' and ACCOUNT_IND = '3'");
				if (!qry1.equals(null)){
					String qyrSetting = "select substr(SUB_SYS_INDS,2,1) AP_USED from msf940 where  DSTRCT_CODE = '"+DST1+"' and GL_CODE = '"+ACCT1+"'";
					log.info ("qyrSetting  : " + qyrSetting);
					def qry = sql.firstRow(qyrSetting);
					if (!qry.equals(null)){
						if (qry.AP_USED.equals("N")){
							MsoField errField = new MsoField();
							errField.setName("ACCOUNT1I1");
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
						errField.setName("ACCOUNT1I1");
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

			if (!VALUE2.trim().equals("") && ACCT2.trim().equals("")) {
				MsoField errField = new MsoField();
				errField.setName("ACCOUNT1I2");
				errField.setCurrentCursor(true);
				screen.setErrorMessage(new MsoErrorMessage("ACCOUNT CODE REQUIRED !","99999",
						"ACCOUNT CODE REQUIRED !",
						MsoErrorMessage.ERR_TYPE_ERROR,
						MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
				screen.setCurrentCursorField(errField)
				return screen;
			}else {
				def qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST2+"' and ACCOUNT_CODE = '"+ACCT2+"' and ACCOUNT_IND = '3'");
				if (!qry1.equals(null)){
					String qyrSetting = "select substr(SUB_SYS_INDS,2,1) AP_USED from msf940 where  DSTRCT_CODE = '"+DST2+"' and GL_CODE = '"+ACCT2+"'";
					def qry = sql.firstRow(qyrSetting);
					if (!qry.equals(null)){
						if (qry.AP_USED.equals("N")){
							MsoField errField = new MsoField();
							errField.setName("ACCOUNT1I2");
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
						errField.setName("ACCOUNT1I2");
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

			if (!VALUE3.trim().equals("") && ACCT3.trim().equals("")) {
				MsoField errField = new MsoField();
				errField.setName("ACCOUNT1I3");
				errField.setCurrentCursor(true);
				screen.setErrorMessage(new MsoErrorMessage("ACCOUNT CODE REQUIRED !","99999",
						"ACCOUNT CODE REQUIRED !",
						MsoErrorMessage.ERR_TYPE_ERROR,
						MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
				screen.setCurrentCursorField(errField)
				return screen;
			}else {
				def qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST3+"' and ACCOUNT_CODE = '"+ACCT3+"' and ACCOUNT_IND = '3'");
				if (!qry1.equals(null)){
					String qyrSetting = "select substr(SUB_SYS_INDS,2,1) AP_USED from msf940 where  DSTRCT_CODE = '"+DST3+"' and GL_CODE = '"+ACCT3+"'";
					def qry = sql.firstRow(qyrSetting);
					if (!qry.equals(null)){
						if (qry.AP_USED.equals("N")){
							MsoField errField = new MsoField();
							errField.setName("ACCOUNT1I3");
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
						errField.setName("ACCOUNT1I3");
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
		}
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM265A logging.version: ${hookVersion}");

		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
