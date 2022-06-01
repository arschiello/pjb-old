package PJB.FMFC

/**
 * @EMS 2018
 *
 * FMFC Customisation - Check the Actual data before complete WO
 **/

import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.service.m3620.workorderutility.WorkOrderUtilityService
import com.mincom.ellipse.types.m3620.instances.WorkOrderUtilityDTO;
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.eql.impl.*
import com.mincom.eql.*

class WorkOrderUtilityService_completeWorkOrderLabourCosting extends ServiceHook{

	String hookVersion = "v1"
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks onPreExecute logging.version: ${hookVersion}")
		WorkOrderUtilityDTO  c = (WorkOrderUtilityDTO) input
		
		//FORWARD FIT FROM WorkOrderService_complete - START
		String dis_code = tools.commarea.District
		String work_order = c.getWorkOrder().getValue()
		Constraint c4 = MSF620Key.dstrctCode.equalTo(dis_code)
		Constraint c5 = MSF620Key.workOrder.equalTo(work_order)
	
		log.info("district Code Pre: " + tools.commarea.District)
		
		Query query3 = new QueryImpl(MSF620Rec.class).and(c4).and(c5)
		
		
		MSF620Rec msf620Rec = tools.edoi.firstRow(query3)
		String status_wo =  msf620Rec.getWoStatusM()
		String task_aptw_sw = msf620Rec.getTaskAptwSw()
		String aptw_exists_sw = msf620Rec.getAptwExistsSw()
		String maint_type = msf620Rec.getMaintType()
		
		//Get segment 3 & 4 account code (segmen 3: 33 & segmen 4: 370,350)
		String acct_code = msf620Rec.getDstrctAcctCode()
		String segmen_3 = acct_code.substring(7,9)
		String segmen_4 = acct_code.substring(9,12)
		
		log.info("STATUS wO Pre: ${status_wo}")
		
		if (status_wo!="C")
		{	
			/*
			Maint type CR, EJ dan EM harus dibuatkan APTW
			Pengecualian untuk Muara Tawar Blok 5 dan Muara Karang Blok 2
			*/
			if(segmen_3!="33"){
				if(segmen_4!="370" || segmen_4!="350"){
					if (maint_type=="CR" || maint_type=="EM" || maint_type=="EJ") {
						if(task_aptw_sw!="Y" && aptw_exists_sw!="Y")
						{
							log.info("Exception ")
							throw new EnterpriseServiceOperationException(
								new ErrorMessageDTO(
								"9999", "APTW INPUT REQUIRED", "", 0, 0))
								return input
						}
					}
				}
			}
		}
				
		
		//FORWARD FIT FROM WorkOrderService_complete - END
		
		//***FMFC START
		log.info("District Code : " + tools.commarea.District)
		log.info("WO NO : " + c.getWorkOrder().getValue())
		Boolean MSSS_DATA_FLAG = false;
		Boolean MSSS_DATA_FLAG2 = false;
		Boolean MANDATORY = false;
		
		String QRY1 = "select * from msf071 " +
			"where ENTITY_TYPE = 'SSS' and trim(ENTITY_VALUE) in ('"+tools.commarea.District+c.getWorkOrder().getValue()+"ACT') and REF_NO = '001'";
		log.info("Query : " + QRY1)
		 
		def QueryRes1 = sql.firstRow(QRY1);
		log.info ("QueryRes1 : " + QueryRes1);
		
		if (!QueryRes1.equals(null)){
			if (!QueryRes1.REF_CODE.equals(null)){
				if (!QueryRes1.REF_CODE.trim().equals("")){
					MSSS_DATA_FLAG = false;
				}else{
					MSSS_DATA_FLAG = true;
				}
			}else{
				MSSS_DATA_FLAG = true;
			}
		}else{
			MSSS_DATA_FLAG = true;
		}
		
		String QRY2 = "select * from msf071 " +
			"where ENTITY_TYPE = 'SSS' and trim(ENTITY_VALUE) in ('"+tools.commarea.District+c.getWorkOrder().getValue()+"OTH') and REF_NO = '001'";
		log.info("Query : " + QRY2)
		 
		def QueryRes2 = sql.firstRow(QRY2);
		log.info ("QueryRes2 : " + QueryRes2);
		
		if (!QueryRes2.equals(null)){
			if (!QueryRes2.REF_CODE.equals(null)){
				if (!QueryRes2.REF_CODE.trim().equals("")){
					MSSS_DATA_FLAG2 = false;
				}else{
					MSSS_DATA_FLAG2 = true;
				}
			}else{
				MSSS_DATA_FLAG2 = true;
			}
		}else{
			MSSS_DATA_FLAG2 = true;
		}
		
		String QRY3 = "select REQUEST_ID from msf620 " +
			"where DSTRCT_CODE = '"+tools.commarea.District+"' and WORK_ORDER = '"+c.getWorkOrder().getValue()+"'";
		log.info("Query : " + QRY3)
		 
		def QueryRes3 = sql.firstRow(QRY3);
		log.info ("QueryRes3 : " + QueryRes3);
		
		if (!QueryRes3.equals(null)){
			if (!QueryRes3.REQUEST_ID.equals(null)){
				if (!QueryRes3.REQUEST_ID.trim().equals("")){
					MANDATORY = true;
				}else{
					MANDATORY = false;
				}
			}else{
				MANDATORY = false;
			}
		}else{
			MANDATORY = false;
		}
		
