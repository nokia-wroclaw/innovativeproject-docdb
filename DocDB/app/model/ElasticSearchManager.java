package model;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchManager {

	public void insert(Client client, Map json, String index, String type) {

		client.prepareIndex(index, type).setSource(json).execute().actionGet();
	}

	public String[][] search(Client client, String content, String index,
			String type) {

		QueryBuilder qb = QueryBuilders.matchQuery("content", content);
		// setFrom(0).setSize(60).setExplain(true)
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(qb) // Query
				.execute().actionGet();

		SearchHit[] results = response.getHits().getHits();
		int n = results.length;
		if (n > 0) {
			String[][] resultArray = new String[n][2];

			int iterator = 0;
			for (SearchHit hit : results) {
				Map<String, Object> result = hit.getSource();
				resultArray[iterator][0] = (String) result.get("title");
				resultArray[iterator][1] = (String) result.get("path");
				iterator++;
			}
			return resultArray;
		} else {
			return null;
		}
	}

	public Map<String, Object> putJsonDocument(String[] json) {

		Map<String, Object> jsonDocument = new HashMap<String, Object>();
		Date postDate = new Date();

		jsonDocument.put("title", json[0]);
		jsonDocument.put("author", json[1]);
		jsonDocument.put("content", json[2]);
		jsonDocument.put("path", json[3]);
		jsonDocument.put("postDate", postDate);

		return jsonDocument;
	}
}
