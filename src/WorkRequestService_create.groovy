/** 
 *  EMS Hooks Script
 *  Copyright � 2014-2016 EMS Paramitra
 *  All rights reserved.
 **********************************************************************************
 *
 *   Service / Screen       : Work Request
 *   Operation / MSO Option : Create
 *      
 *   Description            : Hooks Script.
 *         
 *   Revision History
 *   YYYY/MM/DD
 *   2016/03/11......  a9ms6435
 *   ................  -PN703- set Maintenance Type from Standard Job
 *   2016/01/30......  a9ms6435
 *   ................  set Standard Job
 *   2014/07/13......  a9ep6434
 *   ................  Initial Coding.
 *   ................  set default region value as district login
 *  
 *  
 ***********************************************************************************
**/

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.msocustom.type.Protected;
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
import com.mincom.ellipse.types.m0000.instances.DisabledInd
import com.mincom.ellipse.types.m0000.instances.ProtectField;
import com.mincom.enterpriseservice.ellipse.workrequest.WorkRequestServiceCreateReplyDTO
import com.mincom.enterpriseservice.ellipse.workrequest.WorkRequestServiceCreateRequestDTO


class WorkRequestService_create extends ServiceHook{

    String hookVersion = "3.1"
	String klasif 
	  private CommAreaScriptWrapper commarea
	
    @Override
    public Object onPreExecute(Object input) {
        log.info("Hooks onPreExecute WorkRequestService_create maul.logging.version: ${hookVersion}")
        
		WorkRequestServiceCreateRequestDTO requestWorkReqCreate = (WorkRequestServiceCreateRequestDTO) input
		requestWorkReqCreate.setRegion(tools.commarea.district)
		
		if (requestWorkReqCreate.getClassification() != null  ){

			if (requestWorkReqCreate.getClassification() == "IL"   ){
				requestWorkReqCreate.setRiskCode10("CR")			
			}
			
			if (requestWorkReqCreate.getClassification() == "RP"  ) {
				requestWorkReqCreate.setRiskCode10("CR") 
			}	
			
			if (requestWorkReqCreate.getClassification() == "FP"   ) {
				requestWorkReqCreate.setRiskCode10("PD")
			}
			
			if (requestWorkReqCreate.getClassification() == "SS"  ) {
				requestWorkReqCreate.setRiskCode10("NM")
			}

           if (requestWorkReqCreate.getClassification() == "FF"  ) {
				requestWorkReqCreate.setRiskCode10("EJ")
            }

           if (requestWorkReqCreate.getClassification() == "FR"  ) {
				requestWorkReqCreate.setRiskCode10("EJ")
            }

            if (requestWorkReqCreate.getClassification() == "RO"  ) {
				requestWorkReqCreate.setRiskCode10("CR") 
            }

            if (requestWorkReqCreate.getClassification() == "RV"  ) {
				requestWorkReqCreate.setRiskCode10("CR")				
             }
						
			if (requestWorkReqCreate.getClassification() == "RL"  ) {
			 	 requestWorkReqCreate.setRiskCode10("EV")		
             }
			
			if (requestWorkReqCreate.getClassification() == "RK"  ) {
 				 requestWorkReqCreate.setRiskCode10("SF")
			}
						
		   //C-061883 Perubahan Konfigurasi WR Rekomendasi RCFA, FDT FMEA, iCore, dan Efisiensi
		   if (requestWorkReqCreate.getClassification() == "RE"  ) {
			 	 requestWorkReqCreate.setRiskCode10("CR")
		   }
		   
		   if (requestWorkReqCreate.getClassification() == "RI"  ) {
				requestWorkReqCreate.setRiskCode10("PD")
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
		log.info("Hooks onPreExecute WorkRequestService_Create eko.logging.version: ${hookVersion}")
		
	  return null
    }
	
}
