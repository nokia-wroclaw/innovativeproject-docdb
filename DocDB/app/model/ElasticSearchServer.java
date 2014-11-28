package model;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class ElasticSearchServer {
	public Node node;
	public Client client;
	public ElasticSearchManager elasticSearch;

	// on startup
	public ElasticSearchServer() {
		node = nodeBuilder().node();
		client = node.client();
		elasticSearch = new ElasticSearchManager();
	}

	public void closeNode() {
		node.stop();
	}

}
