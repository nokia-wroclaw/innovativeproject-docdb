package model;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class ElasticSearchServer {
	Node node;
	Client client;

	// on startup
	public ElasticSearchServer() {
		node = nodeBuilder().node();
		client = node.client();
	}

	public void closeNode() {
		node.stop();
	}

}
