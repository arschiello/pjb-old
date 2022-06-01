package TEST

//package PJB

/**
 * @EMS June 2018
 *
 * a9ra5213 - Inventory Asset PJB
 * This customization to create manual journal when receipt the Inventory asset Stock code
 **/

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.table.TableService
import com.mincom.enterpriseservice.ellipse.table.TableServiceRetrieveRequestDTO
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*

import javax.naming.InitialContext

import groovy.sql.Sql;

import com.mincom.ellipse.*
import com.mincom.ellipse.ejra.mso.*;
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.script.util.EROIWrapper;


class TableService_retrieve extends ServiceHook{
	String hookVersion = "1";
	private EROIWrapper eroi;
	/*
	Boolean LOOPFLAG = false;
	Boolean ErrFlag = false;;
	String ErrorMessage = "";
	String StrFKEYS = "";
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	EllipseScreenService screenService = EllipseScreenServiceLocator.ellipseScreenService;
	ConnectionId msoCon = ConnectionHolder.connectionId;
	GenericMsoRecord screen = new GenericMsoRecord();
	*/
	boolean HasMessage = false;
	String StrLogMes = "";
	Integer Ctr = 0;
	String EllipseMsg = "";
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks ContractService_modify onPreExecute logging.version: ${hookVersion}")
		TableServiceRetrieveRequestDTO c = (TableServiceRetrieveRequestDTO) input
		log.info("TableType : " + c.getTableType());
		if (c.getTableType().trim().equals("SR")){
			/*
			EROIWrapper eroi = new EROIWrapper();
			def msslnglnk = eroi.execute('MSSLNG', {msslnglnk ->
			msslnglnk.optionLng = "1"
			msslnglnk.numericVal = 1000
			msslnglnk.lineLength = 50
			})
	
		    log.info("Return Status from mss080 is ${msslnglnk.returnStatus}")
		    */
			
			while (true) {
				Ctr = Ctr + 1;
				log.info("Ctr : " + Ctr.toString())
				Thread.sleep(1000);
				if(Ctr == 60) {
					EllipseMsg = "Time Out";
					log.info("EllipseMsg : " + EllipseMsg)
					break;
				}
			}
		}
		
		return null
	}
	@Override
    public Object onPostExecute(Object input, Object result) {
		
		log.info("Hooks TableService_retrieve onPostExecute logging.version: ${hookVersion}");
		log.info("how_to_invoke_eroi_aka_subroutine")
		
		/*
		TableServiceRetrieveRequestDTO c = (TableServiceRetrieveRequestDTO) input
		log.info("TableType : " + c.getTableType());
		if (c.getTableType().trim().equals("SR")){
			//invoke_MSO905("AAA");
			//if (ErrFlag.equals(true)){
				throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
					"9999",ErrorMessage, "", 0, 0))
					return input
			//}
		}
		*/
		return result;
	}
	/*
	private def MainMSO(){
		while(LOOPFLAG.equals(false)) {
			log.info("MAIN MSO1 :" + screen.mapname.trim())
			screen.setNextAction(GenericMsoRecord.F3_KEY)
			//screen.nextAction = GenericMsoRecord.F3_KEY;
			screen = screenService.execute(msoCon, screen);
			log.info("MAIN MSO2 :" + screen.mapname.trim())
			if ( screen.mapname.trim().equals(new String("MSM905A")) ) {
				LOOPFLAG = true
			}
		}
		LOOPFLAG = false
		
	}
	
	private boolean isErrorOrWarning(GenericMsoRecord screen) {
		return ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_ERROR; //|| ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_WARNING;
	}
	
	private String invoke_MSO905(String PARM_1){
		
		log.info("----------------------------------------------")
		log.info("CALL MSO905")
		log.info("----------------------------------------------")
		String ConID = "";
		screen = screenService.executeByName(msoCon, "MSO905");
		log.info("MSO ID : " + msoCon.getId())
		ErrFlag = false;
		MainMSO()
		if ( screen.mapname.trim().equals(new String("MSM905A")) ) {
			log.info("MSO SCREEN1 : " + screen.mapname.trim())
			
			screen.setFieldValue("OPTION1I", "3");
			screen.setFieldValue("MAN_JNL_NO1I", "TEST01");
			screen.setFieldValue("ACCT_PERIOD1I", "12/17");
			
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isErrorOrWarning(screen) ) {
				ErrFlag = true;
				ErrorMessage = screen.getCurrentCursorField().getName() + " - " + screen.getErrorMessage().getErrorString().toString().trim()
				log.info("Error Message1:" + ErrorMessage)
				MainMSO()
				return ErrorMessage
			}
		}
		
		if ( screen.mapname.trim().equals(new String("MSM906A")) ) {
			log.info("MSO SCREEN2 : " + screen.mapname.trim())
			
			screen.setFieldValue("JOURNAL_DESC1I", "JNL DESC");
			screen.setFieldValue("ACCOUNTANT1I", "ADMIN");
			
			screen.setFieldValue("ACCOUNT_CODE1I1", "AMK311129999960I114");
			screen.setFieldValue("TRAN_AMOUNT1I1", "100");
			screen.setFieldValue("JNL_DESC1I1", "ITEM1");
			screen.setFieldValue("DOCUMENT_REF1I1", "DOC ITEM1");
			
			screen.setFieldValue("ACCOUNT_CODE1I2", "AMK311129999960I114");
			screen.setFieldValue("TRAN_AMOUNT1I2", "-100");
			screen.setFieldValue("JNL_DESC1I2", "ITEM2");
			screen.setFieldValue("DOCUMENT_REF1I2", "DOC ITEM2");
			
			screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
			screen = screenService.execute(msoCon, screen);
			
			if (isErrorOrWarning(screen) ) {
				ErrFlag = true;
				ErrorMessage = screen.getCurrentCursorField().getName() + " - " + screen.getErrorMessage().getErrorString().toString().trim()
				log.info("Error Message2:" + ErrorMessage)
				MainMSO()
				return ErrorMessage
			}
			
			StrFKEYS = screen.getFunctionKeyLine().getValue()
			log.info("StrFKEYS : " + StrFKEYS)
			if (StrFKEYS.trim().equals("XMIT-Confirm") || StrFKEYS.trim().equals("XMIT-Confirm, F5-Previous Screen") || StrFKEYS.trim().equals("XMIT-Validate, F5-Previous Screen")){
				screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
				screen = screenService.execute(msoCon, screen);
				
				if (isErrorOrWarning(screen) ) {
					ErrFlag = true;
					ErrorMessage = screen.getCurrentCursorField().getName() + " - " + screen.getErrorMessage().getErrorString().toString().trim()
					log.info("Error Message3:" + ErrorMessage)
					MainMSO()
					return ErrorMessage
				}
			}
			if ( screen.mapname.trim().equals(new String("MSM906A")) ) {
				log.info("MSO SCREEN3 : " + screen.mapname.trim())
				screen.nextAction = GenericMsoRecord.TRANSMIT_KEY;
				screen = screenService.execute(msoCon, screen);
				
				if (isErrorOrWarning(screen) ) {
					ErrFlag = true;
					ErrorMessage = screen.getCurrentCursorField().getName() + " - " + screen.getErrorMessage().getErrorString().toString().trim()
					log.info("Error Message4:" + ErrorMessage)
					MainMSO()
					return ErrorMessage
				}
			}
		}
		
		log.info("SCREEN MESSAGE: " + screen.getErrorMessage().getErrorString().toString().trim())
		MainMSO()
		log.info("MSO SCREEN LAST: " + screen.mapname.trim())
		log.info ("-----------------------------");
	}
	*/
}