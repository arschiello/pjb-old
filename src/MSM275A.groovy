/**
 * @EMS 2014
 *
 * Revision History
 * 31-May-2022.............a9ra5213 - Ricky Afriano - PJB UBJOM Merge
 * ........................Hooks to Validate Account Code with MSF940.
 * 04-Des-2015.............a9ms6435 WO-689
 * ........................add condition for Tran Type CIC
 * 01-Mar-2014.............a9aa6024 Initial Non User Exit- MSM275A
 * ........................
 * */

import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.eql.impl.*
import com.mincom.eql.*
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.System

import com.mincom.ellipse.edoi.ejb.msf900.MSF900Key
import com.mincom.ellipse.edoi.ejb.msf900.MSF900Rec
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import groovy.sql.Sql;
import com.mincom.eql.impl.QueryImpl;
import javax.naming.InitialContext;

public class MSM275A extends MSOHook {
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	String hookVersion = "04-DES-15 10:10"
	String dstrctCode , description , branchCode,chequeNo    ,bankAcctNo,currentDate ,StrDay,acctDstrct ,districtCd, userno
	String javaCalendar , DumStrMonth , StrMonth ,CurrentPer ,DumStrDay, trnGrpKey,Des1,Des2,Des3,Des4
	BigDecimal actDispVal , dispAmt
	Integer i,j,k
	//String[] descLine

	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
		log.info("Hooks onPreSubmit MSM275A logging.version: ${hookVersion}");

		log.info ("screen.getNextAction()  : " + screen.getNextAction().toString());
		if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			String ACCT1 = screen.getField("GL_ACCOUNT1I1").getValue();
			String ACCT2 = screen.getField("GL_ACCOUNT1I2").getValue();
			String ACCT3 = screen.getField("GL_ACCOUNT1I3").getValue();
			String ACCT4 = screen.getField("GL_ACCOUNT1I4").getValue();

