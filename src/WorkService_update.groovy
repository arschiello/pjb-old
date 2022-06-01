package pjbUpgradeE9.hooks
/**
 * @EMS 2014
 *
 * Revision History
 * 01-Aug 2019.............a9ra5213 Forward Fit To Ellipse 9S
 * */
 
 import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3620.instances.WorkDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.edoi.ejb.msf600.MSF600Key
import com.mincom.ellipse.edoi.ejb.msf600.MSF600Rec
import com.mincom.ellipse.edoi.ejb.msf690.MSF690Key
import com.mincom.ellipse.edoi.ejb.msf690.MSF690Rec
 
import com.mincom.ellipse.script.util.*
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.types.m0000.instances.AccountCode
import com.mincom.ellipse.types.m0000.instances.JobCode;

 class WorkService_update extends ServiceHook{
 
	 private EDOIWrapper edoi;
	 String hookVersion = "1.4"
	 String strCrDT = "";
	 String strCrTM = "";
	 String StrDT = "";
	 String StrMT = "";
	 String StrYR = "";
	 String StrHH = "";
	 String StrMM = "";
	 String StrSS = "";
	 
	 String Errexcpt = "";
	 InitialContext initial = new InitialContext()
	 Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	 def sql = new Sql(CAISource)
	 
	 @Override
	 public Object onPreExecute(Object dto) {

		 log.info("Hooks onPreExecute logging.version: ${hookVersion}")
		 WorkDTO c = (WorkDTO) dto
		 
		 //***FMFC START
		  log.info("Update FMFC_UPDATE : " + hookVersion )
		  log.info("District Code : " + tools.commarea.District)
		  log.info("WO NO : " + c.getWorkOrder().getValue())
		  String strWO = c.getWorkOrder().getValue();
		  String strPRE = "";
		  JobCode j1 = new JobCode();
		  j1.setValue("");
		  JobCode j2 = new JobCode();
		  j2.setValue("");
		  JobCode j3 = new JobCode();
		  j3.setValue("");
		  c.setJobCode1(j1)
		  c.setJobCode2(j2)
		  c.setJobCode3(j3)
		  //Update OLD Job Codes
		  String qryUpd620 = "update msf620 set WO_JOB_CODEX1 = ' ',WO_JOB_CODEX2 = ' ',WO_JOB_CODEX3 = ' ' where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = trim('"+strPRE+strWO+"')";
		  //log.info ("String QueryDelOldJobCode : " + qryUpd620);
		  sql.execute(qryUpd620);
		  
		  String QueryDel = "delete msf071 where ENTITY_TYPE = 'SSS' and ENTITY_VALUE like '"+tools.commarea.District+strPRE+strWO+"%'";
		  //log.info ("String Query : " + QueryDel);
		  sql.execute(QueryDel);
		  /*
		  try{
			  def QueryRes1 = sql.execute(QueryDel);
			  log.info ("QueryRes1 : " + QueryRes1);
			  def qryUpd620Res = sql.execute(qryUpd620);
		  } catch (Exception  e) {
			  println "Exception is " + e
		  }
		  */
		  List<Attribute> custAttribs = c.getCustomAttributes()
		  String PARAM1 = "";
		  String PARAM2 = "";
		  String PARAM3 = "";
		  String PARAM4 = "";
		  
		  Boolean CEK_FLD_ACT0 = false;
		  Boolean CEK_FLD_ACT1 = false;
		  Boolean CEK_FLD_ACT2 = false;
		  Boolean CEK_FLD_OTH0 = false;
		  Boolean CEK_FLD_OTH1 = false;
		  Boolean CEK_FLD_OTH2 = false;
		  Integer CtrPALN1 = 0;
		  Integer CtrPALN2 = 0;
		  
		  custAttribs.each{Attribute customAttribute ->
			  if (customAttribute.getName().equals(new String("w0Plan"))){
				  log.info ("Value W0 : " + customAttribute.getValue());
				  log.info ("getRequestId : " + c.getRequestId().getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  PARAM1 = customAttribute.getValue().trim();
						  CtrPALN1 = 1
					  }
				  }else {
					  PARAM1 = "";
				  }
			  }
			  if (customAttribute.getName().equals(new String("w1Plan"))){
				  log.info ("Value W1 : " + customAttribute.getValue());
				  log.info ("getRequestId : " + c.getRequestId().getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  PARAM2 = customAttribute.getValue().trim();
						  CtrPALN2 = 1
					  }
				  }else {
					  PARAM2 = "";
				  }
			  }
			  if (customAttribute.getName().equals(new String("w0Act"))){
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  PARAM3 = customAttribute.getValue().trim();
						  CEK_FLD_ACT0 = true;
					  }
				  }else{
					  PARAM3 = "";
					  CEK_FLD_ACT0 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("w1Act"))){
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  PARAM4 = customAttribute.getValue().trim();
						  CEK_FLD_ACT1 = true;
					  }
				  }else{
					  PARAM4 = "";
					  CEK_FLD_ACT1 = false;
				  }
			  }
			  //ADD Function to check ACTUAL / OTHER VALUE
			  if (customAttribute.getName().equals(new String("w2Act"))){
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  CEK_FLD_ACT2 = true;
					  }
				  }else{
					  CEK_FLD_ACT2 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField0"))){
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  CEK_FLD_OTH0 = true;
					  }
				  }else{
					  CEK_FLD_OTH0 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField1"))){
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  CEK_FLD_OTH1 = true;
					  }
				  }else{
					  CEK_FLD_OTH1 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField2"))){
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  CEK_FLD_OTH2 = true;
					  }
				  }else{
					  CEK_FLD_OTH2 = false;
				  }
			  }
		  }
		  
		  if (!c.getRequestId().getValue().equals(null)){
			  if (!c.getRequestId().getValue().trim().equals("") && CtrPALN1.equals(0)){
				  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "INPUT REQUIRED", "w0Plan", 0, 0))
					  return dto
			  }
			  if (!c.getRequestId().getValue().trim().equals("") && CtrPALN2.equals(0)){
				  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "INPUT REQUIRED", "w1Plan", 0, 0))
					  return dto
			  }
		  }
		  if (CEK_FLD_OTH0.equals(true) && (CEK_FLD_ACT0.equals(true) || CEK_FLD_ACT1.equals(true) || CEK_FLD_ACT2.equals(true))){
			  throw new EnterpriseServiceOperationException(
				  new ErrorMessageDTO(
				  "9999", "INPUT NOT REQUIRED FOR ACTUAL (FAIL. MODE / FAIL. CAUSE / CORR. ACTION)", "", 0, 0))
				  return dto
		  }
		  if (CEK_FLD_OTH1.equals(true) && (CEK_FLD_ACT1.equals(true) || CEK_FLD_ACT2.equals(true))){
			  throw new EnterpriseServiceOperationException(
				  new ErrorMessageDTO(
				  "9999", "INPUT NOT REQUIRED FOR ACTUAL (FAIL. CAUSE / CORR. ACTION)", "", 0, 0))
				  return dto
		  }
		  log.info ("CEK_FLD_OTH0 : " + CEK_FLD_OTH0);
		  log.info ("CEK_FLD_ACT0 : " + CEK_FLD_ACT0);
		  if ((CEK_FLD_OTH0.equals(true) && CEK_FLD_ACT0.equals(true)) ||
			  (CEK_FLD_OTH1.equals(true) && CEK_FLD_ACT1.equals(true)) ||
			  (CEK_FLD_OTH2.equals(true) && CEK_FLD_ACT2.equals(true))){
			  throw new EnterpriseServiceOperationException(
				  new ErrorMessageDTO(
				  "9999", "COULD NOT INPUT BOTH ACTUAL AND OTHER", "", 0, 0))
				  return dto
		  }
		  String strEqpNo2 = "";
		  def QueryRes1_C = sql.firstRow("select trim(EQUIP_NO) EQUIP_NO from msf620 " +
						  "where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = '"+strPRE+strWO+"'");
		  log.info ("QueryRes1_C : " + QueryRes1_C);
		  if (!QueryRes1_C.equals(null)){
			  strEqpNo2 = QueryRes1_C.EQUIP_NO
		  }
		  def SQL_CEK_MSSS = sql.firstRow("select a.eqp " +
						  "from EMV6A1 a " +
						  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
						  "where trim(b.EQUIP_NO) = '"+strEqpNo2+"'");
		  log.info ("SQL_CEK_MSSS : " + SQL_CEK_MSSS);
		  Boolean MSSS_FLAG = false;
		  if (!SQL_CEK_MSSS.equals(null)){
			  if(!SQL_CEK_MSSS.eqp.equals(null)){
				  MSSS_FLAG = true;
			  }else{
				  MSSS_FLAG = false;
			  }
		  }else{
			  MSSS_FLAG = false;
		  }
		  custAttribs.each{Attribute customAttribute ->
			  if (customAttribute.getName().equals(new String("w0Plan"))){
				  log.info ("w0Plan : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  if (MSSS_FLAG.equals(true)){
							  String strEqpNo = "";
							  def QueryRes1_A = sql.firstRow("select trim(EQUIP_NO) EQUIP_NO from msf620 " +
											  "where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = '"+strPRE+strWO+"'");
							  log.info ("QueryRes1_A : " + QueryRes1_A);
							  if (!QueryRes1_A.equals(null)){
								  strEqpNo = QueryRes1_A.EQUIP_NO
								  def QueryRes1_B = sql.firstRow("select a.FAILURE_MODE " +
										  "from EMV6A1 a " +
										  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
										  "where trim(b.EQUIP_NO) = '"+strEqpNo.trim()+"' and trim(a.FAILURE_MODE) = '"+customAttribute.getValue().trim()+"'");
								   log.info ("QueryRes1_B : " + QueryRes1_B);
								   if (QueryRes1_B.equals(null)){
									   throw new EnterpriseServiceOperationException(
										   new ErrorMessageDTO(
										   "9999", "INVALID FAILURE MODE", "w0Plan", 0, 0))
										   return dto
								   }
							  }
						  }
						  
						  def QueryRes1_B = sql.firstRow("select distinct a.TABLE_CODE " +
								  "from EMV6A2 a " +
								  "where a.TABLE_TYPE = 'W0' and trim(a.TABLE_CODE) = '"+customAttribute.getValue().trim()+"'");
						  log.info ("QueryRes1_B : " + QueryRes1_B);
						  if (QueryRes1_B.equals(null)){
							  throw new EnterpriseServiceOperationException(
								  new ErrorMessageDTO(
								  "9999", "INVALID FAILURE MODE", "w0Plan", 0, 0))
								  return dto
						  }
						  
						  String strPLAN = customAttribute.getValue();
						  String QueryInsert;
						  if (strPLAN.equals(null)){
							  strPLAN = " "
						  }
						  GetNowDateTime();
						  QueryInsert = (
								  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"PLAN"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
						  try{
							  log.info("QueryInsert = : " + QueryInsert);
							  def QueryRes1 = sql.execute(QueryInsert,"001");
							  log.info ("QueryRes1 : " + QueryRes1);
						  }catch (Exception  e) {
							  log.info("execption Insert = : " + e);
							  Errexcpt = e
						  }
					  }
				  }
			  }
			  
			  if (customAttribute.getName().equals(new String("w1Plan"))){
				  log.info ("w1Plan : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  if (MSSS_FLAG.equals(true)){
							  String strEqpNo = "";
							  def QueryRes1_A = sql.firstRow("select trim(EQUIP_NO) EQUIP_NO from msf620 " +
											  "where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = '"+strPRE+strWO+"'");
							  log.info ("QueryRes1_A : " + QueryRes1_A);
							  if (!QueryRes1_A.equals(null)){
								  strEqpNo = QueryRes1_A.EQUIP_NO
								  def QueryRes1_B = sql.firstRow("select a.FAILURE_MODE " +
										  "from EMV6A1 a " +
										  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
										  "where trim(b.EQUIP_NO) = '"+strEqpNo.trim()+"' and trim(a.FAILURE_MODE) = '"+PARAM1+"' and trim(a.FAILURE_CODE) = '"+customAttribute.getValue().trim()+"'");
								   log.info ("QueryRes1_B : " + QueryRes1_B);
								   log.info ("select a.FAILURE_MODE " +
										  "from EMV6A1 a " +
										  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
										  "where trim(b.EQUIP_NO) = '"+strEqpNo.trim()+"' and trim(a.FAILURE_MODE) = '"+PARAM1+"' and trim(a.FAILURE_CODE) = '"+customAttribute.getValue().trim()+"'");
								   if (QueryRes1_B.equals(null)){
									   throw new EnterpriseServiceOperationException(
										   new ErrorMessageDTO(
										   "9999", "INVALID FAILURE CAUSE", "w1Plan", 0, 0))
										   return dto
								   }
							  }
						  }
						  
						  def QueryRes1_B = sql.firstRow("select distinct a.TABLE_CODE " +
								  "from EMV6A2 a " +
								  "where a.TABLE_TYPE = 'W1' and trim(a.TABLE_CODE) = '"+customAttribute.getValue().trim()+"'");
						  log.info ("QueryRes1_B : " + QueryRes1_B);
						  if (QueryRes1_B.equals(null)){
							  throw new EnterpriseServiceOperationException(
								  new ErrorMessageDTO(
								  "9999", "INVALID FAILURE CAUSE", "w1Plan", 0, 0))
								  return dto
						  }
						  
						  String strPLAN = customAttribute.getValue();
						  String QueryInsert;
						  if (strPLAN.equals(null)){
							  strPLAN = " "
						  }
						  GetNowDateTime();
						  QueryInsert = (
								  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"PLAN"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
						  try{
							  log.info("QueryInsert = : " + QueryInsert);
							  def QueryRes1 = sql.execute(QueryInsert,"002");
							  log.info ("QueryRes1 : " + QueryRes1);
						  }catch (Exception  e) {
							  log.info("execption Insert = : " + e);
							  Errexcpt = e
						  }
					  }
				  }
			  }
			  
			  if (customAttribute.getName().equals(new String("w2Plan"))){
				  log.info ("w2Plan : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  if (MSSS_FLAG.equals(true)){
							  String strEqpNo = "";
							  def QueryRes1_A = sql.firstRow("select trim(EQUIP_NO) EQUIP_NO from msf620 " +
											  "where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = '"+strPRE+strWO+"'");
							  log.info ("QueryRes1_A : " + QueryRes1_A);
							  if (!QueryRes1_A.equals(null)){
								  strEqpNo = QueryRes1_A.EQUIP_NO
								  def QueryRes1_B = sql.firstRow("select a.FAILURE_MODE " +
										  "from EMV6A1 a " +
										  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
										  "where trim(b.EQUIP_NO) = '"+strEqpNo.trim()+"' and trim(a.FAILURE_MODE) = '"+PARAM1+"' and trim(a.FAILURE_CODE) = '"+PARAM2+"' and trim(a.FUNCTION_CODE) = '"+customAttribute.getValue().trim()+"'");
								   log.info ("QueryRes1_B : " + QueryRes1_B);
								   if (QueryRes1_B.equals(null)){
									   throw new EnterpriseServiceOperationException(
										   new ErrorMessageDTO(
										   "9999", "INVALID ACTION TO CONFIRM", "w2Plan", 0, 0))
										   return dto
								   }
							  }
						  }
						  
						  def QueryRes1_B = sql.firstRow("select distinct a.TABLE_CODE " +
								  "from EMV6A2 a " +
								  "where a.TABLE_TYPE = 'W2' and trim(a.TABLE_CODE) = '"+customAttribute.getValue().trim()+"'");
						  log.info ("QueryRes1_B : " + QueryRes1_B);
						  if (QueryRes1_B.equals(null)){
							  throw new EnterpriseServiceOperationException(
								  new ErrorMessageDTO(
								  "9999", "INVALID ACTION TO CONFIRM", "w2Plan", 0, 0))
								  return dto
						  }
						  
						  String strPLAN = customAttribute.getValue();
						  String QueryInsert;
						  if (strPLAN.equals(null)){
							  strPLAN = " "
						  }
						  GetNowDateTime();
						  QueryInsert = (
								  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"PLAN"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
						  try{
							  log.info("QueryInsert = : " + QueryInsert);
							  def QueryRes1 = sql.execute(QueryInsert,"003");
							  log.info ("QueryRes1 : " + QueryRes1);
						  }catch (Exception  e) {
							  log.info("execption Insert = : " + e);
							  Errexcpt = e
						  }
					  }
				  }
			  }
			  
			  if (customAttribute.getName().equals(new String("w0Act"))){
				  log.info ("w0Act : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  if (MSSS_FLAG.equals(true)){
							  String strEqpNo = "";
							  def QueryRes1_A = sql.firstRow("select trim(EQUIP_NO) EQUIP_NO from msf620 " +
											  "where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = '"+strPRE+strWO+"'");
							  log.info ("QueryRes1_A : " + QueryRes1_A);
							  if (!QueryRes1_A.equals(null)){
								  strEqpNo = QueryRes1_A.EQUIP_NO
								  def QueryRes1_B = sql.firstRow("select a.FAILURE_MODE " +
										  "from EMV6A1 a " +
										  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
										  "where trim(b.EQUIP_NO) = '"+strEqpNo.trim()+"' and trim(a.FAILURE_MODE) = '"+customAttribute.getValue().trim()+"'");
								   log.info ("QueryRes1_B : " + QueryRes1_B);
								   if (QueryRes1_B.equals(null)){
									   throw new EnterpriseServiceOperationException(
										   new ErrorMessageDTO(
										   "9999", "INVALID FAILURE MODE", "w0Act", 0, 0))
										   return dto
								   }
							  }
						  }
						  
						  def QueryRes1_B = sql.firstRow("select distinct a.TABLE_CODE " +
								  "from EMV6A2 a " +
								  "where a.TABLE_TYPE = 'W0' and trim(a.TABLE_CODE) = '"+customAttribute.getValue().trim()+"'");
						  log.info ("QueryRes1_B : " + QueryRes1_B);
						  if (QueryRes1_B.equals(null)){
							  throw new EnterpriseServiceOperationException(
								  new ErrorMessageDTO(
								  "9999", "INVALID FAILURE MODE", "w0Act", 0, 0))
								  return dto
						  }
						  
						  String strPLAN = customAttribute.getValue();
						  String QueryInsert;
						  if (strPLAN.equals(null)){
							  strPLAN = " "
						  }
						  GetNowDateTime();
						  QueryInsert = (
								  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"ACT"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
						  try{
							  log.info("QueryInsert = : " + QueryInsert);
							  def QueryRes1 = sql.execute(QueryInsert,"001");
							  log.info ("QueryRes1 : " + QueryRes1);
						  }catch (Exception  e) {
							  log.info("execption Insert = : " + e);
							  Errexcpt = e
						  }
					  }
				  }
			  }
			  
			  if (customAttribute.getName().equals(new String("w1Act"))){
				  log.info ("w1Act : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  if (MSSS_FLAG.equals(true)){
							  String strEqpNo = "";
							  def QueryRes1_A = sql.firstRow("select trim(EQUIP_NO) EQUIP_NO from msf620 " +
											  "where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = '"+strPRE+strWO+"'");
							  log.info ("QueryRes1_A : " + QueryRes1_A);
							  if (!QueryRes1_A.equals(null)){
								  strEqpNo = QueryRes1_A.EQUIP_NO
								  def QueryRes1_B = sql.firstRow("select a.FAILURE_MODE " +
										  "from EMV6A1 a " +
										  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
										  "where trim(b.EQUIP_NO) = '"+strEqpNo.trim()+"' and trim(a.FAILURE_MODE) = '"+PARAM3+"' and trim(a.FAILURE_CODE) = '"+customAttribute.getValue().trim()+"'");
								   log.info ("QueryRes1_B : " + QueryRes1_B);
								   if (QueryRes1_B.equals(null)){
									   throw new EnterpriseServiceOperationException(
										   new ErrorMessageDTO(
										   "9999", "INVALID FAILURE CAUSE", "w1Act", 0, 0))
										   return dto
								   }
							  }
						  }
						  
						  def QueryRes1_B = sql.firstRow("select distinct a.TABLE_CODE " +
								  "from EMV6A2 a " +
								  "where a.TABLE_TYPE = 'W1' and trim(a.TABLE_CODE) = '"+customAttribute.getValue().trim()+"'");
						  log.info ("QueryRes1_B : " + QueryRes1_B);
						  if (QueryRes1_B.equals(null)){
							  throw new EnterpriseServiceOperationException(
								  new ErrorMessageDTO(
								  "9999", "INVALID FAILURE CAUSE", "w1Act", 0, 0))
								  return dto
						  }
						  
						  String strPLAN = customAttribute.getValue();
						  String QueryInsert;
						  if (strPLAN.equals(null)){
							  strPLAN = " "
						  }
						  GetNowDateTime();
						  QueryInsert = (
								  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"ACT"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
						  try{
							  log.info("QueryInsert = : " + QueryInsert);
							  def QueryRes1 = sql.execute(QueryInsert,"002");
							  log.info ("QueryRes1 : " + QueryRes1);
						  }catch (Exception  e) {
							  log.info("execption Insert = : " + e);
							  Errexcpt = e
						  }
					  }
				  }
			  }
			  
			  if (customAttribute.getName().equals(new String("w2Act"))){
				  log.info ("w2Act : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  if (MSSS_FLAG.equals(true)){
							  String strEqpNo = "";
							  def QueryRes1_A = sql.firstRow("select trim(EQUIP_NO) EQUIP_NO from msf620 " +
											  "where DSTRCT_CODE = '"+tools.commarea.District+"' and trim(WORK_ORDER) = '"+strPRE+strWO+"'");
							  log.info ("QueryRes1_A : " + QueryRes1_A);
							  if (!QueryRes1_A.equals(null)){
								  strEqpNo = QueryRes1_A.EQUIP_NO
								  def QueryRes1_B = sql.firstRow("select a.FAILURE_MODE " +
										  "from EMV6A1 a " +
										  "join msf600 b on (trim(a.EQP) = trim(b.EQUIP_NO) and b.DSTRCT_CODE = '"+tools.commarea.District+"') " +
										  "where trim(b.EQUIP_NO) = '"+strEqpNo.trim()+"' and trim(a.FAILURE_MODE) = '"+PARAM3+"' and trim(a.FAILURE_CODE) = '"+PARAM4+"' and trim(a.FUNCTION_CODE) = '"+customAttribute.getValue().trim()+"'");
								   log.info ("QueryRes1_B : " + QueryRes1_B);
								   if (QueryRes1_B.equals(null)){
									   throw new EnterpriseServiceOperationException(
										   new ErrorMessageDTO(
										   "9999", "INVALID CORRECTIVE ACTION", "w2Act", 0, 0))
										   return dto
								   }
							  }
						  }
						  
						  def QueryRes1_B = sql.firstRow("select distinct a.TABLE_CODE " +
								  "from EMV6A2 a " +
								  "where a.TABLE_TYPE = 'W2' and trim(a.TABLE_CODE) = '"+customAttribute.getValue().trim()+"'");
						  log.info ("QueryRes1_B : " + QueryRes1_B);
						  if (QueryRes1_B.equals(null)){
							  throw new EnterpriseServiceOperationException(
								  new ErrorMessageDTO(
								  "9999", "INVALID CORRECTIVE ACTION", "w2Act", 0, 0))
								  return dto
						  }
						  
						  String strPLAN = customAttribute.getValue();
						  String QueryInsert;
						  if (strPLAN.equals(null)){
							  strPLAN = " "
						  }
						  GetNowDateTime();
						  QueryInsert = (
								  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"ACT"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
						  try{
							  log.info("QueryInsert = : " + QueryInsert);
							  def QueryRes1 = sql.execute(QueryInsert,"003");
							  log.info ("QueryRes1 : " + QueryRes1);
						  }catch (Exception  e) {
							  log.info("execption Insert = : " + e);
							  Errexcpt = e
						  }
					  }
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField0"))){
				  log.info ("othField0 : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals(null)){
					  if (!customAttribute.getValue().equals("")){
						  String strPLAN = customAttribute.getValue();
						  String QueryInsert;
						  if (strPLAN.equals(null)){
							  strPLAN = " "
						  }
						  GetNowDateTime();
						  QueryInsert = (
								  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"OTH"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
						  try{
							  log.info("QueryInsert = : " + QueryInsert);
							  def QueryRes1 = sql.execute(QueryInsert,"001");
							  log.info ("QueryRes1 : " + QueryRes1);
						  }catch (Exception  e) {
							  log.info("execption Insert = : " + e);
							  Errexcpt = e
						  }
					  }
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField1"))){
				  log.info ("othField1 : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  String strPLAN = customAttribute.getValue();
					  String QueryInsert;
					  if (strPLAN.equals(null)){
						  strPLAN = " "
					  }
					  GetNowDateTime();
					  QueryInsert = (
							  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"OTH"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
					  try{
						  log.info("QueryInsert = : " + QueryInsert);
						  def QueryRes1 = sql.execute(QueryInsert,"002");
						  log.info ("QueryRes1 : " + QueryRes1);
					  }catch (Exception  e) {
						  log.info("execption Insert = : " + e);
						  Errexcpt = e
					  }
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField2"))){
				  log.info ("othField2 : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  String strPLAN = customAttribute.getValue();
					  String QueryInsert;
					  if (strPLAN.equals(null)){
						  strPLAN = " "
					  }
					  GetNowDateTime();
					  QueryInsert = (
							  "Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS','"+tools.commarea.District+strPRE+strWO+"OTH"+"',?,'001','" + strCrDT + "','" + strCrTM + "','" + tools.commarea.UserId + "','"+strPLAN+"','            ')");
					  try{
						  log.info("QueryInsert = : " + QueryInsert);
						  def QueryRes1 = sql.execute(QueryInsert,"003");
						  log.info ("QueryRes1 : " + QueryRes1);
					  }catch (Exception  e) {
						  log.info("execption Insert = : " + e);
						  Errexcpt = e
					  }
				  }
			  }
		  }
		  
			//***FMFC END	 
			//log.info("WorkOrderServiceCreateRequestDTO_NEW: " + c)
			String act_code = "00"
			String mt_type = "PD"
			String wo_type = "MT"
			String equip_ref = ""
			String actcode_seg3 = "00"
			
			//C-065863 - Perbaikan hooks Workorder (Tidak bisa bulk update MSEWJO)
			equip_ref = (c.getEquipmentRef().value != null) ? c.getEquipmentRef().value : c.getEquipmentNo().value
			
			MSF690Key msf690key = new MSF690Key()
			if ((!c.getStdJobNo().value.equals(null) || !c.getStdJobNo().value.equals("")) && (c.getMaintenanceType().value.equals(null) || c.getMaintenanceType().value.equals(""))) {
				Constraint c6 = MSF690Key.stdJobNo.equalTo(c.getStdJobNo().value)
				Query query4 = new QueryImpl(MSF690Rec.class).and(c6)
		  
				MSF690Rec msf690Rec = tools.edoi.firstRow(query4)
				if (msf690Rec != null){
					mt_type = msf690Rec.getMaintType()
					wo_type = msf690Rec.getWoType()
				}
			}else{
					mt_type = c.getMaintenanceType().value
					wo_type = c.getWorkOrderType().value
			}
			
			if(!c.getAccountCode().value.equals(null)){
				   actcode_seg3=c.getAccountCode().value.substring(3,5)
			}		
		
			//C-061881 Perubahan Konfigurasi WO Standing Ellipse 
			//Akan dilakukan perubahan hooks dengan konfigurasi WO type ST dan MT type PM (ST -PM). Untuk konfigurasi tersebut tidak perlu input equipment number. Selain konfigurasi diatas, equipment number wajib diinput (Diterapkan untuk semua unit eksisting)
			if(c.getDistrictCode().value.equals("UPMT") || c.getDistrictCode().value.equals("UPMK") || c.getDistrictCode().value.equals("UCRT") || c.getDistrictCode().value.equals("SPTN") || c.getDistrictCode().value.equals("SGRK") || c.getDistrictCode().value.equals("UBRS")){
				if(!actcode_seg3.equals("33")){
					if(equip_ref.equals(null)){
						if(wo_type.equals("ST")){
							if(!mt_type.equals("PM")){
								throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "Equipment Reference Required", "equipmentRef", 0, 0))
								return dto
							}
						}else{
						 throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "Equipment Reference Required", "equipmentRef", 0, 0))
									   return dto
						}
					}
				}
			}
	  
		  MSF010Key msf010key = new MSF010Key()
		  Constraint c1 = MSF010Key.tableType.equalTo("+WAC")
		  Constraint c2 = MSF010Key.tableCode.equalTo(c.getMaintenanceType().getValue())
		  log.info("nomor kode activity_12-02-2014_new1: " + c.getMaintenanceType().getValue())
		  
		  Query query1 = new QueryImpl(MSF010Rec.class).and(c1).and(c2)
		  
		  MSF010Rec msf010Rec = tools.edoi.firstRow(query1)
		  //log.info("querynya : " + msf010Rec )
		  if (msf010Rec != null){
				act_code =  msf010Rec.getTableDesc()
		 }
		 if (c.getEquipmentRef().getValue() == null)  {
			  if( c.getAccountCode().getValue() != null) {
				  if (c.getAccountCode().getValue().length() > 15) {
				  log.info("akun2" +  c.getAccountCode().getValue().substring(15))
				  AccountCode StrAccountCode = new AccountCode();
				  StrAccountCode.setValue(c.getAccountCode().getValue().substring(0, 13) + act_code + c.getAccountCode().getValue().substring(15));
				  c.setAccountCode(StrAccountCode)
				  c.getAccountCode().getValue().substring(0, 13) + act_code + c.getAccountCode().getValue().substring(15)
				  }
			  }	  
		  }
		  else {
		  if (c.getEquipmentRef().getValue().trim() != ""  ) {
		  
		  MSF600Key msf600key = new MSF600Key()
		  
		  Constraint c3 = MSF600Key.equipNo.equalTo(c.getEquipmentRef().getValue())
		  log.info("nomor equip_pindah_new1 : " + c.getEquipmentRef().getValue())
		  
		  Query query2 = new QueryImpl(MSF600Rec.class).and(c3)
		  
		  MSF600Rec msf600Rec = tools.edoi.firstRow(query2)
		  if (msf600Rec != null){
		  
		  log.info("null3")
		  String account_code =  msf600Rec.getAccountCode()
		  AccountCode StrAccountCode = new AccountCode();
		  StrAccountCode.setValue(account_code.substring(0, 13)+act_code);
		  c.setAccountCode(StrAccountCode)
		  //c.setAccountCode(account_code.substring(0, 13)+act_code)
		  account_code.substring(0, 13)+act_code
		  }else{
			Constraint c4 = MSF600Key.equipNo.greaterThan(" ")
			Constraint c5 = MSF600Rec.plantNo.equalTo(c.getEquipmentRef().getValue())
			Query query3 = new QueryImpl(MSF600Rec.class).and(c4).and(c5)
		  
				  MSF600Rec msf600Recb = tools.edoi.firstRow(query3)
					 if (msf600Recb != null){
					 String account_code =  msf600Recb.getAccountCode()
					  AccountCode StrAccountCode = new AccountCode();
					  StrAccountCode.setValue(account_code.substring(0, 13)+act_code);
					  c.setAccountCode(StrAccountCode)
					  //c.setAccountCode(account_code.substring(0, 13)+act_code)
					  log.info("set AccontCode_NEW2_12Feb_new1: " + account_code.substring(0, 13)+act_code)
					  account_code.substring(0, 13)+act_code
					}
				 }
			 }
		 }
		 
		 //Nota Dinas No. B1013082
		 String project_no = c.getProjectNo().getValue()
		 log.info("Nota Dinas No. B1013082:")
		 log.info("project_no: ${project_no}")
		 if(!project_no.equals(null) && !project_no.equals("")){
		 String segmen_2_prk = project_no.substring(2, 4)
		 log.info("segmen_2_prk: ${segmen_2_prk}")
		 
			   //Preventive
			   if(segmen_2_prk=="2K" || segmen_2_prk=="3K") {
					 if(act_code!="20"){
					  log.info("act_code 20: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			   }
			  //Predictive
			 else if(segmen_2_prk=="2L" || segmen_2_prk=="3L") {
					 if(act_code!="21"){
					  log.info("act_code 21: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //Corrective
			 else if(segmen_2_prk=="2M" || segmen_2_prk=="3M") {
					 if(act_code!="22"){
					  log.info("act_code 22: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //Overhaul
			 else if(segmen_2_prk=="2N" || segmen_2_prk=="3N") {
					 if(act_code!="24"){
					  log.info("act_code 21: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //Project
			 else if(segmen_2_prk=="2O" || segmen_2_prk=="3O") {
					 if(act_code!="26"){
					  log.info("act_code 26: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //Kimia
			 else if(segmen_2_prk=="2H" || segmen_2_prk=="3H") {
					 if(act_code!="17"){
					  log.info("act_code 17: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //K3
			 else if(segmen_2_prk=="2I" || segmen_2_prk=="3I") {
					 if(act_code!="18"){
					  log.info("act_code 18: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //Lingkungan
			 else if(segmen_2_prk=="2J" || segmen_2_prk=="3J") {
					 if(act_code!="19"){
					  log.info("act_code 19: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //Sarana
			 else if(segmen_2_prk=="2P" || segmen_2_prk=="3P") {
					 if(act_code!="60"){
					  log.info("act_code 60: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
			 //Jasa OM
			 else if(segmen_2_prk=="2G" || segmen_2_prk=="3G") {
					 if(act_code!="10" || act_code!="11"){
					  log.info("act_code 10 11: ${act_code}")
					  throw new EnterpriseServiceOperationException(
									 new ErrorMessageDTO(
									 "9999", "INCORRECT PROJECT NO", "", 0, 0))
						   return dto
					 }
			 }
		  }
		 return null
	 }
	 
	 public def GetNowDateTime() {
		 Date InPer = new Date();
		 //log.info("Hasil InPer : " + InPer.toString())
		 
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
		 //log.info("StrDT : " + StrDT )
		 
		 "(imonth + 1) untuk membuat bulan sesuai"
		 if ((imonth + 1).toString().trim().length() < 2){
			 StrMT = "0" + (imonth + 1).toString().trim()
		 }else{
			 StrMT = (imonth + 1).toString().trim()
		 }
		 //log.info("StrMT : " + StrMT )
		 if (iyear.toString().trim().length() < 3){
			 StrYR = "20" + iyear.toString().trim()
		 }else{
			 StrYR = iyear.toString().trim()
		 }
		 //log.info("StrYR : " + StrYR )
		 strCrDT = StrYR + StrMT + StrDT
		 //log.info("strCrDT : " + strCrDT )
		 
		 if (iHH.toString().trim().length() < 2){
			 StrHH = "0" + iHH.toString().trim()
		 }else{
			 StrHH = iHH.toString().trim()
		 }
		 //log.info("StrHH : " + StrHH )
		 
		 if (iMM.toString().trim().length() < 2){
			 StrMM = "0" + iMM.toString().trim()
		 }else{
			 StrMM = iMM.toString().trim()
		 }
		 //log.info("StrMM : " + StrMM )
		 
		 if (iSS.toString().trim().length() < 2){
			 StrSS = "0" + iSS.toString().trim()
		 }else{
			 StrSS = iSS.toString().trim()
		 }
		 //log.info("StrSS : " + StrSS )
		 
		 strCrTM = StrHH + StrMM + StrSS
		 //log.info("strCrTM : " + strCrTM )
	 }
 
 }