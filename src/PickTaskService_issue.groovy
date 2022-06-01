package PJB

import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.edoi.ejb.msf100.MSF100Rec
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Key
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Rec
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Key
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Rec

/**
 * @EMS June 2018
 *
 * a9ra5213 - Inventory Asset PJB
 * a9aa4037 - Preventive Budget Control PJB
 **/

import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.edoi.ejb.msf621.MSF621Key
import com.mincom.ellipse.edoi.ejb.msf621.MSF621Rec
import com.mincom.ellipse.edoi.ejb.msf660.MSF660Key
import com.mincom.ellipse.edoi.ejb.msf660.MSF660Rec
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m0000.instances.DocumentKey
import com.mincom.ellipse.types.m3140.instances.DocumentKeyTypeDTO
import com.mincom.ellipse.types.m3140.instances.PickTaskIssueServiceResult
import com.mincom.ellipse.types.m3140.instances.PickTaskIssueDTO
import com.mincom.ellipse.types.m0000.instances.StockCode
import com.mincom.ellipse.types.m3140.instances.CompleteHoldingDetailsDTO
import com.mincom.ellipse.types.m0000.instances.TrackRefNo
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.eql.Constraint
import com.mincom.eql.impl.QueryImpl
import org.apache.derby.iapi.services.classfile.CONSTANT_Index_info

import javax.naming.InitialContext

import groovy.sql.Sql

import java.text.DecimalFormat
import java.text.SimpleDateFormat;

