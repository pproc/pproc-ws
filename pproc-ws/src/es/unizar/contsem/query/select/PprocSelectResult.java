package es.unizar.contsem.query.select;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

public class PprocSelectResult {

	private List<String> varNames;
	private List<QuerySolution> results;
	private String sparqlEndpoint;
	private String sparqlQuery;

	public PprocSelectResult(ResultSet rs, String sparqlEndpoint, String sparqlQuery) {
		this.varNames = rs.getResultVars();
		this.results = ResultSetFormatter.toList(rs);
		this.sparqlEndpoint = sparqlEndpoint;
		this.sparqlQuery = sparqlQuery;
	}

	public int size() {
		return results.size();
	}

	public int getColumnCount() {
		return varNames.size();
	}

	public String getSparqlEndpoint() {
		return sparqlEndpoint;
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}

	public Set<String> asSimpleColumn() {
		return new HashSet<String>(this.getColumn(0));
	}

	public List<String> getRow(int row) {
		List<String> returnThis = new ArrayList<String>();
		for (int i = 0; i < varNames.size(); i++) {
			// Condicional proporciona "null" para evitar NullPointExceptio en
			// los casos con clausulas OPTIONAL sin bindear
			if (results.get(row).get(varNames.get(i)) != null)
				returnThis.add(results.get(row).get(varNames.get(i)).toString());
			else
				returnThis.add("null");
		}
		return returnThis;
	}

	public List<String> getColumn(int column) {
		List<String> returnThis = new ArrayList<String>();
		for (int i = 0; i < results.size(); i++) {
			returnThis.add(results.get(i).get(varNames.get(column)).toString());
		}
		return returnThis;
	}

	public String getColumnName(int column) {
		return varNames.get(column);
	}

	public String getElement(int row, int column) {
		return results.get(row).get(varNames.get(column)).toString();
	}

	public String getElement(int row, String columnName) {
		return results.get(row).get(columnName).toString();
	}

	public static String asJson(List<PprocSelectResult> resultList) {
		JSONArray array = new JSONArray();
		for (PprocSelectResult result : resultList) {
			for (int i = 0; i < result.size(); i++) {
				JSONObject object = new JSONObject();
				for (int j = 0; j < result.getColumnCount(); j++) {
					String key = null;
					switch (result.getColumnName(j)) {
					case "contract":
						key = "contractURI";
						break;
					case "title":
						key = "contractName";
						break;
					case "budgetValue":
						key = "budget";
						break;
					}
					if (result.getRow(i).get(j).indexOf("^^http") > -1)
						object.put(key, result.getRow(i).get(j).substring(0, result.getRow(i).get(j).indexOf("^^http")));
					else
						object.put(key, result.getRow(i).get(j));
				}
				object.put("sparqlEndpoint", result.sparqlEndpoint);
				array.add(object);
			}
		}
		return array.toJSONString();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + Objects.hashCode(this.varNames);
		hash = 17 * hash + Objects.hashCode(this.results);
		hash = 17 * hash + Objects.hashCode(this.sparqlEndpoint);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PprocSelectResult other = (PprocSelectResult) obj;
		if (!Objects.equals(this.varNames, other.varNames)) {
			return false;
		}
		if (!Objects.equals(this.results, other.results)) {
			return false;
		}
		if (!Objects.equals(this.sparqlEndpoint, other.sparqlEndpoint)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			if (i != 0)
				sb.append("\n");
			// sb.append(getRow(i).get(0));
			for (int j = 0; j < getColumnCount(); j++) {
				sb.append("[prop=" + getColumnName(j) + "");
				sb.append(",value=" + getRow(i).get(j) + "]");
			}
		}
		return sb.toString();
	}
}
