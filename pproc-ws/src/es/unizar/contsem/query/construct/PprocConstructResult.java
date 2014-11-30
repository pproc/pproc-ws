package es.unizar.contsem.query.construct;

import java.io.StringWriter;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class PprocConstructResult {

	private Model model;
	private String sparqlEndpoint;

	public PprocConstructResult(Model model, String sparqlEndpoint) {
		this.model = model;
		this.sparqlEndpoint = sparqlEndpoint;
	}

	public Model getModel() {
		return model;
	}

	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	public static String asJson(List<PprocConstructResult> resultList) {
		Model model = ModelFactory.createDefaultModel();
		for (PprocConstructResult result : resultList) {
			model.add(result.getModel());
		}
		StringWriter out = new StringWriter();
		model.write(out, "JSON-LD");
		return out.toString();
	}

}
