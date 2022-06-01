package pjbUpgradeE9.hooks
/**
 * @EMS 2014
 *
 * Revision History
 * 04 Nov 2018.............a9ra5213 Add FMFC Customisation
 * 12 Nov 2016.............19ep6434 Change substring to fetch account code msf600
 * 12-Feb-2014.............a9ms6435 Initial Non User Exit-
 * ........................calculate date in Lead Time Field
 * */
 
 import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateRequestDTO
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.edoi.ejb.msf600.MSF600Key
import com.mincom.ellipse.edoi.ejb.msf600.MSF600Rec
import com.mincom.ellipse.edoi.ejb.msf690.MSF690Key
import com.mincom.ellipse.edoi.ejb.msf690.MSF690Rec
import com.mincom.ellipse.script.util.*
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceUpdateEstimatesReplyDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceUpdateEstimatesRequestDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceReadRequestDTO
import com.mincom.enterpriseservice.ellipse.table.TableServiceReadReplyDTO
import com.mincom.enterpriseservice.ellipse.table.TableServiceReadRequestDTO
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
 
 
 class WorkOrderService_create extends ServiceHook{
 
	 private EDOIWrapper edoi;
	 private EROIWrapper eroi;
	 String hookVersion = "1.5"
	 String strCrDT = "";
	 String strCrTM = "";
	 String StrDT = "";
	 String StrMT = "";
	 String StrYR = "";
	 String StrHH = "";
	 String StrMM = "";
	 String StrSS = "";
	 String Errexcpt = "";
	 private CommAreaScriptWrapper commarea
	 InitialContext initial = new InitialContext()
	 Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	 def sql = new Sql(CAISource)
	 
	 @Override
	 public Object onPreExecute(Object dto) {	 
		 
	   log.info("Hooks onPreExecute logging.version: ${hookVersion}")
	   String act_code = "00"
	   String mt_type = "PD"
	   String actcode_seg3 = "00"
		 WorkOrderServiceCreateRequestDTO c = (WorkOrderServiceCreateRequestDTO) dto
		 log.info("WorkOrderServiceCreateRequestDTO_NEW: " + c)
		 log.info("standard job : " + c.getStdJobNo())
		 
		 MSF690Key msf690key = new MSF690Key()
		 if (c.getStdJobNo() != null && c.getMaintenanceType() == null) {
			Constraint c6 = MSF690Key.stdJobNo.equalTo(c.getStdJobNo())
			Query query4 = new QueryImpl(MSF690Rec.class).and(c6)
		 
			MSF690Rec msf690Rec = tools.edoi.firstRow(query4)
			if (msf690Rec != null){
			   log.info("690 s")
			   mt_type  =  msf690Rec.getMaintType()   
			 }
		 }
		  
		  if (c.getRiskCode10() != null)
		  {
			  mt_type = c.getRiskCode10()
			  c.setMaintenanceType(mt_type)
		  }
		  else{
			   mt_type = c.getMaintenanceType()
		  }
 
		 MSF010Key msf010key = new MSF010Key()
	 
		 Constraint c1 = MSF010Key.tableType.equalTo("+WAC")
		 Constraint c2 = MSF010Key.tableCode.equalTo(mt_type)
		 log.info("nomor kode activity_12-02-2014_new1: " + mt_type)
		 
		 Query query1 = new QueryImpl(MSF010Rec.class).and(c1).and(c2)
		 
		 
		 MSF010Rec msf010Rec = tools.edoi.firstRow(query1)
		 if (msf010Rec != null){
		 act_code =  msf010Rec.getTableDesc()
		 }
		 log.info("nomor equip_pindah_new1 : " + c.getEquipmentRef())
		 log.info("nomor equip_pindah_new2 : " + c.getEquipmentNo())
		 if (c.getEquipmentRef() == null && c.getAccountCode() != null) {
			 if (c.getAccountCode().length() > 15) {
			  log.info("akun2" +  c.getAccountCode().substring(15))			
			 c.setAccountCode(c.getAccountCode().substring(0, 13) + act_code + c.getAccountCode().substring(15))	 
			 c.getAccountCode().substring(0, 13) + act_code + c.getAccountCode().substring(15)	 
			 }
			 else {
			 c.setAccountCode(c.getAccountCode()) 
			}
			}
		  else {
		 
		 Constraint c3
		 MSF600Key msf600key = new MSF600Key()
		 if (c.getEquipmentRef() != null) {
		  c3 = MSF600Key.equipNo.equalTo(c.getEquipmentRef())
		 } else {
		  c3 = MSF600Key.equipNo.equalTo(c.getEquipmentNo())
 	    }
		 
		 Query query2 = new QueryImpl(MSF600Rec.class).and(c3)
		 
		 MSF600Rec msf600Rec = tools.edoi.firstRow(query2)
		 if (msf600Rec != null){
		 
		 log.info("null1")
		 String account_code =  msf600Rec.getAccountCode()
		 log.info("Cost Centre_new1: ${account_code}")
		 c.setAccountCode(account_code.substring(0, 13)+act_code)
		 log.info("set AccontCode_NEW2_12Feb_new1: " + account_code.substring(0, 13)+act_code)
						 
		 } else {
					 Constraint c4 = MSF600Key.equipNo.greaterThan(" ")
					 Constraint c5 = MSF600Rec.plantNo.equalTo(c.getEquipmentRef())
					 Query query3 = new QueryImpl(MSF600Rec.class).and(c4).and(c5)
 
						
				 MSF600Rec msf600Recb = tools.edoi.firstRow(query3)
					if (msf600Recb != null){
		 
					  log.info("null2")
		 
				  String account_code =  msf600Recb.getAccountCode()
				  log.info("Cost Centre_new1: ${account_code}")
				  c.setAccountCode(account_code.substring(0, 13)+act_code)
				  log.info("set AccontCode_NEW2_12Feb_new1: " + account_code.substring(0, 13)+act_code)
						 
					   }
				}
		  }
		 return null  
	 }
	 
	 public Object onPostExecute(Object input, Object result) {
		 log.info("Hooks onPostExecute WorkOrderService_create version: ${hookVersion}")
		 //FMFC-START
		 WorkOrderServiceCreateReplyDTO c = (WorkOrderServiceCreateReplyDTO) result;
		 String strREQ_ID = c.getRequestId();
		 String strWO = "";
		 if (c.getWorkOrder().getPrefix().equals(null)){
			 strWO = c.getWorkOrder().getNo();
		 }else{
			 strWO = c.getWorkOrder().getPrefix() + c.getWorkOrder().getNo();
		 }
		 
		 def QueryRes1 = sql.firstRow("select REF_CODE from msf071 " +
			 "where ENTITY_TYPE = 'SSS' and REF_NO = '001' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = trim('"+tools.commarea.district+strREQ_ID+"PLAN')");
		 log.info ("QueryRes1 : " + QueryRes1);
		 
		 if (!QueryRes1.equals(null)){
			 if (!QueryRes1.REF_CODE.equals(null)){
				 String StrMSSS = QueryRes1.REF_CODE;
				 GetNowDateTime();
				 String QueryInsert = (
						 "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS',?,'001','001','" + strCrDT + "','" + strCrTM + "','"+tools.commarea.userId+"','"+StrMSSS+"','            ')");
				 try{
					 log.info("QueryInsert = : " + QueryInsert);
					 def QueryRes2 = sql.execute(QueryInsert,tools.commarea.district+strWO+"PLAN");
					 log.info ("ENTITY_VALUE : " + tools.commarea.district+strWO+"PLAN");
				 }catch (Exception  e) {
					 log.info("execption Insert = : " + e);
					 Errexcpt = e
				 }
				 if (!Errexcpt.contains("invalid SQL statement") && !Errexcpt.equals("")){
					 throw new EnterpriseServiceOperationException(
						 new ErrorMessageDTO(
						 "9999", Errexcpt, "", 0, 0))
		
						 return result;
				 }
			 }
		 }
		 //FMFC-END
	 }
	 public def GetNowDateTime() {
		 Date InPer = new Date();
		 log.info("Hasil InPer : " + InPer.toString())
		 
		 Calendar cal = Calendar.getInstance();
		 cal.setTime(InPer);
		 int iyear = cal.get(Calendar.YEAR);
		 int imonth = cal.get(Calendar.MONTH);
		 int iday = cal.get(Calendar.DAY_OF_MONTH);
		 int iHH = cal.get(Calendar.HOUR);
		 int iMM = cal.get(Calendar.MINUTE);
		 int iSS = cal.get(Calendar.SECOND);
		 
		 if (iday.toString().trim().length() < 2){
			 StrDT = "0" + iday.toString().trim()
		 }else{
			 StrDT = iday.toString().trim()
		 }
		 log.info("StrDT : " + StrDT )
		 
		 "(imonth + 1) untuk membuat bulan sesuai"
		 if ((imonth + 1).toString().trim().length() < 2){
			 StrMT = "0" + (imonth + 1).toString().trim()
		 }else{
			 StrMT = (imonth + 1).toString().trim()
		 }
		 log.info("StrMT : " + StrMT )
		 if (iyear.toString().trim().length() < 3){
			 StrYR = "20" + iyear.toString().trim()
		 }else{
			 StrYR = iyear.toString().trim()
		 }
		 log.info("StrYR : " + StrYR )
		 strCrDT = StrYR + StrMT + StrDT
		 log.info("strCrDT : " + strCrDT )
		 
		 if (iHH.toString().trim().length() < 2){
			 StrHH = "0" + iHH.toString().trim()
		 }else{
			 StrHH = iHH.toString().trim()
		 }
		 log.info("StrHH : " + StrHH )
		 
		 if (iMM.toString().trim().length() < 2){
			 StrMM = "0" + iMM.toString().trim()
		 }else{
			 StrMM = iMM.toString().trim()
		 }
		 log.info("StrMM : " + StrMM )
		 
		 if (iSS.toString().trim().length() < 2){
			 StrSS = "0" + iSS.toString().trim()
		 }else{
			 StrSS = iSS.toString().trim()
		 }
		 log.info("StrSS : " + StrSS )
		 
		 strCrTM = StrHH + StrMM + StrSS
		 log.info("strCrTM : " + strCrTM )
	 }
	 
 }
 