			String DST1 = screen.getField("ACCT_DSTRCT1I1").getValue();
			String DST2 = screen.getField("ACCT_DSTRCT1I2").getValue();
			String DST3 = screen.getField("ACCT_DSTRCT1I3").getValue();
			String DST4 = screen.getField("ACCT_DSTRCT1I4").getValue();
			
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
			String qyrSetting = "";
			def qry;
			def qry1 = sql.firstRow("select * from msf966 where DSTRCT_CODE = '"+DST1+"' and ACCOUNT_CODE = '"+ACCT1+"' and ACCOUNT_IND = '3'");
			if (!qry1.equals(null)){
				qyrSetting = "select substr(SUB_SYS_INDS,2,1) AP_USED from msf940 where  DSTRCT_CODE = '"+DST1+"' and GL_CODE = '"+ACCT1+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.AP_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("GL_ACCOUNT1I1");
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
					errField.setName("GL_ACCOUNT1I1");
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
				qyrSetting = "select substr(SUB_SYS_INDS,2,1) AP_USED from msf940 where  DSTRCT_CODE = '"+DST2+"' and GL_CODE = '"+ACCT2+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.AP_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("GL_ACCOUNT1I2");
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
					errField.setName("GL_ACCOUNT1I2");
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
				qyrSetting = "select substr(SUB_SYS_INDS,2,1) AP_USED from msf940 where  DSTRCT_CODE = '"+DST3+"' and GL_CODE = '"+ACCT3+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.AP_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("GL_ACCOUNT1I3");
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
					errField.setName("GL_ACCOUNT1I3");
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
				qyrSetting = "select substr(SUB_SYS_INDS,2,1) AP_USED from msf940 where  DSTRCT_CODE = '"+DST4+"' and GL_CODE = '"+ACCT4+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.AP_USED.equals("N")){
						MsoField errField = new MsoField();
						errField.setName("GL_ACCOUNT1I4");
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
					errField.setName("GL_ACCOUNT1I4");
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
	
	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit logging.version: ${hookVersion}")
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date nowdate = new Date();
		String currentDate = dateFormat.format(nowdate)
		log.info("now" + nowdate)

		log.info("Current date :" + currentDate)
		log.info("acctDstrct1 :" + acctDstrct)
		districtCd = tools.commarea.District
		Des1 = input.getField("DESCRIPTION1I1").getValue().trim()
		Des2 = input.getField("DESCRIPTION1I2").getValue().trim()
		Des3 = input.getField("DESCRIPTION1I3").getValue().trim()
		Des4 = input.getField("DESCRIPTION1I4").getValue().trim()
		branchCode = input.getField("BRANCH_CODE1I").getValue()
		chequeNo = input.getField("CHEQUE_NO1I").getValue()
		bankAcctNo = input.getField("BANK_ACCT_NO1I").getValue()
		if (acctDstrct == null){
			if (Des1 != "") {
				acctDstrct = input.getField("ACCT_DSTRCT1I1").getValue()
			} else if (Des2 != "") {
				acctDstrct = input.getField("ACCT_DSTRCT1I2").getValue()
			}else if (Des3 != "") {
				acctDstrct = input.getField("ACCT_DSTRCT1I3").getValue()
			} else {
				acctDstrct = input.getField("ACCT_DSTRCT1I4").getValue()
			}
		}
		log.info("acctDstrct2 :" + acctDstrct)
		log.info("districtCd :" + districtCd)
		dstrctCode = districtCd
		if (acctDstrct != districtCd && acctDstrct != ""  ){
			dstrctCode = acctDstrct
		}
		log.info("dstrctCode :" + dstrctCode)
		//if (input.getField("EQUIP_REF1I").isProtected() == true){
		Constraint key900_1 = MSF900Key.dstrctCode.equalTo(districtCd)
		Constraint key900_2 = MSF900Key.processDate.equalTo(currentDate)
		Constraint key900_3 = MSF900Key.rec900Type.equalTo("C")
		Constraint key900_4 = MSF900Key.transactionNo.greaterThan(" ")
		Constraint key900_5 = MSF900Key.userno.greaterThan(" ")
		Constraint rec900_6 = MSF900Rec.branchCode.equalTo(branchCode)
		Constraint rec900_7 = MSF900Rec.chequeNo.equalTo(chequeNo)
		Constraint rec900_8 = MSF900Rec.bankAcctNo.equalTo(bankAcctNo)

		//begin WO-689 a9ms6435
		Constraint rec900_9 = MSF900Rec.tranType.equalTo("CIC")
		//Constraint rec900_9 = MSF900Rec.tranType.equalTo("CHQ")

		//end WO-689


		QueryImpl query900_1 = new QueryImpl(MSF900Rec.class).and(key900_1).and(key900_2).and(key900_3).and(key900_4).and(key900_5).and(rec900_6).and(rec900_7).and(rec900_8).and(rec900_9).orderByDescending(MSF900Rec.msf900Key)

		MSF900Rec msf900rec = tools.edoi.firstRow(query900_1)
		if (msf900rec != null){
			log.info("MASUK  -1")
			MSF900Key msf900keya = new MSF900Key()
			msf900keya = msf900rec.getPrimaryKey()
			trnGrpKey = msf900rec.getTranGroupKey()
			userno = msf900keya.getUserno()
			log.info("trnGrpKey :" +  trnGrpKey )
			log.info("userno :" + userno )

			Constraint key900_21 = MSF900Key.dstrctCode.greaterThan(" ")
			Constraint key900_22 = MSF900Key.processDate.equalTo(currentDate)
			Constraint key900_23 = MSF900Key.rec900Type.equalTo("C")
			Constraint key900_24 = MSF900Key.transactionNo.greaterThan(" ")
			Constraint key900_25 = MSF900Key.userno.equalTo(userno)
			Constraint rec900_26 = MSF900Rec.branchCode.equalTo(branchCode)
			Constraint rec900_27 = MSF900Rec.chequeNo.equalTo(chequeNo)
			Constraint rec900_28 = MSF900Rec.bankAcctNo.equalTo(bankAcctNo)
			Constraint rec900_29 = MSF900Rec.tranType.equalTo("CHG")
			Constraint rec900_30 = MSF900Rec.tranGroupKey.equalTo(trnGrpKey)

			QueryImpl query900_3 = new QueryImpl(MSF900Rec.class).and(key900_21).and(key900_22).
					and(key900_23).and(key900_24).and(key900_25).and(rec900_26).and(rec900_27).
					and(rec900_28).and(rec900_29).and(rec900_30)
			i = 0
			//descLine=new String[20]
			//MSF900Rec msf900rec_3 = tools.edoi.firstRow(query900_3)
			List<String> descLine = new ArrayList<String>();
			tools.edoi.search(query900_3,{MSF900Rec msf900rec_3 ->
				if (msf900rec_3 != null){
					//    	descLine[i] = msf900rec_3.getDescLine()
					descLine.add(msf900rec_3.getDescLine());
					log.info("i : "+ i)
					log.info("descripti line :" + descLine.get(i) )

				}
				i++
			})
			Constraint key900_31 = MSF900Key.dstrctCode.greaterThan(" ")
			Constraint key900_32 = MSF900Key.processDate.equalTo(currentDate)
			Constraint key900_33 = MSF900Key.rec900Type.equalTo("C")
			Constraint key900_34 = MSF900Key.transactionNo.greaterThan(" ")
			Constraint key900_35 = MSF900Key.userno.equalTo(userno)
			Constraint rec900_36 = MSF900Rec.branchCode.equalTo(branchCode)
			Constraint rec900_37 = MSF900Rec.chequeNo.equalTo(chequeNo)
			Constraint rec900_38 = MSF900Rec.bankAcctNo.equalTo(bankAcctNo)
			Constraint rec900_39 = MSF900Rec.tranType.equalTo("CHQ")
			Constraint rec900_40 = MSF900Rec.tranGroupKey.equalTo(trnGrpKey)

			QueryImpl query900_4 = new QueryImpl(MSF900Rec.class).and(key900_31).and(key900_32).
					and(key900_33).and(key900_34).and(key900_35).and(rec900_36).and(rec900_37).
					and(rec900_38).and(rec900_39).and(rec900_40)
			j = i -1
			tools.edoi.search(query900_4,{MSF900Rec msf900rec_4 ->
				if (msf900rec_4 != null){
					MSF900Rec msf900recb = new MSF900Rec()
					msf900recb = msf900rec_4
					msf900recb.setDescLine(descLine.get(j))
					tools.edoi.update(msf900recb)
					log.info("j : "+ j)
					log.info("Record post MSF900 CHQ updated 4 DES:"  + districtCd + " " +descLine.get(j))
				}
				//	j++
			})
			Constraint key900_41 = MSF900Key.dstrctCode.greaterThan(" ")
			Constraint key900_42 = MSF900Key.processDate.equalTo(currentDate)
			Constraint key900_43 = MSF900Key.rec900Type.equalTo("C")
			Constraint key900_44 = MSF900Key.transactionNo.greaterThan(" ")
			Constraint key900_45 = MSF900Key.userno.equalTo(userno)
			Constraint rec900_46 = MSF900Rec.branchCode.equalTo(branchCode)
			Constraint rec900_47 = MSF900Rec.chequeNo.equalTo(chequeNo)
			Constraint rec900_48 = MSF900Rec.bankAcctNo.equalTo(bankAcctNo)
			Constraint rec900_49 = MSF900Rec.tranType.equalTo("CIC")
			Constraint rec900_50 = MSF900Rec.tranGroupKey.equalTo(trnGrpKey)

			QueryImpl query900_5 = new QueryImpl(MSF900Rec.class).and(key900_41).and(key900_42).and(key900_43).
					and(key900_44).and(key900_45).and(rec900_46).and(rec900_47).and(rec900_48).and(rec900_49).and(rec900_50)
			k = 0
			tools.edoi.search(query900_5,{MSF900Rec msf900rec_5 ->
				if (msf900rec_5 != null){
					MSF900Rec msf900recc = new MSF900Rec()
					msf900recc = msf900rec_5
					msf900recc.setDescLine(descLine.get(k))
					tools.edoi.update(msf900recc)
					log.info("k : "+ k)
					log.info("Record post MSF900 CIC updated 4 DES:"  + dstrctCode + " " +descLine.get(k))
				}
				k++
				if (k == i){
					k = 0
				}
			})
			descLine.clear()
		}
	}
	//return result
}
