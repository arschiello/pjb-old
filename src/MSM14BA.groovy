import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.edoi.ejb.msf141.MSF141Key
import com.mincom.ellipse.edoi.ejb.msf141.MSF141Rec
import com.mincom.ellipse.edoi.ejb.msf231.MSF231Key
import com.mincom.ellipse.edoi.ejb.msf231.MSF231Rec
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Key
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Rec
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.edoi.ejb.msf660.MSF660Key
import com.mincom.ellipse.edoi.ejb.msf660.MSF660Rec
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.eql.Constraint
import com.mincom.eql.impl.QueryImpl
import groovy.sql.Sql
import javax.naming.InitialContext

class MSM14BA extends MSOHook {
//region Global Variable -- please maintain it as simple as can be
    String hookVersion = "2"
    BigDecimal totalBudget
    BigDecimal commitmentAI
    BigDecimal commitmentAO
    BigDecimal actualAI
    BigDecimal actualAO
//endregion

//region Generic function
    def lastN(String input, int n) {
        return n > input?.size() ? null : n ? input[-n..-1] : ''
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
    def getProjectCosting(String districtCode, String reqNo, String reqItem, String itemType){
        String result
        String projectNbr
        String workOrder
        try{
            log.info("Getting Project Cost Information...")
            String requisitionNo = reqNo.trim() + "  " + reqItem.trim()

            String requisitionNo2
            String req232Type
            if (itemType != "SC") {
                requisitionNo2 = reqNo.trim() + "  000"
                req232Type = "P"
            } else {
                requisitionNo2 = reqNo.trim() + "  0000"
                req232Type = "I"
            }

            log.info("requisitionNo: $requisitionNo")
            log.info("requisitionNo2: $requisitionNo2")

            Constraint district = MSF232Key.dstrctCode.equalTo(districtCode)
            Constraint reqNbr = MSF232Key.requisitionNo.equalTo(requisitionNo)
            Constraint reqNbr2 = MSF232Key.requisitionNo.equalTo(requisitionNo2)
            Constraint rec232Type = MSF232Key.req_232Type.equalTo(req232Type)

            def qry = new QueryImpl(MSF232Rec.class).and(district).and(reqNbr).and(rec232Type)

            MSF232Rec mSF232rec = tools.edoi.firstRow(qry)
            if(mSF232rec){
                log.info("Data Found on Item...")
                projectNbr = mSF232rec.getProjectNo().trim()
                workOrder = mSF232rec.getWorkOrder().trim()
            }
            else{
                def qry2 = new QueryImpl(MSF232Rec.class).and(district).and(reqNbr2).and(rec232Type)
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

            if(workOrder
                    && workOrder.trim() != ""
                    && (!projectNbr || projectNbr.trim() == "")){
                projectNbr = getProjectWo(districtCode, workOrder)
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
        result = projectNbr
        log.info("result: $result")
        return result
    }
    def getProjectWo(String district, String wo) {
        String result
        try {
            Constraint c1 = MSF620Key.dstrctCode.equalTo(district)
            Constraint c2 = MSF620Key.workOrder.equalTo(wo)

            def query = new QueryImpl(MSF620Rec.class).and(c1).and(c2);

            MSF620Rec msf620Rec = tools.edoi.firstRow(query);

            if (msf620Rec != null){
                log.info("Record was found")
                result = msf620Rec.getProjectNo()
            }else{
                log.info("No record was found")
            }
        }catch (EnterpriseServiceOperationException e){
            List <ErrorMessageDTO> listError = e.getErrorMessages()
            listError.each{ErrorMessageDTO errorDTO ->
                log.info ("Error Code:" + errorDTO.getCode())
                log.info ("Error Message:" + errorDTO.getMessage())
                log.info ("Error Fields: " + errorDTO.getFieldName())
            }
        }
        log.info("result: $result")
        return result
    }
    def validateBudget(String districtCode, String projectNo, BigDecimal tranValue, BigDecimal committedValue) {
        log.info("Calculating Budget ...")

        Boolean result
        totalBudget = getTotalBudget(districtCode, projectNo)
        commitmentAI = getCommitmentAI(districtCode, projectNo)
        commitmentAO = getCommitmentAO(districtCode, projectNo)
        actualAI = getActualAI(districtCode, projectNo)
        actualAO = getActualAO(districtCode, projectNo)

        log.info("totalBudget: $totalBudget")
        log.info("commitmentAI: $commitmentAI")
        log.info("commitmentAO: $commitmentAO")
        log.info("actualAI: $actualAI")
        log.info("actualAO: $actualAO")
        log.info("committedValue: $committedValue")
        log.info("tranValue: $tranValue")

        if (!totalBudget || totalBudget == 0) {
            result = false
        } else{
            BigDecimal checkValue = (((tranValue - committedValue + commitmentAO + commitmentAI + actualAO + actualAI) / totalBudget) * 100).setScale(2, BigDecimal.ROUND_HALF_UP)
            BigDecimal pembilang = tranValue - committedValue + commitmentAO + commitmentAI + actualAO + actualAI
            BigDecimal penyebut = totalBudget
            BigDecimal hasil = pembilang / penyebut
            BigDecimal hasil2 = hasil * 100

            log.info("Pembilang: $pembilang")
            log.info("Penyebut: $penyebut")
            log.info("hasil: $hasil")
            log.info("hasil2: $hasil2")
            log.info("checkValue: $checkValue")

            if (checkValue > 100) {
                result = true
            } else {
                result = false
            }
        }
        return result
    }
    def getTotalBudget(String districtCode, String projectNbr) {
        log.info("districtCode: $districtCode")
        log.info("Get total budget of Project Nbr: " + projectNbr)

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
                            "AND REVSD_PERIOD = '000000'" +
                            "AND PROJECT_NO = '$projectNbr' " +
                            "AND DSTRCT_CODE = '$districtCode'")

                    budgetInTotal = sql.firstRow("SELECT TOT_EST_COST totEstCost " +
                            "FROM MSF667 " +
                            "WHERE CATEGORY_CODE = ' ' " +
                            "AND EXP_REV_IND = 'E' " +
                            "AND REVSD_PERIOD = '000000'" +
                            "AND PROJECT_NO = '$projectNbr' " +
                            "AND DSTRCT_CODE = '$districtCode'")
                }
                else if (projectType == "ADM") {
                    String projectNo = projectNbr.substring(0, 4)
                    log.info("Query Command: " +
                            "SELECT TOT_EST_COST totEstCost " +
                            "FROM MSF667 " +
                            "WHERE CATEGORY_CODE = ' ' " +
                            "AND EXP_REV_IND = 'E' " +
                            "AND REVSD_PERIOD = '000000'" +
                            "AND PROJECT_NO = '$projectNo' " +
                            "AND DSTRCT_CODE = '$districtCode'")

                    budgetInTotal = sql.firstRow("SELECT TOT_EST_COST totEstCost " +
                            "FROM MSF667 " +
                            "WHERE CATEGORY_CODE = ' ' " +
                            "AND EXP_REV_IND = 'E' " +
                            "AND REVSD_PERIOD = '000000'" +
                            "AND PROJECT_NO = '$projectNo' " +
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
        log.info("result Total Budget: $result")
        return result
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
    def getCommittedValue(String districtCode, String reqNo, String reqItemNo, String itemType) {
        log.info("getCommittedValue ...")
        BigDecimal result
        log.info("itemType: $itemType")
        log.info("districtCode: $districtCode")
        log.info("reqNo: $reqNo")
        log.info("reqItemNo: $reqItemNo")

        if (itemType == "S") {
            Constraint district = MSF141Key.dstrctCode.equalTo(districtCode)
            Constraint iRNo = MSF141Key.ireqNo.equalTo(reqNo)
            Constraint iRItemNo = MSF141Key.ireqItem.equalTo(reqItemNo)
            def query = new QueryImpl(MSF141Rec.class).and(district).and(iRNo).and(iRItemNo)
            MSF141Rec msf141Rec = tools.edoi.firstRow(query)
            if (msf141Rec) {
                BigDecimal qtyReqd = msf141Rec.getQtyReq() as BigDecimal
                BigDecimal itemPrice
                if (msf141Rec.getItemPrice()) {
                    itemPrice = msf141Rec.getItemPrice() as BigDecimal
                }
                else {
                    itemPrice = 0
                }
                log.info("qtyReqd: $qtyReqd")
                log.info("itemPrice: $itemPrice")
                result = qtyReqd * itemPrice
                log.info("resultS1-->: $result")
            } else {
                result = 0
                log.info("resultS2-->: $result")
            }
        }

        if (itemType == "D" || itemType == "G") {
            Constraint district = MSF231Key.dstrctCode.equalTo(districtCode)
            Constraint pRNo = MSF231Key.preqNo.equalTo(reqNo)
            Constraint pRItemNo = MSF231Key.preqItemNo.equalTo(reqItemNo)
            def query = new QueryImpl(MSF231Rec.class).and(district).and(pRNo).and(pRItemNo)
            MSF231Rec msf231Rec = tools.edoi.firstRow(query)
            if (msf231Rec) {
                BigDecimal qtyReqd = msf231Rec.getPrQtyReqd() as BigDecimal
                BigDecimal itemPrice
                if (msf231Rec.getActGrossPr()) {
                    itemPrice = msf231Rec.getActGrossPr() as BigDecimal
                }
                else {
                    itemPrice = 0
                }
                log.info("qtyReqd: $qtyReqd")
                log.info("itemPrice: $itemPrice")
                result = qtyReqd * itemPrice
                log.info("resultD1-->: $result")
            } else {
                result = 0
                log.info("resultD2-->: $result")
            }
        }

        if (itemType == "V") {
            Constraint district = MSF231Key.dstrctCode.equalTo(districtCode)
            Constraint pRNo = MSF231Key.preqNo.equalTo(reqNo)
            Constraint pRItemNo = MSF231Key.preqItemNo.equalTo(reqItemNo)
            def query = new QueryImpl(MSF231Rec.class).and(district).and(pRNo).and(pRItemNo)
            MSF231Rec msf231Rec = tools.edoi.firstRow(query)
            if (msf231Rec) {
                BigDecimal qtyReqd = msf231Rec.getPrQtyReqd() as BigDecimal
                BigDecimal itemPrice
                if (msf231Rec.getActGrossPr()) {
                    itemPrice = msf231Rec.getActGrossPr() as BigDecimal
                }
                else {
                    itemPrice = 0
                }
                log.info("qtyReqd: $qtyReqd")
                log.info("itemPrice: $itemPrice")
                result = qtyReqd * itemPrice
                log.info("resultV1-->: $result")
            } else {
                result = 0
                log.info("resultV2-->: $result")
            }
        }

        log.info("result: $result")
        if (result == null) result = 0
        return result
    }
    def getPRItemType(String districtCode, String reqNo, String reqItemNo) {
        String result

        Constraint district = MSF231Key.dstrctCode.equalTo(districtCode)
        Constraint pRNo = MSF231Key.preqNo.equalTo(reqNo)
        Constraint pRItemNo = MSF231Key.preqItemNo.equalTo(reqItemNo)

        def query = new QueryImpl(MSF231Rec.class).and(district).and(pRNo).and(pRItemNo)
        MSF231Rec msf231Rec = tools.edoi.firstRow(query)

        if (msf231Rec) {
            result = msf231Rec.getReqType()
        }
        else {
            result = "NOT FOUND"
        }
        log.info("PRItemType: $result")
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
    def getProjectType(String districtCode, String projectNo) {
        log.info("Get Project Type Version $hookVersion ...............")
        String result
        String tableTypeAI
        String tableTypeAO

        InitialContext initialContext = new InitialContext()
        Object dataSource = initialContext.lookup("java:jboss/datasources/ReadOnlyDatasource")
        def sql = new Sql(dataSource)

        String checkValue = projectNo.substring(2, 4)
        String queryAI = "SELECT TABLE_TYPE as tableType FROM MSF010 WHERE TABLE_TYPE = '+AI' AND TABLE_CODE = '$checkValue'"
        String queryAO = "SELECT TABLE_TYPE as tableType FROM MSF010 WHERE TABLE_TYPE = '+AO' AND TABLE_CODE = '$checkValue'"

        log.info("queryAI: $queryAI")
        log.info("queryAO: $queryAO")

        try {
            def queryAIResult = sql.firstRow(queryAI)
            log.info("queryAIResult: $queryAIResult")

            if (queryAIResult) {
                tableTypeAI = queryAIResult.tableType
                log.info("tableTypeAI: $tableTypeAI")

                if (tableTypeAI == "+AI") {
                    result = "AI"
                } else {
                    def queryAOResult = sql.firstRow(queryAO)
                    if (queryAOResult) {
                        tableTypeAO = queryAOResult.tableType
                        log.info("tableTypeAO: $tableTypeAO")
                        if (tableTypeAO == "+AO") {
                            result = "AO"
                        } else {
                            result = "N/A"
                        }
                    }
                }
            } else {
                def queryAOResult = sql.firstRow(queryAO)
                log.info("queryAOResult: $queryAOResult")

                if (queryAOResult) {
                    tableTypeAO = queryAOResult.tableType
                    log.info("tableTypeAO: $tableTypeAO")
                    if (tableTypeAO == "+AO") {
                        result = "AO"
                    } else {
                        result = "N/A"
                    }
                }
            }
        }  catch(EnterpriseServiceOperationException e) {
            List <ErrorMessageDTO> listError = e.getErrorMessages()
            listError.each{ErrorMessageDTO errorDTO ->
                log.info ("Error Code: " + errorDTO.getCode())
                log.info ("Error Message: " + errorDTO.getMessage())
                log.info ("Error Fields: " + errorDTO.getFieldName())
            }
            result = "ERROR"
        }

        log.info("result Project Status: $result")
        return result
    }
//endregion

//region Main Program
    @Override
    GenericMsoRecord onDisplay(GenericMsoRecord screen) {
        log.info("[Arsiadi] Hooks onDisplay MSM14BA logging.version: $hookVersion")
        log.info("ireqNo: " + screen.getField("IREQ_NO1I").getValue().trim())
        log.info("itemNo1: " + screen.getField("ITEM_NO1I1").getValue().trim())
        log.info("itemNo2: " + screen.getField("ITEM_NO1I2").getValue().trim())
        return null
    }

    @Override
    GenericMsoRecord onPreSubmit(GenericMsoRecord screen) {
        log.info("[Arsiadi] Hooks onPreSubmit MSM14BA logging.version: $hookVersion")
        String pBCActiveFlag = getModuleSwitch("+PBC", "MSM14BA")
        log.info("PBC Active Flag: $pBCActiveFlag")
        if (pBCActiveFlag != "" && pBCActiveFlag != "Y") {
            return null
        }

        int nextAction = screen.nextAction
        log.info("nextAction: $nextAction")

        if (nextAction == 0 || nextAction == 1) {
            String districtCode = tools.commarea.District
            String ireqNo = screen.getField("IREQ_NO1I").getValue().trim()
            log.info("ireqNo: $ireqNo")

            for (int i = 1; i <= 2; i++) {
                log.info("i: $i")

                String checkItemNo = screen.getField("ITEM_NO1I" + i.toString()).getValue()
                log.info("item no: $checkItemNo")

                if (!checkItemNo) {
                    log.info("item is null")
                    break
                }

                String itemType
                String ireqItemNo
                BigDecimal tranValue
                BigDecimal qtyReqd
                BigDecimal estPrice

                String screenItemType = screen.getField("TYPE1I" + i.toString()).getValue().trim()

                if (screen.getField("QTY_REQD1I"+ i.toString()).getValue()) {
                    qtyReqd = screen.getField("QTY_REQD1I"+ i.toString()).getValue() as BigDecimal
                }
                else {
                    qtyReqd = 0
                }

                if (screen.getField("EST_PRICE1I"+ i.toString()).getValue()) {
                    estPrice = screen.getField("EST_PRICE1I"+ i.toString()).getValue() as BigDecimal
                }
                else {
                    estPrice = 0
                }

                log.info("screenItemType: $screenItemType")
                log.info("qtyReqd: $qtyReqd")
                log.info("estPrice: $estPrice")

                if (screenItemType == "S") {
                    if (screen.getField("ITEM_NO1I" + i.toString()).getValue().trim() != "") {
                        ireqItemNo = lastN("0000" + screen.getField("ITEM_NO1I" + i.toString()).getValue().trim(), 4)
                    }
                    tranValue = qtyReqd * estPrice
                    itemType = "SC"
                }
                else {
                    if (screen.getField("ITEM_NO1I" + i.toString()).getValue().trim() != "") {
                        ireqItemNo = lastN("000" + screen.getField("ITEM_NO1I" + i.toString()).getValue().trim(), 3)
                    }
                    itemType = getPRItemType(districtCode, ireqNo, ireqItemNo)
                    log.info("itemType: $itemType")
                    if (itemType == "G" || itemType == "X" || itemType == "Q") {
                        tranValue = qtyReqd * estPrice
                    }
                    else if (itemType == "S" || itemType == "F") {
                        tranValue = estPrice
                    }
                    else {
                        tranValue = 0
                    }
                }
                log.info("tranValue: $tranValue")
                log.info("districtCode: $districtCode, ireqNo: $ireqNo, ireqItemNo: $ireqItemNo, itemType: $itemType")

                BigDecimal committedValue = getCommittedValue(districtCode, ireqNo, ireqItemNo, itemType)
                String projectNo = getProjectCosting(districtCode, ireqNo, ireqItemNo, itemType)

                log.info("CommittedValue: $committedValue, projectNo: $projectNo")

                if (projectNo.trim() == "") {
                    String errorMessage = "Nomor PRK belum dimasukkan"
                    String errorCode = "9999"

                    screen.setErrorMessage(
                            new MsoErrorMessage("",
                                    errorCode,
                                    errorMessage,
                                    MsoErrorMessage.ERR_TYPE_ERROR,
                                    MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

                    MsoField errField = new MsoField()
                    errField.setName("EST_PRICE1I"+ i.toString())
                    screen.setCurrentCursorField(errField)
                    return screen
                    break
                }

                Boolean overBudget
                if (projectNo && projectNo != "") {
                    String finalisedDate = getFinalisedDate(districtCode, projectNo)
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
                        errField.setName("EST_PRICE1I"+ i.toString())
                        screen.setCurrentCursorField(errField)
                        return screen
                        break
                    }

                    String projectType = getProjectType(districtCode, projectNo)
                    log.info("projectType: $projectType")

                    totalBudget = getTotalBudget(districtCode, projectNo)
                    log.info("totalBudget: $totalBudget")

                    if (projectType == "AI") {
                        commitmentAI = getCommitmentAI(districtCode, projectNo)
                        actualAI = getActualAI(districtCode, projectNo)

                        log.info("totalBudget: $totalBudget")
                        log.info("commitmentAI: $commitmentAI")
                        log.info("actualAI: $actualAI")
                        log.info("committedValue: $committedValue")
                        log.info("tranValue: $tranValue")

                        if (!totalBudget || totalBudget == 0) {
                            overBudget = false
                        } else{
                            BigDecimal checkValue = (((tranValue - committedValue + commitmentAI + actualAI) / totalBudget) * 100).setScale(2, BigDecimal.ROUND_HALF_UP)
                            BigDecimal counter = tranValue - committedValue + commitmentAI + actualAI
                            BigDecimal denominator = totalBudget
                            BigDecimal divResult = counter / denominator
                            BigDecimal divResultPercent = divResult * 100

                            log.info("Pembilang: $counter")
                            log.info("Penyebut: $denominator")
                            log.info("divResult: $divResult")
                            log.info("divResultPercent: $divResultPercent")
                            log.info("checkValue: $checkValue")

                            if (checkValue > 100) {
                                overBudget = true
                            } else {
                                overBudget = false
                            }
                        }
                        log.info("overBudget flag: $overBudget")
                    } else {
                        if (projectType == "AO") {
                            commitmentAO = getCommitmentAO(districtCode, projectNo)
                            actualAO = getActualAO(districtCode, projectNo)

                            log.info("totalBudget: $totalBudget")
                            log.info("commitmentAI: $commitmentAO")
                            log.info("actualAI: $actualAO")
                            log.info("committedValue: $committedValue")
                            log.info("tranValue: $tranValue")

                            if (!totalBudget || totalBudget == 0) {
                                overBudget = false
                            } else{
                                BigDecimal checkValue = (((tranValue - committedValue + commitmentAO + actualAO) / totalBudget) * 100).setScale(2, BigDecimal.ROUND_HALF_UP)
                                BigDecimal counter = tranValue - committedValue + commitmentAO + actualAO
                                BigDecimal denominator = totalBudget
                                BigDecimal divResult = counter / denominator
                                BigDecimal divResultPercent = divResult * 100

                                log.info("Pembilang: $counter")
                                log.info("Penyebut: $denominator")
                                log.info("divResult: $divResult")
                                log.info("divResultPercent: $divResultPercent")
                                log.info("checkValue: $checkValue")

                                if (checkValue > 100) {
                                    overBudget = true
                                } else {
                                    overBudget = false
                                }
                            }
                            log.info("overBudget flag: $overBudget")
                        } else {
                            overBudget = false
                        }
                    }
                }

                log.info("Overbudget Flag: $overBudget")

                if (overBudget) {
                    BigDecimal totalCommitment = commitmentAI + commitmentAO
                    BigDecimal totalActual = actualAI + actualAO
                    BigDecimal remainingBudget = totalBudget - totalActual - totalCommitment

                    log.info("Error Message Checkpoint...")
                    log.info("totalBudget: $totalBudget")
                    log.info("totalCommitment: $totalCommitment")
                    log.info("totalActual: $totalActual")
                    log.info("sisaAnggaran: $remainingBudget")
                    log.info("ProjNo: ---$projectNo---")
                    log.info("ProjNoLength: " + projectNo.length())
                    log.info("proj34: " + projectNo.substring(2, 4))
                    log.info("proj4: " + projectNo.substring(3, 4).trim())

                    String errorMessage = "Nilai transaksi overbudget: \n" +
                            "Alokasi PRK: $totalBudget\n " +
                            "Realisasi PRK: $totalActual\n" +
                            "Commitment PRK: $totalCommitment\n" +
                            "Sisa Anggaran PRK: $remainingBudget\n" +
                            "Nilai transaksi saat ini sebesar $tranValue lebih besar dari nilai anggaran\n" +
                            "Segera ajukan revisi anggaran kepada divisi terkait"
                    String errorCode = "9999"

                    screen.setErrorMessage(
                            new MsoErrorMessage("",
                                    errorCode,
                                    errorMessage,
                                    MsoErrorMessage.ERR_TYPE_ERROR,
                                    MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

                    MsoField errField = new MsoField()
                    errField.setName("EST_PRICE1I"+ i.toString())
                    screen.setCurrentCursorField(errField)
                    return screen
                    break
                }
            }
        }
        return null
    }

    @Override
    GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
        return result
    }
//    endregion
}
