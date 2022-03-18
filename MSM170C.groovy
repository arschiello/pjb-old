import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.ellipse.hook.hooks.MSOHook
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils

import javax.naming.InitialContext

class MSM170C extends MSOHook{
    String hookVersion = "1"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

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
    GenericMsoRecord onPostSubmit(GenericMsoRecord request, GenericMsoRecord response){
        log.info("Arsiadi Hooks MSM170C onPostSubmit version: $hookVersion")
        log.info("NextAction: ${request.nextAction}")
        log.info("StockCode: ${request.getField("STOCK_CODE3I").getValue()}")
        log.info("stockDesc: ${request.getField("ITEM_NAMEA3I").getValue().trim()}")
        log.info("uoi: ${request.getField("UNIT_OF_ISSUE3I").getValue().trim()}")
        log.info("warehouseId: ${request.getField("HOME_WHOUSE3I").getValue()}")
        log.info("primaryCategory: ${request.getField("PRIMARY_CATEG3I").getValue()}")

        Integer nextAction = request.nextAction
        String districtCode = tools.commarea.District
        String stockCode = request.getField("STOCK_CODE3I").getValue()

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//        Query to get soh -- start
// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

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
        BigDecimal available = soh - duesOut

        log.info("---soh: $soh")
        log.info("---duesOut: $duesOut")
        log.info("---available: $available")

// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//        Query to get soh -- end
// --------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        String queryMSF100 = "SELECT STK_DESC FROM MSF100 WHERE STOCK_CODE = '${stockCode.trim()}'"
        def queryMSF100Result = sql.firstRow(queryMSF100)
        String stockDesc = queryMSF100Result ? queryMSF100Result.STK_DESC.trim() : ""
        stockDesc = stockDesc.length() > 50 ? stockDesc.substring(0, 49) : stockDesc
//        String stockDesc = StringEscapeUtils.escapeXml(request.getField("ITEM_NAMEA3I").getValue().trim())

        String unitOfIssue = StringEscapeUtils.escapeXml(request.getField("UNIT_OF_ISSUE3I").getValue().trim())
        String warehouseId = request.getField("HOME_WHOUSE3I").getValue()
        String primaryCateg = request.getField("PRIMARY_CATEG3I").getValue() != null && request.getField("PRIMARY_CATEG3I").getValue() != "" ? request.getField("PRIMARY_CATEG3I").getValue().trim() : "K1"

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

        String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SyncMXE-ITEM-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:45:48+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                "    <MXE-ITEM-XMLSet>\n" +
                "        <ELLIPSEITEM>\n" +
                "            <ITEMNUM>$stockCode</ITEMNUM>\n" +
                "            <DESCRIPTION>${StringEscapeUtils.escapeXml(stockDesc)}</DESCRIPTION>\n" +
                "            <AVAILABLE>$available</AVAILABLE>\n" +
                "            <SOH>$soh</SOH>\n" +
                "            <UOM>$unitOfIssue</UOM>\n" +
                "            <STOREROOM>$warehouseId</STOREROOM>\n" +
                "            <ORGID>UBPL</ORGID>\n" +
                "            <SITEID>$districtFormatted</SITEID>\n" +
                "            <DISTRICT>$districtCode</DISTRICT>\n" +
                "            <CATEGORY>$primaryCateg</CATEGORY>\n" +
                "        </ELLIPSEITEM>\n" +
                "    </MXE-ITEM-XMLSet>\n" +
                "</SyncMXE-ITEM-XML>"
        log.info("message: $xmlMessage")

//      membaca informasi instance Ellipse yang sedang aktif dan assign ke variable "ip" dengan tipe InetAddress
        InetAddress ip = InetAddress.getLocalHost()

//      membaca url Ellipse yang sedang aktif dan assign ke variable "hostname" dengan tipe String
        String hostname = ip.getHostName()
        String hostUrl = getHostUrl(hostname)

//      mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
        String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-ITEM-XML"
        log.info("postUrl: $postUrl")

// proses berikut menjelaskan urutan pengiriman data ke API Maximo
        def url = new URL(postUrl)
        HttpURLConnection connection = url.openConnection()
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.setRequestProperty("Content-Type", "application/xml")
        connection.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")

        try {
// pada baris ini, pesan yang sudah diformat dalam bentuk xml dikirimkan ke API Maximo
            if (nextAction == 0 || nextAction == 1){
                connection.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
                log.info("responsecode: ${connection.getResponseCode()}")

                if (connection.getResponseCode() != 200) {
                    String responseMessage = connection.getInputStream().getText()
                    log.info("responseMessage: $responseMessage")
                    String errorCode = "9999"
                    request.setErrorMessage(
                            new MsoErrorMessage("",
                                    errorCode,
                                    responseMessage,
                                    MsoErrorMessage.ERR_TYPE_ERROR,
                                    MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

                    MsoField errField = new MsoField()
                    errField.setName("SUPPLIER_NAME2I")
                    request.setCurrentCursorField(errField)

// jika error maka kembalikan request / input ke layar ellipse
                    return request
                }
            }
        } catch(Exception e){
            log.info("Exception: $e")
// membaca response dari API Maximo. Jika response code bukan "200" maka kembalikan error
            if (connection.getResponseCode() != 200) {
                String responseMessage = connection.getInputStream().getText()
                log.info("responseMessage: $responseMessage")
                String errorCode = "9999"
                request.setErrorMessage(
                        new MsoErrorMessage("",
                                errorCode,
                                responseMessage,
                                MsoErrorMessage.ERR_TYPE_ERROR,
                                MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

                MsoField errField = new MsoField()
                errField.setName("SUPPLIER_NAME2I")
                request.setCurrentCursorField(errField)

// jika error maka kembalikan request / input ke layar ellipse
                return request
            }
        }
        return null
    }
}
