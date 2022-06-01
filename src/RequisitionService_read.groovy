import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.requisition.RequisitionServiceReadReplyDTO
import groovy.sql.Sql
import javax.naming.InitialContext

class RequisitionService_read extends ServiceHook{
    String hookVersion = "1"

    Object onPostExecute(Object input, Object result){
        log.info("[ARSIADI] Hooks onPostExecute RequisitionService_read logging.version: $hookVersion")

        String lockScreen = "N"
        String lockScreenDummy = "N"

        RequisitionServiceReadReplyDTO requisitionServiceReadReplyDTO = (RequisitionServiceReadReplyDTO) result

        String preqNo = requisitionServiceReadReplyDTO.getPreqNo() ? requisitionServiceReadReplyDTO.getPreqNo().trim() : ""
        String districtCode = requisitionServiceReadReplyDTO.getDistrictCode() ? requisitionServiceReadReplyDTO.getDistrictCode().trim() : tools.Commarea.District
        String reqType = requisitionServiceReadReplyDTO.getIreqType() ? requisitionServiceReadReplyDTO.getIreqType().trim() : ""
        String workOrder = requisitionServiceReadReplyDTO.getWorkOrderA().toString()
        String workProj = requisitionServiceReadReplyDTO.getWorkProjA().toString()
        String workProjIndA = requisitionServiceReadReplyDTO.getWorkProjIndA() ? requisitionServiceReadReplyDTO.getWorkProjIndA().trim() : ""

        log.info("preqNo: $preqNo")
        log.info("districtCode: $districtCode")
        log.info("reqType: $reqType")
        log.info("workOrder: $workOrder")
        log.info("workProj: $workProj")
        log.info("workProjIndA: $workProjIndA")

        InitialContext initial = new InitialContext()
        Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")

        if (reqType == "PR"){
            String queryMSF232 = "SELECT * FROM MSF232 WHERE DSTRCT_CODE = '$districtCode' AND REQUISITION_NO LIKE '$preqNo%'"
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
        }
        requisitionServiceReadReplyDTO.setAnswerG(lockScreen)
        return requisitionServiceReadReplyDTO
    }
}
