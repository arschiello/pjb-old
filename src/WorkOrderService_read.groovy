package PJB_DEPLOY_FMFC
/**
* @EMS 2014
*
* Revision History
* 04 Nov 2018.............a9ra5213 Add FMFC Customisation
* 16-Jun-2015.............a9ms6435 --PN604
*.........................add prefix "0" in WO Number if first character is numeric
* 03-Jun-2015.............a9ms6435 --PN598
* ........................add prefix "0" in WO Number
* 13-Apr-2015.............a9ms6435 Initial Non User Exit-
* ........................update work order status (ZZ) if WO status Closed 
* * */

import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import org.slf4j.LoggerFactory;
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.edoi.ejb.msf627.MSF627Key
import com.mincom.ellipse.edoi.ejb.msf627.MSF627Rec
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateRequestDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceReadRequestDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceReadReplyDTO
//
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCompleteRequestDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceCompleteReplyDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WOUserStatHistDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
//
import com.mincom.enterpriseservice.ellipse.workordertask.WorkOrderTaskServiceRetrieveReplyCollectionDTO
import com.mincom.enterpriseservice.ellipse.workordertask.WorkOrderTaskServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.ellipse.workordertask.WorkOrderTaskServiceRetrieveReplyDTO
import com.mincom.enterpriseservice.ellipse.workordertask.WorkOrderTaskServiceRetrieveRequiredAttributesDTO
import com.mincom.enterpriseservice.ellipse.workordertask.WorkOrderTaskServiceCompleteReplyCollectionDTO
import com.mincom.enterpriseservice.ellipse.workordertask.WorkOrderTaskServiceCompleteRequestDTO
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.script.util.*
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.batch.environment.*;
import com.mincom.ellipse.client.connection.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.script.util.CommAreaScriptWrapper

//==


class WorkOrderService_read extends ServiceHook{

