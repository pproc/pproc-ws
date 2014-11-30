package es.unizar.contsem.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import es.unizar.contsem.methods.Methods;

@Path("/facetQuery")
public class WS_FacetQuery {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String test(String json_string) {
		try {
			String output = Methods.facetQuery(json_string);
			System.out.println(output);
			return output;
		} catch (Exception e) {
			e.printStackTrace();
			return "{\"error\":\"error\"}";
		}
	}

}