class PickTaskService_issue extends ServiceHook{
	String hookVersion = "2"
	String ProjAcct = "";
	String StrDstrct = "";
	String StrSTK = "";
	String StrREF = "";
	Boolean ErrFlag;
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)

	def getProjectFromWO(String district, String workOrder) {
		String result
		Constraint districtCode = MSF620Key.dstrctCode.equalTo(district)
		Constraint wONo = MSF620Key.workOrder.equalTo(workOrder)
		def query = new QueryImpl(MSF620Rec.class).and(districtCode).and(wONo)
		MSF620Rec msf620Rec = tools.edoi.firstRow(query)
		if (msf620Rec) {
			String projectNo = msf620Rec.getProjectNo()
			if (projectNo) {
				result = projectNo.trim()
			} else {
				result = ""
			}
		} else {
			result = ""
		}
		log.info("Project From WO: $result")
		return result
	}
	def getProjectNo(String district, String requisitionNo) {
		String result

		Constraint districtCode = MSF232Key.dstrctCode.equalTo(district)
		Constraint reqNo = MSF232Key.requisitionNo.equalTo(requisitionNo)

		def query = new QueryImpl(MSF232Rec.class).and(districtCode).and(reqNo)

		MSF232Rec msf232Rec = tools.edoi.firstRow(query)

		if (msf232Rec) {
			String prjNo = msf232Rec.getProjectNo().trim()
			if (prjNo != "") {
				result = prjNo
			} else {
				String workOrder = msf232Rec.getWorkOrder().trim()
				if (workOrder != "") {
					result = getProjectFromWO(district, workOrder)
				} else {
					result = ""
					log.info("result 111: $result")
				}
			}
		} else {
			String requisitionNo2 = requisitionNo.substring(0, 6) + "  000"
			Constraint districtCode2 = MSF232Key.dstrctCode.equalTo(district)
			Constraint reqNo2 = MSF232Key.requisitionNo.equalTo(requisitionNo2)

			def query2 = new QueryImpl(MSF232Rec.class).and(districtCode2).and(reqNo2)

			MSF232Rec msf232Rec2 = tools.edoi.firstRow(query2)

			if (msf232Rec2) {
				String prjNo2 = msf232Rec2.getProjectNo().trim()
				if (prjNo2 != "") {
					result = prjNo2
				} else {
					String workOrder2 = msf232Rec2.getWorkOrder().trim()
					if (workOrder2 != "") {
						result = getProjectFromWO(district, workOrder2)
					} else {
						result = ""
						log.info("result 222: $result")
					}
				}
			}
		}
		log.info("Project after search: $result")
		return result
	}
	def getPreqStkCode(String district, String pONo, String pOItemNo) {
		log.info("Arsiadi getPreqStkCode: $district - $pONo - $pOItemNo")
		String result
		Constraint purchaseOrderNo = MSF221Key.poNo.equalTo(pONo)
		Constraint purchaseOrderItem = MSF221Key.poItemNo.equalTo(pOItemNo)

		def query = new QueryImpl(MSF221Rec.class).and(purchaseOrderNo).and(purchaseOrderItem)
		MSF221Rec msf221Rec = tools.edoi.firstRow(query)

		if (msf221Rec) {
			result = msf221Rec.getPreqStkCode().trim()
		} else {
			result = ""
		}
		log.info("dfsggdfsddgfsdgfsgsgf: $result")
		return result
	}
	def getPoItemType(String district, String pONo, String pOItemNo) {
		String result
		InitialContext initialContext = new InitialContext()
		Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
		def sql = new Sql(dataSource)

		def msf221Rec = sql.firstRow("SELECT PO_ITEM_TYPE " +
				"FROM MSF221 " +
				"WHERE DSTRCT_CODE = '$district' AND PO_NO = '$pONo' AND PO_ITEM_NO = '$pOItemNo'")

		if (msf221Rec) {
			String[] poItemType = msf221Rec.PO_ITEM_TYPE
			result = poItemType[0].toString().trim()
			log.info("fasfasdfasfdasdfasf: $result")
		} else {
			result = ""
		}
		return result
	}
	def getPoGrossPrice(String district, String pONo, String pOItemNo) {
		BigDecimal result
		InitialContext initialContext = new InitialContext()
		Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
		def sql = new Sql(dataSource)

		def msf221Rec = sql.firstRow("SELECT GROSS_PRICE_P " +
				"FROM MSF221 " +
				"WHERE DSTRCT_CODE = '$district' AND PO_NO = '$pONo' AND PO_ITEM_NO = '$pOItemNo'")

		if (msf221Rec) {
			BigDecimal[] grossPrice = msf221Rec.GROSS_PRICE_P
			result = grossPrice[0].toBigDecimal()
		} else {
			result = 0
		}
		return result
	}
	def validateBudget(String districtCode, String projectNo, BigDecimal tranValue) {
		Boolean overBudget

		BigDecimal totalBudget = getTotalBudget(districtCode, projectNo)
		BigDecimal commitmentAI = getCommitmentAI(districtCode, projectNo)
		BigDecimal commitmentAO = getCommitmentAO(districtCode, projectNo)
		BigDecimal actualAI = getActualAI(districtCode, projectNo)
		BigDecimal actualAO = getActualAO(districtCode, projectNo)

		log.info("totalBudget: $totalBudget")
		log.info("commitmentAI: $commitmentAI")
		log.info("commitmentAO: $commitmentAO")
		log.info("actualAI: $actualAI")
		log.info("actualAO: $actualAO")
		log.info("tranValue: $tranValue")

		BigDecimal pembilang = tranValue + commitmentAI + commitmentAO + actualAI + actualAO
		BigDecimal penyebut = totalBudget
		BigDecimal totalAAA = pembilang / penyebut * 100

		log.info("Pembilangggg: $pembilang")
		log.info("Penyebut: $penyebut")
		log.info("TotalAAA: $totalAAA")

		BigDecimal calculatedValue = ((tranValue + commitmentAI + commitmentAO + actualAI + actualAO) / totalBudget) * 100
		log.info("calculatedValue: $calculatedValue")

		if (calculatedValue > 100) {
			overBudget = true
		} else {
			overBudget = false
		}
		return overBudget
	}
	def checkZeroBudget(String districtCode, String projectNo) {
		log.info("Checking zero budget...")
		Boolean result
		BigDecimal totalBudget = getTotalBudget(districtCode, projectNo)
		if(!totalBudget || totalBudget == 0) {
			result = true
		} else {
			result = false
		}
		return result
	}
	def getTotalBudget(String districtCode, String projectNo) {
		log.info("Get total budget of Project Nbr: " + projectNo)
		BigDecimal result = 0
		String projectNbr
		if(projectNo && projectNo.trim() != "") {
			String projectType
			if(projectNo.substring(2, 4) == "3Y"
					|| projectNo.substring(2, 4) == "4A"
					|| projectNo.substring(2, 4) == "4B") {
				projectType = "AI"
			}
			else if(projectNo.substring(2, 4) == "2O"
					|| projectNo.substring(2, 4) == "3O") {
				projectType = "AO"
			}
			else if(projectNo.substring(3, 4) == "G"
					|| projectNo.substring(3, 4) == "H"
					|| projectNo.substring(3, 4) == "I"
					|| projectNo.substring(3, 4) == "J"
					|| projectNo.substring(3, 4) == "K"
					|| projectNo.substring(3, 4) == "L"
					|| projectNo.substring(3, 4) == "N"
					|| projectNo.substring(3, 4) == "P") {
				projectType = "HAR"
			}
			else if(projectNo.substring(3, 4) == "S") {
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
							"AND PROJECT_NO = '$projectNo' " +
							"AND DSTRCT_CODE = '$districtCode'")

					budgetInTotal = sql.firstRow("SELECT TOT_EST_COST totEstCost " +
							"FROM MSF667 " +
							"WHERE CATEGORY_CODE = ' ' " +
							"AND EXP_REV_IND = 'E' " +
							"AND PROJECT_NO = '$projectNo' " +
							"AND DSTRCT_CODE = '$districtCode'")
				}
				else if(projectType == "ADM") {
					projectNbr = projectNo.substring(0, 4)
					log.info("Query Command: " +
							"SELECT TOT_EST_COST totEstCost " +
							"FROM MSF667 " +
							"WHERE CATEGORY_CODE = ' ' " +
							"AND EXP_REV_IND = 'E' " +
							"AND PROJECT_NO = '$projectNbr' " +
							"AND DSTRCT_CODE = '$districtCode'")

					budgetInTotal = sql.firstRow("SELECT TOT_EST_COST totEstCost " +
							"FROM MSF667 " +
							"WHERE CATEGORY_CODE = ' ' " +
							"AND EXP_REV_IND = 'E' " +
							"AND PROJECT_NO = '$projectNbr' " +
							"AND DSTRCT_CODE = '$districtCode'")
				}
				else {
					budgetInTotal = null
				}

				if(budgetInTotal){
					String[] budget = budgetInTotal.totEstCost
					result = budget[0].trim() as BigDecimal
				}
				else {
					result = 0
				}
				log.info("Query result totalBudget: $result")
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
			}
		}
		log.info("result from getTotalBudget: $result")
		return result
	}
	def getCommitmentAI(String districtCode, String projectNbr) {
		log.info("Get AI Commitment of Project Nbr: " + projectNbr)
		BigDecimal result = 0
		if (projectNbr) {
			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)
				log.info("AAAA Query command: " +
						"SELECT COMMITMENTS " +
						"FROM VPBC_COMMITMENT_AI " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				def aIcommitment = sql.firstRow("SELECT COMMITMENTS " +
						"FROM VPBC_COMMITMENT_AI " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				if (aIcommitment) {
					String[] aiCommit = aIcommitment.COMMITMENTS
					result = aiCommit[0].trim() as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result commitmentAI: " + result)
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
			}
		}
		return result
	}
	def getCommitmentAO(String districtCode, String projectNbr) {
		log.info("Get AO Commitment of Project Nbr: " + projectNbr)
		BigDecimal result
		if(projectNbr) {
			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)
				log.info("Query command: " +
						"SELECT COMMITMENTS " +
						"FROM VPBC_COMMITMENT_AO " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				def aOCommitment = sql.firstRow("SELECT COMMITMENTS " +
						"FROM VPBC_COMMITMENT_AO " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				if(aOCommitment){
					String[] aoCommit = aOCommitment.COMMITMENTS
					result =  aoCommit[0] as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result commitmentAO: " + result)
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
			}
		}
		return result
	}
	def getActualAI(String districtCode, String projectNbr) {
		log.info("Get AI Actuals of Project Nbr: " + projectNbr)
		BigDecimal result = 0
		if(projectNbr) {
			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)
				log.info("Query command: " +
						"SELECT ACTUALS " +
						"FROM VPBC_ACTUAL_AI " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				def aIActual = sql.firstRow("SELECT ACTUALS " +
						"FROM VPBC_ACTUAL_AI " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				if(aIActual) {
					String[] aiAct = aIActual.ACTUALS
					result = aiAct[0] as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result actualAI: " + result)
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
			}
		}
		return result
	}
	def getActualAO(String districtCode, String projectNbr) {
		log.info("Get AO Actuals of Project Nbr: " + projectNbr)
		BigDecimal result
		if(projectNbr) {
			try {
				InitialContext initialContext = new InitialContext()
				Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql = new Sql(dataSource)
				log.info("Query command: " +
						"SELECT COMMITMENTS " +
						"FROM VPBC_ACTUAL_AO " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				def aOActual = sql.firstRow("SELECT ACTUALS " +
						"FROM VPBC_ACTUAL_AO " +
						"WHERE DSTRCT_CODE = '" + districtCode +
						"' AND PROJECT_NO = '" + projectNbr + "'")
				if(aOActual){
					String[] aoAct = aOActual.ACTUALS
					result = aoAct[0] as BigDecimal
				} else {
					result = 0
				}
				log.info("Query result actualAO: " + result)
			} catch(EnterpriseServiceOperationException e) {
				List <ErrorMessageDTO> listError = e.getErrorMessages()
				listError.each{ErrorMessageDTO errorDTO ->
					log.info ("Error Code: " + errorDTO.getCode())
					log.info ("Error Message: " + errorDTO.getMessage())
					log.info ("Error Fields: " + errorDTO.getFieldName())
				}
			}
		}
		return result
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
	def integrateActualCost(Object input, Object result){
		log.info("Arsiadi integrateActualCost PickTaskService_issue version: $hookVersion")

		InitialContext initial = new InitialContext()
		Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
		def sql = new Sql(CAISource)

		InetAddress ip
		ip = InetAddress.getLocalHost()
		String hostname = ip.getHostName()
		String hostUrl = getHostUrl(hostname)

//      mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
		String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-ACTCOST-XML"

		PickTaskIssueServiceResult responseDto = (PickTaskIssueServiceResult) result
//		PickTaskIssueDTO[] requestDto = (PickTaskIssueDTO[]) input

		PickTaskIssueDTO pickTaskIssueDTO = responseDto.getPickTaskIssueDTO()
		String documentKey = pickTaskIssueDTO.getDocumentKey().getValue()
		String reqNbr = documentKey.substring(4, 10)
		String reqItemNbr = documentKey.substring(10)
		String districtNo = documentKey.substring(0,4)

		String districtFormatted
		if (districtNo){
			districtFormatted = districtNo.trim().substring(2) == "PT" ||
					districtNo.trim().substring(2) == "IN" ||
					districtNo.trim().substring(2) == "RB" ||
					districtNo.trim().substring(2) == "PC" ||
					districtNo.trim().substring(2) == "TA" ||
					districtNo.trim().substring(2) == "MK" ||
					districtNo.trim().substring(2) == "MT" ? districtNo.trim().substring(2) :
					districtNo.trim() == "SGRK" ? "GR" : "PLNUPJB"
		}

		log.info("documentKey: $documentKey")
		log.info("reqNbr: $reqNbr")
		log.info("reqItemNbr: $reqItemNbr")
		log.info("districtNo: $districtNo")
		log.info("district formatted: $districtFormatted")

		sql.eachRow("select distinct work_order from msf232 " +
				"where dstrct_code = '$districtNo' " +
				"and substr(requisition_no, 1, 6) like '${reqNbr}%' and work_order <> ' '"){row ->
			String workOrder = row.WORK_ORDER as String
			workOrder = workOrder.trim() != "" ? workOrder.trim() : ""
			log.info("workOrder: $workOrder")
			if (workOrder != ""){
//				String qryIreqItem = "WITH MAT AS(SELECT DSTRCT_CODE\n" +
//						"                  ,WORK_ORDER\n" +
//						"                  ,SUM(TRAN_AMOUNT) MAT_COST\n" +
//						"            FROM MSF900 WHERE WORK_ORDER = '$workOrder'\n" +
//						"            AND (REC900_TYPE = 'S' OR (REC900_TYPE = 'P' AND SERV_ITM_IND = ' '))\n" +
//						"            AND DSTRCT_CODE = '$districtNo'\n" +
//						"            GROUP BY DSTRCT_CODE\n" +
//						"                    ,WORK_ORDER), \n" +
//						"     SER AS(SELECT DSTRCT_CODE\n" +
//						"                 ,WORK_ORDER\n" +
//						"                 ,SUM(TRAN_AMOUNT) SERV_COST \n" +
//						"            FROM MSF900\n" +
//						"            WHERE WORK_ORDER = '$workOrder'\n" +
//						"            AND DSTRCT_CODE = '$districtNo'\n" +
//						"            AND REC900_TYPE = 'P'\n" +
//						"            AND serv_itm_ind = 'S'\n" +
//						"            GROUP BY DSTRCT_CODE\n" +
//						"                    ,WORK_ORDER)\n" +
//						"SELECT A.DSTRCT_CODE\n" +
//						"      ,A.WORK_ORDER\n" +
//						"      ,CASE WHEN B.MAT_COST IS NOT NULL THEN B.MAT_COST ELSE 0 END MAT_COST\n" +
//						"      ,CASE WHEN C.SERV_COST IS NOT NULl THEN C.SERV_COST ELSE 0 END SERV_COST\n" +
//						"      ,(CASE WHEN B.MAT_COST IS NOT NULL THEN B.MAT_COST ELSE 0 END) + (CASE WHEN C.SERV_COST IS NOT NULL THEN C.SERV_COST ELSE 0 END) TOTAL_COST\n" +
//						"FROM MSF620 A\n" +
//						"LEFT OUTER JOIN MAT B ON A.DSTRCT_CODE = B.DSTRCT_CODE AND A.WORK_ORDER = B.WORK_ORDER \n" +
//						"LEFT OUTER JOIN SER C ON A.DSTRCT_CODE = C.DSTRCT_CODE AND A.WORK_ORDER = C.WORK_ORDER \n" +
//						"WHERE A.DSTRCT_CODE = '$districtNo' \n" +
//						"AND A.WORK_ORDER = '$workOrder'"
				String qryIreqItem = "WITH WOCOST AS (SELECT A.DSTRCT_CODE\n" +
						"      ,A.WORK_ORDER\n" +
						"      ,SUM(CASE WHEN A.DSTRCT_CODE = '$districtNo' AND (A.REC900_TYPE = 'S' OR (A.REC900_TYPE = 'P' AND B.SERV_ITM_IND = ' ')) THEN B.TRAN_AMOUNT ELSE 0 END) MAT_COST\n" +
						"      ,SUM(CASE WHEN A.DSTRCT_CODE = '$districtNo' AND A.REC900_TYPE = 'P' AND B.SERV_ITM_IND = 'S' THEN B.TRAN_AMOUNT ELSE 0 END) SERV_COST\n" +
						"FROM (SELECT * FROM MSFX99 WHERE DSTRCT_CODE = '$districtNo' AND WORK_ORDER = '$workOrder') A\n" +
						"INNER JOIN MSF900 B ON (A.DSTRCT_CODE = B.DSTRCT_CODE AND A.PROCESS_DATE = B.PROCESS_DATE AND A.FULL_PERIOD = B.FULL_PERIOD AND A.REC900_TYPE = B.REC900_TYPE AND A.TRANSACTION_NO = B.TRANSACTION_NO AND A.USERNO = B.USERNO AND A.ACCOUNT_CODE = B.ACCOUNT_CODE AND A.WORK_ORDER = B.WORK_ORDER)\n" +
						"GROUP BY A.DSTRCT_CODE, A.WORK_ORDER)\n" +
						"SELECT WO.DSTRCT_CODE\n" +
						"      ,WO.WORK_ORDER\n" +
						"      ,CASE WHEN WOCOST.MAT_COST IS NOT NULL THEN WOCOST.MAT_COST ELSE 0 END MAT_COST\n" +
						"      ,CASE WHEN WOCOST.SERV_COST IS NOT NULl THEN WOCOST.SERV_COST ELSE 0 END SERV_COST\n" +
						"      ,(CASE WHEN WOCOST.MAT_COST IS NOT NULL THEN WOCOST.MAT_COST ELSE 0 END) + (CASE WHEN WOCOST.SERV_COST IS NOT NULL THEN WOCOST.SERV_COST ELSE 0 END) TOTAL_COST\n" +
						"FROM MSF620 WO\n" +
						"LEFT OUTER JOIN WOCOST ON (WO.DSTRCT_CODE = WOCOST.DSTRCT_CODE AND WO.WORK_ORDER = WOCOST.WORK_ORDER)\n" +
						"WHERE WO.DSTRCT_CODE = '$districtNo'\n" +
						"AND WO.WORK_ORDER = '$workOrder'"
				log.info("qryIreqItem: $qryIreqItem")

				def sql2 = new Sql(CAISource)
				def queryIreqItem = sql2.firstRow(qryIreqItem)
				log.info("queryIreqItem: $queryIreqItem")

				BigDecimal matCost = 0
				BigDecimal servCost = 0
				BigDecimal totalCost = 0

				if (queryIreqItem){
					matCost = queryIreqItem.MAT_COST as BigDecimal
					servCost = queryIreqItem.SERV_COST as BigDecimal
					totalCost = queryIreqItem.TOTAL_COST as BigDecimal
				}
				log.info("matCost: $matCost")
				log.info("servCost: $servCost")
				log.info("totalCost: $totalCost")

				String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<SyncMXE-ACTCOST-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:37:06+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
						"  <MXE-ACTCOST-XMLSet>\n" +
						"    <WORKORDER>\n" +
						"      <WONUM>$workOrder</WONUM>\n" +
						"      <TOTALCOSTELLIPSE>$totalCost</TOTALCOSTELLIPSE>\n" +
						"      <MATCOSTELLIPSE>$matCost</MATCOSTELLIPSE>\n" +
						"      <SERVCOSTELLIPSE>$servCost</SERVCOSTELLIPSE>\n" +
						"      <ORGID>UBPL</ORGID>\n" +
						"      <SITEID>$districtFormatted</SITEID>\n" +
						"    </WORKORDER>\n" +
						"  </MXE-ACTCOST-XMLSet>\n" +
						"</SyncMXE-ACTCOST-XML>"

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
					return input
				}
			}
		}
	}
	def integrateActualQty(Object input, Object result){
		log.info("Arsiadi integrateActualQty version: $hookVersion")

		InitialContext initial = new InitialContext()
		Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
		def sql = new Sql(CAISource)

		BigDecimal qtyIssued = 0

		InetAddress ip
		ip = InetAddress.getLocalHost()
		String hostname = ip.getHostName()
		String hostUrl = getHostUrl(hostname)

//      mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
		String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-ACTMAT-XML"

		PickTaskIssueServiceResult responseDto = (PickTaskIssueServiceResult) result
		PickTaskIssueDTO[] requestDto = (PickTaskIssueDTO[]) input

		PickTaskIssueDTO pickTaskIssueDTO = responseDto.getPickTaskIssueDTO()
		String documentKey = pickTaskIssueDTO.getDocumentKey().getValue()
		String reqNbr = documentKey.substring(4, 10)
		String issueWarehouseID = pickTaskIssueDTO.getIssueWarehouseId() ? pickTaskIssueDTO.getIssueWarehouseId().getValue().trim(): ""
		String reqItemNbr = documentKey.substring(10)
		String districtNo = documentKey.substring(0,4)
		BigDecimal quantityPicked = pickTaskIssueDTO.getQuantityPicked().getValue()

		def date = new Date()
		def sdf = new SimpleDateFormat("yyyy-MM-dd")
		String currentDate = sdf.format(date)

		String districtFormatted
		if (districtNo){
			districtFormatted = districtNo.trim().substring(2) == "PT" ||
					districtNo.trim().substring(2) == "IN" ||
					districtNo.trim().substring(2) == "RB" ||
					districtNo.trim().substring(2) == "PC" ||
					districtNo.trim().substring(2) == "TA" ||
					districtNo.trim().substring(2) == "MK" ||
					districtNo.trim().substring(2) == "MT" ? districtNo.trim().substring(2) :
					districtNo.trim() == "SGRK" ? "GR" : "PLNUPJB"
		}

		log.info("documentKey: $documentKey")
		log.info("reqNbr: $reqNbr")
		log.info("reqItemNbr: $reqItemNbr")
		log.info("district formatted: $districtFormatted")
		log.info("districtNo: $districtNo")
		log.info("quantityPicked: $quantityPicked")

		sql.eachRow("with ireq_alloc as (\n" +
				"select distinct \n" +
				"       dstrct_code\n" +
				"      ,substr(requisition_no, 1, 6) ireq_no\n" +
				"      ,work_order \n" +
				"from msf232\n" +
				")\n" +
				"select a.dstrct_code\n" +
				"      ,a.ireq_no\n" +
				"      ,a.ireq_item\n" +
				"      ,a.stock_code\n" +
				"      ,a.whouse_id\n" +
				"      ,a.last_acq_date\n" +
				"      ,c.unit_of_issue\n" +
				"      ,b.work_order\n" +
				"      ,a.qty_issued\n" +
				"      ,a.qty_req\n" +
				"      ,a.item_price\n" +
				"      ,trim(c.item_name || ' ' || c.stk_desc) stk_desc\n" +
				"from msf141 a\n" +
				"inner join ireq_alloc b on a.dstrct_code = b.dstrct_code and a.ireq_no = b.ireq_no\n" +
				"inner join msf100 c on a.stock_code = c.stock_code\n" +
				"where a.dstrct_code = '$districtNo'\n" +
				"and a.ireq_no = '$reqNbr'\n" +
				"and a.ireq_item = '$reqItemNbr'") {row ->
			String workOrder = row.WORK_ORDER as String
			String stockCode = row.STOCK_CODE as String
			String stockDesc = row.STK_DESC as String
			BigDecimal qtyReq = row.QTY_REQ as BigDecimal
			BigDecimal qtyIss = row.QTY_ISSUED as BigDecimal
			stockDesc = stockDesc ? stockDesc.trim() : ""
			if (stockDesc != ""){
				if (stockDesc.trim().length() > 50){
					stockDesc = stockDesc.substring(0, 49)
				}
			}
//			String lastAcqDate = row.LAST_ACQ_DATE as String
			String transDate = currentDate
//			BigDecimal qtyIssued = row.QTY_ISSUED as BigDecimal
			qtyIssued = quantityPicked ? quantityPicked : qtyIss ? qtyIss : qtyReq
			String uom = row.UNIT_OF_ISSUE as String
			BigDecimal itemPrice = row.ITEM_PRICE as BigDecimal

			log.info("workOrder: $workOrder")
			log.info("stockCode: $stockCode")
			log.info("stockDesc: $stockDesc")
			log.info("transDate: $transDate")
			log.info("qtyIssued: $qtyIssued")
			log.info("qtyIss: $qtyIss")
			log.info("qtyReq: $qtyReq")
			log.info("uom: $uom")
			log.info("itemPrice: $itemPrice")

			BigDecimal totalPrice = qtyIssued * itemPrice
//			String storeRoom = "WH$districtFormatted"
			String storeRoom = issueWarehouseID
			String binCode = "${districtFormatted}1-1"

			workOrder = workOrder.trim() != "" ? workOrder.trim() : ""
			log.info("workOrder: $workOrder")
			if (workOrder != ""){
				String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<SyncMXE-ACTMAT-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:37:06+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
						"    <MXE-ACTMAT-XMLSet>\n" +
						"        <ACTMATELLIPSE>\n" +
						"            <WONUM>$workOrder</WONUM>\n" +
						"            <ITEMNUM>$stockCode</ITEMNUM>\n" +
						"            <DESCRIPTION>${org.apache.commons.lang.StringEscapeUtils.escapeXml(stockDesc)}</DESCRIPTION>\n" +
						"            <TRANSDATE>$transDate</TRANSDATE>\n" +
						"            <QTY>$qtyIssued</QTY>\n" +
						"            <UOM>$uom</UOM>\n" +
						"            <PRICE>$itemPrice</PRICE>\n" +
						"            <TOTALPRICE>$totalPrice</TOTALPRICE>\n" +
						"            <STOREROOM>$storeRoom</STOREROOM>\n" +
						"            <BIN>$binCode</BIN>\n" +
						"            <ORGID>UBPL</ORGID>\n" +
						"            <SITEID>$districtFormatted</SITEID>\n" +
						"        </ACTMATELLIPSE>\n" +
						"    </MXE-ACTMAT-XMLSet>\n" +
						"</SyncMXE-ACTMAT-XML>"

				log.info("ARS --- XML: $xmlMessage")

				def url = new URL(postUrl)
				HttpURLConnection connection = url.openConnection()
				connection.setRequestMethod("POST")
				connection.setDoOutput(true)
				connection.setRequestProperty("Content-Type", "application/xml")
				connection.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")

				try{
					connection.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
					log.info("responsecode: ${connection.getResponseCode()}")
				}
				catch (Exception e){
					log.info("exception: $e")
				}

				if (connection.getResponseCode() != 200) {
					String responseMessage = connection.content.toString()
					log.info("responseMessage: $responseMessage")
					String errorCode = "9999"

					throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
									errorCode, responseMessage, "", 0, 0))
					return input
				}
			}
			updateSOH(input, result, stockCode, stockDesc, uom, qtyIssued)
		}
	}
	def updateSOH(Object input, Object result, String stockCode, String stockDesc, String unitOfMeasure, BigDecimal qtyIssued){
		InitialContext initial = new InitialContext()
		Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
		def sql = new Sql(CAISource)

		InetAddress ip
		ip = InetAddress.getLocalHost()
		String hostname = ip.getHostName()
		String hostUrl = getHostUrl(hostname)

//      mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
		String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-ITEM-XML"

		PickTaskIssueServiceResult responseDto = (PickTaskIssueServiceResult) result
		PickTaskIssueDTO[] requestDto = (PickTaskIssueDTO[]) input

		PickTaskIssueDTO pickTaskIssueDTO = responseDto.getPickTaskIssueDTO()
		String documentKey = pickTaskIssueDTO.getDocumentKey().getValue()
		String reqNbr = documentKey.substring(4, 10)
		String reqItemNbr = documentKey.substring(10)
		String districtNo = documentKey.substring(0,4)
		BigDecimal quantityPicked = pickTaskIssueDTO.getQuantityPicked().getValue()
		String inventCategory = pickTaskIssueDTO.getSelectedCategory().getValue()

		def date = new Date()
		def sdf = new SimpleDateFormat("yyyy-MM-dd")
		String currentDate = sdf.format(date)

		String districtFormatted
		if (districtNo){
			districtFormatted = districtNo.trim().substring(2) == "PT" ||
					districtNo.trim().substring(2) == "IN" ||
					districtNo.trim().substring(2) == "RB" ||
					districtNo.trim().substring(2) == "PC" ||
					districtNo.trim().substring(2) == "TA" ||
					districtNo.trim().substring(2) == "MK" ||
					districtNo.trim().substring(2) == "MT" ? districtNo.trim().substring(2) :
					districtNo.trim() == "SGRK" ? "GR" : "PLNUPJB"
		}
		inventCategory = inventCategory ? inventCategory.trim() : ""

//		String warehouseId = "WH$districtFormatted"
		String warehouseId = pickTaskIssueDTO.getIssueWarehouseId() ? pickTaskIssueDTO.getIssueWarehouseId().getValue() : ""
		log.info("warehouseId: $warehouseId")
		log.info("documentKey: $documentKey")
		log.info("reqNbr: $reqNbr")
		log.info("reqItemNbr: $reqItemNbr")
		log.info("district formatted: $districtFormatted")

		stockDesc = stockDesc ? stockDesc.trim() : ""
		if (stockDesc != ""){
			if (stockDesc.trim().length() > 50){
				stockDesc = stockDesc.substring(0, 49)
			}
		}
		stockDesc = org.apache.commons.lang.StringEscapeUtils.escapeXml(stockDesc)
		log.info("stockCode: $stockCode")
		log.info("stockDesc: $stockDesc")
		log.info("warehouseId: $warehouseId")
		log.info("inventCategory: $inventCategory")

		String queryCommand = "with b as (\n" +
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
				"where a.dstrct_code = '$districtNo'\n" +
				"and a.stock_code = '$stockCode'"
		log.info("queryCommand: $queryCommand")
		def  queryResult = sql.firstRow(queryCommand)
		log.info("queryResult: $queryResult")

		BigDecimal soh = (queryResult ? queryResult.SOH : "0") as BigDecimal
		BigDecimal duesOut = (queryResult ? queryResult.DUES_OUT : "0") as BigDecimal
		BigDecimal available = soh - duesOut

		log.info("soh: $soh")
		log.info("duesOut: $duesOut")
		log.info("quantityPicked: $quantityPicked")
		log.info("qtyIssued: $qtyIssued")
		log.info("avalable: $available")

		String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<SyncMXE-ITEM-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:45:48+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
				"    <MXE-ITEM-XMLSet>\n" +
				"        <ELLIPSEITEM>\n" +
				"            <ITEMNUM>$stockCode</ITEMNUM>\n" +
				"            <DESCRIPTION>$stockDesc</DESCRIPTION>\n" +
				"            <AVAILABLE>$available</AVAILABLE>\n" +
				"            <SOH>$soh</SOH>\n" +
				"            <UOM>$unitOfMeasure</UOM>\n" +
				"            <STOREROOM>$warehouseId</STOREROOM>\n" +
				"            <ORGID>UBPL</ORGID>\n" +
				"            <SITEID>$districtFormatted</SITEID>\n" +
				"            <DISTRICT>$districtNo</DISTRICT>\n" +
				"            <CATEGORY>$inventCategory</CATEGORY>\n" +
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
			return input
		}
	}
	String getHostUrl(String hostName){
		String result
		String instance

		InitialContext initialContext = new InitialContext()
		Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
		Sql sql = new Sql(dataSource)


		if (hostName.contains("ellprd")){
			instance = "ELLPRD"
		}
		else if (hostName.contains("elltrn")){
			instance = "ELLTRN"
		}
		else if (hostName.contains("elltst")){
			instance = "ELLTST"
		}
		else {
			instance = "ELLDEV"
		}

		String queryMSF010 = "select table_desc as tableDesc from msf010 where table_type = '+MAX' and table_code = '$instance'"
		Object queryMSF010Result = sql.firstRow(queryMSF010)
		result = queryMSF010Result ? queryMSF010Result.tableDesc ? queryMSF010Result.tableDesc.trim(): "" : ""

		return result
	}

	@Override
	Object onPreExecute(Object input) {
		log.info("[ARSIADI] Hooks PickTaskService_issue onPreExecute logging.version: $hookVersion")

		String pBCActiveFlag = getModuleSwitch("+PBC", "MSE1TP")
		log.info("PBC Active Flag: $pBCActiveFlag")
		if (pBCActiveFlag != "" && pBCActiveFlag != "Y"|| pBCActiveFlag == "") {
			return null
		}

		PickTaskIssueDTO pickTaskIssueDTO = (PickTaskIssueDTO) input
		CompleteHoldingDetailsDTO[] completeHoldingDetailsDTOS = pickTaskIssueDTO.getCompleteHoldingDetailsDTO()

		for (int i = 0; i < completeHoldingDetailsDTOS.length; i++) {
			String documentKey = pickTaskIssueDTO.getDocumentKey().getValue()
			BigDecimal quantity = completeHoldingDetailsDTOS[i].getQuantity().getValue()
			String stockCode = pickTaskIssueDTO.getStockCode().getValue()
			String inventCat = completeHoldingDetailsDTOS[i].getInventoryCategory().getValue()

			String districtCode = documentKey.substring(0, 4)
			String issueReqNo = documentKey.substring(4, 10)
			String issueReqItem = documentKey.substring(10)

			log.info("districtCode: $districtCode")
			log.info("issueReqNo: $issueReqNo")
			log.info("issueReqItem: $issueReqItem")
			log.info("quantity: $quantity")
			log.info("stockCode: $stockCode")
			log.info("inventCat: $inventCat")

			String queryInventCostPr
			if (inventCat && inventCat.trim() != "") {
				queryInventCostPr = "SELECT CASE WHEN B.INVENT_COST_PR IS NOT NULL THEN B.INVENT_COST_PR " +
						"ELSE A.INVENT_COST_PR END AS INVENT_COST_PR " +
						"FROM MSF170 a LEFT OUTER JOIN MSF194 b ON a.dstrct_code = b.dstrct_code AND a.stock_code = b.stock_code " +
						"WHERE a.STOCK_CODE = '$stockCode' " +
						"AND a.DSTRCT_CODE = '$districtCode' " +
						"AND (B.INVENT_CAT = '$inventCat' OR B.INVENT_CAT IS NULL)"
			} else {
				queryInventCostPr = "SELECT INVENT_COST_PR " +
						"FROM MSF170 " +
						"WHERE STOCK_CODE = '$stockCode' " +
						"AND DSTRCT_CODE = '$districtCode'"
			}
			log.info("queryInventCostPr: $queryInventCostPr")

			InitialContext context = new InitialContext()
			Object source = context.lookup("java:jboss/datasources/ReadOnlyDatasource")
			def q = new Sql(source)
			def qr = q.firstRow(queryInventCostPr)
			log.info("qr: $qr")
			BigDecimal inventCostPr = qr.INVENT_COST_PR as BigDecimal
			log.info("inventCostPr: $inventCostPr")

			InitialContext initialContext = new InitialContext()
			Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
			def sql = new Sql(dataSource)
			String queryCommand = "SELECT PROJECT_NO, WORK_ORDER FROM MSF232 WHERE DSTRCT_CODE = '$districtCode' AND REQUISITION_NO = '$issueReqNo  $issueReqItem'"
			log.info("queryCommand: $queryCommand")
			def  queryResult = sql.firstRow(queryCommand)
			log.info("queryResult: $queryResult")

			String projectNo
			String workOrder
			if (queryResult) {
				projectNo = queryResult.PROJECT_NO
				workOrder = queryResult.WORK_ORDER
				log.info("projectNoooo1: $projectNo")
				log.info("workOrder: $workOrder")

				if (projectNo.trim() == "" && workOrder.trim() != "") {
					Constraint district = MSF620Key.dstrctCode.equalTo(districtCode)
					Constraint woNumber = MSF620Key.workOrder.equalTo(workOrder)

					def qry = new QueryImpl(MSF620Rec.class).and(district).and(woNumber)
					MSF620Rec msf620Rec = tools.edoi.firstRow(qry)
					if (msf620Rec) {
						projectNo = msf620Rec.getProjectNo()
						log.info("projectNo from WO: $projectNo")
					}
				}
			} else {
				InitialContext initialContext2 = new InitialContext()
				Object dataSource2 = initialContext2.lookup("java:jboss/datasources/ReadOnlyDatasource")
				def sql2 = new Sql(dataSource2)
				String queryCommand2 = "SELECT PROJECT_NO, WORK_ORDER FROM MSF232 WHERE DSTRCT_CODE = '$districtCode' AND REQUISITION_NO = '$issueReqNo  0000'"
				log.info("queryCommand2: $queryCommand2")

				def queryResult2 = sql.firstRow(queryCommand2)
				log.info("queryResult2: $queryResult2")

				String projectNo2
				String workOrder2
				if (queryResult2) {
					projectNo2 = queryResult2.PROJECT_NO
					workOrder2 = queryResult2.WORK_ORDER
					log.info("projectNoooo2: $projectNo2")
					log.info("workOrder2: $workOrder2")

					if (projectNo2.trim() == "" && workOrder2.trim() != "") {
						Constraint district2 = MSF620Key.dstrctCode.equalTo(districtCode)
						Constraint woNumber2 = MSF620Key.workOrder.equalTo(workOrder2)

						def qry2 = new QueryImpl(MSF620Rec.class).and(district2).and(woNumber2)
						MSF620Rec msf620Rec2 = tools.edoi.firstRow(qry2)
						if (msf620Rec2) {
							projectNo = msf620Rec2.getProjectNo()
							log.info("projectNo from WO2: $projectNo")
							if (!projectNo || (projectNo && projectNo.trim() == "")) {
								throw new EnterpriseServiceOperationException(
										new ErrorMessageDTO(
												"9999",
												"Kode PRK belum dimasukkan di Work Order",
												"stockCode",
												0,
												0
										)
								)
								return input
							}
						}
					} else {
						projectNo = projectNo2
					}
				}
				log.info("projectNoooo2: $projectNo")
			}
			log.info("projectNo To Process: $projectNo")
			BigDecimal tranValue = quantity * inventCostPr

			if (projectNo && projectNo != "") {
				String finalisedDate = getFinalisedDate(districtCode, projectNo)
				log.info("finalisedDate: $finalisedDate")
				if (finalisedDate && finalisedDate != "") {
					String errorMessage = "Transaksi tidak dapat dilakukan karena PRK sudah difinalisasi"
					String errorCode = "9999"
					throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
									errorCode,
									errorMessage,
									"stockCode",
									0,
									0
							)
					)
					return input
				}
			}

			Boolean overBudget = validateBudget(districtCode, projectNo, tranValue)
			log.info("overBudget: $overBudget")

			if (overBudget) {
				BigDecimal totalCommitment = getCommitmentAI(districtCode, projectNo) +
						getCommitmentAO(districtCode, projectNo)
				BigDecimal totalActual = getActualAI(districtCode, projectNo) +
						getActualAO(districtCode, projectNo)
				BigDecimal totalBudget = getTotalBudget(districtCode, projectNo)
				BigDecimal remainingBudget = totalBudget - totalActual - totalCommitment

				DecimalFormat formatter = new DecimalFormat("#,###.00")

				String totBudget = formatter.format(totalBudget)
				String totCommit = formatter.format(totalCommitment)
				String totActual = formatter.format(totalActual)
				String remainBudget = formatter.format(remainingBudget)
				String itmValue = formatter.format(tranValue)

				String errorMessage = "Nilai transaksi overbudget: \n" +
						"Alokasi PRK: $totBudget\n " +
						"Realisasi PRK: $totActual\n" +
						"Commitment PRK: $totCommit\n" +
						"Sisa Anggaran PRK: $remainBudget\n" +
						"Nilai transaksi saat ini sebesar $itmValue lebih besar dari nilai anggaran\n" +
						"Segera ajukan revisi anggaran kepada divisi terkait"
				String errorCode = "9999"

				throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
								errorCode,
								errorMessage,
								"stockCode",
								0,
								0
						)
				)
				return input
			}
		}
		return null
	}

	@Override
	Object onPostExecute(Object input, Object result) {
		
		log.info("[ARSIADI] Hooks PickTaskService_issue onPostExecute logging.version: $hookVersion");
		
		PickTaskIssueServiceResult reply = (PickTaskIssueServiceResult) result
		PickTaskIssueDTO c = (PickTaskIssueDTO) input
		
		TrackRefNo ObjTrack = new TrackRefNo()
		StockCode stk = new StockCode()
		stk = c.getStockCode();
		StrSTK = stk.getValue()
		StrDstrct = tools.commarea.District;
		log.info("StrSTK : " + StrSTK);
		log.info("StrDstrct : " + StrDstrct);
		CompleteHoldingDetailsDTO[] CompDTO = new CompleteHoldingDetailsDTO();
		CompDTO = c.getCompleteHoldingDetailsDTO()
		log.info("CompDTO.size : " + CompDTO.size());
		Integer i;
		String StrObjTrack = "";
		for (i=0;i<CompDTO.size();i++){
			ObjTrack = CompDTO[i].getSerialNumber()
			log.info("ObjTrack : " + ObjTrack.getValue());
			StrObjTrack = ObjTrack.getValue();
			if (!StrObjTrack.equals(null)){
				try {
					ErrFlag = false;
					String QuerySelect = ("select ASSET_IND from msf100 where trim(STOCK_CODE) = trim('"+StrSTK+"')");
					def QueryRes1 = sql.firstRow(QuerySelect);
					log.info("QueryRes1 : " + QueryRes1);
					if (!QueryRes1.equals(null)){
						if (!QueryRes1.ASSET_IND.equals(null)){
							if (QueryRes1.ASSET_IND.equals("Y")){
								String QuerySelect2 = ("select trim(REF_CODE) REF_CODE from msf071 " +
								"where trim(ENTITY_TYPE) = 'CAT' and trim(ENTITY_VALUE) = '"+StrSTK+"' and " +
								"trim(REF_NO) = '003' and trim(SEQ_NUM) = '001'");
								def QueryRes2 = sql.firstRow(QuerySelect2);
								log.info("QueryRes2 : " + QueryRes2);
								if (!QueryRes2.equals(null)){
									if (!QueryRes2.REF_CODE.equals(null)){
										if (QueryRes2.REF_CODE.equals("")){
											ErrFlag = true;
										}else{
											StrREF = QueryRes2.REF_CODE.trim();
											
											String Queryupdate2 = ("update msf685 " +
												"set ASSET_CLASSIF = '"+StrREF+"' " +
												"where trim(TRACK_NUMBER) = trim('"+StrObjTrack+"') AND (DSTRCT_CODE,ASSET_NO) in ( " +
												"select DSTRCT_CODE,ASSET_NO from msf680 " +
												"where trim(STOCK_CODE) = trim('"+StrSTK+"') and DSTRCT_CODE = '"+tools.commarea.District+"')");
												def QueryRes4 = sql.executeUpdate(Queryupdate2);
											
										}
									}
								}else{
									ErrFlag = true;
								}
							}
						}
					}
				} catch (Exception  e) {
					log.info("Exception : " + e);
					throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999","Exception In Select Query - " + e.message, "", 0, 0))
			
						return input
				}
			}
		}
					
		
		log.info("ErrFlag : " + ErrFlag);
		if (ErrFlag.equals(true)){
			throw new EnterpriseServiceOperationException(
				new ErrorMessageDTO(
				"9999","ASSET CLASS NOT DEFINED IN STOCK CODE", "stockCode", 0, 0))
	
				return input
		}

		integrateActualCost(input, result)
		integrateActualQty(input, result)
		return result
	}
}