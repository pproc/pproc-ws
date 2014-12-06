package es.unizar.contsem.methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import es.unizar.contsem.Log;
import es.unizar.contsem.query.PprocQuery;
import es.unizar.contsem.query.PprocQueryFactory;
import es.unizar.contsem.query.PprocQueryFactoryConfig;
import es.unizar.contsem.query.construct.PprocConstructResult;
import es.unizar.contsem.query.select.PprocSelectResult;

public class Methods {

	private static Map<String, String> sparqlEndpoints;

	static {

		try {
			sparqlEndpoints = getSparqlEndpoints("http://contsem.unizar.es/docs/sparqlEndpoints.json");
		} catch (IOException e) {
			Log.error(Methods.class, "error retrieving sparql endpoints from web");
			sparqlEndpoints = new HashMap<String, String>();
			sparqlEndpoints.put("http://datos.zaragoza.es/sparql", "Ayuntamiento de Zaragoza");
			sparqlEndpoints.put("http://www.dphuesca.es/sparql", "Diputacion Provincial de Huesca");
		}

		for (String key : sparqlEndpoints.keySet()) {
			Log.info(Methods.class, key + " - " + sparqlEndpoints.get(key));
		}

	}

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
		for (String sparqlEndpoint : sparqlEndpoints.keySet()) {
			PprocQueryFactory fact = new PprocQueryFactory(new PprocQueryFactoryConfig(sparqlEndpoint));
			PprocQuery query = fact.getFacetedQuery(facetMap);
			resultList.add(query.doSelect());
		}
		return PprocSelectResult.asJson(resultList);
	}

	public static String contractQuery(String json_string) {

		// Transforming JSON string to JSON object
		Object obj = JSONValue.parse(json_string);
		JSONObject json = (JSONObject) obj;

		// Actual query
		PprocQueryFactory fact = new PprocQueryFactory(new PprocQueryFactoryConfig(json.get("sparqlEndpoint")
				.toString()));
		PprocQuery query = fact.getContractQuery(json.get("contractUri").toString());
		return query.doConstruct().asJsonLD();
	}

	public static Map<String, String> getSparqlEndpoints(String urlToJson) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		InputStream is = new URL(urlToJson).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char) cp);
			}
			String jsonText = sb.toString();
			Object obj = JSONValue.parse(jsonText);
			JSONArray jsonArray = (JSONArray) obj;
			for (Iterator iter = jsonArray.listIterator(); iter.hasNext();) {
				JSONObject json = (JSONObject) iter.next();
				map.put(json.get("sparqlEndpoint").toString(), json.get("name").toString());
			}
		} finally {
			is.close();
		}
		return map;
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

}
