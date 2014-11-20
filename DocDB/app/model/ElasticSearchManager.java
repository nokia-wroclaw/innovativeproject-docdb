package model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchManager {

	/*
	 * Method that put json file on server
	 */
	public void insert(Client client, Map json, String index, String type) {
		if (client != null && json != null && index != null && type != null)
			client.prepareIndex(index, type).setSource(json).execute()
					.actionGet();
		else
			System.out.println("Problem with insert()");
	}

	public String[][] search(Client client, String content, String index,
			String type) {

		// creating query to find out if any of files on server contain search
		// value
		try {
			QueryBuilder qb = QueryBuilders.matchQuery("content", content);
			String[] fieldNames = { "title", "content", "author", "size" }; // "postDate",
																			// "size"};

			MultiMatchQueryBuilder qb3 = new MultiMatchQueryBuilder(content,
					fieldNames);

			// proceed search with query created above
			SearchResponse response = client.prepareSearch(index)
					.setTypes(type)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(qb3) // Query
					.execute().actionGet();

			// getting search result in form of array
			SearchHit[] results = response.getHits().getHits();
			int n = results.length;
			if (n > 0) {
				// two dimensional array that will contain all titles and paths
				// to
				// files that
				// satisfied search conditions. In following form:
				// resultArray[i][0] = title; resultArray[i][1] = path
				String[][] resultArray = new String[n][4];
				int iterator = 0;
				for (SearchHit hit : results) {
					Map<String, Object> result = hit.getSource();
					resultArray[iterator][0] = (String) result.get("title");
					resultArray[iterator][1] = (String) result.get("path");
					resultArray[iterator][2] = (String) result.get("size");
					resultArray[iterator][3] = (String) result.get("content");
					iterator++;
				}
				return resultArray;

			} else
				// if search is empty then return null
				return null;
		} catch (Exception e) {
			return null;
		}
	}

	public Map<String, Object> putJsonDocument(String[] json) {
		if (json != null) {
			Map<String, Object> jsonDocument = new HashMap<String, Object>();
			Date postDate = new Date();

			jsonDocument.put("title", json[0]);
			jsonDocument.put("author", json[1]);
			jsonDocument.put("content", json[2]);
			jsonDocument.put("path", json[3]);
			jsonDocument.put("postDate", postDate);
			jsonDocument.put("size", json[4]);

			return jsonDocument;
		} else
			return null;
	}
}
