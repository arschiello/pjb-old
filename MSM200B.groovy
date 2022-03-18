import com.mincom.ellipse.edoi.ejb.msf200.MSF200Key
import com.mincom.ellipse.edoi.ejb.msf200.MSF200Rec
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.eql.Constraint
import com.mincom.eql.impl.QueryImpl
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils

import javax.naming.InitialContext

class MSM200B extends MSOHook{
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
    GenericMsoRecord onPostSubmit(GenericMsoRecord request, GenericMsoRecord response){
        log.info("Arsiadi Hooks MSM200B onPostSubmit version: $hookVersion")
        log.info("NextAction: ${request.nextAction}")
        log.info("SupplierNo: ${request.getField("SUPPLIER_NO2I").getValue()}")
        log.info("SupplierName: ${request.getField("SUPPLIER_NAME2I").getValue()}")
        log.info("OrderAddress1: ${request.getField("ORDER_ADDR_12I").getValue()}")
        log.info("OrderAddress2: ${request.getField("ORDER_ADDR_22I").getValue()}")
        log.info("OrderAddress3: ${request.getField("ORDER_ADDR_32I").getValue()}")
        log.info("OrderZip: ${request.getField("ORDER_ZIP2I").getValue()}")
        log.info("OrderContact: ${request.getField("ORDER_CONTACT2I").getValue()}")
        log.info("OrderPhone: ${request.getField("ORDER_PHONE2I").getValue()}")
        log.info("OrderEmail: ${request.getField("ORDER_EMAIL_L12I").getValue()}")
        log.info("OrderFax: ${request.getField("ORDER_FAX_NO2I").getValue().trim()}")

        int nextAction = request.nextAction
        String supplierNo = request.getField("SUPPLIER_NO2I").getValue().trim()
        String supplierName = StringEscapeUtils.escapeXml(request.getField("SUPPLIER_NAME2I").getValue().trim())
        String orderAddress1 = StringEscapeUtils.escapeXml(request.getField("ORDER_ADDR_12I").getValue().trim())
        String orderAddress2 = StringEscapeUtils.escapeXml(request.getField("ORDER_ADDR_22I").getValue().trim())
        String orderAddress3 = StringEscapeUtils.escapeXml(request.getField("ORDER_ADDR_32I").getValue().trim())
        String orderZip = request.getField("ORDER_ZIP2I").getValue().trim()
        String orderContact = request.getField("ORDER_CONTACT2I").getValue().trim()
        String orderPhone = request.getField("ORDER_PHONE2I").getValue().trim()
        String orderEmail = request.getField("ORDER_EMAIL_L12I").getValue().trim()
        String orderFax = request.getField("ORDER_FAX_NO2I").getValue().trim()

        String currencyType
        if (nextAction == 1 || nextAction == 0){
            Constraint constSupplierNo = MSF200Key.supplierNo.equalTo(supplierNo)
            def queryMSF200 = new QueryImpl(MSF200Rec.class).and(constSupplierNo)
            MSF200Rec msf200Rec = tools.edoi.firstRow(queryMSF200)
            log.info("queryMSF200 result: $msf200Rec")
            currencyType = msf200Rec ? msf200Rec.getCurrencyType() ? msf200Rec.getCurrencyType().trim() : "" : ""
            log.info("currencyType: $currencyType")
        }

        String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SyncMXE-COMP-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:37:06+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                "    <MXE-COMP-XMLSet>\n" +
                "        <COMPANIES>\n" +
                "            <COMPANY>$supplierNo</COMPANY>\n" +
                "            <NAME>$supplierName</NAME>\n" +
                "            <TYPE>V</TYPE>\n" +
                "            <CURRENCYCODE>$currencyType</CURRENCYCODE>\n" +
                "            <ADDRESS1>$orderAddress1</ADDRESS1>\n" +
                "            <ADDRESS2>$orderAddress2</ADDRESS2>\n" +
                "            <ADDRESS3>$orderAddress3</ADDRESS3>\n" +
                "            <ADDRESS4>$orderZip</ADDRESS4>\n" +
                "            <PHONE>$orderPhone</PHONE>\n" +
                "            <FAX>$orderFax</FAX>\n" +
                "            <ORGID>UBPL</ORGID>\n"
        if ((orderContact != null && orderContact != "") || (orderEmail != null && orderEmail != "") || (orderFax != null && orderFax != "") || (orderPhone != null && orderPhone != "")){
            xmlMessage = "$xmlMessage" + "            <COMPCONTACT>\n"

            if (orderContact != null && orderContact != ""){
                xmlMessage = xmlMessage + "                <CONTACT>$orderContact</CONTACT>\n"
            }

            if (orderEmail != null && orderEmail != ""){
                xmlMessage = xmlMessage + "                <EMAIL>$orderEmail</EMAIL>\n"
            }

            if (orderFax != null && orderFax != ""){
                xmlMessage = xmlMessage + "                <FAXPHONE>$orderFax</FAXPHONE>\n" +
                        "                <POSITION>DIREKTUR</POSITION>\n"
            }

            if (orderPhone != null && orderPhone != ""){
                xmlMessage = xmlMessage + "                <VOICEPHONE>${StringEscapeUtils.escapeXml(orderPhone)}</VOICEPHONE>\n"
            }

            xmlMessage = xmlMessage + "            </COMPCONTACT>\n"
        }

        xmlMessage = xmlMessage + "        </COMPANIES>\n" +
                "    </MXE-COMP-XMLSet>\n" +
                "</SyncMXE-COMP-XML>"

        log.info("message: $xmlMessage")

//      membaca informasi instance Ellipse yang sedang aktif dan assign ke variable "ip" dengan tipe InetAddress
        InetAddress ip = InetAddress.getLocalHost()

//      membaca url Ellipse yang sedang aktif dan assign ke variable "hostname" dengan tipe String
        String hostname = ip.getHostName()
        String hostUrl = getHostUrl(hostname)

//      mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
        String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-COMP-XML"
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
            if (currencyType != ""){
                connection.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
                log.info("responsecode: ${connection.getResponseCode()}")
                if (connection.getResponseCode() != 200) {
                    String exceptionMsg = connection.getInputStream().getText()
                    log.info("responseMessage: $exceptionMsg")
                    String errorCode = "9999"
                    request.setErrorMessage(
                            new MsoErrorMessage("",
                                    errorCode,
                                    exceptionMsg,
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
                String responseMessage = connection.content.toString()
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
