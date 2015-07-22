package es.unizar.contsem.test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.JsonLDWriter;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.util.Context;

import es.unizar.contsem.query.PprocQuery;
import es.unizar.contsem.query.PprocQueryFactory;
import es.unizar.contsem.query.construct.PprocConstructResult;
import es.unizar.contsem.query.select.PprocSelectResult;

public class Test {

    public static String json_string = "{" + "\"object_text\" : \"equipamiento piscinas\","
            + "\"object_cps\" : \"43324100\"," + "\"budget\" : \"75000.0 95000.0\","
            + "\"tenderState\" : \"tenderSubmission_phase\"," + "\"contractingAuthority\" : \"zaragoza ayuntamiento\","
            + "\"supplier_text\" : \"Equipamientos ACME\"," + "\"supplier_cif\" : \"A-50105667\","
            + "\"tenderStartDate\" : \"23-12-2014:30-12-2014\"," + "\"tenderNoticeDate\" : \"23-12-2014:30-12-2014\","
            + "\"tenderDeadline\" : \"23-12-2014:30-12-2014\"," + "\"awardDate\" : \"23-12-2014:30-12-2014\","
            + "\"formalizedDate\" : \"23-12-2014:30-12-2014\"," + "\"procedureType\" : \"RegularOpen\"" + "}";

    public static void main(String[] args) {
        test_facetQuery();
        // test_json_decode();
        // test_json_encode();
        // test_contractQuery();
        // test_customQuery();
    }

    public static void test_customQuery() {
        PprocQueryFactory fact = new PprocQueryFactory();
        // fact
    }

    public static void test_facetQuery() {
        PprocQueryFactory fact = new PprocQueryFactory();
        Map<String, String> facetMap = new HashMap<String, String>();
        // facetMap.put("object_text", "agua potable");
        // facetMap.put("object_cpv", "45212290");
        // facetMap.put("budget", "75000.0 95000.0");
        // facetMap.put("tenderState", "execution_phase");
        // facetMap.put("contractingAuthority", "zaragoza ayuntamiento");
        facetMap.put("supplier_text", "urbanco");
        // facetMap.put("supplier_cif", "A-50105667");
        // facetMap.put("tenderStartDate", "2014-11-04 2014-11-04");
        // facetMap.put("tenderNoticeDate", "2014-11-01 2014-11-15");
        // facetMap.put("tenderDeadline", "2014-09-01 2014-11-15");
        // facetMap.put("awardDate", "2014-10-01 2014-11-15");
        // facetMap.put("formalizedDate", "2014-10-02 2014-10-02");
        facetMap.put("procedureType", "RegularOpen");
        PprocQuery query = fact.getFacetedQuery(facetMap);
        System.out.println(query.getSPARQLstring());
        List<PprocSelectResult> resultList = new ArrayList<PprocSelectResult>();
        resultList.add(query.doSelect());
//        String result = PprocSelectResult.asJson(resultList);
        // System.out.println(result);
        System.out.println(resultList.get(0).toString());
        System.out.println("--------------- end");
    }

    public static void test_json_decode() {
        Object obj = JSONValue.parse(json_string);
        JSONObject json = (JSONObject) obj;
        if (json.get("object_text") == null) {
            System.out.println("es null");
        }
        System.out.println(json.get("object_text"));
    }

    private static void test_json_encode() {
        JSONArray array = new JSONArray();
        array.add("we");
    }

    private static void test_contractQuery() {

        PprocQueryFactory fact = new PprocQueryFactory();

        PprocQuery query1 = fact
                .getContractQuery("http://www.zaragoza.es/api/recurso/sector-publico/contrato/0308848-14");
        Model model1 = query1.doConstruct().getModel();
        model1.write(System.out, "JSON-LD");
        // PprocQuery query2 = fact
        // .getContractQuery2("http://www.zaragoza.es/api/recurso/sector-publico/contrato/0308848-14");
        // Model model2 = query2.doConstruct().getModel();
        // Model difference = model1.difference(model2);
        // difference.write(System.out, "JSON-LD");

        Dataset dataset = DatasetFactory.create(model1);
        Context cont = dataset.asDatasetGraph().getContext();
        System.out.println(cont.size());

        JsonLDWriter writer = new JsonLDWriter(RDFFormat.JSONLD_PRETTY);
        Map<String, String> map = new HashMap<String, String>();
        map.put("pproc", "http://contsem.unizar.es/def/sector-publico/pproc#");
        PrefixMap prefixMap = PrefixMapFactory.create(map);
        Context context = new Context();
        writer.write(System.out, dataset.asDatasetGraph(), prefixMap, "", context);

    }
}