	private EDOIWrapper edoi;
	String hookVersion = "read_03_06_15"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPreExecute(Object dto) {
		
		log.info("Hooks onPreExecute logging.version: ${hookVersion}")

		
		//WorkOrderServiceCompleteReplyDTO reply = (WorkOrderServiceCompleteReplyDTO) result
		WorkOrderServiceReadRequestDTO  c = (WorkOrderServiceReadRequestDTO) dto
		
		
		String dis_code = c.getDistrictCode()
		String work_order = c.getWorkOrder().toString().trim()
		
//		if (work_order.length() < 8 && work_order.substring(0, 1).matches("[0-9]") ){
//			work_order = work_order.padLeft(8,"0")
//	    }
		
		Constraint c4 = MSF620Key.dstrctCode.equalTo(dis_code)
		Constraint c5 = MSF620Key.workOrder.equalTo(work_order)
	
		log.info("district Code Pre: " + c.getDistrictCode())
		
		Query query3 = new QueryImpl(MSF620Rec.class).and(c4).and(c5)
		
		
		MSF620Rec msf620Rec = tools.edoi.firstRow(query3)
		if (msf620Rec != null ) {
		String status_wo =  msf620Rec.getWoStatusM()
		log.info("STATUS wO Pre: ${status_wo}")
		String close_date = msf620Rec.getClosedDt()
		String close_time = msf620Rec.getClosedTime()
		String complete_by = msf620Rec.getCompletedBy()
		String creation_date = msf620Rec.getCreationDate()
		String creation_time = msf620Rec.getCreationTime()


				if (status_wo=="C")
				{
					
					log.info("MASUK IF Pre " )
					WorkOrderServiceCreateReplyDTO workOrder = new WorkOrderServiceCreateReplyDTO()
					workOrder.setWorkOrderStatusU("ZZ")
					log.info("wORK ORDER STATUS U Pre " + workOrder.getWorkOrderStatusU())
					
					WOUserStatHistDTO wouserStat = new WOUserStatHistDTO()
					WorkOrderDTO workorder = new WorkOrderDTO()
					
					String stat="ZZ"
					wouserStat.setWOUSHStatus(stat)
					log.info("user history status Pre: " + wouserStat.getWOUSHStatus())
					log.info("wORK ORDER STATUS U Pre " + workOrder.getWorkOrderStatusU())
					
					String TranDate = 99999999-creation_date.toInteger()
					String TranTime = 999999-creation_time.toInteger()
					Constraint c6 = MSF627Key.dstrctCode.equalTo(dis_code)
					Constraint c7 = MSF627Key.workOrder.equalTo(work_order)
					Constraint c8 = MSF627Key.tranDateRevsd.equalTo(TranDate)
					Constraint c9 = MSF627Key.tranTimeRevsd.equalTo(TranTime)
					
					Query query4 = new QueryImpl(MSF627Rec.class).and(c6).and(c7)
					//.and(c8).and(c9)
					MSF627Rec msf627Rec = tools.edoi.firstRow(query4)
					if (msf627Rec != null ) {
					msf627Rec.setWoStatusUNew("ZZ")
					tools.edoi.update(msf627Rec)
					
					log.info("Wo Status HIstory New Pre " + msf627Rec.getWoStatusUNew())
					}
					
				
				
				msf620Rec.setWoStatusU("ZZ")
				
				tools.edoi.update(msf620Rec)
				
				log.info("record updated for district code pre: ${dis_code},Work order : ${work_order}")
					
					
				}
		
		
		
		}
		
		
		
		else{
			throw new EnterpriseServiceOperationException(
		new ErrorMessageDTO(
		"0039", "WORK ORDER NOT ON FILE","", 0, 0))
		//return dto
	   }
		
		
		//String status_wo =  msf620Rec.getWoStatusM()
		//log.info("STATUS wO Pre: ${status_wo}")
		
		
						
		return null
		//}
 
	}
	@Override
	public Object onPostExecute(Object input, Object result) {
		
		log.info("Hooks onPostExecute logging.version: ${hookVersion}")

		WorkOrderServiceReadReplyDTO reply = (WorkOrderServiceReadReplyDTO) result
		WorkOrderServiceReadRequestDTO  c = (WorkOrderServiceReadRequestDTO) input
		
		//***FMFC START
		 Attribute[] ATT = new Attribute[18];
		 log.info("ATT SIZE: " + ATT.size());
		 ATT[0] = new Attribute();
		 ATT[0].setName("planLbl1");
		 ATT[0].setValue("Failure Mode");
		 
		 ATT[1] = new Attribute();
		 ATT[1].setName("planLbl2");
		 ATT[1].setValue("Failure Cause");
		 
		 ATT[2] = new Attribute();
		 ATT[2].setName("planLbl3");
		 ATT[2].setValue("Action to Confirm");
		 
		 String strWO = c.getWorkOrder().getNo();
		 String strPRE = c.getWorkOrder().getPrefix();
		 
		 def QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '001' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"PLAN'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 ATT[3] = new Attribute();
		 ATT[12] = new Attribute();
		 if (!QueryRes1.equals(null)){
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[3].setName("w0Plan");
				 ATT[3].setValue(QueryRes1.REF_CODE);
				 //Show Description in separated Field
				 def QueryRes1_A = sql.firstRow("select trim(STD_STATIC_2) ||' '|| trim(STD_STATIC_3) NEW_DESC from MSF096_STD_STATIC " +
						 "where trim(STD_KEY) = 'W0  '||trim('"+QueryRes1.REF_CODE+"')  and  STD_TEXT_CODE = 'TT' and STD_LINE_NO = '0000'");
				 log.info ("QueryRes1_A : " + QueryRes1_A);
				 if (!QueryRes1_A.equals(null)){
					 ATT[12].setName("planDesc0");
					 ATT[12].setValue(QueryRes1_A.NEW_DESC);
				 }else{
					 ATT[12].setName("planDesc0");
					 ATT[12].setValue("");
				 }
			 }else{
				 ATT[3].setName("w0Plan");
				 ATT[3].setValue("");
				 ATT[12].setName("planDesc0");
				 ATT[12].setValue("");
			 }
		 }else{
			 ATT[3].setName("w0Plan");
			 ATT[3].setValue("");
			 ATT[12].setName("planDesc0");
			 ATT[12].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '002' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"PLAN'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 ATT[4] = new Attribute();
		 ATT[13] = new Attribute();
		 if (!QueryRes1.equals(null)){
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[4].setName("w1Plan");
				 ATT[4].setValue(QueryRes1.REF_CODE);
				 def QueryRes1_A = sql.firstRow("select trim(STD_STATIC_2) ||' '|| trim(STD_STATIC_3) NEW_DESC from MSF096_STD_STATIC " +
						 "where trim(STD_KEY) = 'W1  '||trim('"+QueryRes1.REF_CODE+"')  and  STD_TEXT_CODE = 'TT' and STD_LINE_NO = '0000'");
				 log.info ("QueryRes1_A : " + QueryRes1_A);
				 if (!QueryRes1_A.equals(null)){
					 ATT[13].setName("planDesc1");
					 ATT[13].setValue(QueryRes1_A.NEW_DESC);
				 }else{
					 ATT[13].setName("planDesc1");
					 ATT[13].setValue("");
				 }
			 }else{
				 ATT[4].setName("w1Plan");
				 ATT[4].setValue("");
				 ATT[13].setName("planDesc1");
				 ATT[13].setValue("");
			 }
		 }else{
			 ATT[4].setName("w1Plan");
			 ATT[4].setValue("");
			 ATT[13].setName("planDesc1");
			 ATT[13].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '003' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"PLAN'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 ATT[5] = new Attribute();
		 ATT[14] = new Attribute();
		 if (!QueryRes1.equals(null)){
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[5].setName("w2Plan");
				 ATT[5].setValue(QueryRes1.REF_CODE);
				 def QueryRes1_A = sql.firstRow("select trim(STD_STATIC_2) ||' '|| trim(STD_STATIC_3) NEW_DESC from MSF096_STD_STATIC " +
						 "where trim(STD_KEY) = 'W2  '||trim('"+QueryRes1.REF_CODE+"')  and  STD_TEXT_CODE = 'TT' and STD_LINE_NO = '0000'");
				 log.info ("QueryRes1_A : " + QueryRes1_A);
				 if (!QueryRes1_A.equals(null)){
					 ATT[14].setName("planDesc2");
					 ATT[14].setValue(QueryRes1_A.NEW_DESC);
				 }else{
					 ATT[14].setName("planDesc2");
					 ATT[14].setValue("");
				 }
			 }else{
				 ATT[5].setName("w2Plan");
				 ATT[5].setValue("");
				 ATT[14].setName("planDesc2");
				 ATT[14].setValue("");
			 }
		 }else{
			 ATT[5].setName("w2Plan");
			 ATT[5].setValue("");
			 ATT[14].setName("planDesc2");
			 ATT[14].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '001' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"ACT'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 ATT[6] = new Attribute();
		 ATT[15] = new Attribute();
		 if (!QueryRes1.equals(null)){
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[6].setName("w0Act");
				 ATT[6].setValue(QueryRes1.REF_CODE);
				 def QueryRes1_A = sql.firstRow("select trim(STD_STATIC_2) ||' '|| trim(STD_STATIC_3) NEW_DESC from MSF096_STD_STATIC " +
						 "where trim(STD_KEY) = 'W0  '||trim('"+QueryRes1.REF_CODE+"')  and  STD_TEXT_CODE = 'TT' and STD_LINE_NO = '0000'");
				 log.info ("QueryRes1_A : " + QueryRes1_A);
				 if (!QueryRes1_A.equals(null)){
					 ATT[15].setName("actDesc0");
					 ATT[15].setValue(QueryRes1_A.NEW_DESC);
				 }else{
					 ATT[15].setName("actDesc0");
					 ATT[15].setValue("");
				 }
			 }else{
				 ATT[6].setName("w0Act");
				 ATT[6].setValue("");
				 ATT[15].setName("actDesc0");
				 ATT[15].setValue("");
			 }
		 }else{
			 ATT[6].setName("w0Act");
			 ATT[6].setValue("");
			 ATT[15].setName("actDesc0");
			 ATT[15].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '002' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"ACT'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 ATT[7] = new Attribute();
		 ATT[16] = new Attribute();
		 if (!QueryRes1.equals(null)){
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[7].setName("w1Act");
				 ATT[7].setValue(QueryRes1.REF_CODE);
				 def QueryRes1_A = sql.firstRow("select trim(STD_STATIC_2) ||' '|| trim(STD_STATIC_3) NEW_DESC from MSF096_STD_STATIC " +
						 "where trim(STD_KEY) = 'W1  '||trim('"+QueryRes1.REF_CODE+"')  and  STD_TEXT_CODE = 'TT' and STD_LINE_NO = '0000'");
				 log.info ("QueryRes1_A : " + QueryRes1_A);
				 if (!QueryRes1_A.equals(null)){
					 ATT[16].setName("actDesc1");
					 ATT[16].setValue(QueryRes1_A.NEW_DESC);
				 }else{
					 ATT[16].setName("actDesc1");
					 ATT[16].setValue("");
				 }
			 }else{
				 ATT[7].setName("w1Act");
				 ATT[7].setValue("");
				 ATT[16].setName("actDesc1");
				 ATT[16].setValue("");
			 }
		 }else{
			 ATT[7].setName("w1Act");
			 ATT[7].setValue("");
			 ATT[16].setName("actDesc1");
			 ATT[16].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '003' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"ACT'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 ATT[8] = new Attribute();
		 ATT[17] = new Attribute();
		 if (!QueryRes1.equals(null)){
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[8].setName("w2Act");
				 ATT[8].setValue(QueryRes1.REF_CODE);
				 def QueryRes1_A = sql.firstRow("select trim(STD_STATIC_2) ||' '|| trim(STD_STATIC_3) NEW_DESC from MSF096_STD_STATIC " +
						 "where trim(STD_KEY) = 'W2  '||trim('"+QueryRes1.REF_CODE+"')  and  STD_TEXT_CODE = 'TT' and STD_LINE_NO = '0000'");
				 log.info ("QueryRes1_A : " + QueryRes1_A);
				 if (!QueryRes1_A.equals(null)){
					 ATT[17].setName("actDesc2");
					 ATT[17].setValue(QueryRes1_A.NEW_DESC);
				 }else{
					 ATT[17].setName("actDesc2");
					 ATT[17].setValue("");
				 }
			 }else{
				 ATT[8].setName("w2Act");
				 ATT[8].setValue("");
				 ATT[17].setName("actDesc2");
				 ATT[17].setValue("");
			 }
		 }else{
			 ATT[8].setName("w2Act");
			 ATT[8].setValue("");
			 ATT[17].setName("actDesc2");
			 ATT[17].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '001' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"OTH'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 
		 if (!QueryRes1.equals(null)){
			 ATT[9] = new Attribute();
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[9].setName("othField0");
				 ATT[9].setValue(QueryRes1.REF_CODE);
			 }else{
				 ATT[9].setName("othField0");
				 ATT[9].setValue("");
			 }
		 }else{
			 ATT[9] = new Attribute();
			 ATT[9].setName("othField0");
			 ATT[9].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '002' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"OTH'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 
		 if (!QueryRes1.equals(null)){
			 ATT[10] = new Attribute();
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[10].setName("othField1");
				 ATT[10].setValue(QueryRes1.REF_CODE);
			 }else{
				 ATT[10].setName("othField1");
				 ATT[10].setValue("");
			 }
		 }else{
			 ATT[10] = new Attribute();
			 ATT[10].setName("othField1");
			 ATT[10].setValue("");
		 }
		 
