package model;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MultiMatchQuery.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import ucar.units.Prefix;

import com.google.common.collect.ObjectArrays;

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

	public ArrayList<ArrayList<String>> search(Client client, String content, String index, String type, Boolean limit) {
		String[] fieldNames = { "title", "content", "author", "size", "tags" };
		return search(client, content, index, type, fieldNames, limit);

	}

	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<String>> search(Client client, String content, String index, String type,
			String[] fieldNames, Boolean limit) {

		// creating query to find out if any of files on server contain search
		// value

		// QueryBuilder qb = QueryBuilders.matchQuery("content", content);

		MultiMatchQueryBuilder qb3 = new MultiMatchQueryBuilder(content, fieldNames);
		int resultSize = 9;
		if (limit == true)
			resultSize = Integer.MAX_VALUE;

		// proceed search with query created above
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(qb3).setSize(resultSize)
				.addHighlightedField(content) // Query
				.execute().actionGet();

		// getting search result in form of array
		SearchHit[] resultsArray = response.getHits().getHits();
		int n = resultsArray.length;
		if (n > 0) {
			return searchResult(resultsArray);
		} else {
			// if normal search didn't found anything then proceed prefix search
			MultiMatchQueryBuilder prefixQ = QueryBuilders.multiMatchQuery(content, fieldNames).type(
					MatchQueryBuilder.Type.PHRASE_PREFIX);
			response = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(prefixQ).setSize(resultSize).addHighlightedField(content) // Query
					.execute().actionGet();
			resultsArray = response.getHits().getHits();
			n = resultsArray.length;
			if (n > 0) {
				System.out.println("Prefix search");
				return searchResult(resultsArray);
			} else {
				// if search is empty then return null
				return null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ArrayList<ArrayList<String>> searchResult(SearchHit[] resultsArray) {
		// two dimensional array that will contain all titles and paths
		// to files that satisfied search conditions. In following form:
		// resultArray[i][0] = title; resultArray[i][1] = path

		ArrayList<ArrayList<String>> resultArray = new ArrayList<ArrayList<String>>();

		for (SearchHit hit : resultsArray) {
			Map<String, Object> result = hit.getSource();
			ArrayList<String> temp = new ArrayList<String>();
			temp.add((String) result.get("title"));
			temp.add((String) result.get("path"));
			temp.add((String) result.get("size"));
			temp.add((String) result.get("content"));
			temp.addAll((ArrayList<String>) result.get("tags"));
			temp.addAll((ArrayList<String>) (result.get("locationCoordinates")));
			resultArray.add(temp);
		}

		return resultArray;
	}

	public XContentBuilder putJsonDocument(ArrayList<String> parsedFile, Set<String> tagList,
			String[] locationCoordinates) {
		if (parsedFile.isEmpty() == false) {
			XContentBuilder builder = null;
			try {
				Date postDate = new Date();
				builder = jsonBuilder().startObject().field("title", parsedFile.get(0))
						.field("author", parsedFile.get(1)).field("content", parsedFile.get(2))
						.field("path", parsedFile.get(3)).field("size", parsedFile.get(4))
						.field("MD5", parsedFile.get(5)).field("postDate", postDate).field("tags", tagList)
						.field("locationCoordinates", locationCoordinates).endObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder;
		} else
			return null;
	}
}
