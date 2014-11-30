package es.unizar.contsem.query;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.resultset.ResultSetException;

import es.unizar.contsem.query.construct.PprocConstructResult;
import es.unizar.contsem.query.select.PprocSelectResult;

public class PprocQuery {

	private static int NUMBER_OF_QUERIES = 0;
	private String queryString;
	private PprocQueryFactoryConfig config;
	private Query query;
	private QueryExecution qexec;

	protected PprocQuery(String queryString, PprocQueryFactoryConfig config) {
		this.query = QueryFactory.create(queryString);
		this.qexec = QueryExecutionFactory.sparqlService(config.getSparqlEndpoint(), query);
		this.queryString = queryString;
		this.config = config;
	}

	@Override
	public String toString() {
		return query.toString();
	}

	public String getSPARQLstring() {
		return queryString;
	}

	public PprocSelectResult doSelect() throws ResultSetException {
		NUMBER_OF_QUERIES++;
		int i = 0;
		PprocSelectResult rs = null;
		while (i < 3 && rs == null) {
			i++;
			try {
				ResultSet resultSet = qexec.execSelect();
				rs = new PprocSelectResult(resultSet, config.getSparqlEndpoint(), this.getSPARQLstring());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		qexec.close();
		return rs;
	}

	public PprocConstructResult doConstruct() {
		NUMBER_OF_QUERIES++;
		int i = 0;
		PprocConstructResult rs = null;
		while (i < 3 && rs == null) {
			i++;
			try {
				Model model = qexec.execConstruct();
				rs = new PprocConstructResult(model, config.getSparqlEndpoint());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		qexec.close();
		return rs;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + (this.queryString != null ? this.queryString.hashCode() : 0);
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
		final PprocQuery other = (PprocQuery) obj;
		if ((this.queryString == null) ? (other.queryString != null) : !this.queryString.equals(other.queryString)) {
			return false;
		}
		return true;
	}
}
