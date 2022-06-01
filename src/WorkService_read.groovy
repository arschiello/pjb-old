import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3620.instances.WorkDTO
import com.mincom.ellipse.types.m3620.instances.WorkServiceResult
import groovy.sql.Sql

import javax.naming.InitialContext

class WorkService_read extends ServiceHook{
    String hookVersion = "2"

    def getOldPRK(String entityType, String entityValue, String refNo, String seqNum) {
        String result

        String query = "SELECT REF_CODE refCode FROM ELLIPSE.MSF071 WHERE ENTITY_TYPE = '$entityType' AND ENTITY_VALUE = '$entityValue' " +
                "AND REF_NO = '$refNo' AND SEQ_NUM = '$seqNum'"
        log.info("strSql: $query")

        InitialContext initial = new InitialContext()
        Object source = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
        def sql = new Sql(source)
        def queryResult = sql.firstRow(query)

        if (queryResult) {
            result = queryResult.refCode
        }
        else {
            result = "NOTFOUND"
        }
        return result
    }

    Object onPreExecute(Object input) {
        log.info("[Arsiadi] Hooks WorkService_read onPreExecute logging.version: $hookVersion")
        return null
    }

    Object onPostExecute(Object input, Object result) {
        log.info("[Arsiadi] Hooks WorkService_read onPostExecute logging.version: $hookVersion")
        WorkDTO workDTO = (WorkDTO) input
        WorkServiceResult workServiceResult = (WorkServiceResult) result

        String workOrder = workDTO.getWorkOrder().getValue().trim()
        String districtCode = tools.commarea.District

        log.info("WorkOrder: " + workOrder)
        log.info("District: " + districtCode)

        String entityType = "PRK"
        String entityValue = "1$districtCode$workOrder"

        String oldPRK = getOldPRK("PRK", entityValue, "002", "001")
        log.info("Old PRK: $oldPRK")

        String newPRK = getOldPRK("PRK", entityValue, "001", "001")
        log.info("New PRK: $newPRK")

        Attribute[] customAttribute = new Attribute[3]
        log.info("Attribute Size: " + customAttribute.size())

        customAttribute[0] = new Attribute()
        customAttribute[0].setName("pRKNo1")
        customAttribute[0].setValue(oldPRK.trim())

        customAttribute[1] = new Attribute()
        customAttribute[1].setName("pRKNo2")
        customAttribute[1].setValue("")

        customAttribute[2] = new Attribute()
        customAttribute[2].setName("changeDate")
        customAttribute[2].setValue("")
    }
}
