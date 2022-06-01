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
import com.mincom.ellipse.service.m3620.work.WorkService;
import com.mincom.ellipse.types.m3620.instances.WorkDTO;
import com.mincom.ellipse.types.m0000.instances.AccountCode;
import com.mincom.ellipse.types.m0000.instances.MaintType;
import com.mincom.ellipse.types.m3620.instances.WorkServiceResult;
import com.mincom.ellipse.service.ServiceDTO;

class WorkService_create  extends ServiceHook{
	private EDOIWrapper edoi;
	private EROIWrapper eroi;
	String hookVersion = "1.8"
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
		String wo_type = "MT"
		String equip_ref = ""
		String actcode_seg3 = "00"
		WorkDTO c = (WorkDTO) dto
		
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
		
		//Cek PM PdM OH Pengecualian Untuk UBJOM C-056138
		if (!c.getMaintenanceType().value.equals(null) && !c.getMaintenanceType().value.equals(""))
		 {
		   if(c.getDistrictCode().value.equals("UPMT") || c.getDistrictCode().value.equals("UPMK")){
				if(!actcode_seg3.equals("33")){
						if(c.getWorkOrderType().value.equals("MT")){
							   if(!c.getMaintenanceType().value.equals("CR") && !c.getMaintenanceType().value.equals("EJ") && !c.getMaintenanceType().value.equals("NM") && !c.getMaintenanceType().value.equals("OM")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = MT and maintenance type other than CR, EJ, OM & NM directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("CP")) {
							   if(!c.getMaintenanceType().value.equals("EJ")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = CP and maintenance type other than EJ directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("ES")) {
							   if(!c.getMaintenanceType().value.equals("SF") && !c.getMaintenanceType().value.equals("EV") && !c.getMaintenanceType().value.equals("CH")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = EV and maintenance type other than SF, EV & CH directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("WR")) {
							   if(!c.getMaintenanceType().value.equals("OH")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = WR and maintenance type other than OH directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("ST")) {
							   if(!c.getMaintenanceType().value.equals("OH") && !c.getMaintenanceType().value.equals("PM") && !c.getMaintenanceType().value.equals("PD") && !c.getMaintenanceType().value.equals("NM")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = ST and maintenance type other than OH, PM, PD & NM directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
				}
		   }
		   else{
			   if(!c.getDistrictCode().value.equals("UJIN") && !c.getDistrictCode().value.equals("UJPC") && !c.getDistrictCode().value.equals("UJPT") && !c.getDistrictCode().value.equals("UJRB") && !c.getDistrictCode().value.equals("UJTA")){
				   if(c.getWorkOrderType().value.equals("MT")){
						  if(!c.getMaintenanceType().value.equals("CR") && !c.getMaintenanceType().value.equals("EJ") && !c.getMaintenanceType().value.equals("NM") && !c.getMaintenanceType().value.equals("OM")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = MT and maintenance type other than CR, EJ, OM & NM directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("CP")) {
							   if(!c.getMaintenanceType().value.equals("EJ")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = CP and maintenance type other than EJ directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("ES")) {
							   if(!c.getMaintenanceType().value.equals("SF") && !c.getMaintenanceType().value.equals("EV") && !c.getMaintenanceType().value.equals("CH")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = EV and maintenance type other than SF, EV & CH directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("WR")) {
							   if(!c.getMaintenanceType().value.equals("OH")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = WR and maintenance type other than OH directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
						   else if(c.getWorkOrderType().value.equals("ST")) {
							   if(!c.getMaintenanceType().value.equals("OH") && !c.getMaintenanceType().value.equals("PM") && !c.getMaintenanceType().value.equals("PD") && !c.getMaintenanceType().value.equals("NM")){
								   throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "You can't create a WO with combination of work type = ST and maintenance type other than OH, PM, PD & NM directly", "maintenanceType", 0, 0))
								   return dto
							   }
						   }
					}
			}
		 }
		 
		 if (!c.getRiskCode10().value.equals(null) && !c.getRiskCode10().value.equals(""))
		 {
			 mt_type = c.getRiskCode10().value
			 MaintType VAR_MT_TYPE = new MaintType();
			 VAR_MT_TYPE.setValue(mt_type);
			 c.setMaintenanceType(VAR_MT_TYPE);
			 log.info("mt type pre : " + mt_type)
			 log.info("mt type pre2 : " + c.getMaintenanceType().value)
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
		 log.info("nomor equip_pindah_new1 : " + c.getEquipmentRef().value)
		 log.info("nomor equip_pindah_new2 : " + c.getEquipmentNo().value)
		 
		 if ((c.getEquipmentRef().value.equals(null) || c.getEquipmentRef().value.equals("")) && (!c.getAccountCode().value.equals(null) && !c.getAccountCode().value.equals(""))) {
			 if (c.getAccountCode().value.length() > 15) {
			  log.info("akun2" +  c.getAccountCode().value.substring(15))
			  AccountCode VAR_CCT = new AccountCode();
			  VAR_CCT.setValue(c.getAccountCode().value.substring(0, 13) + act_code + c.getAccountCode().value.substring(15))
			 c.setAccountCode(VAR_CCT)
			 c.getAccountCode().value.substring(0, 13) + act_code + c.getAccountCode().value.substring(15)
			 }
			 else {
				 AccountCode VAR_CCT = new AccountCode();
				 VAR_CCT.setValue(c.getAccountCode().value)
				 c.setAccountCode(VAR_CCT)
			 }
			}
		  else {
		 
		 Constraint c3
		 MSF600Key msf600key = new MSF600Key()
		 if (!c.getEquipmentRef().value.equals(null)) {
		  c3 = MSF600Key.equipNo.equalTo(c.getEquipmentRef().value)
		 } else {
		  c3 = MSF600Key.equipNo.equalTo(c.getEquipmentNo().value)
		 }
		 
		 Query query2 = new QueryImpl(MSF600Rec.class).and(c3)
		 
		 MSF600Rec msf600Rec = tools.edoi.firstRow(query2)
		 if (msf600Rec != null){
		 
		 log.info("null1")
		 String account_code =  msf600Rec.getAccountCode()
		 String plant_no123 =  msf600Rec.getPlantNo()
		 log.info("plant_no123 : " + plant_no123)
		 log.info("Cost Centre_new1: ${account_code}")
		 AccountCode VAR_CCT = new AccountCode();
		 VAR_CCT.setValue(account_code.substring(0, 13)+act_code)
		 c.setAccountCode(VAR_CCT)
		 log.info("set AccontCode_NEW2_12Feb_new1: " + account_code.substring(0, 13)+act_code)
						 
		 } else {
					 Constraint c4 = MSF600Key.equipNo.greaterThan(" ")
					 Constraint c5 = MSF600Rec.plantNo.equalTo(c.getEquipmentRef().value)
					 Query query3 = new QueryImpl(MSF600Rec.class).and(c4).and(c5)
 
						
				 MSF600Rec msf600Recb = tools.edoi.firstRow(query3)
					if (msf600Recb != null){
		 
					  log.info("null2")
		 
				  String account_code =  msf600Recb.getAccountCode()
				  log.info("Cost Centre_new1: ${account_code}")
				  AccountCode VAR_CCT = new AccountCode();
				  VAR_CCT.setValue(account_code.substring(0, 13)+act_code)
				  c.setAccountCode(VAR_CCT)
				  log.info("set AccontCode_NEW2_12Feb_new1: " + account_code.substring(0, 13)+act_code)
						 
					   }
				}
		  }
		  
		  //Nota Dinas No. B1013082
		  String project_no = c.getProjectNo().value
		  if(project_no != null){
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
					if(act_code!="10" && act_code!="11"){
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
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks onPostExecute WorkOrderService_create version: ${hookVersion}")
		//FMFC-START
		WorkServiceResult ServRes = (WorkServiceResult) result;
		WorkDTO c = new WorkDTO();
		c = ServRes.getWorkDTO()
		if (!c.equals(null)) {
			String strREQ_ID = c.getRequestId().getValue();
			String strWO = "";
			/*
			if (c.getWorkOrder().getPrefix().equals(null)){
				strWO = c.getWorkOrder().getNo();
			}else{
				strWO = c.getWorkOrder().getPrefix() + c.getWorkOrder().getNo();
			}
			*/
			strWO = c.getWorkOrder().getValue();
			
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
