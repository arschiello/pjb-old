import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import org.apache.http.entity.ContentLengthStrategy

import static java.util.UUID.randomUUID;

import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Key
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Rec
import com.mincom.ellipse.edoi.ejb.msf231.MSF231Key
import com.mincom.ellipse.edoi.ejb.msf231.MSF231Rec
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Key
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Rec
import com.mincom.ellipse.edoi.ejb.msf26a.MSF26AKey
import com.mincom.ellipse.edoi.ejb.msf26a.MSF26ARec
import com.mincom.ellipse.edoi.ejb.msf900.MSF900Key
import com.mincom.ellipse.edoi.ejb.msf900.MSF900Rec
import com.mincom.ellipse.edoi.ejb.msf665.MSF665_DATA_665_CKey
import com.mincom.ellipse.edoi.ejb.msf665.MSF665_DATA_665_CRec
import com.mincom.ellipse.edoi.ejb.msf666.MSF666Key
import com.mincom.ellipse.edoi.ejb.msf666.MSF666Rec
import com.mincom.ellipse.edoi.ejb.msf230.MSF230Key
import com.mincom.ellipse.edoi.ejb.msf230.MSF230Rec
import com.mincom.ellipse.edoi.ejb.msf660.MSF660Key
import com.mincom.ellipse.edoi.ejb.msf660.MSF660Rec
import com.mincom.ellipse.edoi.ejb.msf667.MSF667Key
import com.mincom.ellipse.edoi.ejb.msf667.MSF667Rec
import com.mincom.ellipse.edoi.ejb.msf140.MSF140Key
import com.mincom.ellipse.edoi.ejb.msf140.MSF140Rec
import com.mincom.ellipse.edoi.ejb.msf141.MSF141Key
import com.mincom.ellipse.edoi.ejb.msf141.MSF141Rec
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.eql.Constraint
import com.mincom.eql.StringConstraint
import com.mincom.eql.impl.QueryImpl
import com.mincom.batch.environment.*;
import com.mincom.ellipse.ejra.mso.MsoField
import java.text.DecimalFormat
import javax.naming.InitialContext
import groovy.sql.Sql;

public class MSM23EA extends MSOHook {

	String hookVersion = "1"
	String ReqNo,ProjNo,WOProjNo,entity_key,RandUUID,uuid,uuid2,District
	String ALLOC_DATA,USR_ACT,Preq_Itm
	Integer FindLine = 0
	Boolean SetWarning,ZeroBudget = false
	Boolean overBudget = false
	String[] partsAloc
	String DGT,KEG_DES,INTI_DES,PAR_DES
	BigDecimal SRO_KEG,SRO_INTI,SRO_PAR,PO_PROJECT_KEG,PO_PROJECT_INTI,PO_PROJECT_PAR = 0
	BigDecimal PO_WO_KEG,PO_WO_INTI,PO_WO_PAR,NOI_ALL_KEG,NOI_ALL_INTI,NOI_ALL_PAR = 0
	BigDecimal PAYMENT_KEG,PAYMENT_INTI,PAYMENT_PAR,EST_RL_CASH_KEG,EST_RL_CASH_INTI,EST_RL_CASH_PAR = 0
	BigDecimal IJIN_PROSES_KEG,IJIN_PROSES_INTI,IJIN_PROSES_PAR = 0
	BigDecimal EST_REA_PENG_KEG,EST_REA_PENG_INTI,EST_REA_PENG_PAR = 0
	BigDecimal BUDGET_PENG_KEG,BUDGET_PENG_INTI,BUDGET_PENG_PAR = 0
	BigDecimal BUDGET_BEBAN_KEG,BUDGET_BEBAN_INTI,BUDGET_BEBAN_PAR = 0
	BigDecimal IR_PROJECT_KEG,IR_PROJECT_INTI,IR_PROJECT_PAR = 0
	BigDecimal IR_WO_KEG,IR_WO_INTI,IR_WO_PAR = 0
	BigDecimal MAN_JNL_KEG,MAN_JNL_INTI,MAN_JNL_PAR = 0
	BigDecimal EST_RL_BEB_KEG,EST_RL_BEB_INTI,EST_RL_BEB_PAR = 0
	BigDecimal EST_REA_BEB_KEG,EST_REA_BEB_INTI,EST_REA_BEB_PAR = 0
	List<String> listWo = new ArrayList<String>();
	BigDecimal PerAllPengKeg,PerAllPengInti,PerAllPengPar
	BigDecimal PerAllBebKeg,PerAllBebInti,PerAllBebPar
	BigDecimal TotalPrevItem,TotalCurrItem, NilTrans,ActGrossPr
	BigDecimal ScrItmVal,ScrGrosPr
	BigDecimal commitmentAI, commitmentAO, actualAI, actualAO, totalBudget, tranValue, totalCommitment, totalActual
	BigDecimal sisaAnggaran, currentPRValue
	String districtCode, reqNbr, reqItemNbr, projectNbr, workOrder, projectNo, processItem, qtyReqd, estPrice, actualGrossPr, reqItemType


