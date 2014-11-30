package es.unizar.contsem.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import es.unizar.contsem.query.PprocQuery;
import es.unizar.contsem.query.PprocQueryFactory;
import es.unizar.contsem.query.PprocQueryFactoryConfig;
import es.unizar.contsem.query.construct.PprocConstructResult;
import es.unizar.contsem.query.select.PprocSelectResult;

public class Methods {

	private static String[] sparqlEndpoints = { "http://datos.zaragoza.es/sparql"
	// , "http://www.dphuesca.es/sparql"
	};

	/**
	 * @param json_object
	 *            a json object with the faceted query
	 * @return a json object with the queried results
	 * @throws JSONException
	 * @throws XMLStreamException
	 */
	public static String facetQuery(String json_string) {

		// Transforming JSON string to Map<String,String>
		Object obj = JSONValue.parse(json_string);
		JSONObject json = (JSONObject) obj;
		Map<String, String> facetMap = new HashMap<String, String>();
		facetMap.put("object_text", json.get("object_text") == null ? null : json.get("object_text").toString());
		facetMap.put("object_cpv", json.get("object_cpv") == null ? null : json.get("object_cpv").toString());
		facetMap.put("budget", json.get("budget") == null ? null : json.get("budget").toString());
		facetMap.put("tenderState", json.get("tenderState") == null ? null : json.get("tenderState").toString());
		facetMap.put("contractingAuthority",
				json.get("contractingAuthority") == null ? null : json.get("contractingAuthority").toString());
		facetMap.put("supplier_text", json.get("supplier_text") == null ? null : json.get("supplier_text").toString());
		facetMap.put("supplier_cif", json.get("supplier_cif") == null ? null : json.get("supplier_cif").toString());
		facetMap.put("tenderStartDate", json.get("tenderStartDate") == null ? null : json.get("tenderStartDate")
				.toString());
		facetMap.put("tenderNoticeDate", json.get("tenderNoticeDate") == null ? null : json.get("tenderNoticeDate")
				.toString());
		facetMap.put("tenderDeadline", json.get("tenderDeadline") == null ? null : json.get("tenderDeadline")
				.toString());
		facetMap.put("awardDate", json.get("awardDate") == null ? null : json.get("awardDate").toString());
		facetMap.put("formalizedDate", json.get("formalizedDate") == null ? null : json.get("formalizedDate")
				.toString());
		facetMap.put("procedureType", json.get("procedureType") == null ? null : json.get("procedureType").toString());

		// Actual query
		List<PprocSelectResult> resultList = new ArrayList<PprocSelectResult>();
		for(int i=0; i<sparqlEndpoints.length; i++) {
			PprocQueryFactory fact = new PprocQueryFactory(new PprocQueryFactoryConfig(sparqlEndpoints[i]));
			PprocQuery query = fact.getFacetedQuery(facetMap);
			resultList.add(query.doSelect());
		}
		return PprocSelectResult.asJson(resultList);
	}

	public static String contractQuery(String json_string) {

		// Transforming JSON string to Map<String,String>
		Object obj = JSONValue.parse(json_string);
		JSONObject json = (JSONObject) obj;
		Map<String, String> facetMap = new HashMap<String, String>();
		facetMap.put("contractUri", json.get("contractUri") == null ? null : json.get("contractUri").toString());
		facetMap.put("sparqlEndpoint", json.get("sparqlEndpoint") == null ? null : json.get("sparqlEndpoint").toString());
		
		// Actual query
		List<PprocConstructResult> resultList = new ArrayList<PprocConstructResult>();
		for(int i=0; i<sparqlEndpoints.length; i++) {
			PprocQueryFactory fact = new PprocQueryFactory(new PprocQueryFactoryConfig(sparqlEndpoints[i]));
			PprocQuery query = fact.getContractQuery(facetMap);
			resultList.add(query.doConstruct());
		}
		return PprocConstructResult.asJson(resultList);
	}

}
