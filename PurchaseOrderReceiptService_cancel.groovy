import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.ellipse.types.m3140.instances.PurchaseOrderReceiptDTO
import com.mincom.ellipse.types.m3140.instances.PurchaseOrderReceiptServiceResult
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import javax.naming.InitialContext

class PurchaseOrderReceiptService_cancel extends ServiceHook{
    String version = "1"

    InitialContext initialContext = new InitialContext()
    Object CAISource = initialContext.lookup("java:jboss/datasources/ApplicationDatasource")
    def sql = new Sql(CAISource)

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
    Object onPreExecute(Object input){
        PurchaseOrderReceiptDTO purchaseOrderReceiptDTO = (PurchaseOrderReceiptDTO) input

        String changeNumber = purchaseOrderReceiptDTO.changeNumber ? purchaseOrderReceiptDTO.changeNumber.getValue() : ""
        log.info("---orderNumber: ${purchaseOrderReceiptDTO.purchaseOrderNumber.getValue()}")
        log.info("---orderItemNumber: ${purchaseOrderReceiptDTO.purchaseOrderItemNumber.getValue()}")
        log.info("---receiptRef: ${purchaseOrderReceiptDTO.receiptReference.getValue()}")
        log.info("---receiptRefCancel: ${purchaseOrderReceiptDTO.receiptReferenceCancel.getValue()}")
        log.info("---changeNumber: $changeNumber")

        PurchaseOrderReceiptServiceResult purchaseOrderReceiptServiceResult = new PurchaseOrderReceiptServiceResult()

        if (!changeNumber || changeNumber == ""){
            String orderNumber = purchaseOrderReceiptDTO.purchaseOrderNumber ? purchaseOrderReceiptDTO.purchaseOrderNumber.getValue() : ""
            String orderItemNumber = purchaseOrderReceiptDTO.purchaseOrderItemNumber ?purchaseOrderReceiptDTO.purchaseOrderItemNumber.getValue() : ""
            String receiptRef = purchaseOrderReceiptDTO.receiptReference ? purchaseOrderReceiptDTO.receiptReference.getValue() : ""

            String queryMSF222 = "select min(change_no) changeNo from msf222 " +
                    "where po_no = '$orderNumber' " +
                    "and po_item = '$orderItemNumber' " +
                    "and receipt_ref = '$receiptRef' " +
                    "and value_rcvd_loc > 0"

            log.info("queryMSF222: $queryMSF222")

            def queryMSF222Result = sql.firstRow(queryMSF222)
            log.info("queryMSF222Result: $queryMSF222Result")
            if (queryMSF222Result){
                String changeNo = queryMSF222Result.changeNo ? queryMSF222Result.changeNo : ""
                purchaseOrderReceiptDTO.changeNumber.setValue(changeNo)

                purchaseOrderReceiptServiceResult.setPurchaseOrderReceiptDTO(purchaseOrderReceiptDTO)
            }
            log.info("---changeNumber2: ${purchaseOrderReceiptServiceResult.getPurchaseOrderReceiptDTO().changeNumber.getValue()}")
        }
        return null
    }


}
