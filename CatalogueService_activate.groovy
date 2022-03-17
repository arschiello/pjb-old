import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3101.instances.CatalogueDTO
import com.mincom.ellipse.types.m3101.instances.CatalogueServiceResult
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils

import javax.naming.InitialContext

class CatalogueService_activate extends ServiceHook{
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
    Object onPostExecute(Object request, Object results){
        log.info("ARS Hooks CatalogueService_activate onPostExecute version: $hookVersion")

        InetAddress ip = InetAddress.getLocalHost()
        String hostname = ip.getHostName()

        def postUrl
        if (hostname.contains("ellprd"))
        {
            postUrl = "http://maximo-production.ptpjb.com:9080/meaweb/es/EXTSYS1/MXE-ITEM-XML"
        }
        else if (hostname.contains("elltst"))
        {
            postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-ITEM-XML"
        }
        else
        {
            postUrl = "http://maximo-training.ptpjb.com:9080/meaweb/es/EXTSYS1/MXE-ITEM-XML"
        }

        CatalogueDTO req = (CatalogueDTO)request
        CatalogueServiceResult res = (CatalogueServiceResult)results

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

        String stockCode = res.getCatalogueDTO().getStockCode().getValue().trim()
        String templateName = res.getCatalogueDTO().getCatalogueTemplateName().getValue()
        String stockDescription = res.getCatalogueDTO().getDescription().getValue()

        log.info("stockCode: $stockCode")
        log.info("templateName: $templateName")
        log.info("stockDescription: $stockDescription")
        log.info("districtCode: $districtCode")
        log.info("districtFormatted: $districtFormatted")

        String description = res.getCatalogueDTO().getCatalogueTemplateName().getValue() ? res.getCatalogueDTO().getCatalogueTemplateName().getValue() : res.getCatalogueDTO().getDescription().getValue()
        log.info("description: $description")
        String stockDesc = StringEscapeUtils.escapeXml(description)
        if (stockDesc){
            if (stockDesc.trim().length() > 50){
                stockDesc = stockDesc.substring(0, 49)
            }
        }
        String warehouseId = "WH$districtFormatted"
        String unitOfMeasure = res.getCatalogueDTO().getUnitOfIssue().getValue().trim()

        log.info("warehouseId: $warehouseId")
        log.info("unitOfMeasure: $unitOfMeasure")
        log.info("stockDesc: $stockDesc")

        String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SyncMXE-ITEM-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:45:48+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                "    <MXE-ITEM-XMLSet>\n" +
                "        <ELLIPSEITEM>\n" +
                "            <ITEMNUM>$stockCode</ITEMNUM>\n" +
                "            <DESCRIPTION>$stockDesc</DESCRIPTION>\n" +
                "            <AVAILABLE>0</AVAILABLE>\n" +
                "            <SOH>0</SOH>\n" +
                "            <UOM>$unitOfMeasure</UOM>\n" +
                "            <STOREROOM></STOREROOM>\n" +
                "            <ORGID>UBPL</ORGID>\n" +
                "            <SITEID>$districtFormatted</SITEID>\n" +
                "            <DISTRICT></DISTRICT>\n" +
                "            <CATEGORY>K1</CATEGORY>\n" +
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
            return request
        }
        return results
    }
}
