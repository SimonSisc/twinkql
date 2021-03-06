package org.twinkql.result;

import static org.junit.Assert.*
import static org.easymock.EasyMock.*

import org.junit.Test
import org.twinkql.result.ResultBindingProcessor;
import org.twinkql.result.ResultMapNotFoundException;

import com.hp.hpl.jena.query.QuerySolution
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.RDFNode
import com.hp.hpl.jena.rdf.model.ResourceFactory



import org.twinkql.context.Qname
import org.twinkql.context.TwinkqlContext
import org.twinkql.instance.DefaultClassForNameInstantiator
import org.twinkql.model.NamedResultMap
import org.twinkql.model.ResultMapChoice
import org.twinkql.model.ResultMapChoiceItem
import org.twinkql.model.RowMap
import org.twinkql.model.SparqlMap
import org.twinkql.model.SparqlMapItem
import org.twinkql.model.types.BindingPart
import org.twinkql.result.callback.AfterResultBinding
import org.twinkql.result.callback.CallbackContext



class ResultBindingProcessorTest {

	@Test
	void testInitCaches(){
		def result1 = new NamedResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId1",
			extends:"ns:resultId2",
			resultMapChoice:
				new ResultMapChoice(
					resultMapChoiceItem:[
						new ResultMapChoiceItem(
							rowMap:
								new RowMap(
								beanProperty: "oneProp",
								var: "o",
								varType: BindingPart.LITERALVALUE
							)
						)
					]
				)
		);
	
		def result2 = new NamedResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId2",
			resultMapChoice:
				new ResultMapChoice(
					resultMapChoiceItem:[
						new ResultMapChoiceItem(
							rowMap:
								new RowMap(
								beanProperty: "twoProp",
								var: "o",
								varType: BindingPart.LITERALVALUE
							)
						)
					]
				)
		);
		
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							resultMap:result1
						),
						new SparqlMapItem(
							resultMap:result2
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		
		def processor = new ResultBindingProcessor(twinkqlContext)
		
		processor.initCaches();
		
		assertNotNull processor.resultMaps.get(Qname.toQname("ns:resultId1"))
		assertNotNull processor.resultMaps.get(Qname.toQname("ns:resultId2"))
		
		assertEquals 2, processor.resultMaps.get(Qname.toQname("ns:resultId1")).resultMapChoice.resultMapChoiceItem.length
		assertEquals 1, processor.resultMaps.get(Qname.toQname("ns:resultId2")).resultMapChoice.resultMapChoiceItem.length
		
	}
 
/*
	@Test
	void testBindForWithNoMatch(){

		ResultSet resultset = createMock(ResultSet)
	
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") },
			isLiteral: { false }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.hasNext()).andReturn(true);
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new ResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId",
			resultMapItem:[
				new ResultMapItem(
					roMap:
						new RowMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
				)
			]	
			
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, null, Qname.toQname("ns:resultId"))

		assertEquals "my value", r.oneProp;
	
	}


	@Test
	void testBindForObjectWithTripleMappingsDefault(){

		ResultSet resultset = createMock(ResultSet)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId",
			compositeResultMapItem:[
				new CompositeResultMapItem(
					tripleMap:
				
						new TripleMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
				)
			]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, null, Qname.toQname("ns:resultId"))

		assertEquals "my value", r.oneProp;
	
	}
	
	@Test
	void testBindForObjectWithTripleMappingsWithList(){

		ResultSet resultset = createMock(ResultSet)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId",
			compositeResultMapItem:[
				new CompositeResultMapItem(
					tripleMap:
						new TripleMap(
							beanProperty: "list[]",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
				)
			]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, null, Qname.toQname("ns:resultId"))

		assertEquals 1, r.list.size()
		assertEquals "my value", r.list.get(0)
	
	}
*/
	@Test
	void testBindForObjectWithTripleMappingsWithNestedCompositeList(){

		ResultSet resultset = createMock(ResultSet)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") },
			isURIResource: { true }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new NamedResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId",
			resultMapChoice:
				new ResultMapChoice(
					resultMapChoiceItem:[
						new ResultMapChoiceItem(
							rowMap: 
								new RowMap(
								beanProperty: "compositeList[].threeProp",
								match: "?p = http://predicateUri",
								var: "o",
								varType: BindingPart.LITERALVALUE
							)
						)
					]
				)
		);
	
		def twinkqlContext = [
			getInstantiators:{ [new DefaultClassForNameInstantiator() ] as Set },
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							resultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bind(resultset, Qname.toQname("ns:resultId"))

		assertEquals 1, r.get(0).compositeList.size()
		assertEquals "my value", r.get(0).compositeList.get(0).threeProp
	
	}
