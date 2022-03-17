/**
 * @EMS June 2018
 *
 * a9ra5213 - Inventory Asset PJB
 **/

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3140.receipttask.ReceiptTaskService
import com.mincom.ellipse.types.m0000.instances.StockAdjType_121;
import com.mincom.ellipse.types.m3140.instances.ReceiptTaskDTO;
import com.mincom.ellipse.types.m3140.instances.ReceiptTaskReceiveDTO;
import com.mincom.ellipse.types.m3140.instances.ReceiptTaskReceiveServiceResult;
import com.mincom.ellipse.types.m0000.instances.StockCode
import com.mincom.ellipse.types.m0000.instances.TaskId
import com.mincom.ellipse.types.m0000.instances.EmployeeId
import com.mincom.ellipse.types.m3140.instances.UninstallCompleteHoldingsDetailsDTO
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.*
import com.mincom.ellipse.ejra.mso.*;
import com.mincom.ellipse.client.connection.*
import org.apache.commons.lang.StringEscapeUtils

import javax.naming.InitialContext

import groovy.sql.Sql;

class ReceiptTaskService_receive extends ServiceHook{
	String hookVersion = "1";
	String ProjAcct = "";
	String StrDstrct = "";
	String StrSTK = "";
	String StrTaskID = "";
	Boolean LOOPFLAG = false;
	Boolean ErrFlag = false;;
	String ErrorMessage = "";
	String StrFKEYS = "";
	BigDecimal GROS_PR = 0;
	BigDecimal REC_QTY = 0;
	String EMP_REC = "";
	String PO_NO = "";
	def QueryRes6;

	InetAddress ip = InetAddress.getLocalHost()
	String hostname = ip.getHostName()
	def postUrl

	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	EllipseScreenService screenService = EllipseScreenServiceLocator.ellipseScreenService;
	ConnectionId msoCon = ConnectionHolder.connectionId;
	GenericMsoRecord screen = new GenericMsoRecord();
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks ReceiptTaskService_receive onPreExecute logging.version: ${hookVersion}");
		ReceiptTaskReceiveDTO c = (ReceiptTaskReceiveDTO) input
		TaskId  TSK_ID = new TaskId()
		TSK_ID = c.getTaskId()
		EmployeeId EMP_ID = new EmployeeId()
		EMP_ID = c.getReceivedBy()
		StrTaskID = TSK_ID.getValue()
		log.info("StrTaskID : " + StrTaskID);
		EMP_REC = EMP_ID.getValue();
		log.info("EMP_REC : " + EMP_REC);
		if (!StrTaskID.equals(null)){
			String QuerySelect = ("select STOCK_CODE from msf1th where trim(TASK_ID) = '"+StrTaskID.trim()+"'");
			log.info("QuerySelect : " + QuerySelect);
			def QueryRes1 = sql.firstRow(QuerySelect);
			log.info("QueryRes1 : " + QueryRes1);
			if (!QueryRes1.equals(null)){
				if (!QueryRes1.STOCK_CODE.equals(null)){
					StrSTK = QueryRes1.STOCK_CODE
				}
			}
			
			StrDstrct = tools.commarea.District;
			log.info("StrSTK : " + StrSTK);
			log.info("StrDstrct : " + StrDstrct);
			
			String QuerySelect6 = ("select a.PO_NO,a.PO_ITEM_NO,to_char(a.GROSS_PRICE_P * b.TASK_QTY_REC_9) TOTAL_REC_DEB,to_char(-1 * a.GROSS_PRICE_P * b.TASK_QTY_REC_9) TOTAL_REC_CRE " +
				"from msf221 a " +
				"left outer join msf1tr b on (b.TASK_ID = '"+StrTaskID+"') " +
				"where (a.PO_NO,a.PO_ITEM_NO) in ( " +
				"select substr(DOCUMENT_KEY,1,6) PO_NO,substr(DOCUMENT_KEY,7,3) PO_NO_ITEM from MSF1FH where " +
				"TASK_ID = '"+StrTaskID+"' and DOCMNT_TYPE = 'PO') and a.DSTRCT_CODE = '"+StrDstrct+"'");
			QueryRes6 = sql.firstRow(QuerySelect6);
			
		}
		return null;
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		
		log.info("Hooks ReceiptTaskService_receive onPostExecute logging.version: ${hookVersion}")

