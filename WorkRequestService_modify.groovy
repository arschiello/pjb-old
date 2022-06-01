package PJB_DEPLOY_FMFC
/** 
 *  EMS Hooks Script
 *  Copyright � 2014-2016 EMS Paramitra
 *  All rights reserved.
 **********************************************************************************
 *
 *   Service / Screen       : Work Request
 *   Operation / MSO Option : Modify
 *      
 *   Description            : Hooks Script.
 *         
 *   Revision History
 *   YYYY/MM/DD
 *   2018/11/04......  a9ra5213
 *   ................  add FMFC customisation
 *   2016/03/11......  a9ms6435
 *   ................  -PN703- set Maintenance Type from Standard Job
 *   2016/01/30......  a9ms6435
 *   ................  set Standard Job
 *   2014/07/13        a9ep6434
 *   ................  Initial Coding.
 *   ................  set default region value as district login
 *  
 *  
 ***********************************************************************************
**/

import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import org.slf4j.LoggerFactory;
import com.mincom.batch.environment.*;
import com.mincom.batch.script.*;
import com.mincom.ellipse.script.util.*;
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.script.util.CommAreaScriptWrapper
import com.mincom.enterpriseservice.ellipse.workrequest.WorkRequestServiceModifyReplyDTO
import com.mincom.enterpriseservice.ellipse.workrequest.WorkRequestServiceModifyRequestDTO


class WorkRequestService_modify extends ServiceHook{

