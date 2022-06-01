import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceModifyHeaderReplyDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceModifyHeaderRequestDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceReadReplyDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.eql.Constraint
import com.mincom.eql.impl.QueryImpl
import groovy.sql.Sql

import javax.naming.InitialContext

class RequisitionService_modifyHeader extends ServiceHook{
    String hookVersion = "1"

    def changeLineText(String NewLine, int row, String entityKey) {
        try {
            log.info ("Change Line Text...")

            String ChangeLine
            File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entityKey)

            ChangeLine = checkFile1.readLines().get(row - 1).substring(0)
            BufferedReader file = new BufferedReader(new FileReader("/var/opt/appliance/efs/work/PBC." + entityKey))

            String line
            String tmp
            while ((line = file.readLine()) != null) tmp += line + "\n"
            file.close()

            tmp = tmp.replace(ChangeLine, NewLine)

            FileOutputStream fileOut = new FileOutputStream("/var/opt/appliance/efs/work/PBC." + entityKey)
            fileOut.write(tmp.getBytes())
            fileOut.close()
        } catch (EnterpriseServiceOperationException e){
            List <ErrorMessageDTO> listError = e.getErrorMessages()
            listError.each{ErrorMessageDTO errorDTO ->
                log.info ("Error Code:" + errorDTO.getCode())
                log.info ("Error Message:" + errorDTO.getMessage())
                log.info ("Error Fields: " + errorDTO.getFieldName())
            }
        }
        return
    }
    def insertLineFile(String StrLine, String entityKey) {
        try {
            File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entityKey)
            checkFile1 << StrLine
        } catch (EnterpriseServiceOperationException e){
            List <ErrorMessageDTO> listError = e.getErrorMessages()
            listError.each{ErrorMessageDTO errorDTO ->
                log.info ("Erorr Code:" + errorDTO.getCode())
                log.info ("Error Message:" + errorDTO.getMessage())
                log.info ("Error Fields: " + errorDTO.getFieldName())
            }
        }
        return ""
    }
    def getProjectNo(Object input){
        String result
        RequisitionServiceModifyHeaderRequestDTO inputDTO = (RequisitionServiceModifyHeaderRequestDTO) input
        if (inputDTO.getWorkOrderA() != null && inputDTO.getCostCentreA() == null){
            String woNum = inputDTO.getWorkOrderA().getPrefix() + inputDTO.getWorkOrderA().getNo()
            String reqType = inputDTO.getIreqType()
            String districtCode
            if(reqType == "PR")
            {
                districtCode = tools.commarea.District;
            } else{
                districtCode = inputDTO.getCostDistrictA()
            }

            InitialContext initial = new InitialContext()
            Object dataSource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
            def sql = new Sql(dataSource)
            String q = "select trim(substr(x.dstrct_acct_code, 5, 24)) as acc_code, project_no " +
                    "from msf620 x " +
                    "where x.dstrct_code='$districtCode' " +
                    "and x.work_order = '$woNum'"
            log.info("query: $q")
            def queryResult = sql.firstRow(q)
            log.info ("queryResult: $queryResult")

            if (queryResult){
                String accCode = queryResult.ACC_CODE
                String projectNo = queryResult.PROJECT_NO
                log.info ("Account Code :" + accCode)
                log.info ("Project No :" + projectNo)
                inputDTO.setCostCentreA(accCode)
            }
        } else{
            log.info("Tidak Masuk...")
        }

        if (inputDTO.getWorkOrderA().toString().trim() != "" || inputDTO.getProjectA().toString().trim() != ""){
            if (inputDTO.getWorkOrderA() != null && inputDTO.getWorkOrderA().toString().trim() != ""){
                try{
                    WorkOrderServiceReadReplyDTO WKReadReply = tools.service.get('WorkOrder').read({
                        it.districtCode = inputDTO.getDistrictCode()
                        it.workOrder = inputDTO.getWorkOrderA()
                    })
                    result = WKReadReply.getProjectNo()
                } catch (EnterpriseServiceOperationException e){
                    List <ErrorMessageDTO> listError = e.getErrorMessages()
                    listError.each{ErrorMessageDTO errorDTO ->
                        log.info ("Error Code:" + errorDTO.getCode())
                        log.info ("Error Message:" + errorDTO.getMessage())
                        log.info ("Error Fields: " + errorDTO.getFieldName())
                    }
                }
            }else{
                result = inputDTO.getProjectA()
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

    @Override
    Object onPreExecute(Object input){
        log.info("Hooks onPreExecute RequisitionService_modifyHeader logging.version: $hookVersion")

        String pBCActiveFlag = getModuleSwitch("+PBC", "MSE140")
        log.info("PBC Active Flag: $pBCActiveFlag")
        if (pBCActiveFlag != "" && pBCActiveFlag != "Y") {
            return null
        }

        RequisitionServiceModifyHeaderRequestDTO inputDTO = (RequisitionServiceModifyHeaderRequestDTO) input

        log.info ("inputDTO.getWorkOrderA():" + inputDTO.getWorkOrderA())
        log.info ("inputDTO.getCostCentreA():" + inputDTO.getCostCentreA())
        log.info ("inputDTO.getProjectNoA():" + inputDTO.getProjectA())

        if (inputDTO.getWorkOrderA() != null && inputDTO.getCostCentreA() == null){
            String woNum = inputDTO.getWorkOrderA().getPrefix() + inputDTO.getWorkOrderA().getNo()
            String reqType = inputDTO.getIreqType()
            String districtCode
            if(reqType == "PR")
            {
                districtCode = tools.commarea.District;
            } else{
                districtCode = inputDTO.getCostDistrictA()
            }

            log.info("woNum.length():" + woNum.length())
            log.info("woNum: $woNum")
            log.info("reqType: $reqType")
            log.info("districtCode: $districtCode")

            InitialContext initial = new InitialContext()
            Object dataSource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
            def sql = new Sql(dataSource)
            String q = "select trim(substr(x.dstrct_acct_code, 5, 24)) as acc_code, project_no " +
                    "from msf620 x " +
                    "where x.dstrct_code='$districtCode' " +
                    "and x.work_order = '$woNum'"
            log.info("query: $q")
            def queryResult = sql.firstRow(q)
            log.info ("queryResult: $queryResult")

            if (queryResult){
                String accCode = queryResult.ACC_CODE
                String projectNo = queryResult.PROJECT_NO
                log.info ("Account Code :" + accCode)
                log.info ("Project No :" + projectNo)
                inputDTO.setCostCentreA(accCode)
            }
        } else{
            log.info("Tidak Masuk...")
        }

        String entityKey
        if (inputDTO.getPreqNo() == null || inputDTO.getPreqNo().trim() == ""){
            entityKey = inputDTO.getDistrictCode() + inputDTO.getIreqType() + inputDTO.getIreqNo()
        }else{
            entityKey = inputDTO.getDistrictCode() + inputDTO.getIreqType() + inputDTO.getPreqNo()
        }
        log.info("entityKey : ${entityKey}")

        String projectNo
        if (inputDTO.getWorkOrderA().toString().trim() != "" || inputDTO.getProjectA().toString().trim() != ""){
            if (inputDTO.getWorkOrderA() != null && inputDTO.getWorkOrderA().toString().trim() != ""){
                try{
                    WorkOrderServiceReadReplyDTO WKReadReply = tools.service.get('WorkOrder').read({
                        it.districtCode = inputDTO.getDistrictCode()
                        it.workOrder = inputDTO.getWorkOrderA()
                    })
                    projectNo = WKReadReply.getProjectNo()
                } catch (EnterpriseServiceOperationException e){
                    List <ErrorMessageDTO> listError = e.getErrorMessages()
                    listError.each{ErrorMessageDTO errorDTO ->
                        log.info ("Error Code:" + errorDTO.getCode())
                        log.info ("Error Message:" + errorDTO.getMessage())
                        log.info ("Error Fields: " + errorDTO.getFieldName())
                    }
                }

                if (projectNo != null){
                    if (projectNo.trim() == ""){
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
                projectNo = inputDTO.getProjectA()
            }
            log.info ("projectNo : " + projectNo)
        }
        return null
    }

    @Override
    Object onPostExecute(Object input, Object result) {
        log.info("Hooks onPostExecute RequisitionService_modifyHeader logging.version: $hookVersion")

        String pBCActiveFlag = getModuleSwitch("+PBC", "MSE140")
        log.info("PBC Active Flag: $pBCActiveFlag")
        if (pBCActiveFlag != "" && pBCActiveFlag != "Y") {
            return null
        }

        String entityKey
        String projectNo = getProjectNo(input)

        RequisitionServiceModifyHeaderRequestDTO inputDTO = (RequisitionServiceModifyHeaderRequestDTO) input
        RequisitionServiceModifyHeaderReplyDTO reply = (RequisitionServiceModifyHeaderReplyDTO) result

        if (reply.getPreqNo() == null || reply.getPreqNo().trim() == ''){
            entityKey = reply.getDistrictCode() + reply.getIreqType() + reply.getIreqNo()
        }else{
            entityKey = reply.getDistrictCode() + reply.getIreqType() + reply.getPreqNo()
        }
        log.info("entityKey : $entityKey")

        try{
            File checkFile1 = new File("/var/opt/appliance/efs/work/PBC." + entityKey)
            def lines = checkFile1.readLines()

            log.info("Line Size : " + lines.size())

            if (lines.size() > 2){
                changeLineText("3.PROJ : $projectNo", 3, entityKey)
            } else{
                insertLineFile("3.PROJ : $projectNo", entityKey)
            }
        } catch (EnterpriseServiceOperationException e){
            List <ErrorMessageDTO> listError = e.getErrorMessages()
            listError.each{ErrorMessageDTO errorDTO ->
                log.info ("Error Code:" + errorDTO.getCode())
                log.info ("Error Message:" + errorDTO.getMessage())
                log.info ("Error Fields: " + errorDTO.getFieldName())
            }
        }
        return result
    }
}
