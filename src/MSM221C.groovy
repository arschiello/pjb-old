/**
* @EMS 2014
*
* Revision History
* 01-Mar-2014.............a9ms6435 Initial Non User Exit- MSM221C
* ........................
* */

import groovy.lang.Binding;
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.ellipse.ejra.mso.MsoField
import java.lang.Math;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.DecimalFormat;
import java.lang.System
import static java.lang.Math.*;

import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException

public class MSM221C extends MSOHook {

   String hookVersion = "2-MAUL-1MAR-14"

   @Override
   public GenericMsoRecord onDisplay(GenericMsoRecord screen){
	   
	   // onDisplay is only to be used for setting up fields.

	   log.info("Hooks onDisplay logging.version: ${hookVersion}")
	   //Integer qty_to_come=
	   //screen.getField("PREQ_NO1I").getValue().value
	   //Integer qty_to_come = Math.floor(screen.getField("QTY_TO_COME_I3I").getValue().trim().toDouble()).toInteger()
	   Integer qty_to_come = Math.floor(screen.getField("QTY_TO_COME_I3I").getValue().trim().toDouble()).toInteger()
	   screen.getField("QTY_TO_COME_I3I").setIsProtected(false)
	   screen.getField("QTY_TO_COME_I3I").setValue("")
	   screen.getField("QTY_TO_COME_I3I").setValue(qty_to_come.toString())
	   screen.getField("QTY_TO_COME_I3I").setIsProtected(true)
	  
	   // floor (double).valueOf(qty_to_come)
	   log.info("QTY_TO_COME_I3I JAM 15.14: "+ qty_to_come.toString())
	   
	   return null
   }
   
}
