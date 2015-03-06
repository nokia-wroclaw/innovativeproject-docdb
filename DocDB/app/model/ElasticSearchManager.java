package model;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;

import java.util.ArrayList;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchManager {

	/*
	 * Method that put json file on server
	 */
	public void insert(Client client, XContentBuilder json, String index, String type) {
		try {
			if (client != null && json != null && index != null && type != null)
				client.prepareIndex(index, type).setSource(json).execute().actionGet();
			else
				System.out.println("Problem with insert()");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ArrayList<String>> search(Client client, String content, String index, String type,
			String fieldName, Boolean limit) {

		int searchLimit = limit ? 9 : Integer.MAX_VALUE;

		MatchQueryBuilder query = new MatchQueryBuilder(content, fieldName);
		SearchResponse response = performSearch(client, index, type, searchLimit, query);

		// getting search result in form of arrayList
		SearchHit[] resultsArray = response.getHits().getHits();
		int n = resultsArray.length;
		if (n > 0) {
			return searchResult(resultsArray);
		} else {
			if (content.length() > 2) {
				System.out.println("Prefix search");
				// normal search didn't found anything, proceed prefix search
				MatchQueryBuilder prefixQery = QueryBuilders.matchQuery(content, fieldName).type(
						MatchQueryBuilder.Type.PHRASE_PREFIX);

				SearchResponse prefixResponse = performSearch(client, index, type, searchLimit, prefixQery);
				resultsArray = prefixResponse.getHits().getHits();
				n = resultsArray.length;
				if (n > 0) // if prefix search found sth
					return searchResult(resultsArray);
			}
			return null;
		}
	}

	private SearchResponse performSearch(Client client, String index, String type, int searchLimit,
			MatchQueryBuilder query) {
		// @formatter:off
		SearchResponse response = client.prepareSearch(index)
				.setTypes(type)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(query)
				.setFrom(0)
				.setSize(searchLimit)
				.setExplain(true)
				.setHighlighterTagsSchema("default")
				.addHighlightedField("content", 80, 1)
				.execute().actionGet();
		// @formatter:on
		return response;
	}

	private ArrayList<ArrayList<String>> searchResult(SearchHit[] resultsArray) {

		ArrayList<ArrayList<String>> resultArray = new ArrayList<ArrayList<String>>();

		for (SearchHit hit : resultsArray) {
			ArrayList<String> actualResult = new ArrayList<String>();
			// instead of returning content, we return already highlighted text
			actualResult.add((hit.getHighlightFields().get("content").getFragments()[0]).toString());
			actualResult.add((String) hit.getId());
			resultArray.add(actualResult);
		}

		return resultArray;
	}

	public XContentBuilder putJsonDocument(String content) {
		if (!content.isEmpty()) {
			XContentBuilder builder = null;
			try {
				builder = jsonBuilder().startObject().field("content", content).endObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder;
		} else
			return null;
	}
}
