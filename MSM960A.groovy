//Revision History
//20210703 - Arsiadi Initial Coding
//.................. Development Integrasi Ellipse - Maximo untuk mentrigger pembuatan atau perubahan COA ke Maximo
//.................. pada saat Create / Modify COA di Ellipse

/*Library yang digunakan*/
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.ellipse.hook.hooks.MSOHook
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils

import javax.naming.InitialContext
import javax.sql.DataSource
import java.text.SimpleDateFormat

//definisi class hooks untuk program MSO960 (Screen MSM960A). "Extends MSOHook" perlu ditambahkan di setiap hooks yang dibangun untuk program MSO
class MSM960A extends MSOHook{
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
    GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
//      log.info(string) digunakan untuk menampilkan isi variable atau text pada trace file yang dihasilkan
        log.info("MSM960A Hooks onPreSubmit")

//      def keyword digunakan untuk mendefinisikan tipe variable atau function secara umum sehingga definisi variable dengan tipe apa pun bisa menggunakan def
//      new Date() digunakan untuk menangkap (instantiate) tanggal saat ini dari system
        def date = new Date()

//      SimpleDateFormat digunakan untuk menentukan format tanggal yang akan dibaca
        def sdf = new SimpleDateFormat("yyyy-MM-dd")

//      assign variable activeStatDate dengan tanggal hari ini dari system dengan format yang sudah didefinisikan oleh variable "sdf"
        String activeStatDate = sdf.format(date)

//      Instantiate MsoField dan assign ke variable errField untuk menangkap pesan error setelah hooks dijalankan
        MsoField errField = new MsoField()

//      membaca informasi instance Ellipse yang sedang aktif dan assign ke variable "ip" dengan tipe InetAddress
        InetAddress ip = InetAddress.getLocalHost()

//      membaca url Ellipse yang sedang aktif dan assign ke variable "hostname" dengan tipe String
        String hostname = ip.getHostName()
        String hostUrl = getHostUrl(hostname)

//      mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
        String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-COA-XML"

//        if (hostname.contains("ellprd"))
//        {
//            postUrl = "http://maximo-production.ptpjb.com:9080/meaweb/es/EXTSYS1/MXE-COA-XML"
//        }
//        else if (hostname.contains("elltst"))
//        {
//            postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-COA-XML"
//        }
//        else
//        {
//            postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-COA-XML"
//        }

        log.info("FKeys: ${screen.getField("FKEYS1I").getValue()}")
        log.info("NextAction: ${screen.nextAction}")

//      trace argument untuk keperluan tracing proses
        log.info("NextAction1: ${screen.nextAction}")
        log.info("AccountCode: ${screen.getField("ACCOUNT1I1").getValue()}")
        log.info("Account Desc: ${screen.getField("ACCOUNT_DESC1I1").getValue()}")
        log.info("Active Status: ${screen.getField("ACTIVE_STATUS1I1").getValue()}")
        log.info("Action: ${screen.getField("ACTION1I1").getValue()}")
        log.info("FKeys1: ${screen.getField("FKEYS1I").getValue()}")

        String AccountCode = ""
        String accountSegment1 = ""
        String accountSegment2 = ""
        String accountSegment3 = ""
        String accountSegment4 = ""
        String accountSegment5 = ""
        String accountSegment6 = ""
        String accountSegment7 = ""
        String expElement = ""
        String accountCodeFormatted = ""
//      StringEscapeUtils.escapeXml berfungsi untuk memastikan tidak ada special character yang mengganggu file XML sehingga tidak bisa terbaca
        String accountDesc = StringEscapeUtils.escapeXml(screen.getField("ACCOUNT_DESC1I1").getValue().trim())
        String ActiveStatus = screen.getField("ACTIVE_STATUS1I1").getValue().trim()
        String action1i1 = screen.getField("ACTION1I1").getValue().trim()
        String activeStat = ""

