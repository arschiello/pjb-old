/**
* @EMS 2014
*
* Revision History
* 24-Sept-2014.............a9ms6435 Initial Non User Exit- MSM22CA
* ........................ UPDATE Due Date, Due Site Date when modify
* */


import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.eql.impl.*
import com.mincom.eql.*
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.lang.System
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Key
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Rec
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException

public class MSM22CA extends MSOHook {

   String hookVersion = "v01"
   Calendar javaCalendar = null
   String currentDate
   GregorianCalendar bulan
   String answer
   String StrMonth, StrDay, DumStrMonth, DumStrDay, stockCode,actCtr, poNo, poItemNo
   String dueDate = "", inputtedDate ="", dueDate_22C = "", dueSite_22C = ""
   Integer leadTime
   
   @Override
   public GenericMsoRecord onDisplay(GenericMsoRecord screen){}//end of OnDisplay
   
   @Override
   public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){

	   log.info("Hooks onPreSubmit logging.version: ${hookVersion}")
	   
	   //DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	   

	   //String dateStart = screen.getField("ORDER_DATE1I").getValue()
	   //log.info("Hooks onPreSubmit order date from input: ${dateStart}")
	   
	   //String dateStop = screen.getField("DUE_DATE1I").getValue()
	   //log.info("Hooks onPreSubmit due date from input: ${dateStop}")
	   
	   //dueDate = screen.getField("DUE_DATE1I").getValue()
	   //dueDate = screen.getField("DUE_DATE1I").setValue("20140408")
			  
	   		
		//START
	   // check PO created MSM22C
	   poNo = screen.getField("PO_NO1I").getValue()
	   poItemNo = screen.getField("PO_ITEM_NO1I").getValue()
	   dueDate_22C = screen.getField("DUE_DATE1I").getValue()
	   dueSite_22C = screen.getField("DUE_SITE1I").getValue()
	   
	   if (poItemNo.length() < 3){
			   poItemNo = poItemNo.padLeft(3,'0')
	   }
	   
	   log.info("PO NO:" + poNo)
	   log.info("ITEM NO:" + poItemNo)
	   log.info("dueDate_22C:" + dueDate_22C)
	   log.info("dueSite_22C:" + dueSite_22C)
	   log.info("ACTIVITY CTR:" + actCtr)
	   
	   if(!poNo.trim().equals("")){
		   log.info("browse MSF221")
		   Constraint key221_1 = MSF221Key.poNo.equalTo(poNo)
		   Constraint key221_2 = MSF221Key.poItemNo.equalTo(poItemNo)
		   
		   QueryImpl query221 = new QueryImpl(MSF221Rec.class).and(key221_1).and(key221_2)
		   
		   MSF221Rec msf221rec_1 = tools.edoi.firstRow(query221)
		   log.info("due date:" + dueDate_22C)
		   log.info("current date:" + currentDate)
		  
		   Constraint key071_5 = MSF071Key.entityType.equalTo("L22")
		   Constraint key071_6 = MSF071Key.entityValue.equalTo(poNo+poItemNo)
		   Constraint key071_7 = MSF071Key.refNo.equalTo("001")
		   Constraint key071_8 = MSF071Key.seqNum.equalTo("001")

		   QueryImpl query071_2 = new QueryImpl(MSF071Rec.class).and(key071_5).and(key071_6).and(key071_7).and(key071_8)

		   MSF071Rec msf071rec_2 = tools.edoi.firstRow(query071_2)
		   
		   if (msf071rec_2 == null){
					   MSF071Rec msf071recc = new MSF071Rec()
					   MSF071Key msf071keyc = new MSF071Key()
					   //Key Columns1:
					   msf071keyc.setEntityType("L22")
					   msf071keyc.setEntityValue(poNo+poItemNo)
					   msf071keyc.setRefNo("001")
					   msf071keyc.setSeqNum("001")
					   msf071recc.setPrimaryKey(msf071keyc)
					   //Non-Key Columns:
					   msf071recc.setRefCode(dueDate_22C)
					   tools.edoi.create(msf071recc)
					   log.info("Record MSF071-L22 created:" + dueDate_22C)
				   }else{
						MSF071Rec msf071recc = new MSF071Rec()
						msf071recc = msf071rec_2
		
						//Update
						msf071recc.setRefCode(dueDate_22C)
						tools.edoi.update(msf071recc)
						log.info("Record MSF071-L22 updated:" + dueDate_22C)
				   }
				   if (msf221rec_1 != null ){
					   log.info("Record MSF221 Found")
					   MSF221Rec msf221recb = new MSF221Rec()
					   msf221recb = msf221rec_1
						//Update
					   msf221recb.setOrigDueDate(dueDate_22C)
					   msf221recb.setCurrDueDate(dueDate_22C)
					   msf221recb.setDueSiteDate(dueDate_22C)
					   tools.edoi.update(msf221recb)
					   log.info("MSF221 eckops Updated:" + dueDate)
				   }
		  
				
		   
		  return null
		}
		
   }//end of onPreSubmit
   
   @Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		
		
		
			   log.info("Hooks onPreSubmit logging.version: ${hookVersion}")
			   
			   //DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			   
		
			   //String dateStart = screen.getField("ORDER_DATE1I").getValue()
			   //log.info("Hooks onPreSubmit order date from input: ${dateStart}")
			   
			   //String dateStop = screen.getField("DUE_DATE1I").getValue()
			   //log.info("Hooks onPreSubmit due date from input: ${dateStop}")
			   
			   //dueDate = screen.getField("DUE_DATE1I").getValue()
			   //dueDate = screen.getField("DUE_DATE1I").setValue("20140408")
					  
					   
				//START
			   // check PO created MSM22C
			   poNo = input.getField("PO_NO1I").getValue()
			   poItemNo = input.getField("PO_ITEM_NO1I").getValue()
			   dueDate_22C = input.getField("DUE_DATE1I").getValue()
			   dueSite_22C = input.getField("DUE_SITE1I").getValue()
			   
			   if (poItemNo.length() < 3){
					   poItemNo = poItemNo.padLeft(3,'0')
			   }
			   
			   log.info("PO NO:" + poNo)
			   log.info("ITEM NO:" + poItemNo)
			   log.info("dueDate_22C:" + dueDate_22C)
			   log.info("dueSite_22C:" + dueSite_22C)
			   log.info("ACTIVITY CTR:" + actCtr)
			   
			   if(!poNo.trim().equals("")){
				   log.info("browse MSF221")
				   Constraint key221_1 = MSF221Key.poNo.equalTo(poNo)
				   Constraint key221_2 = MSF221Key.poItemNo.equalTo(poItemNo)
				   
				   QueryImpl query221 = new QueryImpl(MSF221Rec.class).and(key221_1).and(key221_2)
				   
				   MSF221Rec msf221rec_1 = tools.edoi.firstRow(query221)
				   log.info("due date:" + dueDate_22C)
				   log.info("current date:" + currentDate)
				  
				   Constraint key071_5 = MSF071Key.entityType.equalTo("L22")
				   Constraint key071_6 = MSF071Key.entityValue.equalTo(poNo+poItemNo)
				   Constraint key071_7 = MSF071Key.refNo.equalTo("001")
				   Constraint key071_8 = MSF071Key.seqNum.equalTo("001")
		
				   QueryImpl query071_2 = new QueryImpl(MSF071Rec.class).and(key071_5).and(key071_6).and(key071_7).and(key071_8)
		
				   MSF071Rec msf071rec_2 = tools.edoi.firstRow(query071_2)
				   
				  //
				   if (msf071rec_2 == null){
					   MSF071Rec msf071recc = new MSF071Rec()
					   MSF071Key msf071keyc = new MSF071Key()
					   //Key Columns1:
					   msf071keyc.setEntityType("L22")
					   msf071keyc.setEntityValue(poNo+poItemNo)
					   msf071keyc.setRefNo("001")
					   msf071keyc.setSeqNum("001")
					   msf071recc.setPrimaryKey(msf071keyc)
					   //Non-Key Columns:
					   msf071recc.setRefCode(dueDate_22C)
					   tools.edoi.create(msf071recc)
					   log.info("Record MSF071-L22 created:" + dueDate_22C)
				   }else{
						MSF071Rec msf071recc = new MSF071Rec()
						msf071recc = msf071rec_2
		
						//Update
						msf071recc.setRefCode(dueDate_22C)
						tools.edoi.update(msf071recc)
						log.info("Record MSF071-L22 updated:" + dueDate_22C)
				   }
				   if (msf221rec_1 != null ){
					   log.info("Record MSF221 Found")
					   MSF221Rec msf221recb = new MSF221Rec()
					   msf221recb = msf221rec_1
						//Update
					   msf221recb.setOrigDueDate(dueDate_22C)
					   msf221recb.setCurrDueDate(dueDate_22C)
					   msf221recb.setDueSiteDate(dueDate_22C)
					   tools.edoi.update(msf221recb)
					   log.info("MSF221 eckops Updated:" + dueDate)
				   }
				  // 
						
				   
				  
				}
				
		   
		
		return result
		}//end of onPostSubmit
   
}