/*	
	@Test
	void testBindForObjectWithTripleMappingsWithListTwoEntries(){

		ResultSet resultset = createMock(ResultSet)
		
		QuerySolution querysolution1 = createMock(QuerySolution)
		
		def predicate1 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution1.get("p")).andReturn(predicate1)
		
		def object1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 1") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution1.get("o")).andReturn(object1)
		
		QuerySolution querysolution2 = createMock(QuerySolution)
		
		def predicate2 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution2.get("p")).andReturn(predicate2)
		
		def object2 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 2") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution2.get("o")).andReturn(object2)
		
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution1)
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution2)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution1, querysolution2)
		
		def result = new CompositeResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId",
			compositeResultMapItem:[
				new CompositeResultMapItem(
					tripleMap: 
						new TripleMap(
							beanProperty: "list[]",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
				)
			]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, null, Qname.toQname("ns:resultId"))

		assertEquals 2, r.list.size()
		assertEquals "my value 1", r.list.get(0)
		assertEquals "my value 2", r.list.get(1)
	
	}
	
	@Test
	void testBindForObjectWithTripleMappingsWithAfterCallback(){

		ResultSet resultset = createMock(ResultSet)
	
		QuerySolution querysolution = createMock(QuerySolution)
		
		def predicate = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri") }
		] as RDFNode
		
		expect(querysolution.get("p")).andReturn(predicate)
		
		def object = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution.get("o")).andReturn(object)
		
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new CompositeResultMap(
			resultClass: "org.twinkql.result.TestResult",
			afterMap:"org.twinkql.result.TestAfterBinding",
			id: "resultId",
			compositeResultMapItem:[
				new CompositeResultMapItem(
					tripleMap:
						new TripleMap(
							beanProperty: "oneProp",
							predicateUri: "http://predicateUri",
							var: "o",
							varType: BindingPart.LITERALVALUE
						)
				)
			]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							compositeResultMap:result
						)
					]
				)
				] as Set
			},
			getInstantiators:{ [ new DefaultClassForNameInstantiator() ] as Set },
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForObject(resultset, null, Qname.toQname("ns:resultId"))

		assertEquals "Modified!!", r.oneProp;
	
	}
	
	*/

	@Test
	void testBindForObjectWithTripleMappingsWithExtends(){
	
		ResultSet resultset = createMock(ResultSet)
		
		QuerySolution querysolution1 = createMock(QuerySolution)
		
		def subject = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://mysubject") },
			isURIResource: { true }
		] as RDFNode
		
		def predicate1 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri1") },
			isURIResource: { true }
		] as RDFNode
		
		expect(querysolution1.get("p")).andReturn(predicate1).anyTimes()
		
		def object1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution1.get("o")).andReturn(object1).anyTimes()
		expect(querysolution1.get("s")).andReturn(subject).anyTimes()
		expect(querysolution1.contains("s")).andReturn(true).anyTimes()
		
		QuerySolution querysolution2 = createMock(QuerySolution)
		
		def predicate2 = [
			asNode: { com.hp.hpl.jena.graph.Node.createURI("http://predicateUri2") },
			isURIResource: { true },
		] as RDFNode
		
		expect(querysolution2.get("p")).andReturn(predicate2).anyTimes()
		
		def object2 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my extended value") },
			isLiteral: { true }
		] as RDFNode
	
		expect(querysolution2.get("o")).andReturn(object2).anyTimes()
		expect(querysolution2.get("s")).andReturn(subject).anyTimes()
		expect(querysolution2.contains("s")).andReturn(true).anyTimes()
		
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution1)
		expect(resultset.hasNext()).andReturn(true)
		expect(resultset.next()).andReturn(querysolution2)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution1, querysolution2)
		
		def result1 = new NamedResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultMap1",
			extends: "resultMap2",
			uniqueResult: "s",
			resultMapChoice:
				new ResultMapChoice(
					resultMapChoiceItem:[
						new ResultMapChoiceItem(
							rowMap: 
								new RowMap(
									beanProperty: "oneProp",
									match: "?p = http://predicateUri1",
									var: "o",
									varType: BindingPart.LITERALVALUE
								)
						)
					]
				)
		);
	
		def result2 = new NamedResultMap(
			id: "resultMap2",
			resultMapChoice:
				new ResultMapChoice(
					resultMapChoiceItem:[
						new ResultMapChoiceItem(
							rowMap:
								new RowMap(
									beanProperty: "twoProp",
									match: "?p = http://predicateUri2",
									var: "o",
									varType: BindingPart.LITERALVALUE
								)
						)
					]
				)
		);
	
		def twinkqlContext = [
			getInstantiators:{ [new DefaultClassForNameInstantiator() ] as Set },
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							resultMap:result1
						),
						new SparqlMapItem(
							resultMap:result2
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bind(resultset, Qname.toQname("ns:resultMap1"))
	
		assertEquals 1, r.size()
		assertEquals "my value", r.get(0).oneProp;
		assertEquals "my extended value", r.get(0).twoProp;
	
	}

	@Test
	void testBindForRows(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new NamedResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId",
			resultMapChoice:
				new ResultMapChoice(
					resultMapChoiceItem:[
						new ResultMapChoiceItem(
							rowMap:
								new RowMap(
									beanProperty:"oneProp",
									var:"var1",
									varType:BindingPart.LITERALVALUE
								)
						)
					]
				)
		);
	
		def twinkqlContext = [
			getInstantiators:{ [new DefaultClassForNameInstantiator() ] as Set },
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							resultMap:result
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bind(resultset, Qname.toQname("ns:resultId"))

		assertEquals 1, r.size()
		assertEquals "my value", r.get(0).oneProp;
	
	}
	
	@Test(expected=ResultMapNotFoundException)
	void testBindInvalidResultSet(){
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bind(null, Qname.toQname("ns:__INVALID__"))

	
	}
	
	@Test
	void testBindForRowsWithAfterCallback(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result = new NamedResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultId",
			afterMap:"org.twinkql.result.TestAfterBinding",
			resultMapChoice:
				new ResultMapChoice(
					resultMapChoiceItem:[
						new ResultMapChoiceItem(
							rowMap:
								new RowMap(
								beanProperty:"oneProp",
								var:"var1",
								varType:BindingPart.LITERALVALUE
							)
						)
					]
				)
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							resultMap:result
						)
					]
				)
				] as Set
			},
			getInstantiators:{ [ new DefaultClassForNameInstantiator() ] as Set },
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bind(resultset, Qname.toQname("ns:resultId"))

		assertEquals 1, r.size()
		assertEquals "Modified!!", r.get(0).oneProp
	}
