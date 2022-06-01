package pjbUpgradeE9.hooks
/**
 * @EMS 2014
 *
 * Revision History
 * 04-nov 2018.............a9ra5213 Add FMFC Customisation
 * 12-Nov-2016.............a9ep6434 Change fetch account count from msf600
 * 12-Feb-2014.............a9ms6435 Initial Non User Exit-
 * ........................calculate date in Lead Time Field
 * */
 
 import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
 //import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateReplyDTO
 //import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateRequestDTO
 import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyRequestDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.edoi.ejb.msf600.MSF600Key
import com.mincom.ellipse.edoi.ejb.msf600.MSF600Rec
 
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
 
 
 class WorkOrderService_modify extends ServiceHook{
 
	 private EDOIWrapper edoi;
	 String hookVersion = "1.1"
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
 
		 WorkOrderServiceModifyRequestDTO c = (WorkOrderServiceModifyRequestDTO) dto
		 
		 //***FMFC START
		 /*
		  log.info("Update FMFC_UPDATE : " + hookVersion )
		  log.info("District Code : " + tools.commarea.District)
		  log.info("WO NO : " + c.getWorkOrder().getNo())
		  String strWO = c.getWorkOrder().getNo();
		  String strPRE = c.getWorkOrder().getPrefix();
		  String QueryDel = "delete msf071 where ENTITY_TYPE = 'SSS' and ENTITY_VALUE like '"+tools.commarea.District+strPRE+strWO+"%'";
		  log.info ("String Query : " + QueryDel);
		  try{
			  def QueryRes1 = sql.execute(QueryDel);
			  log.info ("QueryRes1 : " + QueryRes1);
		  } catch (Exception  e) {
			  println "Exception is " + e
		  }
		  
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
				  log.info ("getRequestId : " + c.getRequestId());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  PARAM1 = customAttribute.getValue().trim();
					  CtrPALN1 = 1
				  }
			  }
			  if (customAttribute.getName().equals(new String("w1Plan"))){
				  log.info ("Value W1 : " + customAttribute.getValue());
				  log.info ("getRequestId : " + c.getRequestId());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  PARAM2 = customAttribute.getValue().trim();
					  CtrPALN2 = 1
				  }
			  }
			  if (customAttribute.getName().equals(new String("w0Act"))){
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  PARAM3 = customAttribute.getValue().trim();
					  CEK_FLD_ACT0 = true;
				  }else{
					  CEK_FLD_ACT0 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("w1Act"))){
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  PARAM4 = customAttribute.getValue().trim();
					  CEK_FLD_ACT1 = true;
				  }else{
					  CEK_FLD_ACT1 = false;
				  }
			  }
			  //ADD Function to check ACTUAL / OTHER VALUE
			  if (customAttribute.getName().equals(new String("w2Act"))){
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  CEK_FLD_ACT2 = true;
				  }else{
					  CEK_FLD_ACT2 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField0"))){
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  CEK_FLD_OTH0 = true;
				  }else{
					  CEK_FLD_OTH0 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField1"))){
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  CEK_FLD_OTH1 = true;
				  }else{
					  CEK_FLD_OTH1 = false;
				  }
			  }
			  if (customAttribute.getName().equals(new String("othField2"))){
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
					  CEK_FLD_OTH2 = true;
				  }else{
					  CEK_FLD_OTH2 = false;
				  }
			  }
		  }
		  if (!c.getRequestId().equals(null)){
			  if (!c.getRequestId().trim().equals("") && CtrPALN1.equals(0)){
				  throw new EnterpriseServiceOperationException(
					  new ErrorMessageDTO(
					  "9999", "INPUT REQUIRED", "w0Plan", 0, 0))
					  return dto
			  }
			  if (!c.getRequestId().trim().equals("") && CtrPALN2.equals(0)){
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
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
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
			  
			  if (customAttribute.getName().equals(new String("w1Plan"))){
				  log.info ("w1Plan : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
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
			  
			  if (customAttribute.getName().equals(new String("w2Plan"))){
				  log.info ("w1Plan : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
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
			  
			  if (customAttribute.getName().equals(new String("w0Act"))){
				  log.info ("w0Act : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
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
			  
			  if (customAttribute.getName().equals(new String("w1Act"))){
				  log.info ("w1Act : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
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
			  
			  if (customAttribute.getName().equals(new String("w2Act"))){
				  log.info ("w2Act : " + customAttribute.getValue());
				  if (!customAttribute.getValue().equals("") && !customAttribute.getValue().equals(null)){
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
			  if (customAttribute.getName().equals(new String("othField0"))){
				  log.info ("othField0 : " + customAttribute.getValue());
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
						  def QueryRes1 = sql.execute(QueryInsert,"001");
						  log.info ("QueryRes1 : " + QueryRes1);
					  }catch (Exception  e) {
						  log.info("execption Insert = : " + e);
						  Errexcpt = e
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
		  */
		  //***FMFC END
		 
		 log.info("WorkOrderServiceCreateRequestDTO_NEW: " + c)
		 String act_code = "00"
		 //if (c.getWorkOrder()==null){
		 MSF010Key msf010key = new MSF010Key()
	 
		 
		 Constraint c1 = MSF010Key.tableType.equalTo("+WAC")
		 Constraint c2 = MSF010Key.tableCode.equalTo(c.getMaintenanceType())
		 log.info("nomor kode activity_12-02-2014_new1: " + c.getMaintenanceType())
		 
		 
		 Query query1 = new QueryImpl(MSF010Rec.class).and(c1).and(c2)
		 
		 
		 MSF010Rec msf010Rec = tools.edoi.firstRow(query1)
		 //log.info("querynya : " + msf010Rec )
		 if (msf010Rec != null){
		 
		 act_code =  msf010Rec.getTableDesc()
		}
	 if (c.getEquipmentRef() == null)  {
		 if( c.getAccountCode() != null) {
			 if (c.getAccountCode().length() > 15) {
			 log.info("akun2" +  c.getAccountCode().substring(15))
			 c.setAccountCode(c.getAccountCode().substring(0, 13) + act_code + c.getAccountCode().substring(15))
			 c.getAccountCode().substring(0, 13) + act_code + c.getAccountCode().substring(15)
			 }
				 }
			 
		 }
		 else {			 
		 if (c.getEquipmentRef().trim() != ""  ) {
		 
		 MSF600Key msf600key = new MSF600Key()
		 
		 Constraint c3 = MSF600Key.equipNo.equalTo(c.getEquipmentRef())
		 log.info("nomor equip_pindah_new1 : " + c.getEquipmentRef())
		 
		 Query query2 = new QueryImpl(MSF600Rec.class).and(c3)
		 
		 MSF600Rec msf600Rec = tools.edoi.firstRow(query2)
		 if (msf600Rec != null){
		 
		 log.info("null3")
		 String account_code =  msf600Rec.getAccountCode()
		 c.setAccountCode(account_code.substring(0, 13)+act_code)
		 account_code.substring(0, 13)+act_code
		 }else{
		 Constraint c4 = MSF600Key.equipNo.greaterThan(" ")
 
						Constraint c5 = MSF600Rec.plantNo.equalTo(c.getEquipmentRef())
						Query query3 = new QueryImpl(MSF600Rec.class).and(c4).and(c5)
		 
				 MSF600Rec msf600Recb = tools.edoi.firstRow(query3)
					if (msf600Recb != null){
					String account_code =  msf600Recb.getAccountCode()
					 c.setAccountCode(account_code.substring(0, 13)+act_code)
					 log.info("set AccontCode_NEW2_12Feb_new1: " + account_code.substring(0, 13)+act_code)
					 account_code.substring(0, 13)+act_code			 
				   }
				}
			}						 
		}
		
		//Nota Dinas No. B1013082
		String project_no = c.getProjectNo()
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