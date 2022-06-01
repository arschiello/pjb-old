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

public class MSM24BA extends MSOHook{
	String hookVersion = "1";

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	String SUPP = "";
	String INV = "";
	String District260 = "";
	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("Hooks onDisplay MSM24BA logging.version: ${hookVersion}");

		return null;
	}
	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM24BA logging.version: ${hookVersion}");\

		log.info ("screen.getNextAction()  : " + screen.getNextAction().toString());
		if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			String DST1 = tools.commarea.District.trim();
			String ACT1 = screen.getField("PROCESS_CODE1I").getValue();
			String STK1 = screen.getField("STOCK_CODE1I").getValue();
			String RO1 = screen.getField("RO_NO1I").getValue();
			String invCat = screen.getField("INVENT_CAT1I").getValue();
			def qry = sql.firstRow("select lpad('"+RO1+"',3,'0') RO_NO from dual");
			RO1 = qry.RO_NO;
			
			if (ACT1.trim().equals("L")) {
				String qyrSetting = "select a.ACTIVITY_CTR,a.DSTRCT_CODE,a.STOCK_CODE,a.INVENT_CAT,b.CATEG_MGT_FLG,b.CAT_COST_SW " +
						"from msf240 a left outer join msf170 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE) " +
						"where a.ACTIVITY_CTR = '"+RO1+"' and a.DSTRCT_CODE = '"+DST1+"' and a.STOCK_CODE = '"+STK1+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && (qry.INVENT_CAT.trim().equals("") || invCat.trim().equals(""))){
						MsoField errField = new MsoField();
						errField.setName("INVENT_CAT1I");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
								"INVENTORY CATEGORY REQUIRED !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}else {
					qyrSetting = "select * from msf170 b where b.DSTRCT_CODE = '"+DST1+"' and b.STOCK_CODE = '"+STK1+"'"
					qry = sql.firstRow(qyrSetting);
					if (!qry.equals(null)){
						if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && (invCat.trim().equals(""))){
							MsoField errField = new MsoField();
							errField.setName("INVENT_CAT1I");
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
	}
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM24BA logging.version: ${hookVersion}");

		return result
	}
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	}
}
