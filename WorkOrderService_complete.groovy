/**
* @EMS 2014
*
* Revision History
* 13-Apr-2015.............a9ms6435 Initial Non User Exit-
* ........................update work order status (ZZ) if WO status Closed 
* * */

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


class WorkOrderService_complete extends ServiceHook{

	private EDOIWrapper edoi;
	String hookVersion = "kayu putih"
	
	@Override
	public Object onPreExecute(Object dto) throws Exception{
		
		log.info("Hooks onPreExecute logging.version: ${hookVersion}")

		
		//WorkOrderServiceCompleteReplyDTO reply = (WorkOrderServiceCompleteReplyDTO) result
		WorkOrderServiceCompleteRequestDTO  c = (WorkOrderServiceCompleteRequestDTO) dto
		
		
		String dis_code = c.getDistrictCode()
		String work_order = c.getWorkOrder()
		Constraint c4 = MSF620Key.dstrctCode.equalTo(dis_code)
		Constraint c5 = MSF620Key.workOrder.equalTo(work_order)
	
		log.info("district Code Pre: " + c.getDistrictCode())
		
		Query query3 = new QueryImpl(MSF620Rec.class).and(c4).and(c5)
		
		
		MSF620Rec msf620Rec = tools.edoi.firstRow(query3)
		String status_wo =  msf620Rec.getWoStatusM()
		log.info("STATUS wO Pre: ${status_wo}")
		
		
		String close_date = msf620Rec.getClosedDt()
		String close_time = msf620Rec.getClosedTime()
		String complete_by = msf620Rec.getCompletedBy()
		String creation_date = msf620Rec.getCreationDate()
		String creation_time = msf620Rec.getCreationTime()
		String task_aptw_sw = msf620Rec.getTaskAptwSw()
		String aptw_exists_sw = msf620Rec.getAptwExistsSw()
		String maint_type = msf620Rec.getMaintType()
		
		
		
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
		
		else
		{			
				if (maint_type=="CR" || maint_type=="EM" || maint_type=="EJ") {
					if(task_aptw_sw!="Y" && aptw_exists_sw!="Y")
					{ 
						throw new EnterpriseServiceOperationException()
					}
				}
		}
		
						
		return null
		//}
 
	}
	@Override
	public Object onPostExecute(Object input, Object result) throws Exception{
		
		log.info("Hooks onPostExecute logging.version: ${hookVersion}")

		WorkOrderServiceCompleteReplyDTO reply = (WorkOrderServiceCompleteReplyDTO) result
		WorkOrderServiceCompleteRequestDTO  c = (WorkOrderServiceCompleteRequestDTO) input
		
		
		String dis_code = c.getDistrictCode()
		String work_order = c.getWorkOrder()
		Constraint c4 = MSF620Key.dstrctCode.equalTo(dis_code)
		Constraint c5 = MSF620Key.workOrder.equalTo(work_order)
	
		log.info("district Code : " + c.getDistrictCode())
		
		Query query3 = new QueryImpl(MSF620Rec.class).and(c4).and(c5)
		
		
		MSF620Rec msf620Rec = tools.edoi.firstRow(query3)
		String status_wo =  msf620Rec.getWoStatusM()
		log.info("STATUS wO : ${status_wo}")
		
		
		String close_date = msf620Rec.getClosedDt()
		String close_time = msf620Rec.getClosedTime()
		String complete_by = msf620Rec.getCompletedBy()
		String creation_date = msf620Rec.getCreationDate()
		String creation_time = msf620Rec.getCreationTime()
		String task_aptw_sw = msf620Rec.getTaskAptwSw()
		String aptw_exists_sw = msf620Rec.getAptwExistsSw()
		String maint_type = msf620Rec.getMaintType()
		
		
		
		if (status_wo=="C")
		{
			
			log.info("MASUK IF " )
			WorkOrderServiceCreateReplyDTO workOrder = new WorkOrderServiceCreateReplyDTO()
			workOrder.setWorkOrderStatusU("ZZ")
			log.info("wORK ORDER STATUS U " + workOrder.getWorkOrderStatusU())
			
			WOUserStatHistDTO wouserStat = new WOUserStatHistDTO()
			WorkOrderDTO workorder = new WorkOrderDTO()
			
			String stat="ZZ"
			wouserStat.setWOUSHStatus(stat)
			log.info("user history status : " + wouserStat.getWOUSHStatus())
			log.info("wORK ORDER STATUS U " + workOrder.getWorkOrderStatusU())
			
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
			
			log.info("Wo Status HIstory New " + msf627Rec.getWoStatusUNew())
			}
			
		
		
		msf620Rec.setWoStatusU("ZZ")
		
		tools.edoi.update(msf620Rec)
		
		log.info("record updated for district code: ${dis_code},Work order : ${work_order}")
			
			
		}
		
		else
		{			
				if (maint_type=="CR" || maint_type=="EM" || maint_type=="EJ") {
					if(task_aptw_sw!="Y" && aptw_exists_sw!="Y")
					{ 
						throw new EnterpriseServiceOperationException()
					}
				}
		}
		
			
	
		return result
	}
	
}