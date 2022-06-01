//Revision History
//20210824 - Arsiadi Initial Coding
//.................. Development Integrasi Ellipse - Maximo untuk mentrigger modifikasi
//.................. Budget Code ke Maximo pada saat modifikasi Project di Ellipse

/*Library yang digunakan*/

import com.mincom.ellipse.edoi.ejb.msf667.MSF667Key
import com.mincom.ellipse.edoi.ejb.msf667.MSF667Rec
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.ellipse.project.ProjectServiceModifyReplyDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.eql.Constraint
import com.mincom.eql.Query
import com.mincom.eql.impl.QueryImpl
import groovy.sql.Sql

import javax.naming.InitialContext

class ProjectService_modify extends ServiceHook{
//  annotation @Override menunjukkan bahwa event onPostExecute ini akan mengganti class standard Ellipse (jika ada)
//  Untuk program MSE, terdapat 2 event standard yang bisa dimodify dengan tipe parameter Object:
//  1. onPreExecute(Object request) - dijalankan sebelum operation standard Ellipse
//  2. onPostExecute(Object request, GenericMsoRecord response) - dijalankan setelah operation standard Ellipse. Terdapat dua parameter yaitu request dan response
    @Override
    Object onPostExecute(Object input, Object result){
// Casting result ke tipe object "ProjectServiceModifyReplyDTO". Tipe object cast yang bisa digunakan untuk request dan response bisa dilihat dari hasil trace file
        ProjectServiceModifyReplyDTO projectServiceModifyReplyDTO = (ProjectServiceModifyReplyDTO) result
// definisi dan assign variable dengan membaca nilai yang dimasukkan oleh user di layar MSE660 dari object "reply" untuk dikirimkan ke Maximo
        String projectNo = projectServiceModifyReplyDTO.getProjectNo().trim()
        String projectDesc = org.apache.commons.lang.StringEscapeUtils.escapeXml(projectServiceModifyReplyDTO.getProjDesc().trim())
        String districtCode = projectServiceModifyReplyDTO.getDistrictCode()
        String statusProject = projectServiceModifyReplyDTO.getStatus()

        Constraint consDistrictNo = MSF667Key.dstrctCode.equalTo(districtCode)
        Constraint constProjectNo = MSF667Key.projectNo.equalTo(projectNo)
        Constraint constExpRevInd = MSF667Key.expRevInd.equalTo("E")
        Constraint constBudgetCode = MSF667Key.budgetCode.equalTo("0000")
        Constraint constCategoryCode = MSF667Key.categoryCode.equalTo("          ")

        Query queryMSF667 = new QueryImpl(MSF667Rec.class).and(consDistrictNo).and(constProjectNo).and(constExpRevInd).and(constBudgetCode).and(constCategoryCode)
        MSF667Rec msf667Rec = tools.edoi.firstRow(queryMSF667)
        log.info("MSF667 result: $msf667Rec")
        BigDecimal directUnallocFinPeriodEst = msf667Rec.getTotEstCost() ? msf667Rec.getTotEstCost() : 0
// transform kode district Ellipse ke kode Site Id Maximo yang akan dikirim
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

        log.info("Project No: $projectNo")
        log.info("Project Desc: $projectDesc")
        log.info("Site ID: $districtFormatted")
        log.info("directUnallocFinPeriodEst: $directUnallocFinPeriodEst")
// mendefinisikan pesan dalam format XML (sebagai String) untuk dikirimkan ke API Maximo
        String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SyncMXE-PRK-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T12:30:30+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                "    <MXE-PRK-XMLSet>\n" +
                "        <PROJECT_CONTROL>\n" +
                "            <PROJECT_NO>$projectNo</PROJECT_NO>\n" +
                "            <PROJ_DESC>$projectDesc</PROJ_DESC>\n" +
                "            <STATUSPRK>1</STATUSPRK>\n" +
                "            <ORGID>UBPL</ORGID>\n" +
                "            <SITEID>$districtFormatted</SITEID>\n" +
                "            <AMOUNT>$directUnallocFinPeriodEst</AMOUNT>\n" +
                "        </PROJECT_CONTROL>\n" +
                "    </MXE-PRK-XMLSet>\n" +
                "</SyncMXE-PRK-XML>"

        log.info("ARS --- XML: $xmlMessage")
// membaca informasi instance Ellipse yang sedang aktif dan assign ke variable "ip" dengan tipe InetAddress
        InetAddress ip = InetAddress.getLocalHost()
// membaca url Ellipse yang sedang aktif dan assign ke variable "hostname" dengan tipe String
        String hostname = ip.getHostName()
        String hostUrl = getHostUrl(hostname)
// mendefinisikan variable "postUrl" yang akan menampung url tujuan integrasi ke API Maximo
        String postUrl = "${hostUrl}/meaweb/es/EXTSYS1/MXE-PRK-XML"
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

            throw new EnterpriseServiceOperationException(
                    new ErrorMessageDTO(
                            errorCode, responseMessage, "", 0, 0))
// jika error maka kembalikan request / input ke layar ellipse
//            return input
        }
// jika tidak error maka kembalikan response standard ke layar
        return result
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
