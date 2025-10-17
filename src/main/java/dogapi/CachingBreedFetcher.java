package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {

    private final BreedFetcher fetcher;
    private final Map<String, List<String>> cache = new HashMap<>();
    private int callsMade = 0;
    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        if (cache.containsKey(breed)) {
            return cache.get(breed);
        }

        // Cache miss: delegate to underlying fetcher and count the call
        callsMade++;
        List<String> result = fetcher.getSubBreeds(breed); // may throw BreedNotFoundException

        // On success, cache an unmodifiable copy so external callers can't mutate our cache
        List<String> immutable = Collections.unmodifiableList(new ArrayList<>(result));
        cache.put(breed, immutable);
        return immutable;
    }

    public int getCallsMade() {
        return callsMade;
    }
}