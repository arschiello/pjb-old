import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3660.instances.ProjectEstimateDTO
import com.mincom.ellipse.types.m3660.interfaces.IProjectEstimateDTO
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import groovy.sql.Sql

import javax.naming.InitialContext

class ProjectEstimateService_multipleUpdate extends ServiceHook{
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
    Object onPostExecute(Object input, Object result){
        ProjectEstimateDTO[] projectEstimateDTOS = (ProjectEstimateDTO[])input
        log.info("projectEstimateDTOS size: ${projectEstimateDTOS.size()}")

        String projectNo = projectEstimateDTOS[0].getProjectNo().getValue()
        String districtCode = projectEstimateDTOS[0].getDistrictCode().getValue()
        BigDecimal directUnallocFinPeriodEst = projectEstimateDTOS[0].getDirectUnallocFinPeriodEst().getValue()

        InitialContext initial = new InitialContext()
        Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
        def sql = new Sql(CAISource)

        String queryMSF660 = "SELECT * FROM MSF660 WHERE DSTRCT_CODE = '$districtCode' AND PROJECT_NO = '$projectNo'"
        log.info("queryMSF660: $queryMSF660")
        def queryMSF660Result = sql.firstRow(queryMSF660)
        log.info("queryMSF660Result: $queryMSF660Result")
        log.info("directUnallocFinPeriodEst: $directUnallocFinPeriodEst")

        String projectDesc = queryMSF660Result ? queryMSF660Result.PROJ_DESC : ""
        if (projectDesc && projectDesc != ""){
            projectDesc = org.apache.commons.lang.StringEscapeUtils.escapeXml(projectDesc.trim())
        }

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

        String queryMSF667 = "SELECT * FROM MSF667 WHERE DSTRCT_CODE = '$districtCode' AND PROJECT_NO = '$projectNo' AND EXP_REV_IND = 'E'"
        log.info("queryMSF667: $queryMSF667")
        def queryMSF667Result = sql.firstRow(queryMSF667)
        log.info("queryMSF667Result: $queryMSF667Result")
        log.info("directUnallocFinPeriodEst: $directUnallocFinPeriodEst")

        String dirEstCost = queryMSF667Result ? queryMSF667Result.DIR_EST_COST : "0"

        log.info("Project No: $projectNo")
        log.info("Project Desc: $projectDesc")
        log.info("Site ID: $districtFormatted")
        log.info("dirEstCost: $dirEstCost")
        log.info("directUnallocFinPeriodEst: $directUnallocFinPeriodEst")

        String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SyncMXE-PRK-XML xmlns=\"http://www.ibm.com/maximo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" creationDateTime=\"2021-04-15T12:30:30+07:00\" baseLanguage=\"EN\" transLanguage=\"EN\" event=\"0\" maximoVersion=\"7620190514-1348V7611-365\">\n" +
                "    <MXE-PRK-XMLSet>\n" +
                "        <PROJECT_CONTROL>\n" +
                "            <PROJECT_NO>$projectNo</PROJECT_NO>\n" +
                "            <PROJ_DESC>$projectDesc</PROJ_DESC>\n" +
                "            <STATUS>1</STATUS>\n" +
                "            <ORGID>UBPL</ORGID>\n" +
                "            <SITEID>$districtFormatted</SITEID>\n" +
                "            <AMOUNT>$directUnallocFinPeriodEst</AMOUNT>\n" +
                "        </PROJECT_CONTROL>\n" +
                "    </MXE-PRK-XMLSet>\n" +
                "</SyncMXE-PRK-XML>"

        log.info("ARS --- XML: $xmlMessage")
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
}
