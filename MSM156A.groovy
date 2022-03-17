import com.mincom.ellipse.edoi.ejb.msf010.MSF010Key
import com.mincom.ellipse.edoi.ejb.msf010.MSF010Rec
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Key
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Rec
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Key
import com.mincom.ellipse.edoi.ejb.msf232.MSF232Rec
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.edoi.ejb.msf621.MSF621Key
import com.mincom.ellipse.edoi.ejb.msf621.MSF621Rec
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
import javax.sql.DataSource

class MSM156A extends MSOHook{
    String hookVersion = "1.0"

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
    def validateBudget(String districtCode, String projectNo, BigDecimal tranValue, BigDecimal orderValue) {
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

        BigDecimal pembilang = tranValue + commitmentAI + commitmentAO + actualAI + actualAO - orderValue
        BigDecimal penyebut = totalBudget
        BigDecimal totalAAA = pembilang / penyebut * 100

        log.info("Pembilangggg: $pembilang")
        log.info("Penyebut: $penyebut")
        log.info("TotalAAA: $totalAAA")

        BigDecimal calculatedValue = ((tranValue + commitmentAI + commitmentAO + actualAI + actualAO - orderValue) / totalBudget) * 100
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
    def integrateActualCost(GenericMsoRecord screen){
        MsoField errField = new MsoField()
        InetAddress ip
        ip = InetAddress.getLocalHost()
        String hostname = ip.getHostName()

        log.info("Arsiadi integrateActualCost MSM156A Version: $hookVersion")

        def postUrl
        if (hostname.contains("ellprd"))
        {
            postUrl = "http://maximo-production.ptpjb.com:9080/meaweb/es/EXTSYS1/MXE-ACTCOST-XML"
        }
        else if (hostname.contains("elltst"))
        {
            postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-ACTCOST-XML"
        }
        else
        {
            postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-ACTCOST-XML"
        }

        log.info("FKeys: ${screen.getField("FKEYS1I").getValue()}")
        log.info("NextAction: ${screen.nextAction}")

        InitialContext initial = new InitialContext()
        Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
        def sql = new Sql(CAISource)

        if (screen.getField("FKEYS1I").getValue().trim() == "XMIT-Confirm"){
            String requisitionNo
            String poNo = screen.getField("PO_NO1I").getValue().trim()
            String poItemNo = screen.getField("PO_ITEM_NO1I").getValue().trim()
            BigDecimal valueReceived = screen.getField("VALUE_RECVD1I").getValue() as BigDecimal
            String districtCode = tools.commarea.District

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

            log.info("queryMSF221: SELECT distinct substr(preq_stk_code, 1, 6)\n" +
                    "              FROM MSF221 WHERE PO_NO = '$poNo' AND dstrct_code = '$districtCode' and po_item_no = '$poItemNo'")
            sql.eachRow("SELECT distinct substr(preq_stk_code, 1, 6) as REQ_NO\n" +
                    "              FROM MSF221 WHERE PO_NO = '$poNo' AND dstrct_code = '$districtCode' and po_item_no = '$poItemNo'") {row ->

                String reqNo = row.REQ_NO as String

                log.info("districtCode: $districtCode")
                log.info("reqNo: $reqNo")

                def queryWO = sql.firstRow("select distinct work_order as WORK_ORDER from msf232\n" +
                        "where dstrct_code = '$districtCode' \n" +
                        "and substr(requisition_no, 1, 6) like '${reqNo}%'")
                log.info("queryWO: $queryWO")

                if (queryWO){
                    String wo = queryWO.WORK_ORDER as String
                    if (wo.trim() == "")
                        return null
                } else {
                    return null
                }

                sql.eachRow("select distinct work_order from msf232\n" +
                        "where dstrct_code = '$districtCode' \n" +
                        "and substr(requisition_no, 1, 6) like '${reqNo}%'") {row2 ->

                    String workOrder = row2.WORK_ORDER as String
                    log.info("workOrder: $workOrder")

                    def queryIreqItem = sql.firstRow("WITH MAT AS(\n" +
                            "SELECT DSTRCT_CODE\n" +
                            "      ,WORK_ORDER\n" +
                            "      ,SUM(TRAN_AMOUNT) MAT_COST\n" +
                            "FROM MSF900\n" +
                            "WHERE WORK_ORDER <> ' '\n" +
                            "AND (REC900_TYPE = 'S' OR (REC900_TYPE = 'P' AND SERV_ITM_IND = ' '))\n" +
                            "GROUP BY DSTRCT_CODE\n" +
                            "        ,WORK_ORDER),\n" +
                            "SER AS(\n" +
                            "SELECT DSTRCT_CODE, WORK_ORDER, SUM(TRAN_AMOUNT) SERV_COST\n" +
                            "FROM MSF900\n" +
                            "WHERE WORK_ORDER <> ' '\n" +
                            "AND DSTRCT_CODE = 'UJPC'\n" +
                            "AND REC900_TYPE = 'P'\n" +
                            "AND serv_itm_ind = 'S'\n" +
                            "GROUP BY DSTRCT_CODE, WORK_ORDER)\n" +
                            "SELECT A.DSTRCT_CODE\n" +
                            "      ,A.WORK_ORDER\n" +
                            "      ,CASE WHEN B.MAT_COST IS NOT NULL THEN B.MAT_COST ELSE 0 END MAT_COST\n" +
                            "      ,CASE WHEN C.SERV_COST IS NOT NULl THEN C.SERV_COST ELSE 0 END SERV_COST\n" +
                            "      ,(CASE WHEN B.MAT_COST IS NOT NULL THEN B.MAT_COST ELSE 0 END) + (CASE WHEN C.SERV_COST IS NOT NULL THEN C.SERV_COST ELSE 0 END) TOTAL_COST\n" +
                            "FROM MSF620 A\n" +
                            "LEFT OUTER JOIN MAT B ON A.DSTRCT_CODE = B.DSTRCT_CODE AND A.WORK_ORDER = B.WORK_ORDER\n" +
                            "LEFT OUTER JOIN SER C ON A.DSTRCT_CODE = C.DSTRCT_CODE AND A.WORK_ORDER = C.WORK_ORDER\n" +
                            "WHERE A.DSTRCT_CODE = '$districtCode'\n" +
                            "AND A.WORK_ORDER = '$workOrder'")
                    log.info("queryIreqItem: $queryIreqItem")

                    BigDecimal matCost = 0
                    BigDecimal servCost = 0
                    BigDecimal totalCost = 0
                    BigDecimal serviceCost = 0
                    BigDecimal totCost = 0

                    if (queryIreqItem){
                        matCost = queryIreqItem.MAT_COST as BigDecimal
                        servCost = queryIreqItem.SERV_COST as BigDecimal
                        totalCost = queryIreqItem.TOTAL_COST as BigDecimal

                        serviceCost = (servCost ? servCost : 0) + (valueReceived ? valueReceived : 0)
                        totCost = (totalCost ? totalCost : 0) + (valueReceived ? valueReceived : 0)
                    }

                    log.info("matCost: $matCost")
                    log.info("servCost: $servCost")
                    log.info("totalCost: $totalCost")
                    log.info("-------------------------------------------------")
                    log.info("valueReceived: $valueReceived")
                    log.info("totCost: $totCost")
                    log.info("serviceCost: $serviceCost")

                    String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<SyncMXE-ACTCOST-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:37:06+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                            "  <MXE-ACTCOST-XMLSet>\n" +
                            "    <WORKORDER>\n" +
                            "      <WONUM>$workOrder</WONUM>\n" +
                            "      <TOTALCOSTELLIPSE>$totCost</TOTALCOSTELLIPSE>\n" +
                            "      <MATCOSTELLIPSE>$matCost</MATCOSTELLIPSE>\n" +
                            "      <SERVCOSTELLIPSE>$serviceCost</SERVCOSTELLIPSE>\n" +
                            "      <ORGID>UBPL</ORGID>\n" +
                            "      <SITEID>$districtFormatted</SITEID>\n" +
                            "    </WORKORDER>\n" +
                            "  </MXE-ACTCOST-XMLSet>\n" +
                            "</SyncMXE-ACTCOST-XML>"

                    log.info("ARS --- XML: $xmlMessage")

                    def url = new URL(postUrl)
                    HttpURLConnection authConn = url.openConnection()
                    authConn.setRequestMethod("POST")
                    authConn.setDoOutput(true)
                    authConn.setRequestProperty("Content-Type", "application/xml")
                    authConn.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")
                    authConn.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
                    log.info("responsecode: ${authConn.getResponseCode()}")
                    log.info("responseMessage: ${authConn.content.toString()}")

//                    if (authConn.getResponseCode() != 200) {
//                        String responseMessage = authConn.content.toString()
//                        log.info("responseMessage: $responseMessage")
//                        String errorCode = "9999"
//                        screen.setErrorMessage(
//                                new MsoErrorMessage("",
//                                        errorCode,
//                                        responseMessage,
//                                        MsoErrorMessage.ERR_TYPE_ERROR,
//                                        MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
//
//                        errField.setName("VALUE_RECVD1I")
//                        screen.setCurrentCursorField(errField)
//                        return screen
//                    }
                }
            }
        }
        return null
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

    @Override
    GenericMsoRecord onPreSubmit(GenericMsoRecord screen) {
        log.info("[Arsiadi] Hooks onPreSubmit MSM156A logging.version: $hookVersion")

        integrateActualCost(screen)

        String pBCActiveFlag = getModuleSwitch("+PBC", "MSM156A")
        log.info("PBC Active Flag: $pBCActiveFlag")
        if (pBCActiveFlag != "" && pBCActiveFlag != "Y") {
            return null
        }

        int nextAction = screen.nextAction
        log.info("nextAction: $nextAction")

        if (nextAction == 0 || nextAction == 1) {
            String districtCode = tools.commarea.District
            String pONo = screen.getField("PO_NO1I").getValue().trim()
            String pOItemNo = screen.getField("PO_ITEM_NO1I").getValue().trim()
            if (pOItemNo != "") pOItemNo = lastN(pOItemNo, 3)

            Constraint pONumber = MSF221Key.poNo.equalTo(pONo)
            Constraint pOItemNumber = MSF221Key.poItemNo.equalTo(pOItemNo)

            def query = new QueryImpl(MSF221Rec.class).and(pONumber).and(pOItemNumber)

            MSF221Rec msf221Rec = tools.edoi.firstRow(query)
            String printedStatus = msf221Rec.getStatus_221().trim()
            String lastPrintRun = msf221Rec.getLastPrtRunno().trim()

            log.info("printedStatus: $printedStatus")
            log.info("lastPrintRun: $lastPrintRun")
            if (printedStatus != "1" && lastPrintRun == "0000") {
                screen.setErrorMessage(
                        new MsoErrorMessage(
                                "",
                                "9999",
                                "PO belum dicetak sehingga tidak bisa diproses, mohon cetak PO terlebih dahulu",
                                MsoErrorMessage.ERR_TYPE_ERROR,
                                MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED
                        )
                )
                MsoField errField = new MsoField()
                errField.setName("VALUE_RECVD1I")
                screen.setCurrentCursorField(errField)
                return screen
            }

            BigDecimal tranValue = screen.getField("VALUE_RECVD1I").getValue().trim() as BigDecimal
            BigDecimal orderValue = screen.getField("ORDER_VAL1I").getValue().trim() as BigDecimal

            String preqStkCode = getPreqStkCode(districtCode, pONo, pOItemNo)
            String poItemType = getPoItemType(districtCode, pONo, pOItemNo)
            BigDecimal pOGrossPrice = getPoGrossPrice(districtCode,pONo, pOItemNo)

            log.info("District: $districtCode")
            log.info("PO Number: $pONo")
            log.info("PO Item: $pOItemNo")
            log.info("preqStkCode: $preqStkCode")
            log.info("PO Item Type: $poItemType")
            log.info("pOGrossPrice: $pOGrossPrice")
            log.info("Ordered Value: $orderValue")

            String requisitionNo
            if (poItemType != "" && poItemType.trim() != "O") {
                requisitionNo = preqStkCode.substring(0, 6) + "  " + preqStkCode.substring(7, 9)
            } else {
                requisitionNo = preqStkCode.trim()
            }
            String projectNo = getProjectNo(districtCode, requisitionNo)
            log.info("Project No on preExecute: $projectNo")

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
                    errField.setName("VALUE_RECVD1I")
                    screen.setCurrentCursorField(errField)
                    return screen
                }

                Boolean zeroBudget = checkZeroBudget(districtCode, projectNo)
                log.info("zeroBudget Flag: $zeroBudget")
                if (zeroBudget) {
                    screen.setErrorMessage(
                            new MsoErrorMessage(
                                    "",
                                    "9999",
                                    "PRK tidak bisa digunakan, karena tidak memiliki budget!",
                                    MsoErrorMessage.ERR_TYPE_ERROR,
                                    MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED
                            )
                    )
                    MsoField errField = new MsoField()
                    errField.setName("VALUE_RECVD1I")
                    screen.setCurrentCursorField(errField)
                    return screen
                }

                Boolean overBudget = validateBudget(districtCode, projectNo, tranValue, orderValue)
                log.info("Overbudget Flag: $overBudget")
                if (overBudget) {
                    BigDecimal totalCommitment = getCommitmentAI(districtCode, projectNo) +
                            getCommitmentAO(districtCode, projectNo)
                    BigDecimal totalActual = getActualAI(districtCode, projectNo) +
                            getActualAO(districtCode, projectNo)
                    BigDecimal totalBudget = getTotalBudget(districtCode, projectNo)
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
                    errField.setName("VALUE_RECVD1I")
                    screen.setCurrentCursorField(errField)
                    return screen
                }
            }
        }

        return null
    }

    @Override
    GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
        log.info("[Arsiadi] Hooks onPostSubmit MSM156A logging.version: $hookVersion")
//        integrateActualCost(result)
        return result
    }
}
