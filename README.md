pproc-ws
========

Web service to retrieve structured data published in SPARQL endpoint of public contracts without the needs of knowing the SPARQL language. The information of the SPARQL endpoint have to follow the specification of [PPROC ontology](http://contsem.unizar.es).

The web service, deployed at /pproc-ws, have two calls: **facetQuery** and **contractQuery**.

## facetQuery call
Entry point to facet-query the public contracts data. 
### facetQuery input
This call responds to JSON format inputs with the following structure:
```
{
  "object_text" : "agua potable",
  "object_cpv" : "43324100",
  "budget" : "75000.0 95000.0",
  "tenderState" : "tenderSubmission_phase",
  "contractingAuthority" : "Zaragoza ayuntamiento",
  "supplier_text" : "urbanco",
  "supplier_cif" : "A-50105667",
  "tenderStartDate" : "2014-11-04 2014-11-04",
  "tenderNoticeDate" : "2014-11-01 2014-11-15",
  "tenderDeadline" : "2014-09-01 2014-11-15",
  "awardDate" : "2014-10-01 2014-11-15",
  "formalizedDate" : "2014-10-02 2014-10-02",
  "procedureType" : "RegularOpen"
}
```
The next rules applies:
- Case sensitive
- CPV code does not support checksum
- **object_text**, **contractingAuthority** and date facets splits words/dates with an space
- **budget** is "minBudget maxBudget", setting 0 will search for maximum and minimum amounts
- **tenderState** can have the following closed values:
  - *start_phase* filters contracts that:
    - pproc:tenderDossierStartDate < now
    - without pproc:TenderNotice
    - without pc:Tender
  - *tenderSubmission_phase* filters contracts that:
    - pproc:noticeDate of a pproc:TenderNotice < now
    - without pc:Tender
  - *awardDecision_phase* filters contracts that:
    - pproc:tenderDeadline < now
    - now < pproc:awardDate
    - without pproc:AwardedTender
  - *execution_phase* filters contracts that:
    - pproc:awardDate < now
    - a pproc:FormalizedTender exists
    - without pc:actualEndDate
  - *end_phase* filters contracts that: (not yet implemented)
    - pc:actualEndDate exists
- **procedureType** can have the following closed values:
  - *Minor*
  - *CompetitiveDialogue*
  - *Negoatiated*
  - *RegularOpen*
  - *Restricted*
  - *SimpleOpen*
### facetQuery output
The call answers with a JSON list with the following structure:
```
[
  {
    "contractUri" : "http://www.zaragoza.es/api/recurso/sector-publico/contrato/0608354-06",
    "sparqlEndpoint" : "http://datos.zaragoza.es/sparql",
    "contractName" : "Equipamiento de las piscinas del Actur",
    "budget" : "3650.55"
  },
  {
    "contractUri" : "http://www.zaragoza.es/api/recurso/sector-publico/contrato/0608353-06",
    "sparqlEndpoint" : "http://datos.zaragoza.es/sparql",
    "contractName" : "Reemplazo y mantenimiento de los extintores del ayuntamiento",
    "budget" : "6100.50"
  },
  ...
]
```
## contractQuery call
Method that provides all the information about one contract in JSON-LD format.
### contractQuery input
This call responds to JSON format inputs with the following structure:
```
{
  "contractUri" : "http://www.zaragoza.es/api/recurso/sector-publico/contrato/0608354-06",
  "sparqlEndpoint" : "http://datos.zaragoza.es/sparql"
}
```
Note that one could use an element from the JSON list returned from **facetQuery**, as **contractName** and **budget** will be ignored in this call.
### contractQuery output
The call answers with a JSON-LD with all the information found about the given contract. The triples in the JSON-LD are not organized or sorted in any way, we recommend to use the [JSON-LD javascript library](http://json-ld.org/) to structure it the way you need.
You could expect to have [something like this](https://www.dropbox.com/s/9qc0rffv85mnbo8/contrato_0874341-13.jsonld?dl=0) as a JSON- LD response.

## TODO
- Include lucene-like cache
- Include *end_phase* as a valid value to **tenderState** facet
