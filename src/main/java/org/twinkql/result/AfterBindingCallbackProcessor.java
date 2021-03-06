/*
 * Copyright: (c) 2004-2012 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twinkql.result;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.twinkql.instance.BeanInstantiator;
import org.twinkql.result.callback.AfterResultBinding;
import org.twinkql.result.callback.CallbackContext;


/**
 * The Class AfterBindingCallbackProcessor.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class AfterBindingCallbackProcessor {

	@Resource
	private BeanInstantiator beanInstantiator;
	
	/**
	 * Instantiates a new after binding callback processor.
	 */
	public AfterBindingCallbackProcessor(){
		super();
	}
	
	/**
	 * Instantiates a new after binding callback processor.
	 *
	 * @param beanInstantiator the bean instantiator
	 */
	public AfterBindingCallbackProcessor(BeanInstantiator beanInstantiator){
		super();
		this.beanInstantiator = beanInstantiator;
	}
	
	/**
	 * Process.
	 *
	 * @param callbackClass the callback class
	 * @param resultObject the result object
	 * @param context the context
	 */
	public void process(String callbackClass, Object resultObject, CallbackContext context) {
		
		if(StringUtils.isNotBlank(callbackClass) &&
				resultObject != null){
			@SuppressWarnings("unchecked")
			AfterResultBinding<Object> callback = 
				this.beanInstantiator.instantiate(callbackClass, AfterResultBinding.class, false);
			
			callback.afterBinding(resultObject, context);
		}
	}
}
