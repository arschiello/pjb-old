import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.service.m3140.receiptdocument.ReceiptDocumentService
import com.mincom.ellipse.types.m3140.instances.ReceiptDocumentDTO
import com.mincom.ellipse.types.m3140.instances.ReceiptDocumentServiceResult
import com.mincom.ellipse.types.m3140.instances.ReceiptPurchaseOrderItemDTO
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import groovy.sql.Sql
import javax.naming.InitialContext

class ReceiptDocumentService_update extends ServiceHook{
    String hookVersion = "2"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    InetAddress ip = InetAddress.getLocalHost()
    String hostname = ip.getHostName()
    String hostUrl = getHostUrl(hostname)
// mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
    String postUrl

    @Override
    Object onPostExecute(Object request, Object results){
        log.info("Arsiadi Hooks ReceiptDocumentService_update onPostExecute version: $hookVersion")
        BigDecimal receiptVal = 0
        postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-ACTCOST-XML"

        ReceiptDocumentServiceResult res = (ReceiptDocumentServiceResult)results
        ReceiptDocumentDTO inp = (ReceiptDocumentDTO)request

        String districtCode = res.getReceiptDocumentDTO().getDocumentDistrictCode().getValue()
        String districtFormatted
        log.info("districtCode: $districtCode")

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

        log.info("districtFormatted: $districtFormatted")
        String poNo = res.getReceiptDocumentDTO().getDocumentNumber().getValue()
        String receiptRef = res.getReceiptDocumentDTO().getReceiptReference().getValue().trim()
        log.info("poNo: $poNo")
        log.info("receiptRef: $receiptRef")

        ReceiptPurchaseOrderItemDTO[] receiptPurchaseOrderItemDTOs = res.getReceiptDocumentDTO().getReceiptPurchaseOrderItemDTOs()
        ReceiptPurchaseOrderItemDTO[] receiptPurchaseOrderItemDTO2 = inp.getReceiptPurchaseOrderItemDTOs()
        log.info("ARSIADI request receiptPurchaseOrderItemDTO2: $receiptPurchaseOrderItemDTO2")
        log.info("ARSIADI response receiptPurchaseOrderItemDTOs: $receiptPurchaseOrderItemDTOs")

// Periksa apakah ada item non-inventory dari PO yang diterima
        String queryPONonStock = "SELECT * \nFROM MSF221 \nWHERE DSTRCT_CODE = '$districtCode' \nAND PO_NO = '$poNo' \nAND PO_ITEM_TYPE IN ('P', 'S')"
        log.info("--- queryPONonStock: \n$queryPONonStock")

        def resultQueryPONonStock = sql.firstRow(queryPONonStock)
        log.info("---resultQueryPONonStock: $resultQueryPONonStock")

// jika ada, update biaya work order dengan membaca status biaya terakhir
        if (resultQueryPONonStock){
            sql.eachRow(queryPONonStock){Object it ->
                updateActCost(it)
            }
        }

// Periksa apakah ada item inventory dari PO yang diterima
        String queryPOStock = "SELECT * \nFROM MSF221 \nWHERE DSTRCT_CODE = '$districtCode' \nAND PO_NO = '$poNo' \nAND PO_ITEM_TYPE IN ('O', 'F')"
        log.info("--- queryPOStock: \n$queryPOStock")

        def resultQueryPOStock = sql.firstRow(queryPOStock)
        log.info("---resultQueryPOStock: $resultQueryPOStock")
    }

