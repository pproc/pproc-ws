package es.unizar.contsem.query;

import java.io.InputStream;
import java.util.Map;

import es.unizar.contsem.methods.Methods;

public class PprocQueryFactory {

	private final PprocQueryFactoryConfig config;

	private final String PREFIXES = "PREFIX pproc: <http://contsem.unizar.es/def/sector-publico/pproc#> \n"
			+ "PREFIX dcterms: <http://purl.org/dc/terms/> \n" + "PREFIX gr: <http://purl.org/goodrelations/v1#> \n"
			+ "PREFIX s: <http://schema.org/> \n" + "PREFIX org: <http://www.w3.org/ns/org#> \n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX pc: <http://purl.org/procurement/public-contracts#> \n";
	private final String SELECT = "SELECT DISTINCT ";
	private final String WHERE = "WHERE { \n";
	private final String END = "} \n";
	private final int SHORTEST_STRING = 3;

	public PprocQueryFactory() {
		this.config = new PprocQueryFactoryConfig();
	}

	public PprocQueryFactory(PprocQueryFactoryConfig config) {
		this.config = config;
	}

	public PprocQuery getFacetedQuery(Map<String, String> facetMap) {
		String[] altArray;
		String altString;

		// ////////////////////////////
		// SubQuery SELECT
		StringBuilder querySelect = new StringBuilder();
		querySelect.append(SELECT).append("?contract ?title ?budgetValue \n");

		// ////////////////////////////
		// SubQuery WHERE
		StringBuilder queryWhere = new StringBuilder();
		queryWhere.append(WHERE);
		queryWhere.append("?contract a pproc:Contract .\n");

		// ////////////////////////////
		// FACET object_text
		if (facetMap.get("object_text") != null) {
			altArray = facetMap.get("object_text").split(" ");
			queryWhere.append("?contract dcterms:title ?title .\n");
			for (int i = 0; i < altArray.length; i++) {
				// Filtering short strings
				if (altArray[i].length() >= SHORTEST_STRING) {
					queryWhere.append("FILTER regex( str(?title), \"" + altArray[i] + "\", \"i\") \n");
				}
			}
		}

		// ////////////////////////////
		// FACET object_cpv
		if ((altString = facetMap.get("object_cpv")) != null) {
			queryWhere.append("?contract pproc:contractObject ?co .\n");
			queryWhere.append("?co pproc:mainObject <http://purl.org/cpv/2008/code-" + altString + "> .\n");
		}

		// ////////////////////////////
		// FACET budget
		if (facetMap.get("budget") != null) {
			altArray = facetMap.get("budget").split(" ");
			if (!queryWhere.toString().contains("pproc:contractObject"))
				queryWhere.append("?contract pproc:contractObject ?co .\n");
			queryWhere.append("?co pproc:contractEconomicConditions ?cec .\n");
			queryWhere.append("?cec pproc:budgetPrice ?budgetPrice .\n");
			queryWhere.append("?budgetPrice gr:hasCurrencyValue ?budgetValue .\n");
			queryWhere.append("?budgetPrice gr:valueAddedTaxIncluded 1 . \n");
			if (Float.parseFloat(altArray[0]) > 0)
				queryWhere.append("FILTER (?budgetValue > " + altArray[0] + ") \n");
			if (Float.parseFloat(altArray[1]) > 0)
				queryWhere.append("FILTER (?budgetValue < " + altArray[1] + ") \n");
		}

		// ////////////////////////////
		// FACET tenderState
		if ((altString = facetMap.get("tenderState")) != null) {
			switch (altString) {
			case "start_phase":
				queryWhere.append("?contract pproc:contractProcedureSpecifications ?pce .\n");
				queryWhere.append("?pce pproc:tenderDossierStartDate ?startDate .\n");
				queryWhere.append("FILTER (xsd:dateTime(?startDate) <= now() ) \n");
				queryWhere.append("OPTIONAL { ?pce pproc:notice ?notice .\n");
				queryWhere.append("?notice a pproc:ContractNotice .\n");
				queryWhere.append("?notice pproc:noticeDate ?noticeDate } \n");
				queryWhere.append("FILTER ( !bound(?noticeDate) ) \n");
				queryWhere.append("OPTIONAL { ?contract pc:tender ?tender } \n");
				queryWhere.append("FILTER ( !bound(?tender) ) \n");
				break;
			case "tenderSubmissionPhase":
				queryWhere.append("?contract pproc:contractProcedureSpecifications ?pce .\n");
				queryWhere.append("?pce pproc:notice ?notice .\n");
				queryWhere.append("?notice a pproc:ContractNotice .\n");
				queryWhere.append("?notice pproc:noticeDate ?noticeDate .\n");
				queryWhere.append("FILTER (xsd:dateTime(?noticeDate) <= now() ) \n");
				queryWhere.append("OPTIONAL { ?contract pc:tender ?tender } \n");
				queryWhere.append("FILTER ( !bound(?tender) ) \n");
				break;
			case "execution_phase":
				queryWhere.append("?contract pc:tender ?tender .\n");
				queryWhere.append("?tender a pproc:FormalizedTender .\n");
				break;
			case "end_phase":
				break;
			}
		}

		// ////////////////////////////
		// FACET contractingAuthority
		if (facetMap.get("contractingAuthority") != null) {
			altArray = facetMap.get("contractingAuthority").split(" ");
			queryWhere.append("?contract pc:contractingAuthority ?ca .\n");
			queryWhere.append("?ca dcterms:title ?caTitle .\n");
			for (int i = 0; i < altArray.length; i++) {
				// Filtering short strings
				if (altArray[i].length() >= SHORTEST_STRING) {
					queryWhere.append("FILTER regex( str(?caTitle), \"" + altArray[i] + "\", \"i\") \n");
				}
			}
		}

		// ////////////////////////////
		// FACET supplier_text
		if (facetMap.get("supplier_text") != null) {
			altArray = facetMap.get("supplier_text").split(" ");
			queryWhere.append("?contract pc:tender ?tender .\n");
			queryWhere.append("?tender pc:supplier ?supplier .\n");
			queryWhere.append("?supplier s:name ?supplierName .\n");
			for (int i = 0; i < altArray.length; i++) {
				// Filtering short strings
				if (altArray[i].length() >= SHORTEST_STRING) {
					queryWhere.append("FILTER regex( str(?supplierName), \"" + altArray[i] + "\", \"i\") \n");
				}
			}
		}

		// ////////////////////////////
		// FACET supplier_cif
		if ((altString = facetMap.get("supplier_cif")) != null) {
			if (!queryWhere.toString().contains("contractProcedureSpecifications"))
				queryWhere.append("?contract pproc:contractProcedureSpecifications ?pce .\n");
			if (!queryWhere.toString().contains("pc:supplier"))
				queryWhere.append("?tender pc:supplier ?supplier .\n");
			queryWhere.append("?supplier org:identifier ?identifier .\n");
			queryWhere.append("FILTER regex(?identifier, \"" + altString + "\", \"i\") \n");
		}

		// ////////////////////////////
		// FACET tenderStartDate
		if (facetMap.get("tenderStartDate") != null) {
			altArray = facetMap.get("tenderStartDate").split(" ");
			if (!queryWhere.toString().contains("pproc:contractProcedureSpecifications"))
				queryWhere.append("?contract pproc:contractProcedureSpecifications ?pce .\n");
			if (!queryWhere.toString().contains("pproc:tenderDossierStartDate"))
				queryWhere.append("?pce pproc:tenderDossierStartDate ?startDate .\n");
			queryWhere.append("FILTER (xsd:dateTime(?startDate) >= xsd:dateTime(str(\"" + altArray[0] + "\"))) \n");
			queryWhere.append("FILTER (xsd:dateTime(?startDate) <= xsd:dateTime(str(\"" + altArray[1] + "\"))) \n");
		}

		// ////////////////////////////
		// FACET tenderNoticeDate
		if (facetMap.get("tenderNoticeDate") != null) {
			altArray = facetMap.get("tenderNoticeDate").split(" ");
			if (!queryWhere.toString().contains("pproc:contractProcedureSpecifications"))
				queryWhere.append("?contract pproc:contractProcedureSpecifications ?pce .\n");
			if (!queryWhere.toString().contains("pproc:notice"))
				queryWhere.append("?pce pproc:notice ?notice .\n");
			if (!queryWhere.toString().contains("pproc:ContractNotice"))
				queryWhere.append("?notice a pproc:ContractNotice .\n");
			if (!queryWhere.toString().contains("?pproc:noticeDate"))
				queryWhere.append("?notice pproc:noticeDate ?noticeDate .\n");
			queryWhere.append("FILTER (xsd:dateTime(?noticeDate) >= xsd:dateTime(str(\"" + altArray[0] + "\"))) \n");
			queryWhere.append("FILTER (xsd:dateTime(?noticeDate) <= xsd:dateTime(str(\"" + altArray[1] + "\"))) \n");
		}

		// ////////////////////////////
		// FACET tenderDeadline
		if (facetMap.get("tenderDeadline") != null) {
			altArray = facetMap.get("tenderDeadline").split(" ");
			if (!queryWhere.toString().contains("contractProcedureSpecifications"))
				queryWhere.append("?contract pproc:contractProcedureSpecifications ?pce .\n");
			queryWhere.append("?pce pproc:tenderDeadline ?deadlineDate .\n");
			queryWhere.append("FILTER (xsd:dateTime(?deadlineDate) >= xsd:dateTime(str(\"" + altArray[0] + "\"))) \n");
			queryWhere.append("FILTER (xsd:dateTime(?deadlineDate) <= xsd:dateTime(str(\"" + altArray[1] + "\"))) \n");
		}

		// ////////////////////////////
		// FACET awardDate
		if (facetMap.get("awardDate") != null) {
			altArray = facetMap.get("awardDate").split(" ");
			if (!queryWhere.toString().contains("pc:tender"))
				queryWhere.append("?contract pc:tender ?tender .\n");
			if (!queryWhere.toString().contains("?awardDate"))
				queryWhere.append("?tender pproc:awardDate ?awardDate .\n");
			queryWhere.append("FILTER (xsd:dateTime(?awardDate) >= xsd:dateTime(str(\"" + altArray[0] + "\"))) \n");
			queryWhere.append("FILTER (xsd:dateTime(?awardDate) <= xsd:dateTime(str(\"" + altArray[1] + "\"))) \n");
		}

		// ////////////////////////////
		// FACET formalizedDate
		if (facetMap.get("formalizedDate") != null) {
			altArray = facetMap.get("formalizedDate").split(" ");
			if (!queryWhere.toString().contains("pc:tender"))
				queryWhere.append("?contract pc:tender ?tender .\n");
			if (!queryWhere.toString().contains("pproc:formalizedDate"))
				queryWhere.append("?tender pproc:formalizedDate ?formalizedDate .\n");
			queryWhere
					.append("FILTER (xsd:dateTime(?formalizedDate) >= xsd:dateTime(str(\"" + altArray[0] + "\"))) \n");
			queryWhere
					.append("FILTER (xsd:dateTime(?formalizedDate) <= xsd:dateTime(str(\"" + altArray[1] + "\"))) \n");
		}

		// ////////////////////////////
		// FACET procedureType
		if ((altString = facetMap.get("procedureType")) != null) {
			if (!queryWhere.toString().contains("contractProcedureSpecifications"))
				queryWhere.append("?contract pproc:contractProcedureSpecifications ?pce .\n");

			queryWhere.append("?pce pproc:procedureType pproc:" + altString + " .\n");
		}

		// ////////////////////////////
		// Adding OPTIONAL title and budget binds
		if (facetMap.get("object_text") == null)
			queryWhere.append("OPTIONAL { ?contract dcterms:title ?title } \n");
		if (facetMap.get("budget") == null && facetMap.get("object_cpv") == null) {
			queryWhere.append("OPTIONAL { ?contract pproc:contractObject ?co .\n");
			queryWhere.append("?co pproc:contractEconomicConditions ?cec .\n");
			queryWhere.append("?cec pproc:budgetPrice ?budgetPrice .\n");
			queryWhere.append("?budgetPrice gr:hasCurrencyValue ?budgetValue .\n");
			queryWhere.append("?budgetPrice gr:valueAddedTaxIncluded 1 } \n");
		} else if (facetMap.get("budget") == null && facetMap.get("object_cpv") != null) {
			queryWhere.append("OPTIONAL { ?co pproc:contractEconomicConditions ?cec .\n");
			queryWhere.append("?cec pproc:budgetPrice ?budgetPrice .\n");
			queryWhere.append("?budgetPrice gr:hasCurrencyValue ?budgetValue .\n");
			queryWhere.append("?budgetPrice gr:valueAddedTaxIncluded 1 } \n");
		}

		// ////////////////////////////
		// Create final query
		String query = PREFIXES + querySelect.toString() + queryWhere.toString() + END;

		return new PprocQuery(query.toString(), config);
	}

	public PprocQuery getContractQuery(String contractUri) {
		InputStream inputStream = getClass().getResourceAsStream("/es/unizar/contsem/query/construct/constructQuery.txt");
		String string = Methods.getStringFromInputStream(inputStream);
		return new PprocQuery(string.replaceAll("contractUri", contractUri), config);
	}
	
	public PprocQuery getContractQuery2(String contractUri) {
		InputStream inputStream = getClass().getResourceAsStream("/es/unizar/contsem/test/constructQuery01.txt");
		String string = Methods.getStringFromInputStream(inputStream);
		return new PprocQuery(string.replaceAll("contractUri", contractUri), config);
	}

}
