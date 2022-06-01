/**
 * @EMS May 2022
 *
 * 20220530 - a9ra5213 - Ricky Afriano - PJB UBJOM Merge
 *            Initial Coding - Hooks to validate Inventory Category in stockcode when live to PO.  
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

public class MSM24DB extends MSOHook{
	String hookVersion = "1";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	String SUPP = "";
	String INV = "";
	String District260 = "";
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM24DB logging.version: ${hookVersion}");

		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM24DB logging.version: ${hookVersion}");\

		log.info ("screen.getNextAction()  : " + screen.getNextAction().toString());
		if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {

			String DST1 = tools.commarea.District.trim();
			String DST2 = tools.commarea.District.trim();
			String DST3 = tools.commarea.District.trim();
			String DST4 = tools.commarea.District.trim();
			String DST5 = tools.commarea.District.trim();

			String STK1 = screen.getField("STOCK_CODE2I1").getValue();
			String STK2 = screen.getField("STOCK_CODE2I2").getValue();
			String STK3 = screen.getField("STOCK_CODE2I3").getValue();
			String STK4 = screen.getField("STOCK_CODE2I4").getValue();
			String STK5 = screen.getField("STOCK_CODE2I5").getValue();

			String RO1 = screen.getField("ACTIVITY_CTR2I1").getValue();
			String RO2 = screen.getField("ACTIVITY_CTR2I2").getValue();
			String RO3 = screen.getField("ACTIVITY_CTR2I3").getValue();
			String RO4 = screen.getField("ACTIVITY_CTR2I4").getValue();
			String RO5 = screen.getField("ACTIVITY_CTR2I5").getValue();

			String ACT1 = screen.getField("ACTION2I1").getValue();
			String ACT2 = screen.getField("ACTION2I2").getValue();
			String ACT3 = screen.getField("ACTION2I3").getValue();
			String ACT4 = screen.getField("ACTION2I4").getValue();
			String ACT5 = screen.getField("ACTION2I5").getValue();

			if (ACT1.trim().equals("L")) {
				String qyrSetting = "select a.ACTIVITY_CTR,a.DSTRCT_CODE,a.STOCK_CODE,a.INVENT_CAT,b.CATEG_MGT_FLG,b.CAT_COST_SW " +
						"from msf240 a left outer join msf170 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE) " +
						"where a.ACTIVITY_CTR = '"+RO1+"' and a.DSTRCT_CODE = '"+DST1+"' and a.STOCK_CODE = '"+STK1+"'";
				def qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && qry.INVENT_CAT.trim().equals("")){
						MsoField errField = new MsoField();
						errField.setName("STOCK_CODE2I1");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
								"INVENTORY CATEGORY REQUIRED !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}
			}
			if (ACT2.trim().equals("L")) {
				String qyrSetting = "select a.ACTIVITY_CTR,a.DSTRCT_CODE,a.STOCK_CODE,a.INVENT_CAT,b.CATEG_MGT_FLG,b.CAT_COST_SW " +
						"from msf240 a left outer join msf170 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE) " +
						"where a.ACTIVITY_CTR = '"+RO2+"' and a.DSTRCT_CODE = '"+DST2+"' and a.STOCK_CODE = '"+STK2+"'";
				def qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && qry.INVENT_CAT.trim().equals("")){
						MsoField errField = new MsoField();
						errField.setName("STOCK_CODE2I2");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
								"INVENTORY CATEGORY REQUIRED !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}
			}
			if (ACT3.trim().equals("L")) {
				String qyrSetting = "select a.ACTIVITY_CTR,a.DSTRCT_CODE,a.STOCK_CODE,a.INVENT_CAT,b.CATEG_MGT_FLG,b.CAT_COST_SW " +
						"from msf240 a left outer join msf170 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE) " +
						"where a.ACTIVITY_CTR = '"+RO3+"' and a.DSTRCT_CODE = '"+DST3+"' and a.STOCK_CODE = '"+STK3+"'";
				def qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && qry.INVENT_CAT.trim().equals("")){
						MsoField errField = new MsoField();
						errField.setName("STOCK_CODE2I3");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
								"INVENTORY CATEGORY REQUIRED !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}
			}
			if (ACT4.trim().equals("L")) {
				String qyrSetting = "select a.ACTIVITY_CTR,a.DSTRCT_CODE,a.STOCK_CODE,a.INVENT_CAT,b.CATEG_MGT_FLG,b.CAT_COST_SW " +
						"from msf240 a left outer join msf170 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE) " +
						"where a.ACTIVITY_CTR = '"+RO4+"' and a.DSTRCT_CODE = '"+DST4+"' and a.STOCK_CODE = '"+STK4+"'";
				def qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && qry.INVENT_CAT.trim().equals("")){
						MsoField errField = new MsoField();
						errField.setName("STOCK_CODE2I4");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
								"INVENTORY CATEGORY REQUIRED !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}
			}
			if (ACT5.trim().equals("L")) {
				String qyrSetting = "select a.ACTIVITY_CTR,a.DSTRCT_CODE,a.STOCK_CODE,a.INVENT_CAT,b.CATEG_MGT_FLG,b.CAT_COST_SW " +
						"from msf240 a left outer join msf170 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE) " +
						"where a.ACTIVITY_CTR = '"+RO5+"' and a.DSTRCT_CODE = '"+DST5+"' and a.STOCK_CODE = '"+STK4+"'";
				def qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && qry.INVENT_CAT.trim().equals("")){
						MsoField errField = new MsoField();
						errField.setName("STOCK_CODE2I5");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
								"INVENTORY CATEGORY REQUIRED !",
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
		log.info("Hooks onPostSubmit MSM24DB logging.version: ${hookVersion}");

		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