		if (MSSS_DATA_FLAG == true && MSSS_DATA_FLAG2 == true && MANDATORY == true){
			throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "ACTUAL OR OTHER JOB CODES REQUIRED (FAILURE MODE) !", "", 0, 0))
				return input;
		}
		
		MSSS_DATA_FLAG = false;
		MSSS_DATA_FLAG2 = false;
		MANDATORY = false;
		
		QRY1 = "select * from msf071 " +
			"where ENTITY_TYPE = 'SSS' and trim(ENTITY_VALUE) in ('"+tools.commarea.District+c.getWorkOrder().getValue()+"ACT') and REF_NO = '002'";
		log.info("Query : " + QRY1)
		 
		QueryRes1 = sql.firstRow(QRY1);
		log.info ("QueryRes1 : " + QueryRes1);
		
		if (!QueryRes1.equals(null)){
			if (!QueryRes1.REF_CODE.equals(null)){
				if (!QueryRes1.REF_CODE.trim().equals("")){
					MSSS_DATA_FLAG = false;
				}else{
					MSSS_DATA_FLAG = true;
				}
			}else{
				MSSS_DATA_FLAG = true;
			}
		}else{
			MSSS_DATA_FLAG = true;
		}
		
		QRY2 = "select * from msf071 " +
			"where ENTITY_TYPE = 'SSS' and trim(ENTITY_VALUE) in ('"+tools.commarea.District+c.getWorkOrder().getValue()+"OTH') and REF_NO = '002'";
		log.info("Query : " + QRY2)
		 
		QueryRes2 = sql.firstRow(QRY2);
		log.info ("QueryRes2 : " + QueryRes2);
		
		if (!QueryRes2.equals(null)){
			if (!QueryRes2.REF_CODE.equals(null)){
				if (!QueryRes2.REF_CODE.trim().equals("")){
					MSSS_DATA_FLAG2 = false;
				}else{
					MSSS_DATA_FLAG2 = true;
				}
			}else{
				MSSS_DATA_FLAG2 = true;
			}
		}else{
			MSSS_DATA_FLAG2 = true;
		}
		
		QRY3 = "select REQUEST_ID from msf620 " +
			"where DSTRCT_CODE = '"+tools.commarea.District+"' and WORK_ORDER = '"+c.getWorkOrder().getValue()+"'";
		log.info("Query : " + QRY3)
		 
		QueryRes3 = sql.firstRow(QRY3);
		log.info ("QueryRes3 : " + QueryRes3);
		
		if (!QueryRes3.equals(null)){
			if (!QueryRes3.REQUEST_ID.equals(null)){
				if (!QueryRes3.REQUEST_ID.trim().equals("")){
					MANDATORY = true;
				}else{
					MANDATORY = false;
				}
			}else{
				MANDATORY = false;
			}
		}else{
			MANDATORY = false;
		}
		
		if (MSSS_DATA_FLAG == true && MSSS_DATA_FLAG2 == true && MANDATORY == true){
			throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "ACTUAL OR OTHER JOB CODES REQUIRED (FAILURE CAUSE) !", "", 0, 0))
				return input;
		}
		
		MSSS_DATA_FLAG = false;
		MSSS_DATA_FLAG2 = false;
		MANDATORY = false;
		
		QRY1 = "select * from msf071 " +
			"where ENTITY_TYPE = 'SSS' and trim(ENTITY_VALUE) in ('"+tools.commarea.District+c.getWorkOrder().getValue()+"ACT') and REF_NO = '003'";
		log.info("Query : " + QRY1)
		 
		QueryRes1 = sql.firstRow(QRY1);
		log.info ("QueryRes1 : " + QueryRes1);
		
		if (!QueryRes1.equals(null)){
			if (!QueryRes1.REF_CODE.equals(null)){
				if (!QueryRes1.REF_CODE.trim().equals("")){
					MSSS_DATA_FLAG = false;
				}else{
					MSSS_DATA_FLAG = true;
				}
			}else{
				MSSS_DATA_FLAG = true;
			}
		}else{
			MSSS_DATA_FLAG = true;
		}
		
		QRY2 = "select * from msf071 " +
			"where ENTITY_TYPE = 'SSS' and trim(ENTITY_VALUE) in ('"+tools.commarea.District+c.getWorkOrder().getValue()+"OTH') and REF_NO = '003'";
		log.info("Query : " + QRY2)
		 
		QueryRes2 = sql.firstRow(QRY2);
		log.info ("QueryRes2 : " + QueryRes2);
		
		if (!QueryRes2.equals(null)){
			if (!QueryRes2.REF_CODE.equals(null)){
				if (!QueryRes2.REF_CODE.trim().equals("")){
					MSSS_DATA_FLAG2 = false;
				}else{
					MSSS_DATA_FLAG2 = true;
				}
			}else{
				MSSS_DATA_FLAG2 = true;
			}
		}else{
			MSSS_DATA_FLAG2 = true;
		}
		
		QRY3 = "select REQUEST_ID from msf620 " +
			"where DSTRCT_CODE = '"+tools.commarea.District+"' and WORK_ORDER = '"+c.getWorkOrder().getValue()+"'";
		log.info("Query : " + QRY3)
		 
		QueryRes3 = sql.firstRow(QRY3);
		log.info ("QueryRes3 : " + QueryRes3);
		
		if (!QueryRes3.equals(null)){
			if (!QueryRes3.REQUEST_ID.equals(null)){
				if (!QueryRes3.REQUEST_ID.trim().equals("")){
					MANDATORY = true;
				}else{
					MANDATORY = false;
				}
			}else{
				MANDATORY = false;
			}
		}else{
			MANDATORY = false;
		}
		
		if (MSSS_DATA_FLAG == true && MSSS_DATA_FLAG2 == true && MANDATORY == true){
			throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999", "ACTUAL OR OTHER JOB CODES REQUIRED (CORRECTIVE ACTION) !", "", 0, 0))
				return input;
		}
		//***FMFC END
		return null;
	}
}