		 QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					 "where ENTITY_TYPE = 'SSS' and REF_NO = '003' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = '"+tools.commarea.District+strPRE+strWO+"OTH'");
		 log.info ("QueryRes1 : " + QueryRes1);
		 
		 if (!QueryRes1.equals(null)){
			 ATT[11] = new Attribute();
			 if (!QueryRes1.REF_CODE.equals(null)){
				 ATT[11].setName("othField2");
				 ATT[11].setValue(QueryRes1.REF_CODE);
			 }else{
				 ATT[11].setName("othField2");
				 ATT[11].setValue("");
			 }
		 }else{
			 ATT[11] = new Attribute();
			 ATT[11].setName("othField2");
			 ATT[11].setValue("");
		 }
		 reply.setCustomAttributes(ATT);
		 //***FMFC END
		
		String dis_code = c.getDistrictCode()
		String work_order = c.getWorkOrder().toString().trim()
		
//		if (work_order.length() < 8 && work_order.substring(0, 1).matches("[0-9]") ){
//			work_order = work_order.padLeft(8,"0")
//		}
		
		Constraint c4 = MSF620Key.dstrctCode.equalTo(dis_code)
		Constraint c5 = MSF620Key.workOrder.equalTo(work_order)
	
		log.info("district Code Pre: " + c.getDistrictCode())
		
		Query query3 = new QueryImpl(MSF620Rec.class).and(c4).and(c5)
		
		
		MSF620Rec msf620Rec = tools.edoi.firstRow(query3)
		if (msf620Rec != null ) {
		String status_wo =  msf620Rec.getWoStatusM()
		log.info("STATUS wO Pre: ${status_wo}")
		String close_date = msf620Rec.getClosedDt()
		String close_time = msf620Rec.getClosedTime()
		String complete_by = msf620Rec.getCompletedBy()
		String creation_date = msf620Rec.getCreationDate()
		String creation_time = msf620Rec.getCreationTime()
		
		
				if (status_wo=="C")
				{
					
					log.info("MASUK IF Pre " )
					WorkOrderServiceCreateReplyDTO workOrder = new WorkOrderServiceCreateReplyDTO()
					workOrder.setWorkOrderStatusU("ZZ")
					log.info("wORK ORDER STATUS U Pre " + workOrder.getWorkOrderStatusU())
					
					WOUserStatHistDTO wouserStat = new WOUserStatHistDTO()
					WorkOrderDTO workorder = new WorkOrderDTO()
					
					String stat="ZZ"
					wouserStat.setWOUSHStatus(stat)
					log.info("user history status Pre: " + wouserStat.getWOUSHStatus())
					log.info("wORK ORDER STATUS U Pre " + workOrder.getWorkOrderStatusU())
					
					String TranDate = 99999999-creation_date.toInteger()
					String TranTime = 999999-creation_time.toInteger()
					Constraint c6 = MSF627Key.dstrctCode.equalTo(dis_code)
					Constraint c7 = MSF627Key.workOrder.equalTo(work_order)
					Constraint c8 = MSF627Key.tranDateRevsd.equalTo(TranDate)
					Constraint c9 = MSF627Key.tranTimeRevsd.equalTo(TranTime)
					
					Query query4 = new QueryImpl(MSF627Rec.class).and(c6).and(c7)
					//.and(c8).and(c9)
					MSF627Rec msf627Rec = tools.edoi.firstRow(query4)
					if (msf627Rec != null ) {
					msf627Rec.setWoStatusUNew("ZZ")
					tools.edoi.update(msf627Rec)
					
					log.info("Wo Status HIstory New Pre " + msf627Rec.getWoStatusUNew())
					}
					
				
				
				msf620Rec.setWoStatusU("ZZ")
				
				tools.edoi.update(msf620Rec)
				
				log.info("record updated for district code pre: ${dis_code},Work order : ${work_order}")
					
					
				}
		
		
		
		}
		
		
		
		else{
			throw new EnterpriseServiceOperationException(
		new ErrorMessageDTO(
		"0039", "WORK ORDER NOT ON FILE","", 0, 0))
		//return input
	   }
		
			
	
		return result
	}
	
}