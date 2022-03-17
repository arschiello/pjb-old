//Revision History
//20210703 - Arsiadi Initial Coding
//.................. Development Integrasi Ellipse - Maximo untuk mentrigger pembuatan atau perubahan UOM di Maximo
//.................. pada saat Create / Modify UOM di Ellipse

/*Library yang digunakan*/
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.ellipse.hook.hooks.MSOHook
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils

import javax.naming.InitialContext
import javax.sql.DataSource

//definisi class hooks untuk program MSO010 (Screen MSM010B). "Extends MSOHook" perlu ditambahkan di setiap hooks yang dibangun untuk program MSO
class MSM010B extends MSOHook{
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

//  annotation @Override menunjukkan bahwa event onPreSubmit ini akan mengganti class standard Ellipse (jika ada)
//  Untuk program MSO, terdapat 3 event standard yang bisa dimodify dengan tipe parameter GenericMsoRecord:
//  1. onPreSubmit(GeneridMsoRecord request) - dijalankan sebelum fungsi "Submit" standard Ellipse
//  2. onPostSubmit(GenericMsoRecord request, GenericMsoRecord response) - dijalankan setelah fungsi "Submit" standard Ellipse. Terdapat dua parameter yaitu request dan response
//  3. onDisplay(GenericMsoRecord request) - dijalankan pada saat screen dimunculkan
    @Override
    GenericMsoRecord onPreSubmit(GenericMsoRecord request){
        log.info("Arsiadi Hooks MSM010B onPreSubmit version: $hookVersion")
        log.info("NextAction: ${request.nextAction}")

// Instantiate MsoField dan assign ke variable errField untuk menangkap pesan error setelah hooks dijalankan
        MsoField errField = new MsoField()

// membaca informasi instance Ellipse yang sedang aktif dan assign ke variable "ip" dengan tipe InetAddress
        InetAddress ip = InetAddress.getLocalHost()

// membaca url Ellipse yang sedang aktif dan assign ke variable "hostname" dengan tipe String
        String hostname = ip.getHostName()

// mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
        def postUrl
        log.info("FKeys: ${request.getField("FKEYS2I").getValue()}")

// definisi variable dengan membaca nilai user yang dimasukkan di layar MSM010B untuk dikirimkan ke Maximo
        String measureUnitID = request.getField("TABLE_CODE_A2I1").getValue().trim()
        String measureDesc = request.getField("TABLE_DESC2I1").getValue().trim()
        String tableType = request.getField("TABLE_TYPE2I").getValue().trim()
        String activeFlag = request.getField("ACTIVE_FLAG2I1").getValue().trim()
        String actionFlag = request.getField("ACTION2I1").getValue().trim()
        log.info("tableType: $tableType")
        log.info("activeFlag: $activeFlag")
        log.info("actionFlag: $actionFlag")

        String setActiveFlag = ""
        if (activeFlag){
            if (activeFlag.trim() != ""){
                if (activeFlag.trim() == "Y"){
                    setActiveFlag = "1"
                } else {
                    setActiveFlag = "0"
                }
                log.info("setActiveFlag1: $setActiveFlag")
            } else {
                setActiveFlag = "0"
                log.info("setActiveFlag2: $setActiveFlag")
            }
        }

// setting kondisi yang harus dipenuhi untuk pengiriman data ke API Maximo
        if ((request.getField("FKEYS2I").getValue().trim().contains("XMIT-Confirm") && request.nextAction == 0) ||
                (request.getField("FKEYS2I").getValue().trim().contains("XMIT-Update") && request.nextAction == 1)){
// mendefinisikan pesan dalam format XML (sebagai String) untuk dikirimkan ke API Maximo
            String xmlMessage = ""
            if (tableType == 'JABR'){
                if (hostname.contains("ellprd"))
                {
                    postUrl = "http://maximo-production.ptpjb.com:9080/meaweb/es/EXTSYS1/MXE-GLSUB-XML"
                }
                else if (hostname.contains("elltst"))
                {
                    postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-GLSUB-XML"
                }
                else
                {
                    postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-GLSUB-XML"
                }

                if (actionFlag == "D"){
                    xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<SyncMXE-GLSUB-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T12:30:30+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                            "    <MXE-GLSUB-XMLSet>\n" +
                            "        <GLCOASUBLEGDER action=\"Delete\">\n" +
                            "            <ACCOUNTCODE>$measureUnitID</ACCOUNTCODE>\n" +
                            "            <ACTIVE>$setActiveFlag</ACTIVE>\n" +
                            "            <DESCRIPTION>${StringEscapeUtils.escapeXml(measureDesc)}</DESCRIPTION>\n" +
                            "            <ORGID>UBPL</ORGID>\n" +
                            "        </GLCOASUBLEGDER>\n" +
                            "    </MXE-GLSUB-XMLSet>\n" +
                            "</SyncMXE-GLSUB-XML>"
                }
                else{
                    xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<SyncMXE-GLSUB-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T12:30:30+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                            "    <MXE-GLSUB-XMLSet>\n" +
                            "        <GLCOASUBLEGDER>\n" +
                            "            <ACCOUNTCODE>$measureUnitID</ACCOUNTCODE>\n" +
                            "            <ACTIVE>$setActiveFlag</ACTIVE>\n" +
                            "            <DESCRIPTION>${StringEscapeUtils.escapeXml(measureDesc)}</DESCRIPTION>\n" +
                            "            <ORGID>UBPL</ORGID>\n" +
                            "        </GLCOASUBLEGDER>\n" +
                            "    </MXE-GLSUB-XMLSet>\n" +
                            "</SyncMXE-GLSUB-XML>"
                }
            }
            else {
                if (hostname.contains("ellprd"))
                {
                    postUrl = "http://maximo-production.ptpjb.com:9080/meaweb/es/EXTSYS1/MXE-UOM-XML"
                }
                else if (hostname.contains("elltst"))
                {
                    postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-UOM-XML"
                }
                else
                {
                    postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-UOM-XML"
                }

                if (actionFlag == "D"){
                    xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<SyncMXE-UOM-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:21:02+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                            "    <MXE-UOM-XMLSet>\n" +
                            "        <MEASUREUNIT action=\"Delete\">\n" +
                            "            <MEASUREUNITID>$measureUnitID</MEASUREUNITID>\n" +
                            "            <CONTENTUID>1000</CONTENTUID>\n" +
                            "            <DESCRIPTION>${StringEscapeUtils.escapeXml(measureDesc)}</DESCRIPTION>\n" +
                            "            <ORGID>UBPL</ORGID>\n" +
                            "        </MEASUREUNIT>\n" +
                            "    </MXE-UOM-XMLSet>\n" +
                            "</SyncMXE-UOM-XML>"
                }
                else {
                    xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<SyncMXE-UOM-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:21:02+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                            "    <MXE-UOM-XMLSet>\n" +
                            "        <MEASUREUNIT>\n" +
                            "            <MEASUREUNITID>$measureUnitID</MEASUREUNITID>\n" +
                            "            <CONTENTUID>1000</CONTENTUID>\n" +
                            "            <DESCRIPTION>${StringEscapeUtils.escapeXml(measureDesc)}</DESCRIPTION>\n" +
                            "            <ORGID>UBPL</ORGID>\n" +
                            "        </MEASUREUNIT>\n" +
                            "    </MXE-UOM-XMLSet>\n" +
                            "</SyncMXE-UOM-XML>"
                }


            }

            log.info("ARS postUrl: $postUrl")
            log.info("ARS XML MESSAGE: $xmlMessage")

// proses berikut menjelaskan urutan pengiriman data ke API Maximo
            def url = new URL(postUrl)
            HttpURLConnection authConn = url.openConnection()
            authConn.setRequestMethod("POST")
            authConn.setDoOutput(true)
            authConn.setRequestProperty("Content-Type", "application/xml")
            authConn.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")

// pada baris ini, pesan yang sudah diformat dalam bentuk xml dikirimkan ke API Maximo
            authConn.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
            log.info("responsecode: ${authConn.getResponseCode()}")

// membaca response dari API Maximo. Jika response code bukan "200" maka kembalikan error
            if (authConn.getResponseCode() != 200) {
                String responseMessage = authConn.content.toString()
                log.info("responseMessage: $responseMessage")
                String errorCode = "9999"
                request.setErrorMessage(
                        new MsoErrorMessage("",
                                errorCode,
                                responseMessage,
                                MsoErrorMessage.ERR_TYPE_ERROR,
                                MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

                errField.setName("TABLE_TYPE2I")
                request.setCurrentCursorField(errField)
// jika error maka kembalikan request / input ke layar ellipse
                return request
            }
        }
        return null
    }
}
