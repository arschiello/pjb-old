package pjbUpgradeE9.hooks;
/**
* @EMS 2019
*
* Revision History
* 10-Okt-2020.............a9ra5213 - PJB-AR - Add New Screen CSE56R
* 10-Okt-2020.............a9ra5213 - PJB-AR - Add New Screen CSE570
* 10-Okt-2020.............a9ra5213 - PJB-AR - Add New Screen CSE56H
* 10-Okt-2020.............a9ra5213 - PJB-AR - Add New Screen CSE560
* 06-Jul-2019.............a9ra5213 - Initial code
* * */

import groovy.sql.Sql
import javax.naming.InitialContext
import com.mincom.ellipse.attribute.Attribute
import com.mincom.ellipse.hook.hooks.ServiceHook
import org.slf4j.LoggerFactory;
import com.mincom.ellipse.script.util.EDOIWrapper;
import com.mincom.enterpriseservice.exception.*
import com.mincom.enterpriseservice.ellipse.*
import com.mincom.ellipse.script.util.*
import com.mincom.eql.impl.*
import com.mincom.eql.*
import com.mincom.batch.environment.*;
import com.mincom.ellipse.client.connection.*
import com.mincom.enterpriseservice.exception.EnterpriseServiceOperationException
import com.mincom.ellipse.script.util.CommAreaScriptWrapper
import com.mincom.ellipse.service.m1000.genericscript.GenericScriptService
import com.mincom.ellipse.types.m1000.instances.GenericScriptDTO
import com.mincom.ellipse.types.m1000.instances.ScriptName

class GenericScriptService_delete extends ServiceHook{

	String hookVersion = "1"
	
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPreExecute(Object input) {
		log.info("Hooks onPreExecute logging.version: ${hookVersion}")
		Boolean readAction;
		readAction = false;
		String scrptName = "";
		
		GenericScriptDTO inp = (GenericScriptDTO) input
		
		String scName = inp.getScriptName().value;
		List<Attribute> custAttribs = inp.getCustomAttributes()
		custAttribs.each{Attribute customAttribute ->
			log.info ("attrName : " + customAttribute.getName());
			log.info ("attrValue : " + customAttribute.getValue());
			if ((customAttribute.getName().equals("sFcType") || customAttribute.getName().equals("fcType")) && (customAttribute.getValue().equals("STD") || customAttribute.getValue().equals("CMB"))) {
				readAction = true;
				scrptName = "cse6a1Detail";
			}
			if (customAttribute.getName().equals("scrName") && customAttribute.getValue().equals("cse6asDetail")) {
				readAction = true;
				scrptName = "cse6asDetail";
			}
			if ((customAttribute.getName().equals("scrName") && customAttribute.getValue().equals("Cse560Detail"))) {
				scrptName = "Cse560Detail";
				ScriptName seScName = new ScriptName();
				seScName.setValue(scrptName);
				inp.setScriptName(seScName);
			}
			if ((customAttribute.getName().equals("scrName") && customAttribute.getValue().equals("Cse56hDetail"))) {
				scrptName = "Cse56hDetail";
				ScriptName seScName = new ScriptName();
				seScName.setValue(scrptName);
				inp.setScriptName(seScName);
			}
			if ((customAttribute.getName().equals("scrName") && customAttribute.getValue().equals("Cse570Detail"))) {
				scrptName = "Cse570Detail";
				ScriptName seScName = new ScriptName();
				seScName.setValue(scrptName);
				inp.setScriptName(seScName);
			}
			if ((customAttribute.getName().equals("scrName") && customAttribute.getValue().equals("Cse56rDetail"))) {
				scrptName = "Cse56rDetail";
				ScriptName seScName = new ScriptName();
				seScName.setValue(scrptName);
				inp.setScriptName(seScName);
			}
		}
		if ((scName.equals(null) || scName.equals("")) && (scrptName.equals("cse6a1Detail") || scrptName.equals("cse6asDetail")) ) {
			ScriptName seScName = new ScriptName();
			seScName.setValue(scrptName);
			inp.setScriptName(seScName);
		}
		log.info("Script Name : " + inp.getScriptName().value);
		return null
	}
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks onPostExecute logging.version: ${hookVersion}")
		return result
	}
	
}