/*	
	@Test
	void testBindForRowsWithCompositeResultMap(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 1") },
			isLiteral: { true }
		] as RDFNode
	
		def var3 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 3") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		expect(querysolution.get("var3")).andReturn(var3)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result1 = new PerRowResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultMap1",
			perRowResultMapItem: [
				new PerRowResultMapItem(
					rowMap:new RowMap(
								beanProperty:"oneProp",
								var:"var1",
								varType:BindingPart.LITERALVALUE
							)
					)
				],
			resultMapItem: [
				new ResultMapItem(
					nestedResultMap: new NestedResultMap(
						beanProperty:"testResult2",
						resultMap:"ns:resultMap2"
					)
				)
				]
		);
	
		def result2 = new PerRowResultMap(
			resultClass: "org.twinkql.result.TestResult2",
			id: "resultMap2",
			perRowResultMapItem: [
				new PerRowResultMapItem(
					rowMap:new RowMap(
								beanProperty:"threeProp",
								var:"var3",
								varType:BindingPart.LITERALVALUE
							)
					)
				]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							perRowResultMap:result1
						),
						new SparqlMapItem(
							perRowResultMap:result2
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForList(resultset, null, Qname.toQname("ns:resultMap1"))

		assertEquals 1, r.size()
		assertEquals "my value 1", r.get(0).oneProp
		assertNotNull r.get(0).testResult2
		assertEquals "my value 3", r.get(0).testResult2.threeProp
	}
/*	
	@Test
	void testBindForRowsWithDoubleCompositeResultMap(){

		ResultSet resultset = createMock(ResultSet)
		expect(resultset.hasNext()).andReturn(true)
		
		QuerySolution querysolution = createMock(QuerySolution)
		
		def var1 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 1") },
			isLiteral: { true }
		] as RDFNode
	
		def var3 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 3") },
			isLiteral: { true }
		] as RDFNode
	
		def var4 = [
			asLiteral: { ResourceFactory.createPlainLiteral("my value 4") },
			isLiteral: { true }
		] as RDFNode
		
		expect(querysolution.get("var1")).andReturn(var1)
		expect(querysolution.get("var3")).andReturn(var3)
		expect(querysolution.get("var4")).andReturn(var4)
		
		expect(resultset.next()).andReturn(querysolution)
		expect(resultset.hasNext()).andReturn(false)
		
		replay(resultset, querysolution)
		
		def result1 = new PerRowResultMap(
			resultClass: "org.twinkql.result.TestResult",
			id: "resultMap1",
			rowMap:[
					new RowMap(
						beanProperty:"oneProp",
						var:"var1",
						varType:BindingPart.LITERALVALUE
					),
					new RowMap(
						beanProperty:"testResult2",
						resultMapping:"ns:resultMap2"
				)
				]
		);
	
		def result2 = new PerRowResultMap(
			resultClass: "org.twinkql.result.TestResult2",
			id: "resultMap2",
			rowMap:[
					new RowMap(
						beanProperty:"threeProp",
						var:"var3",
						varType:BindingPart.LITERALVALUE
					),
					new RowMap(
						beanProperty:"testResult3",
						resultMapping:"ns:resultMap3"
						)
				]
		);
	
		def result3 = new PerRowResultMap(
			resultClass: "org.twinkql.result.TestResult3",
			id: "resultMap3",
			rowMap:[
					new RowMap(
						beanProperty:"fourProp",
						var:"var4",
						varType:BindingPart.LITERALVALUE
					)
				]
		);
	
		def twinkqlContext = [
			getSparqlMaps:{
				[new SparqlMap(
					namespace:"ns",
					sparqlMapItem: [
						new SparqlMapItem(
							perRowResultMap:result1
						),
						new SparqlMapItem(
							perRowResultMap:result2
						),
						new SparqlMapItem(
							perRowResultMap:result3
						)
					]
				)
				] as Set
			},
			getTwinkqlConfig : {null}
		] as TwinkqlContext
	
		def binding = new ResultBindingProcessor(twinkqlContext)
		
		def r = binding.bindForList(resultset, null, Qname.toQname("ns:resultMap1"))

		assertEquals 1, r.size()
		assertEquals "my value 1", r.get(0).oneProp
		assertNotNull r.get(0).testResult2
		assertEquals "my value 3", r.get(0).testResult2.threeProp
		assertNotNull r.get(0).testResult2.testResult3
		assertEquals "my value 4", r.get(0).testResult2.testResult3.fourProp
	}
*/
}

class TestAfterBinding implements AfterResultBinding {

	public void afterBinding(bindingResult, CallbackContext callbackParams) {
		bindingResult.oneProp = "Modified!!"
	}
	
}

class TestResult {
	def oneProp;
	def twoProp;
	TestResult2 testResult2;
	List<String> list
	List<TestResult2> compositeList
}

class TestResult2 {
	def threeProp;
	TestResult3 testResult3;
}

class TestResult3 {
	def fourProp;
}

class TestTagValue {
	List<TagValue> tagValues = new ArrayList<TagValue>()
}

class TagValue {
	String tag
	String value
}
