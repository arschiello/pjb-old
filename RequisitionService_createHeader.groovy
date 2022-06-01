/**
 * a9ra5213 - EMS Jan 2016
 *
 * Initial Coding
 **/

import static java.util.UUID.randomUUID

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.exception.*
import com.mincom.ellipse.errors.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceCreateHeaderRequestDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceCreateHeaderReplyDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceReadReplyDTO
import com.mincom.batch.environment.*;
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.eql.Constraint
import com.mincom.eql.StringConstraint
import com.mincom.eql.impl.QueryImpl
 
class RequisitionService_createHeader extends ServiceHook{
	
	String hookVersion = "1"
	String uuid,uuid2,RandUUID;
	String entity_key;
	String ProjNo = ""
	Integer FindLine
	String BypassFlag
	
	public def CreateFile() {
		try {
			log.info("CreateFile : ")
			String content = "1.HEADER_UUID : \n2.ITEM_UUID : \n";
			BatchEnvironment env = new BatchEnvironment()
			File file = new File(env.getWorkDir().toString()+"/PBC." + entity_key);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();
				log.info ("File Created:" + env.getWorkDir().toString()+"/PBC." + entity_key)
			}
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return ""
	}
	
	public def CekUUID() {
		Boolean ResetFlag
		try {
			log.info("CekUUID : ")
			ResetFlag = true
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File(env.getWorkDir().toString()+"/PBC." + entity_key)
			def lines = checkFile1.readLines()
			uuid = checkFile1.readLines().get(0).substring(16)
			uuid2 = checkFile1.readLines().get(1).substring(14)
			log.info("uuid : ${uuid}");
			log.info("uuid2 : ${uuid2}");
			if (uuid == uuid2 && !uuid.trim().equals("") && !uuid2.trim().equals("")){
				ResetFlag = false
			}
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return ResetFlag
	}
	
	public def SetHeaderUUID() {
		try {
			log.info("SetHeaderUUID : ")
			String ChangeLine
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File(env.getWorkDir().toString()+"/PBC." + entity_key)
			RandUUID = (randomUUID() as String).toUpperCase();
			if(checkFile1.exists()){
				ChangeLine = checkFile1.readLines().get(0).substring(0)
				log.info ("UUID : " + uuid)
				log.info ("ChangeLine : " + ChangeLine)
				if (uuid == null || uuid.equals("") || ChangeLine.equals("1.HEADER_UUID : ")){
					ScanLine(env.getWorkDir().toString()+"/PBC." + entity_key,"1.HEADER_UUID : ")
					ChangeLineText("1.HEADER_UUID : " + RandUUID,FindLine)
					log.info ("NEW UUID : " + RandUUID)
				}
			}
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return ""
	}
	
	public def ScanLine(String Path,String StrFind) {
		try {
			File file = new File(Path);
			Scanner scanner = new Scanner(file);
			
			//now read the file line by line...
			int lineNum = 0;
			FindLine = 0
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				lineNum++;
				if(line.contains(StrFind)) {
					FindLine = lineNum
					log.info ("Line Find " + FindLine);
				}
			}
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return FindLine
	}
	
	public def ChangeLineText(String NewLine,int row) {
		try {
			log.info ("RESET LINE")
			String ChangeLine
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File(env.getWorkDir().toString()+"/PBC." + entity_key)
			ChangeLine = checkFile1.readLines().get(row - 1).substring(0)
			BufferedReader file = new BufferedReader(new FileReader(env.getWorkDir().toString()+"/PBC." + entity_key));
			String line;
			String tmp = "";
			while ((line = file.readLine()) != null) tmp += line + '\n';
			file.close();
			
			tmp = tmp.replace(ChangeLine, NewLine);
			FileOutputStream fileOut = new FileOutputStream(env.getWorkDir().toString()+"/PBC." + entity_key);
			fileOut.write(tmp.getBytes());
			fileOut.close();
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return ""
	}
	
	public def InsertLineFile(String StrLine) {
		try {
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File(env.getWorkDir().toString()+"/PBC." + entity_key)
			checkFile1 << StrLine
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return ""
	}
		
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks onPreExecute RequisitionService_createHeader logging.version: ${hookVersion}");
		RequisitionServiceCreateHeaderRequestDTO inputDTO = (RequisitionServiceCreateHeaderRequestDTO) input
		
		if (inputDTO.getPreqNo() == null || inputDTO.getPreqNo().trim() == ''){
			entity_key = inputDTO.getDistrictCode() + inputDTO.getIreqType() + inputDTO.getIreqNo();
		}else{
			entity_key = inputDTO.getDistrictCode() + inputDTO.getIreqType() + inputDTO.getPreqNo();
		}
		log.info("entity_key : ${entity_key}");
		
		if (!inputDTO.getWorkOrderA().toString().trim().equals("") || !inputDTO.getProjectA().toString().trim().equals("")){
			if (inputDTO.getWorkOrderA() != null && !inputDTO.getWorkOrderA().toString().trim().equals("")){
				try {
					WorkOrderServiceReadReplyDTO WKReadReply = tools.service.get('WorkOrder').read({
						it.districtCode = inputDTO.getDistrictCode()
						it.workOrder = inputDTO.getWorkOrderA()
					})
					ProjNo = WKReadReply.getProjectNo()
				}catch (EnterpriseServiceOperationException e){
					List <ErrorMessageDTO> listError = e.getErrorMessages()
					listError.each{ErrorMessageDTO errorDTO ->
						log.info ("Erorr Code:" + errorDTO.getCode())
						log.info ("Error Message:" + errorDTO.getMessage())
						log.info ("Error Fields: " + errorDTO.getFieldName())
					}
				}
				if (ProjNo != null){
					if (ProjNo.trim().equals("")){
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
							"9999", "Nomor PRK Belum di Input Pada WO : " + inputDTO.getWorkOrderA().toString(), "workOrderA", 0, 0,),)
						return input
					}
				}else{
					throw new EnterpriseServiceOperationException(
					new ErrorMessageDTO(
						"9999", "Nomor PRK Belum di Input Pada WO : " + inputDTO.getWorkOrderA().toString(), "workOrderA", 0, 0,),)
					return input
				}
			}else{
				ProjNo = inputDTO.getProjectA()
			}
			log.info ("ProjNo : " + ProjNo)
		}
		BypassFlag = "N"
		List<Attribute> custAttribs = inputDTO.getCustomAttributes()
		custAttribs.each{Attribute customAttribute ->
			
			if (customAttribute.getName().equals(new String("PBC_CHK_BOX"))){
				log.info ("PBC Attribute Name = ${customAttribute.getName()}")
				log.info ("PBC Attribute Value = ${customAttribute.getValue()}")
				BypassFlag = customAttribute.getValue().toString().trim()
			}
		}
		return null;
	}
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		
		log.info("Hooks onPostExecute RequisitionService_createHeader logging.version: ${hookVersion}");
		RequisitionServiceCreateHeaderReplyDTO reply = (RequisitionServiceCreateHeaderReplyDTO) result
		RequisitionServiceCreateHeaderRequestDTO inputDTO = (RequisitionServiceCreateHeaderRequestDTO) input

		if (reply.getPreqNo() == null || reply.getPreqNo().trim() == ''){
			entity_key = reply.getDistrictCode() + reply.getIreqType() + reply.getIreqNo();
		}else{
			entity_key = reply.getDistrictCode() + reply.getIreqType() + reply.getPreqNo();
		}
		log.info("entity_key : ${entity_key}");
		
		try {
		        /*	
			CreateFile()
			if (CekUUID().equals(true)){
				SetHeaderUUID()
			}
			
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File(env.getWorkDir().toString()+"/PBC." + entity_key)
			def lines = checkFile1.readLines()
			log.info("Line Size : " + lines.size())
			if (lines.size() > 2){
				ChangeLineText("3.PROJ : " + ProjNo,3)
			}else{
				InsertLineFile("3.PROJ : " + ProjNo)
			}
			
			ScanLine(env.getWorkDir().toString()+"/PBC." + entity_key,"PBC_CHK_BOX")
			if (FindLine != 0){
				ChangeLineText("PBC_CHK_BOX" + " : " + BypassFlag,FindLine)
			}else{
				InsertLineFile("\nPBC_CHK_BOX" + " : " + BypassFlag)
			}
                        */
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
				log.info ("Erorr Code:" + errorDTO.getCode())
				log.info ("Error Message:" + errorDTO.getMessage())
				log.info ("Error Fields: " + errorDTO.getFieldName())
			}
		}

		return null
	}
}