		ReceiptTaskReceiveDTO inp = (ReceiptTaskReceiveDTO)input

		log.info("Arsiadi --- request: $inp")
		log.info("Arsiadi --- request size: ${inp.getCompleteholdingdetailsdto().size()}")

		inp.getCompleteholdingdetailsdto().eachWithIndex{ UninstallCompleteHoldingsDetailsDTO entry, int i ->
			log.info("Arsiadi --- Processing Entry: $entry")

			log.info("i: $i")

			String districtCode = tools.commarea.District
			String documentKey = inp.getDocumentKey().getValue() ? inp.getDocumentKey().getValue().trim() : ""
			String documentType = inp.getDocumentType().getValue() ? inp.getDocumentType().getValue().trim() : ""
			String orderNo = documentKey != "" ? documentKey.substring(0, 6).trim() : ""
			String poItemNo = documentKey != "" ? documentKey.substring(6).trim() : ""
			String stockCode = entry.getStockCode().getValue() ? entry.getStockCode().getValue().trim() : ""
			String nonStockStatus = entry.getNonStockStatus().getValue() ? entry.getNonStockStatus().getValue().trim() : ""
			String unitOfMeasure = inp.getUnitOfMeasure().getValue() ? inp.getUnitOfMeasure().getValue().trim() : ""
			String inventoryCategory = inp.getInventoryCategory().getValue() ? inp.getInventoryCategory().getValue().trim() : ""
			String inventCat = inventoryCategory != "" ? inventoryCategory : "K1"
			BigDecimal quantity = entry.getQuantity().getValue() ? entry.getQuantity().getValue() : 0
			BigDecimal soh = entry.getStockOnHand().getValue() ? entry.getStockOnHand().getValue() : 0

			String receivingWarehouseId = inp.getReceivingWarehouseId().getValue() ? inp.getReceivingWarehouseId().getValue().trim() : ""
			String queryMsf170 = "SELECT * FROM MSF170 WHERE DSTRCT_CODE = '$districtCode' AND STOCK_CODE = '$stockCode'"
			def queryMsf170result = sql.firstRow(queryMsf170)
			String homeWarehouse = queryMsf170result ? queryMsf170result.HOME_WHOUSE.trim() : ""
			String warehouseId = receivingWarehouseId != "" ? receivingWarehouseId : homeWarehouse != "" ? homeWarehouse : ""

			String queryMSF100 = "SELECT STK_DESC FROM MSF100 WHERE STOCK_CODE = '${stockCode.trim()}'"
			def queryMSF100Result = sql.firstRow(queryMSF100)
			String stockDesc = queryMSF100Result ? queryMSF100Result.STK_DESC.trim() : ""
			String stockDescription = stockDesc != "" ? stockDesc.trim().length() > 50 ? stockDesc.substring(0, 50) : stockDesc.trim() : ""
			stockDescription = StringEscapeUtils.escapeXml(stockDescription)

			log.info("---districtCode: ${tools.commarea.District}")
			log.info("---documentKey: ${inp.getDocumentKey().getValue()}")
			log.info("---documentType: $documentType")
			log.info("---poNo: $orderNo")
			log.info("---poItemNo: $poItemNo")
			log.info("---stockCode: $stockCode")
			log.info("---quantity: $quantity")
			log.info("---nonStockStatus: $nonStockStatus")
			log.info("---unitOfMeasure: $unitOfMeasure")
			log.info("---inventoryCategory: $inventoryCategory")
			log.info("---inventCat: $inventCat")
			log.info("---receivingWarehouseId: $receivingWarehouseId")
			log.info("---homeWarehouse: $homeWarehouse")
			log.info("---warehouseId: $warehouseId")
			log.info("---stockDescription: $stockDescription")
			log.info("---soh: $soh")

			String districtFormatted
			if (districtCode){
				districtFormatted = districtCode.trim().substring(2) == "PT" ||
						districtCode.trim().substring(2) == "IN" ||
						districtCode.trim().substring(2) == "RB" ||
						districtCode.trim().substring(2) == "PC" ||
						districtCode.trim().substring(2) == "TA" ||
						districtCode.trim().substring(2) == "MK" ||
						districtCode.trim().substring(2) == "MT" ? districtCode.trim().substring(2) :
						districtCode.trim() == "SGRK" ? "GR" : "PLNUPJB"
			}

			String queryGetStockAvail = "with b as (\n" +
					"select b.DSTRCT_CODE,a.STOCK_CODE,sum(a.SOH) SOH\n" +
					"from  msf1hd a \n" +
					"left outer join msf1cs b on (a.CUSTODIAN_ID = b.CUSTODIAN_ID)\n" +
					"left outer join msf1hb c on (a.CUSTODIAN_ID = c.CUSTODIAN_ID and a.STOCK_CODE = c.STOCK_CODE and c.PRIME_LOCATION = '1')\n" +
					"where a.HOLDING_TYPE = 'F' \n" +
					"and a.STK_OWNERSHP_IND = 'O'\n" +
					"group by b.DSTRCT_CODE,a.STOCK_CODE),\n" +
					"c as (\n" +
					"select b.DSTRCT_CODE,a.STOCK_CODE,sum(a.SOH) SOH\n" +
					"from  msf1hd a \n" +
					"left outer join msf1cs b on (a.CUSTODIAN_ID = b.CUSTODIAN_ID)\n" +
					"left outer join msf1hb c on (a.CUSTODIAN_ID = c.CUSTODIAN_ID and a.STOCK_CODE = c.STOCK_CODE and c.PRIME_LOCATION = '1')\n" +
					"where a.HOLDING_TYPE = 'F' and a.STK_OWNERSHP_IND = 'C'\n" +
					"group by b.DSTRCT_CODE,a.STOCK_CODE),\n" +
					"d as (select \n" +
					"a.DSTRCT_CODE,a.STOCK_CODE,sum(a.QTY_REQ)- (sum(a.QTY_ISSUED) + sum(a.QTY_ISSUED_CON)) NEW_DUES_OUT from MSF141 a\n" +
					"join msf140 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.ireq_no = b.ireq_no and B.AUTHSD_STATUS = 'A') \n" +
					"where a.ITEM_141_STAT not in ('9','8','0','6','4')\n" +
					"group by\n" +
					"a.STOCK_CODE,a.DSTRCT_CODE)\n" +
					"select a.DSTRCT_CODE\n" +
					"      ,a.STOCK_CODE\n" +
					"      ,case when b.SOH is null then 0 else b.SOH end SOH\n" +
					"      ,case when c.SOH is null then 0 else c.SOH end CONSIGN_SOH\n" +
					"      ,case when NEW_DUES_OUT is not null then new_dues_out else 0 end AS DUES_OUT\n" +
					"from msf170 a \n" +
					"left outer join b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE)\n" +
					"left outer join c on (a.DSTRCT_CODE = c.DSTRCT_CODE and a.STOCK_CODE = c.STOCK_CODE)\n" +
					"left outer join d on (a.DSTRCT_CODE = d.DSTRCT_CODE and a.STOCK_CODE = d.STOCK_CODE)\n" +
					"where a.dstrct_code = '$districtCode'\n" +
					"and a.stock_code = '$stockCode'"
			log.info("queryCommand: $queryGetStockAvail")
			def  queryResult = sql.firstRow(queryGetStockAvail)
			log.info("queryResult: $queryResult")

			BigDecimal stockOnHand = (queryResult ? queryResult.SOH : "0") as BigDecimal
			BigDecimal duesOut = (queryResult ? queryResult.DUES_OUT : "0") as BigDecimal
			BigDecimal available = stockOnHand - duesOut

			log.info("stockOnHand: $stockOnHand")
			log.info("duesOut: $duesOut")
			log.info("qtyReceipt: $quantity")
			log.info("available: $available")

			if (hostname.contains("ellprd"))
			{
				postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-ITEM-XML"
			}
			else if (hostname.contains("elltst"))
			{
				postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-ITEM-XML"
			}
			else
			{
				postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-ITEM-XML"
			}

			String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<SyncMXE-ITEM-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:45:48+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
					"    <MXE-ITEM-XMLSet>\n" +
					"        <ELLIPSEITEM>\n" +
					"            <ITEMNUM>$stockCode</ITEMNUM>\n" +
					"            <DESCRIPTION>$stockDescription</DESCRIPTION>\n" +
					"            <AVAILABLE>$available</AVAILABLE>\n" +
					"            <SOH>$soh</SOH>\n" +
					"            <UOM>$unitOfMeasure</UOM>\n" +
					"            <STOREROOM>$warehouseId</STOREROOM>\n" +
					"            <ORGID>UBPL</ORGID>\n" +
					"            <SITEID>$districtFormatted</SITEID>\n" +
					"            <DISTRICT>$districtCode</DISTRICT>\n" +
					"            <CATEGORY>$inventCat</CATEGORY>\n" +
					"        </ELLIPSEITEM>\n" +
					"    </MXE-ITEM-XMLSet>\n" +
					"</SyncMXE-ITEM-XML>"

			log.info("ARS --- XML: $xmlMessage")

			def url = new URL(postUrl)
			HttpURLConnection connection = url.openConnection()
			connection.setRequestMethod("POST")
			connection.setDoOutput(true)
			connection.setRequestProperty("Content-Type", "application/xml")
			connection.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")
			connection.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
			log.info("responsecode: ${connection.getResponseCode()}")

			if (connection.getResponseCode() != 200) {
				String responseMessage = connection.content.toString()
				log.info("responseMessage: $responseMessage")
				String errorCode = "9999"

				throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
								errorCode, responseMessage, "", 0, 0))
				return null
			}
		}
		
		ErrFlag = false
		
		String QuerySelect2 = ("select ASSET_IND from msf100 where trim(STOCK_CODE) = trim('$StrSTK')")
		def QueryRes2 = sql.firstRow(QuerySelect2)
		log.info("QueryRes2: $QueryRes2")

		if (QueryRes2){
			if (QueryRes2.ASSET_IND){
				if (QueryRes2.ASSET_IND == "Y"){
					invoke_MSO905()
				}
			}
		}

		if (ErrFlag){
			throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999",ErrorMessage, "", 0, 0))
				return input
		}

		return result
	}
	private def MainMSO(){
		while(LOOPFLAG.equals(false)) {
			log.info("MAIN MSO1: ${screen.mapname.trim()}")
			screen.setNextAction(GenericMsoRecord.F3_KEY)

			screen = screenService.execute(msoCon, screen)
			log.info("MAIN MSO2:${screen.mapname.trim()}")

			if ( screen.mapname.trim() == "MSM905A") {
				LOOPFLAG = true
			}
		}
		LOOPFLAG = false
		
	}
	
	private boolean isErrorOrWarning(GenericMsoRecord screen) {
		return ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_ERROR; //|| ((char)screen.errorType) == MsoErrorMessage.ERR_TYPE_WARNING;
	}
	
	private String invoke_MSO905(){
		
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
			screen.setFieldValue("MAN_JNL_NO1I", "");
			String QuerySelect3 = ("select CURR_ACCT_MN||'/'||CURR_ACCT_YR CURR_PERIOD from msf000_cp where DSTRCT_CODE = '"+StrDstrct+"' and CONTROL_REC_NO = '0001'");
			def QueryRes3 = sql.firstRow(QuerySelect3);
			log.info("QueryRes3 : " + QueryRes3);
			if (!QueryRes3.equals(null)){
				if (!QueryRes3.CURR_PERIOD.equals(null)){
					screen.setFieldValue("ACCT_PERIOD1I", QueryRes3.CURR_PERIOD);
				}
			}else{
				ErrFlag = true;
				ErrorMessage = "INVALID PERIOD ON QUERY"
				log.info("Error Message:" + ErrorMessage)
				MainMSO()
				return ErrorMessage
			}
			//screen.setFieldValue("ACCT_PERIOD1I", "12/17");
			
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
			
			//screen.setFieldValue("JOURNAL_DESC1I", "JURNAL OTOMATIS MATERIAL CADANG");
			//log.info("UserID : " + tools.commarea.UserID)
			screen.setFieldValue("ACCOUNTANT1I", EMP_REC);
			screen.setFieldValue("APPROVAL_STAT1I", "Y");
			
			String QuerySelect4 = ("select to_char(sysdate,'YYYYMMDD') TGL from dual");
			def QueryRes4 = sql.firstRow(QuerySelect4);
			log.info("QueryRes4 : " + QueryRes4);
			if (!QueryRes4.equals(null)){
				if (!QueryRes4.TGL.equals(null)){
					screen.setFieldValue("TRANS_DATE1I", QueryRes4.TGL);
				}
			}else{
				ErrFlag = true;
				ErrorMessage = "INVALID DATE QUERY"
				log.info("Error Message:" + ErrorMessage)
				MainMSO()
				return ErrorMessage
			}
			
			String QuerySelect5 = ("select trim(ASSOC_REC) ASSOC_REC from msf010 where TABLE_TYPE = '+IA1' and TABLE_CODE = 'ACCT'");
			def QueryRes5 = sql.firstRow(QuerySelect5);
			log.info("QueryRes5 : " + QueryRes5);
			if (!QueryRes5.equals(null)){
				if (!QueryRes5.ASSOC_REC.equals(null)){
					screen.setFieldValue("ACCOUNT_CODE1I1", QueryRes5.ASSOC_REC);
					screen.setFieldValue("ACCOUNT_CODE1I2", QueryRes5.ASSOC_REC);
				}else{
					ErrFlag = true;
					ErrorMessage = "INVALID QUERY ACCOUNT"
					log.info("Error Message:" + ErrorMessage)
					MainMSO()
					return ErrorMessage
				}
			}else{
				ErrFlag = true;
				ErrorMessage = "INVALID QUERY ACCOUNT"
				log.info("Error Message:" + ErrorMessage)
				MainMSO()
				return ErrorMessage
			}
			
			log.info("QueryRes6 : " + QueryRes6);
			if (!QueryRes6.equals(null)){
				if (!QueryRes6.TOTAL_REC_DEB.equals(null)){
					screen.setFieldValue("JOURNAL_DESC1I", "JURNAL OTOMATIS MAT. CADANG PO : " + QueryRes6.PO_NO.trim());
					screen.setFieldValue("TRAN_AMOUNT1I1", QueryRes6.TOTAL_REC_DEB);
					screen.setFieldValue("TRAN_AMOUNT1I2", QueryRes6.TOTAL_REC_CRE);
				}else{
					ErrFlag = true;
					ErrorMessage = "INVALID QUERY AMOUNT"
					log.info("Error Message:" + ErrorMessage)
					MainMSO()
					return ErrorMessage
				}
			}else{
				ErrFlag = true;
				ErrorMessage = "INVALID QUERY AMOUNT"
				log.info("Error Message:" + ErrorMessage)
				MainMSO()
				return ErrorMessage
			}
			
			screen.setFieldValue("JNL_DESC1I1", "PENERIMAAN MATERIAL CADANG");
			screen.setFieldValue("DOCUMENT_REF1I1", QueryRes6.PO_NO);
			
			screen.setFieldValue("JNL_DESC1I2", "ISSUE MATERIAL CADANG");
			screen.setFieldValue("DOCUMENT_REF1I2", "");
			
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

	String getHostUrl(String hostName){
		String result
		String instance

		InitialContext initialContext = new InitialContext()
		Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
		Sql sql = new Sql(dataSource)


		if (hostName.contains("ellprd")){
			instance = "ellprd"
		}
		else if (hostName.contains("elltrn")){
			instance = "elltrn"
		}
		else if (hostName.contains("elltst")){
			instance = "elltst"
		}
		else {
			instance = "elldev"
		}

		String queryMSF010 = "select table_desc as tableDesc from msf010 where table_type = '+MAX' and table_code = '$instance'"
		Object queryMSF010Result = sql.firstRow(queryMSF010)
		result = queryMSF010Result ? queryMSF010Result.tableDesc ? queryMSF010Result.tableDesc.trim(): "" : ""

		return result
	}
}