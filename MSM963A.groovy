/**
* @EMS 2014
*
* Revision History
* 02-Des-2014.............a9ms6435 adding "#" for account +ACC
* 04-Jul-2014.............a9ep6434 Initial Non User Exit- MSM963A
* .................................include amount +DGT
* */


import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.eql.impl.*
import groovy.sql.Sql
import com.mincom.eql.*
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.System
import java.util.regex.*;
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.script.util.CommAreaScriptWrapper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


public class MSM963A extends MSOHook {
   private CommAreaScriptWrapper commarea
   DecimalFormat formatter
   String hookVersion = "MSM963A 27 NOV 003"
   String currAcc,currAcc2,accplus, hash, periodFrom, full, periodTo, postLevel,yearFrom, paramBetweenFrm, paramBetweenTo
   String accDisplay, amountDisplay, budgetDisplay, totalDisplay
   Double bFwd,total = 0
   Double ActVal = 0
   Sql sqla
   int lebar,lebarAcc,selisihLebar
  
   
   @Override
   public GenericMsoRecord onDisplay(GenericMsoRecord screen){
	   formatter = new DecimalFormat("###.##")
	   log.info("Hooks onDisplay logging.version: ${hookVersion}")
	
	   for(int i=2;i <= 15;i++){
		   log.info("nilai i"+i)
		  
		   if(screen.getField("ROW1I" + "" + i).getValue().length() >= 24){
			   currAcc =  screen.getField("ROW1I" + "" + i).getValue().substring(0,24).trim()
			   full=screen.getField("ROW1I" + "" + i).getValue()
			   log.info("masuk if -1 : dng accnt: "+ currAcc)
			   
				if (currAcc.indexOf(" ") != -1)
				{
					
					currAcc = currAcc.substring(0, currAcc.indexOf(" "))
					lebar=screen.getField("ROW1I" + "" + i).getValue().length()
					lebarAcc= currAcc.length()
					selisihLebar= lebar-lebarAcc
			   
			   log.info("NILAI LENGTH currAcc :"+lebarAcc)
			   log.info("NILAI currAcc :"+currAcc)
			   log.info("NILAI lenght currAcc :"+lebar)
			   log.info("NILAI selisih lebar :"+selisihLebar)
			   
			   log.info("ori row ke-${i}:" + screen.getField("ROW1I" + "" + i).getValue())
			   
			   Constraint key010_1 = MSF010Key.tableType.equalTo("+ACC")
			   Constraint key010_2 = MSF010Key.tableCode.equalTo(currAcc)
			   
			   

			   QueryImpl query010_checkACC = new QueryImpl(MSF010Rec.class).and(key010_1).and(key010_2)
			   
			   MSF010Rec msf010rec_checkACC = tools.edoi.firstRow(query010_checkACC)
			   
			   if(msf010rec_checkACC != null) {
				   log.info("account is exist on +ACC")
				  
				   lebar=screen.getField("ROW1I" + "" + i).getValue().length()
				   lebarAcc= currAcc.length()
				   selisihLebar= lebar-lebarAcc
				   
				   accplus =  currAcc
				   log.info("accplus 1 :"+accplus )
				   hash = screen.getField("ROW1I" + "" + i).getValue().substring(lebarAcc+2,lebar)
				   accplus = accplus + " "+"#"
				   log.info("hash :"+hash )
				   log.info("hash value :"+full )
				   log.info("accplus 2 :"+accplus )
				   
				   screen.getField("ROW1I" + "" + i).setIsProtected(false)
				   screen.getField("ROW1I" + "" + i).setValue(accplus+hash)
				   screen.getField("ROW1I" + "" + i).setIsProtected(true)
				   
					   
				  
			   }
			   
		   }
				else {
					currAcc =  screen.getField("ROW1I" + "" + i).getValue().substring(0,24).trim()
					lebar=screen.getField("ROW1I" + "" + i).getValue().length()
					lebarAcc= currAcc.length()
					selisihLebar= lebar-lebarAcc
					
					Constraint key010_1 = MSF010Key.tableType.equalTo("+ACC")
					Constraint key010_2 = MSF010Key.tableCode.equalTo(currAcc)
										
	 
					QueryImpl query010_checkACC = new QueryImpl(MSF010Rec.class).and(key010_1).and(key010_2)
					
					MSF010Rec msf010rec_checkACC = tools.edoi.firstRow(query010_checkACC)
					
					if(msf010rec_checkACC != null) {
						log.info("-else- account is exist on +ACC")
					  
						
						accplus =  currAcc
						log.info("accplus 1 :"+accplus )
						hash = screen.getField("ROW1I" + "" + i).getValue().substring(lebarAcc+2,lebar)
							
						accplus = accplus + " "+"#"
						log.info("hash :"+hash )
						log.info("hash value :"+full )
						log.info("accplus 2 :"+accplus )
						
						screen.getField("ROW1I" + "" + i).setIsProtected(false)
						screen.getField("ROW1I" + "" + i).setValue(accplus+hash)
						screen.getField("ROW1I" + "" + i).setIsProtected(true)
						
							
					   
					}
					
					
					}
			   }
		   
				 
		   
	   }
	  
	   return screen
   }

}