        if ((screen.getField("ACTIVE_STATUS1I1").getValue().trim() == "A" && screen.nextAction == 0 && screen.getField("FKEYS1I").getValue().trim() == "XMIT-Confirm, F8-Modify") ||
                ((screen.getField("ACTIVE_STATUS1I1").getValue().trim() == "I" || screen.getField("ACTIVE_STATUS1I1").getValue().trim() == "A") && screen.nextAction == 1 && screen.getField("FKEYS1I").getValue().trim() == "XMIT-Update, F7-Next Screen, F8-Create") ||
                (screen.getField("ACTION1I1").getValue() == "D")){
//      definisi variable yang akan dikirimkan ke Maximo
            AccountCode = screen.getField("ACCOUNT1I1").getValue().trim()
            accountSegment1 = AccountCode.substring(0,1).trim()
            accountSegment2 = AccountCode.substring(1,3).trim()
            accountSegment3 = AccountCode.substring(3,5).trim()
            accountSegment4 = AccountCode.substring(5,8).trim()
            accountSegment5 = AccountCode.substring(8,11).trim()
            accountSegment6 = AccountCode.substring(11,13).trim()
            accountSegment7 = AccountCode.substring(13,15).trim()
            expElement = AccountCode.substring(15).trim()
            if (accountSegment2 == "PT") {
                accountSegment2 = "PN"
            }
            accountCodeFormatted = "$accountSegment1-$accountSegment2-$accountSegment3-$accountSegment4-$accountSegment5-$accountSegment6-$accountSegment7-$expElement"

            if (ActiveStatus == "A"){
                activeStat = "1"
            }
            else if (ActiveStatus == "I"){
                activeStat = "0"
            }

            if (accountSegment3 != "33") return null

            String xmlMessage = ""
//      memasang kondisi yang hanya mentrigger integrasi jika status Account Code Aktif (A) dan Function Keys di layar mengandung kata-kata "XMIT-Confirm, F8-Modify"
            if ((screen.getField("ACTIVE_STATUS1I1").getValue().trim() == "A" && screen.nextAction == 0 && screen.getField("FKEYS1I").getValue().trim() == "XMIT-Confirm, F8-Modify") ||
                    ((screen.getField("ACTIVE_STATUS1I1").getValue().trim() == "I" || screen.getField("ACTIVE_STATUS1I1").getValue().trim() == "A") && screen.nextAction == 1 && screen.getField("FKEYS1I").getValue().trim() == "XMIT-Update, F7-Next Screen, F8-Create")){
//      mendefinisikan pesan dalam format XML (sebagai String) untuk dikirimkan ke API Maximo
                xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SyncMXE-COA-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:21:02+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                        "    <MXE-COA-XMLSet>\n" +
                        "        <CHARTOFACCOUNTS>\n" +
                        "            <GLACCOUNT>$accountCodeFormatted</GLACCOUNT>\n" +
                        "            <GLCOMP01>$accountSegment1</GLCOMP01>\n" +
                        "            <GLCOMP02>$accountSegment2</GLCOMP02>\n" +
                        "            <GLCOMP03>$accountSegment3</GLCOMP03>\n" +
                        "            <GLCOMP04>$accountSegment4</GLCOMP04>\n" +
                        "            <GLCOMP05>$accountSegment5</GLCOMP05>\n" +
                        "            <GLCOMP06>$accountSegment6</GLCOMP06>\n" +
                        "            <GLCOMP07>$accountSegment7</GLCOMP07>\n" +
                        "            <GLCOMP08>$expElement</GLCOMP08>\n" +
                        "            <ORGID>UBPL</ORGID>\n" +
                        "            <ACTIVEDATE>$activeStatDate</ACTIVEDATE>\n" +
                        "            <ACTIVE>$activeStat</ACTIVE>\n" +
                        "        </CHARTOFACCOUNTS>\n" +
                        "    </MXE-COA-XMLSet>\n" +
                        "</SyncMXE-COA-XML>"
            }

            if (screen.nextAction == 1 && action1i1 == "D" && screen.getField("FKEYS1I").getValue().trim() == "XMIT-Update, F7-Next Screen, F8-Create") {
                xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SyncMXE-COA-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:21:02+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                        "    <MXE-COA-XMLSet>\n" +
                        "        <CHARTOFACCOUNTS action=\"Delete\">\n" +
                        "            <GLACCOUNT>$accountCodeFormatted</GLACCOUNT>\n" +
                        "            <GLCOMP01>$accountSegment1</GLCOMP01>\n" +
                        "            <GLCOMP02>$accountSegment1</GLCOMP02>\n" +
                        "            <GLCOMP03>$accountSegment1</GLCOMP03>\n" +
                        "            <GLCOMP04>$accountSegment1</GLCOMP04>\n" +
                        "            <GLCOMP05>$accountSegment1</GLCOMP05>\n" +
                        "            <GLCOMP06>$accountSegment1</GLCOMP06>\n" +
                        "            <GLCOMP07>$accountSegment1</GLCOMP07>\n" +
                        "            <GLCOMP08>$accountSegment1</GLCOMP08>\n" +
                        "            <ORGID>UBPL</ORGID>\n" +
                        "            <ACTIVEDATE>$activeStatDate</ACTIVEDATE>\n" +
                        "            <ACTIVE>$activeStat</ACTIVE>\n" +
                        "        </CHARTOFACCOUNTS>\n" +
                        "    </MXE-COA-XMLSet>\n" +
                        "</SyncMXE-COA-XML>"
            }

            log.info("ARS --- XML: $xmlMessage")
            log.info("Account Formatted: $accountCodeFormatted")
            log.info("PostUrl: $postUrl")
            log.info("Active Date: $activeStatDate")
            log.info("Active Status: $activeStat")
            log.info("Active Status: ${screen.getField("ACTIVE_STATUS1I1").getValue()}")
            log.info("Account Description: $accountDesc")
            log.info("action1i1: $action1i1")

// proses berikut menjelaskan urutan pengiriman data ke API Maximo
            def url = new URL(postUrl)
            HttpURLConnection authConn = url.openConnection()
            authConn.setDoOutput(true)
            authConn.setRequestProperty("Content-Type", "application/xml")
            authConn.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")
            authConn.setRequestMethod("POST")

//          pada baris ini, pesan yang sudah diformat dalam bentuk xml dikirimkan ke API Maximo
            authConn.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
            log.info("responsecode: ${authConn.getResponseCode()}")

//          membaca response dari API Maximo. Jika response code bukan "200" berarti error
            if (authConn.getResponseCode() != 200) {
                String responseMessage = authConn.content.toString()
                log.info("responseMessage: $responseMessage")
                String errorCode = "9999"
                screen.setErrorMessage(
                        new MsoErrorMessage("",
                                errorCode,
                                responseMessage,
                                MsoErrorMessage.ERR_TYPE_ERROR,
                                MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))

                errField.setName("ACCOUNT1I1")
                screen.setCurrentCursorField(errField)
// jika error maka kembalikan request / input ke layar ellipse
                return screen
            }

// jika tidak error maka kembalikan null
            return null
        }
    }
}