	def CreateFile() {
		try {
			/*
			String content = "1.HEADER_UUID : \n2.ITEM_UUID : \n";
			BatchEnvironment env = new BatchEnvironment()
			File file = new File("/var/opt/appliance/efs/work/PBC." + entity_key);
			// if file doesnt exists, then create it
			//if (!file.exists()) {
				file.createNewFile();
			//}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			*/

			log.info("CreateFile : ")
			String content = "1.HEADER_UUID : \n2.ITEM_UUID : \n";
			BatchEnvironment env = new BatchEnvironment()
			File file = new File("/var/opt/appliance/efs/work/PBC." + entity_key);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();
				log.info ("File Created:" + "/var/opt/appliance/efs/work/PBC." + entity_key)
			}

			System.out.println("Done");
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
	def GetScrVal() {
		try {
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
			def lines = checkFile1.readLines()

			ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"SCRITMVAL : ")
			ScrItmVal = checkFile1.readLines().get(FindLine - 1).substring(12).toBigDecimal()
			ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"SCRGROSPR : ")
			ScrGrosPr = checkFile1.readLines().get(FindLine - 1).substring(12).toBigDecimal()
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
	def SetScrVal(GenericMsoRecord screen) {
		try {
			if (screen.getField("QTY_REQD1I").getValue().trim().equals("") || screen.getField("EST_PRICE1I").getValue().trim().equals("")){
				ScrItmVal = 0
			}else{
				ScrItmVal = screen.getField("QTY_REQD1I").getValue().trim().toBigDecimal() * screen.getField("EST_PRICE1I").getValue().trim().toBigDecimal()
			}

			if (screen.getField("ACT_GROSS_PR1I").getValue().trim().equals("")){
				ScrGrosPr = 0
			}else{
				ScrGrosPr = screen.getField("ACT_GROSS_PR1I").getValue().toBigDecimal()
			}

			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
			def lines = checkFile1.readLines()

			ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"SCRITMVAL : ")
			if (FindLine == 0){
				InsertLineFile("\nSCRITMVAL : " + ScrItmVal)
			}else{
				ChangeLineText("SCRITMVAL : " + ScrItmVal,FindLine)
			}

			ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"SCRGROSPR : ")
			if (FindLine == 0){
				InsertLineFile("\nSCRGROSPR : " + ScrGrosPr)
			}else{
				ChangeLineText("SCRGROSPR : " + ScrGrosPr,FindLine)
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
	def SetItemUUID() {
		try {
			log.info("SetItemUUID : ")
			SetWarning = false
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
			def lines = checkFile1.readLines()
			uuid = checkFile1.readLines().get(0).substring(16)
			String ChangeLine
			ChangeLine = checkFile1.readLines().get(1).substring(0)
			if(checkFile1.readLines().get(0).substring(0).toString().equals("1.HEADER_UUID : ") && checkFile1.readLines().get(1).substring(0).toString().equals("2.ITEM_UUID : ")){
				SetHeaderUUID()
				SetWarning = true
			}else if(ChangeLine.equals("2.ITEM_UUID : ")){
				ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"2.ITEM_UUID : ")
				ChangeLineText("2.ITEM_UUID : " + uuid,FindLine)
				SetWarning = true
			}else{
				uuid2 = checkFile1.readLines().get(1).substring(14)
				if (uuid != uuid2){
					ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"2.ITEM_UUID : ")
					ChangeLineText("2.ITEM_UUID : " + uuid,FindLine)
					SetWarning = true
				}else{
					log.info ("RESET HEADER 1")
					ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"1.HEADER_UUID : ")
					ChangeLineText("1.HEADER_UUID : ",FindLine)
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
	def SetProjectNo() {
		try {
			MSF232Key msf232key = new MSF232Key();

			Constraint c1 = MSF232Key.dstrctCode.equalTo(tools.commarea.District);
			Constraint c2 = MSF232Key.allocCount.equalTo("01");
			Constraint c3 = MSF232Key.requisitionNo.like(ReqNo.padRight(6, " ") + "%");

			QueryImpl query = new QueryImpl(MSF232Rec.class).and(c1).and(c2).and(c3).orderBy(MSF232Rec.msf232Key);
			Integer i = 0

			tools.edoi.search(query,{MSF232Rec msf232rec ->
				i++
				if (msf232rec != null && i == 1){
					BatchEnvironment env = new BatchEnvironment()
					File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
					def lines = checkFile1.readLines()
					log.info("Line Size : " + lines.size())
					if (lines.size() > 2){
						for (int j = lines.size(); j > 2; j--){
							log.info("Line No : " + j)
							ClearProjectLine("/var/opt/appliance/efs/work/PBC." + entity_key,checkFile1.readLines().get(j-1).substring(0))
						}
					}
				}
				if (msf232rec != null){
					log.info("Record was found")
					ProjNo = msf232rec.getProjectNo()
				}else{
					log.info("No record was found")
				}

				if (ProjNo.trim() == "" || ProjNo == null){
					ProjNo = ProjWo(tools.commarea.District,msf232rec.getWorkOrder())
				}
				log.info("Requisition No : " + msf232rec.getPrimaryKey().getRequisitionNo().substring(8, 11))
				if (msf232rec.getPrimaryKey().getRequisitionNo().substring(8, 11) == "000"){
					InsertLineFile("3.PROJ : " + ProjNo)
				}else{
					InsertLineFile("\n" + "ITM" + msf232rec.getPrimaryKey().getRequisitionNo().substring(8, 11) + " : " + ProjNo)
				}
			})

			if (i == 0){
				log.info ("No records MSF232 was found")
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
	def PBC_CALC() {

		try {
			log.info("PBC Calculation")
			ProjNo = GetProj()
			log.info("ProjNo : " + ProjNo)

			if (!ProjNo.equals("") && ProjNo != null){
				KEG_DES = GetProjDes(District,ProjNo)
				INTI_DES = GetProjDes(District,ProjNo.substring(0,6))
				PAR_DES = GetProjDes(District,ProjNo.substring(0,4))

				EST_REA_PENG_KEG = 0
				EST_REA_BEB_INTI = 0
				EST_REA_BEB_PAR = 0
				BUDGET_BEBAN_KEG = 0
				BUDGET_BEBAN_INTI = 0
				BUDGET_BEBAN_PAR = 0

				EST_REA_BEB_KEG = 0
				EST_REA_BEB_INTI = 0
				EST_REA_BEB_PAR = 0
				BUDGET_BEBAN_KEG = 0
				BUDGET_BEBAN_INTI = 0
				BUDGET_BEBAN_PAR = 0

				ZeroBudget = false

				InitialContext initial = new InitialContext()
				Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(CAISource)

				def QueryRes1 = sql.firstRow("select PBC_GET_EST_REA_CASH('" + District + "','" + ProjNo + "') EST_REA_PENG from dual");
				log.info ("QueryRes1 : " + QueryRes1)
				String[] parts1 = QueryRes1.EST_REA_PENG.split(":");
				log.info ("parts1 : " + parts1)
				EST_REA_PENG_KEG = parts1[0].trim().toBigDecimal()
				EST_REA_PENG_INTI = parts1[1].trim().toBigDecimal()
				EST_REA_PENG_PAR = parts1[2].trim().toBigDecimal()
				log.info ("EST_REA_PENG_KEG : " + EST_REA_PENG_KEG)
				log.info ("EST_REA_PENG_INTI : " + EST_REA_PENG_INTI)
				log.info ("EST_REA_PENG_PAR : " + EST_REA_PENG_PAR)
				BUDGET_PENG_KEG = parts1[3].trim().toBigDecimal()
				BUDGET_PENG_INTI = parts1[4].trim().toBigDecimal()
				BUDGET_PENG_PAR = parts1[5].trim().toBigDecimal()
				log.info ("BUDGET_PENG_KEG : " + BUDGET_PENG_KEG)
				log.info ("BUDGET_PENG_INTI : " + BUDGET_PENG_INTI)
				log.info ("BUDGET_PENG_PAR : " + BUDGET_PENG_PAR)

				def QueryRes = sql.firstRow("select PBC_GET_EST_REA_BEBAN('" + District + "','" + ProjNo + "') EST_REA_BEBAN from dual");
				log.info ("QueryRes : " + QueryRes)
				String[] parts = QueryRes.EST_REA_BEBAN.split(":");
				log.info ("parts : " + parts)
				EST_REA_BEB_KEG = parts[0].trim().toBigDecimal()
				EST_REA_BEB_INTI = parts[1].trim().toBigDecimal()
				EST_REA_BEB_PAR = parts[2].trim().toBigDecimal()
				log.info ("EST_REA_BEB_KEG : " + EST_REA_BEB_KEG)
				log.info ("EST_REA_BEB_INTI : " + EST_REA_BEB_INTI)
				log.info ("EST_REA_BEB_PAR : " + EST_REA_BEB_PAR)
				BUDGET_BEBAN_KEG = parts[3].trim().toBigDecimal()
				BUDGET_BEBAN_INTI = parts[4].trim().toBigDecimal()
				BUDGET_BEBAN_PAR = parts[5].trim().toBigDecimal()
				log.info ("BUDGET_BEBAN_KEG : " + BUDGET_BEBAN_KEG)
				log.info ("BUDGET_BEBAN_INTI : " + BUDGET_BEBAN_INTI)
				log.info ("BUDGET_BEBAN_PAR : " + BUDGET_BEBAN_PAR)

				if (BUDGET_PENG_KEG == 0 || BUDGET_PENG_INTI == 0 || BUDGET_PENG_PAR == 0 ||
						BUDGET_BEBAN_KEG == 0 || BUDGET_BEBAN_INTI == 0 || BUDGET_BEBAN_PAR == 0){
					ZeroBudget = true
					return
				}

				GetPrefItem(ReqNo,DGT + Preq_Itm)

				log.info ("USR_ACT:" + USR_ACT)
				if (USR_ACT.equals("L")){
					NilTrans = ActGrossPr
				}
				else{
					NilTrans = TotalPrevItem + TotalCurrItem
				}

				PerAllPengKeg = (((NilTrans + EST_REA_PENG_KEG) / BUDGET_PENG_KEG) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
				PerAllPengInti = (((NilTrans + EST_REA_PENG_INTI) / BUDGET_PENG_INTI) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
				PerAllPengPar = (((NilTrans + EST_REA_PENG_PAR) / BUDGET_PENG_PAR) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);

				PerAllBebKeg = (((NilTrans + EST_REA_BEB_KEG) / BUDGET_BEBAN_KEG) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
				PerAllBebInti = (((NilTrans + EST_REA_BEB_INTI) / BUDGET_BEBAN_INTI) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
				PerAllBebPar = (((NilTrans + EST_REA_BEB_PAR) / BUDGET_BEBAN_PAR) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);

				log.info ("PerAllPengKeg:" + PerAllPengKeg)
				log.info ("PerAllPengInti:" + PerAllPengInti)
				log.info ("PerAllPengPar:" + PerAllPengPar)
				log.info ("PerAllBebKeg:" + PerAllBebKeg)
				log.info ("PerAllBebInti:" + PerAllBebInti)
				log.info ("PerAllBebPar:" + PerAllBebPar)
				log.info ("NilTrans:" + NilTrans)
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
	private boolean isQuestionMarkOnScreen (GenericMsoRecord screen) {
		String screenData = screen.getCurrentScreenDetails().getScreenFields().toString()
		return screenData.contains("?")
	 }
	def GetProjDes(String dstrct, String Proj) {
		String ProjDesc
		try {

			Constraint c1 = MSF660Key.dstrctCode.equalTo(dstrct);
			Constraint c2 = MSF660Key.projectNo.equalTo(Proj);

			def query = new QueryImpl(MSF660Rec.class).and(c1).and(c2);

			MSF660Rec msf660Rec = tools.edoi.firstRow(query);

			if (msf660Rec != null){
				log.info("Record was found")
				ProjDesc = msf660Rec.getProjDesc().trim()
			}else{
				log.info("No record was found")
			}
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return ProjDesc
	}
	def getProjectCosting(String dstrctCode, String preqNo, String preqItem){
		try{
			log.info("Getting Project Cost Information...")

			String requisitionNo = preqNo.trim() + "  " + preqItem.trim()
			String requisitionNo2 = preqNo.trim() + "  000"

			log.info("requisitionNo: $requisitionNo")

			Constraint district = MSF232Key.dstrctCode.equalTo(dstrctCode)
			Constraint preqNbr = MSF232Key.requisitionNo.equalTo(requisitionNo)
			Constraint preqNbr2 = MSF232Key.requisitionNo.equalTo(requisitionNo2)
			Constraint rec232Type = MSF232Key.req_232Type.equalTo("P")

			def qry = new QueryImpl(MSF232Rec.class).and(district).and(preqNbr).and(rec232Type)

			MSF232Rec mSF232rec = tools.edoi.firstRow(qry)
			if(mSF232rec){
				log.info("Data Found on Item...")
				projectNbr = mSF232rec.getProjectNo().trim()
				workOrder = mSF232rec.getWorkOrder().trim()
			}
			else{
				def qry2 = new QueryImpl(MSF232Rec.class).and(district).and(preqNbr2).and(rec232Type)
				MSF232Rec mSF232rec2 = tools.edoi.firstRow(qry2)
				if(mSF232rec2){
					log.info("Data Found on Header...")
					projectNbr = mSF232rec2.getProjectNo().trim()
					workOrder = mSF232rec2.getWorkOrder().trim()
				}
				else {
					log.info("Data Not Found...")
				}
			}

			if(workOrder &&
					workOrder.trim() != "" &&
					(!projectNbr || projectNbr.trim() == "")){
				projectNbr = ProjWo(dstrctCode, workOrder)
			}

			log.info("onDisplay projectNbr: $projectNbr")
			log.info("onDisplay workOrder: $workOrder")
		}
		catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
				log.info ("Error Code:" + errorDTO.getCode())
				log.info ("Error Message:" + errorDTO.getMessage())
				log.info ("Error Fields: " + errorDTO.getFieldName())
			}
		}
		return projectNbr
	}
	def ProjWo(String dstrct, String wo) {
		try {
			MSF620Key msf620key = new MSF620Key();

			Constraint c1 = MSF620Key.dstrctCode.equalTo(dstrct);
			Constraint c2 = MSF620Key.workOrder.equalTo(wo);

			def query = new QueryImpl(MSF620Rec.class).and(c1).and(c2);

			MSF620Rec msf620Rec = tools.edoi.firstRow(query);

			if (msf620Rec != null){
				log.info("Record was found")
				WOProjNo = msf620Rec.getProjectNo()
			}else{
				log.info("No record was found")
			}
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return WOProjNo
	}
	def ClearProjectLine(String file, String lineToRemove) {
		try {

			File inFile = new File(file);

			if (!inFile.isFile()) {
			  System.out.println("Parameter is not an existing file");
			  return;
			}

			//Construct the new file that will later be renamed to the original filename.
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;

			//Read from the original file and write to the new
			//unless content matches data to be removed.
			while ((line = br.readLine()) != null) {

			  if (!line.trim().equals(lineToRemove)) {

				pw.println(line);
				pw.flush();
			  }
			}
			pw.close();
			br.close();

			//Delete the original file
			if (!inFile.delete()) {
			  System.out.println("Could not delete file");
			  return;
			}

			//Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
			  System.out.println("Could not rename file");
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
	def InsertLineFile(String StrLine) {
		try {
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
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
	def ScanLine(String Path, String StrFind) {
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
	def ChangeLineText(String NewLine, int row) {
		try {
			log.info ("RESET LINE")
			String ChangeLine
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
			ChangeLine = checkFile1.readLines().get(row - 1).substring(0)
			BufferedReader file = new BufferedReader(new FileReader("/var/opt/appliance/efs/work/PBC." + entity_key));
			String line;
			String tmp = "";
			while ((line = file.readLine()) != null) tmp += line + '\n';
			file.close();

			tmp = tmp.replace(ChangeLine, NewLine);
			FileOutputStream fileOut = new FileOutputStream("/var/opt/appliance/efs/work/PBC." + entity_key);
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
	def SetHeaderUUID() {
		try {
			log.info("SetHeaderUUID : ")
			String ChangeLine
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
			RandUUID = (randomUUID() as String).toUpperCase();
			ChangeLine = checkFile1.readLines().get(0).substring(0)
			log.info ("UUID : " + uuid)
			log.info ("ChangeLine : " + ChangeLine)
			log.info ("SetWarning : " + SetWarning)
			if (uuid == null || uuid.equals("") || ChangeLine.equals("1.HEADER_UUID : ") || SetWarning.equals(false)){
				ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"1.HEADER_UUID : ")
				ChangeLineText("1.HEADER_UUID : " + RandUUID,FindLine)
				log.info ("NEW UUID : " + RandUUID)
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
	def GetProj() {
		String Project
		try {
			BatchEnvironment env = new BatchEnvironment()
			File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
			def lines = checkFile1.readLines()
			log.info ("Get PROJ lines size :" + lines.size())
			if (lines.size() > 3){
				ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"ITM" + DGT + Preq_Itm)
				log.info ("Get PROJ FindLine :" + FindLine)
				if (FindLine == null || FindLine == 0){
					if (!checkFile1.readLines().get(2).substring(0).trim().equals("")){
						Project = checkFile1.readLines().get(2).substring(9)
					}else{
						Project = ""
					}
				}else {
					Project = checkFile1.readLines().get(FindLine -1).substring(9)
				}
			}else if (lines.size() == 2){
				Project = ""
			}else{
				Project = checkFile1.readLines().get(2).substring(9)
			}
			log.info ("Get PROJ Project :" + Project)
		}catch (EnterpriseServiceOperationException e){
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Erorr Code:" + errorDTO.getCode())
					log.info ("Error Message:" + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
		}
		return Project
	}
	def GetPrefItem(String ReqNo, String PreqItm) {

		try {
			log.info("SEARCH PREVIOUS ITEM")
			String Project

			Constraint c1 = MSF231Key.dstrctCode.equalTo(tools.commarea.District);
			Constraint c2 = MSF231Key.preqNo.equalTo(ReqNo);
			Constraint c3 = MSF231Key.preqItemNo.lessThan(PreqItm)

			QueryImpl query = new QueryImpl(MSF231Rec.class).and(c1).and(c2).and(c3);
			Integer i = 0
			TotalPrevItem = 0
			tools.edoi.search(query,{MSF231Rec msf231rec ->
				i++
				if (msf231rec != null){

					BatchEnvironment env = new BatchEnvironment()
					File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entity_key)
					def lines = checkFile1.readLines()

					if (lines.size() > 3){
						log.info("Find : " + "ITM" + msf231rec.getPrimaryKey().getPreqItemNo())
						ScanLine("/var/opt/appliance/efs/work/PBC." + entity_key,"ITM" + msf231rec.getPrimaryKey().getPreqItemNo())
						if (FindLine == null || FindLine == 0){
							Project = checkFile1.readLines().get(2).substring(9)
						}else {
							Project = checkFile1.readLines().get(FindLine -1).substring(9)
						}
					}else{
						Project = checkFile1.readLines().get(2).substring(9)
					}
					log.info("Project : " + Project)
					log.info("ProjNo : " + ProjNo)
					if (Project.equals(ProjNo)){
						TotalPrevItem = TotalPrevItem + msf231rec.getTotalItemValue()
					}
					log.info("TotalPrevItem : " + TotalPrevItem)
				}else{
					log.info("No record was found")
				}
			})

			if (i == 0){
				log.info ("No records MSF231 found")
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
	def getTotalBudget(String dstrctCode, String projectNbr) {
		log.info("Get total budget of Project Nbr: " + projectNbr)

		if(projectNbr) {
			String projectType
			if(projectNbr.substring(2, 4) == "3Y"
					|| projectNbr.substring(2, 4) == "4A"
					|| projectNbr.substring(2, 4) == "4B") {
				projectType = "AI"
			}
			else if(projectNbr.substring(2, 4) == "2O"
					|| projectNbr.substring(2, 4) == "3O") {
				projectType = "AO"
			}
			else if(projectNbr.substring(3, 4) == "G"
					|| projectNbr.substring(3, 4) == "H"
					|| projectNbr.substring(3, 4) == "I"
					|| projectNbr.substring(3, 4) == "J"
					|| projectNbr.substring(3, 4) == "K"
					|| projectNbr.substring(3, 4) == "L"
					|| projectNbr.substring(3, 4) == "N"
					|| projectNbr.substring(3, 4) == "P") {
				projectType = "HAR"
			}
			else if(projectNbr.substring(3, 4) == "S") {
				projectType = "ADM"
			}
			else {
				projectType = null
			}

			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)
				def budgetInTotal
				if(projectType == "AI" || projectType == "AO" || projectType == "HAR") {
					log.info("Query Command: " +
							"SELECT TOT_EST_COST totEstCost " +
							"FROM MSF667 " +
							"WHERE CATEGORY_CODE = ' ' " +
							"AND EXP_REV_IND = 'E' " +
							"AND PROJECT_NO = '$projectNbr' " +
							"AND DSTRCT_CODE = '$dstrctCode'")

					budgetInTotal = sql.firstRow("SELECT TOT_EST_COST totEstCost " +
							"FROM MSF667 " +
							"WHERE CATEGORY_CODE = ' ' " +
							"AND EXP_REV_IND = 'E' " +
							"AND PROJECT_NO = '$projectNbr' " +
							"AND DSTRCT_CODE = '$dstrctCode'")
				}
				else if(projectType == "ADM") {
					projectNbr = projectNbr.substring(0, 4)
					log.info("Query Command: " +
							"SELECT TOT_EST_COST totEstCost " +
							"FROM MSF667 " +
							"WHERE CATEGORY_CODE = ' ' " +
							"AND EXP_REV_IND = 'E' " +
							"AND PROJECT_NO = '$projectNbr' " +
							"AND DSTRCT_CODE = '$dstrctCode'")

					budgetInTotal = sql.firstRow("SELECT TOT_EST_COST totEstCost " +
							"FROM MSF667 " +
							"WHERE CATEGORY_CODE = ' ' " +
							"AND EXP_REV_IND = 'E' " +
							"AND PROJECT_NO = '$projectNbr' " +
							"AND DSTRCT_CODE = '$dstrctCode'")
				}
				else {
					budgetInTotal = null
				}

				if(budgetInTotal){
					String[] budget = budgetInTotal.totEstCost
					totalBudget = budget[0].trim() as BigDecimal
				}
				else {
					totalBudget = 0
				}
				log.info("Query result totalBudget: " + totalBudget)
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
			}
		}
	}
	def getCommitmentAI(String districtCode, String projectNbr) {
		log.info("districtCode: $districtCode")
		log.info("Get AI Commitment of Project Nbr: " + projectNbr)

		BigDecimal result
		if (projectNbr && projectNbr.trim() != "") {
			String projectType
			if(projectNbr.substring(2, 4) == "3Y"
					|| projectNbr.substring(2, 4) == "4A"
					|| projectNbr.substring(2, 4) == "4B") {
				projectType = "AI"
			}
			else if(projectNbr.substring(2, 4) == "2O"
					|| projectNbr.substring(2, 4) == "3O") {
				projectType = "AO"
			}
			else if(projectNbr.substring(3, 4) == "G"
					|| projectNbr.substring(3, 4) == "H"
					|| projectNbr.substring(3, 4) == "I"
					|| projectNbr.substring(3, 4) == "J"
					|| projectNbr.substring(3, 4) == "K"
					|| projectNbr.substring(3, 4) == "L"
					|| projectNbr.substring(3, 4) == "N"
					|| projectNbr.substring(3, 4) == "P") {
				projectType = "HAR"
			}
			else if(projectNbr.substring(3, 4) == "S") {
				projectType = "ADM"
			}
			else {
				projectType = null
			}

			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")

				def sql = new Sql(dataSource)

				def aIcommitment
				if(projectType == "AI" || projectType == "AO" || projectType == "HAR") {
					log.info("Query command: " +
							"SELECT COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")

					aIcommitment = sql.firstRow("SELECT COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")
				}
				else if(projectType == "ADM") {
					String projectNo = projectNbr.substring(0, 4)
					log.info("Query command: " +
							"SELECT SUM(COMMITMENTS) AS COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNo" + "%'")

					aIcommitment = sql.firstRow("SELECT SUM(COMMITMENTS) AS COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNo" + "%'")
				}
				else {
					aIcommitment = null
				}

				if (aIcommitment) {
					String[] aiCommit = aIcommitment.COMMITMENTS
					result = aiCommit[0].trim() as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result commitmentAI: $result")
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
				result = 0
			}
		} else {
			result = 0
		}
		log.info("result getCommitmentAI: $result")
		return result
	}
	def getCommitmentAO(String districtCode, String projectNbr) {
		log.info("Get AO Commitment of Project Nbr: " + projectNbr)
		log.info("districtCode: $districtCode")

		BigDecimal result
		if(projectNbr && projectNbr.trim() != "") {
			String projectType
			if(projectNbr.substring(2, 4) == "3Y"
					|| projectNbr.substring(2, 4) == "4A"
					|| projectNbr.substring(2, 4) == "4B") {
				projectType = "AI"
			}
			else if(projectNbr.substring(2, 4) == "2O"
					|| projectNbr.substring(2, 4) == "3O") {
				projectType = "AO"
			}
			else if(projectNbr.substring(3, 4) == "G"
					|| projectNbr.substring(3, 4) == "H"
					|| projectNbr.substring(3, 4) == "I"
					|| projectNbr.substring(3, 4) == "J"
					|| projectNbr.substring(3, 4) == "K"
					|| projectNbr.substring(3, 4) == "L"
					|| projectNbr.substring(3, 4) == "N"
					|| projectNbr.substring(3, 4) == "P") {
				projectType = "HAR"
			}
			else if(projectNbr.substring(3, 4) == "S") {
				projectType = "ADM"
			}
			else {
				projectType = null
			}
			log.info("Project Type: $projectType")

			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)

				def aOCommitment
				if(projectType == "AI" || projectType == "AO" || projectType == "HAR") {
					log.info("Query command: " +
							"SELECT COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AO " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")
					aOCommitment = sql.firstRow("SELECT COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AO " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")
				}
				else if (projectType == "ADM") {
					String projectNo = projectNbr.substring(0, 4)
					log.info("Query command: " +
							"SELECT SUM(COMMITMENTS) AS COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AO " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNo" + "%'")
					aOCommitment = sql.firstRow("SELECT SUM(COMMITMENTS) AS COMMITMENTS " +
							"FROM VPBC_COMMITMENT_AO " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNo" + "%'")
				}
				else {
					aOCommitment = null
				}

				if(aOCommitment) {
					String[] aoCommit = aOCommitment.COMMITMENTS
					result =  aoCommit[0] as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result commitmentAO: $result")
			}
			catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
				result = 0
			}
		}
		else {
			result = 0
		}
		log.info("result GetCommitmentAO: $result")
		return result
	}
	def getActualAI(String districtCode, String projectNbr) {
		log.info("Get AI Actuals of Project Nbr: " + projectNbr)
		log.info("districtCode: $districtCode")

		BigDecimal result
		if(projectNbr && projectNbr.trim() != "") {
			String projectType
			if(projectNbr.substring(2, 4) == "3Y"
					|| projectNbr.substring(2, 4) == "4A"
					|| projectNbr.substring(2, 4) == "4B") {
				projectType = "AI"
			}
			else if(projectNbr.substring(2, 4) == "2O"
					|| projectNbr.substring(2, 4) == "3O") {
				projectType = "AO"
			}
			else if(projectNbr.substring(3, 4) == "G"
					|| projectNbr.substring(3, 4) == "H"
					|| projectNbr.substring(3, 4) == "I"
					|| projectNbr.substring(3, 4) == "J"
					|| projectNbr.substring(3, 4) == "K"
					|| projectNbr.substring(3, 4) == "L"
					|| projectNbr.substring(3, 4) == "N"
					|| projectNbr.substring(3, 4) == "P") {
				projectType = "HAR"
			}
			else if(projectNbr.substring(3, 4) == "S") {
				projectType = "ADM"
			}
			else {
				projectType = null
			}
			log.info("Project Type: $projectType")

			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)

				def aIActual
				if(projectType == "AI" || projectType == "AO" || projectType == "HAR") {
					log.info("Query command: " +
							"SELECT ACTUALS " +
							"FROM VPBC_ACTUAL_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")
					aIActual = sql.firstRow("SELECT ACTUALS " +
							"FROM VPBC_ACTUAL_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")
				}
				else if (projectType == "ADM") {
					String projectNo = projectNbr.substring(0, 4)
					log.info("Query command: " +
							"SELECT SUM(ACTUALS) AS ACTUALS " +
							"FROM VPBC_ACTUAL_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNo" + "%'")
					aIActual = sql.firstRow("SELECT SUM(ACTUALS) AS ACTUALS " +
							"FROM VPBC_ACTUAL_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNo" + "%'")
				}
				else {
					aIActual = null
				}

				if(aIActual) {
					String[] aiAct = aIActual.ACTUALS
					result = aiAct[0] as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result actualAI: $result")
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
				result = 0
			}
		}
		else {
			result = 0
		}
		log.info("result GetActualAI: $result")
		return result
	}
	def getActualAO(String districtCode, String projectNbr) {
		log.info("Get AO Actuals of Project Nbr: " + projectNbr)
		log.info("districtCode: $districtCode")

		BigDecimal result
		if(projectNbr && projectNbr.trim() != "") {
			String projectType
			if(projectNbr.substring(2, 4) == "3Y"
					|| projectNbr.substring(2, 4) == "4A"
					|| projectNbr.substring(2, 4) == "4B") {
				projectType = "AI"
			}
			else if(projectNbr.substring(2, 4) == "2O"
					|| projectNbr.substring(2, 4) == "3O") {
				projectType = "AO"
			}
			else if(projectNbr.substring(3, 4) == "G"
					|| projectNbr.substring(3, 4) == "H"
					|| projectNbr.substring(3, 4) == "I"
					|| projectNbr.substring(3, 4) == "J"
					|| projectNbr.substring(3, 4) == "K"
					|| projectNbr.substring(3, 4) == "L"
					|| projectNbr.substring(3, 4) == "N"
					|| projectNbr.substring(3, 4) == "P") {
				projectType = "HAR"
			}
			else if(projectNbr.substring(3, 4) == "S") {
				projectType = "ADM"
			}
			else {
				projectType = null
			}
			log.info("Project Type: $projectType")

			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)

				def aOActual
				if(projectType == "AI" || projectType == "AO" || projectType == "HAR") {
					log.info("Query command: " +
							"SELECT ACTUALS " +
							"FROM VPBC_ACTUAL_AO " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")
					aOActual = sql.firstRow("SELECT ACTUALS " +
							"FROM VPBC_ACTUAL_AO " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO = '$projectNbr'")
				}
				else if (projectType == "ADM") {
					String projectNo = projectNbr.substring(0, 4)
					log.info("Query command: " +
							"SELECT SUM(ACTUALS) AS ACTUALS " +
							"FROM VPBC_ACTUAL_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNbr" + "%'")
					aOActual = sql.firstRow("SELECT SUM(ACTUALS) AS ACTUALS " +
							"FROM VPBC_ACTUAL_AI " +
							"WHERE DSTRCT_CODE = '$districtCode' " +
							"AND PROJECT_NO LIKE '$projectNo" + "%'")
				}
				else {
					aOActual = null
				}

				if(aOActual) {
					String[] aOAct = aOActual.ACTUALS
					result = aOAct[0] as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result actualAO: $result")
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
				result = 0
			}
		}
		else {
			result = 0
		}
		log.info("result GetActualAO: $result")
		return result
	}
	def getCommitPR(String districtCode, String preqNo) {
		BigDecimal result
		log.info("Get Committed PR...")
		try {
			InitialContext initialContext = new InitialContext()
			Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
			def sql = new Sql(dataSource)

			log.info("Query command: " +
					"SELECT SISA " +
					"FROM VPBC_COMMIT_PR " +
					"WHERE DSTRCT_CODE = '" + districtCode +
					"' AND PREQ_NO = '" + preqNo + "'")

			def currPRValue = sql.firstRow("SELECT SISA " +
					"FROM VPBC_COMMIT_PR " +
					"WHERE DSTRCT_CODE = '" + districtCode +
					"' AND PREQ_NO = '" + preqNo + "'")

			if(currPRValue) {
				String[] currentPRVal = currPRValue.SISA
				result = currentPRVal[0] as BigDecimal
			} else {
				result = 0
			}
			log.info("Query result currentPRValue: " + result)
		}
		catch (EnterpriseServiceOperationException e) {
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
				log.info ("Error Code: " + errorDTO.getCode())
				log.info ("Error Message: " + errorDTO.getMessage())
				log.info ("Error Fields: " + errorDTO.getFieldName())
			}
		}
		return result
	}
	def validateBudget(String dstrctCode, String projectNbr) {
		log.info("Calculating Budget...")
		log.info("validateBudget - currentPRValue: $currentPRValue")
		log.info("NilTrans: " + NilTrans)

		try{
			getTotalBudget(dstrctCode, projectNbr)
			getCommitmentAI(dstrctCode, projectNbr)
			getCommitmentAO(dstrctCode, projectNbr)
			getActualAI(dstrctCode, projectNbr)
			getActualAO(dstrctCode, projectNbr)

			if(!totalBudget || totalBudget == 0) {
				ZeroBudget = true
				return
			}

			tranValue = ((NilTrans
					- currentPRValue
					+ commitmentAI
					+ commitmentAO
					+ actualAI
					+ actualAO) / totalBudget * 100).setScale(2, BigDecimal.ROUND_HALF_UP)

			log.info("tranValue: " + tranValue)

			if (tranValue > 100) {
				overBudget = true
			}
		}
		catch (EnterpriseServiceOperationException e) {
			List <ErrorMessageDTO> listError = e.getErrorMessages()
			listError.each{ErrorMessageDTO errorDTO ->
				log.info ("Error Code:" + errorDTO.getCode())
				log.info ("Error Message:" + errorDTO.getMessage())
				log.info ("Error Fields: " + errorDTO.getFieldName())
			}
		}
	}
	def getModuleSwitch(String tblType, String tblCode) {
		String result
		Constraint tableType = MSF010Key.tableType.equalTo(tblType)
		Constraint tableCode = MSF010Key.tableCode.equalTo(tblCode)

		def qMSF010 = new QueryImpl(MSF010Rec.class).and(tableType).and(tableCode)
		MSF010Rec msf010Rec = tools.edoi.firstRow(qMSF010)

		String assocRec
		if (msf010Rec) {
			assocRec = msf010Rec.getAssocRec().trim()
			if (assocRec != "") {
				result = assocRec.substring(0, 1)
			} else {
				result = ""
			}
		} else {
			result = ""
		}
		log.info("result: $result")
		return result
	}
	def getFinalisedDate(String districtCode, String projectNo) {
		log.info("Get Finalised Date ...")
		String result

		Constraint projectNbr = MSF660Key.projectNo.equalTo(projectNo)
		Constraint districtCD = MSF660Key.dstrctCode.equalTo(districtCode)

		def query = new QueryImpl(MSF660Rec.class).and(districtCD).and(projectNbr)
		MSF660Rec msf660Rec = tools.edoi.firstRow(query)

		if (msf660Rec) {
			String finalisedDate = msf660Rec.getFinalisedDate().trim()
			if (finalisedDate && finalisedDate != "") {
				result = "FINALISED"
			} else {
				result = ""
			}
		}
		else {
			result = ""
		}
		log.info("result Project Status: $result")
		return result
	}
	def lastN(String input, int n) {
		return n > input?.size() ? null : n ? input[-n..-1] : ''
	}

	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){
		log.info("[Arsiadi] Hooks onDisplay MSM23EA logging.version: ${hookVersion}")

		String pBCActiveFlag = getModuleSwitch("+PBC", "MSM23EA")
		log.info("PBC Active Flag: $pBCActiveFlag")
		if (pBCActiveFlag != "" && pBCActiveFlag != "Y") {
			return null
		}

//		entity_key = tools.commarea.District + "PR" + screen.getField("PREQ_NO1I").getValue()
//		CreateFile()
//		ReqNo = screen.getField("PREQ_NO1I").getValue()

		districtCode = tools.commarea.District
		reqNbr = screen.getField("PREQ_NO1I").getValue().trim()
		reqItemNbr = lastN("000" + screen.getField("PREQ_ITEM_NO1I").getValue().trim(), 3)

		log.info("districtCode: $districtCode")
		log.info("reqNbr: $reqNbr")
		log.info("reqItemNbr: $reqItemNbr")

		projectNo = getProjectCosting(districtCode, reqNbr, reqItemNbr)
		log.info("projectNo: $projectNo")

//		SetProjectNo()
//		SetHeaderUUID()
//		SetScrVal(screen)
		return null
	}

	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen) {
		log.info("[Arsiadi222] Hooks onPreSubmit MSM23EA logging.version: ${hookVersion}")

		String pBCActiveFlag = getModuleSwitch("+PBC", "MSM23EA")
		log.info("PBC Active Flag: $pBCActiveFlag")
		if (pBCActiveFlag != "" && pBCActiveFlag != "Y") {
			return null
		}

		DecimalFormat formatter = new DecimalFormat("#,###.00")

		districtCode = tools.commarea.District
		reqNbr = screen.getField("PREQ_NO1I").getValue().trim()
		reqItemNbr = lastN("000" + screen.getField("PREQ_ITEM_NO1I").getValue().trim(), 3)
		processItem = screen.getField("PROCESS_ITEM1I").getValue().trim()

		qtyReqd = screen.getField("QTY_REQD1I").getValue().trim()
		estPrice = screen.getField("EST_PRICE1I").getValue().trim()
		actualGrossPr = screen.getField("ACT_GROSS_PR1I").getValue().trim()
		reqItemType = screen.getField("PREQ_TYPE1I").getValue().trim()

		TotalCurrItem = 0

		log.info("qtyReqd: $qtyReqd")
		log.info("estPrice: $estPrice")
		log.info("actualGrossPr: $actualGrossPr")
		log.info("reqItemType: $reqItemType")

		currentPRValue = getCommitPR(districtCode, reqNbr)

		log.info("districtCode: $districtCode")
		log.info("reqNbr: $reqNbr")
		log.info("reqItemNbr: $reqItemNbr")
		log.info("currentPRValue: $currentPRValue")

		projectNo = getProjectCosting(districtCode, reqNbr, reqItemNbr)

		log.info("projectNo: $projectNo")

		int x = screen.nextAction
		log.info ("---Next Action : " + x.toString())

		if ((x == 1 || x == 0) &&
				!isQuestionMarkOnScreen(screen) &&
				!screen.getField("FKEYS1I").getValue().trim().contains("XMIT-Confirm") &&
				screen.getField("ACTION_A1I").getValue().trim() == "" &&
				screen.getField("ACTION_B1I").getValue().trim() == "" &&
				screen.getField("ACTION_C1I").getValue().trim() == "" &&
				screen.getField("ACTION_D1I").getValue().trim() == "" &&
				screen.getField("ACTION_E1I").getValue().trim() == "" &&
				screen.getField("ACTION_F1I").getValue().trim() == "" &&
				screen.getField("PROCESS_ITEM1I").getValue().trim() == "L") {
			log.info("---QTY_REQD1I: " + screen.getField("QTY_REQD1I").getValue())
			log.info("EST_PRICE1I: " + screen.getField("EST_PRICE1I").getValue())
			log.info("ACT_GROSS_PR1I: " + screen.getField("ACT_GROSS_PR1I").getValue())
			log.info("before NilTrans Calculation: $NilTrans")

			if (reqItemType == "G" || reqItemType == "Q") {
				if (qtyReqd != ""
						&& qtyReqd.toBigDecimal() != 0
						&& actualGrossPr != ""
						&& actualGrossPr.toBigDecimal() != 0) {
					TotalCurrItem = qtyReqd.toBigDecimal() * actualGrossPr.toBigDecimal()
				}
			}
			else if (reqItemType == "S"  || reqItemType == "F") {
				if (actualGrossPr.trim() != "" && actualGrossPr.toBigDecimal() != 0) {
					TotalCurrItem = actualGrossPr.toBigDecimal()
				}
			}
			else {
				TotalCurrItem = 0
			}

			if (processItem == "L") {
				if (reqItemType == "G" || reqItemType == "Q") {
					NilTrans = actualGrossPr.toBigDecimal() * qtyReqd.toBigDecimal()
				}
				else if (reqItemType == "S"  || reqItemType == "F") {
					NilTrans = actualGrossPr.toBigDecimal()
				}
				else {
					NilTrans = 0
				}
			}
			else {
				if (reqItemType == "G" || reqItemType == "Q") {
					if (actualGrossPr.toBigDecimal() != 0) {
						NilTrans = actualGrossPr.toBigDecimal() * qtyReqd.toBigDecimal()
					}
					else {
						NilTrans = estPrice.toBigDecimal() * qtyReqd.toBigDecimal()
					}
				}
				else if (reqItemType == "S"  || reqItemType == "F") {
					if (actualGrossPr.toBigDecimal() != 0) {
						NilTrans = actualGrossPr.toBigDecimal()
					}
					else {
						NilTrans = estPrice.toBigDecimal()
					}
				}
				else {
					NilTrans = 0
				}
			}

			log.info("after NilTrans Calculation: $NilTrans")

			if (projectNo && projectNo != "") {
				String finalisedDate = getFinalisedDate(districtCode, projectNo)
				log.info("finalisedDate: $finalisedDate")
				if (finalisedDate &&  finalisedDate != "") {
					String errorMessage = "Transaksi tidak dapat dilakukan karena PRK sudah difinalisasi"
					String errorCode = "9999"
					screen.setErrorMessage(
							new MsoErrorMessage("",
									errorCode,
									errorMessage,
									MsoErrorMessage.ERR_TYPE_ERROR,
									MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

					MsoField errField = new MsoField()
					errField.setName("ACT_GROSS_PR1I")
					screen.setCurrentCursorField(errField)
					return screen
				}

				validateBudget(districtCode, projectNo)

				if (overBudget) {
					totalCommitment = commitmentAI + commitmentAO
					totalActual = actualAI + actualAO
					sisaAnggaran = totalBudget - totalCommitment - totalActual + currentPRValue

					if (sisaAnggaran <= 0) {
						sisaAnggaran = 0
					}

					log.info("Error Message Checkpoint...")
					log.info("totalBudget: $totalBudget")
					log.info("totalCommitment: $totalCommitment")
					log.info("totalActual: $totalActual")
					log.info("currentPRValue: $currentPRValue")
					log.info("sisaAnggaran: $sisaAnggaran")
					log.info("ProjNo: ---$projectNo---")
					log.info("ProjNoLength: " + projectNo.length())

					String totBudget
					String totActual
					String totCommitment
					String anggaranSisa
					String totCurrItem

					if (totalBudget && totalBudget != 0) {
						totBudget = formatter.format(totalBudget)
					} else {
						totBudget = "0.00"
					}

					if (totalActual && totalActual != 0) {
						totActual = formatter.format(totalActual)
					} else {
						totActual = "0.00"
					}

					if (totalCommitment && totalCommitment != 0) {
						totCommitment = formatter.format(totalCommitment)
					} else {
						totCommitment = "0.00"
					}

					if (sisaAnggaran && sisaAnggaran != 0) {
						anggaranSisa = formatter.format(sisaAnggaran)
					} else {
						anggaranSisa = "0.00"
					}

					if (TotalCurrItem && TotalCurrItem != 0) {
						totCurrItem = formatter.format(TotalCurrItem)
					} else {
						totCurrItem = "0.00"
					}

					String errorMessage = "Nilai transaksi overbudget: \r\n" +
							"Alokasi PRK: $totBudget \r\n " +
							"Realisasi PRK: $totActual \r\n" +
							"Commitment PRK: $totCommitment \r\n" +
							"Sisa Anggaran PRK: $anggaranSisa \r\n" +
							"Nilai transaksi saat ini sebesar $totCurrItem lebih besar dari nilai anggaran \r\n" +
							"Segera ajukan revisi anggaran kepada divisi terkait"
					String errorCode = "9999"

					screen.setErrorMessage(
							new MsoErrorMessage("",
									errorCode,
									errorMessage,
									MsoErrorMessage.ERR_TYPE_ERROR,
									MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

					MsoField errField = new MsoField()
					errField.setName("ACT_GROSS_PR1I")
					screen.setCurrentCursorField(errField)

					return screen
				}
			}
		}
		return null
	}

	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit MSM23EA logging.version: ${hookVersion}")
		return result
	}
}