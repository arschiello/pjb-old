import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import org.slf4j.LoggerFactory;
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.ellipse.edoi.ejb.msf622.MSF622Key
import com.mincom.ellipse.edoi.ejb.msf622.MSF622Rec
import com.mincom.ellipse.edoi.ejb.msf623.MSF623Key
import com.mincom.ellipse.edoi.ejb.msf623.MSF623Rec
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.enterpriseservice.ellipse.dependant.dto.WOUserStatHistDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.workordertask.*
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.script.util.*
import com.mincom.batch.script.*
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.batch.environment.*;
import com.mincom.ellipse.client.connection.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.script.util.CommAreaScriptWrapper

class WorkOrderTaskService_complete extends ServiceHook{

	private EDOIWrapper edoi;
	String hookVersion = "rifkyrs"
	
	@Override
	public Object onPreExecute(Object dto) {
		
		log.info("Hooks onPreExecute logging.version: ${hookVersion}")
		log.info("Getting Started...!!!!")

		WorkOrderTaskServiceCompleteRequestDTO  c = (WorkOrderTaskServiceCompleteRequestDTO) dto 
		
		
				String task_nmr="",dis_code="",work_order="";
				
				task_nmr = c.getWOTaskNo().toString()
				dis_code = c.getDistrictCode().toString()
				work_order = c.getWorkOrder().toString()
					
				Constraint c10 = MSF623Key.dstrctCode.equalTo(dis_code)
				Constraint c11 = MSF623Key.workOrder.equalTo(work_order)
				Constraint c12 = MSF623Key.woTaskNo.equalTo(task_nmr)
				Constraint c13 = MSF620Key.dstrctCode.equalTo(dis_code)
				Constraint c14 = MSF620Key.workOrder.equalTo(work_order)
				
				//Task
				Query query3 = new QueryImpl(MSF623Rec.class).and(c10).and(c11) .and(c12) 
				
				MSF623Rec msf623Rec = tools.edoi.firstRow(query3)
				String status_task =  msf623Rec.getTaskStatusM()
				log.info("STATUS Task Pre: ${status_task}")
				
				String aptw_exists_sw = msf623Rec.getAptwExistsSw()
				
				//WO
				Query query9 = new QueryImpl(MSF620Rec.class).and(c13).and(c14)
				
				MSF620Rec msf620Rec = tools.edoi.firstRow(query9)
				String status_wo =  msf620Rec.getWoStatusM()
				log.info("STATUS wO Pre: ${status_wo}")
			
				String task_aptw_sw = msf620Rec.getTaskAptwSw()
				String maint_type = msf620Rec.getMaintType()
				
				if(status_wo!="C")
				{			
						if (maint_type=="CR" || maint_type=="EM" || maint_type=="EJ") {
							if(status_task !="C"){
									if(aptw_exists_sw !="Y")
									{ 
										throw new EnterpriseServiceOperationException(new ErrorMessageDTO("2626", "APTW Must Be Filled","", 0, 0))
									}
								}
						}
				}

		return null
		
	}
	
}
