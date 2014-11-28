package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchManager {

	/*
	 * Method that put json file on server
	 */
	public void insert(Client client, XContentBuilder json, String index,
			String type) {
		try{
		if (client != null && json != null && index != null && type != null)
			client.prepareIndex(index, type).setSource(json).execute()
					.actionGet();
		else
			System.out.println("Problem with insert()");

		}catch (Exception e ){System.out.println("Try");}
		}

	public ArrayList<ArrayList<String>> search(Client client, String content,
			String index, String type) {
		String[] fieldNames = { "title", "content", "author", "size", "tags" };
		return search(client, content, index, type, fieldNames);

	}

	public ArrayList<ArrayList<String>> search(Client client, String content,
			String index, String type, String[] fieldNames) {

		// creating query to find out if any of files on server contain search
		// value

		QueryBuilder qb = QueryBuilders.matchQuery("content", content);
		// String[] fieldNames = { "title", "content", "author", "size", "tags"
		// }; // "postDate",
		// "size"};

		MultiMatchQueryBuilder qb3 = new MultiMatchQueryBuilder(content,
				fieldNames);

		// proceed search with query created above
		SearchResponse response = client.prepareSearch(index).setTypes(type)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(qb3) // Query
				.execute().actionGet();

		// getting search result in form of array
		SearchHit[] resultsArray = response.getHits().getHits();
		int n = resultsArray.length;
		if (n > 0) {
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
				temp.addAll((ArrayList) result.get("tags"));
				resultArray.add(temp);
				// System.out.println(hit.getId());
				System.out.println(result.get("tags"));
			}
			return resultArray;
		} else
			// if search is empty then return null
			return null;
	}

	public XContentBuilder putJsonDocument(ArrayList<String> parsedFile,
			ArrayList<String> tags) {
		if (parsedFile.isEmpty() == false) {
			XContentBuilder builder = null;
			try {
				Date postDate = new Date();

				builder = jsonBuilder().startObject()
						.field("title", parsedFile.get(0))
						.field("author", parsedFile.get(1))
						.field("content", parsedFile.get(2))
						.field("path", parsedFile.get(3))
						.field("size", parsedFile.get(4))
						.field("MD5", parsedFile.get(5))
						.field("postDate", postDate).field("tags", tags)
						.endObject();
				// System.out.println(builder.string());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return builder;
		} else
			return null;
	}
}
