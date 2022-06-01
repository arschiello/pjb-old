/**
 * @EMS 2014
 *
 * Revision History
 * 01-Mar-2014.............a9aa6024 Initial Non User Exit- MSM688A
 * ........................
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
import java.lang.System

import com.mincom.ellipse.edoi.ejb.msf000.MSF000_PCYYMMKey
import com.mincom.ellipse.edoi.ejb.msf000.MSF000_PCYYMMRec
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
 import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
 import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
 
 public class MSM570A extends MSOHook {
 
	String hookVersion = "29-Dec-14 11:38"
    String dstrctCode , startDate , endDate , subAsset  ,retireTy,dispDate ,depTy,depTy2 ,dispAmtS, receiptDate , actDispTx
	String addDeprBk , addDeprTx , deprMeth ,CurrentPer ,acctProf
	BigDecimal actDispVal , dispAmt
	     @Override
    public GenericMsoRecord onDisplay(GenericMsoRecord screen){

        // onDisplay is only to be used for setting up fields.

        log.info("Hooks onDisplay logging.version: ${hookVersion}")
					DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date nowdate = new Date();
			String dateNow = dateFormat.format(nowdate)
			log.info("now"+ dateNow )
			log.info("now" + nowdate)

		receiptDate = screen.getField("RECEIPT_DATE1I").getValue()
		
		log.info("receipt Date is ${receiptDate}")
		
		//screen.getField("RECEIPT_DATE1I").setValue("")
		
        // Override screen field value which will be displayed to the user
        
        //protect field (read-only)
        
        //return the screen which will be displayed (includes the updated field values)
        return null
    }
 @Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
      
		log.info("Hooks onPreSubmit logging.version: ${hookVersion}")
		
		dstrctCode = tools.commarea.District
		def mss002lnk = tools.eroi.execute('MSS002', {mss002lnk ->
			mss002lnk.dstrctCode = dstrctCode
			mss002lnk.module = '3680'
			mss002lnk.optionX = '2'
		})
		CurrentPer =  mss002lnk.glCodeDstrct
		
		//CurrentPer = tools.commarea.3680CP
		log.info("CurrentPer2 is ${CurrentPer}")
		receiptDate = screen.getField("RECEIPT_DATE1I").getValue()
		
		log.info("receipt Date is ${receiptDate}")
     	if (receiptDate != "") {
				 Constraint key000_1 = MSF000_PCYYMMKey.dstrctCode.equalTo(dstrctCode)
		Constraint key000_2 = MSF000_PCYYMMKey.controlRecType.equalTo("PC")
		Constraint key000_3 = MSF000_PCYYMMKey.controlRecNo.equalTo(CurrentPer)
		
		QueryImpl query000 = new QueryImpl(MSF000_PCYYMMRec.class).and(key000_1).and(key000_2).and(key000_3)

		MSF000_PCYYMMRec msf000rec_1 = tools.edoi.firstRow(query000)
		
		 if (msf000rec_1 != null){
			 MSF000_PCYYMMRec msf000recb = new MSF000_PCYYMMRec()
			 msf000recb = msf000rec_1
			 endDate = msf000recb.getThisMeDate()
			 startDate =  msf000recb.getPcStartDate()
			log.info("startDate : ${startDate}" )
			log.info("endDate : ${endDate}" )
			 if ( receiptDate < startDate ||  receiptDate > endDate ) {
			    screen.setErrorMessage(new MsoErrorMessage("test 1", "0131", "RECEIPT DATE MUST BE IN CURRENT PERIOD,  Current Period = ${CurrentPer} ", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
				
							//need to return screen if there is any error as we do not want to run the business logic when there is any error
							return screen				
			       }
			 	 }
		   }
				
		 
	}		 
			 						
	}
		

	
 
 