    String hookVersion = "1"
	private CommAreaScriptWrapper commarea
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
    public Object onPreExecute(Object input) {
        log.info("Hooks onPreExecute WorkRequestService_modify eko.logging.version: ${hookVersion}")
        
		WorkRequestServiceModifyRequestDTO requestWorkReqModify = (WorkRequestServiceModifyRequestDTO) input
		
		//FMFC-START
		List<Attribute> custAttribs = requestWorkReqModify.getCustomAttributes()
		
		custAttribs.each{Attribute customAttribute ->
			log.info ("Attribute Name = ${customAttribute.getName()}")
			log.info ("Attribute Value = ${customAttribute.getValue()}")
			if (customAttribute.getName().equals(new String("W0_PLAN"))){
				String strREQ_ID = requestWorkReqModify.getRequestId();
				String QueryDel = "delete msf071 where ENTITY_TYPE = 'SSS' and REF_NO = ? and SEQ_NUM = '001' and trim(ENTITY_VALUE) = trim('"+tools.commarea.district+strREQ_ID+"PLAN')";
				log.info ("String Query : " + QueryDel);
				try{
					def QueryRes2 = sql.execute(QueryDel,"001");
					log.info ("QueryRes2 : " + QueryRes2);
				} catch (Exception  e) {
					println "Exception is " + e
				}
				String StrMSSS = customAttribute.getValue();
				if (StrMSSS.equals(null)){
					StrMSSS = " ";
				}
				GetNowDateTime();
				String QueryInsert = (
						"Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('SSS',?,'001','001','" + strCrDT + "','" + strCrTM + "','"+tools.commarea.userId+"','"+StrMSSS+"','            ')");
				try{
					log.info("QueryInsert = : " + QueryInsert);
					def QueryRes1 = sql.execute(QueryInsert,tools.commarea.district+strREQ_ID+"PLAN");
					log.info ("QueryRes1 : " + QueryRes1);
				}catch (Exception  e) {
					log.info("execption Insert = : " + e);
					Errexcpt = e
				}
				if (!Errexcpt.contains("invalid SQL statement") && !Errexcpt.equals("")){
					throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", Errexcpt, "", 0, 0))
	   
						return input;
				}
			}
		}
		//FMFC-END
		
		requestWorkReqModify.setRegion(tools.commarea.district)
		
		if (requestWorkReqModify.getClassification() != null  ){
			if (requestWorkReqModify.getClassification() == "IL"   ){
				requestWorkReqModify.setRiskCode10("CR")	
			}
			
			if (requestWorkReqModify.getClassification() == "RP"   ) {
				requestWorkReqModify.setRiskCode10("PM")
			}
			
			if (requestWorkReqModify.getClassification() == "FP"   ) {
				requestWorkReqModify.setRiskCode10("PD")
			}
			
			if (requestWorkReqModify.getClassification() == "SS"   ) {
				requestWorkReqModify.setRiskCode10("NM")
			}
			
			if (requestWorkReqModify.getClassification() == "FF"   ) {
				requestWorkReqModify.setRiskCode10("EJ")
			}

			if (requestWorkReqModify.getClassification() == "FR"   ) {
				requestWorkReqModify.setRiskCode10("EJ")
			}

			if (requestWorkReqModify.getClassification() == "RO"   ) {
				requestWorkReqModify.setRiskCode10("CR")
			}

			if (requestWorkReqModify.getClassification() == "RV"   ) {
				requestWorkReqModify.setRiskCode10("CR")
			}
			
			if (requestWorkReqModify.getClassification() == "RL"   ) {
				requestWorkReqModify.setRiskCode10("EV")
			}

			if (requestWorkReqModify.getClassification() == "RK"   ) {
				requestWorkReqModify.setRiskCode10("SF")
			}
			
		   //C-061883 Perubahan Konfigurasi WR Rekomendasi RCFA, FDT FMEA, iCore, dan Efisiensi
		   if (requestWorkReqModify.getClassification() == "RE"  ) {
			 	 requestWorkReqModify.setRiskCode10("CR")
		   }
		   
		   if (requestWorkReqModify.getClassification() == "RI"  ) {
				requestWorkReqModify.setRiskCode10("PD")
		   }
						
		}
		
		String riskCode1 = requestWorkReqCreate.getRiskCode1()?: "00"
		String riskCode2 = requestWorkReqCreate.getRiskCode2()?: "00"
		String riskCode3 = requestWorkReqCreate.getRiskCode3()?: "00"
		String riskCode4 = requestWorkReqCreate.getRiskCode4()?: "00"
		String riskCode5 = requestWorkReqCreate.getRiskCode5()?: "00"
		String riskCode6 = requestWorkReqCreate.getRiskCode6()?: "00"
		String riskCode7 = requestWorkReqCreate.getRiskCode7()?: "00"
		String riskCode8 = requestWorkReqCreate.getRiskCode8()?: "00"
		String riskCode9 = requestWorkReqCreate.getRiskCode9()?: "00"
		
		//Mandatory Risk Code (Berdasarkan Notulen Pembahasan Konfigurasi Pengisian Table Risiko WR Pada SIT Ellipse)
		if( riskCode1 == "00" && riskCode2 == "00" && riskCode3 == "00" && riskCode4 == "00" && riskCode5 == "00" && riskCode6 == "00" && riskCode7 == "00" && riskCode8 == "00" && riskCode9 == "00")  
		{
			throw new EnterpriseServiceOperationException(new ErrorMessageDTO("9999", "Risk Code Required", "", 0, 0))
				return dto
		} else {
			//Mencari nilai terbesar dari entryan risk code (Berdasarkan Notulen Pembahasan Konfigurasi Pengisian Table Risiko WR Pada SIT Ellipse)
			def riskCodeList = [riskCode1.toInteger(), riskCode2.toInteger(), riskCode3.toInteger(), riskCode4.toInteger(), riskCode5.toInteger(), riskCode6.toInteger(), riskCode7.toInteger(), riskCode8.toInteger(), riskCode9.toInteger()]
			def riskCodeMax = riskCodeList.max()
			
			//mapping riskcode ke prioritycode (Berdasarkan Notulen Pembahasan Konfigurasi Pengisian Table Risiko WR Pada SIT Ellipse)
			if(riskCodeMax == 1 || riskCodeMax == 2){
				requestWorkReqCreate.setPriorityCode("A")
			} 
			else if(riskCodeMax == 3){
				requestWorkReqCreate.setPriorityCode("B")
			} 
			else if(riskCodeMax == 4){ 
				requestWorkReqCreate.setPriorityCode("EM")
			}
		}
		
		return null
	}

    @Override
    public Object onPostExecute(Object input, Object result) {
		log.info("Hooks onPreExecute WorkRequestService_modify eko.logging.version: ${hookVersion}")
		
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