    def updateSOH(Object resultQueryPOStock){
        log.info("---Arsiadi Hooks updateSOH version: $hookVersion")

        String districtCode = resultQueryPOStock.DSTRCT_CODE
        String stockCode = resultQueryPOStock.PREQ_STK_CODE
        log.info("districtCode: $districtCode")
        log.info("Stock Code: $stockCode")

        if (stockCode){
            String qtyReceipt = resultQueryPOStock.QTY_RCV_DIR_I ? resultQueryPOStock.QTY_RCV_DIR_I.trim() : "0"
            String unitOfMeasure = resultQueryPOStock.UNIT_OF_PURCH ? resultQueryPOStock.UNIT_OF_PURCH.trim() : ""
            String inventCategory = resultQueryPOStock.INVENT_CAT ? resultQueryPOStock.INVENT_CAT.trim(): ""
            String warehouseId = resultQueryPOStock.WHOUSE_ID ? resultQueryPOStock.WHOUSE_ID.trim(): ""

            String queryMSF100 = "SELECT ITEM_NAME, STK_DESC FROM MSF100 WHERE STOCK_CODE = '${stockCode.trim()}'"
            def queryMSF100Result = sql.firstRow(queryMSF100)
            String stkDesc = queryMSF100Result ? queryMSF100Result.STK_DESC : ""
            String itemName = queryMSF100Result ? queryMSF100Result.ITEM_NAME : ""
            String stockDesc = "${itemName.trim()} ${stkDesc.trim()}"
            if (stockDesc != ""){
                if (stockDesc.trim().length() > 50){
                    stockDesc = stockDesc.substring(0, 49)
                }
            }

            stockDesc = org.apache.commons.lang.StringEscapeUtils.escapeXml(stockDesc.trim())

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

            postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-ITEM-XML"

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
                    "where a.dstrct_code = '$districtCode'\n" +
                    "and a.stock_code = '$stockCode'"
            log.info("queryCommand: $queryCommand")
            def  queryResult = sql.firstRow(queryCommand)
            log.info("queryResult: $queryResult")

            BigDecimal soh = (queryResult ? queryResult.SOH : "0") as BigDecimal
            BigDecimal duesOut = (queryResult ? queryResult.DUES_OUT : "0") as BigDecimal
            BigDecimal available = soh - duesOut + qtyReceipt

            log.info("soh: $soh")
            log.info("duesOut: $duesOut")
            log.info("qtyReceipt: $qtyReceipt")
            log.info("available: $available")

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
                    "            <DISTRICT>$districtCode</DISTRICT>\n" +
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
                return null
            }
        }
    }

    def updateActCost(Object resultQueryPONonStock){
        log.info("Arsiadi Hooks updateActCost version: $hookVersion")

        String preqStkCode = resultQueryPONonStock.PREQ_STK_CODE as String
        String districtCode = resultQueryPONonStock.DSTRCT_CODE as String

        log.info("preqStkCode: $preqStkCode")
        String preqNo = preqStkCode ? preqStkCode.trim().substring(0, 6) : ""

        String queryMSF232 = "select distinct work_order as WORK_ORDER \nfrom msf232 \n" +
                "where dstrct_code = '$districtCode' \n"+
                "and substr(requisition_no, 1, 6) like '${preqNo}%'"
        log.info("queryMSF232: $queryMSF232")

        def queryWO = sql.firstRow(queryMSF232)
        log.info("queryWO: $queryWO")

        if (queryWO){
            String wo = queryWO.WORK_ORDER as String
            log.info("wo: $wo")

            if (wo.trim() == "") {
                return null
            } else {
                String queryProcessWO = "select distinct work_order as WORK_ORDER from msf232 " +
                        "where dstrct_code = '$districtCode' " +
                        "and substr(requisition_no, 1, 6) like '${preqNo}%'"
                log.info("queryProcessWO: $queryProcessWO")

                sql.eachRow(queryProcessWO) {row2 ->
                    String workOrder = row2.WORK_ORDER as String

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
                            "AND DSTRCT_CODE = '$districtCode'\n" +
                            "AND REC900_TYPE = 'P'\n" +
                            "AND serv_itm_ind = 'S'\n" +
                            "GROUP BY DSTRCT_CODE, WORK_ORDER)\n" +
                            "SELECT A.DSTRCT_CODE\n" +
                            "      ,A.WORK_ORDER\n" +
                            "      ,CASE WHEN B.MAT_COST IS NOT NULL THEN B.MAT_COST ELSE 0 END MAT_COST\n" +
                            "      ,CASE WHEN C.SERV_COST IS NOT NULL THEN C.SERV_COST ELSE 0 END SERV_COST\n" +
                            "      ,(CASE WHEN B.MAT_COST IS NOT NULL THEN B.MAT_COST ELSE 0 END) + (CASE WHEN C.SERV_COST IS NOT NULL THEN C.SERV_COST ELSE 0 END) TOTAL_COST\n" +
                            "FROM MSF620 A\n" +
                            "LEFT OUTER JOIN MAT B ON A.DSTRCT_CODE = B.DSTRCT_CODE AND A.WORK_ORDER = B.WORK_ORDER\n" +
                            "LEFT OUTER JOIN SER C ON A.DSTRCT_CODE = C.DSTRCT_CODE AND A.WORK_ORDER = C.WORK_ORDER\n" +
                            "WHERE A.DSTRCT_CODE = '$districtCode'\n" +
                            "AND A.WORK_ORDER = '$workOrder'")
                    log.info("queryIreqItem: $queryIreqItem")

                    BigDecimal materialCost = 0
                    BigDecimal totCost = 0

                    BigDecimal matCost = 0
                    BigDecimal servCost = 0
                    BigDecimal totalCost = 0

                    if (queryIreqItem){
                        materialCost = queryIreqItem.MAT_COST as BigDecimal
                        totCost = queryIreqItem.TOTAL_COST as BigDecimal

                        matCost = materialCost + receiptVal
                        servCost = queryIreqItem.SERV_COST as BigDecimal
                        totalCost = totCost + receiptVal
                    }
                    log.info("materialCost: $materialCost")
                    log.info("totCost: $totCost")

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
                        return request
                    }
                }
            }
        }
        else {
            return null
        }
    }

    BigDecimal getReceiptValue(ReceiptPurchaseOrderItemDTO[] receiptPurchaseOrderItemDTOs, String districtCode){
        BigDecimal result = 0

        InitialContext initial = new InitialContext()
        Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
        def sql = new Sql(CAISource)

        receiptPurchaseOrderItemDTOs.eachWithIndex{ ReceiptPurchaseOrderItemDTO entry, int i ->
            BigDecimal qtyReceipt = entry.getReceiptQuantity().getValue()
            String poNumber = entry.getDocumentNumber().getValue()
            String poItemNo = entry.getDocumentItem().getValue()

            log.info("districtCode: $districtCode")
            log.info("qtyReceipt: $qtyReceipt")
            log.info("poNumber: $poNumber")
            log.info("poItemNo: $poItemNo")

            String queryPoItem = "SELECT CURR_NET_PR_P as currNetPrP, PO_ITEM_TYPE as poItemType FROM MSF221 WHERE DSTRCT_CODE = '$districtCode' " +
                    "AND PO_NO = '$poNumber' AND PO_ITEM_NO = '$poItemNo'"
            log.info("queryPoItem $i: $queryPoItem")

            def queryPoPrice = sql.firstRow(queryPoItem)

            if (queryPoPrice){
                BigDecimal currNetPrP = queryPoPrice.currNetPrP as BigDecimal
                BigDecimal addVal = currNetPrP * qtyReceipt
                log.info("currNetPrP: $currNetPrP")
                log.info("receiptVal Before: $addVal")
                result += addVal
                log.info("receiptVal after: $addVal")
            }
        }
        return  result
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
}
