/**
 * @EMS 2014
 *
 * Revision History
 * 31-May-2022.............a9ra5213 - Ricky Afriano - PJB UBJOM Merge
 * ........................Hooks to Validate Account Code with MSF940.
 * 13-Oct-2014.............a9ms6435 add condition dueDate > startDate (PN468)
 * 12-Feb-2014.............a9ms6435 Initial Non User Exit- MSM24CA
 * ........................calculate date in Lead Time Field
 * */


import com.mincom.ellipse.ejra.mso.GenericMsoRecord
import com.mincom.ellipse.ejra.mso.MsoErrorMessage
import com.mincom.ellipse.hook.hooks.MSOHook
import com.mincom.ellipse.ejra.mso.MsoField
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.ibm.icu.util.*
import com.ibm.icu.util.GregorianCalendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
//import java.util.GregorianCalendar;
import java.lang.System
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Key
import com.mincom.ellipse.edoi.ejb.msf071.MSF071Rec
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Key
import com.mincom.ellipse.edoi.ejb.msf221.MSF221Rec
import com.mincom.enterpriseservice.ellipse.dependant.dto.WorkOrderDTO
import com.mincom.enterpriseservice.ellipse.workorder.WorkOrderServiceModifyReplyDTO
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import groovy.sql.Sql;
import com.mincom.eql.impl.QueryImpl;
import javax.naming.InitialContext;

public class MSM24CA extends MSOHook {

	String hookVersion = "v02"
	Calendar javaCalendar = null
	String currentDate
	GregorianCalendar bulan
	String answer,answer_odrdte
	String StrMonth, StrDay, DumStrMonth, DumStrDay, stockCode,actCtr, poNo, poItemNo
	String dueDate = "", inputtedDate ="", startDate="", dueDate_post=""
	Integer leadTime
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ReadOnlyDatasource")
	def sql = new Sql(CAISource)

