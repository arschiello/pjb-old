/**
 * @EMS Nov 201812
 *
 * a9ra5213 - Ricky Afriano - Initial Code - PJB - ELL6A1 Tahap 2
 **/
package FMFC_TAHAP_2

import javax.naming.InitialContext

import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*

import org.slf4j.LoggerFactory;

import com.mincom.batch.environment.*;
import com.mincom.batch.script.*;
import com.mincom.ellipse.script.util.*;
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.ellipse.client.connection.*
import com.mincom.ellipse.ejra.mso.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.script.util.CommAreaScriptWrapper
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadReplyDTO
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadRequestDTO
import com.mincom.enterpriseservice.ellipse.equipment.EquipmentServiceReadReplyCollectionDTO

import groovy.sql.Sql

class EquipmentService_read extends ServiceHook{
	String hookVersion = "1"
	
	private CommAreaScriptWrapper commarea
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks onPostExecute EquipmentService_read version: ${hookVersion}")
		
		EquipmentServiceReadReplyDTO c = (EquipmentServiceReadReplyDTO) result
		
		String DST = c.getDistrictCode();
		String EQP = c.getEquipmentNo();
		Attribute ATT = new Attribute[0];
		def QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					"where ENTITY_TYPE = 'FC6' and REF_NO = '001' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = trim('"+DST+EQP+"')");
		log.info ("QueryRes1 : " + QueryRes1);
		
		if (!QueryRes1.equals(null)){
			if (!QueryRes1.REF_CODE.equals(null)){
				ATT.setName("EQP_FC");
				ATT.setValue(QueryRes1.REF_CODE);
				c.setCustomAttributes(ATT);
			}
		}

		return null
	}
}