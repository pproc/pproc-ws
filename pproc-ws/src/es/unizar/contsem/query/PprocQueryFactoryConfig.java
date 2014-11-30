package es.unizar.contsem.query;

public class PprocQueryFactoryConfig {

	private String endpoint;

	public PprocQueryFactoryConfig() {
		this.endpoint = "http://datos.zaragoza.es/sparql";
		// this.endpoint = "http://www.dphuesca.es/sparql";
	}

	public PprocQueryFactoryConfig(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getSparqlEndpoint() {
		return endpoint;
	}

}
