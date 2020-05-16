package org.openstreetmap.josm.plugins.graphview.core.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * TagGroup that uses a key-value-Map to store tags
 */
public class MapBasedTagGroup implements TagGroup {

	private final Map<String, String> tagMap;

	/**
	 * @param tagMap  map from keys to values; != null;
	 *                must not be modified after being used as parameter
	 */
	public MapBasedTagGroup(Map<String, String> tagMap) {
		if (tagMap == null) {
			throw new IllegalArgumentException();
		}

		this.tagMap = tagMap;
	}

	/**
	 * @param tags  tags to add to the group; != null, each != null
	 */
	public MapBasedTagGroup(Iterable<Tag> tags) {
		if (tags == null) {
			throw new IllegalArgumentException();
		}
		this.tagMap = new HashMap<String, String>();
		for (Tag tag : tags) {
			if (tag == null) {
				throw new IllegalArgumentException();
			} else {
				this.tagMap.put(tag.key, tag.value);
			}
		}
	}

	/**
	 * @param tags  tags to add to the group; each != null
	 */
	public MapBasedTagGroup(Tag... tags) {
		this.tagMap = new HashMap<String, String>(tags.length);
		for (Tag tag : tags) {
			if (tag == null) {
				throw new IllegalArgumentException();
			} else {
				this.tagMap.put(tag.key, tag.value);
			}
		}
	}

	@Override
	public String getValue(String key) {
		assert key != null;
		return tagMap.get(key);
	}

	@Override
	public boolean containsKey(String key) {
		assert key != null;
		return tagMap.containsKey(key);
	}

	@Override
	public boolean containsAnyKey(Iterable<String> keys) {
		for (String key : keys) {
			if (this.containsKey(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(String value) {
		assert value != null;
		return tagMap.containsValue(value);
	}

	@Override
	public boolean containsAnyValue(Iterable<String> values) {
		for (String value : values) {
			if (this.containsValue(value)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean contains(Tag tag) {
		assert tag != null;
		return tag.value.equals(tagMap.get(tag.key));
	}

	@Override
	public boolean containsAny(Iterable<Tag> tags) {
		for (Tag tag : tags) {
			if (this.contains(tag)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(String key, String value) {
		assert key != null;
		assert value != null;
		return value.equals(tagMap.get(key));
	}

	@Override
	public boolean containsAny( Iterable<String> keys, String value) {
		for (String key : keys) {
			if (this.contains(key, value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAny(Iterable<String> keys, Iterable<String> values) {
		for (String key : keys) {
			if (this.containsAny(key, values)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAny(String key, Iterable<String> values) {
		for (String value : values) {
			if (this.contains(key, value)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int size() {
		return tagMap.size();
	}

	@Override
	public boolean isEmpty() {
		return tagMap.isEmpty();
	}

	/**
	 * returns an Iterator providing access to all Tags.
	 * The Iterator does not support the {@link Iterator#remove()} method.
	 */
	@Override
	public Iterator<Tag> iterator() {

		Collection<Tag> tagCollection = new LinkedList<Tag>();

		for (String key : tagMap.keySet()) {
			tagCollection.add(new Tag(key, tagMap.get(key)));
		}

		return Collections.unmodifiableCollection(tagCollection).iterator();

	}

	@Override
	public String toString() {
		return tagMap.toString();
	}

}