	@Override
	public GenericMsoRecord onDisplay(GenericMsoRecord screen){

		log.info("Hooks onDisplay logging.version: ${hookVersion}")

		String dateStart = screen.getField("ORDER_DATE1I").getValue()

		javaCalendar = Calendar.getInstance();
		DumStrMonth = (javaCalendar.get(Calendar.MONTH) + 1)

		if (DumStrMonth.trim().length() < 2){
			StrMonth = "0" + DumStrMonth
		}else{
			StrMonth = DumStrMonth
		}

		DumStrDay = javaCalendar.get(Calendar.DATE)
		if (DumStrDay.trim().length() < 2){
			StrDay = "0" + DumStrDay
		}else{
			StrDay = DumStrDay
		}

		currentDate = javaCalendar.get(Calendar.YEAR) +""+ StrMonth +""+ StrDay
		log.info("Current date (14102014) :" + currentDate)

		stockCode = screen.getField("STOCK_CODE1I").getValue()
		actCtr = screen.getField("RO_NUM1I").getValue()

		Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
		Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
		Constraint key071_3 = MSF071Key.refNo.equalTo("001")
		Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

		QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)
		MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)
		if (msf071rec_1 != null && !dateStart.trim().equals("")){
			dueDate = msf071rec_1.getRefCode()
			log.info("Due date from MSF071 A:" + dueDate)
			log.info("eckops Compare current date and due date onDisplay")
			log.info("Compare current date and due date onDisplay (14102014)")
			if (dueDate < currentDate){
				if (screen.getField("DUE_DATE1I").isProtected() == false){
					log.info("is False (14102014)")
					screen.getField("DUE_DATE1I").setValue(dueDate)}
				else{
					log.info("is Protected (14102014)")
					screen.getField("DUE_DATE1I").setIsProtected(false)
					screen.getField("DUE_DATE1I").setValue(dueDate)
					screen.getField("DUE_DATE1I").setIsProtected(true)
				}
			}
		}

		// Disable field CONSIGN_WS_IND1I and set value = S
		screen.getField("CONSIGN_WS_IND1I").setValue("S")
		screen.getField("CONSIGN_WS_IND1I").setIsProtected(true)

		// check PO created
		poNo = screen.getField("ORDER_NO1I").getValue()
		poItemNo = screen.getField("ORDER_ITEM_NO1I").getValue()

		if (poItemNo.length() < 3){
			poItemNo = poItemNo.padLeft(3,'0')
		}

		log.info("PO NO:" + poNo)
		log.info("ITEM NO:" + poItemNo)
		log.info("ACTIVITY CTR:" + actCtr)

		if(!poNo.trim().equals("")){
			log.info("browse MSF221")
			log.info("browse MSF221 (14102014)")
			Constraint key221_1 = MSF221Key.poNo.equalTo(poNo)
			Constraint key221_2 = MSF221Key.poItemNo.equalTo(poItemNo)

			QueryImpl query221 = new QueryImpl(MSF221Rec.class).and(key221_1).and(key221_2)

			MSF221Rec msf221rec_1 = tools.edoi.firstRow(query221)
			log.info("due date (14102014):" + dueDate)
			log.info("current date (14102014):" + currentDate)
			if (dueDate == ""  && msf221rec_1 != null )
			{
				dueDate = msf221rec_1.getCurrDueDate()
			}
			// Check inputed date
			Constraint key071_5 = MSF071Key.entityType.equalTo("L22")
			Constraint key071_6 = MSF071Key.entityValue.equalTo(poNo+poItemNo)
			Constraint key071_7 = MSF071Key.refNo.equalTo("001")
			Constraint key071_8 = MSF071Key.seqNum.equalTo("001")

			QueryImpl query071_2 = new QueryImpl(MSF071Rec.class).and(key071_5).and(key071_6).and(key071_7).and(key071_8)

			MSF071Rec msf071rec_2 = tools.edoi.firstRow(query071_2)

			if (msf071rec_2 == null){
				MSF071Rec msf071recc = new MSF071Rec()
				MSF071Key msf071keyc = new MSF071Key()
				//Key Columns1:
				msf071keyc.setEntityType("L22")
				msf071keyc.setEntityValue(poNo+poItemNo)
				msf071keyc.setRefNo("001")
				msf071keyc.setSeqNum("001")
				msf071recc.setPrimaryKey(msf071keyc)
				//Non-Key Columns:
				msf071recc.setRefCode(dueDate)
				tools.edoi.create(msf071recc)
				log.info("Record MSF071-L22 created (14102014):" + dueDate)
			}else{
				MSF071Rec msf071recc = new MSF071Rec()
				msf071recc = msf071rec_2

				//Update
				msf071recc.setRefCode(dueDate)
				tools.edoi.update(msf071recc)
				log.info("Record MSF071-L22 updated (14102014):" + dueDate)
			}
			if (msf221rec_1 != null && dueDate < currentDate){
				log.info("Record MSF221 Found (14102014)")
				MSF221Rec msf221recb = new MSF221Rec()
				msf221recb = msf221rec_1
				//Update
				msf221recb.setOrigDueDate(dueDate)
				msf221recb.setCurrDueDate(dueDate)
				msf221recb.setDueSiteDate(dueDate)
				tools.edoi.update(msf221recb)
				log.info("MSF221 eckops Updated (14102014):" + dueDate)
			}
		}

		return screen
	}//end of OnDisplay

	@Override
	public GenericMsoRecord onPreSubmit(GenericMsoRecord screen){

		log.info("Hooks onPreSubmit logging.version: ${hookVersion}")

		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		//Date nowdate = new Date();

		String dateStart = screen.getField("ORDER_DATE1I").getValue()
		log.info("Hooks onPreSubmit order date from input: ${dateStart}")

		String dateStop = screen.getField("DUE_DATE1I").getValue()
		log.info("Hooks onPreSubmit due date from input: ${dateStop}")

		//dueDate = screen.getField("DUE_DATE1I").getValue()
		//dueDate = screen.getField("DUE_DATE1I").setValue("20140408")

		//add condition order date ="" and due date != "" a9ms6435 - 20141020
		//start

		if (!dateStop.equals("") && dateStart.equals(""))
		{
			log.info("dateStop != kosong dan dateStart = ksosong")
			javaCalendar = Calendar.getInstance();
			DumStrMonth = (javaCalendar.get(Calendar.MONTH) + 1)

			if (DumStrMonth.trim().length() < 2){
				StrMonth = "0" + DumStrMonth
			}else{
				StrMonth = DumStrMonth
			}

			DumStrDay = javaCalendar.get(Calendar.DATE)
			if (DumStrDay.trim().length() < 2){
				StrDay = "0" + DumStrDay
			}else{
				StrDay = DumStrDay
			}

			leadTime = Integer.parseInt(screen.getField("LEAD_TIME1I").getValue())

			DateFormat DOB = new SimpleDateFormat("yyyyMMdd");
			java.sql.Date convertedDate = new java.sql.Date(DOB.parse(dateStop).getTime());
			//java.sql.Date convertedDue = new java.sql.Date(DOB.parse(dateStart).getTime());

			//GregorianCalendar gcal = new GregorianCalendar
			Calendar cal = Calendar.getInstance();
			//Calendar calDue = Calendar.getInstance();
			cal.setTime(convertedDate);
			//calDue.setTime(convertedDue);
			cal.add(Calendar.DATE, -leadTime);
			//calDue.add(Calendar.DATE, leadTime);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String answer_odrdte = sdf.format(cal.getTime());
			//String answer = sdf.format(cal.getTime());

			currentDate = javaCalendar.get(Calendar.YEAR) +""+ StrMonth +""+ StrDay
			log.info("Current date PN468 20141020:" + currentDate)
			log.info("DUE DATE   PN468 20141020:"+ answer_odrdte)
			log.info("lead time  PN468 20141020:"+ leadTime)


			if (answer_odrdte < dateStop){
				screen.getField("ORDER_DATE1I").setValue("")
				log.info("order date < dateStop ")
				screen.getField("ORDER_DATE1I").setValue(answer_odrdte)
				log.info("Order Date PN468 20141020: "+answer_odrdte)

				stockCode = screen.getField("STOCK_CODE1I").getValue()
				actCtr = screen.getField("RO_NUM1I").getValue()

				// Check inputed date
				Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
				Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
				Constraint key071_3 = MSF071Key.refNo.equalTo("001")
				Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

				QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)

				MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)

				if (msf071rec_1 == null){
					log.info("MASUK KONDISI msf071rec_1 == null ")
					MSF071Rec msf071recb = new MSF071Rec()
					MSF071Key msf071keyb = new MSF071Key()
					//Key Columns:
					msf071keyb.setEntityType("24C")
					msf071keyb.setEntityValue(stockCode + actCtr)
					msf071keyb.setRefNo("001")
					msf071keyb.setSeqNum("001")
					msf071recb.setPrimaryKey(msf071keyb)
					//Non-Key Columns:
					msf071recb.setRefCode(dateStop)
					tools.edoi.create(msf071recb)
					log.info("CREATE")
					log.info("Record MSF071-24C created3:" + dateStop)
				}else{
					log.info("MASUK ELSE UPDATE")
					MSF071Rec msf071recb = new MSF071Rec()
					msf071recb = msf071rec_1

					//Update
					msf071recb.setRefCode(dateStop)
					tools.edoi.update(msf071recb)
					log.info("UPDATE")
					log.info("Record MSF071-24C updated3:" + dateStop)
				}
			}
		}
		//end new req


		if (!dateStop.equals("") && dateStop > dateStart)
		{
			dueDate = screen.getField("DUE_DATE1I").getValue()
			startDate = screen.getField("ORDER_DATE1I").getValue()
			log.info("Hooks masuk !dateStop.equals (14102014) ")
			log.info("Due date PreSu (14102014) "+ dueDate)
			// Override Due Date inputted to Current Date
			javaCalendar = Calendar.getInstance();
			DumStrMonth = (javaCalendar.get(Calendar.MONTH) + 1)

			if (DumStrMonth.trim().length() < 2){
				StrMonth = "0" + DumStrMonth
			}else{
				StrMonth = DumStrMonth
			}

			DumStrDay = javaCalendar.get(Calendar.DATE)
			if (DumStrDay.trim().length() < 2){
				StrDay = "0" + DumStrDay
			}else{
				StrDay = DumStrDay
			}

			currentDate = javaCalendar.get(Calendar.YEAR) +""+ StrMonth +""+ StrDay
			log.info("Current date (14102014):" + currentDate)
			log.info("eckops Compare current date and due date PreSubmit (14102014)")
			if (dueDate < currentDate ){
				log.info("Current date less than today PreSubmit (14102014)")
				screen.getField("DUE_DATE1I").setValue(currentDate)
				log.info("Due date PreSu2 (14102014) : "+ dueDate)
				stockCode = screen.getField("STOCK_CODE1I").getValue()
				actCtr = screen.getField("RO_NUM1I").getValue()

				// Check inputed date
				Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
				Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
				Constraint key071_3 = MSF071Key.refNo.equalTo("001")
				Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

				QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)

				MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)

				if (msf071rec_1 == null){
					log.info("masuk msf071rec_1 == null preS (14102014): " + dueDate)
					MSF071Rec msf071recb = new MSF071Rec()
					MSF071Key msf071keyb = new MSF071Key()
					//Key Columns:
					msf071keyb.setEntityType("24C")
					msf071keyb.setEntityValue(stockCode + actCtr)
					msf071keyb.setRefNo("001")
					msf071keyb.setSeqNum("001")
					msf071recb.setPrimaryKey(msf071keyb)
					//Non-Key Columns:
					msf071recb.setRefCode(dueDate)
					tools.edoi.create(msf071recb)
					log.info("Record MSF071-24C created (14102014): " + dueDate)
				}else{
					log.info("Record MSF071-24C updated (14102014): " + dueDate)
					MSF071Rec msf071recb = new MSF071Rec()
					msf071recb = msf071rec_1

					//Update
					msf071recb.setRefCode(dueDate)
					tools.edoi.update(msf071recb)
					log.info("Record MSF071-24C updated:" + dueDate)

				}
			}
			//start PN468
			if (dueDate > startDate && dueDate >= currentDate){
				log.info("== Due date > startDate == 14/10/14 start PN468")
				log.info("startDate 20141020 : " + startDate)
				log.info("Due date 14/10/14 : "+ dueDate)
				stockCode = screen.getField("STOCK_CODE1I").getValue()
				actCtr = screen.getField("RO_NUM1I").getValue()

				// Check inputed date
				Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
				Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
				Constraint key071_3 = MSF071Key.refNo.equalTo("001")
				Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

				QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)

				MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)

				if (msf071rec_1 == null){
					log.info("masuk msf071rec_1 == null preS (14102014): " + dueDate)
					MSF071Rec msf071recb = new MSF071Rec()
					MSF071Key msf071keyb = new MSF071Key()
					//Key Columns:
					msf071keyb.setEntityType("24C")
					msf071keyb.setEntityValue(stockCode + actCtr)
					msf071keyb.setRefNo("001")
					msf071keyb.setSeqNum("001")
					msf071recb.setPrimaryKey(msf071keyb)
					//Non-Key Columns:
					msf071recb.setRefCode(dueDate)
					tools.edoi.create(msf071recb)
					log.info("Record MSF071-24C created_14102014:" + dueDate)
				}else{
					log.info("Record MSF071-24C updated_14102014: " + dueDate)
					MSF071Rec msf071recb = new MSF071Rec()
					msf071recb = msf071rec_1

					//Update
					msf071recb.setRefCode(dueDate)
					tools.edoi.update(msf071recb)
					log.info("Record MSF071-24C updated:" + dueDate)
				}
			}

			//end PN468 a9ms6435


			// Calculate Lead Time : Due Date - Order Date
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd")
			Date d1 = null
			Date d2 = null
			d1 = format.parse(dateStart)
			d2 = format.parse(dateStop)

			//in milliseconds
			long diff = d2.getTime() - d1.getTime()
			log.info("Hooks onPostSubmit diff : ${diff}")
			String diffDays = diff / (24 * 60 * 60 * 1000)
			log.info("Hooks onPreSubmit diffDays : ${diffDays}")
			screen.getField("LEAD_TIME1I").setValue(diffDays)
		}

		//NEW

		if (dateStop.equals("") && !dateStart.equals(""))
		{
			log.info("MASUK KONDISI dateStop.equals //new")
			//String answer = "20140408"
			// Override Due Date inputted to Current Date
			javaCalendar = Calendar.getInstance();
			DumStrMonth = (javaCalendar.get(Calendar.MONTH) + 1)

			if (DumStrMonth.trim().length() < 2){
				StrMonth = "0" + DumStrMonth
			}else{
				StrMonth = DumStrMonth
			}

			DumStrDay = javaCalendar.get(Calendar.DATE)
			if (DumStrDay.trim().length() < 2){
				StrDay = "0" + DumStrDay
			}else{
				StrDay = DumStrDay
			}

			leadTime = Integer.parseInt(screen.getField("LEAD_TIME1I").getValue())
			//
			//GregorianCalendar bulan
			//String[] namaBulan =  ["JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"]
			//bulan=namaBulan[4-1]

			//GregorianCalendar gcal = new GregorianCalendar(dateStart.substring(0, 4), dateStart.substring(5, 6), dateStart.substring(7, 8));
			log.info("dateStart sblm gcal:"+ dateStart)
			log.info("lead time  PN468 sebelum gcal:"+ leadTime)
			log.info("Tahun :"+ leadTime)
			String tahun = dateStart.substring(0, 4);
			String bulan = dateStart.substring(4, 6);
			String tanggal = dateStart.substring(6, 8);
			log.info("Tahun :"+ tahun)
			log.info("Bulan :"+ bulan)
			log.info("Tanggal :"+ tanggal)
			//GregorianCalendar gcal = new GregorianCalendar(dateStart.substring(0, 4).toInteger(), dateStart.substring(4, 6).toInteger(), dateStart.substring(6, 8).toInteger())
			//GregorianCalendar gcal = new GregorianCalendar(2014,10,15)
			DateFormat DOB = new SimpleDateFormat("yyyyMMdd");
			java.sql.Date convertedDate = new java.sql.Date(DOB.parse(dateStart).getTime());

			//GregorianCalendar gcal = new GregorianCalendar
			Calendar cal = Calendar.getInstance();
			cal.setTime(convertedDate);
			cal.add(Calendar.DATE, leadTime);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String answer = sdf.format(cal.getTime());

			currentDate = javaCalendar.get(Calendar.YEAR) +""+ StrMonth +""+ StrDay
			log.info("Current date PN468 :" + currentDate)
			log.info("Compare current date and due date PreSubmit PN468")
			log.info("DUE DATE 2 PN468 :"+ answer)
			log.info("lead time  PN468 :"+ leadTime)


			if (answer < currentDate){
				screen.getField("DUE_DATE1I").setValue("")
				log.info("MASUK KONDISI < currentDate ")
				screen.getField("DUE_DATE1I").setValue(currentDate)
				log.info("Due Date PN468: "+answer)

				stockCode = screen.getField("STOCK_CODE1I").getValue()
				actCtr = screen.getField("RO_NUM1I").getValue()

				// Check inputed date
				Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
				Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
				Constraint key071_3 = MSF071Key.refNo.equalTo("001")
				Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

				QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)

				MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)

				if (msf071rec_1 == null){
					log.info("MASUK KONDISI msf071rec_1 == null ")
					MSF071Rec msf071recb = new MSF071Rec()
					MSF071Key msf071keyb = new MSF071Key()
					//Key Columns:
					msf071keyb.setEntityType("24C")
					msf071keyb.setEntityValue(stockCode + actCtr)
					msf071keyb.setRefNo("001")
					msf071keyb.setSeqNum("001")
					msf071recb.setPrimaryKey(msf071keyb)
					//Non-Key Columns:
					msf071recb.setRefCode(answer)
					tools.edoi.create(msf071recb)
					log.info("CREATE")
					log.info("Record MSF071-24C created3:" + answer)
				}else{
					log.info("MASUK ELSE UPDATE")
					MSF071Rec msf071recb = new MSF071Rec()
					msf071recb = msf071rec_1

					//Update
					msf071recb.setRefCode(answer)
					tools.edoi.update(msf071recb)
					log.info("UPDATE")
					log.info("Record MSF071-24C updated3:" + answer)
				}
			}
			//start add new 2
			if (answer >= currentDate){
				screen.getField("DUE_DATE1I").setValue("")
				log.info("MASUK KONDISI > ")
				screen.getField("DUE_DATE1I").setValue(answer)
				log.info("Due Date: "+answer)

				stockCode = screen.getField("STOCK_CODE1I").getValue()
				actCtr = screen.getField("RO_NUM1I").getValue()

				// Check inputed date
				Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
				Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
				Constraint key071_3 = MSF071Key.refNo.equalTo("001")
				Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

				QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)

				MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)

				if (msf071rec_1 == null){
					log.info("MASUK KONDISI msf071rec_1 == null PN468")
					MSF071Rec msf071recb = new MSF071Rec()
					MSF071Key msf071keyb = new MSF071Key()
					//Key Columns:
					msf071keyb.setEntityType("24C")
					msf071keyb.setEntityValue(stockCode + actCtr)
					msf071keyb.setRefNo("001")
					msf071keyb.setSeqNum("001")
					msf071recb.setPrimaryKey(msf071keyb)
					//Non-Key Columns:
					msf071recb.setRefCode(answer)
					tools.edoi.create(msf071recb)
					log.info("CREATE")
					log.info("Record MSF071-24C created3 PN468:" + answer)
				}else{
					log.info("MASUK ELSE UPDATE PN468")
					MSF071Rec msf071recb = new MSF071Rec()
					msf071recb = msf071rec_1

					//Update
					msf071recb.setRefCode(answer)
					tools.edoi.update(msf071recb)
					log.info("UPDATE PN468")
					log.info("Record MSF071-24C updated3 PN468:" + answer)
				}
			}

			// end add new 2

			/*// Calculate Lead Time : Due Date - Order Date
			 SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd")
			 Date d1 = null
			 Date d2 = null
			 d1 = format.parse(dateStart)
			 d2 = format.parse(dateStop)
			 //in milliseconds
			 long diff = d2.getTime() - d1.getTime()
			 log.info("Hooks onPostSubmit diff : ${diff}")
			 String diffDays = diff / (24 * 60 * 60 * 1000)
			 log.info("Hooks onPreSubmit diffDays : ${diffDays}")
			 screen.getField("LEAD_TIME1I").setValue(diffDays)*/
		}//END NEW

		//add condition order date ="" and due date != "" a9ms6435 - 20141020
		//start
		if (!dateStop.equals("") && dateStart.equals(""))
		{
			log.info("MASUK KONDISI dateStop.equals //new")
			javaCalendar = Calendar.getInstance();
			DumStrMonth = (javaCalendar.get(Calendar.MONTH) + 1)

			if (DumStrMonth.trim().length() < 2){
				StrMonth = "0" + DumStrMonth
			}else{
				StrMonth = DumStrMonth
			}

			DumStrDay = javaCalendar.get(Calendar.DATE)
			if (DumStrDay.trim().length() < 2){
				StrDay = "0" + DumStrDay
			}else{
				StrDay = DumStrDay
			}

			leadTime = Integer.parseInt(screen.getField("LEAD_TIME1I").getValue())
			log.info("dateStart 20102014 :"+ dateStart)
			log.info("lead time  PN468 20102014 :"+ leadTime)
			String tahun = dateStart.substring(0, 4);
			String bulan = dateStart.substring(4, 6);
			String tanggal = dateStart.substring(6, 8);
			log.info("Tahun :"+ tahun)
			log.info("Bulan :"+ bulan)
			log.info("Tanggal :"+ tanggal)


			DateFormat DOB = new SimpleDateFormat("yyyyMMdd");
			java.sql.Date convertedDate = new java.sql.Date(DOB.parse(dateStop).getTime());
			java.sql.Date convertedDue = new java.sql.Date(DOB.parse(dateStart).getTime());

			//GregorianCalendar gcal = new GregorianCalendar
			Calendar cal = Calendar.getInstance();
			Calendar calDue = Calendar.getInstance();
			cal.setTime(convertedDate);
			calDue.setTime(convertedDue);
			cal.add(Calendar.DATE, leadTime);
			calDue.add(Calendar.DATE, leadTime);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String answer_odrdte = sdf.format(cal.getTime());
			String answer = sdf.format(cal.getTime());

			currentDate = javaCalendar.get(Calendar.YEAR) +""+ StrMonth +""+ StrDay
			log.info("Current date PN468 :" + currentDate)
			log.info("Compare current date and due date PreSubmit PN468")
			log.info("DUE DATE 2 PN468 :"+ answer_odrdte)
			log.info("lead time  PN468 :"+ leadTime)


			if (answer_odrdte < dateStop){
				screen.getField("ORDER_DATE1I").setValue("")
				log.info("MASUK KONDISI < dateStop ")
				screen.getField("ORDER_DATE1I").setValue(answer_odrdte)
				log.info("Order Date PN468: "+answer_odrdte)

				stockCode = screen.getField("STOCK_CODE1I").getValue()
				actCtr = screen.getField("RO_NUM1I").getValue()

				// Check inputed date
				Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
				Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
				Constraint key071_3 = MSF071Key.refNo.equalTo("001")
				Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

				QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)

				MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)

				if (msf071rec_1 == null){
					log.info("MASUK KONDISI msf071rec_1 == null ")
					MSF071Rec msf071recb = new MSF071Rec()
					MSF071Key msf071keyb = new MSF071Key()
					//Key Columns:
					msf071keyb.setEntityType("24C")
					msf071keyb.setEntityValue(stockCode + actCtr)
					msf071keyb.setRefNo("001")
					msf071keyb.setSeqNum("001")
					msf071recb.setPrimaryKey(msf071keyb)
					//Non-Key Columns:
					msf071recb.setRefCode(answer)
					tools.edoi.create(msf071recb)
					log.info("CREATE")
					log.info("Record MSF071-24C created3:" + answer)
				}else{
					log.info("MASUK ELSE UPDATE")
					MSF071Rec msf071recb = new MSF071Rec()
					msf071recb = msf071rec_1

					//Update
					msf071recb.setRefCode(answer)
					tools.edoi.update(msf071recb)
					log.info("UPDATE")
					log.info("Record MSF071-24C updated3:" + answer)
				}
			}
		}
		//end new req
		log.info("screen.getNextAction() : " + screen.getNextAction());
		if (((screen.getNextAction() == 1) || (screen.getNextAction() == 0))) {
			String DST1 = tools.commarea.District.trim();
			String ACT1 = screen.getField("PROCESS_CODE1I").getValue();
			String STK1 = screen.getField("STOCK_CODE1I").getValue();
			String RO1 = screen.getField("RO_NUM1I").getValue();
			String invCat = screen.getField("INVENT_CAT1I").getValue();
			log.info("invCat : " + invCat);
			def qry = sql.firstRow("select lpad('"+RO1+"',3,'0') RO_NO from dual");
			RO1 = qry.RO_NO;

			if (ACT1.trim().equals("L")) {
				String qyrSetting = "select a.ACTIVITY_CTR,a.DSTRCT_CODE,a.STOCK_CODE,a.INVENT_CAT,b.CATEG_MGT_FLG,b.CAT_COST_SW " +
						"from msf240 a left outer join msf170 b on (a.DSTRCT_CODE = b.DSTRCT_CODE and a.STOCK_CODE = b.STOCK_CODE) " +
						"where a.ACTIVITY_CTR = '"+RO1+"' and a.DSTRCT_CODE = '"+DST1+"' and a.STOCK_CODE = '"+STK1+"'";
				qry = sql.firstRow(qyrSetting);
				if (!qry.equals(null)){
					if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && (qry.INVENT_CAT.trim().equals("") || invCat.trim().equals(""))){
						MsoField errField = new MsoField();
						errField.setName("INVENT_CAT1I");
						errField.setCurrentCursor(true);
						screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
								"INVENTORY CATEGORY REQUIRED !",
								MsoErrorMessage.ERR_TYPE_ERROR,
								MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
						screen.setCurrentCursorField(errField)
						return screen;
					}
				}else {
					qyrSetting = "select * from msf170 b where b.DSTRCT_CODE = '"+DST1+"' and b.STOCK_CODE = '"+STK1+"'"
					qry = sql.firstRow(qyrSetting);
					if (!qry.equals(null)){
						if (qry.CATEG_MGT_FLG.equals("Y") && qry.CAT_COST_SW.equals("Y") && (invCat.trim().equals(""))){
							MsoField errField = new MsoField();
							errField.setName("INVENT_CAT1I");
							errField.setCurrentCursor(true);
							screen.setErrorMessage(new MsoErrorMessage("INVENTORY CATEGORY REQUIRED !","99999",
									"INVENTORY CATEGORY REQUIRED !",
									MsoErrorMessage.ERR_TYPE_ERROR,
									MsoErrorMessage.ERR_SEVERITY_UNSPECIFIED));
							screen.setCurrentCursorField(errField)
							return screen;
						}
					}
				}
			}
		}
		return null
	}//end of onPreSubmit

	@Override
	public GenericMsoRecord onPostSubmit(GenericMsoRecord input, GenericMsoRecord result) {
		log.info("Hooks onPostSubmit logging.version: ${hookVersion}")


		javaCalendar = Calendar.getInstance();
		DumStrMonth = (javaCalendar.get(Calendar.MONTH) + 1)

		if (DumStrMonth.trim().length() < 2){
			StrMonth = "0" + DumStrMonth
		}else{
			StrMonth = DumStrMonth
		}

		DumStrDay = javaCalendar.get(Calendar.DATE)
		if (DumStrDay.trim().length() < 2){
			StrDay = "0" + DumStrDay
		}else{
			StrDay = DumStrDay
		}

		currentDate = javaCalendar.get(Calendar.YEAR) +""+ StrMonth +""+ StrDay
		log.info("Current date post:" + currentDate)

		stockCode = input.getField("STOCK_CODE1I").getValue()
		actCtr = input.getField("RO_NUM1I").getValue()

		Constraint key071_1 = MSF071Key.entityType.equalTo("24C")
		Constraint key071_2 = MSF071Key.entityValue.equalTo(stockCode + actCtr)
		Constraint key071_3 = MSF071Key.refNo.equalTo("001")
		Constraint key071_4 = MSF071Key.seqNum.equalTo("001")

		QueryImpl query071 = new QueryImpl(MSF071Rec.class).and(key071_1).and(key071_2).and(key071_3).and(key071_4)
		MSF071Rec msf071rec_1 = tools.edoi.firstRow(query071)
		if (msf071rec_1 != null){
			dueDate = msf071rec_1.getRefCode()
			dueDate_post = input.getField("DUE_DATE1I").getValue()
			log.info("Due date from MSF071 A post:" + dueDate)
			log.info("eckops Compare current date and due date onDisplay POST")
			if (dueDate < currentDate ){
				if (input.getField("DUE_DATE1I").isProtected() == false){
					log.info("is False POST")
					input.getField("DUE_DATE1I").setValue(dueDate)}
				else{
					log.info("is Protected POST")
					input.getField("DUE_DATE1I").setIsProtected(false)
					input.getField("DUE_DATE1I").setValue(dueDate)
					input.getField("DUE_DATE1I").setIsProtected(true)
				}
			}
			/*if (dueDate_post > startDate){
			 if (input.getField("DUE_DATE1I").isProtected() == false){
			 log.info("is False")
			 input.getField("DUE_DATE1I").setValue(dueDate_post)}
			 else{
			 log.info("is Protected")
			 input.getField("DUE_DATE1I").setIsProtected(false)
			 input.getField("DUE_DATE1I").setValue(dueDate_post)
			 input.getField("DUE_DATE1I").setIsProtected(true)
			 }
			 }*/
		}

		// Disable field CONSIGN_WS_IND1I and set value = S
		input.getField("CONSIGN_WS_IND1I").setValue("S")
		input.getField("CONSIGN_WS_IND1I").setIsProtected(true)

		// check PO created
		poNo = input.getField("ORDER_NO1I").getValue()
		poItemNo = input.getField("ORDER_ITEM_NO1I").getValue()

		if (poItemNo.length() < 3){
			poItemNo = poItemNo.padLeft(3,'0')
		}

		log.info("PO NO:" + poNo)
		log.info("ITEM NO:" + poItemNo)
		log.info("ACTIVITY CTR:" + actCtr)

		if(!poNo.trim().equals("")){
			log.info("browse MSF221 post")
			Constraint key221_1 = MSF221Key.poNo.equalTo(poNo)
			Constraint key221_2 = MSF221Key.poItemNo.equalTo(poItemNo)

			QueryImpl query221 = new QueryImpl(MSF221Rec.class).and(key221_1).and(key221_2)

			MSF221Rec msf221rec_1 = tools.edoi.firstRow(query221)
			log.info("due date post:" + dueDate)
			log.info("current date post:" + currentDate)

			// Check inputed date
			Constraint key071_5 = MSF071Key.entityType.equalTo("L22")
			Constraint key071_6 = MSF071Key.entityValue.equalTo(poNo+poItemNo)
			Constraint key071_7 = MSF071Key.refNo.equalTo("001")
			Constraint key071_8 = MSF071Key.seqNum.equalTo("001")

			QueryImpl query071_2 = new QueryImpl(MSF071Rec.class).and(key071_5).and(key071_6).and(key071_7).and(key071_8)

			MSF071Rec msf071rec_2 = tools.edoi.firstRow(query071_2)

			if (msf071rec_2 == null){
				MSF071Rec msf071recc = new MSF071Rec()
				MSF071Key msf071keyc = new MSF071Key()
				//Key Columns1:
				msf071keyc.setEntityType("L22")
				msf071keyc.setEntityValue(poNo+poItemNo)
				msf071keyc.setRefNo("001")
				msf071keyc.setSeqNum("001")
				msf071recc.setPrimaryKey(msf071keyc)
				//Non-Key Columns:
				msf071recc.setRefCode(dueDate)
				tools.edoi.create(msf071recc)
				log.info("Record MSF071-L22 created post:" + dueDate)
			}else{
				MSF071Rec msf071recc = new MSF071Rec()
				msf071recc = msf071rec_2

				//Update
				msf071recc.setRefCode(dueDate)
				tools.edoi.update(msf071recc)
				log.info("Record MSF071-L22 updated post:" + dueDate)
			}
			if (msf221rec_1 != null && dueDate < currentDate){
				log.info("Record MSF221 Found post")
				MSF221Rec msf221recb = new MSF221Rec()
				msf221recb = msf221rec_1
				//Update
				msf221recb.setOrigDueDate(dueDate)
				msf221recb.setCurrDueDate(dueDate)
				msf221recb.setDueSiteDate(dueDate)
				tools.edoi.update(msf221recb)
				log.info("MSF221 eckops Updated post:" + dueDate)
			}
		}

		return result
	}//end of onPostSubmit

}
