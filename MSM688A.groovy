/**
 * @EMS 2014
 *
 * Revision History
 * 01-Mar-2014.............a9aa6024 Initial Non User Exit- MSM688A
 * ........................
 * */
 
 import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.eql.impl.*
import com.mincom.eql.*
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.System

 import com.mincom.ellipse.edoi.ejb.msf685.MSF685Key
 import com.mincom.ellipse.edoi.ejb.msf685.MSF685Rec
 import com.mincom.ellipse.edoi.ejb.msf686.MSF686Key
 import com.mincom.ellipse.edoi.ejb.msf686.MSF686Rec
 import com.mincom.ellipse.edoi.ejb.msf687.MSF687Key
 import com.mincom.ellipse.edoi.ejb.msf687.MSF687Rec
 import com.mincom.ellipse.edoi.ejb.msf68a.MSF68AKey
 import com.mincom.ellipse.edoi.ejb.msf68a.MSF68ARec
 import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
 import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
 import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
 
 public class MSM688A extends MSOHook {
 
	String hookVersion = "17-OKT-14 10:40"
    String dstrctCode , equipRef , assetTy , subAsset  ,retireTy,dispDate ,depTy,depTy2 ,dispAmtS, actDispBk , actDispTx
	String addDeprBk , addDeprTx , deprMeth ,CurrentPer ,acctProf
	BigDecimal actDispVal , dispAmt
	     @Override
    public GenericMsoRecord onDisplay(GenericMsoRecord screen){

        // onDisplay is only to be used for setting up fields.

        log.info("Hooks onDisplay logging.version: ${hookVersion}")
		actDispBk = screen.getField("ACT_DISP_VAL_A1I").getValue()
		actDispTx =  screen.getField("ACT_DISP_VAL_B1I").getValue()
		addDeprBk = screen.getField("ADD_DEPN_A1I").getValue()
		addDeprTx =  screen.getField("ADD_DEPN_B1I").getValue()
		log.info("act disp BK is ${actDispBk}")
		log.info("act disp T1 is ${actDispTx}")
		
		if ( actDispBk == "0.00") {
			screen.getField("ACT_DISP_VAL_A1I").setValue("")
			
			log.info("act disp BK is ${actDispBk}")
		}
		if (actDispTx == "0.00") {
			screen.getField("ACT_DISP_VAL_B1I").setValue("")
			log.info("act disp T1 is ${actDispTx}")
		}
		if ( addDeprBk == "0.00") {
			screen.getField("ADD_DEPN_A1I").setValue("")
			
			log.info("add depn BK is ${addDeprBk}")
		}
		if ( addDeprTx == "0.00") {
			screen.getField("ADD_DEPN_B1I").setValue("")
			
			log.info("add depn BK is ${addDeprTx}")
		}


        // Override screen field value which will be displayed to the user
        
        //protect field (read-only)
        
        //return the screen which will be displayed (includes the updated field values)
        return null
    }
 @Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){
      
		log.info("Hooks onPreSubmit logging.version: ${hookVersion}")
		
		dstrctCode = tools.commarea.District
		def mss002lnk = tools.eroi.execute('MSS002', {mss002lnk ->
			mss002lnk.dstrctCode = dstrctCode
			mss002lnk.module = '3680'
			mss002lnk.optionX = '2'
		})
		CurrentPer = "20" + mss002lnk.glCodeDstrct
		
		//CurrentPer = tools.commarea.3680CP
		log.info("CurrentPer2 is ${CurrentPer}")
		equipRef = screen.getField("EQUIP_REF1I").getValue()
		assetTy = screen.getField("EQUIP_ASSET_TYPE1I").getValue()
		log.info(equipRef)
		if (equipRef.length() < 11 && assetTy == "A"){
			log.info("assetnya")
			def mss680lnk = tools.eroi.execute('MSS680', {mss680lnk ->
				mss680lnk.dstrctCode = dstrctCode
				mss680lnk.inpString680 = equipRef
				
			})
			equipRef = mss680lnk.assetNo
			log.info("asset is ${equipRef}")
				} 
		 if ( assetTy == "E"){
			log.info("equipnya")
			def mss601lnk = tools.eroi.execute('MSS601', {mss601lnk ->
				mss601lnk.option = 'P'
				mss601lnk.dstrctCode = dstrctCode
				mss601lnk.inputString = equipRef
				
			})
			if ( mss601lnk.equipNo != "            "){
			equipRef = mss601lnk.equipNo
			}
			log.info("equip is ${equipRef}")
				} 
		 
		 subAsset = screen.getField("SUB_ASSET_NO1I").getValue()
		 if (subAsset.length() == 1){
			subAsset = "00000" + subAsset
		  }
		 if (subAsset.length() == 2){
			subAsset = "0000" + subAsset
		}
		if (equipRef.length() == 3){
			subAsset = "000" + subAsset
		}
		log.info("Sub asset is ${subAsset}")
						if (screen.getField("EQUIP_REF1I").isProtected() == false){
				 Constraint key685_1 = MSF685Key.dstrctCode.equalTo(dstrctCode)
		Constraint key685_2 = MSF685Key.assetTy.equalTo(assetTy)
		Constraint key685_3 = MSF685Key.assetNo.equalTo(equipRef)
		Constraint key685_4 = MSF685Key.subAssetNo.equalTo(subAsset)

		QueryImpl query685 = new QueryImpl(MSF685Rec.class).and(key685_1).and(key685_2).and(key685_3).and(key685_4)

		MSF685Rec msf685rec_1 = tools.edoi.firstRow(query685)
		
		if (msf685rec_1 != null){
			 MSF685Rec msf685recb = new MSF685Rec()
			 msf685recb = msf685rec_1
			 retireTy = msf685recb.getRetirementCode()
			 acctProf = msf685recb.getAcctProfile()			

				 				  if (retireTy != "  ") {
					  Constraint key071_5 = MSF071Key.entityType.equalTo("R11")
					  Constraint key071_6 = MSF071Key.entityValue.equalTo(dstrctCode+assetTy+equipRef+subAsset)
					  Constraint key071_7 = MSF071Key.refNo.equalTo("001")
					  Constraint key071_8 = MSF071Key.seqNum.equalTo("001")
		   
					  QueryImpl query071_2 = new QueryImpl(MSF071Rec.class).and(key071_5).and(key071_6).and(key071_7).and(key071_8)
		   
					  MSF071Rec msf071rec_2 = tools.edoi.firstRow(query071_2)
					  
					  if (msf071rec_2 == null){
						  MSF071Rec msf071recc = new MSF071Rec()
						  MSF071Key msf071keyc = new MSF071Key()
						  //Key Columns:
						  msf071keyc.setEntityType("R11")
						  msf071keyc.setEntityValue(dstrctCode+assetTy+equipRef+subAsset)
						  msf071keyc.setRefNo("001")
						  msf071keyc.setSeqNum("001")
						  msf071recc.setPrimaryKey(msf071keyc)
						  //Non-Key Columns:
						  msf071recc.setRefCode(retireTy)
						  tools.edoi.create(msf071recc)
						  log.info("Record MSF071-R11 created:" + retireTy)
					  }else{
						   MSF071Rec msf071recc = new MSF071Rec()
						   msf071recc = msf071rec_2
		   
						   //Update
						   msf071recc.setRefCode(retireTy)
						   tools.edoi.update(msf071recc)
						   log.info("Record MSF071-L22 updated:" + retireTy)
					  }
					  }
								   //Update
								   msf685recb.setRetirementCode(" ")
								   tools.edoi.update(msf685recb)
								   log.info("Record MSF685-688A updated:" )
							  }
				 }
				
		 else {
		 Constraint key685_1 = MSF685Key.dstrctCode.equalTo(dstrctCode)
		Constraint key685_2 = MSF685Key.assetTy.equalTo(assetTy)
		Constraint key685_3 = MSF685Key.assetNo.equalTo(equipRef)
		Constraint key685_4 = MSF685Key.subAssetNo.equalTo(subAsset)

		QueryImpl query685 = new QueryImpl(MSF685Rec.class).and(key685_1).and(key685_2).and(key685_3).and(key685_4)

		MSF685Rec msf685rec_1 = tools.edoi.firstRow(query685)
		
		if (msf685rec_1 != null){
			 MSF685Rec msf685recb = new MSF685Rec()
			 msf685recb = msf685rec_1
			  acctProf = msf685recb.getAcctProfile()
		}
			 actDispBk = screen.getField("ACT_DISP_VAL_A1I").getValue()
		actDispTx =  screen.getField("ACT_DISP_VAL_B1I").getValue()
		addDeprBk = screen.getField("ADD_DEPN_A1I").getValue()
		addDeprTx =  screen.getField("ADD_DEPN_B1I").getValue()
		log.info("act disp BK is ${actDispBk}")
		log.info("act disp T1 is ${actDispTx}")
		
		if (actDispBk == "" ) {
			screen.getField("ACT_DISP_VAL_A1I").setValue("0.00")
			actDispBk = "0"
			depTy = "T1"
			depTy2 = "BK"
			log.info("act disp BK is ${actDispBk}")			
		}
		if (actDispTx == "" ) {
			screen.getField("ACT_DISP_VAL_B1I").setValue("0.00")
			actDispTx = "0"
			depTy = "BK"
			depTy2 ="T1"
			log.info("act disp T1 is ${actDispTx}")			
		}
		if (addDeprBk == "" || addDeprBk == "0.00") {
			screen.getField("ADD_DEPN_A1I").setValue("0.00")
			addDeprBk = "0"
			log.info("act disp BK is ${addDeprBk}")
		}
		if (addDeprTx == "" || addDeprTx == "0.00") {
			screen.getField("ADD_DEPN_B1I").setValue("0.00")
			addDeprTx = "0"
			log.info("act disp T1 is ${addDeprTx}")
		}

		if (actDispBk != "0" && actDispTx == "0") {
			 depTy = "BK"
			 depTy2 ="T1"
		} else if (actDispTx != "0" && actDispBk == "0") {
		     depTy = "T1"
			 depTy2 = "BK"
		}
		
		if (actDispBk == "0" || actDispTx == "0") {
		Constraint key686_1 = MSF686Key.dstrctCode.equalTo(dstrctCode)
		Constraint key686_2 = MSF686Key.assetTy.equalTo(assetTy)
		Constraint key686_3 = MSF686Key.assetNo.equalTo(equipRef)
		Constraint key686_4 = MSF686Key.subAssetNo.equalTo(subAsset)
		Constraint key686_5 = MSF686Key.deprRecType.equalTo(depTy)
		
		QueryImpl query686 = new QueryImpl(MSF686Rec.class).and(key686_1).and(key686_2).and(key686_3).and(key686_4).and(key686_5)

		MSF686Rec msf686rec_1 = tools.edoi.firstRow(query686)
		
		if (msf686rec_1 != null){
			MSF686Rec msf686recb = new MSF686Rec()
			msf686recb = msf686rec_1
			actDispVal = msf686recb.getActRetireVal()
			
			if (actDispVal != 0) {
				screen.setErrorMessage(new MsoErrorMessage("test 1", "1560", "ASSET ALREADY DISPOSE ", MsoErrorMessage.ERR_TYPE_ERROR, MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED))
				
							//need to return screen if there is any error as we do not want to run the business logic when there is any error
							return screen
				} else {
				
				Constraint key686_6 = MSF686Key.dstrctCode.equalTo(dstrctCode)
		Constraint key686_7 = MSF686Key.assetTy.equalTo(assetTy)
		Constraint key686_8 = MSF686Key.assetNo.equalTo(equipRef)
		Constraint key686_9 = MSF686Key.subAssetNo.equalTo(subAsset)
		Constraint key686_10 = MSF686Key.deprRecType.equalTo(depTy2)
		
		QueryImpl query686_2 = new QueryImpl(MSF686Rec.class).and(key686_6).and(key686_7).and(key686_8).and(key686_9).and(key686_10)

		MSF686Rec msf686rec_2 = tools.edoi.firstRow(query686_2)
		if (msf686rec_2 != null){
			MSF686Rec msf686recc = new MSF686Rec()
			MSF686Key msf686keyc = new MSF686Key()
			msf686recc = msf686rec_2
			//dispAmt = msf686recc.getActRetireVal()
			//dispAmtS = toString(dispAmt)
			//dispDate = msf686recc.getDateRetired()
			msf686keyc = msf686recc.getPrimaryKey()
			deprMeth = msf686keyc.getDeprRecType()
			//if (dispDate == null || dispDate == "" ){
			//	dispDate = "        "
			//	log.info("dispDate is ${dispDate}")				
			//	}
			//log.info("dispDate is ${dispDate}")
			//log.info("dispAmt  is ${dispAmt}")
			log.info("deprMeth  is ${deprMeth}")
			Constraint key071_1 = MSF071Key.entityType.equalTo("R12")
			Constraint key071_2 = MSF071Key.entityValue.equalTo(dstrctCode+assetTy+equipRef+subAsset+depTy2)
			Constraint key071_3 = MSF071Key.refNo.equalTo("001")
			Constraint key071_4 = MSF071Key.seqNum.equalTo("001")
																												   
			QueryImpl query071_2 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)
																												   
			MSF071Rec msf071rec_2 = tools.edoi.firstRow(query071_2)
																														 
			if (msf071rec_2 == null){
				MSF071Rec msf071recc = new MSF071Rec()
				MSF071Key msf071keyc = new MSF071Key()
				//Key Columns:
				msf071keyc.setEntityType("R12")
				msf071keyc.setEntityValue(dstrctCode+assetTy+equipRef+subAsset+depTy2)
				msf071keyc.setRefNo("001")
				msf071keyc.setSeqNum("001")
				msf071recc.setPrimaryKey(msf071keyc)
				//Non-Key Columns:
				msf071recc.setRefCode(deprMeth)
				tools.edoi.create(msf071recc)
				log.info("Record MSF071-R12 created:" + deprMeth)
			}else{
				 MSF071Rec msf071recc = new MSF071Rec()
				 msf071recc = msf071rec_2
																												   
				 //Update
				 msf071recc.setRefCode(deprMeth)
				 tools.edoi.update(msf071recc)
				 log.info("Record MSF071-L22 updated:" + deprMeth)
			}
			//msf686recc.setDeprMethod(" ")
			msf686keyc.setDeprRecType("BS")
			msf686recc.setPrimaryKey(msf686keyc)
			tools.edoi.update(msf686recc)
			log.info("Record pre MSF686-688A updated:" )
			Constraint key687_1 = MSF687Key.dstrctCode.equalTo(dstrctCode)
			Constraint key687_2 = MSF687Key.assetTy.equalTo(assetTy)
			Constraint key687_3 = MSF687Key.subAssetNo.equalTo(subAsset)
			Constraint key687_4 = MSF687Key.deprRecType.equalTo(depTy2)
			Constraint key687_5 = MSF687Key.acctProfile.equalTo(acctProf)
			Constraint key687_6 = MSF687Key.seqNo.equalTo("0001")
			Constraint key687_7 = MSF687Key.accountPeriod.equalTo(CurrentPer)
			Constraint key687_8 = MSF687Key.assetNo.equalTo(equipRef)
			QueryImpl query687_1 = new QueryImpl(MSF687Rec.class).and(key687_1).and(key687_2).and(key687_3).and(key687_4).and(key687_5).and(key687_6).and(key687_7).and(key687_8)
			MSF687Rec msf687rec_1 = tools.edoi.firstRow(query687_1)
			if (msf687rec_1 != null){
			MSF687Rec msf687recc = new MSF687Rec()
			MSF687Key msf687keyc = new MSF687Key()
			msf687recc = msf687rec_1
			msf687keyc = msf687recc.getPrimaryKey()
			msf687keyc.setDeprRecType("BS")
			msf687recc.setPrimaryKey(msf687keyc)
			tools.edoi.update(msf687recc)
			log.info("Record pre MSF687-688A updated:" )
			
			}
			}
			}
		}
		}
		}
	}		 
			 						
			 //return null
		  
	//end of onPreSubmit
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit logging.version: ${hookVersion}")
	//	dstrctCode = tools.commarea.District
	//	equipRef = input.getField("EQUIP_REF1I").getValue()
	//	assetTy = input.getField("EQUIP_ASSET_TYPE1I").getValue()
	//	subAsset = input.getField("SUB_ASSET_NO1I").getValue()
		//if (input.getField("EQUIP_REF1I").isProtected() == true){
			Constraint key071_5 = MSF071Key.entityType.equalTo("R11")
			Constraint key071_6 = MSF071Key.entityValue.equalTo(dstrctCode+assetTy+equipRef+subAsset)
			Constraint key071_7 = MSF071Key.refNo.equalTo("001")
			Constraint key071_8 = MSF071Key.seqNum.equalTo("001")
 
			QueryImpl query071_2 = new QueryImpl(MSF071Rec.class).and(key071_5).and(key071_6).and(key071_7).and(key071_8)
 
			MSF071Rec msf071rec_2 = tools.edoi.firstRow(query071_2)
			if (msf071rec_2 != null){
				MSF071Rec msf071recd = new MSF071Rec()
				MSF071Key msf071keyd = new MSF071Key()				
			    msf071recd = msf071rec_2
				msf071keyd = msf071recd.getPrimaryKey()
				retireTy = msf071recd.getRefCode()
				Constraint key685_1 = MSF685Key.dstrctCode.equalTo(dstrctCode)
				Constraint key685_2 = MSF685Key.assetTy.equalTo(assetTy)
				Constraint key685_3 = MSF685Key.assetNo.equalTo(equipRef)
				Constraint key685_4 = MSF685Key.subAssetNo.equalTo(subAsset)
	
				QueryImpl query685 = new QueryImpl(MSF685Rec.class).and(key685_1).and(key685_2).and(key685_3).and(key685_4)
	
				MSF685Rec msf685rec_1 = tools.edoi.firstRow(query685)
				
				if (msf685rec_1 != null){
					 MSF685Rec msf685recb = new MSF685Rec()					 
					 msf685recb = msf685rec_1
					 msf685recb.setRetirementCode(retireTy)
					 tools.edoi.update(msf685recb)
					 log.info("Record post MSF685-688A updated:" )
					 tools.edoi.delete(msf071keyd)
				}
			}else {
			log.info("post depTy2: " + depTy2)
				Constraint key071_1 = MSF071Key.entityType.equalTo("R12")
			Constraint key071_2 = MSF071Key.entityValue.equalTo(dstrctCode+assetTy+equipRef+subAsset+depTy2)
			Constraint key071_3 = MSF071Key.refNo.equalTo("001")
			Constraint key071_4 = MSF071Key.seqNum.equalTo("001")
																												   
			QueryImpl query071_3 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)
																												   
			MSF071Rec msf071rec_3 = tools.edoi.firstRow(query071_3)
																														 
			if (msf071rec_3 != null){
				MSF071Rec msf071recd = new MSF071Rec()
				MSF071Key msf071keyd = new MSF071Key()
				msf071recd = msf071rec_3
				msf071keyd = msf071recd.getPrimaryKey()
				deprMeth = msf071recd.getRefCode()
				log.info("deprMeth : " + deprMeth)
			    //dispDate = retireTy.substring(0,8)
				//dispAmtS = retireTy.substring(8)
				//dispAmt = new BigDecimal(dispAmtS)
				//log.info("dispDate: " + dispDate)
				//log.info("dispAmtS: " + dispAmtS)
				//log.info("dispAmt: " + dispAmt)
				
	     Constraint key686_6 = MSF686Key.dstrctCode.equalTo(dstrctCode)
		Constraint key686_7 = MSF686Key.assetTy.equalTo(assetTy)
		Constraint key686_8 = MSF686Key.assetNo.equalTo(equipRef)
		Constraint key686_9 = MSF686Key.subAssetNo.equalTo(subAsset)
		Constraint key686_10 = MSF686Key.deprRecType.equalTo("BS")
		
		QueryImpl query686_2 = new QueryImpl(MSF686Rec.class).and(key686_6).and(key686_7).and(key686_8).and(key686_9).and(key686_10)

		MSF686Rec msf686rec_2 = tools.edoi.firstRow(query686_2)
		if (msf686rec_2 != null){
			MSF686Rec msf686recc = new MSF686Rec()
			MSF686Key msf686keyc = new MSF686Key()			
			MSF686Key msf686keyd = new MSF686Key()			
			msf686recc = msf686rec_2
			msf686keyc = msf686recc.getPrimaryKey()			
			msf686keyd = msf686keyc
			//msf686recc.setDateRetired(dispDate)
			//msf686recc.setActRetireVal(dispAmt)
			msf686keyc.setDeprRecType(deprMeth)
			msf686recc.setPrimaryKey(msf686keyc)
			tools.edoi.update(msf686recc)
			log.info("Record post MSF686-688A updated:" )
			msf686keyd.setDeprRecType("BS")
			tools.edoi.delete(msf686keyd)
			Constraint key687_1 = MSF687Key.dstrctCode.equalTo(dstrctCode)
			Constraint key687_2 = MSF687Key.assetTy.equalTo(assetTy)
			Constraint key687_3 = MSF687Key.subAssetNo.equalTo(subAsset)
			Constraint key687_4 = MSF687Key.deprRecType.equalTo("BS")
			Constraint key687_5 = MSF687Key.acctProfile.equalTo(acctProf)
			Constraint key687_6 = MSF687Key.seqNo.equalTo("0001")
			Constraint key687_7 = MSF687Key.accountPeriod.equalTo(CurrentPer)
			Constraint key687_8 = MSF687Key.assetNo.equalTo(equipRef)
			QueryImpl query687_1 = new QueryImpl(MSF687Rec.class).and(key687_1).and(key687_2).and(key687_3).and(key687_4).and(key687_5).and(key687_6).and(key687_7).and(key687_8)
			MSF687Rec msf687rec_1 = tools.edoi.firstRow(query687_1)
			if (msf687rec_1 != null){
			MSF687Rec msf687recc = new MSF687Rec()
			MSF687Key msf687keyc = new MSF687Key()
			MSF687Key msf687keyd = new MSF687Key()
			msf687recc = msf687rec_1
			msf687keyc = msf687recc.getPrimaryKey()
			msf687keyd = msf687keyc
			msf687keyc.setDeprRecType(deprMeth)
			msf687recc.setPrimaryKey(msf687keyc)
			tools.edoi.update(msf687recc)
			log.info("Record post MSF687-688A updated:" )
			msf687keyd.setDeprRecType("BS")
			tools.edoi.delete(msf687keyd)
			}
			tools.edoi.delete(msf071keyd)
			
				}
		
			}
			if (depTy2 == "T1"){
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Date nowdate = new Date();
			String dateNow = dateFormat.format(nowdate)
			log.info("now"+ dateNow )
			log.info("now" + nowdate)
			Constraint key68a_1 = MSF68AKey.dstrctCode.equalTo(dstrctCode)
			Constraint key68a_2 = MSF68AKey.assetTy.equalTo(assetTy)
			Constraint key68a_3 = MSF68AKey.subAssetNo.equalTo(subAsset)
			Constraint key68a_4 = MSF68AKey.deprRecType.equalTo(depTy2)
			Constraint key68a_5 = MSF68AKey.acctProfile.equalTo(acctProf)
			Constraint key68a_7 = MSF68AKey.accountPeriod.equalTo(CurrentPer)
			Constraint key68a_8 = MSF68AKey.assetNo.equalTo(equipRef)
			Constraint key68a_9 = MSF68AKey.processDate.equalTo(dateNow)
			QueryImpl query68A = new QueryImpl(MSF68ARec.class).and(key68a_1).and(key68a_2).and(key68a_3).and(key68a_4).and(key68a_5).and(key68a_7).and(key68a_8).and(key68a_9)
			log.info("rek68a1 : ")
			tools.edoi.search(query68A,{MSF68ARec msf68arec ->
				log.info("rek68a2 : ")
				if (msf68arec != null){
					log.info("rek68a : ")
					MSF68ARec msf68arecb = new MSF68ARec()
				MSF68AKey msf68akeyb = new MSF68AKey()				
				msf68akeyb = msf68arec.getPrimaryKey()
				msf68arecb.setPrimaryKey(msf68akeyb)
				tools.edoi.delete(msf68akeyb)
				log.info("record deleted" + msf68akeyb)
				}
				log.info("rek68a3 : ")
				})
			log.info("rek68a4 : ")
			}
			}
	}
		
}
	
 
 

