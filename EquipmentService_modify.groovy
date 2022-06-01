/**
 * @EMS Nov 201812
 *
 * a9ra5213 - Ricky Afriano - Initial Code - PJB - ELL6A1 Tahap 2
 **/
package FMFC_TAHAP_2

import javax.naming.InitialContext;
import com.mincom.enterpriseservice.ellipse.ErrorMessageDTO
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentService;
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceModifyReplyDTO;
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceModifyRequestDTO;
import com.mincom.ellipse.hook.hooks.ServiceHook;
import groovy.sql.Sql;
import com.mincom.ellipse.attribute.Attribute;
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException;

class EquipmentService_modify extends ServiceHook{
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	String hookVersion = "1"
	String strCrDT = "";
	String strCrTM = "";
	String StrDT = "";
	String StrMT = "";
	String StrYR = "";
	String StrHH = "";
	String StrMM = "";
	String StrSS = "";
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks onPostExecute EquipmentService_modify version: ${hookVersion}")
		EquipmentServiceModifyReplyDTO c = (EquipmentServiceModifyReplyDTO) result;
		EquipmentServiceModifyRequestDTO i = (EquipmentServiceModifyRequestDTO) input;
		String DST = c.getDistrictCode();
		String EQP = c.getEquipmentNo();
		
		List<Attribute> custAttribs = i.getCustomAttributes()
		custAttribs.each{Attribute customAttribute ->
			log.info ("Attribute Name = ${customAttribute.getName()}")
			if (customAttribute.getName().equals(new String("EQP_FC"))){
				log.info ("Attribute Value = ${customAttribute.getValue()}")
				String FC_CODE = customAttribute.getValue();
				if (FC_CODE.equals(null)){
					FC_CODE = " ";
				}
				//if(!FC_CODE.equals(null)) {
					String StrSQL = ""
					StrSQL = "select * from (select trim(substr(a.ENTITY_VALUE,4,18)) FC_CODE,trim(a.ref_code)||trim(b.ref_code) FC_DESC " +
						"from msf071 a " +
						"left outer join msf071 b on (a.ENTITY_TYPE = b.ENTITY_TYPE and b.ENTITY_VALUE = a.ENTITY_VALUE and b.REF_NO = '000') " +
						"where a.ENTITY_TYPE = 'FC1' and a.REF_NO = '001' and substr(a.ENTITY_VALUE,1,3) = 'STD' " +
						"union all " +
						"select trim(substr(a.ENTITY_VALUE,4,18)) FC_CODE,trim(a.ref_code)||trim(b.ref_code) FC_DESC " +
						"from msf071 a " +
						"left outer join msf071 b on (a.ENTITY_TYPE = b.ENTITY_TYPE and b.ENTITY_VALUE = a.ENTITY_VALUE and b.REF_NO = '000') " +
						"where a.ENTITY_TYPE = 'FC3' and a.REF_NO = '001' and substr(a.ENTITY_VALUE,1,3) = 'CMB') where FC_CODE = '"+FC_CODE+"' order by FC_CODE";
					log.info ("StrSQL : " + StrSQL);
					def QueryRes1 = sql.firstRow(StrSQL);
					log.info ("QueryRes1 : " + QueryRes1);
					
					if (QueryRes1.equals(null) && !FC_CODE.equals(" ")){
						throw new EnterpriseServiceOperationException(
						new ErrorMessageDTO(
						"9999", "IVALID FAILURE CLASS", "", 0, 0))
						return input;
					}else {
						try
						{
							String QueryDel = "delete msf071 where ENTITY_TYPE = 'FC6' and REF_NO = '001' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = trim('"+DST+EQP+"')";
							sql.execute(QueryDel);
							GetNowDateTime();
							String QueryInsert = (
								"Insert into MSF071 (ENTITY_TYPE,ENTITY_VALUE,REF_NO,SEQ_NUM,LAST_MOD_DATE,LAST_MOD_TIME,LAST_MOD_USER,REF_CODE,STD_TXT_KEY) values ('FC6','"+DST+EQP+"','001','001','"+strCrDT+"','"+strCrTM+"','"+tools.commarea.userId+"','"+FC_CODE+"','            ')");
							sql.execute(QueryInsert);
						} catch (Exception  e) {
							log.info ("Exception is : " + e);
								throw new EnterpriseServiceOperationException(
							new ErrorMessageDTO(
							"9999", "EXCEPTION IN QUERY INSERT", "", 0, 0))
							return input;
						}
					}
				}
			//}
		}
		return result;
	}
	public def GetNowDateTime() {
		Date InPer = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(InPer);
		int iyear = cal.get(Calendar.YEAR);
		int imonth = cal.get(Calendar.MONTH);
		int iday = cal.get(Calendar.DAY_OF_MONTH);
		int iHH = cal.get(Calendar.HOUR_OF_DAY);
		int iMM = cal.get(Calendar.MINUTE);
		int iSS = cal.get(Calendar.SECOND);
		
		if (iday.toString().trim().length() < 2){
			StrDT = "0" + iday.toString().trim()
		}else{
			StrDT = iday.toString().trim()
		}
		
		"(imonth + 1) untuk membuat bulan sesuai"
		if ((imonth + 1).toString().trim().length() < 2){
			StrMT = "0" + (imonth + 1).toString().trim()
		}else{
			StrMT = (imonth + 1).toString().trim()
		}
		
		if (iyear.toString().trim().length() < 3){
			StrYR = "20" + iyear.toString().trim()
		}else{
			StrYR = iyear.toString().trim()
		}
		
		strCrDT = StrYR + StrMT + StrDT
		
		if (iHH.toString().trim().length() < 2){
			StrHH = "0" + iHH.toString().trim()
		}else{
			StrHH = iHH.toString().trim()
		}
		
		if (iMM.toString().trim().length() < 2){
			StrMM = "0" + iMM.toString().trim()
		}else{
			StrMM = iMM.toString().trim()
		}
		
		if (iSS.toString().trim().length() < 2){
			StrSS = "0" + iSS.toString().trim()
		}else{
			StrSS = iSS.toString().trim()
		}
		
		strCrTM = StrHH + StrMM + StrSS
	}
}