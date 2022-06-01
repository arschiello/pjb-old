/**
 * a9ra5213 - EMS Jan 2016
 *
 * Initial Coding
 **/

import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import static java.util.UUID.randomUUID;
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.eql.Constraint
import com.mincom.eql.StringConstraint
import com.mincom.eql.impl.QueryImpl
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceReadReplyDTO
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.batch.environment.*;
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Key
import com.mincom.ellipse.edoi.ejb.msf620.MSF620Rec
import com.mincom.ellipse.ejra.mso.MsoField

public class MSM232A extends MSOHook {

        String hookVersion = "1"
        String uuid="";
        String entity_key="";
        String ReqNo = ""
        String ProjNo = ""
        String WO_PROJ_IND = ""
        Integer CntLine = 0
        String ChangeLine = ""
        String WOProjNo = ""

        @Override
        public GenericMsoRecord onDisplay(GenericMsoRecord screen){
                log.info("Hooks onDisplay logging.version: ${hookVersion}")

                return null
        }
        @Override
        public GenericMsoRecord onPreSubmit(GenericMsoRecord screen) {
                log.info("Hooks onPreSubmit MSM232A logging.version: ${hookVersion}")

                int x = screen.nextAction
                log.info ("Next Action : " + x.toString())
                if (x == 0){

                        ReqNo = screen.getField("PURCH_REQ_NO1I").getValue()
                        ProjNo = screen.getField("WO_PROJECT1I1").getValue()
                        WO_PROJ_IND = screen.getField("PROJECT_IND1I1").getValue()
                        log.info ("ReqNo:" + screen.getField("PURCH_REQ_NO1I").getValue())
                        log.info ("AZIZ_ProjNo:" + screen.getField("WO_PROJECT1I1").getValue())
                        log.info ("AZIZ_WO_PROJ_IND:" + screen.getField("PROJECT_IND1I1").getValue())
                        if (WO_PROJ_IND == "W" && ProjNo.trim() != ""){
                                try {
                                        MSF620Key msf620key = new MSF620Key();

                                        Constraint c1 = MSF620Key.dstrctCode.equalTo(tools.commarea.District);
                                        Constraint c2 = MSF620Key.workOrder.equalTo(ProjNo);

                                        def query = new QueryImpl(MSF620Rec.class).and(c1).and(c2);

                                        MSF620Rec msf620Rec = tools.edoi.firstRow(query);

                                        if (msf620Rec != null){
                                                log.info("Record was found")
                                                WOProjNo = msf620Rec.getProjectNo()
                                        }else{
                                                log.info("No record was found")
                                        }
                                }catch (EnterpriseServiceOperationException e){
                                        List <ErrorMessageDTO> listError = e.getErrorMessages()
                                        listError.each{ErrorMessageDTO errorDTO ->
                                                        log.info ("Erorr Code:" + errorDTO.getCode())
                                                        log.info ("Error Message:" + errorDTO.getMessage())
                                                        log.info ("Error Fields: " + errorDTO.getFieldName())
                                                }
                                }
                        }
                        else if ( WO_PROJ_IND.trim() == ""  && ProjNo.trim() == ""){
                                                screen.setErrorMessage(new MsoErrorMessage("", "9999", "Nomor PRK Belum di Input Pada PR : " + ReqNo, MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
                                                MsoField WO_PROJ_FIELD = new MsoField()
                                                WO_PROJ_FIELD.setName("WO_PROJECT1I1")
                                                screen.setCurrentCursorField(WO_PROJ_FIELD)
                                                return screen


                        }

                }
                return null
        }

        @Override
        public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
                log.info("Hooks onPostSubmit MSM23CA logging.version: ${hookVersion}")
                entity_key = tools.commarea.District + "PR" + input.getField("PURCH_REQ_NO1I").getValue()
                int x = input.nextAction
                if (x == 0){
                        try {
                                /*
                                BatchEnvironment env = new BatchEnvironment()
                                File checkFile1 = new File(env.getWorkDir().toString()+"/PBC." + entity_key)
                                if (checkFile1.isFile() == true){
                                        log.info ("FILE FOUND")
                                        def lines = checkFile1.readLines()
                                        CntLine = lines.size()
                                        log.info ("LINE SIZE : " + CntLine)
                                        if (CntLine >= 1){
                                                log.info ("MODIFY LINE 3")
                                                ChangeLine = checkFile1.readLines().get(2).substring(0)
                                                BufferedReader file = new BufferedReader(new FileReader(env.getWorkDir().toString()+"/PBC." + entity_key));
                                                String line;
                                                String tmp = "";
                                                while ((line = file.readLine()) != null) tmp += line + '\n';
                                                file.close();

                                                tmp = tmp.replace(ChangeLine, "3.PROJ : " + ProjNo);
                                                FileOutputStream fileOut = new FileOutputStream(env.getWorkDir().toString()+"/PBC." + entity_key);
                                                fileOut.write(tmp.getBytes());
                                                fileOut.close();
                                        }
                                }else{
                                        log.info ("FILE NOT FOUND, CREATE FILE")
                                        log.info ("WRITE FILE : ${env.getWorkDir().toString()}")
                                        FileWriter fstream = new FileWriter(env.getWorkDir().toString()+"/PBC." + entity_key);
                                        BufferedWriter fileC = new BufferedWriter(fstream);
                                        fileC.write("1." + "\n" + "2." + "\n" + "3.PROJ : " + ProjNo)
                                        fileC.close()
                                }
                                */
                        }catch (EnterpriseServiceOperationException e){
                                List <ErrorMessageDTO> listError = e.getErrorMessages()
                                listError.each{ErrorMessageDTO errorDTO ->
                                        log.info ("Erorr Code:" + errorDTO.getCode())
                                        log.info ("Error Message:" + errorDTO.getMessage())
                                        log.info ("Error Fields: " + errorDTO.getFieldName())
                                }
                        }
                }
                return result
        }
}

