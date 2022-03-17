//Revision History
//20210703 - Arsiadi Initial Coding
//.................. Development Integrasi Ellipse - Maximo untuk mentrigger pembuatan atau perubahan Supllier ke Maximo
//.................. pada saat Create / Modify Supplier di Ellipse

/*Library yang digunakan*/
import com.mincom.ellipse.edoi.ejb.msf200.MSF200Key
import com.mincom.ellipse.edoi.ejb.msf200.MSF200Rec
import com.mincom.ellipse.edoi.ejb.msf20a.MSF20AKey
import com.mincom.ellipse.edoi.ejb.msf20a.MSF20ARec
import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.eql.Constraint
import com.mincom.eql.impl.QueryImpl
import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils

import javax.naming.InitialContext
import javax.sql.DataSource

//definisi class hooks untuk program MSO200 (Screen MSM20DA). "Extends MSOHook" perlu ditambahkan di setiap hooks yang dibangun untuk program MSO
class MSM20DA extends MSOHook{
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
    GenericMsoRecord onPostSubmit(GenericMsoRecord request, GenericMsoRecord response){
        log.info("Arsiadi Hooks MSM20DA onPostSubmit version: $hookVersion")
        log.info("NextAction: ${request.nextAction}")
        log.info("SupplierNo: ${request.getField("SUPPLIER_CODE1I").getValue()}")
        log.info("SupplierName: ${request.getField("SUPPLIER_NAME1I").getValue()}")
        log.info("District: ${request.getField("DSTRCT_CODE1I").getValue()}")

// definisi variable dengan membaca nilai user yang dimasukkan di layar MSM20DA untuk dikirimkan ke Maximo
        String supplierNo = request.getField("SUPPLIER_CODE1I").getValue()
        String supplierName = request.getField("SUPPLIER_NAME1I").getValue()
        String supplierNameFormatted = StringEscapeUtils.escapeXml(supplierName.trim())

// informasi alamat, kontak, dan mata uang dari supplier tidak terdapat di layar MSM20D namun bisa didapatkan dari database, kumpulan baris berikut adalah tahapan proses membaca database
        Constraint consSupplierNo = MSF200Key.supplierNo.equalTo(request.getField("SUPPLIER_CODE1I").getValue().trim())
        Constraint consSupplierNo20A = MSF20AKey.supplierNo.equalTo(request.getField("SUPPLIER_CODE1I").getValue().trim())

        if (request.nextAction == 1) {
            def query = new QueryImpl(MSF200Rec.class).and(consSupplierNo)
            MSF200Rec msf200Rec = tools.edoi.firstRow(query)

            def query20a = new QueryImpl(MSF20ARec.class).and(consSupplierNo20A)
            MSF20ARec msf20ARec = tools.edoi.firstRow(query20a)

            log.info("query 200 result: ${msf200Rec}")
            log.info("query 20A result: ${msf20ARec}")

            if (msf200Rec) {
                String address1 = msf200Rec.getOrderAddr_1() != null ? msf200Rec.getOrderAddr_1().trim() : ""
                String address2 = msf200Rec.getOrderAddr_2() != null ? msf200Rec.getOrderAddr_2().trim() : ""
                String address3 = msf200Rec.getOrderAddr_3() != null ? msf200Rec.getOrderAddr_3().trim() : ""
                String contact = msf200Rec.getOrderContact() != null ? msf200Rec.getOrderContact().trim() : ""
                String fax = msf200Rec.getOrderFaxNo() != null ? msf200Rec.getOrderFaxNo().trim() : ""
                String phone = msf200Rec.getOrderPhone() != null ? msf200Rec.getOrderPhone().trim() : ""
                String zip = msf200Rec.getOrderZip() != null ? msf200Rec.getOrderZip().trim() : ""
                String currencyType = msf200Rec.getCurrencyType() != null ? msf200Rec.getCurrencyType().trim() : ""

                String contactEmail
                if (msf20ARec){
                    contactEmail = msf20ARec.getOrderEmailAddr() ? msf20ARec.getOrderEmailAddr().trim() : ""
                } else{
                    contactEmail = ""
                }

// mendefinisikan pesan dalam format XML (sebagai String) untuk dikirimkan ke API Maximo
                def xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SyncMXE-COMP-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T11:37:06+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                        "    <MXE-COMP-XMLSet>\n" +
                        "        <COMPANIES>\n" +
                        "            <COMPANY>${supplierNo.trim()}</COMPANY>\n" +
                        "            <NAME>$supplierNameFormatted</NAME>\n" +
                        "            <TYPE>V</TYPE>\n" +
                        "            <CURRENCYCODE>$currencyType</CURRENCYCODE>\n" +
                        "            <ADDRESS1>${StringEscapeUtils.escapeXml(address1)}</ADDRESS1>\n" +
                        "            <ADDRESS2>${StringEscapeUtils.escapeXml(address2)}</ADDRESS2>\n" +
                        "            <ADDRESS3>${StringEscapeUtils.escapeXml(address3)}</ADDRESS3>\n" +
                        "            <ADDRESS4>${StringEscapeUtils.escapeXml(zip)}</ADDRESS4>\n" +
                        "            <PHONE>${StringEscapeUtils.escapeXml(phone)}</PHONE>\n" +
                        "            <FAX>${StringEscapeUtils.escapeXml(fax)}</FAX>\n" +
                        "            <ORGID>UBPL</ORGID>\n"
                if ((contact != null && contact != "") || (contactEmail != null && contactEmail != "") || (fax != null && fax != "") || (phone != null && phone != "")){
                    xmlMessage = "$xmlMessage" + "            <COMPCONTACT>\n"

                    if (contact != null && contact != ""){
                        xmlMessage = xmlMessage + "                <CONTACT>${StringEscapeUtils.escapeXml(contact)}</CONTACT>\n"
                    }

                    if (contactEmail != null && contactEmail != ""){
                        xmlMessage = xmlMessage + "                <EMAIL>${StringEscapeUtils.escapeXml(contactEmail)}</EMAIL>\n"
                    }

                    if (fax != null && fax != ""){
                        xmlMessage = xmlMessage + "                <FAXPHONE>${StringEscapeUtils.escapeXml(fax)}</FAXPHONE>\n" +
                                "                <POSITION>DIREKTUR</POSITION>\n"
                    }

                    if (phone != null && phone != ""){
                        xmlMessage = xmlMessage + "                <VOICEPHONE>${StringEscapeUtils.escapeXml(phone)}</VOICEPHONE>\n"
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

//      mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
                def postUrl
                if (hostname.contains("ellprd"))
                {
                    postUrl = "http://maximo-production.ptpjb.com:9080/meaweb/es/EXTSYS1/MXE-COMP-XML"
                }
                else if (hostname.contains("elltrn"))
                {
                    postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-COMP-XML"
                }
                else
                {
                    postUrl = "http://maximo-training.ptpjb.com:9082/meaweb/es/EXTSYS1/MXE-COMP-XML"
                }

// proses berikut menjelaskan urutan pengiriman data ke API Maximo
                def url = new URL(postUrl)
                HttpURLConnection connection = url.openConnection()
                connection.setRequestMethod("POST")
                connection.setDoOutput(true)
                connection.setRequestProperty("Content-Type", "application/xml")
                connection.setRequestProperty("maxauth", "bXhpbnRhZG06bXhpbnRhZG0=")

// pada baris ini, pesan yang sudah diformat dalam bentuk xml dikirimkan ke API Maximo
                connection.getOutputStream().write(xmlMessage.getBytes("UTF-8"))
                log.info("responsecode: ${connection.getResponseCode()}")

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
                    errField.setName("SUPPLIER_CODE2I")
                    request.setCurrentCursorField(errField)

// jika error maka kembalikan request / input ke layar ellipse
                    return request
                }
            }
        }
// jika tidak error maka kembalikan response
        return response
    }
}
