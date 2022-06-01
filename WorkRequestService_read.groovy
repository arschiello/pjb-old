package PJB_DEPLOY_FMFC
/** 
 *  EMS Hooks Script
 *  Copyright � 2014-2016 EMS Paramitra
 *  All rights reserved.
 **********************************************************************************
 *
 *   Service / Screen       : Work Request
 *   Operation / MSO Option : Read
 *      
 *   Description            : Hooks Script.
 *         
 *   Revision History
 *   YYYY/MM/DD
 *   2018/09/06......  19ra5213
 *   ................  Add condition to read Job Codes FMFC
 *  
 *  
 ***********************************************************************************
**/

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
import com.mincom.enterpriseservice.ellipse.workrequest.WorkRequestServiceReadReplyDTO
import com.mincom.enterpriseservice.ellipse.workrequest.WorkRequestServiceReadRequestDTO
import com.mincom.enterpriseservice.ellipse.workrequest.WorkRequestServiceReadReplyCollectionDTO

import groovy.sql.Sql

class WorkRequestService_read extends ServiceHook{
	String hookVersion = "1"
	
	private CommAreaScriptWrapper commarea
	InitialContext initial = new InitialContext()
	Object CAISource = initial.lookup("java:jboss/datasources/ApplicationDatasource")
	def sql = new Sql(CAISource)
	
	@Override
	public Object onPostExecute(Object input, Object result) {
		log.info("Hooks onPostExecute WorkRequestService_read version: ${hookVersion}")
		
		WorkRequestServiceReadReplyDTO c = (WorkRequestServiceReadReplyDTO) result
		Attribute ATT = new Attribute[0];

		
		
		String strREQ_ID = c.getRequestId();
		def QueryRes1 = sql.firstRow("select trim(REF_CODE) REF_CODE from msf071 " +
					"where ENTITY_TYPE = 'SSS' and REF_NO = '001' and SEQ_NUM = '001' and trim(ENTITY_VALUE) = trim('"+tools.commarea.district+strREQ_ID+"PLAN')");
		log.info ("QueryRes1 : " + QueryRes1);
		
		if (!QueryRes1.equals(null)){
			if (!QueryRes1.REF_CODE.equals(null)){
				ATT.setName("W0_PLAN");
				ATT.setValue(QueryRes1.REF_CODE);
				c.setCustomAttributes(ATT);
			}
		}

		return null
	}
}
