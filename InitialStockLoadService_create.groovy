import com.mincom.ellipse.edoi.ejb.msf100.MSF100Key
import com.mincom.ellipse.edoi.ejb.msf100.MSF100Rec
import com.mincom.ellipse.edoi.ejb.msf200.MSF200Key
import com.mincom.ellipse.edoi.ejb.msf200.MSF200Rec
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3140.instances.InitialStockLoadDTO
import com.mincom.ellipse.types.m3140.instances.InitialStockLoadServiceResult
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.eql.Constraint
import com.mincom.eql.impl.QueryImpl
import groovy.sql.Sql

import javax.naming.InitialContext

class InitialStockLoadService_create extends ServiceHook{
    String hookVersion = "1"

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
    Object onPostExecute(Object loadDto, Object result){
        log.info("Arsiadi Hooks InitialStockLoadService_create onPostExecute version: $hookVersion")

        InetAddress ip
        ip = InetAddress.getLocalHost()
        String hostname = ip.getHostName()
        String hostUrl = getHostUrl(hostname)
        String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-ITEM-XML"

        InitialStockLoadDTO request = (InitialStockLoadDTO)loadDto
        InitialStockLoadServiceResult response = (InitialStockLoadServiceResult)result

        InitialStockLoadDTO initialStockLoadDTO = response.getInitialStockLoadDTO()
        String stockCode = initialStockLoadDTO.getStockCode().getValue().trim()
        String stockDesc = org.apache.commons.lang.StringEscapeUtils.escapeXml(initialStockLoadDTO.getStockDescription().getValue().trim())
        BigDecimal soh = initialStockLoadDTO.getQuantityToAdd().getValue()
        String available = soh.toString().trim()
        String districtCode = initialStockLoadDTO.getDistrictCode().getValue().trim()
        String warehouseID = initialStockLoadDTO.getWarehouseId().getValue().trim()

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

        Constraint consStockCode = MSF100Key.stockCode.equalTo(stockCode)
        def query = new QueryImpl(MSF100Rec.class).and(consStockCode)
        MSF100Rec msf100Rec = tools.edoi.firstRow(query)
        String uom = msf100Rec.getUnitOfIssue().trim()
//        String warehouseId = "WH$districtFormatted"
        String warehouseId = initialStockLoadDTO.getWarehouseId() ? initialStockLoadDTO.getWarehouseId().getValue() : ""
        log.info("Arsiadi warehouseId: $warehouseId")

        String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SyncMXE-ITEM-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:45:48+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                "    <MXE-ITEM-XMLSet>\n" +
                "        <ELLIPSEITEM>\n" +
                "            <ITEMNUM>$stockCode</ITEMNUM>\n" +
                "            <DESCRIPTION>$stockDesc</DESCRIPTION>\n" +
                "            <AVAILABLE>$available</AVAILABLE>\n" +
                "            <SOH>$available</SOH>\n" +
                "            <UOM>$uom</UOM>\n" +
                "            <STOREROOM>$warehouseId</STOREROOM>\n" +
                "            <ORGID>UBPL</ORGID>\n" +
                "            <SITEID>$districtFormatted</SITEID>\n" +
                "            <DISTRICT>$districtCode</DISTRICT>\n" +
                "            <CATEGORY>K1</CATEGORY>\n" +
                "        </ELLIPSEITEM>\n" +
                "    </MXE-ITEM-XMLSet>\n" +
                "</SyncMXE-ITEM-XML>"

        def url = new URL(postUrl)
        HttpURLConnection connection = url.openConnection()
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.setRequestProperty("Content-Type", "application/xml")
        connection.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")

        try{
            connection.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
            log.info("responsecode: ${connection.getResponseCode()}")
        } catch(Exception e){
            if (connection.getResponseCode() != 200) {
                String responseMessage = connection.content.toString()
                log.info("responseMessage: $responseMessage")
                String errorCode = "9999"

                throw new EnterpriseServiceOperationException(
                        new ErrorMessageDTO(
                                errorCode, responseMessage, "", 0, 0))
                return loadDto
            }
        }
        return result
    }
}
