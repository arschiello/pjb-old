import com.mincom.ellipse.app.security.SecurityToken
import com.mincom.ellipse.errors.exceptions.FatalException
import com.mincom.ellipse.script.plugin.GenericScriptExecute
import com.mincom.ellipse.script.plugin.GenericScriptPlugin
import com.mincom.ellipse.script.plugin.GenericScriptResult
import com.mincom.ellipse.script.plugin.GenericScriptResults
import com.mincom.ellipse.script.plugin.RequestAttributes
import groovy.sql.Sql
import javax.naming.InitialContext

class mse140FetchLockFlag extends GenericScriptPlugin implements GenericScriptExecute{
    String version = "1"

    InitialContext initial = new InitialContext()
    Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
    def sql = new Sql(CAISource)

    @Override
    GenericScriptResults execute (SecurityToken securityToken, List<RequestAttributes> list, Boolean aBoolean)
    throws FatalException{
        log.info("--- [ARSIADI] fetchPlanUseDate.groovy execute version: $version")
        GenericScriptResults results = new GenericScriptResults()

        String lockScreen = "N"
        String lockScreenDummy = "N"

        RequestAttributes requestAttributes = list[0]
        String ireqNoAtt = requestAttributes.getAttributeStringValue("ireqNo")
        String ireqNo = ireqNoAtt ? ireqNoAtt.trim() : null
        String ireqTypeAtt = requestAttributes.getAttributeStringValue("ireqType")
        String ireqType = ireqTypeAtt ? "${ireqTypeAtt.trim()}" : null
        String districtCode = securityToken.getDistrict()

        log.info("--- ireqNoAtt: $ireqNoAtt")
        log.info("--- ireqTypeAtt: $ireqTypeAtt")

        log.info("--- districtCode: $districtCode")
        log.info("--- ireqNo: $ireqNo")
        log.info("--- ireqType: $ireqType")

        if (!ireqNoAtt) return null
        if (!ireqTypeAtt) return null

        if(ireqType.equals("PR")){
            String queryMSF232 = "SELECT * FROM MSF232 WHERE DSTRCT_CODE = '$districtCode' AND REQUISITION_NO LIKE '$ireqNo%'"
            log.info("queryMSF232: $queryMSF232")

            def sql = new Sql(CAISource)
            sql.eachRow(queryMSF232, {
                if (it.PROJECT_NO){
                    if (it.PROJECT_NO.trim() != ""){
                        String projectNo = it.PROJECT_NO.trim()
                        log.info("ARSIADI projectNo: $projectNo")
                        if (projectNo.substring(2, 4) == "3Y"){
                            lockScreen = "Y"
                        }
                    }
                }

                if (it.WORK_ORDER){
                    if (it.WORK_ORDER.trim() != ""){
                        String wo = it.WORK_ORDER.trim()
                        log.info("ARSIADI wo: $wo")
                        def sql2 = new Sql(CAISource)
                        String queryMSF620 = "SELECT PROJECT_NO FROM MSF620 WHERE DSTRCT_CODE = '$districtCode' AND WORK_ORDER = '$wo'"
                        def queryMSF620Result = sql.firstRow(queryMSF620)
                        if (queryMSF620Result){
                            String projectNo = queryMSF620Result.PROJECT_NO != "" ? queryMSF620Result.PROJECT_NO.trim() : ""
                            log.info("Arsiadi2 projectNo: $projectNo")
                            if (projectNo.substring(2, 4) == "3Y"){
                                lockScreen = "Y"
                            }
                        }
                    }
                }

                if (lockScreen != lockScreenDummy){
                    if (lockScreen == "Y" || lockScreenDummy == "Y"){
                        lockScreen = "Y"
                        log.info("arsiadi lockscreen: $lockScreen")
                    }
                }
            })

            GenericScriptResult result3 = new GenericScriptResult()
            result3.addAttribute("prAo", lockScreen)
            results.add(result3)
        }

        return results
    }
}
