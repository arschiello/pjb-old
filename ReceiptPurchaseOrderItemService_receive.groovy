import com.mincom.ellipse.hook.hooks.ServiceHook

class ReceiptPurchaseOrderItemService_receive extends ServiceHook{
    String hookVersion = "1"

    @Override
    Object onPostExecute(Object request, Object results){
        log.info("Arsiadi ReceiptPurchaseOrderItemService_receive hooks onPostExecute version: $hookVersion")
    }

    @Override
    Object onPreExecute(Object request){
        log.info("Arsiadi ReceiptPurchaseOrderItemService_receive hooks onPreExecute version: $hookVersion")
    }
}
